package behavior.gui.roi;

import java.awt.*;
import java.io.*;

import ij.*;
import ij.process.*;
import ij.gui.*;

import behavior.image.process.Fusion;
import behavior.io.FileManager;

public class LDRoiOperator extends RoiOperator{
	private int allCage;

	public LDRoiOperator(int allCage){
		this.allCage = allCage;
		allRoi = allCage*2;
		roi = new Roi[allRoi];
	}

	@Override
	public boolean loadRoi(){
		String path = (FileManager.getInstance()).getPath(FileManager.PreferenceDir);
		for(int num = 0; num < allRoi; num++){
			String roiFile = null;
			if((num + 1) % 2 != 0){
				roiFile = path + File.separator + "Cage Field" + ((num + 1) / 2 + 1) + "D.roi";
			}else{
				roiFile = path + File.separator + "Cage Field" + ((num + 1) / 2) + "L.roi";
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

	@Override
	public boolean checkRoi(ImagePlus roiImp){
		ImageProcessor roiIp = roiImp.getProcessor();
		roiIp = roiIp.convertToRGB();
		roiImp.setProcessor("Check ROI", roiIp);
		roiIp.setColor(Color.red);
		roiImp.show();
		for(int num = 0; num < allRoi; num++){
			String name = null;
			if((num + 1) % 2 != 0){
				name = "Cage Field" + ((num + 1) / 2 + 1) + "D";
			}else{
				name = "Cage Field" + ((num + 1) / 2) + "L";
			}
			Polygon polygonRoi = roi[num].getPolygon();
			Rectangle outline = polygonRoi.getBounds();
			roiIp.drawString(name, outline.x, outline.y);
			roiIp.drawPolygon(polygonRoi);
		}
		roiImp.updateAndDraw();

		if(!IJ.showMessageWithCancel("Field Check", "use these ROIs?\nPress Yes if ROIs are set properly.\nPress No to quit this plugin and reset ROIs.")){
			roiImp.hide();
			return true;
		}
		return false;
	}

	@Override
	public synchronized ImageProcessor split(ImageProcessor all,int cage){
		all.setRoi(roi[cage * 2]);
		ImageProcessor splitIp = all.crop();
		all.setRoi(roi[cage * 2 + 1]);
		ImageProcessor splitIp2 = all.crop();

		ImageProcessor newCurrentIp = Fusion.fusionImage(splitIp, splitIp2);
		return newCurrentIp;
	}

	@Override
	public synchronized ImageProcessor[] split(ImageProcessor all){
		ImageProcessor[] splitIp = new ImageProcessor[allRoi];
		for(int num = 0; num < allRoi; num++){
			all.setRoi(roi[num]);
			splitIp[num] = all.crop();
		}

		ImageProcessor[] newCurrentIp = new ImageProcessor[allCage];
		for(int cage = 0; cage < allCage; cage++){
			newCurrentIp[cage] = Fusion.fusionImage(splitIp[cage * 2], splitIp[cage * 2 + 1]);
		}
		return newCurrentIp;
	}
}