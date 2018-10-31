package behavior.plugin;

import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import behavior.image.ImageCapture;
import behavior.image.process.OnuThresholder;

public class TestThreshold extends JFrame implements ChangeListener, PlugIn
{
	private static int minThred = 100;
	private static int maxThred = 255;
	private JSlider slide;
	private JSlider slide2;
	private JLabel minLabel;
	private JLabel maxLabel;


	public TestThreshold() {
		super("Test_Threshold");

		ImageCapture ic = ImageCapture.getInstance();
		setLayout(new BorderLayout());
		ImagePlus setImp = ic.capture();
		ImagePlus currentImp = ic.capture();
		ImageProcessor currentIp = setImp.getProcessor();
		ImageProcessor setIp = currentIp.duplicate();
		setIp.invertLut();
		OnuThresholder ot = new OnuThresholder(minThred, maxThred);
		ot.applyThreshold(setIp);
		setImp.show();
		ImageWindow thredWindow = setImp.getWindow();
		thredWindow.setLocation(10, 140);
		currentImp.show();
		ImageWindow currentWindow = currentImp.getWindow();
		currentWindow.setLocation(20 + thredWindow.getWidth(), 140);
		setBounds(10, 140 + thredWindow.getHeight() + 10, 400, 100);

		JPanel panel1 = new JPanel();
		slide = new JSlider(JSlider.HORIZONTAL, 0, 255, minThred);
		slide.addChangeListener(this);
		panel1.add(new JLabel("MIN_Threshold"));
		minLabel = new JLabel(new StringBuilder().append(minThred).toString());
		panel1.add(minLabel);
		panel1.add(slide);

		JPanel panel2 = new JPanel();
		panel2.add(new JLabel("MAX_Threshold"));
		maxLabel = new JLabel(new StringBuilder().append(maxThred).toString());
		panel2.add(maxLabel);
		slide2 = new JSlider(JSlider.HORIZONTAL, 0, 255, maxThred);
		slide2.addChangeListener(this);
		panel2.add(slide2);

		getContentPane().add(panel1, "First");
		getContentPane().add(panel2, "Center");
		//setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);

		while(currentImp.getProcessor() != null || setImp.getProcessor() != null) {
			ot.setThreshold(minThred, maxThred);
			currentIp = ic.capture().getProcessor();
			//currentIp.invertLut();
			if(currentImp.getProcessor() != null)
				currentImp.setProcessor("Live", currentIp);
			setIp = currentIp.duplicate();
			ot.applyThreshold(setIp);
			if(setImp.getProcessor() != null)
				setImp.setProcessor("Threshold(" + minThred + " - " + maxThred + ")", setIp);
		}
		ic.close();
	}

	public void stateChanged(ChangeEvent e) {
		if ((JSlider) e.getSource() == slide) {
			minThred = ((JSlider) e.getSource()).getValue();
			if (minThred > 99)
				minLabel.setText(new StringBuilder().append(minThred).toString());
			else
				minLabel.setText(new StringBuilder(" ").append(minThred).toString());
		} else if ((JSlider) e.getSource() == slide2) {
			maxThred = ((JSlider) e.getSource()).getValue();

			if (maxThred > 99)
				maxLabel.setText(new StringBuilder().append(maxThred).toString());
			else
				maxLabel.setText(new StringBuilder(" ").append(maxThred).toString());
		}
	}

	public void run(String arg0) {
	}
}
