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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.MovieManager;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.ImageLoader;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.YMParameter;
import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.Variable;

public class YMOfflineSetCageDialogPanel extends AbstractDialogPanel implements ActionListener{
	protected JComboBox comboBox;
	protected JButton set;
	protected JButton copy;
	private JTextField _Diameter;
	private JTextField _Hysteresis;
	private double _InnerDiameter;
	private double _HysteresisDistance;
	private int _FrameWidthPixel;
	private int _FrameHeightPixel;
	private int _FrameWidth;
    private int _FrameHeight;
    private double _VerticalResolution;
    private double _HorizontalResolution;

    private final String sep = System.getProperty("file.separator");

	private YMSetCageOfflineMovieManager _Movie;

	//private Logger log = Logger.getLogger("behavior.setup.dialog.CSIOldOfflineSetCageDialogPanel");
	/**
	 * ダイアログのタイトル。
	 * @see behavior.setup.dialog.AbstractDialogPanel#getDialogName()
	 */
	public String getDialogName(){
		return "Set Cage Field";
	}

	/**
	 * コンストラクタ。
	 */
	public YMOfflineSetCageDialogPanel(DialogManager manager){
		super(manager);
		_InnerDiameter = YMParameter.UNSET;
		_HysteresisDistance = YMParameter.UNSET;
	    createDialog();
	}

	/**
	 * ダイアログの表示部分。
	 */
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
	}

	/**
	 * 表示直前に呼び出す処理。
	 * @see behavior.setup.dialog.AbstractDialogPanel#preprocess()
	 */
	public void preprocess(){
       /*try{
		    FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.program) + System.getProperty("file.separator") + "Log.txt", 102400,1);
		    fh.setFormatter(new SimpleFormatter());
	        log.addHandler(fh);
	    }catch(Exception e){
		e.printStackTrace();
	    }*/
		//設定したパラメータを取得
		setFrameSize();

		//RoiをOvalにセット
		Toolbar.getInstance().setTool(Toolbar.OVAL);

		String str[] = manager.getSubjectID();
		_Movie = new YMSetCageOfflineMovieManager(str[0]);

		_FrameWidthPixel = _Movie.getImageWidthSize();
		_FrameHeightPixel = _Movie.getImageHeightSize();
		setVerticalResolution(_FrameHeightPixel, _FrameHeight);
		setHorizontalResolution(_FrameWidthPixel, _FrameWidth);
		//log.log(Level.INFO,""+_FrameHeight+_FrameWidth+_InnerDiameter);
		//log.log(Level.INFO,""+_Movie.getImageWidthSize()+" "+_Movie.getImageHeightSize());
	    _Movie.start();

	    _Diameter.setText(""+_InnerDiameter);
        _Hysteresis.setText(""+_HysteresisDistance);
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

	/**
	 * 表示直後に呼び出す処理。
	 * @see behavior.setup.dialog.AbstractDialogPanel#postprocess()
	 */
	public void postprocess(){
		comboBox.requestFocus();
	}

	/**
	 * 表示後に呼び出す処理。
	 * @see behavior.setup.dialog.AbstractDialogPanel#endprocess()
	 */
	public void endprocess(){
		_Movie.end();
	}

	public void actionPerformed(ActionEvent e){
		if(e.getSource() == set){
			manager.setModifiedParameter(true);
			Roi roi = _Movie.getRoi();
			writeRoi(roi);
		}else if(e.getSource() == comboBox){
			Toolbar.getInstance().setTool(Toolbar.OVAL);
		}else if(e.getSource() == _Diameter){
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

    		if(roi.getType() != Roi.OVAL){
			    BehaviorDialog.showErrorDialog(this, "Oval ROI required.");
			    return;
		    }

    		if(comboBox.getSelectedItem().toString().equals("Center")){
    			Rectangle rec = roi.getBounds();
    			try{
    			    saveRoi(new OvalRoi(rec.x,rec.y,rec.width,rec.height),path+"Center.roi");
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}else{
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
	    		    Rectangle rec = roi.getBounds();

	                int diameterWidthPixel = (int)Math.round(_InnerDiameter*_HorizontalResolution);
	                int diameterHeightPixel = (int)Math.round(_InnerDiameter*_VerticalResolution);

	                Roi newRoi= new OvalRoi((rec.x+(rec.width/2))-(diameterWidthPixel/2),(rec.y+(rec.height/2))-(diameterHeightPixel/2),diameterWidthPixel,diameterHeightPixel);

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

	            _HysteresisDistance = getHysterisisDistance();
	            try{
	        	    saveOuterRoi("Arm1Outer.roi","Arm1.roi",_HysteresisDistance);
			        saveOuterRoi("Arm2Outer.roi","Arm2.roi",_HysteresisDistance);
			        saveOuterRoi("Arm3Outer.roi","Arm3.roi",_HysteresisDistance);
	            }catch(IOException ex){
	        	    ex.printStackTrace();
	    	    }
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

	/**
	 * 指定したパスにRoiを保存する。
	 * @param roi
	 * @param path
	 * @throws IOException
	 */
	private void saveRoi(final Roi roi,final String path)throws IOException{
		OutputStream output_stream = new FileOutputStream(new File(path));
	    RoiEncoder encoder = new RoiEncoder(output_stream);
	    encoder.write(roi);
	    output_stream.close();
	}

	private void saveInnerRoi(final String innerRoiFile,final double diameter)throws IOException{
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

    protected ImageProcessor createRoiImage(ImageProcessor ip)throws IOException{
		ip = ip.convertToRGB();
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir)+sep;

		String roiNameC = path+ "Center.roi";
	    if(new File(roiNameC).exists()){
		    ip.setColor(Color.blue);
	        Roi roiC = new RoiDecoder(roiNameC).getRoi();
		    roiC.drawPixels(ip);
		    Rectangle recC = roiC.getPolygon().getBounds();
		    String strC = "center";
		    char[] charsC = strC.toCharArray();
		    ip.drawString(strC, recC.x + (recC.width - ip.getFontMetrics().charsWidth(charsC, 0, charsC.length)) / 2, 
		   		    recC.y + (recC.height + ip.getFontMetrics().getAscent()) / 2);
	    }

		for(int i=0;i<3;i++){
		    String roiName = path+ "Arm"+(i+1)+".roi";
		    if(!new File(roiName).exists()) continue;
			ip.setColor(Color.red);
		    Roi roi = new RoiDecoder(roiName).getRoi();
			roi.drawPixels(ip);
			Rectangle rec = roi.getPolygon().getBounds();
			String str = "Arm"+(i+1);
			char[] chars = str.toCharArray();
			ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
			   		rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);

			String roiName2 = path+ "Arm"+(i+1)+"Outer.roi";
			if(new File(roiName2).exists()){
				ip.setColor(Color.blue);
				Roi bufRoi = new RoiDecoder(roiName2).getRoi();
				bufRoi.drawPixels(ip);
			}
		}

		return ip;
	}

    public boolean canGoNext(){
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

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

	class YMSetCageOfflineMovieManager extends MovieManager{
		private boolean isRunning;

		private String subjectID;
		private ImageStack stack;
		private ImageProcessor[] backIp;
		private WindowOperator winOperator;
		private ImagePlus[] live;
		private int nextSlice;

		public YMSetCageOfflineMovieManager(String subjectID){
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

		public int getImageWidthSize(){
			return backIp[0].getWidth();
		}

		public int getImageHeightSize(){
			return backIp[0].getHeight();
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
					sleep(1000 / ((IntVariable)Parameter.getInstance().getVar(Parameter.rate)).getVariable());
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
	}
}