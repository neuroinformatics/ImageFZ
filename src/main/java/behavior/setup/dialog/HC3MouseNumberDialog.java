package behavior.setup.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JLabel;

import behavior.gui.BehaviorDialog;
import behavior.gui.ExtendedJTextField;
import behavior.io.FileManager;

/**
 * HC3 で各chamberのマウスの数を入力する．
 */
public class HC3MouseNumberDialog extends AbstractDialogPanel implements ActionListener {

	private int allCage;
	private ExtendedJTextField[] field;

	public String getDialogName(){
		return "Mouse Number";
	}

	public HC3MouseNumberDialog(DialogManager manager, int allCage) {
		super(manager);
		this.allCage = allCage;
	}

	public void preprocess(){
		FileManager fm = FileManager.getInstance();
		String path = fm.getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "mouseNumber.properties";
		Properties prefs = new Properties();
		int[] mouseNumber = new int[allCage];

		// 保存されているパラメータの読み込み
		try{
			FileInputStream fis = new FileInputStream(path);
			prefs.load(fis);
			fis.close();
		}catch(Exception e){
		}

		String[] subjectID = manager.getSubjectID();
		String defaultNumber = "4";
		for(int cage = 0; cage < allCage; cage++)
			mouseNumber[cage] = Integer.parseInt(prefs.getProperty(subjectID[cage], defaultNumber));

		// ダイアログの作成
		removeAll();
		setLayout(new GridBagLayout());
		field = new ExtendedJTextField[allCage];
		for(int i = 0; i < field.length; i++){
			field[i] = new ExtendedJTextField(Integer.toString(mouseNumber[i]), 2);
			field[i].addActionListener(this);
		}

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 1;
		gbc.gridheight = 1;
		gbc.insets = new Insets(10, 0, 10, 0);
		gbc.anchor = GridBagConstraints.LINE_END;

		for(int i = 0; i < field.length; i++){
			gbc.gridx = 0;
			add(new JLabel(subjectID[i] + ": "), gbc);
			gbc.gridx = 1;
			add(field[i], gbc);
			gbc.gridy++;
		}
	}

	public void postprocess(){
		this.getComponent(1).requestFocus();
	}

	public void load(Properties properties) {
	}

	public boolean canGoNext(){
		Properties prefs = new Properties();
		String[] subjectID = manager.getSubjectID();
		try{
			for(int i = 0; i < allCage; i++){
				Integer.parseInt(field[i].getText());	// チェック
				prefs.setProperty(subjectID[i], field[i].getText());
			}

			// パラメータの保存
			FileManager fm = FileManager.getInstance();
			String path = fm.getPath(FileManager.PreferenceDir) + System.getProperty("file.separator") + "mouseNumber.properties";
			FileOutputStream fos = new FileOutputStream(path);
			prefs.store(fos, null);
			fos.close();
		} catch(IOException e){
			BehaviorDialog.showErrorDialog(this, "I/O Error!!");
			return false;
		} catch(Exception e){
			BehaviorDialog.showErrorDialog(this, "Invalid input!!");
			return false;
		}

		return true;
	}

	public void actionPerformed(ActionEvent arg0) {
		manager.getNextButton().doClick();
	}
}
