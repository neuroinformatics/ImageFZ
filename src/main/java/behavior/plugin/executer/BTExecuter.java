package behavior.plugin.executer;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import behavior.gui.roi.RoiOperator;
import behavior.gui.BehaviorDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.BTAnalyzer;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.controller.InputController;

/**
 * Beam-Test(Online)�p��Executer�B�_�̏�ɏ悹��ꂽ�}�E�X�̍s������͂���B
 * ���̎����ƈقȂ�����͎��̎l�B
 * �@�@1.Cage����1�ŌŒ肵�Ă���B
 * �@�@2.bin���g�p���Ȃ��B
 * �@�@3.�o�͐������ł͂Ȃ��f�[�^������������B
 * �@�@4.�����̏I���������l��ނ���B
 * �@�@�@�@�@�@�i�������ԁA�_���痎����AGoalArea�ɓ��B�AAlt+Shift�Ŏ����𒆒f�����ꍇ�j
 * 
 * @author Butoh
 * @version Last Modified 091214
 */
public class BTExecuter extends OnlineExecuter {
	//cage����1�ŌŒ肷��
	private final int CAGE = 1;
	private static InputController input;
	protected ImagePlus[] subtractImp;
	private String[] respectiveFileNames;
	private String[] respectiveHeaderNames;
	protected ScheduledFuture<?> future;

	/**
	 * BTExecuter���\�z���܂��B
	 * bin���g�p���Ȃ��Ƃ���œ��ٓI�ł��B
	 */
	// bin���Ƃɕ������Ȃ�
	public BTExecuter(){
		program = Program.BT;
		//���̂Ƃ���Cage����1�ŌŒ�A�Ȃ��Ȃ�Slip�̒l����x�ɕ�����Cage���Ƃ�͖̂����i�Ȃ͂��j������
		//���ɂ����낢��ʓ|�Ȃ̂�1�ŁB
		this.allCage = CAGE;
		
		String[] fileName = {"Distance Per Movement","Duration Per Movement","Speed Per Movement", "Sliped Time"};
		respectiveFileNames = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			respectiveFileNames[i] = fileName[i];

		String[] headerName = {"Phase", "Phase","Phase","Slip"};
		respectiveHeaderNames = new String[headerName.length];
		for(int i = 0; i < headerName.length; i++)
			respectiveHeaderNames[i] = headerName[i];
	}

	/**
	 * BT�ŗL�̃Z�b�g�A�b�v���s���܂��B
	 * @param backIp -�o�b�N�O���E���h�摜
	 */
	@Override
	protected void subSetup(ImageProcessor[] backIp){
		//InputController�̃C���X�^���X���擾
		if(input != null)
			input.close();
		input = InputController.getInstance(InputController.PORT_IO);

		//GoalTime�̎擾�̂��߂�MainArea��Rectangle��n���K�v������
		Roi[] fieldRoi = ((RoiOperator) roiOperator).getRoi();
		Rectangle[] fieldRec = new Rectangle[1];
		fieldRec[0] = fieldRoi[0].getBounds();

		//cage����1�ŌŒ肳��Ă���
		analyze[0] = new BTAnalyzer(backIp[0]);
		((BTAnalyzer) analyze[0]).createField(fieldRec[0]);
	}

