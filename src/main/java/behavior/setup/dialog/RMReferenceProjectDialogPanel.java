package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileFilter;
//import java.io.IOException;
//import java.util.List;
import java.util.Properties;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
//import behavior.io.RMReferenceManager;
import behavior.setup.Setup;
import behavior.util.FilenameValidator;
import behavior.util.rmconstants.RMConstants;
//import behavior.util.rmconstants.RMConstants;

/**
 * Project の名前を入力する。
 * ONLINE の場合は新規に作成するか既存のものを選択。
 * OFFLINE の場合は既存のものを選択、既存の Project がなければエラーを出して終了。
 */
public class RMReferenceProjectDialogPanel extends AbstractDialogPanel implements ActionListener{
	private JComboBox projectList;
	private int type;
	private final String sep = System.getProperty("file.separator");

	protected JCheckBox check;
	protected JTextField text;

	public String getDialogName(){
		return "Project ID";
	}

	public RMReferenceProjectDialogPanel(DialogManager manager, int type){
		super(manager);
		this.type = type;

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Project ID: ");
		label.setHorizontalAlignment(JLabel.RIGHT);

		projectList = new JComboBox();	// 入力可能な ComboBox
		projectList.setPreferredSize(new Dimension(160,25));

		File root = new File(FileManager.getInstance().getPath(FileManager.program));
		File[] list = root.listFiles();
		//if(!RMConstants.getOffline()){
		    if(list != null){
			    for(int i = 0; i < list.length; i++) {
				    String path = root+sep+list[i].getName()+sep+"References";
				    File file = new File(path);
				    if(list[i].isDirectory()&& file.exists()){
					    File[] referenceList = file.listFiles(new FileFilter(){
						    public boolean accept(File pathname){
							    String name = pathname.getName();
							    if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt なファイルのみ受け付ける
								    return true;
							    else
								    return false;
						    }
					    });

					    if(referenceList.length>0){
					        projectList.addItem(list[i].getName());
					    }
				    }			 
			    }
		    }

		    if(projectList.getItemCount() == 0){
			    if(!RMConstants.isOffline()){
			        BehaviorDialog.showErrorDialog("Can't find any " + manager.getProgram() + "projects for Reference." +
					                                 "\nPlease make Reference File before.");
		        }else{
		        	BehaviorDialog.showErrorDialog("Can't find any " + manager.getProgram() + "projects for Reference.");
			    }
			    manager.setEndFlag();
		    }
		/*}else{
			if(list != null){
				for(int i = 0; i < list.length; i++){
					File path = new File(root+sep+list[i]+sep+"Sessions");

					if(!path.exists())
						continue;

					File[] subList = path.listFiles(new FileFilter(){
						public boolean accept(File pathname){
							String name = pathname.getName();
							if(name.length() >= 4 && name.substring(name.length() - 4).equals(".txt"))	// .txt なファイルのみ受け付ける
								return true;
							else
								return false;
						}
					});

					if(subList==null)
						continue;

					for(int s=0; s< subList.length; s++){
						RMReferenceManager refManager = new RMReferenceManager(root+sep+list[i]+sep+"Sessions"+sep+ subList[s]);
						try{
						    List<String> subIDs = refManager.getIDsAndAlignment();

					    if(subIDs.size() == 0){
						    continue;
					    }

					    }catch(IOException e){
						    e.printStackTrace();
					    }
					    projectList.addItem(list[i].getName());
					}
				}

				if(projectList.getItemCount() == 0){
				    // OFFLINE で Project が存在しなければ終了。
				    BehaviorDialog.showErrorDialog("Can't find any " + Program.getProgramID(manager.getProgram()) + "projects for Reference.");
				    manager.setEndFlag();
			    }
			}
		}*/

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(label, gbc);
		gbc.gridx = 1;
		add(projectList, gbc);
	
		if(type == Setup.OFFLINE){
		check = new JCheckBox("Save file to another location",false);
	    check.addActionListener(this);
	    JLabel label3 = new JLabel("Enter the parent folder name:");
	    text = new JTextField();
	    text.setEnabled(false);

	    JPanel panel = new JPanel();
	    gbc.gridx = 0;
	    gbc.gridy++;
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		add(panel, gbc);
	    gbc.gridx = 0;
	    gbc.gridy++;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 4;
	    add(check, gbc);
	    gbc.gridy++;
	    add(label3, gbc);
	    gbc.gridy++;
	    add(text,gbc);
		}
	}

	public void postprocess(){
		projectList.requestFocus();
	}

	private String getProjectID() {
		return projectList.getSelectedItem().toString();
	}

	public void load(Properties properties){}

	public boolean canGoNext(){    
		String input = getProjectID();

		if(!FilenameValidator.validate(input)){
			BehaviorDialog.showErrorDialog(this, "Invalid Project ID.\nProject ID must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			return false;
		}

		FileManager.getInstance().setProjectID(input);
		File file = new File(FileManager.getInstance().getPath(FileManager.project));
		try{
		    fileCheck(file);
		}catch(IllegalStateException e){
			e.printStackTrace();
		}

		FileManager.getInstance().makeDirectory(manager.getProgram());

		if(type == Setup.OFFLINE){
			File path = new File(FileManager.getInstance().getPath(FileManager.SessionsDir));
			File[] list = path.listFiles(new FileFilter(){
				public boolean accept(File pathname){
					String name = pathname.getName();
					if(name.length() >= 4 && name.substring(name.length()-4).equals(".txt"))	// .txt なファイルのみ受け付ける
						return true;
					else
						return false;
				}
			});
			if(list.length == 0){
				BehaviorDialog.showErrorDialog("Can't find any session.");
				return false;
			}
			if(check.isSelected()){
				if(!FilenameValidator.validate(text.getText())){
				    BehaviorDialog.showErrorDialog(this, "Invalid project name.\nproject name must not be empty, and must not contain: \n\\ / : * ? \" < > |");
				    return false;
			    }
				FileManager.getInstance().setSaveProjectID(text.getText());
			}else{
				FileManager.getInstance().setSaveProjectID(input);
			}
		}

		return true;
	}

	private void fileCheck(File file)throws IllegalStateException{
		if(!file.exists())
			throw new IllegalStateException();;
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(type == Setup.OFFLINE && e.getSource()==check){
			text.setEnabled(check.isSelected());
		}
		
	}
}