package behavior.io;

import java.io.*;

import ij.*;
import ij.io.Opener;
import ij.io.TiffDecoder;

import behavior.io.FileManager;

/**画像をファイルから取得するクラス*/
public class ImageLoader{
	public ImageLoader(){
	}

	/**画像を取得
	 *@param path 画像ファイルが存在するディレクトリの path
	 *@param fileName 画像ファイル名
	 *@return スタック画像
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

	/**ロードに失敗した場合も、エラーメッセージを出さない。失敗は、返却値が null であることで分かる*/
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

	/**こちらは、FileManager から自動的にパスを取得。サブジェクトIDを指定するだけで必要な画像が得られる。
	 */
	public ImageStack loadImage(String subjectID){
		FileManager fileManager = FileManager.getInstance();
		String extension = ".tiff";
		if(new File(fileManager.getPath(FileManager.ImagesDir) +File.separator+ subjectID + ".tif").exists())
			extension = ".tif";

		return loadImage(fileManager.getPath(FileManager.ImagesDir) + File.separator, subjectID + extension);
	}
}