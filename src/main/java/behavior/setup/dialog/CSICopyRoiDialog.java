package behavior.setup.dialog;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import javax.swing.JDialog;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;

public class CSICopyRoiDialog extends CopyRoiDialog{
	private String fromPath;

	public CSICopyRoiDialog(JDialog manager, boolean isModal) {
		super(manager, isModal, 1);
	}
	public String getSelectedPath(){
		return fromPath;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == ok){
			copyRoi();
			setVisible(false);
		}else if(e.getSource() == cancel){
			fromPath = "";
			setVisible(false);
		}
	}

	@Override
	protected void copyRoi(){
		String sep = System.getProperty("file.separator");

		String toPath = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String[] tmp = toPath.split(sep.equals("\\") ? "\\\\" : sep);
		tmp[tmp.length - 2] = (String)list.getSelectedItem();
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < tmp.length; i++)
			if(!tmp[i].equals(""))
				buf.append(sep).append(tmp[i]);
		fromPath = buf.toString();

		if(!(new File(fromPath).exists())){
			BehaviorDialog.showErrorDialog(this, "Not Found: " + fromPath);
		}else{
			if(isOnline){
			    File target = new File(fromPath + sep + "MainArea.roi");
		        read(target,toPath + sep + "MainArea.roi");
			}

		    File target = new File(fromPath + sep + "RInner.roi");
		    read(target,toPath + sep + "RInner.roi");

		    target = new File(fromPath + sep + "LInner.roi");
		    read(target,toPath + sep + "LInner.roi");

		    target = new File(fromPath + sep + "ROuter.roi");
		    read(target,toPath + sep + "ROuter.roi");

		    target = new File(fromPath + sep + "LOuter.roi");
		    read(target,toPath + sep + "LOuter.roi");

		    target = new File(fromPath + sep + "RCageMask.roi");
		    if(target.exists()) read(target,toPath + sep + "RCageMask.roi");

		    target = new File(fromPath + sep + "LCageMask.roi");
		    if(target.exists()) read(target,toPath + sep + "LCageMask.roi");
		}
	}

	private void read(File target, String path){
		if(!target.exists())
			   BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
		    else{
			   try{
				  Roi roi = new RoiDecoder(target.getAbsolutePath()).getRoi();
				  OutputStream output_stream = new FileOutputStream(path);
				  RoiEncoder encoder = new RoiEncoder(output_stream);
				  encoder.write(roi);
				  output_stream.close();
			   }catch(Exception e){
				  e.printStackTrace();
			   }
		    }
	}
}