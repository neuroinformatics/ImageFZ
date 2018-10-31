package behavior.plugin.executer;

import java.io.File;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import behavior.gui.WindowOperator;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.plugin.analyzer.YMAnalyzer;
import behavior.setup.Program;

/**
 * Y-Maze‚Ì‚½‚ß‚ÌExecuter(Online)
 * @author Butoh
 * @version Last Modified 091214
 */
public class YMoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;

	public YMoffExecuter(){
		program = Program.YM;
		String[] fileName = {"dist"};
		binFileName = new String[fileName.length];
		for(int i = 0; i < fileName.length; i++)
			binFileName[i] = fileName[i];
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new YMAnalyzer(backIp[0]);
	}

	@Override
	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract1", ((YMAnalyzer)analyze[cage]).getSubtractImage());
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[1];
		subtractImp[0] = new ImagePlus("subtract1", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

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

		saveBinResult(writeHeader);
		saveOtherResults();
	}


	@Override
	protected void saveBinResult(final boolean writeVersion){
		int[] option = {YMAnalyzer.BIN_DISTANCE};
		for(int num = 0; num < option.length; num++){
			boolean writeHeader = !(new File(FileManager.getInstance().getSaveBinResultPath(binFileName[num])).exists());
			String[][] result = new String[allCage][];
			result[0] = ((YMAnalyzer)analyze[0]).getBinResult(option[num]);
			resSaver.saveOfflineBinResult(binFileName[num], result, writeHeader, writeVersion);
		}
	}

	private void saveOtherResults(){
		String[] subjectID = FileManager.getInstance().getSavePaths(FileManager.SubjectID);
		String path = FileManager.getInstance().getSavePath(FileManager.ResultsDir)+File.separator+FileManager.getInstance().getSavePath(FileManager.SessionID);

		String resultPath = path+"-AltState.txt";
		FileCreate creater = new FileCreate(resultPath);
		creater.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.ALTERNATION_STATE), true);

		String resultPath2 = path+"-EnterTime.txt";
		FileCreate creater2 = new FileCreate(resultPath2);
		creater2.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.ENTER_TIME), true);

		String resultPath3 = path+"-ExitTime.txt";
		FileCreate creater3 = new FileCreate(resultPath3);
		creater3.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.EXIT_TIME), true);

		String resultPath4 = path+"-StayTime.txt";
		FileCreate creater4 = new FileCreate(resultPath4);
		creater4.write(subjectID[0]+((YMAnalyzer)analyze[0]).getOtherResult(YMAnalyzer.FileType.STAY_TIME), true);
	}
}