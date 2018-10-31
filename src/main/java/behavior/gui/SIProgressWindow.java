package behavior.gui;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class SIProgressWindow extends JDialog{
	private int imageSize;
	private int count = 0;
	private JLabel label;
	private JProgressBar progressBar;

	public SIProgressWindow(final int imageSize){
		super(IJ.getInstance());
		this.imageSize = imageSize-2;

		Dimension dm = getToolkit().getScreenSize();
		this.setLocation((dm.width - 200) / 2, (dm.height - 70) / 2);

		label = new JLabel("Waiting for process...(0/"+imageSize+")");
		progressBar = new JProgressBar(0,imageSize);
		
		JPanel panel = new JPanel();
		BoxLayout boxlayout = new BoxLayout(panel,BoxLayout.Y_AXIS);
		panel.setLayout(boxlayout);
		panel.add(label, BorderLayout.NORTH);
		panel.add(progressBar, BorderLayout.SOUTH);

		this.add(panel);

		this.pack();
		this.setResizable(false);
		this.setVisible(true);
	}
	
	public void count(){
		count++;
		if(count<=imageSize){
			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					label.setText("Waiting for process...("+count+"/"+imageSize+")");
				    progressBar.setValue(count);
				}
			});
		}
	}

	public void close(){
		this.setVisible(false);
	}
}