package behavior.plugin.executer;

import java.io.File;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.SIAnalyzer;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.SIResultSaver;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;

public class SIoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;

	public SIoffExecuter(){
		program = Program.SI;
	}

	@Override
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
			resSaver = new SIResultSaver(program, 1, backIp);
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
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		winOperator.closeWindows();
		if(next.nextAnalysis())
			return false;
		else
			return true;
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new SIAnalyzer(backIp[0]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[0].setProcessor("subtract" + 1, ((SIAnalyzer)analyze[0]).getSubtractImage());
	}

	@Override
	protected void setCurrentImage(int sliceNum, ImageProcessor currentIp){
		currentImp[0].setProcessor(subjectID[0], currentIp);
		ImageProcessor traceIp = analyze[0].getTraceImage(sliceNum);
		traceImp[0].setProcessor("", traceIp);
		if(sliceNum != 0 && ((double)sliceNum/Parameter.getInt(Parameter.rate))%60 ==0){
			resSaver.addTraceImage(0, traceIp);
		}

		setEachCurrentImage(0);	//abstract: それぞれの実験の画像表示
	}

	@Override
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		boolean writeHeader = !(new File(FileManager.getInstance().getSavePath(FileManager.totalResPath)).exists());
		boolean writeVersion = (trialNum == 1);
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
	}

	@Override
	protected void saveBinResult(boolean writeVersion){}

}
