package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
//import java.util.logging.*;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileManager;
import behavior.io.RMReferenceManager;
import behavior.util.FilenameValidator;
import behavior.util.rmconstants.RMConstants;

public class RMSubjectDialogPanel extends AbstractDialogPanel implements ActionListener{
	private ExtendedJTextField[] field = new ExtendedJTextField[2];
	private ExtendedJTextField subField;
	private JComboBox comboBox;
	private boolean isFirstTrial = true;

	//private Logger log = Logger.getLogger("behavior.plugin.executer.RMSubjectDialogPanel");
	public String getDialogName(){
		return "TrialName";
	}

	public RMSubjectDialogPanel(DialogManager manager) {
		super(manager);

		isFirstTrial = true;

		setLayout(new GridBagLayout());
		if(RMConstants.isReferenceMemoryMode()){
			comboBox = new JComboBox();
			comboBox.setPreferredSize(new Dimension(180,25));
		}else{
		    subField = new ExtendedJTextField("Subject_ID");
		    subField.addActionListener(this);
		    subField.setPreferredSize(new Dimension(180,25));
		}

		field[0] = new ExtendedJTextField("TrialNumber");
		field[0].addActionListener(this);
		field[0].setPreferredSize(new Dimension(180,25));

		field[1] = new ExtendedJTextField("RoomNO");
	    field[1].addActionListener(this);
	    field[1].setPreferredSize(new Dimension(180,25));

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
		if(RMConstants.isReferenceMemoryMode()){
			gbc.anchor = GridBagConstraints.WEST;
		    add(comboBox, gbc);
		    gbc.anchor = GridBagConstraints.LINE_END;
		}else{
			add(subField, gbc);
		}

		gbc.gridy++;
		gbc.gridx = 0;
		add(new JLabel("TrialNumber" + ": "), gbc);
		gbc.gridx = 1;
		add(field[0], gbc);
		gbc.gridy++;
		gbc.gridx = 0;
		add(new JLabel("RoomNumber" + ": "), gbc);
		gbc.gridx = 1;
		add(field[1], gbc);
	}

	public void postprocess(){
		if(RMConstants.isReferenceMemoryMode()){
			comboBox.requestFocus();
		}else{
			subField.requestFocus();
		}
	}

	public void preprocess(){
		/*try{
			FileHandler fh = new FileHandler(FileManager.getInstance().getPath(FileManager.ReferencesDir) +sep+ "ExecuterLog123.txt",102400,1);
			fh.setFormatter(new SimpleFormatter());
		    log.addHandler(fh);
		}catch(Exception e){
			e.printStackTrace();
		}*/
		if(RMConstants.isReferenceMemoryMode() && isFirstTrial){
		    try{
			    List<String> mouseID = new RMReferenceManager(FileManager.getInstance().getPath(FileManager.referencePath)).getIDs();
	            for(Iterator<String> it = mouseID.iterator();it.hasNext();){
			        comboBox.addItem(it.next());
			    }
	            isFirstTrial = false;
			    //log.log(Level.INFO,mouseID.size()+"");
		    }catch(Exception e){
			    e.printStackTrace();
			    //log.log(Level.INFO, e.toString());
		    }
		}
	}

	public void load(Properties properties){}

	public boolean canGoNext() {
		String[] subject = new String[1];
		if(RMConstants.isReferenceMemoryMode())
			subject[0] = comboBox.getSelectedItem().toString()+ "-" + field[0].getText()+ "-" +field[1].getText();
		else
			subject[0] = subField.getText()+ "-" + field[0].getText()+ "-" +field[1].getText();

		if(!FilenameValidator.validate(subject[0])){
			BehaviorDialog.showErrorDialog(this, "Invalid TrialName."+"\n"+"TrialName must not be empty, and must not contain : \n\\ / : * ? \" < > |");
			return false;
		}

		FileManager fm = FileManager.getInstance();

		fm.setSubjectID(subject);
		if(fm.subjectImageExist()){
			if(!BehaviorDialog.showWarningDialog(this, "The TrialName is already used."+"\n"+"Overwrite?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}
		manager.setSubjectID(subject);
		if(RMConstants.isReferenceMemoryMode())
		    RMConstants.setMouseID(comboBox.getSelectedItem().toString());

		boolean[] isMiceExist = new boolean[1];
	    Arrays.fill(isMiceExist, true);
		manager.setExistMice(isMiceExist);
		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}
}