package behavior.image.process;

import ij.process.*;
import ij.gui.Roi;

/**画像の融合をする(for LD)
 */
public class Fusion{
	/**画像を融合
	 *@param ip1 左側の画像
	 *@param ip2 右側の画像
	 *@return 融合後の画像
	 */
	public static ImageProcessor fusionImage(ImageProcessor ip1, ImageProcessor ip2){
		if(ip1.getHeight() != ip2.getHeight())
			throw new IllegalArgumentException("images' heights do not match");
		ImageProcessor fusionedIp = new ByteProcessor(ip1.getWidth() + ip2.getWidth(), ip1.getWidth());
		fusionedIp.copyBits(ip1, 0, 0, Blitter.COPY);
		fusionedIp.copyBits(ip2, ip1.getWidth(), 0, Blitter.COPY);
		fusionedIp.invertLut();	//どうも、fusion すると、lookUpTable がもとに戻るらしい
		return fusionedIp;
	}

	/**画像を半分にカット
	 *@param ip カットしたい画像
	 *@param width カット幅。左から width のところでカットされる
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



