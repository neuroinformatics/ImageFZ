package behavior.plugin.analyzer;

import java.util.*;

import ij.process.ImageProcessor;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.OFParameter;
import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;

public class OFAnalyzer extends Analyzer{
	/*getBinResult(int) ���\�b�h�ŕԋp����f�[�^���w�肷�邽�߂̃t�B�[���h*/
	public static final int BIN_DISTANCE = 1;
	public static final int CENTER_TIME = 2;
	public static final int PARTITION_AREA = 3;

	protected int[][] xyaData;	//X���W�AY���W�A�ʐρA�̏��Ƀf�[�^������Ƃ���BxyaData[�H�ڂ̂����܂�][�f�[�^�̎��]
	protected int[] prevXyaData;
	protected int moveEpisodeNum = 0, moveFlag = 0;
	protected double totalCenterTime = 0, currentDistance = 0, totalMoveDuration = 0;
	protected double[] binDistance, binCenterTime, areaResult;

	public OFAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate()); //�Ƃ肠��������
		tracer = new Tracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());
	}

	protected void initializeArrays(){
		binDistance = new double[binLength];
		Arrays.fill(binDistance, 0.0);
		binCenterTime  = new double[binLength];
		Arrays.fill(binCenterTime, 0.0);
		areaResult = new double[Parameter.getInt(OFParameter.partitionHorizontal) * Parameter.getInt(OFParameter.partitionVertical)];
		Arrays.fill(areaResult, 0.0);
	}

	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.dilate(ImageManager.SUBTRACT_IMG);
		if(xyaData != null)
			prevXyaData = xyaData[0];	//�O��̐��l���c���Ă���
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
	}

	public void calculate(int currentSlice){
		if(currentSlice > 0){	//���̌v�Z���ƁA�ŏ��̉摜�͉�͂̑Ώۂɓ���Ȃ����ƂɂȂ邪�Abehavior3 �������p���Łd
			currentBinSlice++;
			currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
					prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
			binDistance[currentBin] += currentDistance;
			/*�}�E�X�̓����Ă��鎞�Ԃ�A�����o���񐔂ɂ��ĉ��*/
			if(currentDistance > Parameter.getDouble(OFParameter.movementCriterion)){
				totalMoveDuration += 1.0 / Parameter.getInt(OFParameter.rate);
				if(moveFlag != 1)
					moveEpisodeNum++;
				moveFlag = 1;
			}
			else
				moveFlag = 0;
			/*�}�E�X�����S�ӂ�ɂ��邩�ǂ��������*/
			double center = (double)(atCenter(xyaData[0][X_CENTER], xyaData[0][Y_CENTER], Parameter.getInt(OFParameter.centerArea)))
			/ Parameter.getInt(OFParameter.rate);
			binCenterTime[currentBin] += center;
			totalCenterTime += center;
			calculateArea();

			tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
		}
	    tracer.setPrevXY(xyaData[0][0], xyaData[0][1]);
	}

	/*********
	�}�E�X�̒��S���W���A�t���[�����̂ǂ̋��ɂ��邩�����
	 **********/
	protected void calculateArea(){
		double oneGridWidth = (double)pixelWidth / Parameter.getInt(OFParameter.partitionHorizontal);
		int xPlace = (int)(xyaData[0][X_CENTER] / oneGridWidth);
		if(xPlace == Parameter.getInt(OFParameter.partitionHorizontal))
			xPlace--;
		double oneGridHeight = (double)pixelHeight / Parameter.getInt(OFParameter.partitionVertical);
		int yPlace = (int)(xyaData[0][Y_CENTER] / oneGridHeight);
		if(yPlace == Parameter.getInt(OFParameter.partitionVertical))
			yPlace--;
		areaResult[xPlace + yPlace * Parameter.getInt(OFParameter.partitionHorizontal)] += 1.0 / Parameter.getInt(OFParameter.rate);
	}

	public void nextBin(){
		currentBin++;
		currentBinSlice = 0;
	}

	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + getElapsedTime(sliceNum) + "\t" + xyaData[0][0] + "\t" +
		xyaData[0][1] + "\t" + totalCenterTime + "\t" + Math.round(currentDistance * 10.0) / 10.0;
	}

	public String getXY(int sliceNum){
		String xyData = sliceNum +"\t"+ xyaData[0][X_CENTER] +"\t"+ xyaData[0][Y_CENTER] +"\t" +
		(xyaData[0][EXIST_FLAG] == 0? "NP" : String.valueOf(xyaData[0][AREA]));

		for(int particle = 1; particle < xyaData.length; particle++)
			xyData += "\n"+ sliceNum +"\t"+ xyaData[particle][X_CENTER] +"\t"+ xyaData[particle][Y_CENTER] +"\t" +
			(xyaData[0][EXIST_FLAG] == 0? "NP" : xyaData[particle][AREA] +"\t" + "f");
		return xyData;
	}

	public String[] getResults(){
		String[] results = new String[9];
		double totalDistance = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalDistance += binDistance[bin];
		double meanSpeed = totalDistance / Parameter.getInt(OFParameter.duration);
		double movingSpeed = totalDistance / (totalMoveDuration != 0? totalMoveDuration : 1);
		double distPerMove = totalDistance / (moveEpisodeNum != 0? moveEpisodeNum : 1);
		double durPerMove = totalMoveDuration / (moveEpisodeNum != 0? moveEpisodeNum : 1);
		results[0] = String.valueOf(Math.round(totalDistance * 10.0) / 10.0);	//��͑S�̂œ���������(cm)
		results[1] = String.valueOf(Math.round(totalCenterTime * 10.0) / 10.0); //��͑S�̂Œ��S�ɂ�������(sec)
		results[2] = String.valueOf(Math.round(meanSpeed * 10.0) / 10.0);	//��͑S�̂̕��ϑ��x�icm/sec�j
		results[3] = String.valueOf(Math.round(movingSpeed * 10.0) / 10.0);	//���l�iMOVECR)�ȏ㓮�����������𓮂������Ԃƍl�����ꍇ�̕��ϑ��x�icm/sec�j
		results[4] = String.valueOf(moveEpisodeNum); //�����Ă��Ȃ���Ԃ��瓮���n�߂��t���[����
		results[5] = String.valueOf(Math.round(totalMoveDuration * 10.0) / 10.0); //���l�ȏ㓮�������ԑS�́isec)
		results[6] = String.valueOf(Math.round(distPerMove * 10.0) / 10.0); //�����Ă��Ȃ���Ԃ��瓮���n�߂��t���[�����őS�̂̋���������������(cm/frame)
		results[7] = String.valueOf(Math.round(durPerMove * 10.0) / 10.0); //�����Ă��Ȃ���Ԃ��瓮���n�߂��t���[�����ň��l�ȏ㓮�������ԑS�̂�������(cm/sec)
		results[8] = String.valueOf(Math.round(Parameter.getInt(OFParameter.duration) * 10.0) / 10.0); //�S�̂̎���(sec)
		return results;
	}

	public String[] getBinResult(int option){
		int length = (option != PARTITION_AREA? binLength : Parameter.getInt(OFParameter.partitionHorizontal) * Parameter.getInt(OFParameter.partitionVertical));
		String[] result = new String[length];
		for(int num = 0; num < length; num++){
			switch(option){
			case BIN_DISTANCE: result[num] = String.valueOf(Math.round(binDistance[num]*10.0)/10.0); break;
			case CENTER_TIME: result[num] = String.valueOf(binCenterTime[num]); break;
			case PARTITION_AREA: result[num] = String.valueOf(areaResult[num]); break;
			}
		}
		if(state == INTERRUPT)
			result[length - 1] += "(" + currentBinSlice + "frame )";
		return result;
	}

}









