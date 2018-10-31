package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

public class CSISubjectDialogPanel extends AbstractDialogPanel implements ActionListener{
	private JComboBox comboBox;
	private boolean isFirstTrial = true;
	private JTextField leftID= new JTextField("");
	private JTextField rightID= new JTextField("");

	//private Logger log = Logger.getLogger("behavior.plugin.executer.RMSubjectDialogPanel");
	public String getDialogName(){
		return "TrialName";
	}

	public CSISubjectDialogPanel(DialogManager manager){
		super(manager);

		isFirstTrial = true;

		setLayout(new GridBagLayout());

		comboBox = new JComboBox();
		comboBox.setPreferredSize(new Dimension(220,25));
		comboBox.addPopupMenuListener(new MinWidthPopupMenuListener());
		comboBox.addActionListener(this);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 0);

		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.WEST;
		add(new JLabel("SubjectID" + ": "), gbc);
		gbc.gridy++;
		gbc.gridwidth = 3;
		gbc.anchor = GridBagConstraints.LINE_END;
	    add(comboBox, gbc);
	    gbc.gridwidth = 1;
	    gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.EAST;
	    add(new JLabel("LeftCageID :"),gbc);
	    gbc.gridx = 1;
	    gbc.anchor = GridBagConstraints.WEST;
	    add(leftID,gbc);
	    gbc.gridx = 0;
		gbc.gridy++;
		gbc.anchor = GridBagConstraints.EAST;
	    add(new JLabel("RightCageID :"),gbc);
	    gbc.gridx = 1;
	    gbc.anchor = GridBagConstraints.WEST;
	    add(rightID,gbc);
	}

	public void postprocess(){
		comboBox.requestFocus();
	}

	public void preprocess(){
		if(isFirstTrial){
			leftID.setPreferredSize(new Dimension(100,18));
			rightID.setPreferredSize(new Dimension(100,18));
		    try{
		    	boolean first = true;
		    	String path=FileManager.getInstance().getPath(FileManager.referencePath);
		    	BufferedReader reader = new BufferedReader(new FileReader(path));
		    	String line;
				while((line = reader.readLine()) != null){
					line.trim();
					String[] buf = line.split("\t");
			        comboBox.addItem(buf[0]);
			        if(first && buf.length == 3){
			        	leftID.setEditable(true);
			        	rightID.setEditable(true);
			        	leftID.setText(buf[1]);
			        	rightID.setText(buf[2]);
			        	leftID.setEditable(false);
			        	rightID.setEditable(false);
			        	first = false;
			        }
			    }
				reader.close();
	            isFirstTrial = false;
		    }catch(Exception e){
			    e.printStackTrace();
		    }
		}
	}

	public void load(Properties properties){}

	public boolean canGoNext() {
		String[] subject = new String[1];
		subject[0] = comboBox.getSelectedItem().toString();

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

		boolean exist = false;
		try{
			BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.referencePath)));
			String bufID;
			while((bufID = reader.readLine()) != null){
				bufID.trim();
				String[] subID = bufID.split("\t");
				if(subID.length == 3 && subID[0].equals(subject[0])){
				    ((CSIDialogManager)manager).setOnlineSubCageIDs(subID[1],subID[2]);
				    exist=true;
				    break;
			    }
			}
			reader.close();
		}catch(Exception e){
		    BehaviorDialog.showErrorDialog(this, "Can't load file : " + fm.getPath(FileManager.referencePath));
		    return false;
		}

		if(!exist){
		    BehaviorDialog.showErrorDialog(this, "Can't find CageIDs in : " + fm.getPath(FileManager.referencePath));
	        return false;
		}

		boolean[] isMiceExist = new boolean[1];
	    Arrays.fill(isMiceExist, true);
		manager.setExistMice(isMiceExist);
		return true;
	}

	public void actionPerformed(ActionEvent arg0){
		if(arg0.getSource() == comboBox){
			try{
				BufferedReader reader = new BufferedReader(new FileReader(FileManager.getInstance().getPath(FileManager.referencePath)));
				String bufID;
				while((bufID = reader.readLine()) != null){
					bufID.trim();
					String[] subID = bufID.split("\t");
					if(subID.length == 3 && subID[0].equals(comboBox.getSelectedItem().toString())){
						leftID.setEditable(true);
			        	rightID.setEditable(true);
						leftID.setText(subID[1]);
			        	rightID.setText(subID[2]);
			        	leftID.setEditable(false);
			        	rightID.setEditable(false);
					    break;
				    }
				}
				reader.close();
			}catch(Exception e){
			    return;
			}
		}else{
      		manager.getNextButton().doClick();
		}
	}

	class MinWidthPopupMenuListener implements PopupMenuListener{
		  private static final int POPUP_MIN_WIDTH = 320;
		  private boolean adjusting = false;
		  public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
		    JComboBox combo = (JComboBox)e.getSource();
		    Dimension size  = combo.getSize();
		    if(size.width>=POPUP_MIN_WIDTH) return;
		    if(!adjusting) {
		      adjusting = true;
		      combo.setSize(POPUP_MIN_WIDTH, size.height);
		      combo.showPopup();
		    }
		    combo.setSize(size);
		    adjusting = false;
		  }
		  public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
		  public void popupMenuCanceled(PopupMenuEvent e) {}
	}
}