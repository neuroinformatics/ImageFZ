package behavior.gui.roi;

import java.awt.Rectangle;
import java.awt.Polygon;
import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.gui.Roi;



/**�w�肳�ꂽ�摜�� Roi �� �J���[�Ŏ��ۂɉ摜�ɕ`�ʂ���BRoi �`�F�b�N�Ɏg�p�B*/

public class RoiWriter{
	protected String roiName = null;
	protected ImagePlus imp;

	public RoiWriter(){
	}

	/**@param roiName Roi �̖��O*/
	public RoiWriter(String roiName){
		this.roiName = roiName;
	}

	/**
	 *@param ip �������� ImageProcessor
	 *@param roi �������� Roi
	 *@return �������܂ꂽ ImageProcessor*/
	public ImageProcessor writeRoi(ImageProcessor ip, Roi roi){
		ip = ip.convertToRGB();
		ip.setColor(Color.red);//��ʂɏ������ޕ����̐F�w��
		Polygon polygonRoi = roi.getPolygon();
		ip.drawPolygon(polygonRoi); //ROI �{�̂̏�������
		if(roiName != null){
			Rectangle outline = polygonRoi.getBounds();
			ip.drawString(roiName, outline.x, outline.y); //ROI �̖��O�̏�������
		}
		return ip;
	}

	/**������ ImageProcessor ���ۗL���� Roi �ŏ�������
	 *@return �������܂ꂽ ImageProcessor
	 */
	public ImageProcessor writeRoi(ImageProcessor ip){
		return writeRoi(ip, (new ImagePlus("", ip)).getRoi());
	}

	/**���l�z��Ŏw�肳�ꂽ Roi ����������
	 *@param numericRoi ���l�Œ����`�� Roi ��\�� 0�cx, 1�cy, 2�cwidth, 3�cheight
	 */
	public ImageProcessor writeRoi(ImageProcessor ip, int[] numericRoi){
		Roi roi = new Roi(numericRoi[0], numericRoi[1], numericRoi[2], numericRoi[3]);
		return writeRoi(ip, roi);
	}

	/**�������݂ɉ����ĕ\�����s��
	 */
	public void writeAndShow(ImageProcessor ip, Roi roi){
		ImageProcessor roiIp = writeRoi(ip, roi);
		imp = new ImagePlus("", roiIp);
		imp.show();
	}

	/**�������݁A�\���ɉ����āA���[�U�� Roi ���m�F����_�C�A���[�O�̕\�����s���B(�����摜)
	 *@param message �_�C�A���[�O�ɕ\�����郁�b�Z�[�W
	 *@return �L�����Z���������ꂽ�� true
	 */
	public boolean writeShowAndConfirm(ImageProcessor[] ip, Roi[] roi, String message){
		for(int cage = 0; cage < ip.length; cage++)
			writeAndShow(ip[cage], roi[cage]);
		boolean ans = IJ.showMessageWithCancel("", message);
		imp.hide();
		return !ans;
	}

	/**�������݁A�\���ɉ����āA���[�U�� Roi ���m�F����_�C�A���[�O�̕\�����s���B(�P��摜)
	 *@param message �_�C�A���[�O�ɕ\�����郁�b�Z�[�W
	 *@return �L�����Z���������ꂽ�� true
	 */
	public boolean writeShowAndConfirm(ImageProcessor ip, Roi roi, String message){
		writeAndShow(ip, roi);
		boolean ans = IJ.showMessageWithCancel("", message);
		imp.hide();
		return !ans;
	}

}




