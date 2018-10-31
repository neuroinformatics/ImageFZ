package behavior.plugin.analyzer;

/**
OF-circle 実験用。ある条件を満たすマウスの動きをエピソードと称して記録する
 */
public class Episode{
	private final int ID;

	public enum State{
		START_POINT('o'),
		STRAIGHT('S'),
		OBTUSE_LEFT('L'),OBTUSE_RIGHT('R'),
		SHARP_LEFT('L'),SHARP_RIGHT('R'),
		LEFT ('L'),RIGHT('R'),
		NO_DOMINANCE('N'),
		CIRCLE('C'),NOT_CIRCLE('N'),
		REACHED_ROI('S'),REACHED_MAX_DIST('D'),
		IMMOBILE('I'),INTERRUPT('N');

		private char state;
		private State(char state){this.state = state;}
		public char getStateChar(){return state;}
	}

	private boolean episodeEnd = false;
	/*結果*/
	private State endType, circle, dominance;
	private int obtuseLeftNum = 0, obtuseRightNum = 0, sharpLeftNum = 0, sharpRightNum = 0,straightNum = 0;

	public Episode(int ID){
		this.ID = ID;
	}

	public void addState(State state){
		switch(state){
		    case OBTUSE_LEFT:  obtuseLeftNum++;  break;
		    case OBTUSE_RIGHT: obtuseRightNum++; break;
		    case SHARP_LEFT:   sharpLeftNum++;   break;
		    case SHARP_RIGHT:  sharpRightNum++;  break;
		    case STRAIGHT:     straightNum++;    break;
		default:
			break;
		}
	}

	public boolean doesNotEnd(){
		return !episodeEnd;
	}

	public void endProcess(State endState){
		boolean circleEnd = (endState==State.REACHED_ROI); 
		boolean sharpExist = (sharpLeftNum+sharpRightNum) > 0;
		if(circleEnd && obtuseLeftNum > 0 && obtuseRightNum == 0 && sharpExist == false){
			circle = State.CIRCLE;
			dominance = State.LEFT;
		}else if(circleEnd && obtuseRightNum > 0 && obtuseLeftNum == 0 && sharpExist == false){
			circle = State.CIRCLE;
			dominance = State.RIGHT;
		}else{
			circle = State.NOT_CIRCLE;
			dominance = State.NO_DOMINANCE;
		}

		endType = endState;
		episodeEnd = true;
	}

	public boolean isCircle(){
		if(episodeEnd == false)
			throw new IllegalStateException("before calculating dominance");
		return circle == State.CIRCLE;
	}

	public boolean isLeftCircle(){
		if(episodeEnd == false)
			throw new IllegalStateException("before calculating dominance");
		return dominance == State.LEFT;
	}

	public boolean isRightCircle(){
		if(episodeEnd == false)
			throw new IllegalStateException("before calculating dominance");
		return dominance == State.RIGHT;
	}

	public String getResult(){
		if(episodeEnd == false)
			throw new IllegalStateException("before calculating");
		return ID +"\t"+ obtuseLeftNum +"\t"+ obtuseRightNum +"\t"+ sharpLeftNum +"\t"+ 
		            sharpRightNum +"\t"+ straightNum +"\t"+ endType +"\t"+ dominance +"\t"+ circle;
	}
}