package behavior.plugin.executer;

import java.awt.Toolkit;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
//import java.util.logging.*;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import behavior.gui.BehaviorDialog;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.gui.roi.RoiOperator;
import behavior.image.ImageCapture;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.io.ResultSaver;
import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.RMAnalyzer;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.RMParameter;
import behavior.util.Timer;
import behavior.util.rmconstants.RMConstants;
import behavior.util.rmconstants.StateConstants;
import behavior.util.rmcontroller.RMController;
import behavior.util.rmstate.SensorMonitor;

/**
 * Radial Maze(Online)�p��Executer�B�W�{�̘r�������H�ł̃}�E�X�̍s������͂���B
 * Online�̒��ł��AWorkingMemory(���ׂẴA�[���ɉa������)�ƁAReferenceMemory�i�ꕔ�̃A�[���ɉa���Ȃ��j��2�̃��[�h������B
 *  
 * @author Butoh
 * @version Last Modified 091214
 */
public class RMExecuter extends OnlineExecuter{
	private final int ARM = 0;
	private final int STATE = 1;

	protected ImagePlus[] subtractImp;
	private SensorMonitor sensor;
	private List<String> armHistory = new ArrayList<String>();
	private List<String> episode = new ArrayList<String>();
	private String[] respectiveFileNames;
	private final boolean writeDate = true;

	//private Logger log = Logger.getLogger("behavior.plugin.executer.RMExecuter");

	public RMExecuter(){
		program = Program.RM;
		this.allCage = 1;
		RMConstants.setOffline(false);

		respectiveFileNames = new String[2];
		respectiveFileNames[0] = "ARM";
		respectiveFileNames[1] = "STATE";
	}

	@Override
	protected boolean setup(){
		ONLINE = true;
		imageCapture = ImageCapture.getInstance();
		if(imageCapture.setupFailed())
			return true;

		setup = new Setup(program, Setup.ONLINE, allCage);
		if(setup.setup())
			return true;

		//������ύX�AReferenceMemory�̏ꍇ�ASession�t�@�C���ɉa������A�[���̔ԍ����L�^���邽��
		if(RMConstants.isReferenceMemoryMode()){
		    saveSession();
		}else{
			setup.saveSession();
		}

		roiOperator = new RoiOperator(program, allCage);
		analyze = new Analyzer[allCage];
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		calendar = new Calendar[allCage];

		return false;
	}

	@Override
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;

		//������ύX�AReferenceMemory�̏ꍇ�ASession�t�@�C���ɉa������A�[���̔ԍ����L�^���邽��
		if(RMConstants.isReferenceMemoryMode()){
		    saveSession();
		}else{
			setup.saveSession();
		}

		if(roiOperator.loadRoi()) return true;
		ImageProcessor[] backIp = getBackground();
		resSaver = new ResultSaver(program, allCage, backIp);
		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

		setWindow(backIp);

		subSetup(backIp);
		analyze(subjectID);
		save(trialNum, subjectID, backIp);
		
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
			//�������I������Ƃ��ɑS�Ẵh�A��߂�
			if(!RMConstants.DEBUG){
			    BehaviorDialog.showMessageDialog("Close ALL doors.");
			    RMController.getInstance().closeAllDoors();
			}

