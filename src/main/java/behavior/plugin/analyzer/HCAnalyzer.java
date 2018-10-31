package behavior.plugin.analyzer;

import ij.process.ImageProcessor;
import behavior.image.process.ImageManager;
import behavior.io.FileCreate;
import behavior.setup.parameter.HCParameter;
import behavior.setup.parameter.Parameter;
import behavior.util.GetDateTime;

/**
 * Home Cage 共通Analyze
 */
public abstract class HCAnalyzer extends Analyzer {
	public static final int X = 0;
	public static final int Y = 1;
	public static final int AREA = 2;
	public static final int FLAG = 3;

	protected int[][] xyaData, xorXyaData, prevXyaData;	//X座標、Y座標、面積、の順にデータが入るところ。xyaData[？個目のかたまり][データの種類]
	protected double[] binResult;
	protected int currentXorArea; //現在の XOR 画像で観測されているパーティクルの面積の合計
	protected int isLIGHT = 0; //昼なら0、夜なら1
	protected int LightON, LightOFF; //昼夜切り替え用
	protected String StartTime;
	protected String xyData;
	protected boolean canSave;

	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();

		LightON = Parameter.getInt(HCParameter.LightON);
		LightOFF = Parameter.getInt(HCParameter.LightOFF);
		int currentHour = GetDateTime.getInstance().getDateTimeArray()[GetDateTime.HOUR];

		if(LightON <= currentHour && currentHour < LightOFF) {
			imgManager.applyThreshold(ImageManager.SUBTRACT_IMG, Parameter.getInt(HCParameter.dayThreshold), Parameter.getInt(HCParameter.maxThreshold));
			state = 0;
		}else{ 
			imgManager.applyThreshold(ImageManager.SUBTRACT_IMG, Parameter.getInt(HCParameter.nightThreshold), Parameter.getInt(HCParameter.maxThreshold));
			state = 1;
		}

		for(int i = 0; i < Parameter.getInt(HCParameter.reduceTimes); i++)
			imgManager.reduceNoise(ImageManager.SUBTRACT_IMG);
		for(int i = 0; i < Parameter.getInt(HCParameter.dilateTimes); i++)
			imgManager.dilate(ImageManager.SUBTRACT_IMG);	//肉付け


		if(xyaData != null)
			prevXyaData = xyaData;		//前回の数値を残しておく
		else if(prevXyaData == null)
			prevXyaData = new int[1][];
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData[0]);
		//if(xyaData[0][FLAG] > 0)
		//	mouseExists = true;

		imgManager.xorImage(Parameter.getInt(HCParameter.xorThreshold), Parameter.getBoolean(HCParameter.subtractBackground));
		imgManager.reduceNoise(ImageManager.XOR_IMG);
		if(Parameter.getBoolean(HCParameter.erode))
			imgManager.erode(ImageManager.XOR_IMG);
		xorXyaData = imgManager.analyzeParticleOrg(ImageManager.XOR_IMG, prevXyaData[0]);
	}

	public void nextBin(){
		if(currentBin<binLength-1){
		    currentBin++;
		    currentBinSlice = 0;
		}
	}

	public abstract ImageProcessor getSubtractImage();

	public ImageProcessor getXorImage(){
		return imgManager.getIp(ImageManager.XOR_IMG);
	}

	public void setCurrentXYData(String path){
		FileCreate fc = new FileCreate(path);
		fc.writeChar(path, xyData, true);
	}

	// フラグを確認
	public boolean getCanSave(){
		return canSave;
	}

	// 保存が終わったらフラグを戻す
	public void beSaved(){
		canSave = false;
	}

	public String[] getBinResult(int option){
		String[] result = new String[1];
		result[0] = String.valueOf(Math.round(binResult[option] * 10.0) / 10.0);
		return result;	
	}

	public int getCurrentBin(){
		return currentBin;
	}
}
