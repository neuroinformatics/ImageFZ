package behavior.io;

import java.io.File;
import java.util.List;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import behavior.Version;
import behavior.setup.Header;
import behavior.setup.Program;

public class FZResultSaver extends ResultSaver{
	private ImageStack[] shockStack;
	private ImageStack[] subtractStack;
	private Object lock1 = new Object();
	private Object lock2 = new Object();

	public FZResultSaver(Program program, int allCage, ImageProcessor[] backIp) {
		super(program, allCage, backIp);
		ImagePlus[] imageImp = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			imageImp[cage] = new ImagePlus("", backIp[cage]);
		StackBuilder sb = new StackBuilder(allCage);
		imageImp = sb.buildStack(imageImp);
		shockStack = sb.getStack(imageImp);
		sb.deleteSlice(shockStack, 1);

		ImagePlus[] imageImp2 = new ImagePlus[allCage];
		for(int cage = 0; cage < allCage; cage++)
			imageImp2[cage] = new ImagePlus("", backIp[cage]);
		StackBuilder sb2 = new StackBuilder(allCage);
		imageImp2 = sb2.buildStack(imageImp2);
		subtractStack = sb2.getStack(imageImp2);
		sb2.deleteSlice(subtractStack, 1);
	}

	public void setSubtractImage(int cage, ImageProcessor currentIp){
		synchronized(lock1){
		    subtractStack[cage].addSlice("image", currentIp.convertToByte(false));
		}
	}

	public void setShockImage(int cage, ImageProcessor currentIp){
		synchronized(lock2){
		    shockStack[cage].addSlice("image", currentIp.convertToByte(false));
		}
	}

	@Override
	public void saveImage(ImageProcessor[] backIp){
		super.saveImage(backIp);
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;

			SafetySaver saver = new SafetySaver();
			StringBuilder br2 = new StringBuilder(FileManager.getInstance().getPaths(FileManager.imagePath)[cage]);
			br2.delete(br2.length()-4,br2.length());
			br2.append("_subtract.tif");
			saver.saveImage(br2.toString(), subtractStack[cage]);
		}
	}

	public void saveShockImage(ImageProcessor[] backIp){
		for(int cage = 0; cage < allCage; cage++){
			if(!activeCage[cage])
				continue;

		    System.out.println(""+shockStack[cage].getSize());
			shockStack[cage].addSlice("background", backIp[cage].convertToByte(false));
			SafetySaver saver = new SafetySaver();
			StringBuilder br = new StringBuilder(FileManager.getInstance().getPaths(FileManager.imagePath)[cage]);
			br.delete(br.length()-4,br.length());
			br.append("_shock.tif");
			saver.saveImage(br.toString(), shockStack[cage]);
		}
	}

	public void saveShockOfflineTotalResult(String[] result, boolean writeHeader, boolean writeVersion, boolean writeParameter,String path){
		FileCreate creater = new FileCreate(path);
		if(writeHeader){
			String header = Header.getTotalResultHeader(Program.OF); 
			creater.write(header, true);
		}
		if(writeVersion){
			creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		if(writeParameter){
			saveParameter(creater);
		}
		StringBuffer line = new StringBuffer(subjectID[0]);
		for(int bin = 0; bin < result.length; bin++){
			line.append("\t" + result[bin]);
		}
		creater.write(line.toString(), true);
	}

	public void saveShockRepectiveResults(List<Double> result, boolean writeVersion){
		String respectiveResultsPath = fileManager.getPath(FileManager.ResultsDir)+File.separator+fileManager.getPath(FileManager.SessionID)+"-shock_dist.txt";
		FileCreate creater = new FileCreate(respectiveResultsPath);
		//ヘッダの記入
		if(!(new File(respectiveResultsPath).exists())){
			StringBuilder header = new StringBuilder("SubjectID");
			for(int i=0; i<result.size(); i++)
				header.append("\t" + "bin" + (i+1));
			creater.write(header.toString(), true);
		}
		if(writeVersion){
			creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		//結果の記入
		for(int cage=0; cage<allCage; cage++){
			if(!activeCage[cage])
				continue;
			StringBuffer respectiveResults = new StringBuffer(subjectID[cage]);
			for(double res : result){
				if(res!=-1.0){
					respectiveResults.append("\t" + Math.round(res*10.0)/10.0);
				}else{
					respectiveResults.append("\t" + "-");
				}
	 	    }
			creater.write(respectiveResults.toString(), true);
		}
	}

	public void saveOfflineShockRepectiveResults(List<Double> result, boolean writeVersion){
		String respectiveResultsPath = fileManager.getSavePath(FileManager.ResultsDir)+File.separator+fileManager.getSavePath(FileManager.SessionID)+"-shock_dist.txt";
		FileCreate creater = new FileCreate(respectiveResultsPath);
		//ヘッダの記入
		if(!(new File(respectiveResultsPath).exists())){
			StringBuilder header = new StringBuilder("SubjectID");
			for(int i=0; i<result.size(); i++)
				header.append("\t" + "bin" + (i+1));
			creater.write(header.toString(), true);
		}
		if(writeVersion){
			creater.write("# " + Version.getVersion() + ", ImageJ" + ij.IJ.getVersion(), true);
		}
		//結果の記入
		for(int cage=0; cage<allCage; cage++){
			if(!activeCage[cage])
				continue;
			StringBuffer respectiveResults = new StringBuffer(subjectID[cage]);
			for(double res : result){
				if(res!=-1.0){
					respectiveResults.append("\t" + Math.round(res*10.0)/10.0);
				}else{
					respectiveResults.append("\t" + "-");
				}
	 	    }
			creater.write(respectiveResults.toString(), true);
		}
	}
}