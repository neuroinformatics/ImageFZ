package behavior.gui;

import ij.process.Blitter;
import ij.process.ImageProcessor;
import behavior.image.process.OnuThresholder;

public abstract class MovieManager extends Thread {
	/**
	 * Thread の終了処理
	 */
	public abstract void end();

	/*
	 * ImageManager が使えればいいのだが、パラメータ未設定（ここで設定する）都合上使用できない。
	 * あまりスマートではないが、とりあえず以下のメソッドを用意しておく。
	 */ 
	protected ImageProcessor getSubtractImage(ImageProcessor currentIp, ImageProcessor backIp, int minThreshold, int maxThreshold, boolean invert){
		ImageProcessor bin;
		if(invert){
			bin = backIp.duplicate();
			bin.copyBits(currentIp, 0, 0, Blitter.SUBTRACT);
		} else {
			bin = currentIp.duplicate();
			bin.copyBits(backIp, 0, 0, Blitter.SUBTRACT);
		}
		OnuThresholder st = new OnuThresholder(minThreshold, maxThreshold);
		st.applyThreshold(bin);
		return bin;
	}

	protected ImageProcessor getXORImage(ImageProcessor previousIp, ImageProcessor currentIp, int xorThreshold, int maxThreshold){
		ImageProcessor xor = previousIp.duplicate();
		ImageProcessor tmp = currentIp.duplicate();
		OnuThresholder st  = new OnuThresholder(xorThreshold, maxThreshold);
		st.applyThreshold(xor);
		st.applyThreshold(tmp);
		xor.copyBits(tmp, 0, 0, Blitter.XOR);
		return xor;
	}
}
