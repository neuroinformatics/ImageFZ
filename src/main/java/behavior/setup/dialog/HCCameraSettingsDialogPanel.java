package behavior.setup.dialog;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJButton;
import behavior.gui.WindowOperator;
import behavior.image.ImageCapture;
import behavior.io.FileManager;
import behavior.io.ImageLoader;

/**
 * HC用の CameraSettingDialogPanel
 * 従来の GetBackground の機能を統合。
 */
public class HCCameraSettingsDialogPanel extends CameraSettingsDialogPanel implements ActionListener {

	private ExtendedJButton set;
	private ImagePlus[] background;
	private ImageProcessor back;
	private WindowOperator winOperator;

	private static final String backImageName = "background.tif";

	public HCCameraSettingsDialogPanel(DialogManager manager) {
		super(manager);
	}

	protected void createDialog(){
		super.createDialog();
		remove(message);
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		add(set = new ExtendedJButton("Set Background"), gbc);

		set.addActionListener(this);
	}

	public void preprocess(){
		super.preprocess();

		setBackgroundImage();
		ImageProcessor[] ip = new ImageProcessor[1];
		ip[0] = background[0].getProcessor();
		winOperator = WindowOperator.getInstance(1, ip);
		winOperator.setImageWindow(background, WindowOperator.RIGHT_UP);
	}	

	private void setBackgroundImage(){
		ImageLoader imgLoader = new ImageLoader();
		ImageStack imgStack = imgLoader.loadImageWithoutError(FileManager.getInstance().getPath(FileManager.program) + "/", backImageName);
		background = new ImagePlus[1];
		background[0] = new ImagePlus();
		if(imgStack == null){
			ImageProcessor noImage = ImageCapture.getInstance().capture().getProcessor();
			noImage = noImage.convertToRGB();
			noImage.setColor(Color.black);
			noImage.fill();
			String str = "No Image";
			char[] chars = str.toCharArray();
			noImage.setColor(Color.red);
			noImage.drawString(str, (noImage.getWidth() - noImage.getFontMetrics().charsWidth(chars, 0, str.length())) / 2,
					(noImage.getHeight() - noImage.getFontMetrics().getAscent()) / 2);
			background[0].setProcessor("Background Image", noImage);
		} else {
			back = imgStack.getProcessor(1);
			background[0].setProcessor("Background Image", back.duplicate());
		}
	}

	public ImageProcessor getImage(){
		return back;
	}

	public boolean canGoNext(){
		if(back == null){
			BehaviorDialog.showErrorDialog(this, "The background image is not set!!");
			return false;
		}

		return true;
	}

	public void actionPerformed(ActionEvent e) {
		ImagePlus imp = ImageCapture.getInstance().capture();
		back = imp.getProcessor();
		FileSaver fs = new FileSaver(imp);
		if(fs.saveAsTiff(FileManager.getInstance().getPath(FileManager.program) + "/" + backImageName)){
			background[0].setProcessor("Background Image", back.duplicate());
		}
		else
			BehaviorDialog.showErrorDialog(this, "Can't save a captured image!!");

	}

}
