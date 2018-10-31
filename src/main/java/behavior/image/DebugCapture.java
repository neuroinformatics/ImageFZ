package behavior.image;

import java.io.*;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.Opener;
import ij.io.TiffDecoder;
import ij.process.ImageProcessor;

/**
 * debug 用の画像取得クラス。
 * デバイスが存在しなくても、手元の画像からあたかもカメラから取得したかのように画像を小出しにしていく。
 * デバイスなしでもオンラインのテストができる訳である。
 */
public class DebugCapture extends AbstractCapture{
	private ImageStack imageStack;
	private int sliceNO = 1;

	DebugCapture() throws Exception{
		JFileChooser fileChooser = new JFileChooser("C:"+File.separator);
		fileChooser.setFileFilter(new FileNameExtensionFilter("TIFFファイル(tif,tiff)","tif","tiff"));
		final int option = fileChooser.showOpenDialog(null);
		if(option != JFileChooser.APPROVE_OPTION){
			 throw new Exception();
		}

		File file = fileChooser.getSelectedFile();
		try{
			imageStack = new Opener().openTiffStack(new TiffDecoder(file.getParent()+File.separator, file.getName()).getTiffInfo()).getStack();
		}catch(Exception e){
			throw new Exception();
		}
	}

	@Override
	ImagePlus capture(){
		if(sliceNO>imageStack.getSize()){
			sliceNO = 1;
		}

		ImageProcessor imp = imageStack.getProcessor(sliceNO++).duplicate();
		imp.invert();
		imp.invertLut();
		return new ImagePlus("", imp);
	}

	@Override
	void close(){}
}