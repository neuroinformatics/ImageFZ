package behavior.controller;

import com.teravation.labjack.LabJack;
import com.teravation.labjack.LabJackFactory;

import ij.*;

/**電気的アウトプットを管理するクラス。Labjack, Scion どちらにも対応
 */
public class OutputController {
	/**全チャネルであることを指定するフィールド*/
	public static final int ALL_CHANNEL = 15;
	/**各チャネルを指定するフィールド*/
	public static final int[] CHANNEL = {1, 2, 4, 8};

	/**アウトプットタイプを指定するフィールド(LD)*/
	public static final int LD_TYPE = 1;
	/**アウトプットタイプを指定するフィールド*/
	public static final int NORMAL_TYPE = 2;

	private AbstractOutput device = null;

	private static OutputController output;

	private OutputController(){}

	/**インスタンスを取得*/
	public static OutputController getInstance(){
		if(output == null)
			output = new OutputController();
		return output;
	}

	/**セットアップをする（通常のアウトプットタイプ）
	 *@return 失敗したら true
	 */
	public boolean setup(){
		return setup(NORMAL_TYPE);
	}

	/**セットアップをする
	 *@param outputType アウトプットタイプを指定するフィールド値
	 *@return 失敗したら true
	 */
	public boolean setup(int outputType){
		if(device != null) return false;

		String classPath = System.getProperty("java.class.path");
		try{
			if(classPath.indexOf("labjack") >= 0){
				LabJack[] labjacks = new LabJackFactory().getLabJacks();
				if(labjacks == null || labjacks.length == 0){
					IJ.showMessage("Error: could not detect labjack");
					return debug();
				}
				device = new LabJackOutput(outputType);
				return false;
			}else if(classPath.indexOf("scion") >= 0){
				device = new ScionOutput();
				return open();
			}else{
				IJ.showMessage("Not found any classPath for output device.");
				return debug();
			}
		}catch(Throwable e){
			IJ.showMessage("Not found any output device.");
			return debug();
		}
	}

	private boolean debug(){
		String classPath = System.getProperty("java.class.path");
		if(classPath.indexOf("debug") != 0){
			if(IJ.showMessageWithCancel("debug mode", "Do you use debug mode(output in console)?") == true){
				device = new DebugOutput();
				return false;
			}else{
				return true;
			}
		}else{
			return true;
		}
	}

	/**アウトプットデバイスの作動を開始
	 */
	private boolean open(){
		return device.open();
	}

	/**チャネルを初期化する。
	 *@param bits 初期化するチャネル
	 */
	public void clear(int bits){
		device.clear(bits);
	}

	/**チャネルからアウトプットを出す。
	 *@param bits アウトプットするチャネル
	 */
	public void controlOutput(int bits){
		device.controlOutput(bits);
	}

	/**アウトプットデバイスを終了する。
	 */
	public void close(){
		device.close();
	}
}