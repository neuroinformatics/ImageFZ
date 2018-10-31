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
 * 実験を中断するためのダイアログとその制御を行う。
 * Behavior5系にあったものを移殖したもの。
 * @author anonymous
 */
public class Timer{
	private double ｔime;
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
	 * 現在の時間をセットする。
	 * @param time 現在の経過時間
	 */
	public void run(double time){
		ｔime = Math.round(time*10.0)/10.0;
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
		        timerScreen.setTime(ｔime);
			}
		});
	}

	/**
	 * 現在表示されている時間。
	 * TimerScreenで中断した場合の表示に用いる。
	 * @return　表示中の時間
	 */
	public double getEndTimebySec(){
		return ｔime;
	}

	/**
	 * ダイアログを非表示にする。
	 */
	public void finalize(){
		//直後にnullするからinvokeLater()ではまずい
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
	 * TimerScreenで中断したらtrueを返す。
	 * @return　"Interrupt"ボタンを押した場合true
	 */
	public boolean isInterrupt(){
		if(timerScreen ==null) return false;
		return timerScreen.isInterrupt();
	}

	/**
	 * 実験中に表示するダイアログ。
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
		 * ダイアログに表示する時間をセットする。
		 * @param time　表示させる時間
		 */
		public void setTime(double time){
		    if(label == null) return;
			label.setText("Elapsed Time : " + time);
			label.repaint();
		}

		/**
		 * "Interrupt"ボタンを押したときの処理。
		 */
		public void actionPerformed(ActionEvent e) {
			if(e.getSource() == button){
				interrupt = true;
			}
		}

		/**
		 *　"Interrupt"ボタンを押したかどうか。
		 * @return
		 */
		public boolean isInterrupt(){
			return interrupt;
		}
	}
}