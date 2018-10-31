package behavior.plugin.analyzer;

import java.util.Arrays;

import ij.process.ImageProcessor;

import behavior.image.process.Fusion;
import behavior.image.process.ImageManager;
import behavior.image.process.LDTracer;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.LDParameter;

public final class LDAnalyzer extends Analyzer{
	/*getBinResult(int) メソッドで返却するデータを指定するためのフィールド*/
	public static final int DISTANCE_DARK = 1;
	public static final int DISTANCE_LIGHT = 2;
	public static final int TIME_DARK = 3;
	public static final int TIME_LIGHT = 4;
	public static final int TRANSITION = 5;

	private final int DARK = 0;
	private final int LIGHT = 1;


	private ImageManager[] imgManager = new ImageManager[2];

	private int[][][] xyaData = new int[2][][];	//X座標、Y座標、面積、の順にデータが入るところ。xyaData[LD][？個目のかたまり][データの種類]
	private int[][] prevXyaData = new int[2][];

	private double latency = 0;
	private int prevLD, currentLD = DARK, totalTransition = 0;
	private int[] width = new int[2] , height = new int[2];
	private double[][] binDistance, binTime;
	private double[] totalTime = new double[2], currentDistance = new double[2];
	private double[] totalDistance = new double[2];
	private int[] binTransition;

	public LDAnalyzer(ImageProcessor backIp){
		ImageProcessor[] backIpLD = Fusion.split(backIp, backIp.getWidth() / 2);
		setSize(backIpLD);
		imgManager[DARK] = new ImageManager(backIpLD[DARK].duplicate()); //Dark 用の ImageManager
		imgManager[LIGHT] = new ImageManager(backIpLD[LIGHT].duplicate()); //Light 用の ImageManager
		tracer = new LDTracer(backIp);
		initializeArrays();
	}

	private void setSize(ImageProcessor[] LDIp){
		width[DARK] = LDIp[DARK].getWidth();
		height[DARK] = LDIp[DARK].getHeight();
		width[LIGHT] = LDIp[LIGHT].getWidth();
		height[LIGHT] = LDIp[LIGHT].getHeight();
	}

	protected void initializeArrays(){
		binDistance = new double[2][binLength];
		Arrays.fill(binDistance[DARK], 0.0);
		Arrays.fill(binDistance[LIGHT], 0.0);
		binTime = new double[2][binLength];
		Arrays.fill(binTime[DARK], 0.0);
		Arrays.fill(binTime[LIGHT], 0.0);
		Arrays.fill(totalTime, 0.0);
		Arrays.fill(currentDistance, 0.0);
		Arrays.fill(totalDistance, 0.0);
		binTransition = new int[binLength];
		Arrays.fill(binTransition, 0);
		mouseExistsCount = 0;
	}

