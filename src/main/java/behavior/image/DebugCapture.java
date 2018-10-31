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
 * debug �p�̉摜�擾�N���X�B
 * �f�o�C�X�����݂��Ȃ��Ă��A�茳�̉摜���炠�������J��������擾�������̂悤�ɉ摜�����o���ɂ��Ă����B
 * �f�o�C�X�Ȃ��ł��I�����C���̃e�X�g���ł����ł���B
 */
public class DebugCapture extends AbstractCapture{
	private ImageStack imageStack;
	private int sliceNO = 1;

	DebugCapture() throws Exception{
		JFileChooser fileChooser = new JFileChooser("C:"+File.separator);
		fileChooser.setFileFilter(new FileNameExtensionFilter("TIFF�t�@�C��(tif,tiff)","tif","tiff"));
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