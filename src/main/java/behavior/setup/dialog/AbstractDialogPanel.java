package behavior.setup.dialog;

import java.util.Properties;

import javax.swing.JPanel;



public abstract class AbstractDialogPanel extends JPanel {

	protected DialogManager manager;

	public AbstractDialogPanel(DialogManager manager){
		this.manager = manager;
	}

	/**
	 * Project ID を入力した時点でこのメソッドが呼ばれる。
	 * 一般に、その Project の　preference が読み込まれ、各ダイアログを初期化するために用いる。
	 */
	public abstract void load(Properties properties);

	/**
	 * 各ダイアログの名前（説明）を簡潔に与える。
	 */
	public abstract String getDialogName();

	/**
	 *  Nextを押された時に、validな入力がされているかを
	 *  checkしたい場合はオーバライドする
	 *  入力されたデータの反映もここに書く。	
	 */
	public boolean canGoNext(){
		return true;
	}

	/**
	 * 表示される直前に処理を行いたい場合に使う
	 */
	public void preprocess(){
	}

	/**
	 * 表示された直後に処理を行いたい場合
	 */
	public void postprocess(){
	}

	/**
     * backを押した場合に処理を行いたい場合
     */
	public void backprocess(){
	}

	/**
	 * Next,Back,Cancelに関わらず行いたい処理
	 */
	public void endprocess(){
	}
}
