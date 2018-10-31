package behavior.setup.dialog;

import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.RoiDecoder;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
//import java.util.logging.*;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.YMParameter;
import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.Variable;

public class YMSetCageDialogPanel extends SetCageDialogPanel implements ActionListener{
	private JTextField _Diameter;
	private JTextField _Hysteresis;
	private double _InnerDiameter;
	private double _HysteresisDistance;
    private final String sep = System.getProperty("file.separator");
    protected JComboBox comboBox;
	private int _FrameWidthPixel;
	private int _FrameHeightPixel;
	private int _FrameWidth;
    private int _FrameHeight;
    private double _VerticalResolution;
    private double _HorizontalResolution;
    private JCheckBox ref;
    private final double TAN15 = Math.tan(Math.toRadians(15));
    private final double TAN75 = Math.tan(Math.toRadians(75));

	public YMSetCageDialogPanel(DialogManager manager){
		super(manager,1);
		_InnerDiameter = 48.5;
		_HysteresisDistance = 2.0;
	}

	/**
	 * ダイアログのタイトル。
	 * @see behavior.setup.dialog.AbstractDialogPanel#getDialogName()
	 */
	public String getDialogName(){
		return "Set Cage Field";
	}
	
	protected void createDialog(){
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 20, 10, 0);
		gbc.fill = GridBagConstraints.NONE;

		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.WEST;
		add(new JLabel("RoiType: "), gbc);

		comboBox = new JComboBox();
		comboBox.setPreferredSize(new Dimension(80,18));
		comboBox.addItem("MainArea");
		comboBox.addItem("Center");
		comboBox.addItem("Arm1");
		comboBox.addItem("Arm2");
		comboBox.addItem("Arm3");
		comboBox.addActionListener(this);
		gbc.gridx = 3;
		add(comboBox, gbc);

		for(int i=0;i<2;i++){
		    gbc.gridx = 0;
		    gbc.gridy++;
			gbc.gridwidth = 4;
		    if(i==0){
		        add(new JLabel("InnerDiameter(cm):"),gbc);
		        gbc.gridx = 4;
		        gbc.gridwidth = 2;
		        _Diameter = new JTextField("",0);
		        _Diameter.setPreferredSize(new Dimension(30,18));
		        _Diameter.setHorizontalAlignment(JTextField.RIGHT);
		        _Diameter.setEditable(true);
		        _Diameter.setBackground(Color.white);
		        _Diameter.addActionListener(this);
		        add(_Diameter, gbc);
		    }else{
		    	add(new JLabel("DistanceHysteresis(cm):"),gbc);
		        gbc.gridx = 4;
		        gbc.gridwidth = 2;
		        _Hysteresis = new JTextField("",0);
		        _Hysteresis.setPreferredSize(new Dimension(30,18));
		        _Hysteresis.setHorizontalAlignment(JTextField.RIGHT);
		        _Hysteresis.setEditable(true);
		        _Hysteresis.setBackground(Color.white);
		        _Hysteresis.addActionListener(this);
		        add(_Hysteresis, gbc);
		    }
		}

		set = new ExtendedJButton("Set");
		set.addActionListener(this);

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.EAST;
		add(set, gbc);
		gbc.gridx = 3;
		gbc.anchor = GridBagConstraints.WEST;
		copy = new JButton("Import");
		copy.addActionListener(this);
		add(copy, gbc);

