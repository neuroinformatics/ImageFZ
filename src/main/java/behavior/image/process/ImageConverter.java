package behavior.image.process;

import java.awt.Rectangle;

import ij.IJ;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class ImageConverter{
	public enum Mode{SUBTRACT,XOR}

    /**
     * 
     * @param ip　差分をとる画像
     * @param backIp　背景となる画像
     * @param xloc　背景のどの位置から切り取るかを指定する
     * @param yloc
     * @param mode
     * @return 加工後の画像
     */
	// ImageJのByteBlitterと内容は同一であるが、画像のPixel配列に問題が生じた場合に対応するため新たに作成した。
	public ImageProcessor copyBits(ImageProcessor ip,ImageProcessor backIp,int xloc,int yloc,Mode mode){
		try{
        Rectangle r1 = new Rectangle(backIp.getWidth(), backIp.getHeight());
        Rectangle r2 = new Rectangle(xloc, yloc, ip.getWidth(), ip.getHeight());
        if (!r1.intersects(r2)){
            return backIp;
        }

        if(!(backIp instanceof ByteProcessor)){
            backIp = backIp.convertToByte(false);
        }
        if(!(ip instanceof ByteProcessor)){
            ip = ip.convertToByte(false);
        }

        Rectangle intersectantArea = r1.intersection(r2);

        Object objectBackPixels = backIp.getPixels();
        if(!(objectBackPixels instanceof byte[])){
			int[] wrongPixel = (int[])objectBackPixels;
			byte[] safePixel = new byte[backIp.getWidth()*backIp.getHeight()];
			for(int j = 0; j < wrongPixel.length; j++)
				safePixel[j] = (byte)wrongPixel[j];
			backIp.setPixels(safePixel);
		}
        Object objectSrcPixels = ip.getPixels();
        if(!(objectSrcPixels instanceof byte[])){
			int[] wrongPixel = (int[])objectSrcPixels;
			byte[] safePixel = new byte[ip.getWidth()*ip.getHeight()];
			for(int j = 0; j < wrongPixel.length; j++)
				safePixel[j] = (byte)wrongPixel[j];
			ip.setPixels(safePixel);
		}
        byte[] backPixels = (byte[])backIp.getPixels();
        byte[] srcPixels = (byte[])ip.getPixels();
        byte[] resultPixels = new byte[intersectantArea.width*intersectantArea.height];

        int backIndex,srcIndex,resultIndex;
        int dst;
        for(int y=intersectantArea.y; y<(intersectantArea.y+intersectantArea.height); y++){
            backIndex = y*backIp.getWidth() + intersectantArea.x;
            srcIndex = (y-yloc)*ip.getWidth() + (intersectantArea.x-xloc);
            resultIndex = (y-intersectantArea.y)*intersectantArea.width;
            switch(mode){
                case SUBTRACT:
                    for(int i=intersectantArea.width; --i>=0;){
                    	/*ImageJより：
                    	  "To avoid sign extension, the pixel values must be
                           accessed using a mask(e.g. int i = pixels[j]&0xff)."*/
                        dst = (srcPixels[srcIndex++]&255)-(backPixels[backIndex++]&255);
                        if(dst<0){
                        	dst = 0;
                        }
                        resultPixels[resultIndex++] = (byte)dst;
                    }
                    break;
                case XOR:
                    for(int i=intersectantArea.width; --i>=0;){
                        dst = backPixels[backIndex++]^srcPixels[srcIndex++];
                        resultPixels[resultIndex++] = (byte)dst;
                    }
                    break;
            }
        }

        //戻り値の型はImageProcessorであるが、実際に返すのはByteProcessorである。
        ImageProcessor resultIp = new ByteProcessor(intersectantArea.width,intersectantArea.height);
        resultIp.setPixels(resultPixels);
        resultIp.invertLut();
        return resultIp;
		}catch(Exception e){
			IJ.error(e.getMessage());
		}
        return ip;
    }
}