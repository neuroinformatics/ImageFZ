package behavior.plugin;

import ij.plugin.PlugIn;
import javax.swing.UIManager;

/**
 * 全プログラムの実行前に共通して実行したいことをコンストラクタに記述する．
 */
public abstract class BehaviorEntryPoint implements PlugIn {
	public BehaviorEntryPoint(){
		try{
			// Swing のデフォルトから System の Look&Feel へ変更
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}