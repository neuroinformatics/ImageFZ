package behavior.controller;

public abstract class AbstractInput{
	abstract boolean getInput(int channel);	//input �̎擾
	abstract void clear(int value);	//������
	abstract void reset();	//������?
	abstract void resetAll();	//������?
	abstract void close();	//�f�o�C�X�����
}