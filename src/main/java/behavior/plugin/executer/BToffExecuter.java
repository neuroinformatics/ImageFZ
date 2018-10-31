package behavior.plugin.executer;

import java.io.File;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.BTAnalyzer;
import behavior.setup.Program;

/**
 * Beam-Test(Offline)�p��Executer�B�_�̏�ɏ悹��ꂽ�}�E�X�̍s������͂���B
 * Online�ƈقȂ�����͎��̎O�B
 * �@�@1.Labjack���g�p���Ȃ�
 * �@�@2.Slip,SlipedTime,Fall,Goal�͏o�͂��Ȃ�
 * �@�@3.�����̏I�������͋�ʂ��Ȃ�
 * 
 * @author Butoh
 */
public class BToffExecuter extends OfflineExecuter {
	private ImagePlus[] subtractImp;
	private String[] respectiveFileNames;
	private String[] respectiveHeaderNames;
	
	public BToffExecuter() {
		program = Program.BTO;

		//Offline�ł�Slip�����
		String[] fileName = {"Distance Per Movement","Duration Per Movement","Speed Per Movement", };
		respectiveFileNames = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			respectiveFileNames[i] = fileName[i];

		String[] headerName = {"Phase", "Phase","Phase"};
		respectiveHeaderNames = new String[headerName.length];
		for(int i = 0; i < headerName.length; i++)
			respectiveHeaderNames[i] = headerName[i];
	}

	protected void subSetup(ImageProcessor[] backIp) {
		analyze[0] = new BTAnalyzer(backIp[0]);
	}

	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[allCage];
        subtractImp[0] = new ImagePlus("subtract" + 1, backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
	}

	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract" + 1,((BTAnalyzer) analyze[0]).getSubtractImage());
	}

	@Override
	//BeamTest�ł�Labjack��Offline�ł͎g�p���Ȃ��̂�Online��Offline��
	//calcurate���قȂ��Ă���
	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		((BTAnalyzer)analyze[0]).setFlag(program, allSlice);
		analyze[0].resetDuration(allSlice - 2);
		for(int slice = 1; slice < allSlice; slice++){
			ImageProcessor currentIp = allImage.getProcessor(slice);
			analyze[0].analyzeImage(currentIp);
            //if(analyze.mouseExists()){  //�}�E�X���F�������΁A��͂ƌ��ʕ\��
			//�������C��
			((BTAnalyzer)analyze[0]).calculate(slice - 1);
			//if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			//	analyze[0].nextBin();
			winOperator.setXYText(0, analyze[0].getXY(slice - 1));
			setCurrentImage(slice - 1, currentIp);
            //}
		}
		addRestResult(allSlice - 2);
	}

    //BeamTest�ł�Labjack��Offline�ł͎g�p���Ȃ��̂�Online��Offline��
	//getResult���قȂ��Ă���
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

		saveBinResult(writeVersion);
	}

	//SlipedTime�͋L�^���Ȃ�
	@Override
	protected void saveBinResult(boolean writeVersion) {
		int[] option = {BTAnalyzer.DISTANCE_PER_MOVEMENT, BTAnalyzer.DURATION_PER_MOVEMENT, BTAnalyzer.SPEED_PER_MOVEMENT};
		final int headerNum = 10;
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(respectiveFileNames[num])).exists());
			List<String> result;
			result = ((BTAnalyzer)analyze[0]).getRespectiveResults(option[num]);
			result.add("");
			resSaver.saveOfflineRepectiveResults(respectiveFileNames[num], respectiveHeaderNames[num], headerNum, result, writeHeader,writeVersion);
		}
	}

	//����ς�trace�͎g��
	/*
	public boolean traceUsed(){
		return false;
	}*/
}
