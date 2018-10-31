package behavior.setup;


import java.util.Arrays;


public class WMReferenceManager{

	private final int MAX_SIZE = 100;
	private String[] SubjectNames;
	private int[] currentTrial;
	private double[] averageLatency;
	private int nowCount = 0;
	private int allTrial = 0;

	/**
	 * 実際には、それぞれの検体に対する、現段階におけるトライアル回数を求めるもの
	 *
	 */
	public WMReferenceManager(int trial){
		this.allTrial = trial;
		SubjectNames = new String[MAX_SIZE];
		currentTrial = new int[MAX_SIZE];
		Arrays.fill(currentTrial,0);
		averageLatency = new double[MAX_SIZE];
		Arrays.fill(averageLatency,0.0);
	}

	public void setSubject(String subject){
		SubjectNames[nowCount] = subject;
		nowCount++;
	}

	public boolean subjectEquals(String subject){
		for(int i=0;i<nowCount;i++){
			if(SubjectNames[i].endsWith(subject)){
				return true;
			}
		}
		return false;
	}

	public boolean presubjectEquals(String subject){
		return SubjectNames[nowCount-1].endsWith(subject);
	}

	public void setCurrentTrial(String subject,int currentTrial){
		for(int i=0;i<nowCount;i++){
			if(SubjectNames[i].endsWith(subject))
				this.currentTrial[i] = currentTrial;
		}
	}

	public int getCurrentTrial(String subject){
		for(int i=0;i<nowCount;i++){
			if(SubjectNames[i].endsWith(subject))
				return currentTrial[i];
		}
		return 0;//判定用
	}

	public boolean currentTrialEquals(int currentTrial){
		return allTrial == currentTrial;
	}

	public void setAverageLatency(String subject,double latency){
		for(int i=0;i<nowCount;i++){
			if(subject.startsWith(SubjectNames[i]))
				this.averageLatency[i] = latency;
		}
	}

	public double getAveregeLatency(String subject){
		for(int i=0;i<nowCount;i++){
			if(subject.startsWith(SubjectNames[i]))
				return averageLatency[i];
		}
		return 0;//判定用
	}
}
