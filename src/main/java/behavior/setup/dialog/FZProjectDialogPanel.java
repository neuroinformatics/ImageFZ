package behavior.setup.dialog;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.FileFilter;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.io.FileManager;
import behavior.util.FilenameValidator;

public class FZProjectDialogPanel  extends AbstractDialogPanel{
	private JComboBox projectList;
	private final String sep = System.getProperty("file.separator");

	@Override
	public String getDialogName(){
		return "Project ID";
	}

	public FZProjectDialogPanel(DialogManager manager){
		super(manager);

		setLayout(new GridBagLayout());
		JLabel label = new JLabel("Project ID: ");
		label.setHorizontalAlignment(JLabel.RIGHT);

		projectList = new JComboBox();
		projectList.setPreferredSize(new Dimension(160,25));

		File root = new File(FileManager.getInstance().getPath(FileManager.program));
		File[] list = root.listFiles();
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
		    BehaviorDialog.showErrorDialog("Can't find any " + manager.getProgram() + "projects for Reference." +
			                               "\nPlease make Reference File before.");
		    manager.setEndFlag();
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.anchor = GridBagConstraints.LINE_END;
		add(label, gbc);
		gbc.gridx = 1;
		add(projectList, gbc);
	}

	private String getProjectID() {
		return projectList.getSelectedItem().toString();
	}

	@Override
	public void load(Properties properties){}

	@Override
	public boolean canGoNext(){    
		String input = getProjectID();

		if(!FilenameValidator.validate(input)){
			BehaviorDialog.showErrorDialog(this, "Invalid Project ID.\nProject ID must not be empty, and must not contain: \n\\ / : * ? \" < > |");
			return false;
		}

		FileManager.getInstance().setProjectID(input);
		File file = new File(FileManager.getInstance().getPath(FileManager.project));
		try{
			if(!file.exists())
				throw new IllegalStateException();
		}catch(IllegalStateException e){
			e.printStackTrace();
		}

		FileManager.getInstance().makeDirectory(manager.getProgram());

		return true;
	}
}