package behavior.plugin.executer;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.File;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import behavior.gui.BehaviorDialog;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.gui.roi.RoiOperator;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.io.ResultSaver;
import behavior.plugin.analyzer.Analyzer;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.parameter.Parameter;
import behavior.util.Timer;
/************************************************************
 *�@���̃N���X�́A�S�ẴI�����C���i���A���^�C���ł̃}�E�X�摜�擾�j�����v���O�����̊�b�ƂȂ���̂ŁA
 *�@�e�X�̃I�����C�������v���O�����́A���̃N���X���p�����Aabstract ���\�b�h�𖄂ߍ��ނ��Ƃō���܂��B
 *�@���̃N���X�ōs���̂́A�S�Ă̎����ŋ��ʂȁA���[�U�[����̃v���W�F�N�g�������́A�E�B���h�E�̕\���A
 *�@���A���^�C���摜�擾�A���ʂ̕ۑ��Ȃǂł��B
 ***************************************************************/
/**
 * 
 * @author Modifier:Butoh
 * @version Last Modified 091214
 */
public abstract class OnlineExecuter extends ProgramExecuter{
	protected Setup setup;
	protected RoiOperator roiOperator;
	protected WindowOperator winOperator;
	protected ResultSaver resSaver;
	protected Analyzer[] analyze;

	protected Program program;
	protected int allCage; //�T�u�N���X�Őݒ肳���ׂ����̂ł�
	protected String[] subjectID, binFileName;
	protected ImagePlus currentImp[];
	protected ImagePlus traceImp[];
	protected void initialize(){};
	protected Toolkit beep;
	protected String resultLine = "";
	protected ScheduledFuture<?>[] futures;
	protected CountDownLatch finishSignal;
	protected boolean[] existMice;

	protected Timer timer = null;
	protected Calendar[] calendar;

	/*****
	 * �v���W�F�N�gID�A�Z�b�V����ID�A�p�����[�^�ɂ��ă��[�U����̓��͂��󂯂āA
	 * �����o�ϐ��̏��������s�Ȃ�
	 *******/
	protected boolean setup(){
		ONLINE = true;
		imageCapture = ImageCapture.getInstance();

		if(imageCapture.setupFailed())
			return true;
		setup = new Setup(program, Setup.ONLINE, allCage);
		if(setup.setup())
			return true;
		setup.saveSession();
		roiOperator = new RoiOperator(program, allCage);
		analyze = new Analyzer[allCage];
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		calendar = new Calendar[allCage];
		return false;
	}

