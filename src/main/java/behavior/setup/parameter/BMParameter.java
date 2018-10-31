package behavior.setup.parameter;

import behavior.setup.parameter.variable.BMIntVariable;
import behavior.setup.parameter.variable.DoubleVariable;
import behavior.setup.parameter.variable.IntVariable;

public class BMParameter extends Parameter {	
	public static int movementCriterion;
	public static int armR;
	public static int innerR;//�����̉~�̔��a(pixel)
	public static int distOut;
	public static int outerR;//�O���̉~�̔��a(pixel)

	protected BMParameter(){}

	//Parameter.java�̕ϐ��̃f�t�H���g�l��ς��邽�߂����ɃI�[�o�[���C�g
	//�ϐ����̂ɕύX�͖���
	protected int setupNumber(int i){
		rate = i;
		var[i++] = new IntVariable("rate", "rate(frames/sec): ", 1);
		duration = i;
		var[i++] = new IntVariable("duration", "duration(sec): ", 300);
		binDuration = i;
		var[i++] = new IntVariable("bDuration", "bin duration(sec): ", 300);
		minSize = i;
		var[i++] = new IntVariable("subject.size.min", "subject size - min(pixels): ", 0);
		maxSize = i;
		var[i++] = new IntVariable("subject.size.max", "subject size - max(pixels): ", 999999);
		frameWidth = i;
		var[i++] = new IntVariable("frame.width", "frame size - width(cm): ", 100);
		frameHeight = i;
		var[i++] = new IntVariable("frame.height", "frame size - height(cm): ", 100);
		i = setupSubNumber(i);
		return i;
	}

	protected int setupSubNumber(int i){
//		Properties�t�@�C���ɕۑ�����ۂ̖��O�ƁA���[�U�[�ɕ\�����閼�O
		movementCriterion = i;
		var[i++]  = new DoubleVariable("movement.criterion","movement criterion(cm/sec): ",1.0);
		armR = i;
		var[i++]  = new BMIntVariable("armR","armR(cm): ",11);//���̒��a�H
		distOut = i;
		var[i++]  = new BMIntVariable("distOut","distOut(cm): ",14);//�O����Roi�̒��a�H
		return i;
	}

	protected int setupSubCheckbox(int i){
		return i;
	}
	
	/* BMSetCageDialogPanel��Roi�̎����ݒ�����鎞��
	 * int armR = Parameter.getInt(BMParameter.armR);
	 * �Ƃ���Ɖ��̂����܂������Ȃ��̂ł����p����
	 */
	public static int getarmR() {
		int type = BMParameter.armR;
		int data = 0;
		try{
			data = ((IntVariable)var[type]).getVariable();
		}catch(ClassCastException e){
			throw new IllegalArgumentException(var[type].getName() + " is not int");
		}
		return data;
	}
	
	public static void setarmR(int param) {
		int type = BMParameter.armR;
		((IntVariable)var[type]).setVar(param);
	}
	
	//��Ɠ��l�B
	public static int getDistOut() {
		int type = BMParameter.distOut;
		int data = 0;
		try {
			data = ((IntVariable)var[type]).getVariable();
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(var[type].getName() + "is not int");
		}
		return data;
	}
	
	public static void setDistOut(int param) {
		int type = BMParameter.distOut;
		((IntVariable)var[type]).setVar(param);
	}
	
	//��Ɠ��l�B
	public static int getFrameWidth() {
		int type = BMParameter.frameWidth;
		int data = 0;
		try {
			data = ((IntVariable)var[type]).getVariable();
		} catch (ClassCastException e) {
			throw new IllegalArgumentException(var[type].getName() + "is not int");
		}
		return data;
	}
	
	
	
}