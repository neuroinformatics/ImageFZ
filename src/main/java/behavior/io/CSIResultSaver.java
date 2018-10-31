package behavior.io;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import behavior.setup.Program;

public class CSIResultSaver extends ResultSaver{
	private ImageStack[] subtractStack;
	private Object lock1 = new Object();

	public CSIResultSaver(Program program, int allCage, ImageProcessor[] backIp) {
		super(program, allCage, backIp);

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
}