package behavior.gui.roi;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.process.Blitter;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;

import behavior.gui.OvalDraw;
import behavior.io.FileManager;
import behavior.setup.Program;

/**
 * Cropでは、何故か矩形で切り取られるため、Maskを利用するものに変化
 * また、Nvigator画面の表示も行う（PlatFormや、StartPositionの範囲をさまざまなものに追加して表示する
 * @author tt
 *
 */
public class WMRoiOperator extends RoiOperator {
	public static final int STARTP = 1;
	public static final int POOL = 0;
	public static final int PLATP = 2;
	protected static  int selectStartP = 100;
	protected static int selectPlatP = 0;
	public static final int ErrorselectPlat = 100;

	protected File[] roiFile;
	protected int[] roiType;
	protected int[] spArea;
	public static String[] roiNames;
	public static Roi platFormRoi,poolRoi;
	protected static double[] partitionDegree;
	public static double poolRenge = 0;
	protected double platformWidth,platformHeight,lengthperPixelWidth,lengthperPixelHeight;
	protected Rectangle poolOutline;
	private int pooln;
	/**
	 * RoiOperator を参照
	 */
	public WMRoiOperator(Program program, int allCage) {
		super(program,allCage);
		roi = new Roi[10];
	}
	/**
	 * PlatFormの大きさを習得された画像にあわせcmからpixelに単位を変更する
	 */
	public void setRenge(double renge,double width,double height){
		platformWidth = renge * width;
		platformHeight = renge * height;//Check!
	}

	/**
	 * Platformの大きさを返す　index 0 width ;index 1 height
	 */
	public double[] getPlatFormRenge(){
		double[] renge = {platformWidth,platformHeight};
		return renge;
	}
	/**
	 * 指定されたフォルダにあるRoiファイルを読み出す。
	 * 必要なものしか読み込まないため、不必要なものが存在してもOK。
	 */
	public boolean loadRoi(){
		String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir);
		roiFile = getList(path);
		roi = new Roi[roiFile.length];
		roiType = new int[roiFile.length];
		roiNames = new String[roiFile.length];
		for(int i=0;i<roiFile.length;i++){
			String roiname = roiFile[i].getName();
			if(roiname.startsWith("PlatFormPosition")){
				String roiFile =(FileManager.getInstance()).getPath(FileManager.PreferenceDir) + File.separator+roiname;
				try{
					roi[i] = readRoi(roiFile);
					roiType[i] = PLATP;
					roiNames[i] = roiFile;
				}catch(IOException e){
					IJ.error("ROI(" + roiFile + ") cannot be found. Try to set ROIs or Copy ROIs from other folder.");
					return true;
				}
			}else if(roiname.startsWith("StartPosition")){
				String roiFile =(FileManager.getInstance()).getPath(FileManager.PreferenceDir) + File.separator+roiname;
				try{
					roi[i] = readRoi(roiFile);					
					roiType[i]  = STARTP;
					roiNames[i] = roiFile;
				}catch(IOException e){
					IJ.error("ROI(" + roiFile + ") cannot be found. Try to set ROIs or Copy ROIs from other folder.");
					return true;
				}
			}else if(roiname.startsWith("PoolPosition")){
				String roiFile =(FileManager.getInstance()).getPath(FileManager.PreferenceDir) + File.separator+roiname;
				try{
					roi[i] = readRoi(roiFile);
					poolRoi = readRoi(roiFile);
					pooln = i;
					roiType[i] = POOL;
					roiNames[i] = roiFile;
					poolRenge = (roi[i].getBounds()).getWidth()/2;
					poolOutline=poolRoi.getBounds();
				}catch(IOException e){
					IJ.error("ROI(" + roiFile + ") cannot be found. Try to set ROIs or Copy ROIs from other folder.");
					return true;
				}
			}
		}
		modifyRoi();
		return false;
	}

