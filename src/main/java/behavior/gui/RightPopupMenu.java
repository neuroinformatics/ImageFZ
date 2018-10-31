package behavior.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

/**
 * 右クリック時に表示されるポップアップメニュー．
 */
public class RightPopupMenu extends JPopupMenu {
	
	public RightPopupMenu(final JTextComponent comp){
		JMenuItem cut = new JMenuItem("Cut", KeyEvent.VK_T);
		cut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				comp.cut();
			}			
		});
		JMenuItem copy = new JMenuItem("Copy", KeyEvent.VK_C);
		copy.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				comp.copy();
			}			
		});
		JMenuItem paste = new JMenuItem("Paste", KeyEvent.VK_P);
		paste.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				comp.paste();
			}			
		});
		
		JMenuItem selectAll = new JMenuItem("Select All", KeyEvent.VK_A);
		selectAll.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				comp.selectAll();
			}			
		});
		
		add(cut);
		add(copy);
		add(paste);
		addSeparator();
		add(selectAll);
	}
	
}