	/***
	 *�@���[�U�[����������A�ݒ肩���͂܂ł̃v���Z�X�B
	 *�@���̉�͂�₤�_�C�A���[�O�� yes �������ƁA���̕������J��Ԃ����
	 ***/
	protected boolean run(int trialNum){
		try{
		if(trialNum > 1 && setup.reSetup())
			return true;
		setup.saveSession();
		if(setOtherParameter()) return true;
		if(roiOperator.loadRoi()) return true;
		IJ.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		ImageProcessor[] backIp = getBackground();	//�o�b�N�O���E���h�擾
		resSaver = new ResultSaver(program, allCage, backIp);
		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

		setWindow(backIp);  //�\������E�B���h�E�̐ݒ�

		subSetup(backIp);  //abstract: �e�����ŗL�̃E�B���h�E�\���₻�̂ق��̐ݒ���s��
		analyze(subjectID);	//���
		save(trialNum, subjectID, backIp);	//�ۑ�
		}catch (Throwable e){e.printStackTrace();}
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

	/**
	 * ���ɓ��ɐݒ肷��K�v��������̂͂���ɋL�q����B
	 * @return true��Ԃ��ƏI���B
	 */
	protected boolean setOtherParameter(){
		return false;
	}

	/**
	 * �e�����ŗL�̃E�B���h�E�\���A���̑�
	 */
	protected abstract void subSetup(ImageProcessor[] backIp);

	/**
	 * �����I�����̓���B
	 * �f�t�H���g�ł�bin���Ƃ̌��ʃt�@�C���ɓ��t���������ށB
	 */
	protected void end(){
		resSaver.writeDate(binFileName);
	}

	/**�o�b�N�O���E���h���擾����**/
	protected ImageProcessor[] getBackground(){
		ImageProcessor[] backIp = roiOperator.split(setup.getBackIp());
		return backIp;
	}

	/******
	 * �\������E�B���h�E�̐ݒ�B�\������̂́A�e�P�[�W�̉摜�Ƃ���������������́i�e�����ɂ��قȂ�j�ƁA
	 * �S�̂̏���\������ info �E�B���h�E�A�e�}�E�X��XY���W��\������ XY �E�B���h�E�ł���B
	 *******/
	protected void setWindow(ImageProcessor[] backIp){
		winOperator = WindowOperator.getInstance(allCage, backIp);
		for(int cage = 0; cage < allCage; cage++){
		    currentImp[cage] = new ImagePlus(subjectID[cage], backIp[cage]);
		}
		winOperator.setImageWindow(currentImp, WindowOperator.LEFT_UP,existMice);
		setEachWindow(backIp);	//abstract: �e�����ŗL�̃E�B���h�E��\������

		for(int cage = 0; cage < allCage; cage++){
			traceImp[cage] = new ImagePlus("trace" + (cage + 1), backIp[cage]);
		}
		winOperator.setImageWindow(traceImp, WindowOperator.LEFT_DOWN,existMice);
		resSaver.setTraceImage(backIp);

		winOperator.setInfoWindow(program);
		for(int i=0;i<existMice.length;i++)
			if(!existMice[i]) winOperator.setInfoText("***Empty***", i);
		winOperator.setXYWindow(program,existMice);
	}

	/**
	 * �e�����ŗL�̃E�B���h�E�̐ݒ�B
	 */
	protected abstract void setEachWindow(ImageProcessor[] backIp);

	/********
	 *�@��͒��̈�A�̓�������郁�\�b�h�B��ʓI�ɂ́A�摜���擾���AAnalyze �ɓn���Ď��ۂ̉�͂����āA
	 *�@���ʂ��󂯎���ĕ\��������A�Ƃ���������J��Ԃ��B
	 *********/
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(allCage);
		finishSignal = new CountDownLatch(allCage);
		Runnable[] task = new Runnable[allCage];
		futures = new ScheduledFuture<?>[allCage];
		for(int cage=0;cage<allCage;cage++){
			task[cage] = createAnalyzeTask(cage);
		}

		//FrameRate=3�Ȃǂɂ���ƒ[���������
		final long PERIOD = (long)Math.round(1000000 / Parameter.getInt(Parameter.rate));

		readyToStart();

        timer = new Timer(allCage);

		for(int cage=0;cage<allCage;cage++){
			setStartTimeAndDate(cage);
		    futures[cage] = scheduler.scheduleAtFixedRate(task[cage], 0, PERIOD, TimeUnit.MICROSECONDS);
		    Toolkit.getDefaultToolkit().beep();
	    }

		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		if(interrupt() || timer.isInterrupt())
			   BehaviorDialog.showMessageDialog("Trial has been interrupted by user. time : " + timer.getEndTimebySec());
		timer.finalize();
	}

	protected void readyToStart(){}

	protected void setStartTimeAndDate(int cage){
		calendar[cage] = Calendar.getInstance();
		calendar[cage].set(Calendar.MONTH, Calendar.getInstance().get(Calendar.MONTH));
		calendar[cage].set(Calendar.DAY_OF_MONTH, Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
		calendar[cage].set(Calendar.YEAR, Calendar.getInstance().get(Calendar.YEAR));
		calendar[cage].set(Calendar.AM_PM, Calendar.getInstance().get(Calendar.AM_PM));
		calendar[cage].set(Calendar.HOUR, Calendar.getInstance().get(Calendar.HOUR));
		calendar[cage].set(Calendar.MINUTE, Calendar.getInstance().get(Calendar.MINUTE));
		calendar[cage].set(Calendar.SECOND, Calendar.getInstance().get(Calendar.SECOND));
	}

	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new DefaultAnalyzeTask(cage);
	}

	protected interface AnalyzeTask extends Runnable{
		public void run();
		public void isEnd();
	}

	public class DefaultAnalyzeTask implements AnalyzeTask,Runnable{
		protected int cage = 0;
		protected int allSlice;
		protected int cageSlice;
		protected boolean endAnalyze;
		protected String information;

