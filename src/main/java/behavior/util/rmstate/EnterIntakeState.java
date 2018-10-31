package behavior.util.rmstate;

public class EnterIntakeState implements RMState {

	private static RMState singleton = new EnterIntakeState();
	
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){}

	public void exit(RMROI roi){
		roi.changeState(OutAfterIntakeState.getInstance());
	}
}