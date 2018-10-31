package behavior.setup.dialog;

import behavior.setup.dialog.DialogManager;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

/**
 * BeamTestではRMと同様にSubjectIDに加えてTrialNumberを記録する必要がある。
 * subjectIDを、　subjectID-TrialNumber　の形で設定する。
 * @author Butoh
 */
public class BTSubjectDialogPanel extends AbstractDialogPanel implements ActionListener{
	private ExtendedJTextField[] field;

	public String getDialogName(){
		return "TrialName";
	}

	public BTSubjectDialogPanel(DialogManager manager, int allCage){
		super(manager);

		setLayout(new GridBagLayout());
		field = new ExtendedJTextField[2];
		field[0] = new ExtendedJTextField("Subject_ID");
		field[0].setPreferredSize(new Dimension(180,25));
		field[0].addActionListener(this);

		field[1] = new ExtendedJTextField("TrialNumber");
		field[1].setPreferredSize(new Dimension(180,25));
		field[1].addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		gbc.gridx = 0;
		add(new JLabel("SubjectID" + ": "), gbc);
		gbc.gridx = 1;
		add(field[0], gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		add(new JLabel("TrialNumber" + ": "), gbc);
		gbc.gridx = 1;
		add(field[1], gbc);
	}

	public void postprocess(){
		field[0].requestFocus();
	}

	public void load(Properties properties) {
	}

	public boolean canGoNext() {
		String[] subject = new String[1];
		subject[0] = field[0].getText()+ "-" + field[1].getText();
		if(!FilenameValidator.validate(subject[0])){
			BehaviorDialog.showErrorDialog(this, "Invalid TrialName.\nTrialName must not be empty, and must not contain : \n\\ / : * ? \" < > |");
			return false;
		}

		FileManager fm = FileManager.getInstance();

		fm.setSubjectID(subject);
		if(fm.subjectImageExist()){
			if(!BehaviorDialog.showWarningDialog(this, "The TrialName is already used.\nOverwrite?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}
		manager.setSubjectID(subject);

		boolean[] isMiceExist = new boolean[1];
	    Arrays.fill(isMiceExist, true);
		manager.setExistMice(isMiceExist);

		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}
}