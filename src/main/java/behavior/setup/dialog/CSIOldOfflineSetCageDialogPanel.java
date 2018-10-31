package behavior.setup.dialog;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.OvalRoi;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Properties;
//import java.util.logging.*;

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
import behavior.setup.parameter.CSIParameter;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.variable.BTIntVariable;
import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.Variable;

/**
 * CSI(Old ver)でRoiを設定するためのダイアログ
 * 
 * @author Butoh
 */

public class CSIOldOfflineSetCageDialogPanel  extends AbstractDialogPanel implements ActionListener{
	protected JComboBox comboBox;
	protected JButton set;
	protected JButton copy;
	private JTextField _Contact;
	private JTextField _WaitTime;
	private int _FrameWidthPixel;
	private int _FrameHeightPixel;
	private int _FrameWidth;
    private int _FrameHeight;
    private double _InnerDiameter;
    private double _OuterDiameter;
    private double _Chamber1;
    private double _Chamber2;
    private double _ContactDistance;
    private int _LeftSeparator;
    private int _RightSeparator;
    private int _Wait;
    private double _VerticalResolution;
    private double _HorizontalResolution;

    private final String sep = System.getProperty("file.separator");
    private final int UNSET = -1;

	private CSIOldSetCageOfflineMovieManager _Movie;

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
	public CSIOldOfflineSetCageDialogPanel(DialogManager manager){
		super(manager);
		initialize();
	    createDialog();
	}

	private void initialize(){
		_LeftSeparator = UNSET;
	    _RightSeparator = UNSET;
	    _InnerDiameter = UNSET;
	    _OuterDiameter = UNSET;
	    _Chamber1 = UNSET;
	    _Chamber2 = UNSET;
	    _ContactDistance = 2.0;
	    _Wait = 0;
	}

	/**
	 * ダイアログの表示部分。
	 */
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
		comboBox.setPreferredSize(new Dimension(80,18));
		comboBox.addItem("LeftInner");
		comboBox.addItem("RightInner");
		comboBox.addItem("Chamber");
		comboBox.addActionListener(this);

		set = new ExtendedJButton("Set");
		set.addActionListener(this);

		add(new JLabel("RoiType: "), gbc);
		gbc.gridx = 1;
		add(comboBox, gbc);
		gbc.gridx = 2;
		add(set, gbc);

		gbc.gridy += 2;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.CENTER;
		add(new JLabel("ContactDistance(cm):"),gbc);
		gbc.gridx = 2;
		_Contact = new JTextField(_ContactDistance+"",0);
		_Contact.setPreferredSize(new Dimension(30,18));
		_Contact.setHorizontalAlignment(JTextField.RIGHT);
		_Contact.setEditable(true);
		_Contact.setBackground(Color.white);
		_Contact.addActionListener(this);
		add(_Contact, gbc);

		gbc.gridy += 2;
		gbc.gridx = 0;
		add(new JLabel("Wait(ms):"),gbc);
		gbc.gridx = 2;
		_WaitTime = new JTextField(_Wait+"",0);
		_WaitTime.setPreferredSize(new Dimension(30,18));
		_WaitTime.setHorizontalAlignment(JTextField.RIGHT);
		_WaitTime.setEditable(true);
		_WaitTime.setBackground(Color.white);
		add(_WaitTime, gbc);
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
		_Movie = new CSIOldSetCageOfflineMovieManager(str[0]);

		_FrameWidthPixel = _Movie.getImageWidthSize();
		_FrameHeightPixel = _Movie.getImageHeightSize();
		setVerticalResolution(_FrameHeightPixel, _FrameHeight);
		setHorizontalResolution(_FrameWidthPixel, _FrameWidth);
		//log.log(Level.INFO,""+_FrameHeight+_FrameWidth+_InnerDiameter);
		//log.log(Level.INFO,""+_Movie.getImageWidthSize()+" "+_Movie.getImageHeightSize());
	    _Movie.start();

	    if(_Chamber1 != UNSET)
	    	_LeftSeparator = (int)Math.round(_Chamber1*_HorizontalResolution);
	    if(_Chamber2 != UNSET)
	    	_RightSeparator = (int)Math.round(_Chamber2*_HorizontalResolution);

