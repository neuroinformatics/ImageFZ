package behavior.controller;

import ij.IJ;
import scion.fg.jsfg_fg;
import scion.fg.capture.fg_manager;
import scion.util.InfoDialog;
import scion.util.int_ref;

class ScionInput extends AbstractInput{

	private fg_manager fgm;
	private jsfg_fg fg;
	private int rvalue = 0;
	private int_ref refvalue = new int_ref();

	ScionInput(){
		resetAll();
	}

	@Override
	boolean getInput(int channel){
		String classPath = System.getProperty("java.class.path");

		if(classPath.indexOf("scion") < 0){
			scion_alert();
			return false;
		}

		fgm = new fg_manager(IJ.getInstance());
		if(fgm.openFG(false) != fg_manager.success)
			return false;

		fg = fgm.getFG();
		rvalue = fg.get_port(0,refvalue);
		rvalue = refvalue.value & 15;
		System.err.println(rvalue);
		if(rvalue != 0){
			resetAll();
			close();
		}

		if(rvalue==0){
			return false;
		}else{
			if(rvalue==channel){
			    return true;
			}
		}

		return false;
	}

	void clear(int value){
		fg.clr_port_bits(0,value);
	}

	void reset(){
		OutputController output = OutputController.getInstance();//アウトプット制御クラス
		output.setup();
		IJ.wait(5);
		output.controlOutput(1);
		IJ.wait(5);
		output.controlOutput(0);
		IJ.wait(5);
		output.close();
	}

	void resetAll(){
		// 値をリセットする
		OutputController output = OutputController.getInstance();//アウトプット制御クラス
		output.setup();
		output.clear(15);

		for (int i=1;i<=9;i++){
			output.controlOutput(2);
			IJ.wait(5);
			output.controlOutput(0);
			IJ.wait(5);
		}
		for (int i=1;i<=9;i++){
			output.controlOutput(1);
			IJ.wait(5);
			output.controlOutput(0);
			IJ.wait(5);
		}
		
		//　入力受付可能にする
		for(int i = 0; i < 4; i++)
			output.clear(1 << i);
	}


	void close(){
		fgm.closeFG();
	}

	private void scion_alert(){
		new InfoDialog(IJ.getInstance(), "Scion Java Alert","Plugin requires the Scion Frame Grabber Java Package\n"+"to be installed on your computer.",true);
	}
}
