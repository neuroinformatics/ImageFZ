package behavior.setup.parameter;

import behavior.setup.parameter.variable.BTIntVariable;
import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;
import behavior.setup.parameter.variable.ThresholdBooleanVariable;

public class BTParameter extends Parameter {
	//�ǉ��p�����[�^�[�͎��BTanalyze.calcurate()�Ŏg��
	public static int goalArea;
	public static int movementCriterion;
	public static int goalLine;

	protected BTParameter(){}

	protected int setupNumber(int i) {
		// rate = 1�b������̃t���[����
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 3);
		// duration = ��������
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 90);

		//bin�͎g�p���Ȃ�
		//binDuration = i;
		//var[i++] = new IntVariable("bDuration", "bin duration(sec): ", 150);

		minSize = i;
		var[i++] = new IntVariable("subject.size.min","subject size - min(pixels): ", 50);

		maxSize = i;
		var[i++] = new IntVariable("subject.size.max","subject size - max(pixels): ", 999999);

		frameWidth = i;
		var[i++] = new BTIntVariable("frame.width", "frame size - width(cm): ", 100);

		frameHeight = i;
		var[i++] = new BTIntVariable("frame.height", "frame size - height(cm): ",	10);

		//�ȉ��ǉ�
        //�}�E�X�̈ړ�����������ȉ��������痧���~�܂����Ƃ݂Ȃ�
		movementCriterion = i;
		var[i++] = new DoubleVariable("movement.criterion", "movement Criterion(cm/sec): ", 1.5);

		goalLine = i;
		var[i++] = new BTIntVariable("goalLine", "GoalLine(cm)", 2);

		goalArea = i;
		var[i++] = new BTIntVariable("goalArea", "GoalArea(X-coordinate)", 0);

		return i;
	}

    protected int setupCheckbox(int i){
		//OnlineExecuter.setWindow()

		invertMode = i;
     	var[i++] = new ThresholdBooleanVariable("mode.invert", "invert mode", false);
		return i;
	}

    @Override
	protected int setupSubCheckbox(int i) {
		return i;
	}

    @Override
	protected int setupSubNumber(int i) {
		return i;
	}
}