		public DefaultAnalyzeTask(){}

		public DefaultAnalyzeTask(int cageNO){
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
			this.cage=cageNO;
		}

		@Override
		public void run(){
			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);

			analyze[cage].analyzeImage(currentIp);
			analyze[cage].calculate(cageSlice);

			winOperator.setXYText(cage, analyze[cage].getXY(cageSlice));
			setCurrentImage(cage, cageSlice, currentIp);

			if(analyze[cage].binUsed() && cageSlice != 0 && cageSlice%(Parameter.getInt(Parameter.binDuration)*Parameter.getInt(Parameter.rate)) == 0)
				analyze[cage].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 ������͂�����
				information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);//���}���u
				endAnalyze = true;
			}else{
				information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			}
			winOperator.setInfoText(information,cage);

			if((cageSlice-1) != allSlice){
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			}
			//Timer�Œ��f�����ꍇ
			if((timer != null && timer.isInterrupt()) || interrupt()){
				saveTraceInInterrupt(cage, cageSlice);
			    analyze[cage].interruptAnalysis();
			    isEnd();
			}

			if(endAnalyze) isEnd();

			cageSlice++;
		}

		public void isEnd(){
			futures[cage].cancel(true);
			finishSignal.countDown();
		}
	}

	/**
	 * ��������shift + alt�������ƒ��f����B
	 * @return shift + alt�������ꂽ���ǂ����B
	 */
	protected boolean interrupt(){
		if(IJ.shiftKeyDown() && IJ.altKeyDown()){
			IJ.showMessage("the current analysis was interrupted.(shift + alt key was pressed)");
			for(int cage = 0; cage < allCage; cage++)
				analyze[cage].interruptAnalysis();
			return true;
		}
		return false;
	}

	/**
	 * interrupt(timer)�ŏI�������ꍇ��trace��ۑ�����B
	 * RM,EP�Q�ƁB
	 */
	protected synchronized void saveTraceInInterrupt(int cage, int sliceNum){
		//setCurrentImage()��trace��ۑ�����̂Ɣ��Ȃ��悤�ɂ��邽�߁B
		if(analyze[cage].binUsed()){
		    if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			    return;
		}else{
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0)
				return;
		}

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		resSaver.addTraceImage(cage, traceIp);
	}

	/******
	 *���݂̉摜��\��������ۑ������肷��
	 *******/
	protected synchronized void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		resSaver.setCurrentImage(cage, currentIp);

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);
		if(analyze[cage].binUsed()){
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			    resSaver.addTraceImage(cage, traceIp);
		}else{
	        if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0){
				resSaver.addTraceImage(cage, traceIp);
			}
		}

		setEachCurrentImage(cage);	//abstract: ���ꂼ��̎����̉摜�\��
	}

	/**
	 * �e�����ŗL�̉摜�̕\���A�ۑ��B
	 */
	protected abstract void setEachCurrentImage(int cage);

	/******
	 * ���ʂ̕ۑ�������B
	 *******/
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());	//�ŏ��̉�͂̂Ƃ��́A���ʃt�H���_�Ƀw�b�_���L�ڂ���
		boolean writeVersion = (trialNum==1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		boolean[] endAnalyze = new boolean[allCage];
		for(int cage=0; cage<allCage; cage++){
			endAnalyze[cage] = (existMice[cage] && (analyze[cage].getState() != Analyzer.NOTHING));
	    }
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
	    winOperator.showOnlineTotalResult(program, allCage, endAnalyze, subjectID, totalResult);

		saveBinResult(writeVersion);
		saveOthers(subjectID, backIp);
	}

	/**
	 * �摜�̕ۑ��B
	 */
	protected void saveImage(ImageProcessor[] backIp){
		resSaver.saveImage(backIp);
	}

	/**
	 * bin���Ƃ̌��ʂ�ۑ�����B
	 */
	protected abstract void saveBinResult(boolean writeVerion);

	/**
	 * ���̑��ۑ�������̂�����ꍇ�͂����ɋL�q����B
	 */
	protected void saveOthers(String[] subjectID, ImageProcessor[] backIp){}
}