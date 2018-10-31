package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.SIParameter;
import behavior.image.process.ImageManager;
import behavior.image.process.SITracer;

public class SIAnalyzer extends Analyzer{
	//�ݒ�ς݂̃p�����[�^
	private final double CONTACT_DIST;
	private final double MIN_ACT_DIST;
	private final double MAX_SEPARATION;

	protected int rCount;//	�p�[�e�B�N���̐�
	
	protected int[][] xyaData;	//X���W�AY���W�A�ʐρA�̏��Ƀf�[�^������Ƃ���BxyaData[�H�ڂ̂����܂�][�f�[�^�̎��]
	protected int[] prevXyaData;
	protected int mouseA,mouseB;//���݂̃}�E�XA,B��xyaData�̒��̉��Ԗڂ̃p�[�e�B�N���Ƃ��ē����Ă��邩���L��
	protected int[] prevA,prevB;//�O��̌��ʂ̕ۑ�

	protected double currentDistanceA = 0, currentDistanceB = 0,totalDistanceA =0,totalDistanceB =0;
	protected boolean contactFlag=false;
	protected int contactNum=0,activeTime=0,ContactTime = 0,ncfA=0,ncfB=0;
	protected boolean cfdFlag,cflFlag;
	
	protected int[] currentA,currentB;//mouseA��mouseB�ŊǗ������thresLength�𒴂����Ƃ��ɕs�s�����N����������
	double totalDistance = 0;

	private final int rate = Parameter.getInt(SIParameter.rate);

