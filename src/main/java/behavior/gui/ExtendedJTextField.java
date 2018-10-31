package behavior.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *  JTextField �̋@�\�g��(�E�N���b�N)
 */

public class ExtendedJTextField extends JTextField {
    
	private RightPopupMenu popup;

	public ExtendedJTextField(String text){
        super(text);
        setPopup();
    }

    public ExtendedJTextField(String text,int column){
        super(text,column);
        setPopup();
    }

    protected void setPopup(){  
        popup = new RightPopupMenu(this);
        
        // �E�N���b�N
        this.addMouseListener(new MouseListener(){
        	public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger()){
					popup.show(ExtendedJTextField.this, e.getX(), e.getY());
				}
			}
			public void mouseReleased(MouseEvent e) {
				if(e.isPopupTrigger()){
					popup.show(ExtendedJTextField.this, e.getX(), e.getY());
				}
			}       
			public void mouseClicked(MouseEvent e) {
			}
			public void mouseEntered(MouseEvent e) {
			}
			public void mouseExited(MouseEvent e) {
			}	
        });
        
        this.addFocusListener(new FocusListener(){
			public void focusGained(FocusEvent e) {
				// �E�N���b�N�|�b�v�A�b�v���j���[����߂��Ă����Ƃ��͎��s���Ȃ��D
				if(ExtendedJTextField.this.getParent() instanceof JPanel && e.getOppositeComponent()!=null
						&& !e.getOppositeComponent().equals(((JPanel)ExtendedJTextField.this.getParent()).getRootPane()))
					ExtendedJTextField.this.selectAll();
			}
			
			public void focusLost(FocusEvent e) {
			}
		});
        
    }   
}
