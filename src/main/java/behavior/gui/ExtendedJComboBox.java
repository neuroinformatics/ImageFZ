package behavior.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;


/**
 * editable な JComboBox に対する ExtendedJTextField　と同様の拡張。
 */
public class ExtendedJComboBox extends JComboBox {
	
	private JTextField text;
	private RightPopupMenu popup;
	
	public ExtendedJComboBox(){
		super();
		setEditable(true);
		
		text = (JTextField)this.getEditor().getEditorComponent();
		popup = new RightPopupMenu(text);
		
		// 右クリック
		text.addMouseListener(new MouseListener(){
        	public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					popup.show(text, e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					popup.show(text, e.getX(), e.getY());
				}
			}       
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}	
        });
		
		text.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				try{
				// 右クリックポップアップメニューから戻ってきたときは実行しない．
				if(!e.getOppositeComponent().equals(((JPanel)ExtendedJComboBox.this.getParent()).getRootPane()))
					text.selectAll();
				}catch(NullPointerException ex){}
			}
			
			public void focusLost(FocusEvent e) {
			}
		});
	}
	
	public String getText(){
		return text.getText();
	}
}
