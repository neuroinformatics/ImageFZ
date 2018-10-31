package behavior.plugin.executer;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import behavior.setup.dialog.ReferenceDialogPanel;
import behavior.util.FilenameValidator;
import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJComboBox;
import behavior.io.FileCreate;
import behavior.plugin.executer.ReferenceSettingFrame;

public class FZReferenceSettingExecuter extends ReferenceSettingFrame {
	private String projectID;
	private String sep = System.getProperty("file.separator");
	private String programPath = (IJ.isWindows() ? "" : System.getProperty("user.home")) + System.getProperty("file.separator") + "Image_FZ";
	private String referencePath;
	private String referenceFileName;

	public FZReferenceSettingExecuter(){
		super();
		setTitle("FZ Reference Setup");
	}

	@Override
	protected List<ReferenceDialogPanel> addDialogs(List<ReferenceDialogPanel> dialogs){
		dialogs.add(new ProjectDialog());
		dialogs.add(new ReferenceDialog());
		dialogs.add(new SetupDialog());

		return dialogs;
	}

	@Override
	protected void save(List<ReferenceDialogPanel> dialogs){
		dialogs.get(dialogs.size()-1).save();
	}

    /**
     * Step 1 : 対象となるプロジェクトの設定
     */
    private class ProjectDialog extends ReferenceDialogPanel implements ActionListener{
    	private final String defaultProject = "Input Project Name";
        private ExtendedJComboBox projectList;

