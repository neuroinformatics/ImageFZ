package behavior.setup.dialog;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.OvalRoi;
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.MovieManager;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.ImageLoader;
import behavior.setup.parameter.CSIParameter;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.YMParameter;
import behavior.setup.parameter.variable.BTIntVariable;
import behavior.setup.parameter.variable.CSIBooleanVariable;
import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.Variable;

public class CSIOfflineSetCageDialogPanel  extends AbstractDialogPanel implements ActionListener{
	private JTextField _Diameter;
	private JTextField _Hysteresis;
	private JCheckBox _Mask;
	private double _InnerDiameter;
	private double _HysteresisDistance;
	private int _LeftSeparator;
    private int _RightSeparator;
    private final String sep = System.getProperty("file.separator");
    protected JComboBox comboBox;
    private JButton set;
    protected JButton copy;
	private int _FrameWidthPixel;
	private int _FrameHeightPixel;
	private int _FrameWidth;
    private int _FrameHeight;
    private double _VerticalResolution;
    private double _HorizontalResolution;
    private CSISetCageOfflineMovieManager movie;

	public CSIOfflineSetCageDialogPanel(DialogManager manager){
		super(manager);
		createDialog();
		_InnerDiameter = CSIParameter.UNSET;
		_HysteresisDistance = CSIParameter.UNSET;
		_LeftSeparator=CSIParameter.UNSET;
	    _RightSeparator=CSIParameter.UNSET;
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
		comboBox.setPreferredSize(new Dimension(100,25));
		comboBox.addItem("LeftInner");
		comboBox.addItem("RightInner");
		comboBox.addItem("Chamber");
		comboBox.addItem("LeftCageMask");
		comboBox.addItem("RightCageMask");
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
		        _Diameter = new JTextField(_InnerDiameter+"",0);
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
		        _Hysteresis = new JTextField(_HysteresisDistance+"",0);
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

		gbc.gridx = 0;
		gbc.gridy++;
		gbc.gridwidth = 6;
		gbc.anchor = GridBagConstraints.WEST;
		_Mask = new JCheckBox("Use mask", false);
		_Mask.addActionListener(this);
		add(_Mask, gbc);
	}

