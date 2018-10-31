package behavior.controller;

/**debug �p�̃A�E�g�v�b�g�f�o�C�X�B*/
class DebugOutput extends AbstractOutput{
	DebugOutput(){}

	@Override
	boolean open(){
		System.out.println("Setup output.");
		return false;
	}

	@Override
	void controlOutput(int bits){
		System.out.println("Start Output Channel�@"+bits);
	}

	@Override
	void clear(int bits){
		if(bits<10)
		System.out.println("Stop Output Channel�@"+bits);
	}

	@Override
	void close(){}
}