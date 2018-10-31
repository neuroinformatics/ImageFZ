package behavior.io;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;

/**画像を安全に保存するクラス。画像保存時に ClassCast エラーが出るときはこちらを使用するとなおる。
 */
public class SafetySaver{
	protected String name = null;

	/**保存。
	 *@param name エラー時に表示する画像名
	 */
	public void saveImage(String path, ImageStack stack, String name){
		this.name = name;
		saveImage(path, stack);
	}


	/**保存。
	 *@param name エラー時に表示する画像名
	 */
	public void saveImage(String path, ImagePlus imp, String name){
		this.name = name;
		saveImage(path, imp);
	}

	public void saveImage(String path, ImagePlus imp){
		saveImage(path, imp.getStack());
	}

	public void saveImage(String path, ImageStack stack){
		filterStack(stack);
		ImagePlus saveImp = new ImagePlus("", stack);
		FileSaver saver = new FileSaver(saveImp);
		try{
			if(stack.getSize() > 1)
				saver.saveAsTiffStack(path);
			else
				saver.saveAsTiff(path);
		}catch(ClassCastException e){
			if(name == null)
				IJ.showMessage("Error saving image in behavior.io.SafetySaver");
			else
				IJ.showMessage("Error saving image(" + name + ") in behavior.io.SafetySaver");
		}
		name = null;
	}

	/*保存の際、ClassCastException: [I と出る。これは一部画像がint 配列に変わってしまっているから
	 である。ここでは、これを未然に防ぐ。*/
	public void filterStack(ImageStack stack){
		int size = stack.getSize();
		Object[] pixels = stack.getImageArray();
		for(int i = 0; i < size; i++){
			if(pixels[i] instanceof byte[])
				continue;
			else if(pixels[i] instanceof int[]){
				int[] wrongPixel = (int[])pixels[i];
				byte[] safePixel = new byte[wrongPixel.length];
				for(int j = 0; j < wrongPixel.length; j++)
					safePixel[j] = (byte)wrongPixel[j];
				stack.setPixels(safePixel, i + 1);
			}
		}
	}
}