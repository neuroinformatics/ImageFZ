package behavior.plugin.executer;

import java.io.File;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import behavior.setup.Program;
import behavior.setup.parameter.FZParameter;
import behavior.setup.parameter.Parameter;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.FZResultSaver;
import behavior.io.FileManager;
import behavior.plugin.analyzer.FZAnalyzer;

public class FZoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp, xorImp;

	public FZoffExecuter(){
		program = Program.FZ;
		String[] fileName = {"dist", "immobile"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
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
			resSaver = new FZResultSaver(program, 1, backIp);
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
		analyze[0] = new FZAnalyzer(backIp[0]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[1];
		subtractImp[0] = new ImagePlus("subtract" + 1, backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
		xorImp = new ImagePlus[1];
		xorImp[0] = new ImagePlus("xor" +1, backIp[0]);
		winOperator.setImageWindow(xorImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		analyze[0].resetDuration((allSlice-2)*Parameter.getInt(FZParameter.shockCaptureRate));
		for(int slice = 1; slice < allSlice; slice++){
			ImageProcessor currentIp = allImage.getProcessor(slice);
			analyze[0].analyzeImage(currentIp);
			analyze[0].calculate((slice-1)*Parameter.getInt(FZParameter.shockCaptureRate));
			if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[0].nextBin();
			winOperator.setXYText(0, analyze[0].getXY((slice-1)*Parameter.getInt(FZParameter.shockCaptureRate)));
			setCurrentImage(slice - 1, currentIp);
		}
		addRestResult(allSlice - 2);
	}

	@Override
	protected void addRestResult(int allSlice){
		if(allSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) != 0){
		    ImageProcessor traceIp = analyze[0].getTraceImage(allSlice*Parameter.getInt(FZParameter.shockCaptureRate));
		    resSaver.addTraceImage(0, traceIp);
		}
	}

	protected void setCurrentImage(int sliceNum, ImageProcessor currentIp){
		currentImp[0].setProcessor(subjectID[0], currentIp);
		ImageProcessor traceIp = analyze[0].getTraceImage(sliceNum*Parameter.getInt(FZParameter.shockCaptureRate));
		traceImp[0].setProcessor("", traceIp);
		if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0){
			resSaver.addTraceImage(0, traceIp);
		}
		setEachCurrentImage(0);	//abstract: それぞれの実験の画像表示
	}

	@Override
	protected void setEachCurrentImage(int cage){
		ImageProcessor sub = ((FZAnalyzer)analyze[cage]).getSubtractImage();
		subtractImp[cage].setProcessor("subtract" + 1, sub);
		if(sub!=null){
		    ((FZResultSaver)resSaver).setSubtractImage(cage, sub);
		}
		xorImp[cage].setProcessor("xor" + 1, ((FZAnalyzer)analyze[0]).getXorImage());
	}

	@Override
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		final boolean writeHeader = !(new File(FileManager.getInstance().getSavePath(FileManager.totalResPath)).exists());
		final boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(cageNum == 0){
			writeParameter = setup.isModifiedParameter();
		}else{
			writeParameter = false;
		}

		resSaver.saveOfflineTraceImage();

		resSaver.saveOfflineXYResult(0, ((FZAnalyzer)analyze[0]).getXYText());

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

	@Override
	protected void saveBinResult(boolean writeVersion) {
		int[] option = {FZAnalyzer.BIN_DISTANCE, FZAnalyzer.BIN_FREEZ_PERCENT};

		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[1][];
			result[0] = ((FZAnalyzer)analyze[0]).getBinResult(option[num]);
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}
}