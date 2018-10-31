package behavior.plugin.executer;

import java.io.File;

import ij.process.ImageProcessor;
import ij.*;

import behavior.gui.NextAnalysisDialog;
import behavior.gui.WindowOperator;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.io.ResultSaver;
import behavior.io.SafetySaver;
import behavior.plugin.analyzer.Episode;
import behavior.plugin.analyzer.OFCAnalyzer;
import behavior.setup.Program;

public class OFCoffExecuter extends OFoffExecuter{
	private ImagePlus[] subtractImp, episodeImp;

	public OFCoffExecuter(){
		program = Program.OFC;
		binFileName = new String[3];
		binFileName[0] = "dist";
		binFileName[1] = "ctime";
		binFileName[2] = "area";
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
			resSaver = new ResultSaver(program, 1, backIp);
			resSaver.setSubjectID(subjectID);
			setWindow(subjectIDs[cage], backIp);
			subSetup(backIp);
			analyze(allImage);
			save(subjectIDs[cage], trialNum+cage,cage,cage==subjectIDs.length-1);
			saveOthers(subjectIDs[cage], backIp[0]);
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
	protected void subSetup(ImageProcessor[] backIp){	
		for(int cage = 0; cage < allCage; cage++)
			analyze[cage] = new OFCAnalyzer(backIp[cage]);
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp){
		subtractImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			subtractImp[cage] = new ImagePlus("subtract" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
		episodeImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			episodeImp[cage] = new ImagePlus("episode" + (cage + 1), backIp[cage]);
		winOperator.setImageWindow(episodeImp, WindowOperator.RIGHT_DOWN);
	}

	@Override
	protected void setEachCurrentImage(int cage){
		subtractImp[cage].setProcessor("subtract" + (cage + 1), ((OFCAnalyzer)analyze[cage]).getSubtractImage());
		episodeImp[cage].setProcessor("episode" + (cage + 1), ((OFCAnalyzer)analyze[cage]).getEpisodeTrace());
	}

	protected void saveOthers(String subjectID, ImageProcessor backIp){
		FileManager fm = FileManager.getInstance();

		//ここからは circle 関係のデータを保存
		String path = fm.getSaveBinResultPath("circle-" + subjectID);
		FileCreate fc = new FileCreate(path);
		if(!(new File(path).exists())){
			String header = " ID"+"\t"+"obtuse left"+"\t"+"obtuse right"+"\t"+"sharp left"+"\t"+"sharp right"+
			                     "\t"+"straight"+"\t"+"how finished"+"\t"+"dominant turn"+"\t"+"circle";
			fc.createNewFile();
			fc.write(header, true);
		}
		for(Episode ep : ((OFCAnalyzer)analyze[0]).getEpisodeList()){
			fc.write(ep.getResult(), true);
		}

		//Episode トレースの保存
		SafetySaver saver = new SafetySaver();
		saver.saveImage(fm.getSavePath(FileManager.TracesDir)+ "/" + subjectID + "-episode.tif", ((OFCAnalyzer)analyze[0]).getEpisodeTraceStack());

		//circle の total result を保存
		String circlePath = (FileManager.getInstance()).getSavePath(FileManager.totalResPath);
		int dot = circlePath.lastIndexOf(".");
		circlePath = circlePath.substring(0, dot);
		circlePath += "-circle.txt";
		FileCreate fc2 = new FileCreate(circlePath);
		if(!(new File(circlePath).exists())){
			String circleHeader = " ID"+"\t"+"Number of total episodes"+"\t"+"Number of total circlings"+
			                           "\t"+"Number of left circlings"+"\t"+"Number of right circlings";
			fc2.createFile();
			fc2.write(circleHeader, true);
		}
		String[] result = ((OFCAnalyzer)analyze[0]).getCircleResults();
		String data = subjectID;
		for(int i=0; i<result.length; i++){
			data += "\t" + result[i];
	    }
		fc2.write(data, true);
	}
}