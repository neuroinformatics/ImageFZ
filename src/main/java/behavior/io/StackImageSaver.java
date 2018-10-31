package behavior.io;

import java.io.File;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.io.FileSaver;


/************************
�ꖇ���摜������ƁA�����temp �ɕۑ����āA�Ō�ɃX�^�b�N�Ƃ��ĕۑ�����B
 *************************/
public class StackImageSaver{
	private String name, folderName;
	private int imageNum = 0;

	/****************
	�R���X�g���N�^
	 *****************/
	public StackImageSaver(String name){
		this.name = name;
		folderName = "tempFile" + File.separator + name;
		File file = new File(folderName);	//temp �t�H���_���̎w��B�����̂��̂Əd�Ȃ�Ȃ��悤�ɂ���B
		if(file.exists()){
			for(int i = 0;; i++){
				folderName = "tempFile" + File.separator + name + i;
				file = new File(folderName);
				if(!file.exists())
					break;
			}
		}
		file.mkdirs();
	}

	public void setNextImage(ImageProcessor currentIp){
		String path = folderName + File.separator + imageNum + ".tif";

		FileSaver saver = new FileSaver(new ImagePlus(name, currentIp));
		saver.saveAsTiff(path);
		imageNum++;
	}

	public void saveImage(String path, ImageProcessor backIp){
		if(imageNum == 0) return;
		ImageLoader loader = new ImageLoader();
		ImageStack stack = loader.loadImage(folderName + File.separator, "0.tif");
		(stack.getProcessor(1)).invert();
		for(int i = 1; i < imageNum; i++){
			ImageStack tempStack = loader.loadImage(folderName + File.separator, i + ".tif");
			for(int num = 0; num < tempStack.getSize(); num++){
				ImageProcessor ip = tempStack.getProcessor(num + 1);
				ip.invert();
				stack.addSlice(name, ip);
			}
		}
		if(backIp != null)
			stack.addSlice(name, backIp);
		FileSaver saver = new FileSaver(new ImagePlus(name, stack));
		if(stack.getSize() == 1)
			saver.saveAsTiff(path);
		else
			saver.saveAsTiffStack(path);
		for(int i = 0; i < imageNum; i++){
			File file = new File(folderName + File.separator + i + ".tif");
			file.delete();
		}
		File file = new File(folderName);
		file.delete();
	}
	
	public void saveImageNotinvert(String path, ImageProcessor backIp){//��͔̂��]���Ă��܂��̂�
		if(imageNum == 0) return;
		ImageLoader loader = new ImageLoader();
		ImageStack stack = loader.loadImage(folderName + File.separator, "0.tif");
		stack.getProcessor(1);
		for(int i = 1; i < imageNum; i++){
			ImageStack tempStack = loader.loadImage(folderName + File.separator, i + ".tif");
			for(int num = 0; num < tempStack.getSize(); num++){
				ImageProcessor ip = tempStack.getProcessor(num + 1);
				
				stack.addSlice(name, ip);
			}
		}
		if(backIp != null)
			stack.addSlice(name, backIp);
		FileSaver saver = new FileSaver(new ImagePlus(name, stack));
		if(stack.getSize() == 1)
			saver.saveAsTiff(path);
		else
			saver.saveAsTiffStack(path);
		for(int i = 0; i < imageNum; i++){
			File file = new File(folderName + File.separator + i + ".tif");
			file.delete();
		}
		File file = new File(folderName);
		file.delete();
	}
}

