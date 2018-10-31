package behavior.plugin.executer;

import java.io.File;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.ResultSaver;
import behavior.io.SafetySaver;
import behavior.plugin.analyzer.Analyzer;
import behavior.plugin.analyzer.CSIAnalyzer;
import behavior.plugin.analyzer.MountGraph;
import behavior.setup.CSISetup;
import behavior.setup.Program;
import behavior.setup.Setup;
import behavior.setup.parameter.OldCSIParameter;
import behavior.setup.parameter.Parameter;

/**
 * CSI-oldのためのExecuter(Offline)
 * @author Butoh
 * @version Last Modified 091214
 */
public class CSIOldOffExecuter extends OfflineExecuter{
	//private final int LEFT = 0;
	//private final int RIGHT = 1;
	protected String _Cage1ID,_Cage2ID;
	//protected ImagePlus[] _CageImp = new ImagePlus[2];
	protected ImagePlus[] _SubtractImp;
	protected ImagePlus _GraphImp;
	protected MountGraph _MountGraph;
	//private CSIRoiOperator roiOperator;

	public CSIOldOffExecuter(){
		this.program = Program.OLDCSI;
		binFileName = new String[1];
		binFileName[0] = "dist";//全エリアをまとめて表記する
		/*binFileName[1] = "Rcage";//全エリアをまとめて表記する
		binFileName[2] = "Lcage";//全体の移動距離のみ
		binFileName[3] = "chamber";//全体の移動距離のみ*/
	}

	@Override
	protected boolean setup(){
		ONLINE = false;
		//変更
		setup = new CSISetup(program, Setup.OFFLINE, allCage);
		if(setup.setup())
			return true;
		//roiOperator = new CSIRoiOperator();
		currentImp = new ImagePlus[allCage];
		traceImp = new ImagePlus[allCage];
		analyze = new Analyzer[allCage];

		return false;
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp) {
		for(int cage = 0;cage < 1;cage++)
			analyze[cage] = new CSIAnalyzer(backIp[cage]);
	}

	@Override
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;

