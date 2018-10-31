package behavior.gui.roi;

import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.gui.Roi;



/**指定された画像の Roi を カラーで実際に画像に描写する。Roi チェックに使用。*/

public class RoiWriter{
	protected String roiName = null;
	protected ImagePlus imp;

	public RoiWriter(){
	}

	/**@param roiName Roi の名前*/
	public RoiWriter(String roiName){
		this.roiName = roiName;
	}

	/**
	 *@param ip 書き込む ImageProcessor
	 *@param roi 書き込む Roi
	 *@return 書き込まれた ImageProcessor*/
	public ImageProcessor writeRoi(ImageProcessor ip, Roi roi){
		ip = ip.convertToRGB();
		ip.setColor(Color.red);//画面に書き込む文字の色指定
		Polygon polygonRoi = roi.getPolygon();
		ip.drawPolygon(polygonRoi); //ROI 本体の書き込み
		if(roiName != null){
			Rectangle outline = polygonRoi.getBounds();
			ip.drawString(roiName, outline.x, outline.y); //ROI の名前の書き込み
		}
		return ip;
	}

	/**引数の ImageProcessor が保有する Roi で書き込む
	 *@return 書き込まれた ImageProcessor
	 */
	public ImageProcessor writeRoi(ImageProcessor ip){
		return writeRoi(ip, (new ImagePlus("", ip)).getRoi());
	}

	/**数値配列で指定された Roi を書き込む
	 *@param numericRoi 数値で長方形の Roi を表現 0…x, 1…y, 2…width, 3…height
	 */
	public ImageProcessor writeRoi(ImageProcessor ip, int[] numericRoi){
		Roi roi = new Roi(numericRoi[0], numericRoi[1], numericRoi[2], numericRoi[3]);
		return writeRoi(ip, roi);
	}

	/**書き込みに加えて表示も行う
	 */
	public void writeAndShow(ImageProcessor ip, Roi roi){
		ImageProcessor roiIp = writeRoi(ip, roi);
		imp = new ImagePlus("", roiIp);
		imp.show();
	}

	/**書き込み、表示に加えて、ユーザに Roi を確認するダイアローグの表示も行う。(複数画像)
	 *@param message ダイアローグに表示するメッセージ
	 *@return キャンセルが押されたら true
	 */
	public boolean writeShowAndConfirm(ImageProcessor[] ip, Roi[] roi, String message){
		for(int cage = 0; cage < ip.length; cage++)
			writeAndShow(ip[cage], roi[cage]);
		boolean ans = IJ.showMessageWithCancel("", message);
		imp.hide();
		return !ans;
	}

	/**書き込み、表示に加えて、ユーザに Roi を確認するダイアローグの表示も行う。(単一画像)
	 *@param message ダイアローグに表示するメッセージ
	 *@return キャンセルが押されたら true
	 */
	public boolean writeShowAndConfirm(ImageProcessor ip, Roi roi, String message){
		writeAndShow(ip, roi);
		boolean ans = IJ.showMessageWithCancel("", message);
		imp.hide();
		return !ans;
	}

}




