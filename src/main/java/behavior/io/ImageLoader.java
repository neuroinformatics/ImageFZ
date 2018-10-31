package behavior.io;

import java.io.*;

import ij.*;
import ij.io.Opener;
import ij.io.TiffDecoder;

import behavior.io.FileManager;

/**�摜���t�@�C������擾����N���X*/
public class ImageLoader{
	public ImageLoader(){
	}

	/**�摜���擾
	 *@param path �摜�t�@�C�������݂���f�B���N�g���� path
	 *@param fileName �摜�t�@�C����
	 *@return �X�^�b�N�摜
	 */
	public ImageStack loadImage(String path, String fileName){
		TiffDecoder imgTd = new TiffDecoder(path, fileName);
		Opener open = new Opener();
		ImagePlus stackImp = null;
		try{
			stackImp = open.openTiffStack(imgTd.getTiffInfo());
		}catch(FileNotFoundException e){
			IJ.error("no file:" + fileName + "(in ImageLoader)");
		}catch(IOException e){
			IJ.error("Input error:" + fileName + "(in ImageLoader)");
		}
		return stackImp.getStack();
	}

	/**���[�h�Ɏ��s�����ꍇ���A�G���[���b�Z�[�W���o���Ȃ��B���s�́A�ԋp�l�� null �ł��邱�Ƃŕ�����*/
	public ImageStack loadImageWithoutError(String path, String fileName){
		TiffDecoder imgTd = new TiffDecoder(path, fileName);
		Opener open = new Opener();
		ImagePlus stackImp = null;
		try{
			stackImp = open.openTiffStack(imgTd.getTiffInfo());
		}catch(Exception e){
			return null;
		}
		return stackImp.getStack();
	}

	/**������́AFileManager ���玩���I�Ƀp�X���擾�B�T�u�W�F�N�gID���w�肷�邾���ŕK�v�ȉ摜��������B
	 */
	public ImageStack loadImage(String subjectID){
		FileManager fileManager = FileManager.getInstance();
		String extension = ".tiff";
		if(new File(fileManager.getPath(FileManager.ImagesDir) +File.separator+ subjectID + ".tif").exists())
			extension = ".tif";

		return loadImage(fileManager.getPath(FileManager.ImagesDir) + File.separator, subjectID + extension);
	}
}