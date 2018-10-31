package behavior.image.process;

import ij.*;
import ij.process.*;
/*
 * �������Ȃǂ��ł��Ȃ��A���S���Y���ł���B�v�C���I
 * ParticleAnalyzer������p�[�e�B�N���̏d�S�̈ʒu���킩�������ɁA
 * �K�v�ȃp�[�e�B�N���ȊO���������߂�plugin
 * �������AIJ.doWand�𗘗p���Ă��邽�߁A�p�[�e�B��������Ɍ��������Ă��A�����F�����Ȃ�
 * ������Ƃ��ẮAtool.flood_fill��p����̂��x�X�g�Ǝv����B
 */
public class ParticleRemover {
	//ref-Analyze
	protected final static int X_CENTER = 0;
	protected final static int Y_CENTER = 1;
	protected final static int AREA = 2;
	/*
	 *�@int[] ���������p�[�e�B�N���̂��A���A�ʐς̃f�[�^
	 * int backgroundvalue:���������摜�̔w�i�F�̒l�A�w�i�F�ȊO���w��\
	 * �ꊇ���ď����������Ƃ��ɗp���� 
	 */
	public static ImagePlus doRemove(int[][] xyaData, int backgroundvalue, ImagePlus currentimp){		
		ImageProcessor ip;
		ImagePlus imp = new ImagePlus();
		currentimp.setActivated();
		// ���}���u
		if(xyaData[0] == null){
			return currentimp;
		}
		//�D�L�������c
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

