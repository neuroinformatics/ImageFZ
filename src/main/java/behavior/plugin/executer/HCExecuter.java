package behavior.plugin.executer;

import java.io.File;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import behavior.gui.BehaviorDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.HCAnalyzer;
import behavior.setup.Header;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.util.GetDateTime;

/**
 * Home Cage Executer の各プログラムに共通することはここに記述する。
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public abstract class HCExecuter extends OnlineExecuter {
	protected ImagePlus[] subtractImp, xorImp;
	protected String StartTime;
	protected boolean imgload = false;//バックグラウンドの取得が行われたかどうか。
	protected String[] xyDataPath;
	protected FileManager fm;
	protected String projectName,SessionName;
	protected String[] binPathName;//Bin結果の保存先
	protected int binLength;
	protected int isAM;

	protected String sep = System.getProperty("file.separator");

	@Override
	protected void readyToStart(){
		BehaviorDialog.showMessageDialog("please click OK to start");
	}

	protected void end(){
	}

	protected void subSetup(ImageProcessor[] backIp){
		binLength = Parameter.getInt(Parameter.duration) / Parameter.getInt(Parameter.binDuration);
		fm = FileManager.getInstance();
		projectName = fm.getPath(FileManager.project);
		SessionName = setup.getSessionID();
		binPathName = new String[binFileName.length];
		StartTime = GetDateTime.getInstance().getDateTimeString();

		createHeader();
		setAnalyze(backIp);
	}

	/**
	 * 各ケージのanalyzeオブジェクトの生成
	 */
	protected abstract void setAnalyze(ImageProcessor[] backIp);

	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
		xorImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			xorImp[cage] = new ImagePlus("xor" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(xorImp, WindowOperator.RIGHT_UP);
	}

	protected ImageProcessor[] setImage(){
		ImageProcessor allIp = (imageCapture.capture()).getProcessor();
		if(allIp == null){
			ImagePlus imp = imageCapture.capture();
			allIp = imp.getProcessor();
		}
		ImageProcessor[] currentIp = roiOperator.split(allIp);
		return currentIp;
	}

	/******
	 *現在の画像を表示したり保存したりする
	 *******/
	protected void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		if(program == Program.HC1){ // HC1はトレース対応
			ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
			traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);
			//if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * 60 * Parameter.getInt(Parameter.rate)) == 0)
			//	resSaver.addTraceImage(cage, traceIp);
		}
		setEachCurrentImage(cage);	//abstract: それぞれの実験の画像表示
	}

	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((HCAnalyzer)analyze[cage]).getSubtractImage());
		xorImp[cage].setProcessor("xor" + (cage + 1), ((HCAnalyzer)analyze[cage]).getXorImage());
		((HCAnalyzer)analyze[cage]).setCurrentXYData(xyDataPath[cage]); //XYDataのリアルタイム保存のため
	}

	protected void saveImage(ImageProcessor[] backIp){
	}
	protected void saveBinResult(boolean writeVersion){
	}

	/**
	 * Result,XY-Dataの記録用ファイルの生成
	 */
	protected void createHeader(){
		GetDateTime time = GetDateTime.getInstance();

		// Result
		FileCreate fc = new FileCreate();
		for(int f = 0; f < binFileName.length; f++){
			binPathName[f] = projectName + sep + "Results" + sep + SessionName + "-" + binFileName[f] +"_" + time.getDateString() + ".txt";
			StringBuffer headerString = new StringBuffer("ID\t" + subjectID[0]);
			for(int i = 1; i < subjectID.length; i++)
				headerString.append("\t" + subjectID[i]);
			headerString.append("\tCurrent Time");
			fc.write(binPathName[f], headerString.toString(), true);
		}

		// XY-Data
		FileManager fm = FileManager.getInstance();
		xyDataPath = fm.getPaths(FileManager.xyPath);

		for(int cage = 0; cage < allCage; cage++){
			StringBuffer tempPath = new StringBuffer(xyDataPath[cage]);
			tempPath.delete(tempPath.length() - 4, tempPath.length());
			tempPath.append("_" + time.getDateString() + ".txt");
			xyDataPath[cage] = tempPath.toString();
		}
		String head = Header.getXYHeader(program);
		for(int cage = 0; cage < allCage; cage++){
			fc.createNewFile(xyDataPath[cage]);
			fc.writeChar(xyDataPath[cage], head, true);
		}
	}

	protected void writeDate(){
		resSaver.writeDateWithStartTime(StartTime);
	}

	protected void setResult(){
		boolean canSave = true;
		int currentBin = ((HCAnalyzer)analyze[0]).getCurrentBin();

		// 保存可能のフラグが全て立っていれば保存する
		for(int i = 0; i < allCage; i++)
			canSave &= ((HCAnalyzer)analyze[i]).getCanSave();

		if(canSave){
			String currentTime = GetDateTime.getInstance().getDateTimeString();
			StringBuffer[] savedString = new StringBuffer[binPathName.length];
			for(int i = 0; i < binPathName.length; i++){
				savedString[i] = new StringBuffer("bin" + currentBin);
				for(int j = 0; j < allCage; j++)
					savedString[i].append("\t" + ((HCAnalyzer)analyze[j]).getBinResult(i)[0]);
				savedString[i].append("\t" + currentTime);
			}

			FileCreate fc = new FileCreate();
			for(int i = 0; i < binPathName.length; i++)
				fc.write(binPathName[i], savedString[i].toString(), true);

			for(int i = 0; i < allCage; i++)
				((HCAnalyzer)analyze[i]).beSaved();

			// 1日ごとにファイルを変える
			if(currentBin % ((60 * 60 * 24) / Parameter.getInt(Parameter.binDuration)) == 0 && currentBin != binLength)
				createHeader();

			// XY-Dataウィンドウの初期化
			// 長時間動かす Home Cage では放っておくとメモリ使用量がどんどん増えるため。
			for(int i = 0; i < allCage; i++)
				winOperator.clearXYText(i);
		}
	}

	@Override
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());	//最初の解析のときは、結果フォルダにヘッダを記載する
		boolean writeVersion = (trialNum==1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		int activeCage = 0;
		boolean[] endAnalyze = new boolean[allCage];
		for(int cage = 0; cage < allCage; cage++)
			if(endAnalyze[cage] = (analyze[cage].getState() != Analyzer.NOTHING))
				activeCage++;
		resSaver.setActiveCage(endAnalyze);
		

		saveImage(backIp);

		String[][] totalResult = new String[allCage][];
		for(int cage = 0; cage < allCage; cage++){
			resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
			totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
	    winOperator.showOnlineTotalResult(program, activeCage, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
	}
}
