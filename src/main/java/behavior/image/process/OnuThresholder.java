package behavior.image.process;

import java.awt.*;

import ij.process.*;

public class OnuThresholder{

	public int minThres;
	public int maxThres;

	/**
	 画像にThresholdを行うクラス。引数にProcessorだけを指定すれば、ImageJのautoThresholdを行う。最小値、最大値を指定すれば、その値でThresholdを行う。
	 */
	public OnuThresholder(ImageProcessor ip){
		ip.autoThreshold();
	}

	/**
	 *@param minThres これより小さい値は白、大きいと黒に塗り分けられる
	 */
	public OnuThresholder(int minThres,int maxThres){
		this.minThres = minThres;
		this.maxThres = maxThres;
	}

	/** threshold 値の再設定
	 */
	public void setThreshold(int minThres, int maxThres){
		this.minThres = minThres;
		this.maxThres = maxThres;
	}

	/**
	 複数の画像それぞれにコンストラクタで指定した値のThresholdを行う。
	 */
	public void multiThreshold(ImageProcessor[] ip,int nIp){
		for(int i=0;i<nIp;i++){
			applyThreshold(ip[i]);
		}
	}

	/**
	 コンストラクタで指定した値でThresholdを行う。
	 */
	public void applyThreshold(ImageProcessor ip){
		double saveMin = ip.getMin();
		double saveMax = ip.getMax();
		double minThreshold = ((minThres - saveMin)/(saveMax - saveMin))*255.0;
		double maxThreshold = ((maxThres - saveMin)/(saveMax - saveMin))*255.0;
		ip.resetThreshold();
		int savePixel = ip.getPixel(0,0);
		ip.setColor(Color.black);
		ip.drawPixel(0,0);

		int fcolor = ip.getPixel(0,0);
		ip.setColor(Color.white);
		ip.drawPixel(0,0);
		int bcolor = ip.getPixel(0,0);
		ip.putPixel(0,0,savePixel);
		int[] lut = new int[256];
		for(int i=0; i<256 ; i++){
			if(i>=minThreshold && i<=maxThreshold){
				lut[i] = fcolor;
			}else{
				lut[i] = bcolor;
			}
		}
		ip.applyTable(lut);
	}

}
