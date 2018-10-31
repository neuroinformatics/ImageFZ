package behavior.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *  JTextField の機能拡張(右クリック)
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
        
        // 右クリック
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
				// 右クリックポップアップメニューから戻ってきたときは実行しない．
				if(ExtendedJTextField.this.getParent() instanceof JPanel && e.getOppositeComponent()!=null
						&& !e.getOppositeComponent().equals(((JPanel)ExtendedJTextField.this.getParent()).getRootPane()))
					ExtendedJTextField.this.selectAll();
			}
			
			public void focusLost(FocusEvent e) {
			}
		});
        
    }   
}
