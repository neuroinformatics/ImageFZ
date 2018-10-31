package behavior.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;

/**
 * Enter �Ń{�^����������悤�ɂ���B
 */
public class ExtendedJButton extends JButton implements KeyListener{
	public ExtendedJButton(String value){
		super(value);
		this.addKeyListener(this);
	}

	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode() == KeyEvent.VK_ENTER)
			this.doClick();
	}

	public void keyReleased(KeyEvent arg0) {
	}
	public void keyTyped(KeyEvent arg0) {
	}
}
