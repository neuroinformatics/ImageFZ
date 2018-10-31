package behavior.plugin.analyzer;

import java.util.ArrayList;

import ij.process.ImageProcessor;

import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;
import behavior.setup.parameter.FZShockParameter;
import behavior.setup.parameter.Parameter;

public class FZShockAnalyzer  extends Analyzer{
	/*getBinResult(int) ���\�b�h�ŕԋp����f�[�^���w�肷�邽�߂̃t�B�[���h*/
	public static final int BIN_DISTANCE = 1;
	public static final int CENTER_TIME = 2;
	public static final int PARTITION_AREA = 3;

	protected int[][] xyaData;	//X���W�AY���W�A�ʐρA�̏��Ƀf�[�^������Ƃ���BxyaData[�H�ڂ̂����܂�][�f�[�^�̎��]
	protected int[] prevXyaData;
	protected int moveEpisodeNum = 0, moveFlag = 0;
	protected double currentDistance = 0.0;
	private ArrayList<Double> distancePerShock = new ArrayList<Double>();
	private int totalSlice = 0,totalMoveSlice = 0,totalCenterSlice = 0;
	private boolean isNoCount = false;

	public FZShockAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate()); //�Ƃ肠��������
		tracer = new Tracer(backIp);
		setImageSize(backIp.getWidth(), backIp.getHeight());
		distancePerShock.clear();
		isNoCount = false;
	}

	@Override
	public boolean binUsed(){
		return false;
	}

	@Override
	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//�O��̐��l���c���Ă���
		xyaData = imgManager.analyzeParticle(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	@Override
	public void calculate(int currentSlice){
		totalSlice++;
		/*�}�E�X�����S�ӂ�ɂ��邩�ǂ��������*/
		totalCenterSlice += atCenter(xyaData[0][X_CENTER], xyaData[0][Y_CENTER], Parameter.getInt(FZShockParameter.centerArea));

		if(currentSlice>0){
			if(!isNoCount){
			    currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
					                          prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);

			    distancePerShock.add(currentDistance);

			    /*�}�E�X�̓����Ă��鎞�Ԃ�A�����o���񐔂ɂ��ĉ��*/
			    if(currentDistance > Parameter.getDouble(FZShockParameter.movementCriterion)){
				    totalMoveSlice++;
				    if(moveFlag != 1)
					    moveEpisodeNum++;
				    moveFlag = 1;
			    }else{
				    moveFlag = 0;
			    }
			}else{
				distancePerShock.add(-1.0);
				isNoCount = false;
			}
			tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		}
		tracer.setPrevXY(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
	}

	public void nextShock(){
		isNoCount = true;
	}

	@Override
	public void nextBin(){}

	public ImageProcessor getTraceImage(final int sliceNum,final int duration){
		ImageProcessor traceIp = (tracer.getTrace()).duplicate();
		if(duration!=0 && sliceNum != 0 && sliceNum % (duration*Parameter.getInt(FZShockParameter.shockCaptureRate)) == 0)
			tracer.clearTrace();
		return traceIp;
	}

	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	@Override
	public String getInfo(String subjectID, int sliceNum){return null;}

	@Override
	public String getXY(int sliceNum){
		String xyData = sliceNum +"\t"+ xyaData[0][X_CENTER] +"\t"+ xyaData[0][Y_CENTER]
		                    +"\t"+(xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA]));

		return xyData;
	}

	@Override
	public String[] getResults(){
		String[] results = new String[9];

		double totalDistance = 0.0;
		for(double dist:distancePerShock){
			if(dist != -1.0)
			    totalDistance += dist;
		}
		double totalDuration = (double)totalSlice/Parameter.getInt(FZShockParameter.shockCaptureRate);
		double movingDuration = (double)totalMoveSlice/Parameter.getInt(FZShockParameter.shockCaptureRate);

		//��͑S�̂œ���������(cm)
		results[0] = ""+ Math.round(totalDistance*10.0)/10.0;
		//��͑S�̂Œ��S�ɂ�������(sec)
		results[1] = ""+ Math.round(((double)totalCenterSlice/Parameter.getInt(FZShockParameter.shockCaptureRate))*10.0)/10.0;
		//��͑S�̂̕��ϑ��x�icm/sec�j
		results[2] = ""+ Math.round((totalDistance/totalDuration)*10.0)/10.0;
		//���l�iMOVECR)�ȏ㓮�����������𓮂������Ԃƍl�����ꍇ�̕��ϑ��x�icm/sec�j
		results[3] = ""+ (movingDuration!=0.0 ? Math.round((totalDistance/movingDuration)*10.0)/10.0 : "-");
		 //�����Ă��Ȃ���Ԃ��瓮���n�߂��t���[����
		results[4] = ""+ moveEpisodeNum;
		 //���l�ȏ㓮�������ԑS�́isec)
		results[5] = ""+ Math.round(movingDuration*10.0)/10.0;
		//�����Ă��Ȃ���Ԃ��瓮���n�߂��t���[�����őS�̂̋���������������(cm/frame)
		results[6] = ""+ (moveEpisodeNum!=0 ? Math.round((totalDistance/moveEpisodeNum)*10.0)/10.0 : "-");
		//�����Ă��Ȃ���Ԃ��瓮���n�߂��t���[�����ň��l�ȏ㓮�������ԑS�̂�������(cm/sec)
		results[7] = ""+ (moveEpisodeNum!=0 ? Math.round((movingDuration/moveEpisodeNum)*10.0)/10.0 : "-");
		//�S�̂̎���(sec)
		results[8] = ""+ Math.round(totalDuration *10.0)/10.0;

		return results;
	}

	public ArrayList<Double> getRespectiveResults(){
		return distancePerShock;
	}

	@Override
	public String[] getBinResult(int option){return null;}
}