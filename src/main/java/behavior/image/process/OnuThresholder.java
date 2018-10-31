package behavior.image.process;

import java.awt.*;

import ij.process.*;

public class OnuThresholder{

	public int minThres;
	public int maxThres;

	/**
	 �摜��Threshold���s���N���X�B������Processor�������w�肷��΁AImageJ��autoThreshold���s���B�ŏ��l�A�ő�l���w�肷��΁A���̒l��Threshold���s���B
	 */
	public OnuThresholder(ImageProcessor ip){
		ip.autoThreshold();
	}

	/**
	 *@param minThres �����菬�����l�͔��A�傫���ƍ��ɓh�蕪������
	 */
	public OnuThresholder(int minThres,int maxThres){
		this.minThres = minThres;
		this.maxThres = maxThres;
	}

	/** threshold �l�̍Đݒ�
	 */
	public void setThreshold(int minThres, int maxThres){
		this.minThres = minThres;
		this.maxThres = maxThres;
	}

	/**
	 �����̉摜���ꂼ��ɃR���X�g���N�^�Ŏw�肵���l��Threshold���s���B
	 */
	public void multiThreshold(ImageProcessor[] ip,int nIp){
		for(int i=0;i<nIp;i++){
			applyThreshold(ip[i]);
		}
	}

	/**
	 �R���X�g���N�^�Ŏw�肵���l��Threshold���s���B
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