		String[] subjectIDs = setup.getSubjectID();
		String[] leftCageID = ((CSISetup)setup).getOfflineLeftCageID();
		String[] rightCageID = ((CSISetup)setup).getOfflineRightCageID();
		for(int cage = 0; cage < subjectIDs.length; cage++){
			subjectID = new String[1];
			subjectID[0] = subjectIDs[cage];	//ここで、現在使用する subjectID を指定
			(FileManager.getInstance()).setSubjectID(subjectID);
			ImageStack allImage = getSavedImage(subjectID[0]);
			allSlice = allImage.getSize();
			ImageProcessor[] backIp = getBackground(allImage);
			if(setOtherParameter(backIp)) break;
			resSaver = new ResultSaver(program, 1, backIp);
			resSaver.setSubjectID(subjectID);
			subSetup(backIp);
			//変更
			((CSIAnalyzer)analyze[0]).setSubCageID(leftCageID[cage],rightCageID[cage]);
			setWindow(subjectIDs[cage], backIp);
			analyze(allImage);
			save(subjectIDs[cage], trialNum + cage,cage,cage==subjectIDs.length-1);
			if(cage<subjectIDs.length-1)
			   winOperator.closeWindows();
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
	protected void setEachWindow(ImageProcessor[] backIp){
		_SubtractImp = new ImagePlus[1];
		_SubtractImp[0] = new ImagePlus("subtract1", backIp[0]);
		winOperator.setImageWindow(_SubtractImp, WindowOperator.RIGHT_UP);

		_GraphImp = new ImagePlus("M Graph" , backIp[0]);
		_MountGraph = new MountGraph(backIp[0]);
		_GraphImp.show();
		(_GraphImp.getWindow()).setLocation(backIp[0].getWidth()*2+180,160);
		//ImageProcessor a = ((CSIRoiOperator)roiOperator).split(backIp[0],CSIRoiOperator.LEFT);
		//_CageImp[LEFT] = new ImagePlus(((CSIAnalyzer)analyze[0]).getLeftCageID(),a);
		//_CageImp[RIGHT] = new ImagePlus(((CSIAnalyzer)analyze[0]).getRightCageID(),((CSIRoiOperator)roiOperator).split(backIp[0],CSIRoiOperator.RIGHT));
		//_CageImp[LEFT].show();
		//(_CageImp[LEFT].getWindow()).setLocation(backIp[0].getWidth()+45,500);
		//_CageImp[RIGHT].show();
		//(_CageImp[RIGHT].getWindow()).setLocation(backIp[0].getWidth()+195,500);
		winOperator.setInfoWindow(program);
	}

    @Override
	protected void setEachCurrentImage(int cage){
		_SubtractImp[cage].setProcessor("subtract1", ((CSIAnalyzer)analyze[cage]).getSubtractImage());

		_GraphImp.setProcessor("M Graph",_MountGraph.createGraph(((CSIAnalyzer)analyze[cage]).getSubtractImage()));

		/*if(a){
		_CageImp[LEFT].setProcessor(((CSIAnalyzer)analyze[0]).getLeftCageID(),
				          ((CSIRoiOperator)roiOperator).split(((CSIAnalyzer)analyze[cage]).getSubtractImage(),CSIRoiOperator.LEFT));
		}else{
			_CageImp[LEFT].setProcessor("subtract1", ((CSIAnalyzer)analyze[cage]).getSubtractImage());
		}
		b++;
		if(b>30)
		a=false;
		_CageImp[RIGHT].setProcessor(((CSIAnalyzer)analyze[0]).getRightCageID(),
				          ((CSIRoiOperator)roiOperator).split(((CSIAnalyzer)analyze[cage]).getSubtractImage(),CSIRoiOperator.RIGHT));*/
	}

    @Override
    protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		analyze[0].resetDuration(allSlice - 2);
		final int wait = Parameter.getInt(OldCSIParameter.wait);
		for(int slice = 1; slice < allSlice; slice++){
			ImageProcessor currentIp = allImage.getProcessor(slice);
			analyze[0].analyzeImage(currentIp);
			analyze[0].calculate(slice - 1);

			winOperator.setInfoText(analyze[0].getInfo(subjectID[0], slice),0);

			if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[0].nextBin();
			winOperator.setXYText(0, analyze[0].getXY(slice - 1));
			setCurrentImage(slice - 1, currentIp);
			
			try{
			    Thread.sleep(wait);
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		addRestResult(allSlice - 2);
	}

    @Override
	protected void saveBinResult(boolean writeVersion){
		int[] option = {CSIAnalyzer.BIN_DISTANCE};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[1][];
			result[0] = ((CSIAnalyzer)analyze[0]).getBinResult(option[num]);
			/*if(option[num] == CSIOldAnalyzer.RIGHTCAGE){
				binFileName[num] += "-"+((CSIOldAnalyzer)analyze[0]).getRightCageID();
			}else if(option[num] == CSIOldAnalyzer.LEFTCAGE){
				binFileName[num] += "-"+((CSIOldAnalyzer)analyze[0]).getLeftCageID();
			}*/
			
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader, true); //area だけ binHeader をつけない
		}
	}

	@Override
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		super.save(subjectID,trialNum,cageNum,showResult);
		File imageDir = new File(FileManager.getInstance().getSavePath(FileManager.ImagesDir));
		if(!imageDir.exists())
			imageDir.mkdir();
		File graphDir = new File(FileManager.getInstance().getSavePath(FileManager.ImagesDir)+File.separator+"Mount");
		if(!graphDir.exists())
			graphDir.mkdir();
		SafetySaver ss = new SafetySaver();
		ss.saveImage(graphDir.getPath()+File.separator+subjectID+".tif",new ImagePlus("M_Graph",_MountGraph.getMountGraph()));
		/*if(Parameter.getBoolean(SIParameter.useReference)){
			File refDir = new File(FileManager.getInstance().getPath(FileManager.ReferencesDir)+File.separator);
			if(!refDir.exists())
				refDir.mkdir();
			FileCreate fc = new FileCreate(refDir.getPath()+File.separator+subjectID+".txt");
			fc.createNewFile();
			fc.write(refDir.getPath()+File.separator+subjectID+".txt",((SetupSI)setup).getcage1ID()+":"+((SIanalyze)analyze[0]).getCageResults()[0],true);
			fc.write(refDir.getPath()+File.separator+subjectID+".txt",((SetupSI)setup).getcage2ID()+":"+((SIanalyze)analyze[0]).getCageResults()[1],true);
		}*/
	}
}