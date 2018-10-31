package behavior.plugin.analyzer;

import ij.process.ImageProcessor;

import behavior.image.process.ImageManager;
import behavior.image.process.TracerFrame;
import behavior.setup.parameter.Parameter;

public abstract class Analyzer{
	/******
	この Analyze の state。結果を保存する際に、この state を参考にして結果の表示を返る
	 *******/
	public static final int COMPLETE = 1;	//解析が終了している
	public static final int INTERRUPT = 2;	//解析の途中
	public static final int NOTHING = 3;		//解析が始まっていない
	public static final int ONE_BIN_END = 4;	//ちょうど、bin が終わったところ（次の bin はまだ 0 画像の状態）
	/********
	ParticleAnalyzer から受け取る画像情報の配列の内容。０番目が X 座標、１番目が Y 座標…というふうに
	 *********/
	protected final int X_CENTER = 0;
	protected final int Y_CENTER = 1;
	protected final int AREA = 2;
	protected final int EXIST_FLAG = 3;	//かたまりが存在するかどうか。0 = 非存在、1 = 一つ存在、2 = 複数存在

	protected ImageManager imgManager;	//LD は構造が特殊なため、使わない。
	protected TracerFrame tracer;
	protected int mouseExistsCount = 0;	//一定時間連続してマウスが検出されないとスタートしない場合に使う。その際、startAnalyzeをオーバーライドする。
	protected int binLength, currentBin = 0, currentBinSlice = 0, state = COMPLETE;
	protected int pixelWidth = 0, pixelHeight = 0;

	/*解析*/
	public abstract void analyzeImage(ImageProcessor currentIp);
	public abstract void calculate(int currentSlice);
	public abstract void nextBin();
	/*結果を返す*/
	public abstract String getInfo(String subjectID, int sliceNum);
	public abstract String getXY(int sliceNum);
	public abstract String[] getResults();
	public abstract String[] getBinResult(int option); //option = 返却するデータの種類（各 analyze でフィールドを作ること）

	public Analyzer(){
		if(!binUsed()) return;
		binLength = Parameter.getInt(Parameter.duration) / Parameter.getInt(Parameter.binDuration);
		/* bin に分けると端数がでるようなら、bin の長さを一つ長くする必要がある*/
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
	 始点(sx,sy)から終点(dx,dy)までの距離を計算する。結果は実寸(cm)で返す。
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
	 * getInfo 用。ElapsedTime を返す。
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