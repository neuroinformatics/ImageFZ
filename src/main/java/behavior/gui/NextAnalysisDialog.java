package behavior.gui;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

/**
 * 実験・解析終了後に表示するダイアログ
 */
public class NextAnalysisDialog extends JDialog {
	boolean nextAnalysis;
	
	public NextAnalysisDialog(){
		super(IJ.getInstance());
		nextAnalysis = false;
		
		setTitle("Done");
		setResizable(false);
		setSize(200, 70);
		Dimension dm = getToolkit().getScreenSize();
		setLocation((dm.width - 200) / 2, (dm.height - 70) / 2);
		JButton next = new JButton("Next Analysis");
		next.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0) {
				nextAnalysis = true;
				setVisible(false);
			}
		});
		JButton quit = new JButton("Quit");
		quit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0){
				setVisible(false);
			}
		});
		
		JPanel panel = new JPanel();
		
		panel.add(next, BorderLayout.WEST);
		panel.add(quit, BorderLayout.EAST);
		add(panel);
		
		setVisible(true);
	}
	
	public boolean nextAnalysis(){
		return nextAnalysis;
	}
}