    	public ProjectDialog(){
    		setLayout(new GridBagLayout());
    		JLabel label = new JLabel("Project ID : ");
    		label.setHorizontalAlignment(JLabel.RIGHT);

    		projectList = new ExtendedJComboBox();
    		File root = new File(programPath);
    		File[] list = root.listFiles();
    		if(list != null)
    			for(int i = 0; i < list.length; i++)
    				if(list[i].isDirectory())
    					projectList.addItem(list[i].getName());
    	    projectList.setSelectedItem(defaultProject);
    	    projectList.getEditor().addActionListener(this);
    	    
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
    	
    	public void postprocess(){
    		projectList.requestFocus();
    	}
    	
    	public boolean canGoNext(){
            projectID = projectList.getText();
            
            if(!FilenameValidator.validate(projectID)){
    			BehaviorDialog.showErrorDialog(this, "Invalid Project ID.\nProject ID must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    			return false;
    		}
            
            String path = programPath+sep+projectID+sep+"References";
            File file = new File(path);
            if(file.exists()){
                return true;
            }
            if(BehaviorDialog.showQuestionDialog(this ,"No such Folder : "+projectID+sep+"References"+"\nCreate new file?")
            		== true){
            	file.mkdirs();
            	new File(path).mkdirs();
            	return true;
            }
            return false;
        }

		public void actionPerformed(ActionEvent arg0) {
			next.doClick();
		}
    }

    /**
     * Step 2 : 対象となるReferenceファイルの設定
     */
    private class ReferenceDialog extends ReferenceDialogPanel implements ActionListener{
    	
    	private ExtendedJComboBox referenceList;
    	
    	public void preprocess(){
        	removeAll();
        	
        	setLayout(new GridBagLayout());
    		JLabel label = new JLabel("Reference File : ");
    		label.setHorizontalAlignment(JLabel.RIGHT);
    		
    		referenceList = new ExtendedJComboBox();
    		File path = new File(programPath +sep+ projectID +sep+ "References");
    		File[] list = path.listFiles(new FileFilter(){
    			public boolean accept(File pathname){
    				String name = pathname.getName();
    				if(name.length()>=4 && name.substring(name.length()-4).equals(".txt"))	// .txt なファイルのみ受け付ける
    					return true;
    				else
    					return false;
    			}
    		});
    		for(int i = 0; i < list.length; i++)
    			referenceList.addItem(list[i].getName());

    	    if(list.length == 0)
    	    	referenceList.setSelectedItem("reference");

    	    referenceList.getEditor().addActionListener(this);
    	    
    	    GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.gridx = 0;
    	    gbc.gridy = 0;
    	    gbc.gridwidth = 1;
    	    gbc.gridheight = 1;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    add(label, gbc);
    	    gbc.gridx = 1;
    		add(referenceList, gbc);
        }
    	
    	public void postprocess(){
    		referenceList.requestFocus();
    	}
    	
    	public boolean canGoNext(){
    		referenceFileName = referenceList.getSelectedItem().toString();
    		//log.log(Level.INFO, referenceFileName);
    		if(!FilenameValidator.validate(referenceFileName)){
    			BehaviorDialog.showErrorDialog(this, "Invalid reference file name.\nReference file name must not be empty, and must not contain : \n\\ / : * ? \" < > |");
    			return false;
    		}
    		
    		if((referenceFileName.length()>4) && !referenceFileName.substring(referenceFileName.length()-4).equals(".txt")){
    			referenceFileName += ".txt";
    		}else if(referenceFileName.length()<=4){
    			referenceFileName += ".txt";
    		}

    		referencePath = programPath+sep+projectID+sep+"References"+sep+referenceFileName;
            File file = new File(referencePath);
            if(file.exists()){
                return true;
            }
            if(BehaviorDialog.showQuestionDialog(this ,"Create new file?")
            		== true){
            	new FileCreate(referencePath).createNewFile();
            	return true;
            }
    		return false;
    	}

		public void actionPerformed(ActionEvent arg0) {
			next.doClick();
		}
    }

    /**
     * Step 3 : 実際のSetup
     */
    private class SetupDialog extends ReferenceDialogPanel{
    	private JTable table;
    	private DefaultTableModel tableModel;
    	private JButton up,down,add,delete;
    	
    	public void preprocess(){
    		removeAll();

    		new FileCreate().createFile(referencePath);

    		setLayout(new BorderLayout());

    		// 編集するファイル名の表示
    		JPanel filePanel = new JPanel(new GridBagLayout());
    		GridBagConstraints gbc = new GridBagConstraints();
    	    gbc.gridx = 0;
    	    gbc.gridy = 0;
    	    gbc.gridwidth = 1;
    	    gbc.gridheight = 1;
    	    gbc.weighty = 1.0;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    
    	    JLabel projectLabel = new JLabel("Project : ");
    		JLabel projectName = new JLabel(projectID);
    		JLabel referenceLabel = new JLabel("Reference : ");
    		JLabel referenceName = new JLabel(referenceFileName);
    		// ボタン類
    		JPanel buttonPanel2 = new JPanel(new GridLayout(4, 1, 5, 20));
    		up = new JButton("Up");
    		down = new JButton("Down");
    		add = new JButton("Add");
    		delete = new JButton("Delete");
    		up.addActionListener(new UpActionListener());
    		down.addActionListener(new DownActionListener());
    		add.addActionListener(new AddActionListener());
    		delete.addActionListener(new DeleteActionListener());

    		// 編集用のテーブル
    		JComboBox mode = new JComboBox(new String[] {"Tone", "Shock"});
    		JTextField a = new JTextField("", 3);
    		a.setHorizontalAlignment(JLabel.RIGHT);
    		JTextField b = new JTextField("", 3);
    		b.setHorizontalAlignment(JLabel.RIGHT);

    		tableModel = createTableModel();
    		table = new JTable(tableModel);
    		table.getTableHeader().setReorderingAllowed(false);
    		table.putClientProperty("terminateEditOnFocusLost", true);
    		table.getColumn("Mode").setCellEditor(new DefaultCellEditor(mode));
    		table.getColumn("StartTime").setCellEditor(new DefaultCellEditor(a));
    		table.getColumn("Duration").setCellEditor(new DefaultCellEditor(b));
    		JScrollPane sp = new JScrollPane(table);
    		
    		// 作成
    	    filePanel.add(projectLabel, gbc);
    	    gbc.gridx = 1;
    	    gbc.anchor = GridBagConstraints.LINE_START;
    	    filePanel.add(projectName, gbc);
    	    gbc.gridx = 0;
    	    gbc.gridy = 1;
    	    gbc.anchor = GridBagConstraints.LINE_END;
    	    filePanel.add(referenceLabel, gbc);
    	    gbc.gridx = 1;
    	    gbc.anchor = GridBagConstraints.LINE_START;
    	    filePanel.add(referenceName, gbc);
    	    
    	    buttonPanel2.add(up);
    	    buttonPanel2.add(down);
    	    buttonPanel2.add(add);
    	    buttonPanel2.add(delete);
    	    
    	    add(filePanel, BorderLayout.NORTH);
    	    add(buttonPanel2, BorderLayout.EAST);
    	    add(sp, BorderLayout.CENTER);
    	}
    	
    	public void postprocess(){
    		next.requestFocus();
    	}
    	
    	/**
    	 * reference を読み込んで TableModel を作成
    	 */
    	private DefaultTableModel createTableModel(){
    		String[] column = {"Mode", "StartTime", "Duration"};
    		DefaultTableModel tableModel = new DefaultTableModel(column, 0);

    		try{
    			BufferedReader br = new BufferedReader(new FileReader(referencePath));
    			String line = "";
    			while((line=br.readLine())!=null){
    				if(line.startsWith("#"))
    				     continue;
    				
    				line = line.trim();
    				String[] row = new String[3];
    				String[] str = line.split("\t");
    				if(str.length==3){
    					if(str[0].equals("t")){
    						row[0] = "Tone";
    					}else if(str[0].equals("s")){
    						row[0] = "Shock";
    					}else{
    						br.close();
    						throw new IllegalStateException();
    					}
    				    row[1] = str[1];
    				    row[2] = str[2];
    				    tableModel.addRow(row);
    				}
    			}
    			br.close();
			}catch(Exception e) {
    			e.printStackTrace();
    		}
    		
    		return tableModel;
    	}
    	
    	/**
    	 * 選択された行を上へ
    	 */
    	private class UpActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow()>0){
					tableModel.moveRow(table.getSelectedRow(), table.getSelectedRow(), table.getSelectedRow() - 1);
					table.setRowSelectionInterval(table.getSelectedRow()-1, table.getSelectedRow()-1);
				}
			}
    	}
 