	public void preprocess(){
		String str[] = manager.getSubjectID();
		movie = new CSISetCageOfflineMovieManager(str[0]);
		movie.start();

	    setFrameSize();

	    //RoiをOvalにセット
	    Toolbar.getInstance().setTool(Toolbar.OVAL);

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

	public synchronized void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			manager.setModifiedParameter(true);
			Roi roi = movie.getRoi();
			writeRoi(roi);
		}else if(e.getSource() == comboBox){
		    if(comboBox.getSelectedItem().toString().equals("MainArea") 
		    		|| comboBox.getSelectedItem().toString().equals("Chamber")){
			    Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
		    }else{
			    Toolbar.getInstance().setTool(Toolbar.OVAL);
		    }
	    }else if(e.getSource() == _Diameter){
			_InnerDiameter = getInnerDiameter();
	        try{
	        	saveInnerRoi("LInner.roi",_InnerDiameter);
		        saveInnerRoi("RInner.roi",_InnerDiameter);
	        }catch(IOException ex){
		       	ex.printStackTrace();
		    }

	        _HysteresisDistance = getHysterisisDistance();
			try{
				saveOuterRoi("LOuter.roi","LInner.roi",_HysteresisDistance);
		        saveOuterRoi("ROuter.roi","RInner.roi",_HysteresisDistance);
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}else if(e.getSource() == _Hysteresis){
	    	manager.setModifiedParameter(true);

	    	_HysteresisDistance = getHysterisisDistance();
			try{
				saveOuterRoi("LOuter.roi","LInner.roi",_HysteresisDistance);
		        saveOuterRoi("ROuter.roi","RInner.roi",_HysteresisDistance);
			}catch(IOException ex){
				ex.printStackTrace();
			}
	    }else if(e.getSource() == copy){
			CSICopyRoiDialog dialog = new CSICopyRoiDialog(manager, true);
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

			    String key3 = properties.getProperty("separator.left","none");
  			    if(!key3.equals("none")){
  			        _LeftSeparator = Integer.parseInt(key3);
  			    }
  			    String key4 = properties.getProperty("separator.right","none");
  			    if(!key4.equals("none")){
  			        _RightSeparator = Integer.parseInt(key4);
  			    }

  			    manager.setModifiedParameter(true);
			}
		}else if(e.getSource() == _Mask){
		    manager.setModifiedParameter(true);
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
		double distance;
		try{
            distance = Double.parseDouble(_Hysteresis.getText());
        }catch(NumberFormatException ex){
	        BehaviorDialog.showErrorDialog(this, "Invalid parameter(HysteresisDistance.");
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

            if(comboBox.getSelectedItem().toString().equals("LeftInner")
	    		        || comboBox.getSelectedItem().toString().equals("RightInner")){

	            String roiFile = null;
		        if(comboBox.getSelectedItem().toString().equals("LeftInner")){
		            roiFile = "LInner.roi";
		        }else if(comboBox.getSelectedItem().toString().equals("RightInner")){
		            roiFile = "RInner.roi";
		        }

		        _InnerDiameter = getInnerDiameter();
		        try{
		    		Rectangle rec = roi.getBounds();;

		            int diameterWidthPixel = (int)Math.round(_InnerDiameter*_HorizontalResolution);
		            int diameterHeightPixel = (int)Math.round(_InnerDiameter*_VerticalResolution);

		            Roi newRoi= new OvalRoi((rec.x+(rec.width/2))-(diameterWidthPixel/2),(rec.y+(rec.height/2))-(diameterHeightPixel/2),diameterWidthPixel,diameterHeightPixel);

		           	saveRoi(newRoi,path+roiFile);
		        }catch(IOException ex){
			       	ex.printStackTrace();
			    }

		        try{
		        	saveInnerRoi("LInner.roi",_InnerDiameter);
			        saveInnerRoi("RInner.roi",_InnerDiameter);
		        }catch(IOException ex){
			       	ex.printStackTrace();
			    }
			}else if(comboBox.getSelectedItem().toString().equals("LeftCageMask")
		    		    || comboBox.getSelectedItem().toString().equals("RightCageMask")){
		        if(roi.getType() != Roi.OVAL){
			        BehaviorDialog.showErrorDialog(this, "Oval ROI required.");
				    return;
			    }

		        try{
		            String mainRoiName = path +sep+ "MainArea.roi";
			        Rectangle mainRec = new RoiDecoder(mainRoiName).getRoi().getBounds();

		            String roiPath = null;
                    if(comboBox.getSelectedItem().toString().equals("LeftCageMask")){
			    	    roiPath = path+"LCageMask.roi";
			        }else if(comboBox.getSelectedItem().toString().equals("RightCageMask")){
			    	    roiPath = path+"RCageMask.roi";
			        }

			        Rectangle rec = roi.getBounds();
			        Roi newRoi= new OvalRoi(rec.x-mainRec.x,rec.y-mainRec.y,rec.width,rec.height);
			        saveRoi(newRoi,roiPath);
		        }catch(IOException ex){
			       	ex.printStackTrace();
			    }

		        if(_InnerDiameter != getInnerDiameter()){
					 _InnerDiameter = getInnerDiameter();
				     try{
				       	 saveInnerRoi("LInner.roi",_InnerDiameter);
				         saveInnerRoi("RInner.roi",_InnerDiameter);
				     }catch(IOException ex){
					  	 ex.printStackTrace();
					 }
				 }
		    }else if(comboBox.getSelectedItem().toString().equals("Chamber")){
				if(roi.getType() != Roi.RECTANGLE){
				    BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				    return;
			    }

				Rectangle mainRec2 = null;
				try{
				    String mainRoiName2 = path +sep+ "MainArea.roi";
				    mainRec2 = new RoiDecoder(mainRoiName2).getRoi().getBounds();
				}catch(IOException ex){
		        	ex.printStackTrace();
		        }

		    	_RightSeparator = roi.getBounds().x+roi.getBounds().width - mainRec2.x;
			    _LeftSeparator = roi.getBounds().x - mainRec2.x;

			    if(_InnerDiameter != getInnerDiameter()){
					 _InnerDiameter = getInnerDiameter();
				     try{
				       	 saveInnerRoi("LInner.roi",_InnerDiameter);
				         saveInnerRoi("RInner.roi",_InnerDiameter);
				     }catch(IOException ex){
					  	 ex.printStackTrace();
					 }
				 }
			}

		    _HysteresisDistance = getHysterisisDistance();
	        try{
	            saveOuterRoi("LOuter.roi","LInner.roi",_HysteresisDistance);
	            saveOuterRoi("ROuter.roi","RInner.roi",_HysteresisDistance);
	        }catch(IOException ex){
	        	ex.printStackTrace();
	        }
		}else{
			 if(_InnerDiameter != getInnerDiameter()){
				 _InnerDiameter = getInnerDiameter();
			     try{
			       	 saveInnerRoi("LInner.roi",_InnerDiameter);
			         saveInnerRoi("RInner.roi",_InnerDiameter);
			     }catch(IOException ex){
				  	 ex.printStackTrace();
				 }
			     _HysteresisDistance = getHysterisisDistance();
			     try{
			    	 saveOuterRoi("LOuter.roi","LInner.roi",_HysteresisDistance);
					 saveOuterRoi("ROuter.roi","RInner.roi",_HysteresisDistance);
			     }catch(IOException ex){
				     ex.printStackTrace();
			     }
			 }
			 if(_HysteresisDistance != getHysterisisDistance()){
			     _HysteresisDistance = getHysterisisDistance();
			     try{
			    	 saveOuterRoi("LOuter.roi","LInner.roi",_HysteresisDistance);
					 saveOuterRoi("ROuter.roi","RInner.roi",_HysteresisDistance);
			     }catch(IOException ex){
				     ex.printStackTrace();
			     }
			 }
		}
	}

