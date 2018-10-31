package behavior.setup;

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import ij.process.ImageProcessor;
import behavior.io.FileCreate;
import behavior.io.FileManager;
import behavior.setup.dialog.*;
import behavior.setup.parameter.*;

/**
 * 新セットアップ。
 * TMazeで開発したものをこちらにも適用。
 * 今までのもの(IJのGUI機能を用いたもの)ではThresholdの設定がやりづらいとの指摘があったので、
 * カメラ関係の機能、その他細かな機能を加えました。
 *
 * @author Modifier:Butoh
 */
public class Setup {
	/*オンライン、オフラインを指定するフィールド*/
	public static final int ONLINE = 1;
	public static final int OFFLINE = 2;

	protected DialogManager manager;

	public Setup(final Program program,final int type,final int allCage){
		Parameter.initialize(program);
		// 各プログラムで利用する DialogManager はここで指定．

		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
			        switch(program){
			        case FZS:
				    case FZ:    manager = new FZDialogManager(program, type, allCage);  break;
				    case LD:	manager = new LDDialogManager(program, type, allCage);	break;
				    case CSI:	manager = new CSIDialogManager(program, type);          break;
				    case EP:    manager = new EPDialogManager(program, type, allCage);  break;
				    case RM:    manager = new RMDialogManager(program, type);  break;
				    case YM:    manager = new YMDialogManager(program, type);  break;
				    case BT:    manager = new BTDialogManager(program, type, allCage);  break;
				    case OLDCSI:manager = new CSIOldDialogManager(program, type);       break;
				    case HC1:	manager = new HCDialogManager(program, type, allCage);	break;
				    case HC2:	manager = new HCDialogManager(program, type, allCage);	break;
				    case HC3:	manager = new HC3DialogManager(program, type, allCage);	break;
				    case BM:	manager = new BMDialogManager(program, type, allCage); 	break;
				    case TM:	manager = new TMDialogManager(program, type); 	break;
				    default: 	manager = new DialogManager(program, type, allCage);	break;
			        }
				}
			});
		}catch(InterruptedException e) {
			e.printStackTrace();
		}catch(InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public boolean setup(){
	manager.setModifiedParameter(false);
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
			        manager.showAllDialog();
				}
			});
		}catch(InterruptedException e1){
			e1.printStackTrace();
		}catch(InvocationTargetException e1){
			e1.printStackTrace();
		}

		while(manager.isVisible()){ // 設定ダイアログの表示中はループ
			try{
				Thread.sleep(100);
			} catch(Exception e){
				e.printStackTrace();
			}
		}

		return !manager.isComplete();
	}

	public boolean reSetup(){
	manager.setModifiedParameter(false);
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				public void run(){
					manager.showSubDialog();
				}
			});
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			e1.printStackTrace();
		}

		while(manager.isVisible()){ // 設定ダイアログの表示中はループ
			try{
				Thread.sleep(100);
			} catch(Exception e){
				e.printStackTrace();
			}
		}

		return !manager.isComplete();
	}

	public boolean isModifiedParameter(){
		return manager.isModifiedParameter();
	}

	/**
	 * session ファイルを書き出す。
	 */
	public void saveSession(){
		String[] subjectID = manager.getSubjectID();
		if(manager.isFirstTrial())
			(new FileCreate()).createNewFile(FileManager.getInstance().getPath(FileManager.sessionPath));
		for(int cage = 0; cage < subjectID.length; cage++)
			(new FileCreate()).write(FileManager.getInstance().getPath(FileManager.sessionPath), subjectID[cage], true);
	}

    public boolean isFirstTrial(){
    	return manager.isFirstTrial();
    }

	public String getSessionID(){
		return manager.getSessionID();
	}

	public String[] getSubjectID(){
		return manager.getSubjectID();
	}

	public ImageProcessor getBackIp(){
		return manager.getBackIp();
	}

	public boolean[] getExistMice(){
		return manager.getExistMice();
	}
}
