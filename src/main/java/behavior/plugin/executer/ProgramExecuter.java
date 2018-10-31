package behavior.plugin.executer;

import java.awt.event.*;

import ij.IJ;

import behavior.image.ImageCapture;

public abstract class ProgramExecuter{
	protected ImageCapture imageCapture;
	protected boolean ONLINE;

	public ProgramExecuter(){}

	public void run(){
		initialize();
		
		/*****
		プログラムセットアップ。引数で指定されたプログラムに応じて、プロジェクト名
		パラメータ、サブジェクトIDなどの設定画面をユーザに提供する。
		 ******/
		if(setup()){
			if(ONLINE)
				imageCapture.close();
			return;
		}
		int trialNum = 1;
		while(true){
			IJ.setKeyUp(KeyEvent.VK_SHIFT);	//use these keys to interrupt the analysis.
			IJ.setKeyUp(KeyEvent.VK_ALT);
			if(run(trialNum)){
				if(ONLINE)
					imageCapture.close();
				break;
			}
			trialNum++;
		}
	}
	
	protected abstract boolean setup();

	protected boolean run(int trialNum){
		run();
		return false;
	}

	protected void initialize(){}
}