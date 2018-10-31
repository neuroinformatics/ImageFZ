package behavior.plugin.executer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.RMAnalyzer;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;
import behavior.util.rmconstants.RMConstants;
import behavior.util.rmconstants.StateConstants;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

/**
 * RadialMazeのためのExecuter(Offline)
 * @author Butoh
 * @version Last Modified 091202
 */
public class RMOfflineExecuter extends OfflineExecuter {
	private final int ARM = 0;
	private final int STATE = 1;

	protected ImagePlus[] subtractImp;
	private List<String> armHistory = new ArrayList<String>();
	private List<String> episode = new ArrayList<String>();
	private String[] respectiveFileNames;

	public RMOfflineExecuter(){
		program = Program.RM;
		RMConstants.setOffline(true);

		respectiveFileNames = new String[2];
		respectiveFileNames[ARM] = "ARM";
		respectiveFileNames[STATE] = "STATE";
	}

	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new RMAnalyzer(backIp[0]);
	}

	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[allCage];
		subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_DOWN);
		winOperator.setRMArmWindow(program);
	}

	protected void setEachCurrentImage(final int cage) {
		subtractImp[cage].setProcessor("subtract",((RMAnalyzer) analyze[0]).getSubtractImage());
	}
	protected void analyze(ImageStack allImage){
		String previousText = "";
		String bufferText = "";
		allSlice = allImage.getSize();
		analyze[0].resetDuration(allSlice - 2);
		for(int slice = 1; slice < allSlice; slice++){
			if(((RMAnalyzer)analyze[0]).isEndAnalyze()) break;
			ImageProcessor currentIp = allImage.getProcessor(slice);
			analyze[0].analyzeImage(currentIp);
//			if(analyze.mouseExists()){  //マウスが認識されれば、解析と結果表示
			analyze[0].calculate(slice - 1);
			if(analyze[0].binUsed() && (slice - 1) != 0 && (slice - 1) % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0)
				analyze[0].nextBin();
			winOperator.setXYText(0, analyze[0].getXY(slice - 1));
			//armテキストの更新
			int counter = ((RMAnalyzer)analyze[0]).getArmCounter();
			int latestArmNum = ((RMAnalyzer)analyze[0]).getLatestVisitedArm();
			String latestEpisode = ((RMAnalyzer)analyze[0]).getLatestEpisode();
			if(counter != 0 && !previousText.equals(counter+"\t"+latestArmNum+"\t"+latestEpisode)){
				if(latestEpisode.equals("")){
					winOperator.setRMArmText(counter+"\t"+latestArmNum+"\t"+latestEpisode);
				}else{
					winOperator.setAllRMArmText(bufferText+counter+"\t"+latestArmNum+"\t"+latestEpisode);
					bufferText += counter+"\t"+latestArmNum+"\t"+latestEpisode+"\n";
				}
				previousText = counter+"\t"+latestArmNum+"\t"+latestEpisode;
			}
			setCurrentImage(slice - 1, currentIp);
//			}
		}
		addRestResult(allSlice - 2);
	}

	@Override
	protected void save(String subjectID, int trialNum,int cageNum,boolean showResult){
		boolean writeHeader = !(new File(FileManager.getInstance().getSavePath(FileManager.totalResPath)).exists());	//最初の解析のときは、結果フォルダにヘッダを記載する
		boolean writeVersion = (trialNum==1);
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

	@Override
	protected void saveBinResult(boolean writeVersion){
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
        	!(new File(FileManager.getInstance().getSavePath(FileManager.ResultsDir)+File.separator
        			+FileManager.getInstance().getSavePath(FileManager.SessionID)+"-"+respectiveFileNames[ARM]+".txt")).exists();
		resSaver.saveOfflineRepectiveResults(respectiveFileNames[ARM], "", headerNum, armHistory, writeHeader,writeVersion);
		writeHeader = 
        	!(new File(FileManager.getInstance().getSavePath(FileManager.ResultsDir)+File.separator
        			+FileManager.getInstance().getSavePath(FileManager.SessionID)+"-"+respectiveFileNames[STATE]+".txt")).exists();
		resSaver.saveOfflineRepectiveResults(respectiveFileNames[STATE], "", headerNum, episode, writeHeader,writeVersion);
	}
}