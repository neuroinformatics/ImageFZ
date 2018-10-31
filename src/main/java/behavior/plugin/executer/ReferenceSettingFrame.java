package behavior.plugin.executer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import behavior.gui.ExtendedJButton;
import behavior.setup.dialog.ReferenceDialogPanel;

public abstract class ReferenceSettingFrame extends JDialog{
	private List<ReferenceDialogPanel> dialogs;
	private ReferenceDialogPanel currentDialog;
	private JPanel buttonPanel;
	private int counter;
	protected JButton next,back,cancel;

	public ReferenceSettingFrame(){
		super();
	}

	public final void run(){
		dialogs = new ArrayList<ReferenceDialogPanel>();

		addDialogs(dialogs);
		
		buttonPanel = new JPanel(new GridBagLayout());
        next = new ExtendedJButton("Next >");
        back = new ExtendedJButton("< Back");
        back.setEnabled(false);
        cancel = new ExtendedJButton("Cancel");
        next.addActionListener(new NextActionListener());
        back.addActionListener(new BackActionListener());
        cancel.addActionListener(new CancelActionListener());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.LINE_END;
        buttonPanel.add(back, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0;
        buttonPanel.add(next, gbc);
        gbc.gridx = 2;
        gbc.weightx = 0.6;
        buttonPanel.add(cancel, gbc);
        
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        add(buttonPanel, BorderLayout.SOUTH);
        setBounds(350, 150, 350, 280);
        setResizable(false);
        counter = 0;
        showDialog();
	}

	protected abstract List<ReferenceDialogPanel> addDialogs(List<ReferenceDialogPanel> dialogs);

	private void showDialog(){
        if(counter < 0){
            counter = 0;
        }
        else if(counter >= dialogs.size()){
            save(dialogs);
            dispose();
            return;
        }

        if(currentDialog != null)
            remove(currentDialog);
        
        if(counter < dialogs.size())
            currentDialog = dialogs.get(counter);

        currentDialog.preprocess();
        add(currentDialog,BorderLayout.CENTER);
        setVisible(true);
        currentDialog.postprocess();
        repaint();
	}

	protected abstract void save(List<ReferenceDialogPanel> dialogs);

	public final class NextActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
        	if(!currentDialog.canGoNext()){
                return;
            }
            counter++;
            if(counter == dialogs.size() - 1)
            	next.setText("Save");
            if(counter == 1)
            	back.setEnabled(true);
            showDialog();
        }
    }
    public final class BackActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
        	counter--;
            next.setText("Next >");
            if(counter == 0)
            	back.setEnabled(false);
            showDialog();
        }
    }

    public final class CancelActionListener implements ActionListener{
    	public void actionPerformed(ActionEvent e){
    		dispose();
    	}
    }
}
