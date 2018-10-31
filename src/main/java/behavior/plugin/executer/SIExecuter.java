package behavior.plugin.executer;

import java.awt.Cursor;
import java.io.File;
import java.util.concurrent.TimeUnit;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.AnalyzerForStart;
import behavior.plugin.analyzer.SIAnalyzer;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.SIResultSaver;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;

/**
 * @author Takahashi
 * @author Modifier:Butoh
 */
public class SIExecuter extends OnlineExecuter{
	private ImagePlus[] subtractImp;
	private AnalyzerForStart startAnalyzer;

	public SIExecuter(){
		program = Program.SI;
		this.allCage = 1;
	}

	@Override
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;
		setup.saveSession();
		if(setOtherParameter()) return true;
		if(roiOperator.loadRoi()) return true;
		IJ.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		ImageProcessor[] backIp = getBackground();	//�o�b�N�O���E���h�擾
		resSaver = new SIResultSaver(program, allCage, backIp);
		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

		setWindow(backIp);  //�\������E�B���h�E�̐ݒ�

		subSetup(backIp);  //abstract: �e�����ŗL�̃E�B���h�E�\���₻�̂ق��̐ݒ���s��
		analyze(subjectID);	//���

		save(trialNum, subjectID, backIp);	//�ۑ�
		
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
		else{
			end();
			return true;
		}
	}

	@Override
	protected synchronized void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		resSaver.setCurrentImage(cage, currentIp);

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);

		if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0){
			resSaver.addTraceImage(cage, traceIp);
		}

		setEachCurrentImage(cage);	//abstract: ���ꂼ��̎����̉摜�\��
	}

	@Override
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());	//�ŏ��̉�͂̂Ƃ��́A���ʃt�H���_�Ƀw�b�_���L�ڂ���
		boolean writeVersion = (trialNum==1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		int activeCage = 0;	//���ʂ�ۑ�����P�[�W���B�r���� interrupt ���ꂽ�P�[�W�̌��ʂ͕ۑ�����Ȃ��B
		boolean[] endAnalyze = new boolean[allCage];
		for(int cage=0; cage<allCage; cage++)
			if(endAnalyze[cage] = (analyze[cage].getState() != Analyzer.NOTHING))
				activeCage++;
		resSaver.setActiveCage(endAnalyze);

		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage=0; cage<allCage; cage++){
			if(!existMice[cage]) continue;
			resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
			totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
	    winOperator.showOnlineTotalResult(program, activeCage, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
	}

	@Override
	protected void readyToStart(){
		final int cage=0;
		ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);
		while(!startAnalyzer.startAnalyze(currentIp)){
			try{
			    TimeUnit.MILLISECONDS.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
			currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);
		}
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){	
		analyze[0] = new SIAnalyzer(backIp[0]);
		startAnalyzer = new AnalyzerForStart(backIp[0], program);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract" + 1, backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + 1, ((SIAnalyzer)analyze[cage]).getSubtractImage());
	}

	@Override
	protected void saveBinResult(boolean writeVersion){}

	@Override
	protected void end(){
		resSaver.writeDate(true,false,null);
	}
}