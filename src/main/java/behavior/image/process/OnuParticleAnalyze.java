package behavior.image.process;

import ij.*;
import ij.measure.*;
import ij.process.*;
import ij.plugin.filter.*;

public class OnuParticleAnalyze implements Measurements{

	public int nIp;
	public int[] prevPlot = {0,0,0,0};
	public int[][] plotAll,multiPrevPlot;
	public int[][][] multiPlotAll;
	private ResultsTable rt;
	private int minSize = 0 ,maxSize = 0;

	final int options = 0;
	final int centerMeasure = CENTROID;
	final int areaMeasure = AREA;

	/**
	 �R���X�g���N�^�ɉ����w�肵�Ȃ��ꍇ�A1�̉�ʂ������B
	 */
	public OnuParticleAnalyze(){
		nIp = 1;
	}

	/**
	 �����Ɏw�肳�ꂽ��ʐ��������B
	 */
	public OnuParticleAnalyze(int nIp){
		this.nIp = nIp;
	}

	/**
	 ��͂̌��ɂȂ�l�������Ŏw��B��ʐ���1�B
	 */
	public OnuParticleAnalyze(int[] prevPlot){
		nIp = 1;
		this.prevPlot = prevPlot;
	}

	/**�O��� plot ���Z�b�g
	 */
	public void setPlot(int[] prevPlot){
		this.prevPlot = prevPlot;
	}

	/**�p�����[�^���Z�b�g
	 *@param minSize �F������ŏ��p�[�e�B�N��
	 */
	public void setParameter(int minSize, int maxSize){
		this.minSize = minSize;
		this.maxSize = maxSize;
	}


	/**
	 1�̉�ʂɂ��A��͂��s���B�����́A��͂��s���摜�A�摜�v���Z�b�T�A�g�p����ResultsTable�A
	 Particle�F�����s���ŏ��l�A�ő�l�A���ۂ̕��̉����A�c���BplotAll���e�FplotAll[0][]�ɐ^��Particle�������B
	 Particle�������F�����ꂽ�ꍇ�AplotAll[1][]�ȍ~�ɋU��Particle�������BplotAll[n][0]=X���W�AplotAll[n][1]=Y���W�A
	 plotAll[n][2]=�ʐρAplotAll[n][3]=Particle�@Flag�inull�̂Ƃ�0x00,1�̂Ƃ�0x01�A2�ȏ�̂Ƃ�0x10�j�B
	 */
	public int[][] analyzeParticle(ImageProcessor ip){
		if(rt == null)
			rt = new ResultsTable();
		rt.reset();
		ParticleAnalyzer centerPa = new ParticleAnalyzer(options,centerMeasure,rt,minSize,maxSize);
		ip = ip.convertToByte(false);
		centerPa.analyze(new ImagePlus("", ip), ip);
//		int nParticles = rt.getCounter();//�p�[�e�B�N����
		float[] dx = rt.getColumn(ResultsTable.X_CENTROID);//�S�p�[�e�B�N���̐^�񒆂�X���W
		float[] dy = rt.getColumn(ResultsTable.Y_CENTROID);//�S�p�[�e�B�N���̐^�񒆂�Y���W
		rt.reset();
		ParticleAnalyzer areaPa = new ParticleAnalyzer(options,areaMeasure,rt,minSize,maxSize);
		areaPa.analyze(new ImagePlus("", ip), ip);
		float[] dArea = rt.getColumn(ResultsTable.AREA);//�S�p�[�e�B�N���e�ʐ�
		int index;
		if(dx == null){//�p�[�e�B�N�����F������Ȃ��ꍇ�A�O�̃p�[�e�B�N���������̂܂ܑ���B
			plotAll = new int[1][4];
			plotAll[0][0] = prevPlot[0];
			plotAll[0][1] = prevPlot[1];
			//plotAll[0][2] = prevPlot[2];
			plotAll[0][2] = 0; //miyakawa 010205
			plotAll[0][3] = 0;
		}else{
			plotAll = new int[dx.length][4];
			if(dx.length == 1){
				plotAll[0][0] = Math.round(dx[0]);
				plotAll[0][1] = Math.round(dy[0]); 
				plotAll[0][2] = Math.round(dArea[0]);
				plotAll[0][3] = 1;
			}else{
				//�����p�[�e�B�N���̏ꍇ�A�O�̃p�[�e�B�N�����W��(0,0)�Ȃ�΁i���Ȃ킿���߂ăp�[�e�B�N���F�������Ƃ��j�A�ʐς��ő�̂��̂�^�Ƃ���B
				if(prevPlot[0]==0 && prevPlot[1]==0){
					index = maxArea(dArea);//�p�[�e�B�N���ʐς̔z��ŁA�ʐς��ő�ƂȂ�index�����߂�B
				}else{
					//�����p�[�e�B�N���őO�̃p�[�e�B�N�����W��(0,0)�łȂ��Ƃ��́A�O�̃p�[�e�B�N������ł��߂��ꏊ�ɂ���p�[�e�B�N����^�Ƃ���B
					double[] distance = new double[dx.length];
					for(int n=0;n<dx.length;n++){
						distance[n] = calculateDistance(prevPlot[0],prevPlot[1],Math.round(dx[n]),Math.round(dy[n]));//�V�����F�����ꂽ�p�[�e�B�N���ƑO�̃p�[�e�B�N���̋��������߂�B
					}
					index = minDistance(distance);//�����̔z��ŁA�l���ŏ��ƂȂ�index�����߂�B
				}

				//X���W�AY���W�A�ʐς̔z������ւ���B�^�̃p�[�e�B�N�����([index])��[0]�ցA[0]�Ɋi�[����Ă�������[index]�ցB
				float tempX,tempY,tempArea;
				tempX = dx[0];//�ꎞ�ޔ��B
				tempY = dy[0];
				tempArea = dArea[0];
				dx[0] = dx[index];
				dx[index] = tempX;
				dy[0] = dy[index];
				dy[index] = tempY;
				dArea[0] = dArea[index];
				dArea[index] = tempArea;
				//�z��plotAll�Ɍ��ʂ�������B
				for(int i=0;i<dx.length;i++){
					plotAll[i][0] = Math.round(dx[i]);
					plotAll[i][1] = Math.round(dy[i]);
					plotAll[i][2] = Math.round(dArea[i]);
				}
				plotAll[0][3] = 2;
			}
		}
		return plotAll;
	}

	/**
	 �����Ɏw�肳�ꂽ�z��̂����A�l���ŏ��̂��̂�index�l��Ԃ��B
	 */
	private int minDistance(double[] distance){
		double minValue = distance[0];
		int indexD = 0;
		for(int i = 1; i < distance.length; i++){
			if(distance[i] < minValue){
				indexD = i;
				minValue = distance[i];
			}
		}
		return indexD;
	}
	

	/**
	 �����Ɏw�肳�ꂽ�z��̂����A�l���ő�̂��̂�index�l��Ԃ��B
	 */
	private int maxArea(float[] dArea){
		float maxArea = 0;
		int indexA = 0;
		for(int i = 0; i < dArea.length; i++){
			if(dArea[i] > maxArea){
				indexA = i;
				maxArea = dArea[i];
			}
		}
		return indexA;
	}

	private double calculateDistance(int prevX, int prevY, int x, int y){
		int disX = x - prevX;
		int disY = y - prevY;
		return Math.sqrt(Math.pow((double)disX, 2) + Math.pow((double)disY, 2));
	}
	
}



