package behavior.plugin.analyzer;

import java.util.ArrayList;
import java.util.Arrays;

import ij.process.ImageProcessor;

import behavior.setup.parameter.Parameter;
import behavior.setup.parameter.TSParameter;
import behavior.image.process.ImageManager;
import behavior.image.process.Tracer;

public class TSAnalyzer extends Analyzer{
	/*getBinResult(int) ���\�b�h�ŕԋp����f�[�^���w�肷�邽�߂̃t�B�[���h*/
	public static final int BIN_DISTANCE = 1;
	public static final int BIN_FREEZ_PERCENT = 2;
	public static final int BIN_XOR_AREA = 3;

	/*�}�E�X�������Ă��邩���Ȃ����̃t�B�[���h*/
	protected final int MOVE = 0;
	protected final int NO_MOVE = 1;

	protected int[][] xyaData, xorXyaData;	//X���W�AY���W�A�ʐρA�̏��Ƀf�[�^������Ƃ���BxyaData[�H�ڂ̂����܂�][�f�[�^�̎��]
	protected int[] prevXyaData, binXorArea;
	protected int currentDistance, noMove, conseqFreez = 0, nowXorArea;
	protected double[] binDistance, binFreezPercent, binFreez;

	protected boolean start = false;

	private ArrayList<String> xy_buffer = new ArrayList<String>();
	private ArrayList<String> xy_text = new ArrayList<String>(); 

	/**�T�u�N���X�ŌŗL�̃R���X�g���N�^����邽��
	 */
	protected TSAnalyzer(){
	}

	public TSAnalyzer(ImageProcessor backIp){
		imgManager = new ImageManager(backIp.duplicate()); //�Ƃ肠��������
		tracer = new Tracer(backIp);
		initializeArrays();
		setImageSize(backIp.getWidth(), backIp.getHeight());
	}

	protected void initializeArrays(){
		binDistance = new double[binLength];
		Arrays.fill(binDistance, 0);
		binFreezPercent  = new double[binLength];
		Arrays.fill(binFreezPercent, 0);
		binXorArea  = new int[binLength];
		Arrays.fill(binXorArea, 0);
		binFreez  = new double[binLength];
		Arrays.fill(binFreez, 0);
	}

	public void analyzeImage(ImageProcessor currentIp){
		imgManager.setCurrentIp(currentIp.duplicate());
		imgManager.subtract();
		imgManager.applyThreshold(ImageManager.SUBTRACT_IMG);
		xyaData = imgManager.analyzeParticleOrg(ImageManager.SUBTRACT_IMG, prevXyaData);

		imgManager.xorImage(Parameter.getInt(TSParameter.xorThreshold), Parameter.getBoolean(TSParameter.subtractBackground));
		imgManager.reduceNoise(ImageManager.XOR_IMG);
		if(Parameter.getBoolean(TSParameter.erode))
			imgManager.erode(ImageManager.XOR_IMG);
		xorXyaData = imgManager.analyzeParticleOrg(ImageManager.XOR_IMG, prevXyaData);
	}

	public void setStart(boolean set){
		if(start == true)
			return;
		start = set;
	}

	public boolean startAnalyze(int cage){
		return start;
	}

	public void calculate(int currentSlice){
		if(currentSlice > 0){	//���̌v�Z���ƁA�ŏ��̉摜�͉�͂̑Ώۂɓ���Ȃ����ƂɂȂ邪�Abehavior3 �������p���Łd
			nowXorArea = 0;
			for(int i = 0; i < xorXyaData.length; i++)
				nowXorArea += xorXyaData[i][AREA];
			binXorArea[currentBin] += nowXorArea;
			calculateFreez(currentBinSlice, nowXorArea);
			double currentDistance = getDistance(xyaData[0][X_CENTER], xyaData[0][Y_CENTER],
					prevXyaData[X_CENTER], prevXyaData[Y_CENTER]);
			binDistance[currentBin] += currentDistance;

			tracer.writeTrace(xyaData[0][X_CENTER], xyaData[0][Y_CENTER]);
			currentBinSlice++;
		}
	    tracer.setPrevXY(xyaData[0][0], xyaData[0][1]);
		prevXyaData = xyaData[0];
	}

	/*********
	�}�E�X�� Freezing ���v�Z����
	currentBinSlice = ���݂� bin �̉摜��
	nowXorArea = ���݂� XOR �摜�̃G���A
	 **********/
	protected void calculateFreez(int currentBinSlice, int nowXorArea){
		/*���s�N�Z���ȉ��̓����Ȃ�΁AFreez �𑝉�������*/
		if(nowXorArea <= Parameter.getInt(TSParameter.freezCriterion)){
			//�����Ă��Ȃ�
			conseqFreez++;
			noMove = NO_MOVE;
			binFreez[currentBin]++;
		}
		else{
			/*�����A�����Ă��āA������܂ł� Freez ���Ԃ��K��l�ɒB���Ă��Ȃ���΁A���̕��͂Ȃ��������Ƃɂ���*/
			if(conseqFreez < Parameter.getDouble(TSParameter.minFreezDuration) * Parameter.getInt(TSParameter.rate)){
				if(conseqFreez > currentBinSlice){	//�t���[�Y�� bin ���܂����Ő������ꍇ
					binFreez[currentBin] = 0;
					binFreez[currentBin - 1] -= conseqFreez - currentBinSlice;
				}
				else
					binFreez[currentBin] -= conseqFreez;
			}
			conseqFreez = 0;
			noMove = MOVE;
		}
	}

