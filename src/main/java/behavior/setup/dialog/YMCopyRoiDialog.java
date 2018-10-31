package behavior.setup.dialog;

import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import javax.swing.JDialog;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;

public class YMCopyRoiDialog extends CopyRoiDialog{
	private String fromPath;

	public YMCopyRoiDialog(JDialog manager, boolean isModal){
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
		final String sep = System.getProperty("file.separator");

		String toPath = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String[] tmp = toPath.split(sep.equals("\\") ? "\\\\" : sep);
		tmp[tmp.length - 2] = (String)list.getSelectedItem();
		StringBuffer buf = new StringBuffer();
		for(int i = 0; i < tmp.length; i++)
			if(!tmp[i].equals(""))
				buf.append(sep).append(tmp[i]);
		fromPath = buf.toString();

		if(!(new File(fromPath).exists()))
			BehaviorDialog.showErrorDialog(this, "Not Found: " + fromPath);
		else{
			if(isOnline){
			    File target = new File(fromPath + sep + "MainArea.roi");
		        Roi mainRoi = null;
		        if(!target.exists()){
			        BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
		        }else{
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
			}

			File targetC = new File(fromPath + sep + "Center.roi");
	        if(!targetC.exists()){
		        BehaviorDialog.showErrorDialog(this, "Not Found: " + targetC.getAbsolutePath());
	        }else{
		        try{
			        Rectangle center = new RoiDecoder(targetC.getAbsolutePath()).getRoi().getBounds();
			        Roi centerRoi = new OvalRoi(center.x,center.y,center.width,center.height);
				    String toRoi = toPath + sep + "Center.roi";
				    OutputStream output_stream = new FileOutputStream(toRoi);
				    RoiEncoder encoder = new RoiEncoder(output_stream);
				    encoder.write(centerRoi);
				    output_stream.close();
		        }catch(Exception e){
			        e.printStackTrace();
			    }
		    }

			for(int i=0;i<3;i++){
				File target = new File(fromPath + sep + "Arm"+(i+1)+".roi");
		        Roi armRoi = null;
		        if(!target.exists()){
			        BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
		        }else{
			        try{
				        armRoi = new RoiDecoder(target.getAbsolutePath()).getRoi();
					    String toRoi = toPath + sep + "Arm"+(i+1)+".roi";
					    OutputStream output_stream = new FileOutputStream(toRoi);
					    RoiEncoder encoder = new RoiEncoder(output_stream);
					    encoder.write(armRoi);
					    output_stream.close();
			        }catch(Exception e){
				        e.printStackTrace();
				    }
			    }

		        target = new File(fromPath + sep + "Arm"+(i+1)+"Outer.roi");
		        armRoi = null;
		        if(!target.exists()){
			        BehaviorDialog.showErrorDialog(this, "Not Found: " + target.getAbsolutePath());
		        }else{
			        try{
				        armRoi = new RoiDecoder(target.getAbsolutePath()).getRoi();
					    String toRoi = toPath + sep + "Arm"+(i+1)+"Outer.roi";
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