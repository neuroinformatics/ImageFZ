package behavior.plugin.analyzer;

import java.awt.Rectangle;
import java.util.Arrays;

import behavior.gui.BehaviorDialog;
import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;
import behavior.setup.Setup;
import behavior.setup.dialog.BMSessionDialogPanel;
import behavior.setup.parameter.BMParameter;
import behavior.setup.parameter.Parameter;
import behavior.io.FileManager;

import ij.process.ImageProcessor;
import ij.io.RoiDecoder;
import ij.gui.Roi;
import ij.gui.OvalRoi;

public class BMAnalyzer extends Analyzer{
	protected int mousePosition=0, preMousePosition=0;//0はどの穴の近くにもいない状態 1～12だと、その番号の穴の近くにいることを示す
	protected int durationCount=0;
	protected int[][] xyaData;	//X座標、Y座標、面積、の順にデータが入るところ。xyaData[？個目のかたまり][データの種類]
	protected int[]   prevXyaData;
	protected double currentDistance = 0, totalMoveDuration = 0;
	protected double[] binDistance;
	protected RoiDecoder hole_roi_d;
	protected RoiDecoder field_roi_d;
	protected Roi[] hole_roi = new Roi[12];
	protected Roi field_roi;
	private FileManager fileManager = FileManager.getInstance();
	public static String[][] mousePositionString;
	protected int totalErr, ErrTo1st=0, LatTo1st, OmissionErr=0, MultipleParticles=0, NoParticles=0;
	protected int NoParticleCount = 0;
	protected double DistTo1st, TimeAroundTarget;
	protected int mousePositionCount=0;//何回穴のそばに行ったかを数える、totalErr用
	static public int subjectIDCount = 0;	//Offlineの時のみoffExecuterで初期化。Onlineのnext Analysis?でquitした場合も0にする。
	protected boolean flag1, flag2;
	protected boolean isFirstTime = true;
	protected int[] stayTime = new int[12];//各穴の滞在時間
	private boolean start = false;
	private boolean isFinished = false;
	private String result_sel = "";
	private final String sep = System.getProperty("file.separator");
	public static int type = Setup.ONLINE;
	private int diameter;
	private OvalRoi ovalField;
	
	
	protected void initializeArrays(){
		mousePositionString = new String[1][1];
		mousePositionString[0][0] = "initial";
		Arrays.fill(stayTime, 0);
		binDistance = new double[binLength];
		String hole_roi_string;
		String field_roi_string;
		try {
			field_roi_string = fileManager.getPath(FileManager.PreferenceDir) + sep + "Field.roi";
			field_roi_d = new RoiDecoder(field_roi_string);
			field_roi = field_roi_d.getRoi();
			Rectangle rec = field_roi.getPolygon().getBounds();
		    diameter = (rec.width + rec.height) / 2;
		    ovalField = new OvalRoi(0, 0, diameter, diameter);
		} catch (Exception e) {
			ij.IJ.showMessage("Please set 「Field.roi」 in the Preference folder");
		}
		for(int i=0;i<12;i++){
			try{
				hole_roi_string = fileManager.getPath(FileManager.PreferenceDir) + sep + "hole" + String.valueOf(i+1) + ".roi";
				hole_roi_d = new RoiDecoder(hole_roi_string);
				hole_roi[i] = hole_roi_d.getRoi();
			}catch(Exception e){
				ij.IJ.showMessage("Please set 「hole1.roi～hole12.roi」in the Preference folder.");
			}
		}
		flag1 = true;
		flag2 = true;
	}
	public BMAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate());
		tracer = new Tracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());
	}
	public void analyzeImage(ImageProcessor currentIp){
		ImageProcessor ip = currentIp.duplicate();
		ip.setColor(0);
        Rectangle rec = ip.getRoi();
        ip.setRoi(ovalField);
        ip.fillOutside(ovalField);
        ip.setRoi(rec);
		
		imgManager.setCurrentIp(ip.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//移動距離の計算用に前回の数値を残しておく
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);

		/*
		if(xyaData[0][FLAG] == 0)//マウスが認識されなかった場合
			NoParticles++;
		if(xyaData[0][FLAG] == 2)//パーティクルが複数認識された場合
			MultipleParticles++;
		*/
	}
	public void calculate(int currentSlice){	//どこから呼ばれるかは知らないが、各スタック毎に呼ばれる関数
		if(currentSlice > 0){	//2枚目の画像からでないと、移動距離の計算はできないため
//			currentBinSlice++;	//必要かどうかは不明
			currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER], prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
			binDistance[currentBin] += currentDistance;
		}
		if(currentDistance > Parameter.getDouble(BMParameter.movementCriterion))
			totalMoveDuration += 1.0 / Parameter.getInt(BMParameter.rate);
		if(currentSlice > 0)	//１枚目で加算するとタイムアウトしたときの値がduration+1になってしまう。
			durationCount += 1;

		//マウスがどの穴の近くにいるかの判定など
		preMousePosition = mousePosition;
		int x, y ,width, height, x_center, y_center, innerR, outerR;
		//armR = Parameter.getInt(BMParameter.armR);
		//distOut = Parameter.getInt(BMParameter.distOut);
		innerR = BMParameter.innerR;
		outerR = BMParameter.outerR;
		if(mousePosition != 0){
			stayTime[mousePosition-1]++;
			x = hole_roi[mousePosition-1].getBounds().x;
			y = hole_roi[mousePosition-1].getBounds().y;
			width = hole_roi[mousePosition-1].getBounds().width;
			height = hole_roi[mousePosition-1].getBounds().height;
			x_center = x + width/2;
			y_center = y + height/2;
			OvalRoi hole_oval_roi = new OvalRoi(x_center - outerR, y_center - outerR, outerR*2, outerR*2);
			if(!hole_oval_roi.contains(xyaData[0][X_CENTER], xyaData[0][Y_CENTER])){ //マウスがさっきまでいた穴の近くから離れたかどうかを判定
				mousePosition = 0;
			}
		}
		for(int i=0;i<12;i++){
			x = hole_roi[i].getBounds().x - field_roi.getBounds().x;
			y = hole_roi[i].getBounds().y - field_roi.getBounds().y;
			width = hole_roi[i].getBounds().width;
			height = hole_roi[i].getBounds().height;
			x_center = x + width/2;
			y_center = y + height/2;
			
			OvalRoi hole_oval_roi = new OvalRoi(x_center - innerR, y_center - innerR, innerR*2, innerR*2);
			if(hole_oval_roi.contains(xyaData[0][X_CENTER], xyaData[0][Y_CENTER])){
				mousePosition = i + 1;
			}
		}
		if(mousePosition != preMousePosition && mousePosition != 0){
			result_sel += "\t" + Integer.toString(mousePosition);
			mousePositionCount += 1;
			if(flag1 && mousePosition == BMSessionDialogPanel.targetHole[subjectIDCount]){
				ErrTo1st = mousePositionCount - 1;
				LatTo1st = durationCount;
				DistTo1st = binDistance[currentBin];
				flag1 = false;
			}
			if(mousePosition == BMSessionDialogPanel.targetHole[subjectIDCount]){
				if (isFirstTime)
					isFirstTime = false;
				else
					OmissionErr += 1;
			}
			if(flag2){
				mousePositionString[0][0] = "";//初期化
				flag2 = false;
			}
			mousePositionString[0][0] += String.valueOf(mousePosition) + "\t";
		}
		if (mousePosition == BMSessionDialogPanel.targetHole[subjectIDCount])
			TimeAroundTarget++;
		
		
		if(xyaData[0][EXIST_FLAG] == 0) {//マウスが認識されなかった場合 
			NoParticles++;
			if (currentSlice > 0) {					//analyzeの形式を変えてからcurrentSlice>0がないと１行下で止まる。
				if (prevXyaData[EXIST_FLAG] == 0)			
					NoParticleCount++;
				else
					NoParticleCount = 0;
			}
		}
		if (NoParticleCount >= 3)	//３秒以上認識されなかった場合は穴に入ったか台から落ちたとみなす。
			isFinished = true;
		
		
		if(xyaData[0][EXIST_FLAG] == 2)//パーティクルが複数認識された場合
			MultipleParticles++;
		
		
		if(prevXyaData != null && prevXyaData != null)
			((Tracer)tracer).writeTrace(prevXyaData[X_CENTER], prevXyaData[Y_CENTER], xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
	}
	
	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}
	
	
	public boolean startAnalyze (int cageNo) {
		return start;
	}
	
	public void setStart (final boolean set) {
		if (start == true)
			return;
		start = set;
	}
	
	public boolean checkFinished() {
		return isFinished;
	}
	
	public void setState(boolean finished) {
		this.isFinished = finished;
	}

	/*
	 * どこで呼ばれるのか分からない
	 */
	public void nextBin(){
	}
	public String getInfo(String subjectID, int sliceNum){
		if (sliceNum == 0) 	//実験が始まっていないのにマウスの座標などを表示すると紛らわしいので
			return subjectID + "\t" + getElapsedTime(sliceNum) + "\t \t \t \t" + BMSessionDialogPanel.targetHole[subjectIDCount];
		else
			return subjectID + "\t" + getElapsedTime(sliceNum) + "\t" + xyaData[0][0] + "\t" +
					xyaData[0][1] + "\t" + Math.round(currentDistance * 10.0) / 10.0 + "\t" + BMSessionDialogPanel.targetHole[subjectIDCount];
	}
	public String getXY(int sliceNum){
		String xyData = sliceNum + "\t" + xyaData[0][X_CENTER] +"\t"+ xyaData[0][Y_CENTER] +"\t" + (xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA]));
		return xyData;
	}

	public String[] getResults(){
		subjectIDCount += 1;
		String[] results = new String[13];
		double totalDistance = 0;
		for(int bin = 0; bin < binLength; bin++){
			totalDistance += binDistance[bin];
		}
		try {
			if (durationCount == Parameter.getInt(BMParameter.duration) && flag1) {
				totalErr = mousePositionCount;
				ErrTo1st = totalErr;
				LatTo1st = durationCount;
				DistTo1st = totalDistance;
			} else {
				if (!flag1) //マウスがターゲットに近付いたことがあり、かつduration以内で終了した場合は穴に入ったとみなす。
					totalErr = mousePositionCount-1;
				else {	//マウスがターゲットに１度も近付いていないが終了した場合
					totalErr = mousePositionCount;
					ErrTo1st = totalErr;
					LatTo1st = durationCount;
					DistTo1st = totalDistance;
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			if (subjectIDCount == 0)
				BehaviorDialog.showErrorDialog("subjectIDCount: 0");
			else
				BehaviorDialog.showErrorDialog("target: " + BMSessionDialogPanel.targetHole[subjectIDCount-1]);
		}
		
		double meanSpeed = totalDistance / durationCount;
		double movingSpeed = totalDistance / (totalMoveDuration != 0 ? totalMoveDuration : 1);
		results[0]  = String.valueOf(Math.round(totalErr * 10.0) / 10.0);
		results[1]  = String.valueOf(Math.round(durationCount * 10.0) / 10.0);							//全体の時間(sec)
		results[2]  = String.valueOf(Math.round(totalDistance * 10.0) / 10.0);							//解析全体で動いた距離(cm)
		results[3]  = String.valueOf(Math.round(ErrTo1st * 10.0) / 10.0);
		results[4]  = String.valueOf(Math.round(LatTo1st * 10.0) / 10.0);
		results[5]  = String.valueOf(Math.round(DistTo1st * 10.0) / 10.0);
		results[6]  = String.valueOf(Math.round(OmissionErr * 10.0) / 10.0);
		results[7]  = String.valueOf(Math.round(totalMoveDuration * 10.0) / 10.0);						//一定値以上動いた時間全体（sec)
		results[8]  = String.valueOf(Math.round(TimeAroundTarget * 10.0) / 10.0);
		results[9]  = String.valueOf(Math.round(meanSpeed * 10.0) / 10.0);								//解析全体の平均速度（cm/sec）
		results[10] = String.valueOf(Math.round(movingSpeed * 10.0) / 10.0);							//一定値（MOVECR)以上動いた時だけを動いた時間と考えた場合の平均速度（cm/sec）
		results[11] = String.valueOf(Math.round(MultipleParticles * 10.0) / 10.0);
		results[12] = String.valueOf(Math.round(NoParticles * 10.0) / 10.0);
		return (results);
	}
	
	public String[] getProbeResults() {
		String[] results = new String[13];
		results[0] = BMSessionDialogPanel.targetHole[BMAnalyzer.subjectIDCount - 1] + "";//getResultsが先に呼ばれて＋１されるので引いておく
		for (int hole = 1; hole <= 12; hole++) {
			results[hole] = String.valueOf(stayTime[hole-1]);
		}
		
		return results;
	}
	
	public String getSelResult() {
		return this.result_sel;
	}

	public String[] getBinResult(int option){
		String[] s = {"BManalyze.java getBinResult"};
		return s;
	}
}