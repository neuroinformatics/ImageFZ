package behavior.controller;

import ij.*;

/**電気的 Input のコントロールを行うクラス。
 *Labjack, Scion どちらのデバイスでも、これを使用すれば問題ない。
 */
public class InputController{
	/**LabjackでポートAIを使用する*/
	public static final int PORT_AI = 0;
	/**LabjackでポートIOを使用する*/
	public static final int PORT_IO = 1;
	/**LabjackでポートDを使用する*/
	public static final int PORT_D = 2;

	private AbstractInput device;

	private boolean setupFailed = false;

	private static InputController ic;

	/**
	 *@param type フィールドにあるインプットタイプ
	 */
	private InputController(int type){
		String classPath = System.getProperty("java.class.path");
		try{
			if(classPath.indexOf("labjack") >= 0){
				device = new LabJackInput(type);
			}else if(classPath.indexOf("scion") >= 0){
				device = new ScionInput();
			}else{
				throw new Exception();
			}
		}catch(Throwable e){
			if(classPath.indexOf("debug") >= 0){
				if(IJ.showMessageWithCancel("debug mode", "start debug mode(use button dialog input)?") == true)
					device = new DebugInput();
				else
					setupFailed = true;
			}else{
				setupFailed = true;
			}
		}
	}

	public static InputController getInstance(int type){
		if(ic == null)
			ic = new InputController(type);
		return ic;

	}

	/**input device の設定に失敗したか。失敗したら true
	 */
	public boolean setupFailed(){
		return setupFailed;
	}

	//指定されたチャネルにインプットがあるかどうかを返却。
	public boolean getInput(int channel){
		return device.getInput(channel);
	}

	/**インプットチャネルを初期化
	 *@param value 初期化するチャネル番号(１～４)
	 */
	public void clrPortBits(int value){
		device.clear(value);
	}

	public void reset(){
		device.reset();
	}

	public void resetAll(){
		device.resetAll();
	}


	/**インプットデバイスを終了
	 */
	public void close(){
		device.close();
	}

	public AbstractInput getDevice(){
		return device;
	}

//	public void initialize(){
//	refvalue.initialize();
//	}

}
