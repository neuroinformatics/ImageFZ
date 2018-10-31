package behavior.image.process;

import ij.*;
import ij.process.*;
/*
 * 微調整などができないアルゴリズムである。要修正！
 * ParticleAnalyzer等からパーティクルの重心の位置がわかった時に、
 * 必要なパーティクル以外を消すためのplugin
 * ただし、IJ.doWandを利用しているため、パーティ来る内部に穴があっても、それを認識しない
 * 解決策としては、tool.flood_fillを用いるのがベストと思われる。
 */
public class ParticleRemover {
	//ref-Analyze
	protected final static int X_CENTER = 0;
	protected final static int Y_CENTER = 1;
	protected final static int AREA = 2;
	/*
	 *　int[] 消したいパーティクルのｘ、ｙ、面積のデータ
	 * int backgroundvalue:消したい画像の背景色の値、背景色以外も指定可能
	 * 一括して消去したいときに用いる 
	 */
	public static ImagePlus doRemove(int[][] xyaData, int backgroundvalue, ImagePlus currentimp){		
		ImageProcessor ip;
		ImagePlus imp = new ImagePlus();
		currentimp.setActivated();
		// 応急処置
		if(xyaData[0] == null){
			return currentimp;
		}
		//泥臭い処理…
		if(currentimp.getProcessor().getPixel(xyaData[0][X_CENTER], xyaData[0][Y_CENTER])>200){
			backgroundvalue=0;
		}else{
			backgroundvalue=255;
		}

		for(int i=0;i<xyaData.length;i++){
			IJ.doWand(xyaData[i][X_CENTER],xyaData[i][Y_CENTER]);
			ip =(IJ.getImage()).getProcessor();
			ip.setColor(backgroundvalue);
			ip.fill();
			imp.setProcessor("mouse", ip);
		}
		return imp;
	}
}

