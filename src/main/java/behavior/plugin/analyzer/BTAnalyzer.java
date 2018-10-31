package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;
import behavior.setup.Program;
import behavior.setup.parameter.BTParameter;
import behavior.setup.parameter.Parameter;
import behavior.controller.InputController;

/**
 * BeamTest�p�̉�͂��s���B
 * 
 * �o�͂���f�[�^�͈ȉ���13��ށB
 * TotalDistance         -�}�E�X���S�̂ňړ���������
 * AverageSpeed          -�S�̂ł̃}�E�X�̈ړ����x
 * MovingSpeed           -�}�E�X���ړ��������Ԓ��ł̃}�E�X�̈ړ����x
 * MoveEpisodeNumber     -�}�E�X���ړ����āA�����~�܂�܂ł̉�
 * TotalMovementDuration -�S�̂Ń}�E�X���ړ����Ă�������
 * Slip(*)     �@�@�@�@�@-�}�E�X����������
 * Latency �@�@�@�@-�I�������Ɋւ�炸�A�����S�̂̎���
 * Call(*)     -�{�^���������ďI���������A��͂ŏI��������
 * Duration �@�@�@-��������
 * 
 * �i�ȉ��̃f�[�^�͏o�͐������ł͂Ȃ��j
 * MovingSpeed          -�}�E�X���ړ����āA�����~�܂�܂�(Phase)���Ƃ̑��x
 * DistancePerMovement  -Phase���Ƃ̈ړ�����
 * DurationPerMoveament -Phase���Ƃ̎���
 * SlipedTime(*)           -�}�E�X���������Ƃ��̎���
 * 
 * Latency(�}�E�X���������ꍇ),SlipedTime�̎擾�ɂ�Labjack���g�p�B
 * (*)���t�����f�[�^�́AOffline�ł͏o�͂��Ȃ��B
 * @author Butoh
 */
//�I�������ƁA�o�͐������ł͂Ȃ��v�f�A�����Labjack�����ݍ����ĕ��G�ɂȂ��Ă���B
//���܂������Ȃ�����������𐮗�����
public class BTAnalyzer extends Analyzer {
	//�ʂɌ��ʂ��o�͂��邽�߂̃t�B�[���h
	public static final int DISTANCE_PER_MOVEMENT = 1;
	public static final int DURATION_PER_MOVEMENT = 2;
	public static final int SPEED_PER_MOVEMENT = 3;
	public static final int SLIPED_TIME = 4;

	//�g�p������W
	//private final double LEFTMOST_X = 0.0,TOP_Y  = 0.0; 
	private double rightmostX = 10.0;//lowerY= 10.0;
	protected int[][] xyaData; //�}�E�X�̍��W
	protected int[] prevXyaData; //1�R�}�O�̃}�E�X�̍��W

	//�g�p����p�����[�^�A��X�Ăяo���Ȃ��ł����ŃZ�b�g����
	private final int rate = Parameter.getInt(Parameter.rate);
	private final int duration = Parameter.getInt(Parameter.duration);
	private final double movementCriterion = Parameter.getDouble(BTParameter.movementCriterion);
	private final int goalArea = Parameter.getInt(BTParameter.goalArea);

	//�g�p���鐔�l
	private int currentSlice; //interrupt()�p
	private double currentDistance; //Slice�Ԃ̈ړ�����
	private double distanceP; //�ړ����Ă��痧���~�܂�܂ł̋������ꎞ�I�ɕۑ�(P�������ϐ��̖����͂���)
	private int slicePerMovement; //�ړ����Ă��痧���~�܂�܂ł�Slice��
	private boolean setFreeze = true; //��x�����~�܂����瓮���o���܂�true
	//private double conseqFreeze;
	//private double distanceF;
	private int allSlice;

	protected boolean[] endAnalyze; //����ɂ��Ă͌�ŏC������(�\��)
	private Slip spc = null;

	//���ʃf�[�^
	private List<Double> speedPerMovement = new ArrayList<Double>();
	private List<Double> distancePerMovement = new ArrayList<Double>();
	private List<Double> durationPerMovement = new ArrayList<Double>();
 	//private double goalTime = 0.0,goalTimeB = 0.0, fellTime = 0.0;
	private double time;
	private List<Double> slipedTime = new ArrayList<Double>();
	private boolean button = false;

	//�t���O
    private boolean isGoal = false;
    private boolean offline = false;

	protected BTAnalyzer() {
	}

	public BTAnalyzer(ImageProcessor backIp) {
		imgManager = new ImageManager(backIp.duplicate());
		tracer = new Tracer(backIp);
		setImageSize(backIp.getWidth(), backIp.getHeight());

		//this.endAnalyze[0] = endAnalyze;
	}

	//Offline�p��
	public void setFlag(final Program program, final int allSlice){
		if(program == Program.BTO){
			offline = true;
			this.allSlice = allSlice;
		}
	}

	//bin�͎g�p���Ȃ�
	public boolean binUsed(){
		return false;
	}

	//OnlineExecuter�̃|�C���^��n��
	public void setEndAnalyze(boolean[] endAnalyze){
		this.endAnalyze = endAnalyze;
	}

