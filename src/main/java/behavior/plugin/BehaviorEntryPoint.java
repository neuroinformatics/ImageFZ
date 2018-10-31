package behavior.plugin;

import ij.plugin.PlugIn;
import javax.swing.UIManager;

/**
 * �S�v���O�����̎��s�O�ɋ��ʂ��Ď��s���������Ƃ��R���X�g���N�^�ɋL�q����D
 */
public abstract class BehaviorEntryPoint implements PlugIn {
	public BehaviorEntryPoint(){
		try{
			// Swing �̃f�t�H���g���� System �� Look&Feel �֕ύX
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}