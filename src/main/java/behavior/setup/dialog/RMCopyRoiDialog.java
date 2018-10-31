package behavior.setup.dialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JDialog;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;

public class RMCopyRoiDialog extends CopyRoiDialog {

	public RMCopyRoiDialog(JDialog manager, boolean isModal, int allCage){
		super(manager, isModal, allCage);
	}

	protected void copyRoi(){
		final String sep = System.getProperty("file.separator");

		String toPath = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String[] tmp = toPath.split(sep.equals("\\") ? "\\\\" : sep);
		tmp[tmp.length - 2] = (String)list.getSelectedItem();
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < tmp.length; i++)
			if(!tmp[i].equals(""))
				buf.append(sep).append(tmp[i]);
		String fromPath = buf.toString();

		if(!(new File(fromPath).exists()))
			BehaviorDialog.showErrorDialog(this, "Not Found: " + fromPath);
		else{
		    File target = new File(fromPath + sep + "MainArea.roi");
		    Roi mainRoi = null;
		    if(!target.exists())
			   BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
		    else{
			   try{
				  mainRoi = new RoiDecoder(target.getAbsolutePath()).getRoi();
				  String toRoi = toPath + sep + "MainArea.roi";
				  OutputStream output_stream = new FileOutputStream(toRoi);
				  RoiEncoder encoder = new RoiEncoder(output_stream);
				  encoder.write(mainRoi);
				  output_stream.close();
			   }catch(Exception e){
				  e.printStackTrace();
			   }
		    }

		    target = new File(fromPath + sep + "Center.roi");
		    Roi centerRoi = null;
		    if(!target.exists())
			   BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
		    else{
			   try{
				  centerRoi = new RoiDecoder(target.getAbsolutePath()).getRoi();
				  String toRoi = toPath + sep + "Center.roi";
				  OutputStream output_stream = new FileOutputStream(toRoi);
				  RoiEncoder encoder = new RoiEncoder(output_stream);
				  encoder.write(centerRoi);
				  output_stream.close();
			   }catch(Exception e){
				  e.printStackTrace();
			   }
		    }

		    for(int arm=0;arm<8;arm++){
		        target = new File(fromPath + sep + "Arm"+(arm+1)+".roi");
			    Roi armRoi = null;
			    if(!target.exists())
				   BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
			    else{
				   try{
					  armRoi = new RoiDecoder(target.getAbsolutePath()).getRoi();
					  String toRoi = toPath + sep + "Arm"+(arm+1)+".roi";
					  OutputStream output_stream = new FileOutputStream(toRoi);
					  RoiEncoder encoder = new RoiEncoder(output_stream);
					  encoder.write(armRoi);
					  output_stream.close();
				   }catch(Exception e){
					  e.printStackTrace();
				   }
			    }
		    }
		}
	}
}