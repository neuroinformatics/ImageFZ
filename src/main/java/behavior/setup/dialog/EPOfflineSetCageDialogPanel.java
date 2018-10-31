package behavior.setup.dialog;

import ij.ImagePlus;
import ij.ImageStack;
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
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.MovieManager;
import behavior.gui.WindowOperator;
import behavior.io.FileManager;
import behavior.io.ImageLoader;
//import behavior.io.PropertyManager;
import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.variable.IntVariable;

public class EPOfflineSetCageDialogPanel extends AbstractDialogPanel implements ActionListener{
	private final String sep = System.getProperty("file.separator");
	protected JComboBox comboBox;
	protected JButton set;
	protected JButton copy;
	private EPSetCageOfflineMovieManager movie;

	public String getDialogName(){
		return "Set Cage Field";
	}

	public EPOfflineSetCageDialogPanel(DialogManager manager){
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
		//comboBox.addItem("MainArea");
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
	}

	public void preprocess(){
		String str[] = manager.getSubjectID();
		movie = new EPSetCageOfflineMovieManager(str[0]);
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

			//Center
			if(comboBox.getSelectedItem().toString().equals("Center")){
			    fileName = "center1.roi";
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
	}

    public void load(Properties properties){}

	//‰f‘œ‚ÉRoi‚ð•`‚­
	protected ImageProcessor createRoiImage(ImageProcessor ip) throws IOException{
		ip = ip.convertToRGB();

		//MainArea‚ð•`‚­
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		//Center‚ð•`‚­
		String roiName2 = path + sep + "center1.roi";
		if(new File(roiName2).exists()){
		   ip.setColor(Color.blue);
	       Roi roi = new RoiDecoder(roiName2).getRoi();
		   roi.drawPixels(ip);
		   Rectangle rec = roi.getPolygon().getBounds();
		   String str = "Center";
		   char[] chars = str.toCharArray();
		   ip.drawString(str, rec.x + (rec.width - ip.getFontMetrics().charsWidth(chars, 0, chars.length)) / 2, 
		    	rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		}

		return ip;
	}

	//ŽŸ‚ÌƒXƒeƒbƒv‚Éi‚ñ‚Å‚æ‚¢‚©
	public boolean canGoNext() {
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);

		String roiName2 = path + sep + "center1.roi";
		if(!new File(roiName2).exists()){
			BehaviorDialog.showErrorDialog(this, "Center is not set.");
			return false;
		}
		
		return true;
	}

	class EPSetCageOfflineMovieManager extends MovieManager{
		private boolean isRunning;

		private String subjectID;
		private ImageStack stack;
		private ImageProcessor[] backIp;
		private WindowOperator winOperator;
		private ImagePlus[] live;
		private int nextSlice;

		public EPSetCageOfflineMovieManager(String subjectID){
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