	/******
	 * ���ʂ̕ۑ�������B
	 *******/
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());
		boolean writeVersion = (trialNum == 1);

		int activeCage = 1;	//���ʂ�ۑ�����P�[�W���B�r���� interrupt ���ꂽ�P�[�W�̌��ʂ͕ۑ�����Ȃ��B
		boolean[] endAnalyze = new boolean[allCage];
		Arrays.fill(endAnalyze, true);
		resSaver.setActiveCage(endAnalyze);

		saveImage(backIp);

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];

		resSaver.saveXYResult(0, winOperator.getXYTextPanel(0));
		totalResult[0] = analyze[0].getResults();

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion,setup.isModifiedParameter());

	    winOperator.showOnlineTotalResult(program, activeCage, subjectID, totalResult);

		saveBinResult(writeHeader);
		saveOthers(subjectID, backIp);
	}

	/**
	 * BT�ŗL�̉摜�E�B���h�E�̐ݒ���s���܂�
	 * @param backIp -�o�b�N�O���E���h�摜
	 */
	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
        subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
	}

	/**
	 * BTAnalyze���猻�݂̉摜���擾���܂��B
	 * @param cage -�P�[�W��
	 */
	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract",((BTAnalyzer) analyze[0]).getSubtractImage());
	}

	@Override
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
		finishSignal = new CountDownLatch(1);
		Runnable task = new AnalyzeTaskBT();
		final long PERIOD = (long)Math.round(1000 / Parameter.getInt(Parameter.rate));

		readyToStart();
		setStartTimeAndDate(0);

		future = scheduler.scheduleAtFixedRate(task, 0, PERIOD, TimeUnit.MILLISECONDS);

		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		//�ǉ�
		if(interrupt())
			BehaviorDialog.showMessageDialog("Trial has been interrupted by user.");
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){return null;}

	@Override
	protected void readyToStart(){
		for(;;){
			if(input.getInput(0))
				break;
			try{
			    Thread.sleep(100);
			}catch(Exception e){
				e.printStackTrace();
				break;
			}
		}
	    Toolkit.getDefaultToolkit().beep();

	    ((BTAnalyzer)analyze[0]).setStart(true);
	}


	public class AnalyzeTaskBT implements AnalyzeTask{
		private final int allSlice;
		private int cageSlice;
		private boolean endAnalyze;
		private String information;

		public AnalyzeTaskBT(){
            allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;	
			endAnalyze = false;
		}

		@Override
		public void run(){
			final int CAGE = 0;

			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),CAGE);;
			/* ����for���[�v���� */
			analyze[CAGE].analyzeImage(currentIp);
			analyze[CAGE].calculate(cageSlice);

			winOperator.setXYText(CAGE, analyze[CAGE].getXY(cageSlice));
			setCurrentImage(CAGE, cageSlice, currentIp);

			if(cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[CAGE].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 ������͂�����
				information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice+1);//���}���u
				endAnalyze = true;
			}else{
				information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice);
			}
			winOperator.setInfoText(information,0);

			//�L�[�{�[�h�Œ��f�����ꍇ
			if(interrupt()) isEnd();

			if(endAnalyze || ((BTAnalyzer)analyze[0]).isGoal()) isEnd();

			cageSlice++;
		}

		public void isEnd(){
			future.cancel(true);
			finishSignal.countDown();
		}
	}

	/**
	 * ���ʃt�@�C�����쐬���܂��B
	 */
	@Override
	protected void end(){
		final boolean writeDate = true;
		resSaver.writeDate(writeDate,writeDate,respectiveFileNames);
	}

	/**
	 * bin���Ƃ̉�͌��ʂ̃t�@�C�����쐬���܂��B
	 * �A��BT�ł�bin�ł͂���܂���B
	 */
	//�c�ȗ��p
	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {BTAnalyzer.DISTANCE_PER_MOVEMENT, BTAnalyzer.DURATION_PER_MOVEMENT, BTAnalyzer.SPEED_PER_MOVEMENT, BTAnalyzer.SLIPED_TIME};
		for(int num = 0; num < option.length; num++){
			List<String> result;
			result = ((BTAnalyzer)analyze[0]).getRespectiveResults(option[num]);
			//�v�f��0�������ꍇ��NullPo�h�~
			//���ۂɕK�v���͎����Ă��Ȃ�
			if(result.size()==0)
			    result.add("");
			//�w�b�_�ɋL�������
			final int headerNum = 10;
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(respectiveFileNames[num])).exists());
			resSaver.saveOnlineRepectiveResults(respectiveFileNames[num], respectiveHeaderNames[num], headerNum, result, writeHeader,writeVersion);
		}
	}
}