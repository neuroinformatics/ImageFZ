package behavior.io;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;

public class SISafetySaver extends SafetySaver{

	public void saveRGBImage(String path, ImageStack stack){
		//filterStack(stack);
		ImagePlus saveImp = new ImagePlus("", stack);
		FileSaver saver = new FileSaver(saveImp);
		try{
			if(stack.getSize() > 1)
				saver.saveAsTiffStack(path);
			else
				saver.saveAsTiff(path);
		}catch(ClassCastException e){
			if(super.name == null)
				IJ.showMessage("Error saving image in behavior.io.SafetySaver");
			else
				IJ.showMessage("Error saving image(" + name + ") in behavior.io.SafetySaver");
		}
		name = null;
	}
}