//	NN	
	protected void modifyRoi(){
		for(int i=0;i<roiNames.length;i++){
			Rectangle outline = roi[i].getBounds();
			if(roiType[i] == PLATP | roiType[i] == STARTP)
				roi[i].setLocation(outline.x - poolOutline.x,outline.y - poolOutline.y);
		}
		//poolRoi.setLocation(0,0);
		roi[pooln].setLocation(0,0);
	}
	/**
	 * 指定された相対位置にある、スタート位置を判別する。
	 * @param ip　利用する画像、中心位置を判別するために必要
	 * @param startnum　指定された相対位置
	 */
	private void setStartArea(ImageProcessor ip,int startnum) {
		spArea = new int[roi.length];
		selectStartP = ErrorselectPlat;
		for(int i=0;i<roi.length;i++){
			if(roiType[i]==STARTP){
				Rectangle sr = roi[i].getBounds();
				double px = sr.getCenterX()  - ip.getWidth()/2;
				double py = sr.getCenterY()  - ip.getHeight()/2;
				double acos = Math.acos(px/Math.sqrt((Math.pow(py,2))+(Math.pow(px,2))));
//				acos=(acos/Math.PI)*180.0;
				if (py<0)
					acos=2*Math.PI-acos;

				int c=0;//一番最初にPI*２を超える角度を見つける
				while(partitionDegree[c] < 2*Math.PI){
					c++;
					if(c > 3)
						break;
				}

				spArea[i] = 5;

				for(int j=0;j<3;j++){
					if(j == c - 1){
						if(partitionDegree[j] < acos & acos <= 2*Math.PI){
							spArea[i] = j;
						}else if(0 <= acos & acos < partitionDegree[j+1] - 2*Math.PI){
							spArea[i] = j;
						}
					}else if(j > c - 1){
						if(partitionDegree[j] - 2*Math.PI< acos & acos <= partitionDegree[j+1] - 2*Math.PI){
							spArea[i] = j;
						}
					}else if(j < c -1 ){
						if(partitionDegree[j] < acos & partitionDegree[j+1] >= acos)
							spArea[i] = j;
					}
				}

				if(spArea[i] == 5)
					spArea[i] = 3;

				if(spArea[i]==1){
					spArea[i] = 3;
				}else if(spArea[i]==2){
					spArea[i] = 1;
				}else if(spArea[i]==3){
					spArea[i] = 2;
				} 

				if(spArea[i] == startnum)
					selectStartP = i;
			}
		}	
	}

	/**
	 * 指定されたプラットフォームのRoiがどれか、判別する。
	 * @param filenum 指定されたファイルナンバー
	 * @return　存在しなっかかった場合trueを返却する
	 */
	public boolean loadPlatFormRoi(int filenum){
		for(int i=0;i<roiFile.length;i++){
			if((roiFile[i].getName()).endsWith("PlatFormPosition" + filenum + ".roi")){
				selectPlatP = i;
				return false;
			}
		}
		return true;
	}

	/**
	 * Test用の描画。
	 * ImagePlus　にに対して、必要な情報を書き加える
	 */
	public void setAreaImage(ImagePlus roiImp){
		ImageProcessor roiIp = roiImp.getProcessor();//画像のプロセッサ取得
		roiIp = roiIp.convertToRGB();//RGBモードに変換
		roiImp.setProcessor("Check ROI", roiIp);	//何故か ip をセットし直さないと、ROIが表示されない
		OvalDraw drawer = new OvalDraw();
		setLocations(roiIp,drawer);
		for(int i=0;i<roi.length;i++){
			Rectangle outline = roi[i].getBounds();
			if(roiType[i] == PLATP){
				drawer.setObject(outline.getCenterX()-platformWidth/2,outline.getCenterY()-platformHeight/2,platformWidth,platformHeight,OvalDraw.OVAL);
				if(i==selectPlatP){
					//	drawer.setLetter(outline.getX(),outline.getY(),"Prime PF");
				}else{
					drawer.setObject(outline.getCenterX()-platformWidth/2,outline.getCenterY()-platformHeight/2,platformWidth,platformHeight,OvalDraw.OVAL);
					drawer.setLetter(outline.getCenterX(),outline.getCenterY(),"PF",Color.red);
				}
			}else if(roiType[i] == STARTP){
				drawer.setObject(outline.getX(),outline.getY(),outline.getWidth(),outline.getHeight(),OvalDraw.OVAL,Color.green);
				drawer.setLetter(outline.getX(),outline.getY()- 20,"SP",Color.green);
			}else{
				drawer.setObject(0,0,poolOutline.getWidth(),poolOutline.getHeight(),OvalDraw.OVAL);
			}
		}
	}

	/**
	 * Prive 用の描画
	 * ImagePlus　に　指定されたスタート位置と、プラットフォームを書き加える
	 * SartArea等の発見に失敗したときTrue
	 */
	public boolean setAreaImage(ImagePlus roiImp,int startnum){
		ImageProcessor roiIp = roiImp.getProcessor();//画像のプロセッサ取得
		roiIp = roiIp.convertToRGB();//RGBモードに変換
		roiImp.setProcessor("Check ROI", roiIp);	//何故か ip をセットし直さないと、ROIが表示されない
		OvalDraw drawer = new OvalDraw();
		setLocations(roiIp,drawer);
		drawer.setLocation(roiImp.getWidth()+30,roiImp.getWidth());
		for(int i=0;i<roi.length;i++){
			Rectangle outline = roi[i].getBounds();
			if(roiType[i] == PLATP){
				drawer.setObject(outline.getCenterX()-platformWidth/2,outline.getCenterY()-platformHeight/2,platformWidth,platformHeight,OvalDraw.OVAL);
				if(i==selectPlatP){
					//	drawer.setLetter(outline.getX(),outline.getY(),"Prime PF");
				}else{
					drawer.setObject(outline.getCenterX()-platformWidth/2,outline.getCenterY()-platformHeight/2,platformWidth,platformHeight,OvalDraw.OVAL);
					drawer.setLetter(outline.getCenterX(),outline.getCenterY(),"PF",Color.red);
				}
			}else if(roiType[i] == STARTP){

			}else{
				drawer.setObject(0,0,poolOutline.getWidth(),poolOutline.getHeight(),OvalDraw.OVAL);
			}
		}
		setStartArea(roiIp,startnum);
		if(selectStartP == ErrorselectPlat){
			IJ.showMessage("no StartPosition found. Please retry with another sequence.");
			return true;
		}
		Rectangle outline2 = roi[selectStartP].getBounds();
		drawer.setObject(outline2.getX(),outline2.getY(),outline2.getWidth(),outline2.getHeight(),OvalDraw.OVAL,Color.green);
		drawer.setLetter(outline2.getX(),outline2.getY()-20,"SP" + startnum,Color.green);
		return false;
	}

	/**
	 * Visible Hidden用の描画
	 * ImagePlusにターゲットとスタート位置を書き加える
	 * @param roiImp
	 * @param platnum
	 * @param startnum
	 */
	public boolean setAreaImage(ImagePlus roiImp,int platnum,int startnum){
		ImageProcessor roiIp = roiImp.getProcessor();//画像のプロセッサ取得
		roiIp = roiIp.convertToRGB();//RGBモードに変換
		roiImp.setProcessor("Check ROI", roiIp);	//何故か ip をセットし直さないと、ROIが表示されない
		OvalDraw drawer = new OvalDraw();
		setLocations(roiIp,drawer);
		drawer.setLocation(roiImp.getWidth()+30,roiImp.getWidth());
		Rectangle outline = roi[selectPlatP].getBounds();
		drawer.setObject(outline.getCenterX()-platformWidth/2,outline.getCenterY()-platformHeight/2,platformWidth,platformHeight,OvalDraw.OVAL);
//		drawer.setLetter(outline.getCenterX(),outline.getCenterY(),"Target PF",Color.red);
		//drawer.setLetter(outline.getX(),outline.getY(),"PF" + selectPlatP);
		setStartArea(roiIp,startnum);
		outline = roi[pooln].getBounds();
		drawer.setObject(0,0,poolOutline.getWidth(),poolOutline.getHeight(),OvalDraw.OVAL);
		if(selectStartP == ErrorselectPlat){
			IJ.showMessage("no StartPosition found. Please retry with another sequence.");
			return true;
		}

		Rectangle outline2 = roi[selectStartP].getBounds();
		drawer.setObject(outline2.getX(),outline2.getY(),outline2.getWidth(),outline2.getHeight(),OvalDraw.OVAL,Color.green);
		drawer.setLetter(outline2.getX(),outline2.getY()-20,"SP" + startnum,Color.green);
		return false;
	}

	/**
	 * 角度を求め、クアドラントを変化させる。
	 * @param roiIp
	 */
	public void setLocations(ImageProcessor ip,OvalDraw d){
		Rectangle pr = roi[selectPlatP].getBounds();
		d.setLetter((int)pr.getCenterX(), (int)pr.getCenterY()+15, "Target PF",Color.red); //ROI の名前の書き込み
		double px = pr.getCenterX()  - ip.getWidth()/2;
		double py = pr.getCenterY()  - ip.getHeight()/2;
		double acos = Math.acos(px/Math.sqrt((Math.pow(py,2))+(Math.pow(px,2))));
		if (py<0)
			acos=2*Math.PI-acos;
		int allArea = 4;
		double[] areaDegree = new double[allArea];
		areaDegree[0] = acos + - Math.PI/allArea;
		if(areaDegree[0] < 0)
			areaDegree[0] += Math.PI*2;
		d.setObject((double)ip.getWidth()/2,(double)ip.getHeight()/2,(ip.getWidth()*Math.cos(areaDegree[0]))+ip.getWidth()/2,(ip.getWidth()*Math.sin(areaDegree[0]))+ip.getHeight()/2,OvalDraw.LINE,Color.red);
		d.setLetter((Math.cos(acos + 0*Math.PI*2/allArea)*30) + ip.getWidth()/2,(Math.sin(acos+ 0*Math.PI*2/allArea)*30)+ip.getHeight()/2,"A"+0,Color.blue);
		for(int i=1;i<allArea;i++){
			areaDegree[i] = areaDegree[0] + (i*2*Math.PI)/allArea;//下の行長いので、修正必要
			d.setObject((double)ip.getWidth()/2,(double)ip.getHeight()/2,(ip.getWidth()*Math.cos(areaDegree[i]))+ip.getWidth()/2,(ip.getWidth()*Math.sin(areaDegree[i]))+ip.getHeight()/2,OvalDraw.LINE,Color.red);
			int area = 0;
			if(i==1)
				area = 3;
			if(i==2)
				area = 1;
			if(i==3)
				area = 2;
			d.setLetter((Math.cos(acos + i*Math.PI*2/allArea)*30) + ip.getWidth()/2,(Math.sin(acos+ i*Math.PI*2/allArea)*30)+ip.getHeight()/2,"A"+area,Color.blue);
		}

		partitionDegree = areaDegree;
	}

	/**
	 * Poolの外接のみ表示する。
	 */
	public ImageProcessor[] split(ImageProcessor all){
		ImageProcessor[] currentIp = new ImageProcessor[1];
		currentIp[0] = all;
		currentIp[0].setRoi(poolRoi);
		currentIp[0] = currentIp[0].crop();
		currentIp[0].setRoi(new OvalRoi(0,0,poolOutline.width,poolOutline.height));
		ImageProcessor mask = currentIp[0].getMask();
		mask.invert();
		currentIp[0].invertLut();
		currentIp[0].copyBits(mask, 0, 0, Blitter.SUBTRACT);

		return currentIp;
	}

	/**
	 * クアドラントを仕切る角度を返す
	 * @return　partitionDegree ０が最も小さい角度
	 */
	public static double[] getPartitionDegree(){
		return partitionDegree;
	}

	/**
	 * Hidden　Visible
	 * プラットフォームの内部を現すためのIpを返す
	 * @return
	 */
	public ImageProcessor splitPlatForm(ImageProcessor ip){
		ip.setRoi(getPlatForm());
		ImageProcessor maskIp = ip.getMask();
		ip = ip.crop();
		ImageProcessor subtractIp = ip.duplicate();
		subtractIp.copyBits(maskIp, 0, 0, Blitter.SUBTRACT);
		return subtractIp;
	}

	/**
	 * Probe
	 * プラットフォームの内部を現すためのIpを返す
	 * @return
	 */
	public ImageProcessor[] splitPlatForms(ImageProcessor ip){
		OvalRoi[] proi = getPlatForms();
		ImageProcessor[] subtractIps = new ImageProcessor[proi.length];
		for(int plat = 0;plat<proi.length;plat++){
			ip.setRoi(proi[plat]);
			ImageProcessor maskIp = ip.getMask();
			ip = ip.crop();
			subtractIps[plat] = ip.crop();
			subtractIps[plat].copyBits(maskIp, 0, 0, Blitter.SUBTRACT);
		}
		return subtractIps;
	}

	public OvalRoi[] getPlatForms(){
		OvalRoi[] pfroi = new OvalRoi[4];
		int count = 0;
		for(int i=0;i < roi.length;i++){
			if(roiType[i] == PLATP){
				Rectangle pr = roi[i].getBounds();
				pfroi[count] = new OvalRoi((int)(pr.getCenterX()-platformWidth/2),(int)(pr.getCenterY()-platformHeight/2),(int)platformWidth,(int)platformHeight);
				count++;
			}
		}
		return pfroi;

	}

	public OvalRoi getPlatForm(){
		Rectangle pr = roi[selectPlatP].getBounds();
		OvalRoi pfroi = new OvalRoi((int)(pr.getCenterX()-platformWidth/2),(int)(pr.getCenterY()-platformHeight/2),(int)platformWidth,(int)platformHeight);
		return pfroi;
	}


	public Roi getSelectedStart(){
		return roi[selectStartP];
	}
}