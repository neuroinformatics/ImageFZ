package behavior.plugin;

import behavior.setup.Program;

public class SetCageFieldCSI extends SetCageField {
	public SetCageFieldCSI(){
		super(Program.CSI);
	}

	protected void setup(){
		Chamber = new String[]{"1","2","3","4"};	
	}
}
