package behavior.setup.dialog;

import behavior.setup.dialog.DialogManager;
import ij.gui.Roi;
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
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.variable.BTIntVariable;
import behavior.setup.parameter.variable.Variable;

public class BTSetCageDialogPanel extends SetCageDialogPanel implements ActionListener{
	private JTextField a;
	private JTextField b;
    private static int goalLine = 2;
    private static int goalArea;
    private static int frameWidthPixel;
    private static int frameHeightPixel;
    private static int frameWidth = 100;
    private static int frameHeight = 10;
    //private final Pattern pattern = Pattern.compile("[1-2]?[0-9]?[0-9]");

	public BTSetCageDialogPanel(DialogManager manager, int allCage) {
		super(manager, allCage);
	}

	protected void createDialog() {
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 10, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		comboBox = new JComboBox();
		comboBox.setPreferredSize(new Dimension(80,18));
		comboBox.addItem("MainArea");

		set = new ExtendedJButton("Set");
		set.addActionListener(this);

		add(new JLabel("RoiType: "), gbc);
		gbc.gridx = 1;
		add(comboBox, gbc);
		gbc.gridx = 2;
		add(set, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		copy = new JButton("Import");
		copy.addActionListener(this);
		add(copy, gbc);

		gbc.gridy += 2;
		gbc.gridx = 0;
		add(new JLabel("Frame Width(cm):"),gbc);
		gbc.gridx = 2;
		a = new JTextField(frameWidth+"",2);
		a.setEditable(true);
		a.setBackground(Color.white);
		add(a, gbc);

		gbc.gridy += 1;
		gbc.gridx = 0;
		add(new JLabel("Goal Line(cm):"),gbc);
		gbc.gridx = 2;
		b = new JTextField(goalLine+"",2);
		b.setEditable(true);
		b.setBackground(Color.white);
		add(b, gbc);
	}

	public void load(Properties properties) {
		if(properties != null){
		    Parameter parameter = Parameter.getInstance();
		    for(int i=1; parameter.getVar(i) != null; i++){
			    if(parameter.getVar(i) instanceof BTIntVariable){
			        if(parameter.getVar(i).getName().equals("frame.width") ){
				        parameter.getVar(i).load(properties);
			            frameWidth = ((BTIntVariable) parameter.getVar(i)).getVariable();
			        }else if(parameter.getVar(i).getName().equals("frame.height")){
				        parameter.getVar(i).load(properties);
				        frameHeight = ((BTIntVariable) parameter.getVar(i)).getVariable();
				    }else if(parameter.getVar(i).getName().equals("goalLine")){
				        parameter.getVar(i).load(properties);
			            goalLine = ((BTIntVariable) parameter.getVar(i)).getVariable();
			        }
			    }
		    }
	    }
	}

	public void preprocess(){
		super.preprocess();

		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String name = "MainArea";
		String roiName = path + "/" + name + ".roi";
		if(!new File(roiName).exists()){
			return;
		}
		Roi roi = null;
		try{
			roi = new RoiDecoder(roiName).getRoi();
		}catch(IOException e){
			e.printStackTrace();
		}

		Rectangle rec = roi.getPolygon().getBounds();

	    frameWidthPixel = rec.width;
	    frameHeightPixel = rec.height;
	    double frameRate = (double)frameWidthPixel/frameWidth;

	    Double frame = new Double(frameHeightPixel/frameRate);
	    frameHeight = frame.intValue();
	    goalArea = (int) Math.round(goalLine*frameRate);
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			super.actionPerformed(e);
		}else if(e.getSource() == copy){
			new BTCopyRoiDialog(manager, true, allCage);
			manager.setModifiedParameter(true);
		}
	}

