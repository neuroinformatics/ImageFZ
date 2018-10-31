package behavior.setup.dialog;

import ij.process.ImageProcessor;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.gui.LiveMovieWindow;
import behavior.image.CaptureProperties;
import behavior.image.ImageCapture;

/**
 * カメラの調整画面。
 *　同時に実験で使用する background を取得する。
 */
public class CameraSettingsDialogPanel extends AbstractDialogPanel {

	private LiveMovieWindow movie;
	private JSlider brightness;
	private JSlider contrast;
	private ExtendedJTextField brightnessField;
	private ExtendedJTextField contrastField;
	private JButton setDefault;
	private ImageCapture ic;
	private CaptureProperties cp;
	protected GridBagConstraints gbc;
	protected JLabel message;
	
	private final int FIELD_WIDTH = 3;

	private boolean isChangeParameter = false;
	private int brightnessValue;
	private int contrastValue;
	
	public String getDialogName(){
		return "Camera Settings";
	}

	public CameraSettingsDialogPanel(DialogManager manager){
		super(manager);
		ic = ImageCapture.getInstance();
		cp = ic.getCaptureProperties();
		isChangeParameter = false;
	}

	protected void createDialog(){
		removeAll();
		setLayout(new GridBagLayout()); 
		
		gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		add(new JLabel("Brightness: "), gbc);
		gbc.gridx = 1;
		add(brightness = new JSlider(cp.getMinBrightness(), cp.getMaxBrightness(), ic.getBrightness()), gbc);
		gbc.gridx = 2;
		add(brightnessField = new ExtendedJTextField(Integer.toString(ic.getBrightness()), FIELD_WIDTH), gbc);
		brightnessField.addCaretListener(new ThresholdValueChangeListener(brightness));
		brightness.addChangeListener(new SliderChangeListener(brightnessField));
		gbc.gridy++;
		gbc.gridx = 0;
		add(new JLabel("Contrast: "), gbc);
		gbc.gridx = 1;
		add(contrast = new JSlider(cp.getMinContrast(), cp.getMaxContrast(), ic.getContrast()), gbc);
		gbc.gridx = 2;
		add(contrastField = new ExtendedJTextField(Integer.toString(ic.getContrast()), FIELD_WIDTH), gbc);
		contrastField.addCaretListener(new ThresholdValueChangeListener(contrast));
		contrast.addChangeListener(new SliderChangeListener(contrastField));
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		add(setDefault = new JButton("Default"), gbc);
		gbc.gridy++;
		message = new JLabel("Click 'Next' to get a background image.");
		add(message, gbc);
		setDefault.addActionListener(new DefaultAction());
		setBrightnessAndContrast();
	}

	public void preprocess(){
		createDialog();
		isChangeParameter = true;
		movie = new LiveMovieWindow();
		movie.start();

		manager.getNextButton().requestFocus();
	}

	public void endprocess(){
		isChangeParameter = false;
		movie.end();
	}

	public ImageProcessor getImage(){
		ImageProcessor img = movie.getImage();;

		try{
			Thread.sleep(500); // 画像の取得中に movie が死なないように待機(Nullpo 防止)
		} catch(Exception e){
			e.printStackTrace();
		}
		return img;
	}


	public void load(Properties properties) {
		if (properties != null) {
			try {
				brightnessValue = Integer.valueOf(properties.getProperty("brightness", Integer.toString(cp.getDefaultBrightness())));		
				contrastValue = Integer.valueOf(properties.getProperty("contrast", Integer.toString(cp.getDefaultContrast())));
			} catch(NumberFormatException e) {
				BehaviorDialog.showErrorDialog(this, "Invalid Parameter");
			}
		} else {
			brightnessValue = cp.getDefaultBrightness();
			contrastValue = cp.getDefaultContrast();
		}
	}
	
	public void setBrightnessAndContrast() {
		if(brightnessValue > brightness.getMaximum())
			brightnessValue = brightness.getMaximum();  
		else if(brightnessValue < brightness.getMinimum())
			brightnessValue = brightness.getMinimum();
		if(contrastValue > contrast.getMaximum())
			contrastValue = contrast.getMaximum();  
		else if(contrastValue < contrast.getMinimum())
			contrastValue = contrast.getMinimum();
		brightnessField.setText(String.valueOf(brightnessValue));
		brightness.setValue(brightnessValue);
		contrastField.setText(String.valueOf(contrastValue));
		contrast.setValue(contrastValue);
		ic.setBrightness(brightnessValue);
		ic.setContrast(contrastValue);
	}
	
	public void setProperties(Properties prop) {
		prop.setProperty("brightness", String.valueOf(brightnessValue));
		prop.setProperty("contrast", String.valueOf(contrastValue));
	}

	public class SliderChangeListener implements ChangeListener{
		private JTextField field;
		public SliderChangeListener(JTextField field){
			this.field = field;
		}
		public void stateChanged(ChangeEvent e) {
			if(isChangeParameter)manager.setModifiedParameter(true);
			int value = ((JSlider)e.getSource()).getValue();
			this.field.setText(value+"");
			if(field == brightnessField) {
				brightnessValue = value;
				ic.setBrightness(brightnessValue);
			}else if(field == contrastField) {
				contrastValue = value;
				ic.setContrast(contrastValue);
			}
		}
	}

	private class ThresholdValueChangeListener implements CaretListener{
		private JSlider slider;
		public ThresholdValueChangeListener(JSlider slider){
			this.slider = slider;
		}

		public void caretUpdate(CaretEvent e) {
			String value = ((JTextField)e.getSource()).getText();

			try{
				int v = Integer.parseInt(value);
				if(v > slider.getMaximum())
					v = slider.getMaximum();  
				else if(v < slider.getMinimum())
					v = slider.getMinimum();
				this.slider.setValue(v);
				
				if(slider == brightness) {
					brightnessValue = v;
					ic.setBrightness(brightnessValue);
				}else if(slider == contrast) {
					contrastValue = v;
					ic.setContrast(contrastValue);
				}
			}
			catch (Exception ex) {
			}
		}   
	}
	
	private class DefaultAction implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			brightnessValue = cp.getDefaultBrightness();
			brightness.setValue(brightnessValue);
			contrastValue = cp.getDefaultContrast();
			contrast.setValue(contrastValue);
		}
	}
}
