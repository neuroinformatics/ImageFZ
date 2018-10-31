package behavior.setup.dialog;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.Opener;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.io.TiffDecoder;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.MovieManager;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.variable.IntVariable;

public class TMOfflineSetCageDialogPanel extends AbstractDialogPanel implements ActionListener{
	private final String sep = System.getProperty("file.separator");
	protected JComboBox comboBox;
	protected JButton set;
	private TMSetCageOfflineMovieManager movie;

	public String getDialogName(){
		return "Set Cage Field";
	}

	public TMOfflineSetCageDialogPanel(DialogManager manager){
		super(manager);
		createDialog();
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
		for(int i=0;i<6;i++){
			comboBox.addItem("Cage "+(i+1));
		}

		set = new ExtendedJButton("Set");
		set.addActionListener(this);

		add(new JLabel("Roi type: "), gbc);
		gbc.gridx = 1;
		gbc.ipadx = 20;
		add(comboBox, gbc);
		gbc.gridx = 2;
		gbc.ipadx = 0;
		add(set, gbc);
	}

	public void preprocess(){
		String str[] = manager.getSubjectID();
		movie = new TMSetCageOfflineMovieManager(str[0]);
		movie.start();

		Toolbar.getInstance().setTool(Toolbar.RECTANGLE);
	}

	public void postprocess(){
		comboBox.requestFocus();
	}

	public void endprocess(){
		movie.end();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			Roi roi = movie.getRoi();
			writeRoi(roi);
		}
	}

	protected void writeRoi(Roi roi){
		if(roi != null){
			if(roi.getType() != Roi.RECTANGLE){
				BehaviorDialog.showErrorDialog(this, "Rectangle ROI required.");
				return;
			}
			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + sep;
			String fileName;

			fileName = "Cage Field"+comboBox.getSelectedItem().toString().charAt(comboBox.getSelectedItem().toString().length()-1)+".roi";
            File file = new File(path+fileName);
			File dir = new File(path);
			if(!dir.exists()){
			    dir.mkdirs();
			}

			try{
			    OutputStream output_stream = new FileOutputStream(file);
			    RoiEncoder encoder = new RoiEncoder(output_stream);
			    encoder.write(roi);
			    output_stream.close();
			}catch (Exception exception) {
			   exception.printStackTrace();
			}	
		}
	}

    public void load(Properties properties){}

	//映像にRoiを描く
	protected ImageProcessor createRoiImage(ImageProcessor ip) throws IOException{
		ip = ip.convertToRGB();

		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		for(int i=0;i<6;i++){
		    String roiName = path + sep + "Cage Field"+(i+1)+".roi";
		    if(new File(roiName).exists()){
		        ip.setColor(Color.red);
	            Roi roi = new RoiDecoder(roiName).getRoi();
		        roi.drawPixels(ip);
		        Rectangle rec = roi.getPolygon().getBounds();
		        String str = ""+(i+1);
		        char[] chars = str.toCharArray();
		        ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
		    	    rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		    }
		}
		return ip;
	}

	//次のステップに進んでよいか
	public boolean canGoNext() {
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		for(int i=0;i<6;i++){
		    String roiName = path + sep + "Cage Field"+(i+1)+".roi";
		    if(!new File(roiName).exists()){
			    BehaviorDialog.showErrorDialog(this, "Cage "+(i+1)+"is not set.");
			    return false;
		    }
		}
		
		return true;
	}

	class TMSetCageOfflineMovieManager extends MovieManager{
		private boolean isRunning;

		private String subjectID;
		private ImageStack stack;
		private ImageProcessor[] backIp;
		private WindowOperator winOperator;
		private ImagePlus[] live;
		private int nextSlice;

		public TMSetCageOfflineMovieManager(String subjectID){
			backIp = new ImageProcessor[1];
			live = new ImagePlus[1];

			this.subjectID = subjectID;
			stack = loadImage(subjectID);
			backIp[0] = stack.getProcessor(stack.getSize());
			live[0] = new ImagePlus(subjectID, backIp[0]);
			nextSlice = 1;

			winOperator = WindowOperator.getInstance(1, backIp);
			winOperator.setImageWindow(live, WindowOperator.LEFT_UP);
		}

		public ImageStack loadImage(String subjectID){
			StringBuilder str = new StringBuilder(subjectID);
			for(int i=0;i<2;i++){
				str.setCharAt(str.lastIndexOf("-"),'_');
			}
		    String imageID = str.toString();

			FileManager fileManager = FileManager.getInstance();
			String extension = ".tiff";
			if(new File(fileManager.getPath(FileManager.ImagesDir) +File.separator+ fileManager.getPath(FileManager.SessionID)+File.separator+imageID + ".tif").exists())
				extension = ".tif";
			
			TiffDecoder imgTd = new TiffDecoder(fileManager.getPath(FileManager.ImagesDir) + File.separator+ fileManager.getPath(FileManager.SessionID)+File.separator, imageID + extension);
			Opener open = new Opener();
			ImagePlus stackImp = null;
			try{
				stackImp = open.openTiffStack(imgTd.getTiffInfo());
			}catch(FileNotFoundException e){
				IJ.error("no file:" +imageID + extension + "(in ImageLoader)");
			}catch(IOException e){
				IJ.error("Input error:" + imageID + extension + "(in ImageLoader)");
			}
			return stackImp.getStack();
		}

		public void run(){
			isRunning = true;

			while(isRunning){
				try{
				if(nextSlice >= stack.getSize())
					nextSlice = 1;

				ImageProcessor bufIp = stack.getProcessor(nextSlice);
				bufIp.setInterpolationMethod(ImageProcessor.BICUBIC);
				ImageProcessor ip = createRoiImage(bufIp.resize(320,240));

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