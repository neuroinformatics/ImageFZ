package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

/**
 * SubjectID を設定。
 */
public class BMSubjectDialogPanel extends AbstractDialogPanel implements ActionListener {
	private ExtendedJTextField field;
	private JComboBox referenceList;
	private JComboBox subjectIDList;
	private boolean onceCalled = true;
	private boolean isFirstTime = true;
	private int[] target = new int[200];
	private final String sep = System.getProperty("file.separator");

	public String getDialogName(){
		return "Subject ID";
	}

	public BMSubjectDialogPanel(DialogManager manager, int allCage){
		super(manager);
		
	}

	public void postprocess(){
		if (isFirstTime) {	//２回目以降にもう一度実行されると表示がおかしくなる
			setLayout(new GridBagLayout());
			field = new ExtendedJTextField("1", 5);
			field.addActionListener(this);
			//createReferenceList()ではprojectIDがセットされている必要があるため、postprocessにまわした
			referenceList = createReferenceList();
			referenceList.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e){
					if (onceCalled)	//２回呼ばれるようなので
						referenceChanged();
					onceCalled = !onceCalled;
				}
			});
			subjectIDList = createSubjectIDList();
	
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx = 0;
			gbc.gridy = 0;
			gbc.gridwidth = 1;
			gbc.gridheight = 1;
			gbc.insets = new Insets(10, 0, 10, 0);
			gbc.anchor = GridBagConstraints.LINE_END;
	
			add(new JLabel("Reference File : "), gbc);
			gbc.gridx = 1;
			add(referenceList, gbc);
			gbc.gridy++;
			gbc.gridx = 0;
			add(new JLabel("SubjectID " + ": "), gbc);
			gbc.gridx = 1;
			add(subjectIDList, gbc);
			gbc.gridy++;
			gbc.gridx = 0;
			add(new JLabel("Trial Number " + ": "), gbc);
			gbc.gridx = 1;
			add(field, gbc);
			
			isFirstTime = false;
		}
		
		referenceList.requestFocus();
	}

	public void load(Properties properties) {
	}

	public boolean canGoNext() {
		String[] subject = new String[1];
		subject[0] = subjectIDList.getSelectedItem() + "-" + field.getText();
		if(!FilenameValidator.validate(subject[0])){
			BehaviorDialog.showErrorDialog(this, "Invalid Subject ID.\nSubject ID must not be empty, and must not contain : \n\\ / : * ? \" < > |");
			return false;
		}

		FileManager fm = FileManager.getInstance();

		fm.setSubjectID(subject);
		if(fm.subjectImageExist()){
			if(!BehaviorDialog.showWarningDialog(this, "The Subject ID and Trial Number is already used.\nOverwrite?", "Overwrite", BehaviorDialog.NO_OPTION))
				return false;
		}
		manager.setSubjectID(subject);

		BMSetCageDialogPanel.setTarget(target[subjectIDList.getSelectedIndex()]);

		boolean[] isMiceExist = new boolean[1];
	    Arrays.fill(isMiceExist, true);
		manager.setExistMice(isMiceExist);

		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}
	
	private JComboBox createReferenceList(){
		JComboBox referenceList = new JComboBox();
		
		File path = new File(FileManager.getInstance().getPath(FileManager.ReferencesDir));
		File[] list = path.listFiles(new FileFilter(){
			public boolean accept(File pathname){
				String path = pathname.getPath();
				if(path.substring(path.length() - 4).equals(".txt") /*&& BMReferenceValidator.validate(path)*/)
					return true;
				else
					return false;
			}
		});
		for(int i = 0; i < list.length; i++){
			referenceList.addItem(list[i].getName());
		}
		referenceList.setSelectedIndex(0);
		
		return referenceList;
	}
	
	private JComboBox createSubjectIDList(){
		int count = 0;
		JComboBox combo = new JComboBox();
		String filename = (String)referenceList.getSelectedItem();
		
		String subjectID = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(FileManager.getInstance().getPath(FileManager.ReferencesDir) + sep + filename)));
			String line = "";
			while((line = reader.readLine()) != null){
				if(!line.startsWith("#")){
					if (line.contains(" ")) {
						subjectID = line.substring(0, line.indexOf(" "));
						target[count++] = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
					} else if (line.contains("\t")) {
						subjectID = line.substring(0, line.indexOf("\t"));
						target[count++] = Integer.parseInt(line.substring(line.indexOf("\t") + 1));
					}
					combo.addItem(subjectID);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		return combo;
	}
	
	private void referenceChanged() {
		int count = 0;
		
		//前から消していくと変更前のものが残ることがある
		for (int i = subjectIDList.getItemCount() - 1; i >= 0; i--)
			subjectIDList.removeItemAt(i);
		
		
		String filename = (String)referenceList.getSelectedItem();
		String subjectID = null;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(FileManager.getInstance().getPath(FileManager.ReferencesDir) + sep + filename)));
			String line = "";
			while((line = reader.readLine()) != null){
				if(!line.startsWith("#")){
					if (line.contains(" ")) {
						subjectID = line.substring(0, line.indexOf(" "));
						target[count++] = Integer.parseInt(line.substring(line.indexOf(" ") + 1));
					} else if (line.contains("\t")) {
						subjectID = line.substring(0, line.indexOf("\t"));
						target[count++] = Integer.parseInt(line.substring(line.indexOf("\t") + 1));
					}
					subjectIDList.addItem(subjectID);
				}
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		repaint();
	}
}
