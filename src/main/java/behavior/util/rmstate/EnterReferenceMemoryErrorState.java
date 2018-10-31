package behavior.util.rmstate;

public class EnterReferenceMemoryErrorState implements RMState {
	private static RMState singleton = new EnterReferenceMemoryErrorState();
		
	public static RMState getInstance(){
		return singleton;
	}

	public void intake(RMROI roi){}

	public void missing(RMROI roi){}

	public void enter(RMROI roi){}

	public void exit(RMROI roi){
		roi.changeState(OutAfterReferenceState.getInstance());
	}
}

