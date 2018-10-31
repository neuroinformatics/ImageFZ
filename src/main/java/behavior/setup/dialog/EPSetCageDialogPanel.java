package behavior.setup.dialog;

import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.Dimension;
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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.image.ImageCapture;
import behavior.io.FileManager;

public class EPSetCageDialogPanel extends SetCageDialogPanel implements ActionListener{

	private final String sep = System.getProperty("file.separator");

	public EPSetCageDialogPanel(DialogManager manager, int allCage) {
		super(manager, allCage);
	}

	protected void createDialog(){
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 10, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		comboBox = new JComboBox();
		comboBox.setPreferredSize(new Dimension(60,18));
		comboBox.addItem("MainArea");
		comboBox.addItem("Center");

		set = new ExtendedJButton("Set");
		set.addActionListener(this);

		add(new JLabel("Roi type: "), gbc);
		gbc.gridx = 1;
		gbc.ipadx = 20;
		add(comboBox, gbc);
		gbc.gridx = 2;
		gbc.ipadx = 0;
		add(set, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		copy = new JButton("Import");
		copy.addActionListener(this);
		add(copy, gbc);
	}

	public void preprocess(){
		super.preprocess();
		Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			super.actionPerformed(e);
		} else if(e.getSource() == copy) {
			new EPCopyRoiDialog(manager, true, allCage);
		}
	}

	protected void writeRoi(Roi roi){
		if(roi != null){
			if(roi.getType() != Roi.RECTANGLE){
				BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				return;
			}
			final String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
			String fileName;

			//Center
			if(comboBox.getSelectedItem().toString().equals("Center")){
				final String mainRoiPath = path +sep+ "Cage Field1.roi";
	            File checkFile = new File(mainRoiPath);
			    if(!checkFile.exists()){
			    	BehaviorDialog.showErrorDialog(this, "Set MainArea before.");
				    return;
			    }

			    fileName = "center1.roi";
                File file = new File(path+sep+fileName);
			    File dir = new File(path);
			    if(!dir.exists()){
				    dir.mkdirs();
			    }

			    Roi mainRoi = null;
				try{
				    mainRoi = new RoiDecoder(mainRoiPath).getRoi();
				}catch(Exception e){
					e.printStackTrace();
				}
				final Rectangle mainRec = mainRoi.getBounds();

				final Rectangle rec = roi.getBounds();
				Roi centerRoi= new Roi(rec.x-mainRec.x,rec.y-mainRec.y,rec.width,rec.height);
				try {
				    OutputStream output_stream = new FileOutputStream(file);
				    RoiEncoder encoder = new RoiEncoder(output_stream);
				    encoder.write(centerRoi);
				    output_stream.close();
				}catch (Exception exception) {
				    exception.printStackTrace();
				}
			}else if(comboBox.getSelectedItem().toString().equals("MainArea")){   //MainArea
				fileName = "Cage Field1.roi";
	            File file = new File(path+sep+fileName);
				File dir = new File(path);
				if(!dir.exists()){
				  dir.mkdirs();
				}

				try {
				  OutputStream output_stream = new FileOutputStream(file);
				  RoiEncoder encoder = new RoiEncoder(output_stream);
				  encoder.write(roi);
				  output_stream.close();
				}catch (Exception exception) {
				   exception.printStackTrace();
				}
			}		
		}
	}

	//âfëúÇ…RoiÇï`Ç≠
	protected ImageProcessor createRoiImage() throws IOException{
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();

		//MainAreaÇï`Ç≠
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String roiName = path + sep + "Cage Field1.roi";
		if(new File(roiName).exists()){
		    ip.setColor(Color.red);
		    Roi mainRoi = new RoiDecoder(roiName).getRoi();
		    mainRoi.drawPixels(ip);

		    //CenterÇï`Ç≠
			String roiName2 = path + sep + "center1.roi";
		    if(new File(roiName2).exists()){
                final Rectangle mainRec = mainRoi.getBounds();

				Roi bufRoi = new RoiDecoder(roiName2).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new Roi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.blue);
				centerRoi.drawPixels(ip);
				Rectangle centerRec = centerRoi.getPolygon().getBounds();
			    String str2 = "Center";
			    char[] chars2 = str2.toCharArray();
			    ip.drawString(str2, centerRec.x + (centerRec.width - ip.getFontMetrics().charsWidth(chars2, 0, chars2.length)) / 2, 
			                    	centerRec.y + (centerRec.height + ip.getFontMetrics().getAscent()) / 2);
			 }
		}

		return ip;
	}

	//éüÇÃÉXÉeÉbÉvÇ…êiÇÒÇ≈ÇÊÇ¢Ç©
	public boolean canGoNext() {
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//RoiÇê›íËçœÇ›Ç©Ç«Ç§Ç©ämîF
		String roiName = path + sep + "Cage Field1.roi";
		if(!new File(roiName).exists()){
			BehaviorDialog.showErrorDialog(this, "MainArea is not set.");
			return false;
		}
		String roiName2 = path + sep + "center1.roi";
		if(!new File(roiName2).exists()){
			BehaviorDialog.showErrorDialog(this, "Center is not set.");
			return false;
		}
		
		return true;
	}
}