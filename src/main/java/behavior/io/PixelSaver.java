package behavior.io;

import java.io.*;

import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.IJ;

/**指定された画像のピクセルを、テキストとして保存。デバッグに使える。*/
public class PixelSaver{
	public PixelSaver(){
	}

	/**画像のピクセルをテキストとして保存。
	 *@param fileName 保存するテキストの名前(特にパスを指定しないと、ImageJ フォルダに保存される
	 *@param ip ピクセルを保存したい画像
	 */
	public void save(String fileName, ImageProcessor ip){
		if(!(ip instanceof ByteProcessor))
			throw new IllegalArgumentException("processor must be ByteProcessor");
		byte[] pixel = (byte[])ip.getPixels();
		int length = ip.getWidth();
		StringBuilder sb = new StringBuilder(pixel.length * 2 + length + 10);
		for(int i = 0; i < pixel.length; i++){
			sb.append((int)pixel[i]);
			sb.append(":");
			if((i + 1) % length == 0)
				sb.append("\n");
		}
		try{
			FileWriter writer = new FileWriter(fileName);
			writer.write(sb.toString());
			writer.close();
		}catch(Exception e){
			IJ.showMessage("io error in behavior.io.PixelSaver");
		}

	}
}