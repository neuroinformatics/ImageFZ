package behavior.gui;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import behavior.image.ImageCapture;

public class LiveMovieWindow extends Thread{

	private boolean isRunning;
	private ImagePlus image;
	private WindowOperator winOperator;

	public LiveMovieWindow(){
		isRunning = true;
	}

	public void run(){
		image = ImageCapture.getInstance().capture();
		ImageProcessor roiIp = image.getProcessor();
		roiIp = roiIp.convertToByte(false);
		image.setTitle("Live Movie");
		ImageProcessor[] back = new ImageProcessor[1];
		back[0] = image.getProcessor();
		ImagePlus[] imp = new ImagePlus[1];
		imp[0] = image;
		winOperator = WindowOperator.getInstance(1, back);
		winOperator.setImageWindow(imp, WindowOperator.LEFT_UP);

		while(isRunning){
			if(image.getWindow().isClosed()){
				break;
			}
			ImagePlus movieImp2 = ImageCapture.getInstance().capture();
			ImageProcessor movieIp = movieImp2.getProcessor();
			image.setProcessor("Live Movie",movieIp);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public ImageProcessor getImage(){
		return image.getProcessor();
	}

	public void end(){
		isRunning = false;
		winOperator.closeWindows();
	}


}
