package behavior.setup.dialog;

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
import java.util.Arrays;
//import java.util.logging.*;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.process.ImageProcessor;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.io.RMReferenceManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.RMParameter;
import behavior.setup.parameter.variable.BooleanVariable;
import behavior.setup.parameter.variable.Variable;
import behavior.util.rmconstants.RMConstants;
import behavior.util.rmcontroller.RMController;

public class RMSetCageDialogPanel extends SetCageDialogPanel implements ActionListener{
	private final String sep = System.getProperty("file.separator");
	private String foodArm;
	private boolean[] isFoodArm = new boolean[RMConstants.ARM_NUM];
	//private Logger log = Logger.getLogger("behavior.setup.dialog.RMSetCageDialogPanel");

	public RMSetCageDialogPanel(DialogManager manager) {
		super(manager, 1);
		Arrays.fill(isFoodArm, true);
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
		for(int arm=0;arm<8;arm++){
			comboBox.addItem("Arm"+(arm+1));
		}
		comboBox.addItem("Center");
		comboBox.addActionListener(this);

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
		/*try{
			FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.ReferencesDir)+sep+"executer.txt",102400,1);
			fh.setFormatter(new SimpleFormatter());
		    log.addHandler(fh);
		}catch(Exception e){
			e.printStackTrace();
		}*/
		Toolbar.getInstance().setTool(Toolbar.RECTANGLE);

		if(RMConstants.isReferenceMemoryMode()){
			Arrays.fill(isFoodArm, true);
            try{
		  	    //log.log(Level.INFO,"start");
                foodArm = new RMReferenceManager(FileManager.getInstance().getPath(FileManager.referencePath)).getFoodArmAlignment(RMConstants.getMouseID(), "def");
		        RMConstants.setFoodArmAlignment(foodArm);
		        //log.log(Level.INFO,foodArm+"");
		        int num = 0;
		        for(int i=0;i<RMConstants.ARM_NUM;i++){
		        	if(foodArm.indexOf(""+(i+1)) == -1){
		        		isFoodArm[i] = false;
		        	}else{
		        		num++;
		        	}
		        	//log.log(Level.INFO,isFoodArm[i]+"");
		        }
		        RMConstants.setFoodArmNum(num);
		    }catch(IOException e){
			     //log.log(Level.INFO,e.toString());
                 e.printStackTrace();
            }
		}
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			manager.setModifiedParameter(true);
			super.actionPerformed(e);
		}else if(e.getSource() == copy){
			manager.setModifiedParameter(true);
			new RMCopyRoiDialog(manager, true, allCage);
		}else if(e.getSource() == comboBox){
		    if(comboBox.getSelectedItem().toString().equals("MainArea"))
			    Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
		    else
			    Toolbar.getInstance().setTool(Toolbar.OVAL);
	    }
	}

	protected void writeRoi(Roi roi){
		if(roi != null){
			final String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

			//MainArea
			if(comboBox.getSelectedItem().toString().equals("MainArea")){
			    if(roi.getType() != Roi.RECTANGLE){
			        BehaviorDialog.showErrorDialog(this, "MainArea requires a Rectangle ROI.");
				    return;
			    }

			    final String fileName = "MainArea.roi";
                File file = new File(path +sep+ fileName);
			    File dir = new File(path);
			    if(!dir.exists()){
				    dir.mkdirs();
			    }

			    try {
				    OutputStream output_stream = new FileOutputStream(file);
				    RoiEncoder encoder = new RoiEncoder(output_stream);
				    encoder.write(roi);
				    output_stream.close();
			    }catch (Exception exception){
				    exception.printStackTrace();
			    }
			}else if(comboBox.getSelectedItem().toString().equals("Center")){   //Center
				final String mainRoiPath = path +sep+ "MainArea.roi";
	            File checkFile = new File(mainRoiPath);
			    if(!checkFile.exists()){
			    	BehaviorDialog.showErrorDialog(this, "Set MainArea before.");
				    return;
			    }

			    if(roi.getType() != Roi.OVAL){
				    BehaviorDialog.showErrorDialog(this, "Center requires a Oval ROI.");
				    return;
				}

				final String fileName = "Center.roi";
	            File file = new File(path +sep+ fileName);
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
				Roi centerRoi= new OvalRoi(rec.x-mainRec.x,rec.y-mainRec.y,rec.width,rec.height);
				try {
				    OutputStream output_stream = new FileOutputStream(file);
				    RoiEncoder encoder = new RoiEncoder(output_stream);
				    encoder.write(centerRoi);
				    output_stream.close();
				}catch (Exception exception) {
				    exception.printStackTrace();
				}
			}else{ //Arm
				final String mainRoiPath = path +sep+ "MainArea.roi";
	            File checkFile = new File(mainRoiPath);
			    if(!checkFile.exists()){
			    	BehaviorDialog.showErrorDialog(this, "Set MainArea before.");
				    return;
			    }

			    if(roi.getType() != Roi.OVAL){
			        BehaviorDialog.showErrorDialog(this, "Arms require Oval ROIs.");
				    return;
			    }

			    Roi mainRoi = null;
				try{
				    mainRoi = new RoiDecoder(mainRoiPath).getRoi();
				}catch(Exception e){
					e.printStackTrace();
				}
				final Rectangle mainRec = mainRoi.getBounds();

				final Rectangle rec = roi.getBounds();
			    final String selectedName = comboBox.getSelectedItem().toString();
			    for(int i=0;i<RMConstants.ARM_NUM;i++){
			        if(selectedName.indexOf(""+(i+1))!=-1){
			            final String fileName = selectedName + ".roi";
	                    File file = new File(path +sep+ fileName);
			            File dir = new File(path);
			            if(!dir.exists()){
					        dir.mkdirs();
				        }

			            Roi armRoi= new OvalRoi(rec.x-mainRec.x,rec.y-mainRec.y,rec.width,rec.height);
				        try{
					        OutputStream output_stream = new FileOutputStream(file);
					        RoiEncoder encoder = new RoiEncoder(output_stream);
					        encoder.write(armRoi);
					        output_stream.close();
				        }catch(Exception exception){
					        exception.printStackTrace();
				        }
				        break;
				    }
				}
			}
		}
	}

	//映像にRoiを描く
	protected ImageProcessor createRoiImage() throws IOException{
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();

		final String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//MainAreaを描く
		String roiName = path +sep+ "MainArea.roi";
		if(new File(roiName).exists()){
		    ip.setColor(Color.orange);
	        Roi mainRoi = new RoiDecoder(roiName).getRoi();
		    mainRoi.drawPixels(ip);

		    final Rectangle mainRec = mainRoi.getBounds();

		    //Centerを描く
			String roiName2 = path +sep+ "Center.roi";
			if(new File(roiName2).exists()){
				Roi bufRoi = new RoiDecoder(roiName2).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.blue);
				centerRoi.drawPixels(ip);
				Rectangle rec = centerRoi.getPolygon().getBounds();
			    String str = "Center";
			    char[] chars = str.toCharArray();
			    ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			                    	rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			 }

			 for(int i=0;i<RMConstants.ARM_NUM;i++){
			     String roiName3 = path +sep+ "Arm" + (i+1) + ".roi";
			     if(new File(roiName3).exists()){
			    	 Roi bufRoi = new RoiDecoder(roiName3).getRoi();
					 final Rectangle bufrec = bufRoi.getBounds();
					 Roi armRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

					 if(isFoodArm[i])
						 ip.setColor(Color.red);
					 else
				         ip.setColor(Color.green);
				     armRoi.drawPixels(ip);
				     Rectangle rec = armRoi.getPolygon().getBounds();
			         String str = (i+1) +"";
			         char[] chars = str.toCharArray();
			         ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			                    	rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			    }
			}
		}

		return ip;
	}

	//次のステップに進んでよいか
	public boolean canGoNext(){
		final String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//MainAreaがセットされているか確認
		String mainRoiPath = path +sep+ "MainArea.roi";
		if(!new File(mainRoiPath).exists()){
			BehaviorDialog.showErrorDialog("MainArea is not set.");
			return false;
		}

		//Centerがセットされているか確認
		String centerRoiPath = path +sep+ "Center.roi";
		File centerFile = new File(centerRoiPath);
		if(!centerFile.exists()){
			BehaviorDialog.showErrorDialog("Center is not set.");
			return false;
		}

		//Armがセットされているか確認
		for(int i=0;i<RMConstants.ARM_NUM;i++){
			String armRoiPath = path + sep + "Arm"+(i+1)+".roi";
			File armFile = new File(armRoiPath);
			if(!armFile.exists()){
				BehaviorDialog.showErrorDialog("Arm" + (i+1) + "is not set.");
				return false;
			}
		}

	    if(!RMConstants.DEBUG){
	        //よくないが、他にいい方法が思いつかない
	        try{
		        RMController.getInstance();
	        }catch(Throwable e){
	            BehaviorDialog.showErrorDialog("Please make sure labjack is set properly");
	            e.printStackTrace();
	            System.exit(0);
	        }

	        //ごり押し、現状ではどうにもならない
	        Variable[] var = Parameter.getInstance().getVar();
	        if(!((BooleanVariable)var[RMParameter.NSense]).getVariable()){
	            for(int i=0;i<RMConstants.ARM_NUM;i++){
	        	    try{
	        		    boolean b;
	        	        b = RMController.getInstance().isFoodExist(i);
	        	        if(isFoodArm[i] != b){
	        	    	    if(isFoodArm[i]){
	        	    	        BehaviorDialog.showErrorDialog("Food is NOT found!!(in Arm"+ (i+1) +")");
	        	    	    }else{
	        	    		    BehaviorDialog.showErrorDialog("Food is NOT require!!(in Arm"+ (i+1)+")");
	        	    	    }

					        return false;
	        	        }

	        	        Thread.sleep(200);
	        	    }catch(Exception e){
	        		    e.printStackTrace();
	        	    }
	            }
	        }
	    }

		return true;
	}
}