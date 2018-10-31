package behavior.plugin.executer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import ij.process.ImageProcessor;
import ij.*;

import behavior.plugin.analyzer.FZShockAnalyzer;
import behavior.io.FZResultSaver;
import behavior.io.FileManager;
import behavior.io.ImageLoader;
import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.setup.Program;
import behavior.setup.parameter.FZShockParameter;
import behavior.setup.parameter.Parameter;

public class FZShockOffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;
	private ArrayList<Integer> shockUnit = new ArrayList<Integer>();
	private Iterator<Integer> iter;
	private int duration = -1;

	public FZShockOffExecuter(){
		program = Program.FZS;
	}

	@Override
	protected boolean run(int trialNum){
		if(trialNum > 1 && setup.reSetup())
			return true;
		String[] subjectIDs = setup.getSubjectID();
		for(int cage = 0; cage < subjectIDs.length; cage++){
			subjectID = new String[1];
			subjectID[0] = subjectIDs[cage];	//ここで、現在使用する subjectID を指定
			(FileManager.getInstance()).setSubjectID(subjectID);
			ImageStack allImage = getSavedImage(subjectID[0]);
			allSlice = allImage.getSize();
			ImageProcessor[] backIp = getBackground(allImage);
			if(setOtherParameter(backIp)) break;
			resSaver = new FZResultSaver(program, 1, backIp);
			resSaver.setSubjectID(subjectID);
			setWindow(subjectIDs[cage], backIp);
			if(FZsubSetup(backIp)) return true;
			analyze(allImage);
			save(subjectIDs[cage], trialNum+cage,cage,cage==subjectIDs.length-1);
			if(cage<subjectIDs.length-1){
				winOperator.closeWindows();
			}
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
	protected ImageStack getSavedImage(String id){
		FileManager fManager = FileManager.getInstance();
		String[] subID = {id + "_shock"};
		fManager.setSubjectID(subID);
		return (new ImageLoader()).loadImage(fManager.getPath(FileManager.ImagesDir), File.separator + id + "_shock.tif");
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){/*no use*/}

	private boolean FZsubSetup(ImageProcessor[] backIp){
	    analyze[0] = new FZShockAnalyzer(backIp[0]);

	    shockUnit.clear();

	    String line;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.sessionPath)));
			while((line = reader.readLine()) != null){
				if(line.startsWith("#")) continue;

				String[] buf = line.split("\t");

				if(!buf[0].equals(subjectID[0])) continue;
				if(buf.length!=2) break;

				String[] unit = buf[1].split(" ");
				for(String str : unit){
					shockUnit.add(Integer.parseInt(str));
				}
			}
			reader.close();
		}catch(Exception e){
			IJ.showMessage(String.valueOf(e));
			return true;
		}

		iter = shockUnit.iterator();
		if(iter.hasNext()){
			duration = iter.next()+4;
		}else{
			duration = -1;
		}
		return false;
	}

	@Override
	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		for(int slice=1; slice<allSlice; slice++){
			ImageProcessor currentIp = allImage.getProcessor(slice);
			if(duration!=-1 && (slice-1)!=0 && (slice-1)%(duration*Parameter.getInt(FZShockParameter.shockCaptureRate))==0){
			    ((FZShockAnalyzer)analyze[0]).nextShock();
			}
			analyze[0].analyzeImage(currentIp);
			analyze[0].calculate(slice-1);
			winOperator.setXYText(0, analyze[0].getXY(slice-1));
			setCurrentImage(slice-1, currentIp);
		}
		addRestResult(allSlice-2);
	}

	@Override
	protected void addRestResult(int allSlice){
		ImageProcessor traceIp = ((FZShockAnalyzer)analyze[0]).getTraceImage(allSlice,duration);
		if(duration!=-1 && allSlice!=0 && allSlice%(duration*Parameter.getInt(FZShockParameter.shockCaptureRate))!=0){
	        resSaver.addTraceImage(0, traceIp);
	    }
	}

	@Override
	protected void setCurrentImage(int sliceNum, ImageProcessor currentIp){
		currentImp[0].setProcessor(subjectID[0], currentIp);
		ImageProcessor traceIp = ((FZShockAnalyzer)analyze[0]).getTraceImage(sliceNum,duration);
		traceImp[0].setProcessor("", traceIp);
		if(duration!=-1 && sliceNum!=0 && sliceNum%(duration*Parameter.getInt(FZShockParameter.shockCaptureRate))==0){
			resSaver.addTraceImage(0, traceIp);
			//((FZShockAnalyzer)analyze[0]).nextShock();
			if(iter.hasNext()){
				duration += iter.next()+4;
			}
		}
		setEachCurrentImage(0);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[1];
		subtractImp[0] = new ImagePlus("subtract 1", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void save(String subjectID,int trialNum, int cageNum,boolean showResult){
		StringBuilder br = new StringBuilder(FileManager.getInstance().getSavePath(FileManager.totalResPath));
		br.delete(br.length()-8,br.length());
		br.append("-shock_RES.txt");
		final boolean writeHeader = !(new File(br.toString()).exists());
		final boolean writeVersion = (trialNum == 1);
		boolean writeParameter;
		if(writeHeader){
			writeParameter = true;
		}else if(cageNum == 0){
			writeParameter = setup.isModifiedParameter();
		}else{
			writeParameter = false;
		}

		resSaver.saveOfflineTraceImage();

		resSaver.saveOfflineXYResult(0, winOperator.getXYTextPanel(0));

		String[] totalResult;
		totalResult = analyze[0].getResults();

		((FZResultSaver)resSaver).saveShockOfflineTotalResult(totalResult, writeHeader, writeVersion,  writeParameter,br.toString());

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
	protected void setEachCurrentImage(int cage){
		subtractImp[0].setProcessor("subtract" + 1, ((FZShockAnalyzer)analyze[0]).getSubtractImage());
	}

	@Override
	protected void saveBinResult(boolean writeVersion){
		((FZResultSaver)resSaver).saveOfflineShockRepectiveResults(((FZShockAnalyzer)analyze[0]).getRespectiveResults(),writeVersion);
	}
}