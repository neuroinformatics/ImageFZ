package behavior.controller;

import java.awt.*;
import java.awt.event.*;

import java.util.Arrays;

import javax.swing.*;

//OFCで使用していたようだが、使いづらかったので仕様を変えた　@Butoh,behavior090303
public class DebugInput extends AbstractInput{
	private static boolean[] open = new boolean[4];
	static AskDialog dialog;

	DebugInput(){
		if(dialog == null){
			dialog = new AskDialog();
			Arrays.fill(open, false);
		}
	}

	@Override
	boolean getInput(int channel){
		if(!(channel<0) && !(3<channel)){
			if(open[channel]){
				Arrays.fill(open, false);
				return true;
			}else{
				Arrays.fill(open, false);
				return false;
			}
		}

		return false;
	}

	@Override
	void clear(int value){}
	@Override
	void reset(){}
	@Override
	void resetAll(){}
	@Override
	void close(){Arrays.fill(open, false);}

	private class AskDialog extends JFrame implements ActionListener{
		private JComboBox choice;

		AskDialog(){
			super("Input");
			setBounds(800, 100, 100, 100);
			GridLayout grid = new GridLayout(2, 1);
			setLayout(grid);

			choice = new JComboBox();
			for(int i=0; i<4; i++)
				choice.addItem(""+(i+1));

			JButton button = new JButton("OK");
			button.addActionListener(this);
			add(choice);
			add(button);
			addWindowListener(new WindowAdapter(){
				public void windowClosing(WindowEvent e){setVisible(false);}});
			setVisible(true);
		}

		@Override
		public void actionPerformed(ActionEvent e){
			int select = Integer.parseInt(choice.getSelectedItem().toString());
			Arrays.fill(open, false);
			open[select-1] = true;
		}
	}
}