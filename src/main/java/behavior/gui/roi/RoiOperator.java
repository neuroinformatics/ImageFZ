package behavior.gui.roi;

import java.awt.*;
import java.io.*;

import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.io.*;

import behavior.setup.Program;
import behavior.io.FileManager;

/**Roi の取得、管理を行うクラス*/
/**
 * 
 * @author Modifier:Butoh
 */
public class RoiOperator{
	protected Program program;
	protected int allRoi;
	protected Roi[] roi;

	//LDRoiOperatorのために必要
	public RoiOperator(){}

	/**@param program プログラム番号(behavior.setup.Programから）
	 *@param allCage ケージ数*/
	public RoiOperator(Program program, int allCage){
		this.program = program;
		this.allRoi = allCage;
		roi = new Roi[allRoi];
	}

	/***********
	各ケージの Roi を load して、自身のメンバに代入する
	@return 失敗したら true
	 ************/
	public boolean loadRoi(){
		String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir);
		for(int num = 0; num < allRoi; num++){
			String roiFile = null;
            if(program == Program.BT || program == Program.RM || program == Program.CSI
					    || program == Program.YM){
				roiFile = path + File.separator + "MainArea.roi";
			}else if(program == Program.BM) {
				roiFile = path + File.separator + "Field.roi";
			}else{
				roiFile = path + File.separator + "Cage Field" + (num + 1) + ".roi";
			}

			try{
				roi[num] = readRoi(roiFile);
			}catch(IOException e){
				IJ.error("ROI(" + roiFile + ") cannot be found. Try to set ROIs or Copy ROIs from other folder.");
				return true;
			}
		}
		return false;
	}

	/***********
	 *　全ケージの ROI を取得した画像に描画して、その ROI でよいかどうかユーザに尋ねる。
	 *　わざわざ roiImp を引数として取得しているのは、後に表示する動画との連続性を保つ（チェック画像をそのまま動画として使う）ため。
	 *@param roiImp Roi のチェックに使用する ImagePlus
	 *@return キャンセルが押されたら true
	 ************/
	public boolean checkRoi(ImagePlus roiImp){
		ImageProcessor roiIp = roiImp.getProcessor();//画像のプロセッサ取得
		roiIp = roiIp.convertToRGB();//RGBモードに変換
		roiImp.setProcessor("Check ROI", roiIp);	//何故か ip をセットし直さないと、ROIが表示されない
		roiIp.setColor(Color.red);//画面に書き込む文字の色指定
		roiImp.show();//画像表示
		for(int num = 0; num < allRoi; num++){
			String name = null;
			name = "Cage Field" + (num + 1);
			Polygon polygonRoi = roi[num].getPolygon();
			Rectangle outline = polygonRoi.getBounds();
			roiIp.drawString(name, outline.x, outline.y); //ROI の名前の書き込み
			roiIp.drawPolygon(polygonRoi); //ROI 本体の書き込み
		}
		roiImp.updateAndDraw();//画像更新

		if(!IJ.showMessageWithCancel("Field Check", "use these ROIs?\nPress Yes if ROIs are set properly.\nPress No to quit this plugin and reset ROIs.")){
			roiImp.hide();//画像非表示
			return true;
		}
		return false;
	}

	/************
	現在の Roi で ImageProcessor を crop(カット）し、それにより新たに生成された ImageProcessor を返却する
	 *@param all カットしたい画像
	 *@param cage CageNO
	 *@return 各 Roi でカットされた画像
	 *************/
	public synchronized ImageProcessor split(ImageProcessor all,int cage){
		all.setRoi(roi[cage]);
		ImageProcessor splitIp = all.crop();

		return splitIp;
	}

	/************
	現在の Roi で ImageProcessor を crop(カット）し、それにより新たに生成された ImageProcessor を返却する
	 *@param all カットしたい画像
	 *@return 各 Roi でカットされた画像
	 *************/
	public synchronized ImageProcessor[] split(ImageProcessor all){
		ImageProcessor[] splitIp = new ImageProcessor[allRoi];
		for(int num = 0; num < allRoi; num++){
			all.setRoi(roi[num]);
			splitIp[num] = all.crop();
		}
		return splitIp;
	}

	/************
	メンバのRoi を返す
	 *************/
	public Roi[] getRoi(){
		for(int num = 0; num < allRoi; num++)
			if(roi[num] == null)
				throw new IllegalStateException("roi is null");
		return roi;
	}

	/**
	 *指定したフォルダに含まれるROIファイルを取得する。
	 */
	protected File[] getList(String path){
		File preference = new File(path);
		File[] roiFiles = preference.listFiles(new FilenameFilter(){
			public boolean accept(File dir, String name){
				return name.endsWith(".roi");//ファイル名が".roi"で終わるかどうかでフィルタリングして結果を返す。
			}
		});
		return roiFiles;
	}

	/**
	 指定したファイルからRoiを取得する。
	 */
	protected Roi readRoi(String path) throws IOException{
		RoiDecoder decoder = new RoiDecoder(path);
		Roi roi = null;
		try{
			roi = decoder.getRoi();//pathからROIを取得
		}catch(IOException e){
			throw new IOException();
		}
		return roi;
	}
}