package behavior.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * �����𒆒f���邽�߂̃_�C�A���O�Ƃ��̐�����s���B
 * Behavior5�n�ɂ��������̂��ڐB�������́B
 * @author anonymous
 */
public class Timer{
	private double ��ime;
	private TimerScreen timerScreen;
	
	public Timer(){
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
				    timerScreen = new TimerScreen(1);	
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Timer(final int cage){
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
				    timerScreen = new TimerScreen(cage);	
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public boolean isSetTimerScreen(){
		if(timerScreen == null)
			return false;
		else
			return true;
	}

	/**
	 * ���݂̎��Ԃ��Z�b�g����B
	 * @param time ���݂̌o�ߎ���
	 */
	public void run(double time){
		��ime = Math.round(time*10.0)/10.0;
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
		        timerScreen.setTime(��ime);
			}
		});
	}

	/**
	 * ���ݕ\������Ă��鎞�ԁB
	 * TimerScreen�Œ��f�����ꍇ�̕\���ɗp����B
	 * @return�@�\�����̎���
	 */
	public double getEndTimebySec(){
		return ��ime;
	}

	/**
	 * �_�C�A���O���\���ɂ���B
	 */
	public void finalize(){
		//�����null���邩��invokeLater()�ł͂܂���
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
			        timerScreen.setVisible(false);
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.timerScreen = null;
	}

	/**
	 * TimerScreen�Œ��f������true��Ԃ��B
	 * @return�@"Interrupt"�{�^�����������ꍇtrue
	 */
	public boolean isInterrupt(){
		if(timerScreen ==null) return false;
		return timerScreen.isInterrupt();
	}

	/**
	 * �������ɕ\������_�C�A���O�B
	 * @author anonymous
	 */
	private class TimerScreen extends JFrame  implements ActionListener{
		private JPanel panel1, panel2;
		private JLabel label;
		private JButton button;
		private boolean interrupt;

		public TimerScreen(int cage){
			if(cage==1)
				createDialog();
			else
				createDialog(cage);
		}

		public void createDialog(){
			interrupt = false;
			panel1 = new JPanel();
			panel2 = new JPanel();
			label = new JLabel();
			button = new JButton();
			
			panel1.add(label);
			panel2.add(button);
			
			setLayout(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
		    gbc.gridy = 0;
		    gbc.gridwidth = 1;
		    gbc.gridheight = 1;
		    gbc.anchor = GridBagConstraints.LINE_END;
		    add(panel1, gbc);
		    gbc.gridy = 1;
			add(panel2, gbc);

			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			label.setText("Elapsed Time : 0");
			button.setText("Interrupt");
			button.addActionListener(this);
			pack();
			toFront();
			setTitle("Timer");
			setResizable(false);
			setSize(150, 100);
			setLocation(950,150);
			setVisible(true);
		}

		public void createDialog(int cage){
			interrupt = false;
			panel2 = new JPanel();
			button = new JButton();

			panel2.add(button);

		    setLayout(new GridBagLayout());
		    GridBagConstraints gbc = new GridBagConstraints();
		    gbc.gridx = 0;
	        gbc.gridy = 0;
	        gbc.gridwidth = 1;
	        gbc.gridheight = 1;
	        gbc.anchor = GridBagConstraints.LINE_END;
		    add(panel2, gbc);

		    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		    button.setText("Interrupt");
		    button.addActionListener(this);
		    pack();
		    toFront();

		    setTitle("Timer");
		    setResizable(false);
		    setSize(150, 100);
		    setLocation(870,(120*cage)+30);
		    setVisible(true);
		}

    	/**
		 * �_�C�A���O�ɕ\�����鎞�Ԃ��Z�b�g����B
		 * @param time�@�\�������鎞��
		 */
		public void setTime(double time){
		    if(label == null) return;
			label.setText("Elapsed Time : " + time);
			label.repaint();
		}

		/**
		 * "Interrupt"�{�^�����������Ƃ��̏����B
		 */
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == button){
				interrupt = true;
			}
		}

		/**
		 *�@"Interrupt"�{�^�������������ǂ����B
		 * @return
		 */
		public boolean isInterrupt(){
			return interrupt;
		}
	}
}