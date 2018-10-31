package behavior.util.rmstate;

public class OutState implements RMState {
	private static RMState singleton = new OutState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){
		roi.changeState(EnterAfterOmissionState.getInstance());
	}

	public void exit(RMROI roi){}
}
