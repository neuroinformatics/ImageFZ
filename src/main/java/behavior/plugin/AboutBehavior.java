package behavior.plugin;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.*;

import behavior.Version;


/**
 * behavior のバージョン情報の表示，などなど．
 */
public class AboutBehavior extends BehaviorEntryPoint {
	private final int width = 480;
	private final int height = 320;
	public void run(String arg0) {
		new AboutDialog().setVisible(true);
	}
	
	private class AboutDialog extends JFrame{
		public AboutDialog(){
			setTitle("About Behavior");
			setResizable(false);
			setSize(width, height);
			
			int xLocation = (Toolkit.getDefaultToolkit().getScreenSize().width - width) / 2;
			int yLocation = (Toolkit.getDefaultToolkit().getScreenSize().height - height) / 2;
			setLocation(xLocation, yLocation);
			
			final JTextArea information = new JTextArea();
			information.setText(getInformation());
			information.setEditable(false);
			JScrollPane sp = new JScrollPane(information);
			add(sp, BorderLayout.CENTER);
			
			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					AboutDialog.this.setVisible(false);
				}
			});
			JButton copy = new JButton("Copy");
			copy.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					information.selectAll();
					information.copy();
					information.select(0, 0);
				}
			});
			JPanel panel = new JPanel();
			panel.add(copy);
			panel.add(close);
			add(panel, BorderLayout.SOUTH);
		}
	}
	
	/**
	 * 表示したい内容をここに書く．
	 */
	private String getInformation(){
		StringBuilder sb = new StringBuilder();

		sb.append(Version.getVersion() + "\n");
		sb.append("ImageJ " + ij.IJ.getVersion() + "\n");
		// Java VM
		sb.append("-- Java Information --\n");
		Iterator<Entry<Object, Object>> it = System.getProperties().entrySet().iterator();
		while(it.hasNext()){
			Entry<Object, Object> entry = it.next();
			sb.append(entry.getKey() + " = " + entry.getValue() + "\n");
		}
		
		return sb.toString();
	}
	
}