	private void setSize(){
	    _FrameWidthPixel = movie.getImageWidth();
        _FrameHeightPixel = movie.getImageHeight();
        setVerticalResolution(_FrameHeightPixel, _FrameHeight);
        setHorizontalResolution(_FrameWidthPixel, _FrameWidth);
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

	protected ImageProcessor createRoiImage(ImageProcessor ip) throws IOException{
		ip = ip.convertToRGB();
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		String roiName2 = path +sep+ "LInner.roi";
		if(new File(roiName2).exists()){
			Roi bufRoi = new RoiDecoder(roiName2).getRoi();
			final Rectangle bufrec = bufRoi.getBounds();
			Roi centerRoi= new OvalRoi(bufrec.x,bufrec.y,bufrec.width,bufrec.height);
			ip.setColor(Color.red);
			centerRoi.drawPixels(ip);

			ip.drawString("L", bufrec.x+(bufrec.width/2),bufrec.y);
		}
		String roiName3 = path +sep+ "RInner.roi";
		if(new File(roiName3).exists()){
			Roi bufRoi = new RoiDecoder(roiName3).getRoi();
			final Rectangle bufrec = bufRoi.getBounds();
			Roi centerRoi= new OvalRoi(bufrec.x,bufrec.y,bufrec.width,bufrec.height);

			ip.setColor(Color.red);
			centerRoi.drawPixels(ip);

			ip.drawString("R", bufrec.x+(bufrec.width/2),bufrec.y);
		}
		String roiName4 = path +sep+ "LOuter.roi";
		if(new File(roiName4).exists()){
			Roi bufRoi = new RoiDecoder(roiName4).getRoi();
			final Rectangle bufrec = bufRoi.getBounds();
			Roi centerRoi= new OvalRoi(bufrec.x,bufrec.y,bufrec.width,bufrec.height);

			ip.setColor(Color.blue);
			centerRoi.drawPixels(ip);

			ip.drawString("L", bufrec.x+(bufrec.width/2),bufrec.y);
		}
		String roiName5 = path +sep+ "ROuter.roi";
		if(new File(roiName5).exists()){
			Roi bufRoi = new RoiDecoder(roiName5).getRoi();
			final Rectangle bufrec = bufRoi.getBounds();
			Roi centerRoi= new OvalRoi(bufrec.x,bufrec.y,bufrec.width,bufrec.height);

			ip.setColor(Color.blue);
			centerRoi.drawPixels(ip);

			ip.drawString("R", bufrec.x+(bufrec.width/2),bufrec.y);
		}
		String roiName6 = path +sep+ "LCageMask.roi";
		if(new File(roiName6).exists()){
			Roi bufRoi = new RoiDecoder(roiName6).getRoi();
			final Rectangle bufrec = bufRoi.getBounds();
			Roi setupRoi= new OvalRoi(bufrec.x,bufrec.y,bufrec.width,bufrec.height);

			ip.setColor(Color.green);
			setupRoi.drawPixels(ip);

			ip.drawString("L", bufrec.x+(bufrec.width/2),bufrec.y);
		}
		String roiName7 = path +sep+ "RCageMask.roi";
		if(new File(roiName7).exists()){
			Roi bufRoi = new RoiDecoder(roiName7).getRoi();
			final Rectangle bufrec = bufRoi.getBounds();
			Roi setupRoi= new OvalRoi(bufrec.x,bufrec.y,bufrec.width,bufrec.height);

			ip.setColor(Color.green);
			setupRoi.drawPixels(ip);

			ip.drawString("R", bufrec.x+(bufrec.width/2),bufrec.y);
		}
		if(_LeftSeparator != CSIParameter.UNSET){
			ip.setColor(Color.orange);
		    ip.drawLine((int)_LeftSeparator, 0,(int)_LeftSeparator, ip.getHeight());
		    String str2 = "Left" + "\n" + "Area";
		    char[] chars2 = str2.toCharArray();
		    ip.drawString(str2, (int)_LeftSeparator-5-ip.getFontMetrics().charsWidth(chars2, 0, chars2.length)/2,
		    		ip.getHeight()/2);
		}

		if(_RightSeparator != CSIParameter.UNSET){
			ip.setColor(Color.orange);
		    ip.drawLine((int)_RightSeparator, 0,(int)_RightSeparator, ip.getHeight());
		    String str3 = "Right" + "\n" + "Area";
		    ip.drawString(str3, (int)_RightSeparator+5,ip.getHeight()/2);
		}

		return ip;
	}

	/**
	 * 指定したパスにRoiを保存する。
	 * @param roi
	 * @param path
	 * @throws IOException
	 */
	public void saveRoi(final Roi roi,final String path)throws IOException{
		OutputStream output_stream = new FileOutputStream(new File(path));
	    RoiEncoder encoder = new RoiEncoder(output_stream);
	    encoder.write(roi);
	    output_stream.close();
	}

	public void load(Properties properties){
    	if(properties != null){
    		try{
  			    String key5 = properties.getProperty("diameter.inner","none");
			    if(!key5.equals("none")){
 			        _InnerDiameter = Double.parseDouble(key5);
			    }
  			    String key6 = properties.getProperty("separator.left","none");
  			    if(!key6 .equals("none")){
  			        _LeftSeparator = Integer.parseInt(key6);
  			    }
  			    String key7 = properties.getProperty("separator.right","none");
  			    if(!key7.equals("none")){
  			        _RightSeparator = Integer.parseInt(key7);
  			    }
  			    String key8 = properties.getProperty("distance.hysteresis","none");
			    if(!key8.equals("none")){
			        _HysteresisDistance = Double.parseDouble(key8);
			    }
			    String key9 = properties.getProperty("mask","none");
			    if(!key9.equals("none")){
			    	_Mask.setSelected(key9.equals("true"));
			    }
    		}catch(NumberFormatException e){
    			e.printStackTrace();
    		}
  	    }
    }

	public boolean canGoNext() {   
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//MainAreaがセットされているか確認
		String mainRoiPath = path +sep+ "MainArea.roi";
		if(!new File(mainRoiPath).exists()){
			BehaviorDialog.showErrorDialog("MainArea is not set.");
			return false;
		}

		String roiName = path + sep + "LInner.roi";
		if(!new File(roiName).exists()){
			BehaviorDialog.showErrorDialog(this, "LInner is not set.");
			return false;
		}

		String roiName2 = path + sep + "RInner.roi";
		if(!new File(roiName2).exists()){
			BehaviorDialog.showErrorDialog(this, "RInner is not set.");
			return false;
		}

		double dis = getHysterisisDistance();;
		try{
		    saveOuterRoi("LOuter.roi","LInner.roi",dis);
		    saveOuterRoi("ROuter.roi","RInner.roi",dis);
		}catch(IOException ex){
			ex.printStackTrace();
		}

		String roiName3 = path + sep + "LOuter.roi";
		String roiName4 = path + sep + "ROuter.roi";
		if(!new File(roiName3).exists() || !new File(roiName4).exists()){
			BehaviorDialog.showErrorDialog(this, "ContactArea is not set.");
			return false;
		}

		if(_LeftSeparator == 0){
			BehaviorDialog.showErrorDialog(this, "LeftArea is not set.");
			return false;
		}

		if(_RightSeparator == 0){
			BehaviorDialog.showErrorDialog(this, "RightArea is not set.");
			return false;
		}

		if(_LeftSeparator>_RightSeparator){
			BehaviorDialog.showErrorDialog(this, "Invalid line.");
		    return false;
		}

	    Parameter parameter = Parameter.getInstance();
	    Variable[] vars = parameter.getVar();
	    for(int i=1; vars[i] != null; i++){
		    try{
		    	if(vars[i] instanceof BTIntVariable){
		            if(vars[i].getName().equals("separator.left")){
			            parameter.getVar(i).setVar(_LeftSeparator);
		            }else if(vars[i].getName().equals("separator.right")){
			            parameter.getVar(i).setVar(_RightSeparator);
		            }
		        }else if(vars[i] instanceof CSIDoubleVariable){
		        	if(vars[i].getName().equals("diameter.inner") &&  getInnerDiameter()>0){
			            parameter.getVar(i).setVar(getInnerDiameter());
		            }else if(vars[i].getName().equals("distance.hysteresis") && getHysterisisDistance()>0){
			            parameter.getVar(i).setVar(getHysterisisDistance());
		            }
	            }else if(vars[i] instanceof CSIBooleanVariable){
	            	if(vars[i].getName().equals("mask")){
			            parameter.getVar(i).setVar(_Mask.isSelected());
		            }
	            }
	        }catch(Exception e) {
		        BehaviorDialog.showErrorDialog(manager, "Invalid input!!");
		        return false;
	        }
	    }
		return true;
	}

	class CSISetCageOfflineMovieManager extends MovieManager{
		private boolean isRunning;

		private String subjectID;
		private ImageStack stack;
		private ImageProcessor[] backIp;
		private WindowOperator winOperator;
		private ImagePlus[] live;
		private int nextSlice;

		public CSISetCageOfflineMovieManager(String subjectID){
			backIp = new ImageProcessor[1];
			live = new ImagePlus[1];

			this.subjectID = subjectID;
			stack = (new ImageLoader()).loadImage(subjectID);
			backIp[0] = stack.getProcessor(stack.getSize());
			live[0] = new ImagePlus(subjectID, backIp[0]);
			nextSlice = 1;

			winOperator = WindowOperator.getInstance(1, backIp);
			winOperator.setImageWindow(live, WindowOperator.LEFT_UP);
		}

		public void run(){
			isRunning = true;

			while(isRunning){
				try{
				if(nextSlice >= stack.getSize())
					nextSlice = 1;

				ImageProcessor ip = createRoiImage(stack.getProcessor(nextSlice));

				live[0].setProcessor(subjectID, ip);

				nextSlice++;

				} catch(Exception e){
					e.printStackTrace();
				}
				try{
					sleep(1000);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		public void end(){
			winOperator.closeWindows();
			isRunning = false;
		}

         public Roi getRoi(){
	         return live[0].getRoi();
         }

         public int getImageHeight(){
 			return backIp[0].getHeight();
 		}

          public int getImageWidth(){
        	  return backIp[0].getWidth();
          }
	}
}