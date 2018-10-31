package behavior.io;

import java.io.*;

import ij.process.ImageProcessor;
import ij.process.ByteProcessor;
import ij.IJ;

/**�w�肳�ꂽ�摜�̃s�N�Z�����A�e�L�X�g�Ƃ��ĕۑ��B�f�o�b�O�Ɏg����B*/
public class PixelSaver{
	public PixelSaver(){
	}

	/**�摜�̃s�N�Z�����e�L�X�g�Ƃ��ĕۑ��B
	 *@param fileName �ۑ�����e�L�X�g�̖��O(���Ƀp�X���w�肵�Ȃ��ƁAImageJ �t�H���_�ɕۑ������
	 *@param ip �s�N�Z����ۑ��������摜
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