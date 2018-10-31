package behavior.setup.parameter;

import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class OFParameter extends Parameter{
	public static int centerArea;
	public static int partitionHorizontal;
	public static int partitionVertical;
	public static int movementCriterion;
	public static int saveImage;

	protected OFParameter(){}

	@Override
	protected int setupSubNumber(int i){
		centerArea = i;
		var[i++] = new IntVariable("center.area", "center.area(%): ", 100);
		partitionHorizontal = i;
		var[i++] = new IntVariable("partition.no.horizontal", "partition no(horizontal): ", 3);
		partitionVertical = i;
		var[i++] = new IntVariable("partition.no.vertical", "partition no(vertical): ", 3);
		movementCriterion = i;
		var[i++] = new DoubleVariable("movement.criterion", "movement criterion(cm/sec): ", 1.0);
		return i;
	}

	@Override
	protected int setupSubCheckbox(int i){return i;}
}