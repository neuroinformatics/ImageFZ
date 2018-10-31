package behavior.setup.dialog;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.OvalRoi;
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
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;


import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.WindowOperator;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.io.ImageLoader;
import behavior.plugin.analyzer.BMAnalyzer;
import behavior.setup.Setup;
import behavior.setup.parameter.BMParameter;

public class BMSetCageDialogPanel extends SetCageDialogPanel implements ActionListener {
	private final int ALLHOLE = 12;	//穴の数
	private final String sep = System.getProperty("file.separator");
	private static int target;
	private ExtendedJButton auto;
	private JComboBox cbox;
	private int type;
	private OffRoiMovie offmovie;
	private Rectangle field;
	private JTextField armRField;
	private JTextField distOutField;
	private boolean flag;

	public BMSetCageDialogPanel(DialogManager manager, int allCage, int type) {
		super(manager, allCage);
		this.type = type;
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
		comboBox.addItem("Field");
		for(int i = 1; i <= ALLHOLE; i++)
			comboBox.addItem("hole" + Integer.toString(i));
		
		cbox = new JComboBox();
		for (int i = 1; i <= ALLHOLE; i++)
			cbox.addItem(Integer.toString(i));

		set = new ExtendedJButton("Set");
		set.addActionListener(this);
		auto = new ExtendedJButton("Auto");
		auto.addActionListener(this);
		
		//armR,distOutの値はpreprocess()でセットする（ここではPropatiesファイルの値が読み込まれていないため)。
		armRField = new JTextField(2);
		distOutField = new JTextField(2);
		
		gbc.anchor = GridBagConstraints.EAST;
		add(new JLabel("Roi : "), gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridx = 1;
		gbc.ipadx = 20;
		add(comboBox, gbc);
		gbc.gridx = 2;
		gbc.ipadx = 0;
		add(set, gbc);
		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		copy = new JButton("Import");
		copy.addActionListener(this);
		add(copy, gbc);
		gbc.gridx = 2;
		add(auto, gbc);
		gbc.gridx = 1;
		gbc.gridy++;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.ipadx = 10;
		add(new JLabel("armR(diameter:cm) : "), gbc);
		gbc.gridx = 2;
		add(armRField, gbc);
		gbc.gridx = 1;
		gbc.gridy++;
		add(new JLabel("distOut(diameter:cm) : "), gbc);
		gbc.gridx = 2;
		add(distOutField, gbc);
		
	}

	public void preprocess(){
		flag = false;
		armRField.setText("" + BMParameter.getarmR());
		distOutField.setText("" + BMParameter.getDistOut());
		if (type == Setup.ONLINE)
			super.preprocess();
		else {
			offmovie = new OffRoiMovie();
			offmovie.start();
			comboBox.removeItemAt(0);
		}
	}
	
	public void endprocess(){
		if (type == Setup.ONLINE)
			super.endprocess();
		else 
			offmovie.end();
	}
	
	/*
	public void postprocess(){
		comboBox.requestFocus();
	}
	*/
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == auto) {
			writeRoi_Auto();
		} else if(e.getSource() == set){
			try {
				Integer.parseInt(armRField.getText());
				Integer.parseInt(distOutField.getText());
			} catch (Exception exception) {
				BehaviorDialog.showErrorDialog("Invalid input!!");
				return;
			}
			BMParameter.setarmR(Integer.parseInt(armRField.getText()) );
			BMParameter.setDistOut(Integer.parseInt(distOutField.getText()) );
			if (type == Setup.ONLINE)
				super.actionPerformed(e);
			else { 
				Roi roi = offmovie.getRoi();
				Rectangle rec = roi.getBounds();
				roi = new Roi(field.x + rec.x, field.y + rec.y, rec.width, rec.height);
				writeRoi(roi);
			}
			
		} else if(e.getSource() == copy) {
			manager.setModifiedParameter(true);
			new BMCopyRoiDialog(manager, true, allCage);
		}
	}

	public boolean canGoNext() {   
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		if (!new File(path + sep + "Field.roi").exists()) {
			BehaviorDialog.showErrorDialog(this, "Field is not set.");
			return false;
		}
		for(int i = 1; i <= ALLHOLE; i++){
			String roiName = path + sep + "hole" + i + ".roi";
			if(!new File(roiName).exists()){
				BehaviorDialog.showErrorDialog(this, "hole" + i + " is not set.");
				return false;
			}
		}
		if (!(1 <= target && target <= 12) && type == Setup.ONLINE) {
			BehaviorDialog.showErrorDialog(this, "target is not set.");
			return false;
		}
		try {
			Integer.parseInt(armRField.getText());
			Integer.parseInt(distOutField.getText());
		} catch (Exception e) {
			BehaviorDialog.showErrorDialog("Invalid input!!");
			return false;
		}
		BMParameter.setarmR(Integer.parseInt(armRField.getText()) );
		BMParameter.setDistOut(Integer.parseInt(distOutField.getText()) );
		if (type == Setup.ONLINE)
			setTargetHole();
		return true;
	}

	/**
	 * ROI を書き出す。
	 */
	protected void writeRoi(Roi roi){
		if(roi != null){
			if(roi.getType() != Roi.RECTANGLE){
				BehaviorDialog.showErrorDialog(this, "A rectangle ROI is required.");
				return;
			}
			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;
			String fileName= comboBox.getSelectedItem() + ".roi";

			File file = new File(path+fileName);
			File dir = new File(path);
			if(!dir.exists()){
				dir.mkdirs();
			}

			try {
				OutputStream output_stream = new FileOutputStream(file);
				RoiEncoder encoder = new RoiEncoder(output_stream);
				encoder.write(roi);
				output_stream.close();
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}
	
	private void writeRoi_Auto() {
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;
		if (!(new File(path + "Field.roi").exists()) || !(new File(path + "hole1.roi").exists())) {
			BehaviorDialog.showErrorDialog(this, "Please set \"Field.roi\" and \"hole1.roi\".");
			return;
		} else {
			try {
				Roi field_roi = new RoiDecoder(path + "Field.roi").getRoi();
				Roi hole1_roi = new RoiDecoder(path + "hole1.roi").getRoi();
				if(hole1_roi != null && field_roi != null){
					if(hole1_roi.getType() != Roi.RECTANGLE || field_roi.getType() != Roi.RECTANGLE){
						BehaviorDialog.showErrorDialog(this, "A rectangle ROI is required.");
						return;
					} else {
						flag = true;
						int fieldX = field_roi.getBounds().x;
						int fieldY = field_roi.getBounds().y;
						float fieldWidth = field_roi.getBounds().width;
						int fieldHeight = field_roi.getBounds().height;
						int armR = BMParameter.getarmR();//内側の円の直径
						int innerR = Math.round(armR * (fieldWidth / BMParameter.getFrameWidth()) / 2);
						BMParameter.innerR = innerR;
						BMParameter.outerR = Math.round(BMParameter.getDistOut() * (fieldWidth / BMParameter.getFrameWidth()) / 2);
						int Xc = fieldX + (int)fieldWidth / 2;
						int Yc = fieldY + fieldHeight / 2;
						int X1 = (hole1_roi.getBounds().x + hole1_roi.getBounds().width / 2) - Xc;//１番の穴の円盤の中心に対する相対座標x
						int Y1 = (hole1_roi.getBounds().y + hole1_roi.getBounds().height / 2) - Yc;//１番の穴の円盤の中心に対する相対座標y
						int Xi, Yi;//i番目の穴の円盤の中心に対する相対座標
						Roi roi;
						File file;
						File dir;
						String fileName;
						
						for (int i = 2; i <= 12; i++) {
							fileName = "hole" + i + ".roi";
							file = new File(path+fileName);
							dir = new File(path);
							if (!dir.exists()) {
								dir.mkdirs();
							}
							
							//(X1, Y1)を反時計回りに30*(i-1)度回転
							//Yi,Y1に-を付けているのはフレームのY座標は下が正のため
							Xi = (int) (Math.round(Math.cos((Math.PI / 6) * (i - 1)) * X1) - (Math.sin((Math.PI / 6) * (i - 1)) * (-Y1)));
							Yi = -(int) (Math.round(Math.sin((Math.PI / 6) * (i - 1)) * X1) + (Math.cos((Math.PI / 6) * (i - 1)) * (-Y1)));
							
							roi = new Roi(Xc + Xi - innerR, Yc + Yi - innerR, innerR*2, innerR*2);
							
							try {
								OutputStream output_stream = new FileOutputStream(file);
								RoiEncoder encoder = new RoiEncoder(output_stream);
								encoder.write(roi);
								output_stream.close();
							} catch (Exception exception) {
								exception.printStackTrace();
							}
							
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 現在セットされている ROI　を表示。
	 */
	protected ImageProcessor createRoiImage() throws IOException{
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();
		ip.setColor(Color.red);
		
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String roiName = path + sep + "Field.roi";
		if(new File(roiName).exists()){
		    Roi roi = new RoiDecoder(roiName).getRoi();
		    if (!flag) {
			    BMParameter.innerR = Math.round(BMParameter.getarmR() * ((roi.getBounds().width + 0.0f) / BMParameter.getFrameWidth()) / 2);
			    BMParameter.outerR = Math.round(BMParameter.getDistOut() * ((roi.getBounds().width + 0.0f) / BMParameter.getFrameWidth()) / 2);
		    }
		    Rectangle rec = roi.getPolygon().getBounds();
		    int diameter = (rec.width + rec.height) / 2;
		    OvalRoi ovalField = new OvalRoi(rec.x, rec.y, diameter, diameter);
		    ovalField.drawPixels(ip);
		    String str = "Field";
		    char[] chars = str.toCharArray();
		    ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
		            rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		}
		
		/**
		 * 二重円の表示。
		 */
		path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		int x, y, width, height, x_center, y_center;
		int innerR = BMParameter.innerR;
		int outerR = BMParameter.outerR;
		try {
			for (int i = 1; i <= ALLHOLE; i++) {
				roiName = path + sep + "hole" + i + ".roi";
				if(!new File(roiName).exists())
					continue;
				Roi roi = new RoiDecoder(roiName).getRoi();
				x = roi.getBounds().x;
				y = roi.getBounds().y;
				width = roi.getBounds().width;
				height = roi.getBounds().height;
				x_center = x + width/2;
				y_center = y + height/2;
				OvalRoi inner_oval_roi = new OvalRoi(x_center - innerR, y_center - innerR, innerR*2, innerR*2);
				OvalRoi outer_oval_roi = new OvalRoi(x_center - outerR, y_center - outerR, outerR*2, outerR*2);
				ip.setColor(Color.blue);
				inner_oval_roi.drawPixels(ip);
				outer_oval_roi.drawPixels(ip);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for(int i = 1; i <= ALLHOLE; i++){
			roiName = path + sep + "hole" + i + ".roi";
			if(!new File(roiName).exists())
				continue;
			Roi roi = new RoiDecoder(roiName).getRoi();
			//ip.setColor(Color.red);	//見にくいので四角形のRoiは表示しない。
			//roi.drawPixels(ip);
			Rectangle rec = roi.getPolygon().getBounds();
			String str = Integer.toString(i);
			char[] chars = str.toCharArray();
			ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
					rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		}
		
		
		if (1 <= target && target <= 12 && new File(path + sep + "hole" + target + ".roi").exists()) {
			roiName = path + sep + "hole" + target + ".roi";
			Roi roi = new RoiDecoder(roiName).getRoi();
			//roi.drawPixels(ip);
			Rectangle rec = roi.getPolygon().getBounds();
			String str = "T";
			char[] chars = str.toCharArray();
			ip.setColor(Color.green);
			ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2 - 3,//数字と重ならないように少しずらす 
					rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2 - 3);
		}

		return ip;
	}

	public void load(Properties properties) {
	}
	public void registerParameter(Hashtable table) {
	}
	
	public static int getTarget() {
		return target;
	}
	
	public static void setTargetHole() {
		BMSessionDialogPanel.targetHole[BMAnalyzer.subjectIDCount] = target; 
	}
	
	public static void setTarget(int target) {	//BMSubjectDialogPanelで用いる
		BMSetCageDialogPanel.target = target;
	}
	
	private class OffRoiMovie extends Thread{

		private boolean isRunning;
		private ImagePlus[] image;
		private WindowOperator winOperator;
		private String subjectID;
		private ImageStack stack;
		private ImageProcessor[] backIp;

		public OffRoiMovie(){
			isRunning = true;
			subjectID = manager.getSubjectID()[0];
			backIp = new ImageProcessor[1];
			stack = (new ImageLoader()).loadImage(subjectID);
			backIp[0] = stack.getProcessor(stack.getSize());
			image = new ImagePlus[1];
			image[0] = new ImagePlus(subjectID, backIp[0]);
			winOperator = WindowOperator.getInstance(1, backIp);
			winOperator.setImageWindow(image, WindowOperator.LEFT_UP);
		}

		public void run(){
			while(isRunning){
				try{
					ImageProcessor ip = createOffRoiImage();
					image[0].setProcessor("Live Movie", ip);
					sleep(200);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		public Roi getRoi(){
			return image[0].getRoi();
		}

		public void end(){
			isRunning = false;
			winOperator.closeWindows();
		}
		
		/*protected void writeFieldRoi(Roi roi){
			if(roi != null){
				String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;
				String fileName= "Field.roi";

				File file = new File(path+fileName);
				File dir = new File(path);
				if(!dir.exists()){
					dir.mkdirs();
				}

				try {
					OutputStream output_stream = new FileOutputStream(file);
					RoiEncoder encoder = new RoiEncoder(output_stream);
					encoder.write(roi);
					output_stream.close();
				} catch (Exception exception) {
					exception.printStackTrace();
				}
			}
		}*/
		
		/**
		 * 現在セットされている ROI　を表示(Offline)。
		 * Offlineの場合はField.roiの部分が表示されるのでhole1～12の位置もそれに合わせて表示する。
		 */
		protected ImageProcessor createOffRoiImage() throws IOException{
			ImageProcessor ip = this.backIp[0];
			ip = ip.convertToRGB();
			ip.setColor(Color.red);

			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
			String roiName = path + sep + "Field.roi";
			if(new File(roiName).exists()){
			    Roi roi = new RoiDecoder(roiName).getRoi();
			    //Offlineの場合はここでinnerR,outerRをセット。
			    BMParameter.innerR = Math.round(BMParameter.getarmR() * ((roi.getBounds().width + 0.0f) / BMParameter.getFrameWidth()) / 2);
			    BMParameter.outerR = Math.round(BMParameter.getDistOut() * ((roi.getBounds().width + 0.0f) / BMParameter.getFrameWidth()) / 2);
			    Rectangle fieldRec = roi.getPolygon().getBounds();
			    field = fieldRec;
			    int diameter = (fieldRec.width + fieldRec.height) / 2;
			    OvalRoi ovalField = new OvalRoi(0, 0, diameter, diameter);
			    ovalField.drawPixels(ip);
			    //roi = new Roi(fieldRec.x, fieldRec.y, imageWidth, imageHeight);
			    //writeFieldRoi(roi);
			    ip.drawRect(0, 0, fieldRec.width, fieldRec.height);
			    String str = "Field";
			    char[] chars = str.toCharArray();
			    ip.drawString(str, (fieldRec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			            (fieldRec.height + ip.getFontMetrics().getAscent()) / 2);
			
			    /**
				 * 二重円の表示。
				 */
				path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
				String field_roi_name = path + sep + "Field.roi";
				int x, y, width, height, x_center, y_center, field_x, field_y;
				int innerR = BMParameter.innerR;
				int outerR = BMParameter.outerR;
				try {
					field_x = new RoiDecoder(field_roi_name).getRoi().getBounds().x;
					field_y = new RoiDecoder(field_roi_name).getRoi().getBounds().y;
					for (int i = 1; i <= ALLHOLE; i++) {
						roiName = path + sep + "hole" + i + ".roi";
						if(!new File(roiName).exists())
							continue;
						roi = new RoiDecoder(roiName).getRoi();
						x = roi.getBounds().x - field_x;
						y = roi.getBounds().y - field_y;
						width = roi.getBounds().width;
						height = roi.getBounds().height;
						x_center = x + width/2;
						y_center = y + height/2;
						OvalRoi field_oval_roi = new OvalRoi(x_center - innerR, y_center - innerR, innerR*2, innerR*2);
						OvalRoi outer_oval_roi = new OvalRoi(x_center - outerR, y_center - outerR, outerR*2, outerR*2);
						ip.setColor(Color.blue);
						field_oval_roi.drawPixels(ip);
						outer_oval_roi.drawPixels(ip);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			    
				for(int i = 1; i <= ALLHOLE; i++){
					roiName = path + sep + "hole" + i + ".roi";
					if(!new File(roiName).exists())
						continue;
					roi = new RoiDecoder(roiName).getRoi();
					Rectangle rec = roi.getPolygon().getBounds();
					//ip.setColor(Color.red);	//見にくいので四角形のRoiは表示しない。
					//ip.drawRect(rec.x - fieldRec.x, rec.y - fieldRec.y, rec.width, rec.height);
					str = Integer.toString(i);
					chars = str.toCharArray();
					ip.drawString(str, rec.x -fieldRec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
							rec.y - fieldRec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
				}
				
				/* Offlineでは複数の試行分を一度に解析するのでターゲットの表示は必要ない。
				if (1 <= target && target <= 12 && new File(path + sep + "hole" + target + ".roi").exists()) {
					roiName = path + sep + "hole" + target + ".roi";
					roi = new RoiDecoder(roiName).getRoi();
					Rectangle rec = roi.getPolygon().getBounds();
					ip.drawRect(rec.x - fieldRec.x, rec.y - fieldRec.y, rec.width, rec.height);
					str = "T";
					chars = str.toCharArray();
					ip.setColor(Color.blue);
					ip.drawString(str, rec.x - fieldRec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2 - 3,//数字と重ならないように少しずらす 
							rec.y - fieldRec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2 - 3);
				}
				*/
				
			}

			return ip;
		}
	}

	

}
