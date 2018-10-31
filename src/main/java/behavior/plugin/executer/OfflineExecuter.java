package behavior.plugin.executer;

import java.io.File;
import java.util.ArrayList;

import ij.*;
import ij.process.ImageProcessor;
import behavior.setup.Setup;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.plugin.analyzer.Analyzer;
import behavior.io.ResultSaver;
import behavior.io.FileManager;
import behavior.io.ImageLoader;

public abstract class OfflineExecuter extends ProgramExecuter{
	protected Setup setup;
	protected WindowOperator winOperator;
	protected Analyzer[] analyze;
	protected ResultSaver resSaver;

	protected Program program;
	protected int allCage = 1, allSlice;
	/*オフラインでは一ケージずつ解析するので配列にする必要はないのだが、オンラインとの互換性を持つために配列にしてある。
	 * もっと上手い記述方法があれば変更したい。*/
	protected ImagePlus[] currentImp, traceImp;
	protected String[] subjectID, binFileName;
	protected ArrayList<String[]> results = new ArrayList<String[]>();

	protected boolean setup(){
		ONLINE = false;
		setup = new Setup(program, Setup.OFFLINE, allCage);
		if(setup.setup())
			return true;
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		analyze = new Analyzer[allCage];
		return false;
	}

	protected boolean run(int trialNum){
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
			resSaver = new ResultSaver(program, 1, backIp);
			resSaver.setSubjectID(subjectID);
			setWindow(subjectIDs[cage], backIp);
			subSetup(backIp);
			analyze(allImage);
			save(subjectIDs[cage], trialNum+cage,cage,cage==subjectIDs.length-1);
			if(cage<subjectIDs.length-1){
				winOperator.closeWindows();
			}
		}

		NextAnalysisDialog next = new NextAnalysisDialog();
		while(next.isVisible()){
			try{
				Thread.sleep(200);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		winOperator.closeWindows();
		if(next.nextAnalysis())
			return false;
		else{
			end();
			return true;
		}
	}

	protected ImageStack getSavedImage(String id){
		return (new ImageLoader()).loadImage(id);
	}

	protected boolean setOtherParameter(ImageProcessor[] backIp){
		return false;
	}

	protected abstract void subSetup(ImageProcessor[] backIp);

	protected ImageProcessor[] getBackground(ImageStack allImage){
		ImageProcessor backIp = allImage.getProcessor(allImage.getSize());
		ImageProcessor[] backIps = new ImageProcessor[allCage];
		backIps[0] = backIp;
		return backIps;
	}

	protected void setWindow(String subjectID, ImageProcessor[] backIp){
		winOperator = WindowOperator.getInstance(1, backIp);
		currentImp[0] = new ImagePlus(subjectID, backIp[0]);
		winOperator.setImageWindow(currentImp, WindowOperator.LEFT_UP);
		setEachWindow(backIp);	//abstract: 各実験固有のウィンドウを表示する

		traceImp[0] = new ImagePlus(subjectID, backIp[0]);
		winOperator.setImageWindow(traceImp, WindowOperator.LEFT_DOWN);
		resSaver.setTraceImage(backIp);

		winOperator.setXYWindow(program);
	}

	protected abstract void setEachWindow(ImageProcessor[] backIp);

	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		analyze[0].resetDuration(allSlice - 2);
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
		}
		addRestResult(allSlice - 2);
	}

	protected void addRestResult(int allSlice){
		if(analyze[0].binUsed()){
		    if(allSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) != 0){
			    ImageProcessor traceIp = analyze[0].getTraceImage(allSlice);
			    resSaver.addTraceImage(0, traceIp);
		    }
		}else{
			ImageProcessor traceIp = analyze[0].getTraceImage(allSlice);
		    resSaver.addTraceImage(0, traceIp);
	    }
	}

	protected void setCurrentImage(int sliceNum, ImageProcessor currentIp){
		currentImp[0].setProcessor(subjectID[0], currentIp);
		ImageProcessor traceIp = analyze[0].getTraceImage(sliceNum);
		traceImp[0].setProcessor("", traceIp);
		if(analyze[0].binUsed() && (sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)){
			resSaver.addTraceImage(0, traceIp);
		}

		setEachCurrentImage(0);	//abstract: それぞれの実験の画像表示
	}

	protected abstract void setEachCurrentImage(int cage);

	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		final boolean writeHeader = !(new File(FileManager.getInstance().getSavePath(FileManager.totalResPath)).exists());
		final boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(cageNum == 0){
			writeParameter = true;
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
	}

	protected abstract void saveBinResult(boolean writeVersion);

	protected void end(){
		final boolean writeDate = true;
		resSaver.writeDateOffline(writeDate,writeDate,binFileName);
	}
}