    	/**
    	 * 選択された行を下へ
    	 */
    	private class DownActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow()>=0 && table.getSelectedRow() < table.getRowCount()-1){
					tableModel.moveRow(table.getSelectedRow(), table.getSelectedRow(), table.getSelectedRow()+1);
					table.setRowSelectionInterval(table.getSelectedRow()+1, table.getSelectedRow()+1);
				}
			}
    	}

    	/**
    	 * 行を追加
    	 */
    	private class AddActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e){
				String[] row = new String[3];
				row[0] = "Tone";
				row[1] = "";
				row[2] = "";
				tableModel.addRow(row);
				table.setRowSelectionInterval(table.getRowCount()-1, table.getRowCount()-1);
			}
    	}

    	/**
    	 * 選択された行を全て削除
    	 */
    	private class DeleteActionListener implements ActionListener{
			public void actionPerformed(ActionEvent e) {
				int select = -1;
				while(table.getSelectedRow() >= 0){
					select = table.getSelectedRow();
					tableModel.removeRow(select);
				}
				
				// 削除後の行の選択
				if(select >= 0 && select < table.getRowCount())
					table.setRowSelectionInterval(select, select);
				else if(--select >= 0 && select < table.getRowCount())
					table.setRowSelectionInterval(select, select);
			}
    	}
    	
    	/**
    	 *　入力されたデータを書き出す
    	 */
    	public void save(){
			FileCreate writer = new FileCreate(referencePath);
			StringBuilder buf = new StringBuilder();
			for(int i=0; i<tableModel.getRowCount(); i++){
				String[] row = new String[3];
				if(tableModel.getValueAt(i,0).toString().equals("Tone")){
					row[0] = "t";
				}else if(tableModel.getValueAt(i,0).toString().equals("Shock")){
					row[0]= "s";
				}
				row[1] = tableModel.getValueAt(i,1).toString();
				row[2] = tableModel.getValueAt(i,2).toString();

				if(row[0]==null||row[1]==null||row[2]==null){
					BehaviorDialog.showMessageDialog(this, "Empty Data is exist");
                    return;
                }

				try{
					Integer.parseInt(row[1]+row[2]);
				}catch(NumberFormatException e){
					BehaviorDialog.showMessageDialog(this, "StartTime and Duration must be integer.");
                    return;
				}
				buf.append(row[0]).append("\t").append(row[1].toString()).append("\t").append(row[2].toString());
				if(!(i==tableModel.getRowCount()-1))
					buf.append(System.getProperty("line.separator"));
			}
			writer.write(buf.toString(), false);
   			BehaviorDialog.showMessageDialog(this, "Created the reference file: " + referencePath);
     	}

    	public boolean canGoNext(){
    		if(tableModel.getRowCount() == 0){
    			BehaviorDialog.showErrorDialog(this, "Data table is empty.");
    			return false;
    		}

    		return true;
    	}
    }
}