	public void nextBin(){
		binFreezPercent[currentBin] = (double)binFreez[currentBin] / (double)(Parameter.getInt(TSParameter.binDuration) * Parameter.getInt(TSParameter.rate)) * 100;
		currentBin++;
		currentBinSlice = 0;
	}

	public ImageProcessor getSubtractImage(){
		return imgManager.getIp(ImageManager.SUBTRACT_IMG);
	}

	public ImageProcessor getXorImage(){
		return imgManager.getIp(ImageManager.XOR_IMG);
	}

	public String getInfo(String subjectID, int sliceNum){
		return subjectID + "\t" + getElapsedTime(sliceNum) + "\t" + xyaData[0][0] + "\t" +
		xyaData[0][1] + "\t" + noMove + "\t" + nowXorArea;
	}

	@Override
	public String getXY(int sliceNum){
		String xyData = "";
		if(sliceNum==0){
			xyData = sliceNum +"\t"+ xyaData[0][0] +"\t"+ xyaData[0][1] +"\t"+ (xyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(xyaData[0][AREA]));
			xy_text.add(xyData);
		}else{
			xyData = sliceNum +"\t"+ xyaData[0][0] +"\t"+ xyaData[0][1] + "\t" + (xyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(xyaData[0][AREA])) + "\t" + (xorXyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(nowXorArea))+ "\t" + (noMove==MOVE?"":"Freezing") + "\t" + conseqFreez;
			if(nowXorArea>Parameter.getInt(TSParameter.freezCriterion) || conseqFreez > Parameter.getDouble(TSParameter.minFreezDuration) * Parameter.getInt(Parameter.rate)){
				if(xy_buffer.size()!=0){
					for(int i=0;i<xy_buffer.size();i++){
					    xy_text.add(xy_buffer.get(i) +"\t"+ "" +"\t"+ 0);
					}
				    xy_buffer.clear();
				}
				xy_text.add(xyData);
			}else{
				xy_buffer.add(sliceNum + "\t" + xyaData[0][0] + "\t" + xyaData[0][1] + "\t" + (xyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(xyaData[0][AREA])) + "\t" + (xorXyaData[0][EXIST_FLAG] == 0 ? "NP" : String.valueOf(nowXorArea)));
				if(conseqFreez == Parameter.getDouble(TSParameter.minFreezDuration) * Parameter.getInt(Parameter.rate)){
					for(int i=0;i<xy_buffer.size();i++){
					    xy_text.add(xy_buffer.get(i) +"\t"+ "Freezing" +"\t"+ (i+1));
					}
					xy_buffer.clear();
				}
				
			}
		}

		return xyData;
	}

	public ArrayList<String> getXYText(){
		if(xy_buffer.size()!=0){
			if(conseqFreez<Parameter.getDouble(TSParameter.minFreezDuration) * Parameter.getInt(Parameter.rate)){
				for(int i=0;i<xy_buffer.size();i++){
				    xy_text.add(xy_buffer.get(i) +"\t"+ "" +"\t"+ 0);
				}
			}else{
			    for(int i=0;i<xy_buffer.size();i++){
			        xy_text.add(xy_buffer.get(i) +"\t"+ "Freezing" +"\t"+ (i+1));
			    }
			}
		    xy_buffer.clear();
		}

		return xy_text;
	}

	public void interruptAnalysis(){
		if(currentBin == 0 && currentBinSlice == 0)
			state = NOTHING;
		else if(currentBin == binLength)
			state = COMPLETE;
		else if(currentBinSlice == 0){
			state = ONE_BIN_END;
			binLength = currentBin;
		}
		else{
			state = INTERRUPT;
			binLength = currentBin + 1;
			binFreezPercent[currentBin] = (double)(binFreez[currentBin] / currentBinSlice) * 100;
		}
	}

	public String[] getResults(){
		String[] results = new String[2];
		double totalDistance = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalDistance += binDistance[bin];
		int totalFreez = 0;
		for(int bin = 0; bin < binLength; bin++)
			totalFreez += binFreez[bin];
		double totalFreezPercent = (double)totalFreez / Parameter.getInt(Parameter.duration) * 100;
		results[0] = String.valueOf(Math.round(totalDistance * 10.0) / 10.0);	//��͑S�̂œ���������(cm)
		results[1] = String.valueOf(Math.round(totalFreezPercent * 10.0) / 10.0);
		return results;
	}

	public String[] getBinResult(int option){
		String[] result = new String[binLength];
		for(int bin = 0; bin < binLength; bin++){
			switch(option){
			case BIN_DISTANCE: result[bin] = String.valueOf(Math.round(binDistance[bin]*10.0)/10.0); break;
			case BIN_FREEZ_PERCENT: result[bin] = String.valueOf(Math.round(binFreezPercent[bin]*10.0)/10.0); break;
			case BIN_XOR_AREA: result[bin] = String.valueOf(binXorArea[bin]); break;
			default: throw new IllegalArgumentException("the option cannot be used in this method");
			}
		}
		if(state == INTERRUPT)
			result[binLength - 1] += "(" + currentBinSlice + "frame)";
		return result;
	}

}