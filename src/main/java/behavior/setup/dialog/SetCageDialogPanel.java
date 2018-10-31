package behavior.setup.dialog;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.io.RoiDecoder;
import ij.io.RoiEncoder;
import ij.process.ImageProcessor;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.WindowOperator;
import behavior.image.ImageCapture;
import behavior.io.FileManager;


/**
 * ROI を設定する。
 */
public class SetCageDialogPanel extends AbstractDialogPanel implements ActionListener{
	protected JComboBox comboBox;
	protected JButton set;
	protected JButton copy;
	protected int allCage;
	private RoiMovie movie;

	public String getDialogName(){
		return "Set Cage Field";
	}

	public SetCageDialogPanel(DialogManager manager, int allCage){
		super(manager);
		this.allCage = allCage;
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
		for(int i = 1; i <= allCage; i++)
			comboBox.addItem(Integer.toString(i));

		set = new ExtendedJButton("Set");
		set.addActionListener(this);

		add(new JLabel("Chamber: "), gbc);
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
		movie = new RoiMovie();
		movie.start();
	}

	public void postprocess(){
		comboBox.requestFocus();
	}

	public void endprocess(){
		movie.end();
	}

	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == set){
			manager.setModifiedParameter(true);
			Roi roi = movie.getRoi();
			writeRoi(roi);
		} else if(e.getSource() == copy) {
			manager.setModifiedParameter(true);
			new CopyRoiDialog(manager, true, allCage);
		}
	}

	public boolean canGoNext() {   
		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		for(int i = 1; i <= allCage; i++){
			String roiName = path + "/Cage Field" + i + ".roi";
			if(!new File(roiName).exists()){
				BehaviorDialog.showErrorDialog(this, "Cage Field" + i + " is not set.");
				return false;
			}
		}

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
			String path = FileManager.getInstance().getPath(FileManager.PreferenceDir) + "/";
			String fileName;

			fileName = "Cage Field" + comboBox.getSelectedItem() + ".roi";

			File dir = new File(path);
			if(!dir.exists()){
				dir.mkdirs();
			}

			try{
				saveRoi(roi,path+fileName);
			}catch (Exception exception) {
				exception.printStackTrace();
			}
		}
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

	/**
	 * 現在セットされている ROI　を表示。
	 */
	protected ImageProcessor createRoiImage() throws IOException{
		ImageProcessor ip = ImageCapture.getInstance().capture().getProcessor();
		ip = ip.convertToRGB();
		ip.setColor(Color.red);

		String path = FileManager.getInstance().getPath(FileManager.PreferenceDir);
		for(int i = 1; i <= allCage; i++){
			String roiName = path + "/Cage Field" + i + ".roi";
			if(!new File(roiName).exists())
				continue;
			Roi roi = new RoiDecoder(roiName).getRoi();
			roi.drawPixels(ip);
			Rectangle rec = roi.getPolygon().getBounds();
			char[] str = Character.toChars(i);
			ip.drawString(Integer.toString(i), rec.x + (rec.width - ip.getFontMetrics().charsWidth(str, 0, str.length)) / 2, 
					rec.y + (rec.height + ip.getFontMetrics().getAscent()) / 2);
		}

		return ip;
	}

	public void load(Properties properties) {
	}

	@SuppressWarnings("unchecked")
	public void registerParameter(Hashtable table) {
	}

    protected int getImageWidth(){
    	if(movie == null) return 0;
    	return movie.getImageWidthSize();
    }

    protected int getImageHeight(){
    	if(movie == null) return 0;
    	return movie.getImageHeightSize();
    }

	private class RoiMovie extends Thread{

		private boolean isRunning;
		private ImagePlus[] image;
		private WindowOperator winOperator;

		public RoiMovie(){
			isRunning = true;
			image = new ImagePlus[1];
			image[0] = new ImagePlus("Live Movie", ImageCapture.getInstance().capture().getProcessor());
			ImageProcessor[] backIp = new ImageProcessor[1];
			backIp[0] = ImageCapture.getInstance().capture().getProcessor();
			winOperator = WindowOperator.getInstance(1, backIp);
			winOperator.setImageWindow(image, WindowOperator.LEFT_UP);
		}

		public void run(){
			while(isRunning){
				try{
					ImageProcessor ip = createRoiImage();
					image[0].setProcessor("Live Movie", ip);
					sleep(200);
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		}

		public int getImageWidthSize(){
			return image[0].getWidth();
		}

		public int getImageHeightSize(){
			return image[0].getHeight();
		}

		public Roi getRoi(){
			return image[0].getRoi();
		}

		public void end(){
			isRunning = false;
			winOperator.closeWindows();
		}
	}
}