package behavior.plugin.executer;

import java.io.File;

import ij.process.ImageProcessor;
import ij.*;

import behavior.gui.WindowOperator;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.io.SafetySaver;
import behavior.setup.parameter.Parameter;
import behavior.plugin.analyzer.AnalyzerForStart;
import behavior.plugin.analyzer.Episode;
import behavior.plugin.analyzer.OFCAnalyzer;
import behavior.setup.Program;

public class OFCExecuter extends OFExecuter{
	private ImagePlus[] subtractImp, episodeImp;
	//private InputController input;

	public OFCExecuter(int allCage){
		program = Program.OFC;
		this.allCage = allCage;

		//binFileName = new String[5];
		binFileName = new String[3];
		binFileName[0] = "dist";
		binFileName[1] = "ctime";
		binFileName[2] = "area";
		//binFileName[3] = "nRear";
		//binFileName[4] = "durRear";

		//input = InputController.getInstance(InputController.PORT_D);
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		startAnalyzer = new AnalyzerForStart[allCage];
		for(int cage=0;cage<allCage;cage++){
			analyze[cage] = new OFCAnalyzer(backIp[cage]);
			startAnalyzer[cage] = new AnalyzerForStart(backIp[cage],program);
		}
	}

	@Override
	protected AnalyzeTask createAnalyzeTask(final int cage){
		return new OFCAnalyzeTask(cage);
	}

	public class OFCAnalyzeTask implements AnalyzeTask{
		protected int cage = 0;
		protected int allSlice;
		protected int cageSlice;
		protected boolean endAnalyze;
		protected String information;

		public OFCAnalyzeTask(int cageNO){
			allSlice = Parameter.getInt(Parameter.rate) * Parameter.getInt(Parameter.duration);
			cageSlice = 0;
			endAnalyze = false;
			this.cage=cageNO;
		}

		@Override
		public void run(){
			try{
			/*if(input.getInput(cage) == true)
				((OFCAnalyzer)analyze[cage]).isRearing();*/
			ImageProcessor currentIp = roiOperator.split((imageCapture.capture()).getProcessor(),cage);

			analyze[cage].analyzeImage(currentIp);
			analyze[cage].calculate(cageSlice);

			winOperator.setXYText(cage, analyze[cage].getXY(cageSlice));
			setCurrentImage(cage, cageSlice, currentIp);

			if(analyze[cage].binUsed() && cageSlice != 0 && cageSlice%(Parameter.getInt(Parameter.binDuration)*Parameter.getInt(Parameter.rate)) == 0)
				analyze[cage].nextBin();

			if(cageSlice == allSlice){	//allSlice + 1 枚分解析をする
				information = analyze[cage].getInfo(subjectID[cage], cageSlice+1);//応急処置
				endAnalyze = true;
			}else{
				information = analyze[cage].getInfo(subjectID[cage], cageSlice);
			}
			winOperator.setInfoText(information,cage);

			if(timer != null &&allCage==1 && (cageSlice-1) != allSlice){
	            timer.run((double)cageSlice/Parameter.getInt(Parameter.rate));
			}
			//Timerで中断した場合
			if((timer != null && timer.isInterrupt()) || interrupt()){
				saveTraceInInterrupt(cage, cageSlice);
			    analyze[cage].interruptAnalysis();
			    isEnd();
			}

			if(endAnalyze) isEnd();

			cageSlice++;
			}catch(Throwable e){
				e.printStackTrace();
			}
		}

		public void isEnd(){
			futures[cage].cancel(true);
			finishSignal.countDown();
		}
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage+1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);

		episodeImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			episodeImp[cage] = new ImagePlus("episode" + (cage+1), backIp[cage]);
		winOperator.setImageWindow(episodeImp, WindowOperator.RIGHT_DOWN);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage+1), ((OFCAnalyzer)analyze[cage]).getSubtractImage());
		episodeImp[cage].setProcessor("episode" + (cage+1), ((OFCAnalyzer)analyze[cage]).getEpisodeTrace());

		/*
		if(input.getInput(cage) == true)
			((OFCAnalyzer)analyze[cage]).isRearing();
		*/
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

		resSaver.saveTraceImage();

		String[][] totalResult = new String[allCage][];
		for(int cage = 0; cage < allCage; cage++){
			if(!existMice[cage]) continue;
	        resSaver.saveXYResult(cage, winOperator.getXYTextPanel(cage));
    		totalResult[cage] = analyze[cage].getResults();
		}

		resSaver.saveOnlineTotalResult(totalResult,calendar, writeHeader, writeVersion, writeParameter);
		winOperator.showOnlineTotalResult(program, allCage, subjectID, totalResult);

		saveBinResult(writeHeader);
		saveOthers(subjectID, backIp);
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		//OF と同じ内容を保存
		super.saveBinResult(writeVersion);

		/*int[] option = {OFCAnalyzer.BIN_REAR_TIMES, OFCAnalyzer.BIN_REAR_SECOND};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			for(int cage = 0; cage < allCage; cage++)
				result[cage] = ((OFCAnalyzer)analyze[cage]).getBinResult(option[num]);
			resSaver.saveBinResult(binFileName[num+3], result, writeHeader, writeVersion);
		}*/
	}

	@Override
	protected void saveOthers(String[] subjectID, ImageProcessor[] backIp){
		//ここからは circle 関係のデータを保存
		String header = " ID"+"\t"+"obtuse left"+"\t"+"obtuse right"+"\t"+"sharp left"+"\t"+"sharp right"+
		                      "\t"+"straight"+"\t"+"how finished"+"\t"+"dominant turn"+"\t"+"circle";
		for(int cage=0; cage<allCage; cage++){
			FileManager fm = FileManager.getInstance();
			String path = fm.getBinResultPath("circle-" + subjectID[cage]);
			FileCreate fc = new FileCreate(path);
			if(!(new File(path).exists())){
				fc.createNewFile();
			    fc.write(header, true);
			}
			for(Episode ep : ((OFCAnalyzer)analyze[cage]).getEpisodeList()){
				fc.write(ep.getResult(), true);
			}
		}

		//Episode トレースの保存
		FileManager fm = FileManager.getInstance();
		for(int cage=0; cage<allCage; cage++){
			SafetySaver saver = new SafetySaver();
			saver.saveImage(fm.getPath(FileManager.TracesDir)+ "/" + subjectID[cage] + "-episode.tif", ((OFCAnalyzer)analyze[cage]).getEpisodeTraceStack());
		}

		//circle の total result を保存
		String[][] result = new String[allCage][];
		for(int cage = 0; cage < allCage; cage++)
			result[cage] = ((OFCAnalyzer)analyze[cage]).getCircleResults();

		String path = (FileManager.getInstance()).getPath(FileManager.totalResPath);
		int dot = path.lastIndexOf(".");
		path = path.substring(0, dot);
		path += "-circle.txt";
		FileCreate fc = new FileCreate(path);
		if(!(new File(path).exists())){
			String circleHeader = " ID"+"\t"+"Number of total episodes"+"\t"+"Number of total circlings"+
                                     "\t"+"Number of left circlings"+"\t"+"Number of right circlings";
		    fc.createFile();
		    fc.write(circleHeader, true);
		}
		for(int cage = 0; cage < allCage; cage++){
			String data = subjectID[cage];
			for(int i = 0; i < result[cage].length; i++)
				data += "\t" + result[cage][i];
			fc.write(data, true);
		}
	}

	@Override
	public void end(){
		super.end();
		FileCreate fc = new FileCreate();
		for(int cage=0; cage<allCage; cage++){
			String path = (FileManager.getInstance()).getBinResultPath("circle-" + subjectID[cage]);
		    fc.writeDate(path);
		}
		//circle total result に日時をつける
		String path = (FileManager.getInstance()).getPath(FileManager.totalResPath);
		int dot = path.lastIndexOf(".");
		path = path.substring(0, dot);
		path += "-circle.txt";
		fc.writeDate(path);
	}
}