		ref = new JCheckBox("Show reference line");
		ref.addActionListener(this);
		gbc.gridy ++;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		add(ref,gbc);
	}

	public void preprocess(){
		super.preprocess();
	    //設定したパラメータを取得
	    setFrameSize();

	    //RoiをOvalにセット
	    Toolbar.getInstance().setTool(Toolbar.RECTANGLE);

	    setSize();

	    _Diameter.setText(""+_InnerDiameter);
        _Hysteresis.setText(""+_HysteresisDistance);
	}

	public void postprocess(){
		comboBox.requestFocus();
	}

	private void setVerticalResolution(final int verticalPixels, final int imageVerticalPlainScale){
		_VerticalResolution = (double)verticalPixels/imageVerticalPlainScale;
	}

	private void setHorizontalResolution(final int horizontalPixels, final int imageHorizontalPlainScale){
		_HorizontalResolution  = (double)horizontalPixels/imageHorizontalPlainScale;
	}
	
	private void setFrameSize(){
		Variable[] var = Parameter.getInstance().getVar();
		_FrameWidth = ((IntVariable)var[Parameter.frameWidth]).getVariable();
		_FrameHeight = ((IntVariable)var[Parameter.frameHeight]).getVariable();
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == set){
			manager.setModifiedParameter(true);
			super.actionPerformed(e);
		}else if(e.getSource() == comboBox){
		    if(comboBox.getSelectedItem().toString().equals("MainArea")){
			    Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
		    }else{
			    Toolbar.getInstance().setTool(Toolbar.OVAL);
		    }
		}else if(e.getSource() == _Diameter){
			if(!isMainAreaExist(true)) return;

			_InnerDiameter = getInnerDiameter();
	        try{
	        	saveInnerRoi("Arm1.roi",_InnerDiameter);
	        	saveInnerRoi("Arm2.roi",_InnerDiameter);
	        	saveInnerRoi("Arm3.roi",_InnerDiameter);
	        }catch(IOException ex){
		       	ex.printStackTrace();
		    }

	        _HysteresisDistance = getHysterisisDistance();
			try{
			    saveOuterRoi("Arm1Outer.roi","Arm1.roi",_HysteresisDistance);
			    saveOuterRoi("Arm2Outer.roi","Arm2.roi",_HysteresisDistance);
			    saveOuterRoi("Arm3Outer.roi","Arm3.roi",_HysteresisDistance);
			}catch(IOException ex){
				ex.printStackTrace();
			}
	    }else if(e.getSource() == _Hysteresis){
	    	if(!isMainAreaExist(true)) return;

	    	manager.setModifiedParameter(true);

	    	_HysteresisDistance = getHysterisisDistance();
			try{
			    saveOuterRoi("Arm1Outer.roi","Arm1.roi",_HysteresisDistance);
			    saveOuterRoi("Arm2Outer.roi","Arm2.roi",_HysteresisDistance);
			    saveOuterRoi("Arm3Outer.roi","Arm3.roi",_HysteresisDistance);
			}catch(IOException ex){
				ex.printStackTrace();
			}
	    }else if(e.getSource() == copy){
			YMCopyRoiDialog dialog = new YMCopyRoiDialog(manager, true);
			if(!dialog.getSelectedPath().equals("")){
			    Properties properties = new Properties();
			    try{
				    File file = new File(dialog.getSelectedPath()+File.separator+"PJ_Prefs.properties");
				    if(file.exists()){
					    properties.load(new FileInputStream(file));
				    }else{
				    	manager.setModifiedParameter(true);
				    	return;
				    }
			    }catch(FileNotFoundException ex){
				    ex.printStackTrace();
			    }catch(IOException ex){
				    ex.printStackTrace();
			    }
			    String key1 = properties.getProperty("diameter.inner","none");
			    if(!key1.equals("none")){
 			        _InnerDiameter = Double.parseDouble(key1);
			    }else{
			    	_InnerDiameter = YMParameter.UNSET;
			    }
			    _Diameter.setText(""+_InnerDiameter);
    			String key2 = properties.getProperty("distance.hysteresis","none");
			    if(!key1.equals("none")){
			        _HysteresisDistance = Double.parseDouble(key2);
			    }else{
			    	_HysteresisDistance = YMParameter.UNSET;
			    }
			    _Hysteresis.setText(""+_HysteresisDistance);
			    manager.setModifiedParameter(true);
			}
		}else if(e.getSource() == ref){
			if(ref.isSelected()){
				comboBox.setEnabled(false);
				set.setEnabled(false);
				copy.setEnabled(false);
				_Hysteresis.setEnabled(false);
				_Diameter.setEnabled(false);
			}else{
				comboBox.setEnabled(true);
				set.setEnabled(true);
				copy.setEnabled(true);
				_Hysteresis.setEnabled(true);
				_Diameter.setEnabled(true);
			}
		}
	}

	private double getInnerDiameter(){
		double diameter;
		try{
            diameter = Double.parseDouble(_Diameter.getText());
        }catch(NumberFormatException ex){
	        BehaviorDialog.showErrorDialog(this, "Invalid parameter(InnerDiameter).");
            return 0;
	    }

        if(diameter<0){
            return 0;
        }
        return diameter;
	}

	private double getHysterisisDistance(){
		double distance=0;
		try{
            distance = Double.parseDouble(_Hysteresis.getText());
        }catch(NumberFormatException ex){
	        BehaviorDialog.showErrorDialog(this, "Invalid parameter(HysteresisDistance).");
            return 0;
	    }

        if(distance<0){
            return 0;
        }
        return distance;
	}

	protected void writeRoi(Roi roi){
		if(roi != null){
			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;

			File dir = new File(path);
	    	if(!dir.exists()){
		        dir.mkdirs();
	        }

	    	if(comboBox.getSelectedItem().toString().equals("MainArea")){   //MainArea
	    		if(roi.getType() != Roi.RECTANGLE){
				    BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				    return;
			    }

				try{
					saveRoi(roi, path+"MainArea.roi");
				}catch (Exception exception) {
				   exception.printStackTrace();
				}
				setSize();

				_InnerDiameter = getInnerDiameter();
		        try{
		        	saveInnerRoi("Arm1.roi",_InnerDiameter);
		        	saveInnerRoi("Arm2.roi",_InnerDiameter);
		        	saveInnerRoi("Arm3.roi",_InnerDiameter);
		        }catch(IOException ex){
			       	ex.printStackTrace();
			    }
	    	}else if(comboBox.getSelectedItem().toString().equals("Center")){
	    		if(roi.getType() != Roi.OVAL){
				    BehaviorDialog.showErrorDialog(this, "Oval ROI required.");
				    return;
			    }

		        if(!isMainAreaExist(true)) return;

		        try{
		    		String mainRoiName = path +sep+ "MainArea.roi";
		            Rectangle mainRec = new RoiDecoder(mainRoiName).getRoi().getBounds();

		            Rectangle rec = roi.getBounds();
			        Roi centerRoi= new OvalRoi(rec.x-mainRec.x,rec.y-mainRec.y,rec.width,rec.height);
			        saveRoi(centerRoi,path +sep+ "Center.roi");
			    }catch(Exception e){
		        	e.printStackTrace();
		        }		
	    	}else{
	    		if(roi.getType() != Roi.OVAL){
				    BehaviorDialog.showErrorDialog(this, "Oval ROI required.");
				    return;
			    }

		        if(!isMainAreaExist(true)) return;

	            String roiFile = null;
		        if(comboBox.getSelectedItem().toString().equals("Arm1")){
		            roiFile = "Arm1.roi";
		        }else if(comboBox.getSelectedItem().toString().equals("Arm2")){
		            roiFile = "Arm2.roi";
		        }else if(comboBox.getSelectedItem().toString().equals("Arm3")){
		            roiFile = "Arm3.roi";
		        }

		        _InnerDiameter = getInnerDiameter();
		        try{
		    		String mainRoiName = path +sep+ "MainArea.roi";
		            Rectangle mainRec = new RoiDecoder(mainRoiName).getRoi().getBounds();
		    		Rectangle rec = roi.getBounds();;

		            int diameterWidthPixel = (int)Math.round(_InnerDiameter*_HorizontalResolution);
		            int diameterHeightPixel = (int)Math.round(_InnerDiameter*_VerticalResolution);

		            Roi newRoi= new OvalRoi((rec.x+(rec.width/2))-(diameterWidthPixel/2)-mainRec.x,(rec.y+(rec.height/2))-(diameterHeightPixel/2)-mainRec.y,diameterWidthPixel,diameterHeightPixel);

		           	saveRoi(newRoi,path+roiFile);
		        }catch(IOException ex){
			       	ex.printStackTrace();
			    }

		        try{
			       	 saveInnerRoi("Arm1.roi",_InnerDiameter);
			         saveInnerRoi("Arm2.roi",_InnerDiameter);
			         saveInnerRoi("Arm3.roi",_InnerDiameter);
			     }catch(IOException ex){
				  	 ex.printStackTrace();
				 }
			}

	    	_HysteresisDistance = getHysterisisDistance();
	        try{
	        	saveOuterRoi("Arm1Outer.roi","Arm1.roi",_HysteresisDistance);
			    saveOuterRoi("Arm2Outer.roi","Arm2.roi",_HysteresisDistance);
			    saveOuterRoi("Arm3Outer.roi","Arm3.roi",_HysteresisDistance);
	        }catch(IOException ex){
	        	ex.printStackTrace();
	    	}
		}else{
			 if(_InnerDiameter != getInnerDiameter()){
				 _InnerDiameter = getInnerDiameter();
			     try{
			       	 saveInnerRoi("Arm1.roi",_InnerDiameter);
			         saveInnerRoi("Arm2.roi",_InnerDiameter);
			         saveInnerRoi("Arm3.roi",_InnerDiameter);
			     }catch(IOException ex){
				  	 ex.printStackTrace();
				 }
			     _HysteresisDistance = getHysterisisDistance();
			     try{
			    	 saveOuterRoi("Arm1Outer.roi","Arm1.roi",_HysteresisDistance);
					 saveOuterRoi("Arm2Outer.roi","Arm2.roi",_HysteresisDistance);
					 saveOuterRoi("Arm3Outer.roi","Arm3.roi",_HysteresisDistance);
			     }catch(IOException ex){
				     ex.printStackTrace();
			     }
			 }
			 if(_HysteresisDistance != getHysterisisDistance()){
			     _HysteresisDistance = getHysterisisDistance();
			     try{
			    	 saveOuterRoi("Arm1Outer.roi","Arm1.roi",_HysteresisDistance);
					 saveOuterRoi("Arm2Outer.roi","Arm2.roi",_HysteresisDistance);
					 saveOuterRoi("Arm3Outer.roi","Arm3.roi",_HysteresisDistance);
			     }catch(IOException ex){
				     ex.printStackTrace();
			     }
			 }
		}
	}

	private void setSize(){
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
	    String mainRoiPath = path +sep+ "MainArea.roi";
	    if(!new File(mainRoiPath).exists()) return;

	    try{
	        Rectangle mainRec = new RoiDecoder(mainRoiPath).getRoi().getBounds();
		    _FrameWidthPixel = mainRec.width;
	        _FrameHeightPixel = mainRec.height;
	        setVerticalResolution(_FrameHeightPixel, _FrameHeight);
	        setHorizontalResolution(_FrameWidthPixel, _FrameWidth);
	    }catch(IOException ex){
        	ex.printStackTrace();
        }
	}

	private void saveInnerRoi(final String innerRoiFile,final double diameter) throws IOException{
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;
		if(!new File(path+innerRoiFile).exists()){
            return;
		}
		Rectangle rec = new RoiDecoder(path+innerRoiFile).getRoi().getBounds();;

        int diameterWidthPixel = (int)Math.round(diameter*_HorizontalResolution);
        int diameterHeightPixel = (int)Math.round(diameter*_VerticalResolution);

        Roi newRoi= new OvalRoi((rec.x+(rec.width/2))-(diameterWidthPixel/2),(rec.y+(rec.height/2))-(diameterHeightPixel/2),diameterWidthPixel,diameterHeightPixel);

       	saveRoi(newRoi,path+innerRoiFile);
	}

	/**
	 * 元のRoiから算出したRoiをパスに保存する。
	 * @param path
	 * @param cageFilePath
	 * @param contactDistance
	 * @throws IOException
	 */
	private void saveOuterRoi(final String file, final String cageFile,final double contactDistance)throws IOException{
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;
		if(!new File(path+cageFile).exists()){
            return;
		}

		Roi cageRoi = new RoiDecoder(path+cageFile).getRoi();

        int contactDistanceWidthPixel = (int)Math.round(contactDistance*_HorizontalResolution);
        int contactDistanceHeightPixel = (int)Math.round(contactDistance*_VerticalResolution);

        Roi contactRoi = new OvalRoi(cageRoi.getBounds().x-contactDistanceWidthPixel, cageRoi.getBounds().y-contactDistanceHeightPixel,
        		cageRoi.getBounds().width+contactDistanceWidthPixel*2,cageRoi.getBounds().height+contactDistanceHeightPixel*2);

       	saveRoi(contactRoi,path+file);
	}

	protected ImageProcessor createRoiImage() throws IOException{
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//MainAreaを描く
		String roiName = path +sep+ "MainArea.roi";
		if(new File(roiName).exists() && !ref.isSelected()){
		    ip.setColor(Color.orange);
	        Roi mainRoi = new RoiDecoder(roiName).getRoi();
		    mainRoi.drawPixels(ip);

		    final Rectangle mainRec = mainRoi.getBounds();

		    String roiNameC = path +sep+ "Center.Roi";
		    if(new File(roiNameC).exists()){
				Roi bufRoi = new RoiDecoder(roiNameC).getRoi();
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

			String roiName2 = path +sep+ "Arm1.roi";
			if(new File(roiName2).exists()){
				Roi bufRoi = new RoiDecoder(roiName2).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.red);
				centerRoi.drawPixels(ip);
				Rectangle rec = centerRoi.getPolygon().getBounds();
			    String str = "Arm1";
			    char[] chars = str.toCharArray();
			    ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			    		rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			}
			String roiName3 = path +sep+ "Arm2.roi";
			if(new File(roiName3).exists()){
				Roi bufRoi = new RoiDecoder(roiName3).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.red);
				centerRoi.drawPixels(ip);
				Rectangle rec = centerRoi.getPolygon().getBounds();
			    String str = "Arm2";
			    char[] chars = str.toCharArray();
			    ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			    		rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			}
			String roiName4 = path +sep+ "Arm3.roi";
			if(new File(roiName4).exists()){
				Roi bufRoi = new RoiDecoder(roiName4).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.red);
				centerRoi.drawPixels(ip);
				Rectangle rec = centerRoi.getPolygon().getBounds();
			    String str = "Arm3";
			    char[] chars = str.toCharArray();
			    ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			    		rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
			}
			String roiName5 = path +sep+ "Arm1Outer.roi";
			if(new File(roiName5).exists()){
				Roi bufRoi = new RoiDecoder(roiName5).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.blue);
				centerRoi.drawPixels(ip);
			}
			String roiName6 = path +sep+ "Arm2Outer.roi";
			if(new File(roiName6).exists()){
				Roi bufRoi = new RoiDecoder(roiName6).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.blue);
				centerRoi.drawPixels(ip);
			}
			String roiName7 = path +sep+ "Arm3Outer.roi";
			if(new File(roiName7).exists()){
				Roi bufRoi = new RoiDecoder(roiName7).getRoi();
				final Rectangle bufrec = bufRoi.getBounds();
				Roi centerRoi= new OvalRoi(bufrec.x+mainRec.x,bufrec.y+mainRec.y,bufrec.width,bufrec.height);

				ip.setColor(Color.blue);
				centerRoi.drawPixels(ip);
			}
		}

		if(ref.isSelected()){
			ip.setColor(Color.green);
			double width = ip.getWidth();
			double height = ip.getHeight();
			ip.drawLine((int)Math.round(width/2), (int)Math.round(height/2), (int)Math.round((width+height)/2), 0);
			ip.drawLine((int)Math.round(width/2), (int)Math.round(height/2), (int)Math.round((width/2)-(height/(2*TAN15))), 0);
			ip.drawLine((int)Math.round(width/2), (int)Math.round(height/2), (int)Math.round((width/2)+(height/(2*TAN75))), (int)Math.round(height));
		}

		return ip;
	}

	public void load(Properties properties){
    	if(properties != null){
    		try{
    			String key = properties.getProperty("diameter.inner","none");
			    if(!key.equals("none")){
 			        _InnerDiameter = Double.parseDouble(key);
			    }
    			String key1 = properties.getProperty("distance.hysteresis","none");
			    if(!key1.equals("none")){
			        _HysteresisDistance = Double.parseDouble(key1);
			    }
    		}catch(NumberFormatException e){
    			e.printStackTrace();
    		}
  	    }
    }

	public boolean canGoNext(){
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//MainAreaがセットされているか確認
		String mainRoiPath = path +sep+ "MainArea.roi";
		if(!new File(mainRoiPath).exists()){
			BehaviorDialog.showErrorDialog("MainArea is not set.");
			return false;
		}

		String roiNameC = path + sep + "Center.roi";
		if(!new File(roiNameC).exists()){
			BehaviorDialog.showErrorDialog(this, "Center is not set.");
			return false;
		}

		for(int i=0;i<3;i++){
			String roiName = path + sep + "Arm"+(i+1)+".roi";
			if(!new File(roiName).exists()){
				BehaviorDialog.showErrorDialog(this, "Arm"+(i+1)+" is not set.");
				return false;
			}

			String roiName2 = path +sep+ "Arm"+(i+1)+"Outer.roi";
			if(!new File(roiName2).exists()){
				BehaviorDialog.showErrorDialog(this, "Arm"+(i+1)+"Outer"+" is not set.");
				return false;
			}
		}

	    Parameter parameter = Parameter.getInstance();
	    Variable[] vars = parameter.getVar();
	    for(int i=1; vars[i] != null; i++){
		    try{
		        if(vars[i] instanceof CSIDoubleVariable){
		        	if(vars[i].getName().equals("diameter.inner") &&  getInnerDiameter()>0){
			            parameter.getVar(i).setVar(getInnerDiameter());
		            }else if(vars[i].getName().equals("distance.hysteresis") && getHysterisisDistance()>0){
			            parameter.getVar(i).setVar(getHysterisisDistance());
		            }
	            }
	        }catch(Exception e) {
		        BehaviorDialog.showErrorDialog(manager, "Invalid input!!");
		        return false;
	        }
	    }
		
	    return true;
	}

	private boolean isMainAreaExist(boolean message){
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		String mainRoiPath = path +sep+ "MainArea.roi";
        File checkFile = new File(mainRoiPath);
	    if(!checkFile.exists()){
	    	if(message)
	    	    BehaviorDialog.showErrorDialog(this, "Set MainArea before.");
		    return false;
	    }
	    return true;
	}
}