	/**
	 * ROI を書き出す。
	 */
	protected void writeRoi(Roi roi) {
		try{
		    frameWidth = Integer.parseInt(a.getText());
		    goalLine = Integer.parseInt(b.getText());
		}catch(NumberFormatException e){
			BehaviorDialog.showErrorDialog(this, "Invalid parameter.");
		    return;
		}

		if((frameWidth < 1 || 200 < frameWidth) || (goalLine < 1 || 200 < goalLine)){
			BehaviorDialog.showErrorDialog(this, "Invalid parameter.");
		    return;
	    }

		if(roi != null){
			if(roi.getType() != Roi.RECTANGLE){
				BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				return;
			}
			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + "/";
			String fileName;

			fileName = comboBox.getSelectedItem().toString() + ".roi";

			File file = new File(path + fileName);
			File dir = new File(path);
			if(!dir.exists()){
				dir.mkdirs();
			}

			try{
				OutputStream output_stream = new FileOutputStream(file);
				RoiEncoder encoder = new RoiEncoder(output_stream);
				encoder.write(roi);
				output_stream.close();
			}catch(Exception exception){
				exception.printStackTrace();
			}

			Rectangle rec = roi.getPolygon().getBounds();

			frameWidthPixel = rec.width;
			frameHeightPixel = rec.height;
		    double frameRate = (double)frameWidthPixel/frameWidth;

			Double frame = new Double(frameHeightPixel/frameRate);
		    frameHeight = frame.intValue();
		}
	}

	/**
	 * 現在セットされている ROI を表示。
	 */
	protected ImageProcessor createRoiImage() throws IOException {
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();

		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

      	//MainAreaを描く
		ip.setColor(Color.red);
      	String name = "MainArea";
		String roiName = path + "/" + name + ".roi";
		if(!new File(roiName).exists()){
			return ip;
		}
		Roi roi = new RoiDecoder(roiName).getRoi();
		roi.drawPixels(ip);
		Rectangle rec = roi.getPolygon().getBounds();
		String str1 = name;
		char[] chars1 = str1.toCharArray();
		ip.drawString(str1, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars1, 0, chars1.length)) / 2, rec.y
				+ (rec.height + ip.getFontMetrics().getAscent()) / 2);

		//goalAreaを設定する
		double frameRate = (double)frameWidthPixel/frameWidth;
        goalArea = (int)Math.round(goalLine*frameRate);

        //GoalLineを描く
		ip.setColor(Color.green);
		ip.drawLine((rec.x+rec.width)-goalArea, rec.y, (rec.x+rec.width)-goalArea, rec.y+rec.height);
		String str2 = "Goal" + "\n" + "Line";
		char[] chars2 = str2.toCharArray();
		ip.drawString(str2, (rec.x+rec.width)-(goalArea+2) - ip.getFontMetrics().charsWidth(chars2, 0, chars2.length) / 2, rec.y
				+ (rec.height + ip.getFontMetrics().getAscent()) / 2);

		return ip;
	}

	public boolean canGoNext() {
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String roiName = path + System.getProperty("file.separator") + "MainArea" + ".roi";
		if(!new File(roiName).exists()){
			BehaviorDialog.showErrorDialog(this, "MainArea" + " is not set.");
			return false;
		}
		if(frameWidth != Integer.parseInt(a.getText()) || goalLine != Integer.parseInt(b.getText())){
			BehaviorDialog.showErrorDialog(this, "Setting Error：please set again.");
	        return false;
		}
		if(frameWidth < goalLine){
			BehaviorDialog.showErrorDialog(this, "Setting Error:Goal Line is invalid..");
		    return false;
		}
		Parameter parameter = Parameter.getInstance();
		Variable[] vars = parameter.getVar();
		for(int i=1; vars[i] != null; i++){
			try{
			  if(vars[i] instanceof BTIntVariable){
			     if(vars[i].getName().equals("frame.width") )
				    parameter.getVar(i).setVar(frameWidth);
			     else if(vars[i].getName().equals("frame.height"))
				    parameter.getVar(i).setVar(frameHeight);
			     else if(vars[i].getName().equals("goalLine"))
				    parameter.getVar(i).setVar(goalLine);
			     else if(vars[i].getName().equals("goalArea"))
				    parameter.getVar(i).setVar(goalArea);
		       	}
		      }catch(Exception e){
			     BehaviorDialog.showErrorDialog(manager, "Invalid input!!");
			     return false;
		      }
		}

		return true;
	}
}