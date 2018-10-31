package behavior.plugin.executer;

import java.io.File;
import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;

import ij.ImagePlus;
import ij.process.ImageProcessor;

import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.gui.roi.RoiOperator;
import behavior.io.CSIResultSaver;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.io.SafetySaver;
import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.AnalyzerForStart;
import behavior.plugin.analyzer.CSIAnalyzer;
import behavior.plugin.analyzer.MountGraph;
import behavior.setup.CSISetup;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.parameter.Parameter;
import behavior.image.ImageCapture;

/**
 * CSIのためのExecuter(Online)
 * @author Butoh
 * @version Last Modified 091214
 */
public class CSIExecuter extends OnlineExecuter{
	private ImagePlus[] subtractImp;//cageImp;
	protected StringTokenizer st;
	protected MountGraph mountGraph;
	protected ImagePlus graphImp;
	private String rightCageID;
	private String leftCageID;
	private AnalyzerForStart startAnalyzer;

	public CSIExecuter(){
		program = Program.CSI;
		this.allCage = 1;

		String[] fileName = {"dist", "ST-Lcage","ST-Rcage","NE-Lcage", "NE-Rcage",
				"dist-Lcage","dist-Rcage","ST-Larea","ST-Rarea","ST-Center","NE-Larea",
				"NE-Rarea","NE-Center","dist-Larea","dist-Rarea","dist-Center"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected boolean setup(){
		ONLINE = true;
		imageCapture = ImageCapture.getInstance();

		if(imageCapture.setupFailed())
			return true;
		setup = new CSISetup(program, Setup.ONLINE, 1);
		if(setup.setup())
			return true;
		//setup.saveSession();
		roiOperator = new RoiOperator(program, allCage);
		analyze = new Analyzer[allCage];
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		calendar = new Calendar[allCage];
		return false;
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new CSIAnalyzer(backIp[0]);
		startAnalyzer = new AnalyzerForStart(backIp[0], program);
	}

	/***
	 *　ユーザーが実感する、設定から解析までのプロセス。
	 *　次の解析を問うダイアローグで yes を押すと、この部分が繰り返される
	 ***/
	@Override
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;
		
		if(setOtherParameter()) return true;
		if(roiOperator.loadRoi()) return true;
		ImageProcessor[] backIp = getBackground();	//バックグラウンド取得
		resSaver = new CSIResultSaver(program, allCage, backIp);
		subjectID = setup.getSubjectID();
		resSaver.setSubjectID(subjectID);

		existMice = setup.getExistMice();

		setWindow(backIp);  //表示するウィンドウの設定

		subSetup(backIp);  //abstract: 各実験固有のウィンドウ表示やそのほかの設定を行う

		leftCageID = ((CSISetup)setup).getOnlineLeftCageID();
	    rightCageID = ((CSISetup)setup).getOnlineRightCageID();	    
		//変更
		((CSIAnalyzer)analyze[0]).setSubCageID(leftCageID,rightCageID);
		saveSession();

		analyze(subjectID);	//解析
		save(trialNum, subjectID, backIp);	//保存

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
	 * ReferenceMemory用。SessionファイルにTrialNameと餌のあるアームの番号をTSVで記録。
	 */
	private void saveSession(){
		subjectID = setup.getSubjectID();
		final String path = FileManager.getInstance().getPath(FileManager.sessionPath);
		if(setup.isFirstTrial())
			(new FileCreate()).createNewFile(path);

		(new FileCreate()).write(path, subjectID[0] +"\t"+ leftCageID +"\t"+ rightCageID, true);
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new AnalyzeTaskCSI();
	}

	@Override
	protected void readyToStart(){
		int count = 0;
		for(;;){
		    if(startAnalyzer.startAnalyze(roiOperator.split((imageCapture.capture()).getProcessor(),0))){
		    	count++;
		    }else{
		    	count = 0;
		    }
		    if(count>1){
		    	break;
		    }
		    try{
		        TimeUnit.MILLISECONDS.sleep(100L);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
	}

	public class AnalyzeTaskCSI implements AnalyzeTask{
		private final int allSlice;
		private int cageSlice;
		private boolean endAnalyze;
		private String information;

		public AnalyzeTaskCSI(){
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

			setCurrentImage(CAGE, cageSlice, currentIp);

			if(cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[CAGE].nextBin();


			winOperator.setXYText(CAGE, analyze[CAGE].getXY(cageSlice));

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice+1);//応急処置
				endAnalyze = true;
			}else{
				information = analyze[CAGE].getInfo(subjectID[CAGE], cageSlice);
			}
			winOperator.setInfoText(information,0);

			if((cageSlice-1) != allSlice)
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));


			//Timerで中断した場合
			if(timer.isInterrupt() || interrupt()){
				saveTraceInInterrupt(CAGE, cageSlice);
			    analyze[CAGE].interruptAnalysis();
			    isEnd();
			}

			if(endAnalyze) isEnd();
			
			cageSlice++;
		}

		public void isEnd(){
			futures[0].cancel(true);
			finishSignal.countDown();
		}
	}

	/*
	 * ParticleRemove使用のためのメソッド
	 * 
	 */
	//private void removeparticle() {
		//removedImp = ParticleRemover.doRemove(((CSIAnalyzer)analyze[0]).getxyaData(),0, subtractImp[0]);
	//}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[1];
		subtractImp[0] = new ImagePlus("subtract1", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);

		graphImp = new ImagePlus("M Graph" , backIp[0]);
		mountGraph = new MountGraph(backIp[0]);
		graphImp.show();
		(graphImp.getWindow()).setLocation(backIp[0].getWidth()*2+180,200);
		//ImageProcessor a = ((CSIRoiOperator)roiOperator).split(backIp[0],CSIRoiOperator.LEFT);
		//_CageImp[LEFT] = new ImagePlus(((CSIAnalyzer)analyze[0]).getLeftCageID(),a);
		//_CageImp[RIGHT] = new ImagePlus(((CSIAnalyzer)analyze[0]).getRightCageID(),((CSIRoiOperator)roiOperator).split(backIp[0],CSIRoiOperator.RIGHT));
		//_CageImp[LEFT].show();
		//(_CageImp[LEFT].getWindow()).setLocation(backIp[0].getWidth()+45,500);
		//_CageImp[RIGHT].show();
		//(_CageImp[RIGHT].getWindow()).setLocation(backIp[0].getWidth()+195,500);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		ImageProcessor sub = ((CSIAnalyzer)analyze[cage]).getSubtractImage();
		subtractImp[cage].setProcessor("subtract" + (cage + 1), sub);
		if(sub!=null){
		    ((CSIResultSaver)resSaver).setSubtractImage(cage, sub);
		}
		graphImp.setProcessor("M Graph",mountGraph.createGraph(((CSIAnalyzer)analyze[cage]).getSubtractImage()));
//		cageImp[0].setProcessor(((SetupCSI)setup).getcage1ID(),((CSIRoiOperator)roiOperator).split(((CSIanalyze)analyze[cage]).getSubtractImage(),1));
//		cageImp[1].setProcessor(((SetupCSI)setup).getcage1ID(),((CSIRoiOperator)roiOperator).split(((CSIanalyze)analyze[cage]).getSubtractImage(),2));
	}

