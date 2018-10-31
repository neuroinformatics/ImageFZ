package behavior.plugin.executer;

import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.TSAnalyzer;
import behavior.controller.InputController;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.SafetySaver;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.util.Timer;

import java.util.logging.*;

/**
 * TailSuspentionのためのExecuter(Offline)
 * @author Butoh
 * @version Last Modified 091214
 */
public class TSExecuter extends OnlineExecuter{
	protected ImagePlus[] subtractImp, xorImp;
	private InputController input;
	private ImageStack[] xorImageStack;
	private Logger log = Logger.getLogger("behavior.plugin.executer.TSExecuter");

	public TSExecuter(int allCage){
		program = Program.TS;
		this.allCage = allCage;
		binFileName = new String[3];
		binFileName[0] = "dist";
		binFileName[1] = "immobile";
		binFileName[2] = "area";
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++)
			if(existMice[cage])
			    analyze[cage] = new TSAnalyzer(backIp[cage]);

		xorImageStack = new ImageStack[allCage];
		for(int cage = 0; cage < allCage; cage++)
			if(existMice[cage])
			    xorImageStack[cage] = (new ImagePlus("xor", backIp[cage])).createEmptyStack();

		// Input Device の初期化
		if(input != null)
			input.close();

		IJ.getInstance().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		input = InputController.getInstance(InputController.PORT_IO);

