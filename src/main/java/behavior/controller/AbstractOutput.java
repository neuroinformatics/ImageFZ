package behavior.controller;

public abstract class AbstractOutput{
	abstract boolean open(); //device ���J��
	abstract void clear(int bits);	//������(�A�E�g�v�b�g�����j
	abstract void controlOutput(int bits);	//�A�E�g�v�b�g���o��
	abstract void close();	//device �����

}