	public void analyzeImage(ImageProcessor currentIp){
		boolean mouseExists = false;
		ImageProcessor[] currentIpLD = Fusion.split(currentIp, currentIp.getWidth() / 2);
		for(int LD = DARK; LD <= LIGHT; LD++){
			imgManager[LD].setCurrentIp(currentIpLD[LD].duplicate());
			imgManager[LD].subtract();
			imgManager[LD].applyThreshold(ImageManager.SUBTRACT_IMG);
			imgManager[LD].dilate(ImageManager.SUBTRACT_IMG);
			xyaData[LD] = imgManager[LD].analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData[LD]);
			if(xyaData[LD][0][EXIST_FLAG] > 0)
				mouseExists = true;
		}
		if(mouseExists){
			mouseExistsCount++;
		}else if(mouseExistsCount < 20){
			mouseExistsCount = 0;
		}
	}

	public boolean startAnalyze(int cageNo) {
		return (mouseExistsCount >= 20);
	}

	public void calculate(int currentSlice){
		modifyXY();
		if(currentSlice > 0){	//この計算だと、最初の画像は解析の対象に入れないことになるが、behavior3 を引き継いで‥
			currentBinSlice++;
			setCurrentLD();
			if(latency == 0 && currentLD == LIGHT)
				latency = currentSlice / Parameter.getInt(LDParameter.rate);
			if(currentLD != prevLD){
				binTransition[currentBin] += 1;
				totalTransition += 1;
			}
			for(int LD = DARK; LD <= LIGHT; LD++){
				setImageSize(width[LD], height[LD]);
				currentDistance[LD] = getDistance(xyaData[LD][0][X_CENTER], xyaData[LD][0][Y_CENTER], prevXyaData[LD][X_CENTER], prevXyaData[LD][Y_CENTER]);
				binDistance[LD][currentBin] += currentDistance[LD];
				totalDistance[LD] += currentDistance[LD];
				if(xyaData[LD][0][EXIST_FLAG] > 0 || currentLD == LD){
					double time = (double)atCenter(xyaData[LD][0][X_CENTER], xyaData[LD][0][Y_CENTER], 100);
					binTime[LD][currentBin] += time / Parameter.getInt(LDParameter.rate);
					totalTime[LD] += time / Parameter.getInt(LDParameter.rate);
				}
			}
			
			writeTrace();
		}
		((LDTracer)tracer).setPrevXY(xyaData[DARK][0][X_CENTER], xyaData[DARK][0][Y_CENTER], xyaData[LIGHT][0][X_CENTER] + width[DARK], xyaData[LIGHT][0][Y_CENTER]);

		prevLD = currentLD;
		prevXyaData[DARK] = xyaData[DARK][0];
		prevXyaData[LIGHT] = xyaData[LIGHT][0];
	}

	/**************
	 *現在マウスが Light-Dark どちらにいるかを判断して決める
	 *判断できなかったら前回と同じほうにいるとする
	 ***************/
	private void setCurrentLD(){
		if(xyaData[DARK][0][EXIST_FLAG] > 0 && xyaData[LIGHT][0][EXIST_FLAG] == 0)
			currentLD = DARK;
		else if(xyaData[DARK][0][EXIST_FLAG] == 0 && xyaData[LIGHT][0][EXIST_FLAG] > 0)
			currentLD = LIGHT;
	}

	/*********
	画像から取得された XY 座標を、適正な値に修正して、メンバ X, Y に代入する
	画像から取得された時点の値は、画像融合前であり、実際の位置とはずれがある。また、
	Light Dark 双方にパーティクルが認識された場合は、適当な修正を行う必要が出てくる。
	 **********/
	private void modifyXY(){
		int doorX = width[DARK];
		int doorY = height[DARK] / 2;
		if(xyaData[DARK][0][EXIST_FLAG] == 0 && xyaData[LIGHT][0][EXIST_FLAG] != 0){	//Light だけに認識されているときは Dark はドアの位置に
			xyaData[DARK][0][X_CENTER] = doorX;
			xyaData[DARK][0][Y_CENTER] = doorY;
		}
		if(xyaData[LIGHT][0][EXIST_FLAG] == 0 && xyaData[DARK][0][EXIST_FLAG] != 0){
			xyaData[LIGHT][0][X_CENTER] = 0;
			xyaData[LIGHT][0][Y_CENTER] = doorY;
		}
	}

	/**	
	 始点(sx,sy)から終点(dx,dy)までの距離を計算する。結果は実寸(cm)で返す。
	 */
	/*
	protected double getDistance(int sx,int sy,int dx,int dy){
		if(sx + sy == 0 || dx + dy == 0)
			return 0.0;
		if(pixelWidth == 0 || pixelHeight == 0)
			throw new IllegalStateException("Analyze.setImageSize(int, int) was not called");
		float distX = sx - dx;
		float distY = sy - dy;
		double lengthPerPixelHor = (double)Parameter.getInt(Parameter.frameWidth) / pixelWidth;
		double lengthPerPixelVer = (double)Parameter.getInt(Parameter.frameHeight) / pixelHeight;
		double distance = Math.sqrt(Math.pow(distX * lengthPerPixelHor, 2) + Math.pow(distY * lengthPerPixelVer, 2));
		return distance;
	}
	*/

	private void writeTrace(){
		((LDTracer)tracer).writeTraceMouseA(xyaData[DARK][0][X_CENTER], xyaData[DARK][0][Y_CENTER]);
		((LDTracer)tracer).writeTraceMouseB(xyaData[LIGHT][0][X_CENTER] + width[DARK], xyaData[LIGHT][0][Y_CENTER]);
	}

	public void nextBin(){
		currentBin++;
		currentBinSlice = 0;
	}

	public ImageProcessor getSubtractImage(){
		ImageProcessor[] subtractIp = new ImageProcessor[2];
		subtractIp[DARK] = imgManager[DARK].getIp(ImageManager.SUBTRACT_IMG);
		subtractIp[LIGHT] = imgManager[LIGHT].getIp(ImageManager.SUBTRACT_IMG);
		return Fusion.fusionImage(subtractIp[DARK], subtractIp[LIGHT]);
	}

	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + getElapsedTime(sliceNum) + "\t" + (int)totalDistance[DARK] + "\t" + (int)totalDistance[LIGHT] + "\t" +
		totalTime[DARK] + "\t" + totalTime[LIGHT] + "\t" + totalTransition;
	}

	public String getXY(int sliceNum){
		String xyData = "";
		boolean NP = (xyaData[DARK][0][EXIST_FLAG] == 0 && xyaData[LIGHT][0][EXIST_FLAG] == 0);
		if(xyaData[DARK][0][EXIST_FLAG] != 0 && xyaData[LIGHT][0][EXIST_FLAG] != 0){
			xyData = setXY(sliceNum, DARK, NP);
			xyData += "\n" + setXY(sliceNum, LIGHT, NP);
		}else if(xyaData[LIGHT][0][EXIST_FLAG] != 0){
			xyData = setXY(sliceNum, LIGHT, NP);
		}else{
			xyData = setXY(sliceNum, DARK, NP);
		}
		return xyData;
	}


	private String setXY(int sliceNum, int LD, boolean NP){
		int addWidth = (LD == DARK? 0 : 1) * width[DARK];
		String L_D = (LD == DARK? "D" : "L");
		String area = (NP? "NP" : Integer.toString(xyaData[LD][0][2]));
		String data = sliceNum + "\t" + (xyaData[LD][0][0] + addWidth) + "\t" + xyaData[LD][0][1] + "\t" + area + "\t" + L_D;
		for(int i = 1; i < xyaData[LD].length; i++)
			data += "\n" + sliceNum + "\t" + (xyaData[LD][i][0] + addWidth) + "\t" +
			xyaData[LD][i][1] + "\t" + xyaData[LD][i][2] + "\t" + L_D + "\t" + "f";
		return data;
	}

	public String[] getResults(){
		String[] results = new String[7];
		results[0] = String.valueOf(Math.round(totalDistance[DARK] * 10.0) / 10.0);
		results[1] = String.valueOf(Math.round(totalDistance[LIGHT] * 10.0) / 10.0);
		results[2] = String.valueOf(totalTime[DARK]);
		results[3] = String.valueOf(totalTime[LIGHT]);
		results[4] = String.valueOf(totalTransition);
		results[5] = String.valueOf(Parameter.getInt(LDParameter.duration));
		results[6] = String.valueOf(latency);
		return results;
	}

	public String[] getBinResult(int option){
		int length = binLength;
		String[] result = new String[length];
		for(int bin = 0; bin < length; bin++){
			switch(option){
			case DISTANCE_DARK: result[bin] = String.valueOf(Math.round(binDistance[DARK][bin] * 10.0) / 10.0); break;
			case DISTANCE_LIGHT: result[bin] = String.valueOf(Math.round(binDistance[LIGHT][bin] * 10.0) / 10.0); break;
			case TIME_DARK: result[bin] = String.valueOf(Math.round(binTime[DARK][bin] * 10.0) / 10.0); break;
			case TIME_LIGHT: result[bin] = String.valueOf(Math.round(binTime[LIGHT][bin] * 10.0) / 10.0); break;
			case TRANSITION: result[bin] = String.valueOf(binTransition[bin]); break;
			}
		}
		if(state == INTERRUPT)
			result[length - 1] += "(" + currentBinSlice + "frame)";
		return result;
	}
}
