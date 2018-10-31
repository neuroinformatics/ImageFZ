package behavior.setup.parameter;

import behavior.setup.parameter.variable.BTIntVariable;
import behavior.setup.parameter.variable.CSIDoubleVariable;
import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class OldCSIParameter extends CSIParameter{
	public static int conDist;
	public static int wait;

	protected OldCSIParameter(){}

	@Override
	protected int setupSubNumber(int i){
		conDist = i;
		var[i++] = new CSIDoubleVariable("distance.contact","",UNSET);
		leftSeparator = i;
		var[i++] = new BTIntVariable("separator.left","",UNSET);
		rightSeparator = i;
		var[i++] = new BTIntVariable("separator.right","",UNSET);
		movementCriterion = i;
		var[i++] = new DoubleVariable("moving.criterion", "movement Criterion(cm/s): ", 1.5);
		mountDensity = i;
		var[i++] = new IntVariable("mountgraph.value","mountgraph.value:",1);

		wait = i;
		var[i++] = new BTIntVariable("wait", "Wait(ms)",0);

		return i;
	}
}