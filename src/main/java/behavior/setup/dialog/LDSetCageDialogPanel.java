package behavior.setup.dialog;

import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.image.ImageCapture;
import behavior.io.FileManager;

/**
 * LD 用 Set Cage Field
 * Cage 毎に同じ高さの ROI である Light と Dark を指定する必要がある。
 */
public class LDSetCageDialogPanel extends SetCageDialogPanel {

	private JComboBox LorD;

	public LDSetCageDialogPanel(DialogManager manager, int allCage) {
		super(manager, allCage);
	}

	protected void createDialog(){
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		comboBox = new JComboBox();
		for(int i = 1; i <= allCage; i++)
			comboBox.addItem(Integer.toString(i));

		LorD = new JComboBox();
		LorD.addItem("D");
		LorD.addItem("L");

		add(new JLabel("L or D: "), gbc);
		gbc.gridx = 1;
		gbc.ipadx = 5;
		add(LorD, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.ipadx = 0;
		add(new JLabel("Chamber: "), gbc);
		gbc.gridx = 1;
		gbc.ipadx = 5;
		add(comboBox, gbc);
		
		set = new ExtendedJButton("Set");
		set.addActionListener(this);
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 2;
		add(set, gbc);

		copy = new ExtendedJButton("Import");
		copy.addActionListener(new CopyActionListener());
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.ipadx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		add(copy, gbc);
	}

	protected void writeRoi(Roi roi){
		if(roi != null){
			if(roi.getType() != Roi.RECTANGLE){
				BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				return;
			}
			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + "/";
			String fileName;

			fileName = "Cage Field" + comboBox.getSelectedItem() + LorD.getSelectedItem() + ".roi";

			File file = new File(path+fileName);
			File dir = new File(path);
			if(!dir.exists()){
				dir.mkdirs();
			}

			try {
				OutputStream output_stream = new FileOutputStream(file);
				RoiEncoder encoder = new RoiEncoder(output_stream);
				encoder.write(roi);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	protected ImageProcessor createRoiImage() throws IOException{
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();

		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		for(int ld = 0; ld < 2; ld++){
			String LD;
			if(ld == 0){
				ip.setColor(Color.green);
				LD = "L";
			} else {
				ip.setColor(Color.blue);
				LD = "D";
			}
			for(int i = 1; i <= allCage; i++){
				String roiName = path + "/Cage Field" + i + LD + ".roi";
				if(!new File(roiName).exists())
					continue;
				Roi roi = new RoiDecoder(roiName).getRoi();
				roi.drawPixels(ip);
				Rectangle rec = roi.getPolygon().getBounds();
				String str = i + LD;
				char[] chars = str.toCharArray();
				ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
						rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			}
		}

		return ip;
	}

	public boolean canGoNext() {   
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		for(int ld = 0; ld < 2; ld++){
			String LD = ld == 0 ? "L" : "D";
			for(int i = 1; i <= allCage; i++){
				String roiName = path + "/Cage Field" + i + LD + ".roi";
				if(!new File(roiName).exists()){
					BehaviorDialog.showErrorDialog(this, "Cage Field" + i + LD + " is not set.");
					return false;
				}
			}
		}

		// 高さチェック
		for(int i = 1; i <= allCage; i++){
			try{
				String roiNameD = path + "/Cage Field" + i + "D.roi";
				String roiNameL = path + "/Cage Field" + i + "L.roi";
				Roi roiD = new RoiDecoder(roiNameD).getRoi();
				Roi roiL = new RoiDecoder(roiNameL).getRoi();
				if(roiD.getBounds().height != roiL.getBounds().height){
					BehaviorDialog.showErrorDialog(this, "\"" + i + "D\" & \"" + i + "L\" must have the same height.");
					return false;
				}
			} catch(Exception e){
				e.printStackTrace();
			}
		}

		return true;
	}

	private class CopyActionListener implements ActionListener{

		public void actionPerformed(ActionEvent arg0) {
			new LDCopyRoiDialog(manager, true, allCage);
		}

	}
}
