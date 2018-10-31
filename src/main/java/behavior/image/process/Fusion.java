package behavior.image.process;

import ij.process.*;
import ij.gui.Roi;

/**�摜�̗Z��������(for LD)
 */
public class Fusion{
	/**�摜��Z��
	 *@param ip1 �����̉摜
	 *@param ip2 �E���̉摜
	 *@return �Z����̉摜
	 */
	public static ImageProcessor fusionImage(ImageProcessor ip1, ImageProcessor ip2){
		if(ip1.getHeight() != ip2.getHeight())
			throw new IllegalArgumentException("images' heights do not match");
		ImageProcessor fusionedIp = new ByteProcessor(ip1.getWidth() + ip2.getWidth(), ip1.getWidth());
		fusionedIp.copyBits(ip1, 0, 0, Blitter.COPY);
		fusionedIp.copyBits(ip2, ip1.getWidth(), 0, Blitter.COPY);
		fusionedIp.invertLut();	//�ǂ����Afusion ����ƁAlookUpTable �����Ƃɖ߂�炵��
		return fusionedIp;
	}

	/**�摜�𔼕��ɃJ�b�g
	 *@param ip �J�b�g�������摜
	 *@param width �J�b�g���B������ width �̂Ƃ���ŃJ�b�g�����
	 */
	public static ImageProcessor[] split(ImageProcessor ip, int width){
		ImageProcessor[] splitIp = new ImageProcessor[2];
		Roi roi1 = new Roi(0, 0, width, ip.getHeight());
		Roi roi2 = new Roi(width, 0, ip.getWidth() - width, ip.getHeight());
		ip.setRoi(roi1);
		splitIp[0] = ip.crop();
		ip.setRoi(roi2);
		splitIp[1] = ip.crop();
		return splitIp;
	}

}



