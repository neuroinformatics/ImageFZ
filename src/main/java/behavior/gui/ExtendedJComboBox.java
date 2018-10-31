package behavior.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.*;


/**
 * editable �� JComboBox �ɑ΂��� ExtendedJTextField�@�Ɠ��l�̊g���B
 */
public class ExtendedJComboBox extends JComboBox {
	
	private JTextField text;
	private RightPopupMenu popup;
	
	public ExtendedJComboBox(){
		super();
		setEditable(true);
		
		text = (JTextField)this.getEditor().getEditorComponent();
		popup = new RightPopupMenu(text);
		
		// �E�N���b�N
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
				// �E�N���b�N�|�b�v�A�b�v���j���[����߂��Ă����Ƃ��͎��s���Ȃ��D
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