		try{
			FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "ImageErrorLog.txt", 102400,1);
			fh.setFormatter(new SimpleFormatter());
		    log.addHandler(fh);
		}catch(Exception e){
	       e.printStackTrace();
		}
	}

	@Override
	protected void analyze(String[] subjectID){
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(allCage);
		finishSignal = new CountDownLatch(allCage);
		Runnable[] task = new Runnable[allCage];
		futures = new ScheduledFuture<?>[allCage];
		for(int cage=0;cage<allCage;cage++){
			task[cage] = new AnalyzeTaskTS(cage);
		}

		final long PERIOD = (long)Math.round(1000 / Parameter.getInt(Parameter.rate));

		boolean[] isStart = new boolean[allCage];
		for(int i=0;i<allCage;i++){
			if(existMice[i]){
				isStart[i] = false;
			}else{
				isStart[i] = true;
				finishSignal.countDown();
			}
		}
		boolean isEnd;
		for(;;){
			for(int i=0;i<allCage;i++){
			    if(!isStart[i] && input.getInput(i)){
					setStartTimeAndDate(i);
			        futures[i] = scheduler.scheduleAtFixedRate(task[i], 0, PERIOD, TimeUnit.MILLISECONDS);
			        Toolkit.getDefaultToolkit().beep();
			        isStart[i]=true;
			    }
			}

			isEnd = true;
			for(int i=0;i<isStart.length;i++){
				isEnd &= isStart[i];
			}

			if(isEnd){
				timer = new Timer(allCage);
				break;
			}

			try{
				TimeUnit.MILLISECONDS.sleep(100);
			}catch(InterruptedException e){
				e.printStackTrace();
			}
		}
		try{
		    finishSignal.await();
		}catch(InterruptedException e){
			e.printStackTrace();
		}
		scheduler.shutdown();

		timer.finalize();
	}

	public class AnalyzeTaskTS implements AnalyzeTask{
		private int cage;
		private final int allSlice;
		private int cageSlice;
		private boolean endAnalyze;
		private String information;

		public AnalyzeTaskTS(int cageNO){
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
			this.cage=cageNO;
		}

		@Override
		public void run(){
			if(timer != null && timer.isSetTimerScreen() && timer.isInterrupt()){
				analyze[cage].interruptAnalysis();
				isEnd();
			}

			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);

			Object pixel = currentIp.getPixels();
			if(!(pixel instanceof byte[])){
				log.log(Level.INFO, "8bit Error at "+ cageSlice+ ".Type is "+pixel.getClass().getName());
			}

			analyze[cage].analyzeImage(currentIp);	
            analyze[cage].calculate(cageSlice);
			winOperator.setXYText(cage, analyze[cage].getXY(cageSlice));
			setCurrentImage(cage, cageSlice, currentIp);
			if(analyze[cage].binUsed() && cageSlice != 0 && cageSlice % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0){
				analyze[cage].nextBin();
			}

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);//応急処置
				endAnalyze = true;
			}else{
				information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			}
			winOperator.setInfoText(information,cage);

			//キーボードで中断させない
			//if(interrupt()) isEnd();
			if(timer != null && timer.isSetTimerScreen() && timer.isInterrupt()){
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

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		xorImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++){
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
			xorImp[cage] = new ImagePlus("xor" + (cage + 1), backIp[cage]);
		}
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN,existMice);
		winOperator.setImageWindow(xorImp, WindowOperator.RIGHT_UP,existMice);
	}

	@Override
	protected synchronized void setCurrentImage(int cage, int sliceNum, ImageProcessor currentIp){
		currentImp[cage].setProcessor(subjectID[cage], currentIp);
		resSaver.setCurrentImage(cage, currentIp);

		ImageProcessor traceIp = analyze[cage].getTraceImage(sliceNum);
		traceImp[cage].setProcessor("trace" + (cage + 1), traceIp);
		if(analyze[cage].binUsed()){
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
			    resSaver.addTraceImage(cage, traceIp);
		}else{
			if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.duration) * Parameter.getInt(Parameter.rate)) == 0)
				resSaver.addTraceImage(cage, traceIp);
		}

		setEachCurrentImage(cage,sliceNum);	//abstract: それぞれの実験の画像表示
	}

	protected void setEachCurrentImage(int cage,int num){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((TSAnalyzer)analyze[cage]).getSubtractImage());
		xorImp[cage].setProcessor("xor" + (cage + 1), ((TSAnalyzer)analyze[cage]).getXorImage());

		if(num!=0)
		xorImageStack[cage].addSlice("xor", ((TSAnalyzer)analyze[cage]).getXorImage().convertToByte(false));
	}

	@Override
	protected void save(int trialNum, String[] subjectID, ImageProcessor[] backIp){
		boolean writeHeader = !(new File(FileManager.getInstance().getPath(FileManager.totalResPath)).exists());	//最初の解析のときは、結果フォルダにヘッダを記載する
		boolean writeVersion = (trialNum==1);
		boolean writeParameter;
		if(trialNum==1){
			writeParameter = true;
		}else{
			writeParameter = setup.isModifiedParameter();
		}

		resSaver.setActiveCage(existMice);

		saveImage(backIp);

		new File(FileManager.getInstance().getPath(FileManager.ImagesDir)+File.separator+"XOR").mkdir();
		for(int cage = 0; cage < allCage; cage++){
			if(!existMice[cage]) continue;
		    SafetySaver saver = new SafetySaver();
		    saver.saveImage(FileManager.getInstance().getPath(FileManager.ImagesDir)+File.separator+"XOR"+File.separator+FileManager.getInstance().getPaths(FileManager.SubjectID)[cage]+"_XOR.tif", xorImageStack[cage]);
		}

		resSaver.saveTraceImage();

		for(int cage = 0; cage < allCage; cage++){
			if(!existMice[cage]) continue;
			resSaver.saveXYResult(cage,  ((TSAnalyzer)analyze[cage]).getXYText());
		}

		String[][] totalResult = new String[allCage][0];
		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);

		saveBinResult(writeVersion);

		/*if(trialNum == 1){
		    FileCreate fcr = new FileCreate(FileManager.getInstance().getPath(FileManager.PreferenceDir)+File.separator+"VideoFormat.txt");
		    fcr.createNewFile();
		    fcr.write(FileManager.getInstance().getFormat(), false);
		}*/
		
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {TSAnalyzer.BIN_DISTANCE, TSAnalyzer.BIN_FREEZ_PERCENT, TSAnalyzer.BIN_XOR_AREA};

		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++){
				if(!existMice[cage]) continue;
				result[cage] = ((TSAnalyzer)analyze[cage]).getBinResult(option[num]);
			}
			resSaver.saveOnlineBinResult(binFileName[num], result, writeHeader,writeVersion);
		}
	}

	@Override
	protected void end(){
		resSaver.writeDate(false, true, binFileName);
	}

	@Override
	protected void setEachCurrentImage(int cage){}
}