	//�摜�����H
	public void analyzeImage(ImageProcessor currentIp) {
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		if (xyaData != null)
			prevXyaData = xyaData[0];
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG,	prevXyaData);
	}

	//MainArea.roi����E�[��X���W���擾����
	public void createField(Rectangle rec) {
		rightmostX = rec.getWidth(); // �E����
		//lowerY = rec.getHeight(); // ������
	}

	//�����ŃX���b�h���J�n������
	public void setStart(boolean set){
		isGoal = false;
		spc = new Slip();
		spc.start();
	}

	//���
	public void calculate(final int currentSlice) {
		this.currentSlice = currentSlice;	//interruptAnalysis()�p
		//slicePerMovement++;
		if (currentSlice > 0) {
			currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
			//�}�E�X�������Ȃ��ꍇ
			/*if(setFreeze && (currentDistance <= movementCriterion/rate)){
				//distanceF += currentDistance;
				//conseqFreeze++;
			}*/
			//�}�E�X���ړ������ꍇ�����������Ă���
			//currentDistance > movementCriterion/rate
			if((currentDistance*rate) > movementCriterion){
		        distanceP += currentDistance;
		        slicePerMovement++;

		        /*//�����Ȃ������Ƃ݂Ȃ����Ԃ̈ړ�������movementCriterion�𒴂����ꍇ
				if(distanceF > movementCriterion){
				  distanceP += distanceF; 
				  slicePerMovement += conseqFreeze;
				  //������
				  distanceF = 0.0; conseqFreeze = 0.0;
				}*/
				setFreeze = false;
		    }
			//�}�E�X���ړ�������A�����Ȃ��Ȃ�����v�f��ǉ�
			//currentDistance > movementCriterion/rate
		    if(!setFreeze && ((currentDistance*rate) <= movementCriterion)){
		    	double durationP = (double)slicePerMovement/rate;
		    	double speedP = distanceP/durationP;
		    	setPhase(speedP,distanceP, durationP);
		    	//������
		    	distanceP = 0.0; slicePerMovement = 0;
		    	//���ɓ����܂ł���͍s��Ȃ�
		    	setFreeze = true;
		    }


		    //GoalLine(GoalArea.roi)�̍��[��X���W�𒴂�����GoalTime���Z�b�g
			if (!offline && xyaData[0][X_CENTER] >= (rightmostX-goalArea)) {
				time = ((double)(currentSlice + 1) / rate);
				//�Z�b�g����͈̂�x�̂�
				setGoal();
			}
			//GoalTime���Z�b�g�����ꍇ��͂��I��

			//trace��`��
			((Tracer)tracer).writeTrace(prevXyaData[X_CENTER], prevXyaData[Y_CENTER],	xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);

			if(offline && currentSlice == allSlice)
				spc.end();
			//�������Ԃ�������X���b�h���I��������
			if(!offline && currentSlice == duration*rate)
				spc.end();
		}
	}

	//���ʂ��Z�b�g
	private void setPhase(final double speedP,final double distanceP, final double durationP){
    	speedPerMovement.add(speedP);
    	distancePerMovement.add(distanceP);
    	durationPerMovement.add(durationP);
	}

	//���̕ӂ͐����悭�������Ă��Ȃ�
	public void interruptAnalysis(){
		if(currentSlice == 0)
			state = NOTHING;
		else if(currentSlice == (rate*duration)+1)
			state = COMPLETE;
		else{
			state = INTERRUPT;
		}
	}

	private void setGoal(){
		isGoal = true;
	}

	//��͂��I��������
	public boolean isGoal(){
		return isGoal;
	}

	public ImageProcessor getSubtractImage() {
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	//���̕ӂ͂ǂ����������ɉ�ʂ�����Ȃ��̂œK����
	public String getInfo(final String subjectID, final int sliceNum) {
		return subjectID + "\t" + getElapsedTime(sliceNum) + "\t" + slipedTime.size();
	}

	public String getXY(final int sliceNum) {
		String xyData = (sliceNum + 1) + "\t" + xyaData[0][X_CENTER] + "\t"	+ xyaData[0][Y_CENTER] + "\t"
				          + (xyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(xyaData[0][AREA]));
		return xyData;
	}

	public String[] getResults() {
		double durationOfAnalysis;
		double totalDistance = 0.0;
		double averageSpeed = 0.0;
		double movingSpeed = 0.0;
		double totalMovementDuration = 0.0;
		int movingEpisodeNumber = speedPerMovement.size();

		//�I���������Ƃɕ�����K�v������
		if(time != 0.0)        durationOfAnalysis=time;
		else if(state == INTERRUPT)durationOfAnalysis=(double)currentSlice/rate;
		else                       durationOfAnalysis=duration;

		if(offline)durationOfAnalysis = (double)(allSlice-2)/rate;

		//�Ō�ɗ����~�܂��Ă��炻��܂ł̕���������K�v������
		if(slicePerMovement != 0 && distanceP != 0.0){
			double durationP = (double)slicePerMovement/rate;
	    	double speedP = distanceP/durationP;
	    	setPhase(speedP,distanceP, durationP);
	    	movingEpisodeNumber = speedPerMovement.size();
		}

		//�������ԂŏI�����āA1��������~�܂�Ȃ������ꍇ
		if(movingEpisodeNumber == 0 && distanceP != 0.0){
			double speedP = distanceP/durationOfAnalysis;
			setPhase(speedP,distanceP,durationOfAnalysis);
	    	movingEpisodeNumber = speedPerMovement.size();
		}

		//TotalDistance(cm)
		for(Iterator<Double> i=distancePerMovement.iterator(); i.hasNext();){
	       totalDistance += i.next();
		}

		//AverageSpeed(cm/s)
		averageSpeed = totalDistance/durationOfAnalysis;

		//TotalMovementDuration(sec)
		for(Iterator<Double> i=durationPerMovement.iterator(); i.hasNext();){
			totalMovementDuration += i.next();
		}

		//MovingSpeed(cm/s)
		if(totalMovementDuration == 0){
			movingSpeed = 0.0;
		}else{
		    movingSpeed = totalDistance/totalMovementDuration;
		}

		//�����_��2�ʈȉ��͎l�̌ܓ������
		//""+�@��String.ValueOf()�Ɠ�������
		int i = offline ?7:9;
		String[] results = new String[i];
		results[0] = "" + Math.round(totalDistance*10.0)/10.0;
		results[1] = "" + Math.round(averageSpeed*10.0)/10.0;
		results[2] = "" + Math.round(movingSpeed*10.0)/10.0;
		results[3] = "" + movingEpisodeNumber;
		results[4] = "" + Math.round(totalMovementDuration*10.0)/10.0;
		//Online��Offline�ŏo�͌��ʂ��قȂ�
		if(!offline){
		  results[5] = "" + slipedTime.size();  //SlipCount
		  results[6] = "" + Math.round(durationOfAnalysis*10.0)/10.0;
		  results[7] = button ?"Button":"Analysis";
		  results[8] = "" + duration;
		}else{
		  results[5] = "" + Math.round(durationOfAnalysis*10.0)/10.0;
		  results[6] = "" + duration;
	    }

		return results;
	}

	//�v�f�����łȂ����Ōʂɏo�͂�����̂̃f�[�^��n��
    public List<String> getRespectiveResults(final int option) {
		int length = 4;
		List<String> result = new ArrayList<String>();
		for(int num = 0; num < length; num++){
			switch(option){
			case DISTANCE_PER_MOVEMENT:
				for(Iterator<Double> i=distancePerMovement.iterator();i.hasNext();){
					result.add("" + Math.round(i.next()*10.0)/10.0);
				    i.remove();
			    }
				break;
			case DURATION_PER_MOVEMENT:
				for(Iterator<Double> i=durationPerMovement.iterator();i.hasNext();){
					result.add("" + Math.round(i.next()*10.0)/10.0);
				    i.remove();
			    }
				break;
			case SLIPED_TIME: 
				for(Iterator<Double> i=slipedTime.iterator();i.hasNext();){
				    result.add("" + Math.round(i.next()*10.0)/10.0);
			        i.remove();
		        }
				break;
			case SPEED_PER_MOVEMENT:
				for(Iterator<Double> i=speedPerMovement.iterator();i.hasNext();){
					result.add("" + Math.round(i.next()*10.0)/10.0);
				    i.remove();
			    }
				break;
			default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}

		if(state == INTERRUPT)
			result.add("(" + currentSlice + "frame)");
		return result;
	}

	@Override
	public String[] getBinResult(int option){return null;}
	@Override
	public void nextBin(){}
	@Override
	public void resetDuration(int duration){}

	//fellTime�ɂ̓{�^��1�AslipedTime�ɂ̓{�^��2���g�p
	//�I��������ꍇ��setEndAnalyze()��
	class Slip extends Thread{
		private boolean isRunnable;
		private long startTime;
		private InputController input;
		private boolean twoPushed;
		private boolean setOver;

		protected Slip() {
			isRunnable = true;
			if (input != null)
				input.close();
			input = InputController.getInstance(InputController.PORT_IO);
		}

		public void run(){
			startTime = System.currentTimeMillis();
			while (isRunnable) {
				try {
					//�����J�n�ɂ��{�^��1���g�p���邽��
					if(!setOver && input.getInput(0))
						setOver = true;

					//if(!onePushed &&setOver && value == 1){
					//�{�^��1���������ꍇ��͂��I��
					if(setOver && !offline && input.getInput(0)){
						isRunnable = false;
						setGoal();
						time = (double)(System.currentTimeMillis()-startTime)/1000;
						button = true;
					}

                    //slipedTime���Z�b�g
                    if(!twoPushed && input.getInput(1)){
                    	double x = (double)(System.currentTimeMillis()-startTime)/1000;
                    	slipedTime.add(Math.round(x*10.0)/10.0);
                    	twoPushed = true;
                    }
                    //�������h�~
                    if(twoPushed && !input.getInput(1))
                    	twoPushed = false;

                    Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		protected void end() {
			isRunnable = false;
		}
	}
}