	public SIAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate());
		tracer = new SITracer(backIp);
		setImageSize(backIp.getWidth(), backIp.getHeight());

		MIN_ACT_DIST = Parameter.getDouble(SIParameter.minActDis)/Parameter.getInt(Parameter.rate);
		CONTACT_DIST = Parameter.getDouble(SIParameter.contactDis);
		MAX_SEPARATION = 120/Parameter.getInt(Parameter.rate);
	}

	@Override
	public boolean binUsed(){return false;}

	@Override
	public void analyzeImage(ImageProcessor currentIp){
		ImageProcessor ip = currentIp.duplicate();
		int size =ip.getWidth()*ip.getHeight();
		Object pixels = ip.getPixels();
		if(!(pixels instanceof byte[])){ 
			int[] wrongPixel = (int[])pixels;
			byte[] safePixel = new byte[size];
			for(int j = 0; j < wrongPixel.length; j++)
				safePixel[j] = (byte)wrongPixel[j];
			ip = ip.convertToByte(false);
			ip.setPixels(safePixel);
		}
		
		imgManager.setCurrentIp(ip.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		imgManager.reduceNoise(ImageManager.SUBTRACT_IMG);

		if(currentA != null)
			prevA = currentA;	//�O��̐��l���c���Ă���
		if(currentB != null)
			prevB = currentB;	//�O��̐��l���c���Ă���
			
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);
		rCount = xyaData[0][EXIST_FLAG];

		mouseA = 0;//�ꖇ�ڂ̉摜�ŕK�v�H�H
		if(rCount==1) mouseB = 0;
		if(rCount>1) mouseB = 1;
		if(rCount>2){
			rCount = xyaData.length;
		}
	}

	@Override
	public void calculate(int currentSlice){
		if(currentSlice == 0){
			currentA=xyaData[mouseA];
			currentB=xyaData[mouseB];
		}else{
			checkmouseA();
			checkmouseB();
			checkContact();

			totalDistance += currentDistanceA+currentDistanceB;

			((SITracer)tracer).writeTraceMouseA(currentA[X_CENTER], currentA[Y_CENTER]);
			((SITracer)tracer).writeTraceMouseB(currentB[X_CENTER], currentB[Y_CENTER]);
		}
		((SITracer)tracer).setPrevXY(currentA[X_CENTER], currentA[Y_CENTER],currentB[X_CENTER], currentB[Y_CENTER]);
	}

	private void checkmouseA(){
		mouseA=0;
		currentDistanceA = 10000;
		double distance;
		for(int a=0;a<rCount;a++){//rCount=1�̎�������ł悢
			distance = getDistance(xyaData[a][X_CENTER], xyaData[a][Y_CENTER],
					                    prevA[X_CENTER], prevA[Y_CENTER]);
			if(distance < currentDistanceA){
				mouseA = a;
				currentDistanceA = distance;				
			}
		}
		currentA=xyaData[mouseA];
		
		if(currentDistanceA > MAX_SEPARATION){
			currentDistanceA = 0;
			currentA=prevA;
		}
			
		totalDistanceA += currentDistanceA;
	}
	
	private void checkmouseB(){
		mouseB=0;
		currentDistanceB = 10000;
		double distance;
		if(rCount>1){
			for(int a=0;a<rCount;a++){
				distance = getDistance(xyaData[a][X_CENTER], xyaData[a][Y_CENTER],
						prevB[X_CENTER], prevB[Y_CENTER]);
				if((distance < currentDistanceB)&&(a!=mouseA)){
					mouseB = a;
					currentDistanceB = distance;				
				}
			}
			currentB=xyaData[mouseB];
		}

		if(rCount==1) currentB=xyaData[0];

		currentDistanceB = getDistance(currentB[X_CENTER], currentB[Y_CENTER],
				prevB[X_CENTER], prevB[Y_CENTER]);

		if(currentDistanceB > MAX_SEPARATION){
			currentDistanceB = 0;
			currentB=prevB;
		}

		totalDistanceB += currentDistanceB;
	}
	
	private void checkContact(){
		double distanceOfMice = getDistance(currentA[X_CENTER], currentA[Y_CENTER],currentB[X_CENTER], currentB[Y_CENTER]);
		
		if(distanceOfMice <= CONTACT_DIST){
			if(!contactFlag) contactNum++;
			ContactTime++;
			if((currentDistanceA >= MIN_ACT_DIST)||(currentDistanceB >= MIN_ACT_DIST))
				activeTime++;
			cfdFlag = cflFlag = false;

			if((currentDistanceA>=currentDistanceB)&&!contactFlag&&(currentDistanceA>=MIN_ACT_DIST)){
				cfdFlag = true;
				contactFlag = true;
				ncfA++;
				
			}
			if((currentDistanceB>currentDistanceA)&&!contactFlag&&(currentDistanceB>=MIN_ACT_DIST)){
				cflFlag = true;
				contactFlag = true;
				ncfB++;
			}

			contactFlag = true;
		}else{
            contactFlag = false;
		}
	}

	@Override
	public ImageProcessor getTraceImage(int sliceNum){
		ImageProcessor traceIp = (tracer.getTrace()).duplicate();

		if(sliceNum != 0 && ((double)sliceNum/Parameter.getInt(Parameter.rate))%60 ==0){
			tracer.clearTrace();
		}

		return traceIp;
	}

	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	@Override
	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + getElapsedTime(sliceNum)+ "\t" + (Math.round(ContactTime*10.0/(double)rate)/10.0) ;
	}

	@Override
	public String getXY(int sliceNum){
		String xyData="";
		xyData += "\n"+ sliceNum +"A" +"\t"+ currentA[X_CENTER] +"\t"+ currentA[Y_CENTER] +"\t"+ currentA[AREA];
		xyData += "\n"+ sliceNum +"B" +"\t"+ currentB[X_CENTER] +"\t"+ currentB[Y_CENTER] +"\t"+ currentB[AREA];
		xyData += contactFlag? "\t"+ "Contact"+(cfdFlag?" fromA":"")+(cflFlag?" fromB":"") :"";

		return xyData;
	}

	@Override
	public String[] getResults(){
		String[] results = new String[9];

		results[0] = String.valueOf(Math.round(ContactTime * 10.0 / (double)rate)  / 10.0);	//Contact�����b��
		results[1] = String.valueOf(Math.round(activeTime * 10.0 / (double)rate ) / 10.0);	//acitiveContact�̕b��
		results[2] = String.valueOf(Math.round(ContactTime *10.0 / ((double)rate * (double)contactNum)) / 10.0);	//
		results[3] = String.valueOf(Math.round(contactNum));	//nContact
		results[4] = String.valueOf(Math.round(ncfA));	//A�����ActiveContact�̉�
		results[5] = String.valueOf(Math.round(ncfB));	//B�����ActiveContact�̉�
		results[6] = String.valueOf(Math.round(totalDistanceA * 10.0) / 10.0);	//��͑S�̂�A������������(cm)
		results[7] = String.valueOf(Math.round(totalDistanceB * 10.0) / 10.0);	//��͑S�̂�B������������(cm)
		results[8] = String.valueOf(Math.round((totalDistanceA + totalDistanceB) * 5.0) / 10.0);//��Q�̕��ρi�V���ɒǉ��j
		
		return results;
	}

	@Override
	public void nextBin(){}
	@Override
	public String[] getBinResult(int option){return null;}
}