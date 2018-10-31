package behavior.plugin.executer;

import java.io.File;

import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.BMResultSaver;
import behavior.io.FileManager;
import behavior.plugin.analyzer.BMAnalyzer;
import behavior.plugin.executer.OfflineExecuter;
import behavior.setup.Program;
import behavior.setup.parameter.BMParameter;
import behavior.setup.parameter.Parameter;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class BMoffexecuter extends OfflineExecuter {
	private ImagePlus[] subtractImp;
	private final String sep = System.getProperty("file.separator");
	
	public BMoffexecuter(){
		this.program = Program.BM;
	}
	protected void subSetup(ImageProcessor[] backIp){
//		Analyzeクラスの初期化（変数名は宣言済み）
		analyze[0] = new BMAnalyzer(backIp[0]);
	}
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((BMAnalyzer)analyze[cage]).getSubtractImage());
	}
	
	protected boolean run(int trialNum){
		/*if(trialNum > 1){
			if(setup.getSession())
				return true;
		}
		setup.loadSubjectID();*/
		if(trialNum > 1 && setup.reSetup())
			return true;
		String[] subjectIDs = setup.getSubjectID();
		for(int cage = 0; cage < subjectIDs.length; cage++){
			subjectID = new String[1];
			subjectID[0] = subjectIDs[cage];	//ここで、現在使用する subjectID を指定
			(FileManager.getInstance()).setSubjectID(subjectID);
			ImageStack allImage = getSavedImage(subjectID[0]);
			allSlice = allImage.getSize();
			ImageProcessor[] backIp = getBackground(allImage);
			if(setOtherParameter(backIp)) break;
			resSaver = new BMResultSaver(program, 1, backIp);
			resSaver.setSubjectID(subjectID);
			setWindow(subjectIDs[cage], backIp);
			subSetup(backIp);
			analyze(allImage);
			save(subjectIDs[cage], trialNum+cage,cage,cage==subjectIDs.length-1);
			if(cage<subjectIDs.length-1)
			   winOperator.closeWindows();
		}
		
		NextAnalysisDialog next = new NextAnalysisDialog();
		while(next.isVisible()){
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		winOperator.closeWindows();
		if(next.nextAnalysis()) {
			BMAnalyzer.subjectIDCount = 0;
			return false;
		} else {
			BMAnalyzer.subjectIDCount = 0;
			return true;
		}
	}
	
	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		if (allSlice - 2 > Parameter.getInt(BMParameter.duration)) { //本来こうはならないはずだが、wikiの090515にあるようなことが起こった場合のため(原因や、これで直るのかは不明。ただ、XYが302まであるということはなんらからの原因でallSliceがそれより大きくなったとしか考えられない)。
			allSlice = Parameter.getInt(BMParameter.duration) + 2;
		}
		analyze[0].resetDuration(allSlice - 2);
		if (allSlice - 2 == Parameter.getInt(BMParameter.duration)) {
			for(int slice = 1; slice < allSlice; slice++){
				ImageProcessor currentIp = allImage.getProcessor(slice);
				analyze[0].analyzeImage(currentIp);
	//			if(analyze.mouseExists()){  //マウスが認識されれば、解析と結果表示
				analyze[0].calculate(slice - 1);
				if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
					analyze[0].nextBin();
				winOperator.setXYText(0, analyze[0].getXY(slice - 1));
				setCurrentImage(slice - 1, currentIp);
	//			}
				if (((BMAnalyzer) analyze[0]).checkFinished())
						break;
			}
		} else {	//途中で終了した場合は、(Onlineではおそらく最後の１枚が計算に入っていないため)allSlice-1としないと結果が合わない。
			for(int slice = 1; slice < allSlice - 1; slice++){
				ImageProcessor currentIp = allImage.getProcessor(slice);
				analyze[0].analyzeImage(currentIp);
	//			if(analyze.mouseExists()){  //マウスが認識されれば、解析と結果表示
				analyze[0].calculate(slice - 1);
				if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
					analyze[0].nextBin();
				winOperator.setXYText(0, analyze[0].getXY(slice - 1));
				setCurrentImage(slice - 1, currentIp);
	//			}
				if (((BMAnalyzer) analyze[0]).checkFinished())
						break;
			}
		}
		addRestResult(allSlice - 2);
	}
	
	
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		final boolean writeHeader = !(new File(FileManager.getInstance().getSavePath(FileManager.ResultsDir) + sep + FileManager.getInstance().getPath(FileManager.SessionID) + "_res.txt").exists());
		final boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(cageNum == 0){
			writeParameter = setup.isModifiedParameter();
		}else{
			writeParameter = false;
		}

		resSaver.saveOfflineTraceImage();

		resSaver.saveOfflineXYResult(0, winOperator.getXYTextPanel(0));

		String[] totalResult;
		totalResult = analyze[0].getResults();

		resSaver.saveOfflineTotalResult(totalResult, writeHeader, writeVersion,  writeParameter);

		String[] resultsLine = new String[totalResult.length+1];
		resultsLine[0] = subjectID;
		for(int i=0;i<totalResult.length;i++){
			resultsLine[i+1] = totalResult[i];
		}
		results.add(resultsLine);

		if(showResult){
		    winOperator.showOfflineTotalResult(program,results);
		    results.clear();
		}
		
		saveBinResult(writeVersion);
		
		String[] result = ((BMAnalyzer) analyze[0]).getProbeResults();
		if (new File(FileManager.getInstance().getPath(FileManager.ResultsDir) + sep + FileManager.getInstance().getPath(FileManager.SessionID) + "_probe.txt").exists())   
			((BMResultSaver) resSaver).saveOfflineProbeResult(result, false);
		else 
			((BMResultSaver) resSaver).saveOfflineProbeResult(result, true);
		
		String result_sel = ((BMAnalyzer) analyze[0]).getSelResult();
		((BMResultSaver) resSaver).saveOfflineSelResult(result_sel, true);
	}
	/*
	protected void save(String subjectID, int trialNum){
		boolean writeHeader = (trialNum == 1 || toWrite);	//最初の解析のときは、結果フォルダにヘッダを記載する
		toWrite = false;
		boolean writeVersion = false;

		resSaver.saveTraceImage();

		String[][] totalResult = new String[1][];
		resSaver.saveXYResult(0, winOperator.getXYTextPanel(0));
		totalResult[0] = analyze[0].getResults();
		}else if(program != Program.TS){
			resSaver.saveTotalResult(totalResult, writeHeader);
			String[] subjectIDs = new String[1];
			subjectIDs[0] = subjectID;
			//winOperator.showTotalResult(program, 1, subjectIDs, totalResult,WindowOperator.OFFLINE);
		}
		saveBinResult(writeVersion);
		
		String[] result = ((BMAnalyzer) analyze[0]).getProbeResults();
		if (new File(FileManager.getInstance().getPath(FileManager.ResultsDir) + sep + FileManager.getInstance().getPath(FileManager.SessionID) + "_probe.txt").exists())   
			((BMResultSaver) resSaver).saveProbeResult(result, false);
		else 
			((BMResultSaver) resSaver).saveProbeResult(result, true);
		
		String result_sel = ((BMAnalyzer) analyze[0]).getSelResult();
		((BMResultSaver) resSaver).saveSelResult(result_sel, true);
	}
	*/
	
	
	protected void saveBinResult(boolean writeVersion){
		//resSaver.saveBinResult("visit", BMAnalyzer.mousePositionString, writeHeader);
	}
}