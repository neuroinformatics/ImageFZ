package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import behavior.image.process.ImageManager;
import behavior.image.process.TracerFrame;
import behavior.setup.parameter.Parameter;

public abstract class Analyzer{
	/******
	���� Analyze �� state�B���ʂ�ۑ�����ۂɁA���� state ���Q�l�ɂ��Č��ʂ̕\����Ԃ�
	 *******/
	public static final int COMPLETE = 1;	//��͂��I�����Ă���
	public static final int INTERRUPT = 2;	//��͂̓r��
	public static final int NOTHING = 3;		//��͂��n�܂��Ă��Ȃ�
	public static final int ONE_BIN_END = 4;	//���傤�ǁAbin ���I������Ƃ���i���� bin �͂܂� 0 �摜�̏�ԁj
	/********
	ParticleAnalyzer ����󂯎��摜���̔z��̓��e�B�O�Ԗڂ� X ���W�A�P�Ԗڂ� Y ���W�c�Ƃ����ӂ���
	 *********/
	protected final int X_CENTER = 0;
	protected final int Y_CENTER = 1;
	protected final int AREA = 2;
	protected final int EXIST_FLAG = 3;	//�����܂肪���݂��邩�ǂ����B0 = �񑶍݁A1 = ����݁A2 = ��������

	protected ImageManager imgManager;	//LD �͍\��������Ȃ��߁A�g��Ȃ��B
	protected TracerFrame tracer;
	protected int mouseExistsCount = 0;	//��莞�ԘA�����ă}�E�X�����o����Ȃ��ƃX�^�[�g���Ȃ��ꍇ�Ɏg���B���̍ہAstartAnalyze���I�[�o�[���C�h����B
	protected int binLength, currentBin = 0, currentBinSlice = 0, state = COMPLETE;
	protected int pixelWidth = 0, pixelHeight = 0;

	/*���*/
	public abstract void analyzeImage(ImageProcessor currentIp);
	public abstract void calculate(int currentSlice);
	public abstract void nextBin();
	/*���ʂ�Ԃ�*/
	public abstract String getInfo(String subjectID, int sliceNum);
	public abstract String getXY(int sliceNum);
	public abstract String[] getResults();
	public abstract String[] getBinResult(int option); //option = �ԋp����f�[�^�̎�ށi�e analyze �Ńt�B�[���h����邱�Ɓj

	public Analyzer(){
		if(!binUsed()) return;
		binLength = Parameter.getInt(Parameter.duration) / Parameter.getInt(Parameter.binDuration);
		/* bin �ɕ�����ƒ[�����ł�悤�Ȃ�Abin �̒��������������K�v������*/
		if(Parameter.getInt(Parameter.duration) % Parameter.getInt(Parameter.binDuration) != 0)
			binLength++;
	}

	public boolean binUsed(){
		return true;
	}

	public void resetDuration(int duration){
		if(!binUsed()) return;
		binLength = (int)Math.floor(duration / (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)));
		if(duration % (Parameter.getInt(Parameter.binDuration)*Parameter.getInt(Parameter.rate)) != 0)
			binLength++;
		initializeArrays();
	}

	protected void initializeArrays(){}

	public ImageProcessor getTraceImage(int sliceNum){
		ImageProcessor traceIp = (tracer.getTrace()).duplicate();
		if(binUsed()){
		    if(sliceNum != 0 && sliceNum % (Parameter.getInt(Parameter.binDuration) * Parameter.getInt(Parameter.rate)) == 0){
			    tracer.clearTrace();
		    }
		}
		return traceIp;
	}

	protected void setImageSize(int pixelWidth, int pixelHeight){
		this.pixelWidth = pixelWidth;
		this.pixelHeight = pixelHeight;
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
		}
	}

	public int getState(){
		return state;
	}

	protected int atCenter(int x,int y, int centerArea){
		if(pixelWidth == 0 || pixelHeight == 0)
			throw new IllegalStateException("Analyze.setImageSize(int, int) was not called");
		double centerWidth = Math.sqrt(centerArea) / 10 * pixelWidth;
		double centerHeight = Math.sqrt(centerArea) / 10 * pixelHeight;
		double centerX1 = (pixelWidth - centerWidth) / 2;
		double centerY1 = (pixelHeight - centerHeight) / 2;
		double centerX2 = (pixelWidth + centerWidth) / 2;
		double centerY2 = (pixelHeight + centerHeight) / 2;
		if((x >= centerX1 && y >= centerY1) && (x <= centerX2 && y <= centerY2))
			return 1;
		else
			return 0;
	}

	/**	
	 �n�_(sx,sy)����I�_(dx,dy)�܂ł̋������v�Z����B���ʂ͎���(cm)�ŕԂ��B
	 */
	protected double getDistance(int sx,int sy,int dx,int dy){
		if(sx + sy == 0 || dx + dy == 0)
			return 0.0;
		if(pixelWidth == 0 || pixelHeight == 0)
			throw new IllegalStateException("Analyze.setImageSize(int, int) was not called");
		float distX = sx - dx;
		float distY = sy - dy;
		double lengthPerPixelHor = (double)Parameter.getInt(Parameter.frameWidth) / pixelWidth;
		double lengthPerPixelVer = (double)Parameter.getInt(Parameter.frameHeight) / pixelHeight;
		double distance = Math.sqrt(Math.pow(distX * lengthPerPixelHor, 2) + Math.pow(distY * lengthPerPixelVer, 2));
		return distance;
	}

	/**
	 * getInfo �p�BElapsedTime ��Ԃ��B
	 */
	protected String getElapsedTime(int sliceNum){
		if(Math.ceil((double) sliceNum / Parameter.getInt(Parameter.rate)) > (Parameter.getInt(Parameter.duration)))
			return "Finished";
		else
			return (sliceNum / Parameter.getInt(Parameter.rate)) + " / " + (Parameter.getInt(Parameter.duration));
	}

	public void Test(){
		System.out.println("Test");
	}
}