		if(_InnerDiameter != UNSET){
            try{
		        String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) +sep;
		        String oldLeftCageFile = "Cage Field1.roi";
		        String newLeftCageFile = "LInner.roi";
		        String oldRightCageFile = "Cage Field2.roi";
		        String newRightCageFile = "RInner.roi";

		        if(new File(path+oldLeftCageFile).exists() && !new File(path+newLeftCageFile).exists()){
			        remakeRoiFile(path+newLeftCageFile,path+"LOuter.roi",path+oldLeftCageFile,_OuterDiameter);
		        }
		        if(new File(path+oldRightCageFile).exists() && !new File(path+newRightCageFile).exists()){
		            remakeRoiFile(path+newRightCageFile,path+"ROuter.roi",path+oldRightCageFile,_OuterDiameter);
		        }
		    }catch(IOException e){
            	e.printStackTrace();
            }
		}

		if(_OuterDiameter != UNSET){
			_ContactDistance = (_OuterDiameter-_InnerDiameter)/2;
			_Contact.setText(""+_ContactDistance);
		}else if(_ContactDistance != UNSET){
			_Contact.setText(""+_ContactDistance);
		}

		_WaitTime.setText(""+_Wait);
	}

	private void remakeRoiFile(String newFilePath,String newContactFilePath,String oldFilePath,
			                                                            double contactDistance)throws IOException{
	    int cageDiameterWidthPixel = (int)Math.round(_InnerDiameter*_HorizontalResolution);
        int cageDiameterHeightPixel = (int)Math.round(_InnerDiameter*_VerticalResolution);
        //log.log(Level.INFO,""+cageDiameterWidthPixel+" "+cageDiameterHeightPixel);
        //log.log(Level.INFO,""+_HorizontalResolution+" "+_VerticalResolution);
		Roi oldRoi = new RoiDecoder(oldFilePath).getRoi();
        Roi newCageRoi = new OvalRoi((int)oldRoi.getBounds().getCenterX()-(int)cageDiameterWidthPixel/2,
        		                       (int)oldRoi.getBounds().getCenterY()-(int)cageDiameterHeightPixel/2,
		                                cageDiameterWidthPixel,cageDiameterHeightPixel);
        saveRoi(newCageRoi,newFilePath);

        if(contactDistance != UNSET){
	        saveOldOuterRoi(newContactFilePath,oldFilePath,contactDistance);
	     }
	}

	private void saveOldOuterRoi(final String path, final String cageFilePath,final double contactDistance)throws IOException{
		if(!new File(cageFilePath).exists())
            return;

		Roi cageRoi = new RoiDecoder(cageFilePath).getRoi();

        int contactDistanceWidthPixel = (int)Math.round(contactDistance*_HorizontalResolution);
        int contactDistanceHeightPixel = (int)Math.round(contactDistance*_VerticalResolution);

        if(contactDistanceWidthPixel<cageRoi.getBounds().width || contactDistanceHeightPixel<cageRoi.getBounds().height){
        	BehaviorDialog.showErrorDialog(this, "OuterDiameter is smaller than Inner.");
            return;
        }

        Roi contactRoi = new OvalRoi((int)cageRoi.getBounds().getCenterX()-contactDistanceWidthPixel/2, (int)cageRoi.getBounds().getCenterY()-contactDistanceHeightPixel/2,
                               contactDistanceWidthPixel,contactDistanceHeightPixel);

       	saveRoi(contactRoi,path);
	}
    /*private double getVerticalPixels(final double verticalPlainScale){
    	return verticalPlainScale*getVerticalResolution(_FrameWidthPixel,_FrameWidth);
    }*/
  
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

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			Roi roi = _Movie.getRoi();

			saveNewOuterRoi();

			writeRoi(roi);
		}else if(e.getSource() == comboBox){
		    if(comboBox.getSelectedItem().toString().equals("LeftInner") 
		    		|| comboBox.getSelectedItem().toString().equals("RightInner") )
			    Toolbar.getInstance().setTool(Toolbar.OVAL);
		    else
			    Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
	    }else if(e.getSource() == _Contact){
	    	saveNewOuterRoi();
	    }
	}

	private void saveNewOuterRoi(){
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;

        try{
            _ContactDistance = Double.parseDouble(_Contact.getText());
        }catch(NumberFormatException ex){
	        BehaviorDialog.showErrorDialog(this, "Invalid parameter.");
            return;
	    }

        if(_ContactDistance<0){
        	BehaviorDialog.showErrorDialog(this, "Invalid parameter.");
            return;
        }
        try{
            saveOuterRoi(path+"LOuter.roi",path+"LInner.roi",_ContactDistance);
            saveOuterRoi(path+"ROuter.roi",path+"RInner.roi",_ContactDistance);
        }catch(IOException ex){
        	ex.printStackTrace();
        }
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
		        if(roi.getType() != Roi.OVAL){
			        BehaviorDialog.showErrorDialog(this, "Oval ROI required.");
				    return;
			    }

			    try{
			        //Cage
			        if(comboBox.getSelectedItem().toString().equals("LeftInner")){
			            saveRoi(roi, path+"LInner.roi");
			        }else if(comboBox.getSelectedItem().toString().equals("RightInner")){
			        	saveRoi(roi, path+"RInner.roi");
			        }
			    }catch(IOException e){
			    	//log.log(Level.INFO,e.toString());
	        	    e.printStackTrace();
	            }        
		    }else if(comboBox.getSelectedItem().toString().equals("Chamber")){
				if(roi.getType() != Roi.RECTANGLE){
				    BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				    return;
			    }

		        _LeftSeparator = roi.getBounds().x;
		    	_RightSeparator = roi.getBounds().x+roi.getBounds().width;
			}

	    	double contactDistance;
	        try{
	            contactDistance = Double.parseDouble(_Contact.getText());
	        }catch(NumberFormatException ex){
	        	//log.log(Level.INFO,ex.toString());
		        BehaviorDialog.showErrorDialog(this, "Invalid parameter.");
                return;
		    }

	        if(contactDistance<0){
	        	BehaviorDialog.showErrorDialog(this, "Invalid parameter.");
                return;
	        }
	        try{
	            saveOuterRoi(path+"LOuter.roi",path+"LInner.roi",contactDistance);
	            saveOuterRoi(path+"ROuter.roi",path+"RInner.roi",contactDistance);
	        }catch(IOException ex){
	        	ex.printStackTrace();
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

	/**
	 * 元のRoiから算出したRoiをパスに保存する。
	 * @param path
	 * @param cageFilePath
	 * @param contactDistance
	 * @throws IOException
	 */
	private void saveOuterRoi(final String path, final String cageFilePath,final double contactDistance)throws IOException{
		if(!new File(cageFilePath).exists())
            return;

		Roi cageRoi = new RoiDecoder(cageFilePath).getRoi();

        int contactDistanceWidthPixel = (int)Math.round(contactDistance*_HorizontalResolution);
        int contactDistanceHeightPixel = (int)Math.round(contactDistance*_VerticalResolution);

        Roi contactRoi = new OvalRoi(cageRoi.getBounds().x-contactDistanceWidthPixel, cageRoi.getBounds().y-contactDistanceHeightPixel,
        		cageRoi.getBounds().width+contactDistanceWidthPixel*2,cageRoi.getBounds().height+contactDistanceHeightPixel*2);

       	saveRoi(contactRoi,path);
	}

    public void load(Properties properties){
    	if(properties != null){
    		try{
    		    String key = properties.getProperty("cage.diameter(cm)","none");
  			    if(key != "none")
  			        _InnerDiameter = Double.parseDouble(key);
  			    String key2 = properties.getProperty("contact.distance(cm)","none");
  			    if(key2 != "none")
  			        _OuterDiameter = Double.parseDouble(key2);
  			    String key3 = properties.getProperty("chamber1(cm)","none");
  			    if(key3 != "none")
  			        _Chamber1 = Double.parseDouble(key3);
  			    String key4 = properties.getProperty("chamber2(cm)","none");
  			    if(key4 != "none")
  			        _Chamber2 = Double.parseDouble(key4);

  			    String key5 = properties.getProperty("distance.contact","none");
			    if(key5 != "none")
 			        _ContactDistance = Double.parseDouble(key5);
  			    String key6 = properties.getProperty("separator.left","none");
  			    if(key6 != "none")
  			        _LeftSeparator = Integer.parseInt(key6);
  			    String key7 = properties.getProperty("separator.right","none");
  			    if(key7 != "none")
  			        _RightSeparator = Integer.parseInt(key7);
  			    String key8 = properties.getProperty("wait","none");
			    if(key8 != "none")
			        _Wait = Integer.parseInt(key8);
    		}catch(NumberFormatException e){
    			e.printStackTrace();
    		}
  	    }
    }

	//映像にRoiを描く
	protected ImageProcessor createRoiImage(ImageProcessor ip) throws IOException{
		ip = ip.convertToRGB();
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		String roiName = path +sep+ "LInner.roi";
		if(new File(roiName).exists()){
		   ip.setColor(Color.red);
	       Roi roi = new RoiDecoder(roiName).getRoi();
		   roi.drawPixels(ip);
		   Rectangle rec = roi.getPolygon().getBounds();
		   String str = "Left";
		   char[] chars = str.toCharArray();
		   ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
		    	rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		}
		String roiName2 = path +sep+ "RInner.roi";
		if(new File(roiName2).exists()){
		   ip.setColor(Color.red);
	       Roi roi = new RoiDecoder(roiName2).getRoi();
		   roi.drawPixels(ip);
		   Rectangle rec = roi.getPolygon().getBounds();
		   String str = "Right";
		   char[] chars = str.toCharArray();
		   ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
		    	rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		}

		String roiName3 = path +sep+ "LOuter.roi";
		if(new File(roiName3).exists()){
		   ip.setColor(Color.blue);
	       Roi roi = new RoiDecoder(roiName3).getRoi();
		   roi.drawPixels(ip);
		}
		String roiName4 = path +sep+ "ROuter.roi";
		if(new File(roiName4).exists()){
		   ip.setColor(Color.blue);
	       Roi roi = new RoiDecoder(roiName4).getRoi();
		   roi.drawPixels(ip);
		}

		if(_LeftSeparator != CSIParameter.UNSET){
			ip.setColor(Color.orange);
		    ip.drawLine((int)_LeftSeparator, 0,(int)_LeftSeparator, _FrameHeightPixel);
		    String str2 = "Left" + "\n" + "Area";
		    char[] chars2 = str2.toCharArray();
		    ip.drawString(str2, (int)_LeftSeparator-5-ip.getFontMetrics().charsWidth(chars2, 0, chars2.length)/2,
		    		(_FrameHeightPixel+ip.getFontMetrics().getAscent())/2);
		}

	    if(_RightSeparator != CSIParameter.UNSET){
			ip.setColor(Color.orange);
		    ip.drawLine((int)_RightSeparator, 0,(int)_RightSeparator, _FrameHeightPixel);
		    String str3 = "Right" + "\n" + "Area";
		    ip.drawString(str3, (int)_RightSeparator+5,
		    		(_FrameHeightPixel+ip.getFontMetrics().getAscent())/2);
	    }

		return ip;
	}

	//次のステップに進んでよいか
	public boolean canGoNext() {
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

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

		String roiName3 = path + sep + "LOuter.roi";
		String roiName4 = path + sep + "ROuter.roi";
		if(!new File(roiName3).exists() || !new File(roiName4).exists()){
			BehaviorDialog.showErrorDialog(this, "ContactArea is not set.");
			return false;
		}

		if(_LeftSeparator == UNSET){
			BehaviorDialog.showErrorDialog(this, "LeftArea is not set.");
			return false;
		}

		if(_RightSeparator == UNSET){
			BehaviorDialog.showErrorDialog(this, "RightArea is not set.");
			return false;
		}

		if(_LeftSeparator>_RightSeparator){
			BehaviorDialog.showErrorDialog(this, "Invalid line.");
		    return false;
		}

		try{
		    _Wait = Integer.parseInt(_WaitTime.getText());
		}catch(NumberFormatException e){
			BehaviorDialog.showErrorDialog(this, "Invalid Value(Wait).");
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
		            }else if(vars[i].getName().equals("wait")){
			            parameter.getVar(i).setVar(_Wait);
			        }
		        }else if(vars[i] instanceof CSIDoubleVariable){
	                if(vars[i].getName().equals("distance.contact") ){
			            parameter.getVar(i).setVar(_ContactDistance);
		            }
	            }
	        }catch(Exception e) {
		        BehaviorDialog.showErrorDialog(manager, "Invalid input!!");
		        return false;
	        }
	    }
		return true;
	}

	class CSIOldSetCageOfflineMovieManager extends MovieManager{
		private boolean isRunning;

		private String subjectID;
		private ImageStack stack;
		private ImageProcessor[] backIp;
		private WindowOperator winOperator;
		private ImagePlus[] live;
		private int nextSlice;

		public CSIOldSetCageOfflineMovieManager(String subjectID){
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