	@Override
	protected void saveBinResult( boolean writeVersion){
		int[] option = {CSIAnalyzer.BIN_DISTANCE,CSIAnalyzer.STLCAGE,CSIAnalyzer.STRCAGE,CSIAnalyzer.NELCAGE,CSIAnalyzer.NERCAGE,CSIAnalyzer.DISLCAGE,CSIAnalyzer.DISRCAGE
				,CSIAnalyzer.STLA,CSIAnalyzer.STRA,CSIAnalyzer.STCE,CSIAnalyzer.NELA,CSIAnalyzer.NERA,CSIAnalyzer.NECE,CSIAnalyzer.DISLA,CSIAnalyzer.DISRA,CSIAnalyzer.DISCE};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((CSIAnalyzer)analyze[cage]).getBinResult(option[num]);
//			if(option[num] == CSIanalyze.CAGE1){
//				binFileName[num] += "-"+((SetupCSI)setup).getcage1ID();
//			}else if(option[num] == CSIanalyze.CAGE2){
//				binFileName[num] += "-"+((SetupCSI)setup).getcage2ID();
//			}
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader, true,writeVersion); //area だけ binHeader をつけない
		}
	}

	@Override
	protected void saveOthers(String[] subjectID, ImageProcessor[] backIp){
		File graphDir = new File(FileManager.getInstance().getPath(FileManager.ImagesDir)+File.separator+"Mount");
		if(!graphDir.exists())
			graphDir.mkdir();
		SafetySaver ss = new SafetySaver();
		ss.saveImage(graphDir.getPath()+File.separator+subjectID[0]+".tif",new ImagePlus("M_Graph",mountGraph.getMountGraph()));
	}

	@Override
	protected void end(){
		resSaver.writeDate(true,true,binFileName);
	}
}