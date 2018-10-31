package behavior.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;

import behavior.image.ImageCapture;

/**
 * キャプチャークラスの選択．
 */
public class SelectCaptureDialog extends JDialog {
	
	private int selected;
	private Vector<Integer> list;
	private JComboBox box;
	
	public SelectCaptureDialog(Vector<Integer> list){
		this.list = list;
		selected = -1;
		
		setTitle("Select");
		setResizable(false);
		JLabel label = new JLabel("Select Capture: ");
		box = new JComboBox();
		for(int i = 0; i < list.size(); i++)
			box.addItem(ImageCapture.DEVICE[list.elementAt(i)]);
		JButton ok = new JButton("OK");
		JButton cancel = new JButton("Cancel");
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;

		gbc.anchor = GridBagConstraints.EAST;
		add(label, gbc);
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.WEST;
		add(box, gbc);
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.gridy++;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		JPanel panel = new JPanel();
		panel.add(ok);
		panel.add(cancel);
		add(panel, gbc);
		
		ok.addActionListener(new OKAction());
		cancel.addActionListener(new CancelAction());
		
		Dimension dm = getToolkit().getScreenSize();
		setLocation((dm.width - 250) / 2, (dm.height - 150) / 2);
		setSize(250, 150);
		setVisible(true);
	}
	
	public int getSelected(){
		return selected;
	}
	
	private class OKAction implements ActionListener{
		public void actionPerformed(ActionEvent arg0) {
			for(int i = 0; i < list.size(); i++)
				if(box.getSelectedItem().equals(ImageCapture.DEVICE[list.elementAt(i)])){
					selected = list.elementAt(i);
					setVisible(false);
					return;
				}
			setVisible(false);
		}	
	}
	
	private class CancelAction implements ActionListener{
		public void  actionPerformed(ActionEvent arg0){
			setVisible(false);
		}
	}
}
