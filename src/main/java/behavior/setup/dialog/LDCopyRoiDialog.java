package behavior.setup.dialog;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JDialog;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;

public class LDCopyRoiDialog extends CopyRoiDialog {
	public LDCopyRoiDialog(JDialog manager, boolean isModal, int allCage){
		super(manager, isModal, allCage);
	}

	protected void copyRoi(){
		String sep = System.getProperty("file.separator");

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
		else
			for(int i = 0; i < allCage; i++){
				File target = new File(fromPath + sep + "Cage Field" + (i + 1) + "L.roi");
				if(!target.exists())
					BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
				else{
					try{
						Roi roi = new RoiDecoder(target.getAbsolutePath()).getRoi();
						String toRoi = toPath + sep + "Cage Field" + (i + 1) + "L.roi";
						OutputStream output_stream = new FileOutputStream(toRoi);
						RoiEncoder encoder = new RoiEncoder(output_stream);
						encoder.write(roi);
						output_stream.close();
					} catch(Exception e){
						e.printStackTrace();
					}
				}

				target = new File(fromPath + sep + "Cage Field" + (i + 1) + "D.roi");
				if(!target.exists())
					BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
				else{
					try{
						Roi roi = new RoiDecoder(target.getAbsolutePath()).getRoi();
						String toRoi = toPath + sep + "Cage Field" + (i + 1) + "D.roi";
						OutputStream output_stream = new FileOutputStream(toRoi);
						RoiEncoder encoder = new RoiEncoder(output_stream);
						encoder.write(roi);
						output_stream.close();
					} catch(Exception e){
						e.printStackTrace();
					}
				}
			}
	}
}