			end();
			return true;
		}
	}

	/**
	 * ReferenceMemory�p�BSession�t�@�C����TrialName�Ɖa�̂���A�[���̔ԍ���TSV�ŋL�^�B
	 */
	private void saveSession(){
		subjectID = setup.getSubjectID();
		final String path = FileManager.getInstance().getPath(FileManager.sessionPath);
		if(setup.isFirstTrial())
			(new FileCreate()).createNewFile(path);

		(new FileCreate()).write(path, subjectID[0] +"\t"+ RMConstants.getFoodArmAlignment(), true);
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		    /*try{
			    FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "ExecuterLog.txt",102400,1);
			    fh.setFormatter(new SimpleFormatter());
		        log.addHandler(fh);
		    }catch(Exception e){
			    e.printStackTrace();
		    }*/

		if(!RMConstants.DEBUG){
			//���ׂẴh�A���J����i���[�^�[�̒����ɕK�v�Ƃ̂��Ɓj
			RMController.getInstance().openAllDoors();
			    
			    //final long start = System.currentTimeMillis();
			    //log.log(Level.INFO, "Opened all doors(for setup).");

			//5 seconds
			try{
			    Thread.sleep(5000);
			}catch(InterruptedException e){
				e.printStackTrace();
			}

			////���ׂẴh�A��߂�i�}�E�X�����邽�߁j
			RMController.getInstance().closeAllDoors();
		        //final long end = System.currentTimeMillis();
		        //log.log(Level.INFO, "Closed all doors(for setup) after " + (end-start) + "ms.");

			//NSense�o�Ȃ��ꍇ�ASensorMonitor�i�a�̃`�F�b�N�p�j��������
		    if(!Parameter.getBoolean(RMParameter.NSense))
		        SensorMonitor.getInstance().initialize();
		}
		analyze[0] = new RMAnalyzer(backIp[0]);
	}

	@Override
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(allCage);
		finishSignal = new CountDownLatch(allCage);
		Runnable[] task = new Runnable[allCage];
		futures = new ScheduledFuture<?>[allCage];
		for(int cage=0;cage<allCage;cage++){
			task[cage] = createAnalyzeTask(cage);
		}

		final long PERIOD = (long)Math.round(1000 / Parameter.getInt(Parameter.rate));

		readyToStart();

        timer = new Timer();

		setStartTimeAndDate(0);
	    futures[0] = scheduler.scheduleAtFixedRate(task[0], 0, PERIOD, TimeUnit.MILLISECONDS);
		Toolkit.getDefaultToolkit().beep();

		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		saveTrace();

		if(!(RMConstants.DEBUG || Parameter.getBoolean(RMParameter.NSense))){
		    sensor.end();
	     }

		if(interrupt() || timer.isInterrupt())
			   BehaviorDialog.showMessageDialog("Trial has been interrupted by user. time : " + timer.getEndTimebySec());
		timer.finalize();
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskRM();
	}

	//�_�C�A���O���o���AOK���N���b�N����Ɖ�͂��J�n����
	@Override
    protected void readyToStart(){
    	//�_�C�A���O���o��
        BehaviorDialog.showMessageDialog("please click OK to start");

        if(!RMConstants.DEBUG){
    	   	//SensorMonitor���N��
    	   	if(!Parameter.getBoolean(RMParameter.NSense)){
		         sensor = SensorMonitor.getInstance();
		         sensor.start();
    	   	}
    	    //��͊J�n��A���ׂẴh�A���J����
    	   	RMController.getInstance().openAllDoors();
    	}
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
		//�A�[���ԍ��A�G�s�\�[�h��\��
		winOperator.setRMArmWindow(program);
	}

	@Override
	protected void setEachCurrentImage(final int cage) {
		subtractImp[cage].setProcessor("subtract",((RMAnalyzer) analyze[0]).getSubtractImage());
	}

	public class AnalyzeTaskRM extends DefaultAnalyzeTask implements AnalyzeTask{
		private String previousText = "";
		private String bufferText = "";

		public AnalyzeTaskRM(){
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
		}

		@Override
		public void run(){
			final int CAGE = 0;

			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),CAGE);

			analyze[CAGE].analyzeImage(currentIp);
			analyze[CAGE].calculate(cageSlice);

			winOperator.setXYText(CAGE, analyze[CAGE].getXY(cageSlice));
			setCurrentImage(CAGE, cageSlice, currentIp);

			if(cageSlice == allSlice)
				endAnalyze = true;

			//�ǉ�
			//info�E�B���h�E�ɍ��킹�邽�߂��̈ʒu
			if((cageSlice-1) != allSlice)
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			
			information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice);	
			/* �����܂Ł@*/
			winOperator.setInfoText(information,CAGE);

			//ArmWindow�̃e�L�X�g�̍X�V
			int counter = ((RMAnalyzer)analyze[CAGE]).getArmCounter();
			int latestArmNum = ((RMAnalyzer)analyze[CAGE]).getLatestVisitedArm();
			String latestEpisode = ((RMAnalyzer)analyze[CAGE]).getLatestEpisode();
			if(counter != 0 && !previousText.equals(counter+"\t"+latestArmNum+"\t"+latestEpisode)){
				if(latestEpisode.equals("")){
					winOperator.setRMArmText(counter+"\t"+latestArmNum+"\t"+latestEpisode);
				}else{
					winOperator.setAllRMArmText(bufferText+counter+"\t"+latestArmNum+"\t"+latestEpisode);
					bufferText += counter+"\t"+latestArmNum+"\t"+latestEpisode+"\n";
				}
				previousText = counter+"\t"+latestArmNum+"\t"+latestEpisode;
			}

			//�ύX
			//Timer�Œ��f�����ꍇ
			if(timer.isInterrupt()){
				saveTraceInInterrupt(CAGE, cageSlice);
			    analyze[CAGE].interruptAnalysis();
			    isEnd();
			}

			//�L�[�{�[�h�Œ��f�����ꍇ
			if(interrupt()) isEnd();

			if(endAnalyze || ((RMAnalyzer)analyze[0]).isEndAnalyze()) isEnd();

			cageSlice++;
		}
	}

	private void saveTrace(){
		ImageProcessor traceIp = analyze[0].getTraceImage(0);
		if(traceIp != null)
		    resSaver.addTraceImage(0, traceIp);
	}

	//writeHeader�������ɂȂ�����saveOthers()�͖��ɗ����Ȃ�
	//�K�₵���A�[���A�G�s�\�[�h�����ꂼ��ʂ̃t�@�C���ɋL�^����
	@Override
	protected void saveBinResult(final boolean writeVersion){
		final int headerNum = 0;

		Iterator<Integer> it = ((RMAnalyzer)analyze[0]).getArmHistory().iterator();
        while(it.hasNext()){
        	String res = it.next().toString();
         	armHistory.add(res);
        }

        Iterator<StateConstants> it2 = ((RMAnalyzer)analyze[0]).getEpisode().iterator();
        while(it2.hasNext()){
         	String res = it2.next().getString();
         	episode.add(res);
        }

        boolean writeHeader = 
        	!(new File(FileManager.getInstance().getPath(FileManager.ResultsDir)+File.separator
        			+FileManager.getInstance().getPath(FileManager.SessionID)+"-"+respectiveFileNames[ARM]+".txt")).exists();
        //�K�₵���A�[���̔ԍ���TSV�ŏ��ɋL�^����
		resSaver.saveOnlineRepectiveResults(respectiveFileNames[ARM], "", headerNum, armHistory, writeHeader,writeVersion);
		writeHeader = 
        	!(new File(FileManager.getInstance().getPath(FileManager.ResultsDir)+File.separator
        			+FileManager.getInstance().getPath(FileManager.SessionID)+"-"+respectiveFileNames[STATE]+".txt")).exists();
		 //Intake�AOmission�Ȃǂ�TSV�ŏ��ɋL�^����
		resSaver.saveOnlineRepectiveResults(respectiveFileNames[STATE], "", headerNum, episode, writeHeader,writeVersion);
	}

	@Override
	protected void end() {
		resSaver.writeDate(writeDate, writeDate, respectiveFileNames);
	}
}