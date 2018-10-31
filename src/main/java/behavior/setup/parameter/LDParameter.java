package behavior.setup.parameter;

import behavior.setup.parameter.variable.IntVariable;

public class LDParameter extends Parameter{
	public static int doorOpen;

	protected LDParameter(){}

	protected int setupSubNumber(int i){
		doorOpen = i;
		var[i++] = new IntVariable("open.door", "door open(sec): ", 1);
		return i;
	}

	protected int setupSubCheckbox(int i){
		return i;
	}
}







