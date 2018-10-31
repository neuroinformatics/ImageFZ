package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

/**
 * SubjectID ‚ðÝ’èB
 */
public class SubjectDialogPanel extends AbstractDialogPanel implements ActionListener {
	private ExtendedJTextField[] field;
	private JCheckBox[] check;
	boolean[] isMiceExist;
	private int allCage;

	public String getDialogName(){
		return "Subject ID";
	}

	public SubjectDialogPanel(DialogManager manager, int allCage){
		super(manager);
		this.allCage = allCage;

		setLayout(new GridBagLayout());
		field = new ExtendedJTextField[allCage];
		isMiceExist = new boolean[allCage];
	    Arrays.fill(isMiceExist, true);
		if(allCage>1){
		    check = new JCheckBox[allCage];
		    for(int i = 0; i < field.length; i++){
			    field[i] = new ExtendedJTextField("Subject_ID"+(i+1));
			    field[i].setPreferredSize(new Dimension(160,25));
			    field[i].addActionListener(this);
			    check[i] = new JCheckBox("",true);
			    check[i].addActionListener(new CheckBoxListener(i));
		    }
		}else if(allCage==1){
			field[0] = new ExtendedJTextField("Subject_ID" +1);
		    field[0].setPreferredSize(new Dimension(160,25));
		    field[0].addActionListener(this);
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		if(allCage>4){
			for(int i = 0; i < allCage; i++){
				gbc.gridx = 0;
				gbc.gridwidth = 1;
				add(new JLabel("Cage " + (i + 1) + ": "), gbc);
			    gbc.anchor = GridBagConstraints.EAST;
			    gbc.gridx = 1;
			    add(check[i], gbc);
				gbc.anchor = GridBagConstraints.LINE_END;
				gbc.gridx = 2;
				gbc.gridwidth = 2;
				add(field[i], gbc);

				gbc.gridy++;
			}
		}else{
			for(int i = 0; i < allCage; i++){
				gbc.gridx = 0;
				gbc.gridwidth = 1;
				add(new JLabel("Cage " + (i + 1) + ": "), gbc);
				gbc.gridy++;
				if(allCage>1){
				    gbc.anchor = GridBagConstraints.EAST;
				    gbc.gridx = 0;
				    add(check[i], gbc);
				}
				gbc.anchor = GridBagConstraints.LINE_END;
				gbc.gridx = 1;
				gbc.gridwidth = 2;
				add(field[i], gbc);
				
				gbc.gridy++;
			}
		}
	}

	public void postprocess(){
		field[0].requestFocus();
	}

	public void load(Properties properties) {
	}

	public boolean canGoNext(){
		String[] subjectID = new String[field.length];
		if(allCage>1){
		    boolean setCorrect = false;
		    for(int i=0;i<field.length;i++){
			    if(check[i].isSelected()){
				    subjectID[i]= field[i].getText();
				    setCorrect = true;
			    }else{
				    subjectID[i]="# null";
			    }
		    }

		    if(!setCorrect){
			    BehaviorDialog.showErrorDialog(this, "Please select cage.");
			    return false;
		    }
		}else if(allCage==1){
			subjectID[0]= field[0].getText();
		}

		for(int i=0;i<subjectID.length;i++){
			if(!FilenameValidator.validate(subjectID[i])){
				BehaviorDialog.showErrorDialog(this, "Invalid Subject ID.\nSubject ID must not be empty, and must not contain : \n\\ / : * ? \" < > |");
				return false;
			}
		}

		manager.setExistMice(isMiceExist);

		FileManager fm = FileManager.getInstance();

		fm.setSubjectID(subjectID);
		if(fm.subjectImageExist()){
			if(!BehaviorDialog.showWarningDialog(this, "The Subject ID is already used.\nOverwrite?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}
		manager.setSubjectID(subjectID);

		return true;
	}

	public void actionPerformed(ActionEvent arg0){
		manager.getNextButton().doClick();
	}

	class CheckBoxListener implements ActionListener{
		private int cageNo;

		protected CheckBoxListener(int num){
			cageNo = num;
		}

		public void actionPerformed(ActionEvent arg0) {
			if(check[cageNo].isSelected()){
				field[cageNo].setEnabled(true);
				isMiceExist[cageNo] = true;
			}else{
				field[cageNo].setEnabled(false);
				isMiceExist[cageNo] = false;
			}
		}
	}
}