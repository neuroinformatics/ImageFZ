package behavior.controller;


import com.teravation.labjack.*;

import ij.*;

import behavior.controller.OutputController;

class LabJackOutput extends AbstractOutput{
	private LabJack lj;
	private LabJack[] labjacks;
	private int outputType;

	LabJackOutput(int outputType) throws Exception{
		this.outputType = outputType;
		try{
			labjacks = new LabJackFactory().getLabJacks();
			if(labjacks == null || labjacks.length == 0){
				IJ.showMessage("Error: could not detect labjack");
				throw new Exception(); //OutputController に知らせる
			}else{
				lj = labjacks[0];
			}
		}catch(Throwable e){
			IJ.showMessage("Error: set up labjack");
			throw new Exception(); //OutputController に知らせる
		}
		try{
			for(int i=0;i<4;i++){
				if(outputType == OutputController.LD_TYPE)
					lj.setDForOutput(i);
				else
					lj.setIOForOutput(i);
			}
		}catch(LabJackException e){
			IJ.showMessage("Error: set up LabJack");
			throw new Exception(); //OutputController に知らせる
		}
	}

	/**device のオープン
	 *@return 失敗したら true
	 */
	boolean open(){
		return false;
	}

	void controlOutput(int bits){
		control(bits, true);
	}

	void clear(int bits){
		control(bits, false);
	}

	private void control(int bits, boolean open){
		if(bitsCheck(bits, 3)){
			if(outputType == OutputController.NORMAL_TYPE)
				controlIO(3, open);
			else if(outputType == OutputController.LD_TYPE)
				controlD(3, open);
		}
		if(bitsCheck(bits, 2)){
			if(outputType == OutputController.NORMAL_TYPE)
				controlIO(2, open);
			else if(outputType == OutputController.LD_TYPE)
				controlD(2, open);
		}
		if(bitsCheck(bits, 1)){
			if(outputType == OutputController.NORMAL_TYPE)
				controlIO(1, open);
			else if(outputType == OutputController.LD_TYPE)
				controlD(1, open);
		}
		if(bitsCheck(bits, 0)){
			if(outputType == OutputController.NORMAL_TYPE)
				controlIO(0, open);
			else if(outputType == OutputController.LD_TYPE)
				controlD(0, open);
		}
	}

	private void controlD(int channel, boolean open){
		try{
			lj.setD(channel, open);
			lj.updateD(channel);
		}catch(LabJackException e){
			IJ.showMessage("Error: clear LabJack");
		}
	}

	private void controlIO(int channel, boolean open){
		try{
			lj.setIO(channel, open);
			lj.updateIO(channel);
		}catch(LabJackException e){
			IJ.showMessage("Error: clear LabJack");
		}
	}

	void close(){
	}

	private boolean bitsCheck(int bits, int portNo){	//bitをport number　になおす。もっと簡単にならないか？
		if(bits >= 8){
			if(portNo == 3){
				return true;
			}
			bits -= 8;
		}
		if(bits >= 4){
			if(portNo == 2){
				return true;
			}
			bits -= 4;
		}
		if(bits >= 2){
			if(portNo == 1){
				return true;
			}
			bits -= 2;
		}
		if(bits >= 1){
			if(portNo == 0){
				return true;
			}
			bits -= 1;
		}
		return false;
	}
}
