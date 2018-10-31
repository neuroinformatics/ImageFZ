package behavior.plugin.executer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.plugin.analyzer.TMAnalyzer;
import behavior.setup.Program;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.io.TiffDecoder;
import ij.process.ImageProcessor;

public class TMoffExecuter extends OfflineExecuter{
	private ImagePlus[] subtractImp;

	public TMoffExecuter(){
		program = Program.TM;
	}

	@Override
	protected void subSetup(ImageProcessor[] backIp){
		analyze[0] = new TMAnalyzer(backIp[0]);
	}

	protected ImageProcessor[] getBackground(ImageStack allImage){
		ImageProcessor bufIp =allImage.getProcessor(1);
		bufIp.setInterpolationMethod(ImageProcessor.BICUBIC);
		ImageProcessor backIp = bufIp.resize(320,240);
		ImageProcessor[] backIps = new ImageProcessor[allCage];
		backIps[0] = backIp;
		return backIps;
	}

	protected void analyze(ImageStack allImage){
		allSlice = allImage.getSize();
		analyze[0].resetDuration(allSlice - 2);
		for(int slice = 2; slice <= allSlice; slice++){
			ImageProcessor bufIp =allImage.getProcessor(slice);
			bufIp.setInterpolationMethod(ImageProcessor.BICUBIC);
			ImageProcessor currentIp = bufIp.resize(320,240);
	
			analyze[0].analyzeImage(currentIp);
//			if(analyze.mouseExists()){  //マウスが認識されれば、解析と結果表示
			analyze[0].calculate(slice - 2);
			winOperator.setXYText(0, analyze[0].getXY(slice - 2));
			setCurrentImage(slice - 2, currentIp);
//			}
		}
		addRestResult(allSlice - 2);
		
	}
	@Override
	protected ImageStack getSavedImage(String id){
		StringBuilder str = new StringBuilder(id);
		for(int i=0;i<2;i++){
			str.setCharAt(str.lastIndexOf("-"),'_');
		}
	    String imageID = str.toString();

		FileManager fileManager = FileManager.getInstance();
		String extension = ".tiff";
		if(new File(fileManager.getPath(FileManager.ImagesDir) +File.separator+ fileManager.getPath(FileManager.SessionID)+File.separator+imageID + ".tif").exists())
			extension = ".tif";

		TiffDecoder imgTd = new TiffDecoder(fileManager.getPath(FileManager.ImagesDir) + File.separator+ fileManager.getPath(FileManager.SessionID)+File.separator, imageID + extension);
		Opener open = new Opener();
		ImagePlus stackImp = null;
		try{
			stackImp = open.openTiffStack(imgTd.getTiffInfo());
		}catch(FileNotFoundException e){
			IJ.error("no file:" +imageID + extension + "(in ImageLoader)");
		}catch(IOException e){
			IJ.error("Input error:" + imageID + extension + "(in ImageLoader)");
		}
		
		return stackImp.getStack();
	}

	@Override
	protected void setEachWindow(ImageProcessor[] backIp) {
		subtractImp = new ImagePlus[1];
		subtractImp[0] = new ImagePlus("subtract", backIp[0]);
		winOperator.setImageWindow(subtractImp, WindowOperator.RIGHT_UP);
	}

	@Override
	protected void setEachCurrentImage(int cage) {
		subtractImp[cage].setProcessor("subtract", ((TMAnalyzer)analyze[cage]).getSubtractImage());
	}

	@Override
	protected void saveBinResult(boolean writeVersion){}
}