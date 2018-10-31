package behavior.image.process;

import java.util.Arrays;

import ij.process.*;

import behavior.image.process.OnuParticleAnalyze;
import behavior.image.process.OnuThresholder;
import behavior.setup.Program;
import behavior.setup.parameter.Parameter;

/*********
画像処理を統合的に処理するクラス。このクラスを介して全ての画像処理を行うことが出来る。
処理は、一ケージずつ。（一括して複数のケージを扱うことはできない）
 **********/
public class ImageManager{
	/**処理すべき画像を指定するフィールド、サブトラクト（引き算した）画像*/
	public static final int SUBTRACT_IMG = 1;
	/**処理すべき画像を指定するフィールド、XOR画像*/
	public static final int XOR_IMG = 2;
	/**処理すべき画像を指定するフィールド、トレース画像*/
	public static final int TRACE_IMG = 3;

	private ImageProcessor backIp, currentIp;
	private ImageProcessor subtractIp, xorIp, previousIp = null;

	//private Program program;//Water_Maze用に追加 Subtract内の記述を変更した。

	private final OrgParticleAnalyze org = new OrgParticleAnalyze(); // Org 内に前回の画像の情報が格納してあるので、初期化してはならない。

	/*******
	コンストラクタ。
	 *@param backIp バックグラウンド画像。
	 ********/
	public ImageManager(ImageProcessor backIp){
		this.backIp = backIp;
	}

	/******
	 * バックグラウンド画像のgetter
	 */	
	public ImageProcessor getBackIp(){
		return backIp;
	}

	/*******
	取得された現在の画像をセットする。画像は Roi で切ったあとのもの。
	 *@param currentIp roiでカットした一ケージ分の画像
	 ********/
	public void setCurrentIp(ImageProcessor currentIp){
		this.currentIp = currentIp;
	}

	public void setProgram(Program program){
		//this.program = program;
	}

	/*******
	現在の画像からバックグラウンドを引き算する。invertMode のときはその逆。
	 ********/
	public void subtract(){
		//if(program == Program.WM){
			//subtractIp = currentIp;
		//}else
		if(Parameter.getBoolean(Parameter.invertMode)){
			subtractIp = new ImageConverter().copyBits(backIp,currentIp, 0, 0, ImageConverter.Mode.SUBTRACT);
		}else{
			subtractIp = new ImageConverter().copyBits(currentIp,backIp, 0, 0, ImageConverter.Mode.SUBTRACT);
		}
	}

	/*******
	画像に threshold をかける（白黒にする）。
	 *@param type 画像指定フィールド
	 ********/
	public void applyThreshold(int type){
		applyThreshold(type, Parameter.getInt(Parameter.minThreshold), Parameter.getInt(Parameter.maxThreshold));
	}

	/*******
	画像に threshold をかける（白黒にする）。
	 *@param minThres 下の閾値
	 *@param maxThres 上の閾値。
	 ********/
	public void applyThreshold(int type, int minThres, int maxThres){
		ImageProcessor thresholdIp = getIp(type);
		OnuThresholder ot = new OnuThresholder(minThres, maxThres);
		ot.applyThreshold(thresholdIp);
	}

	/*******
	画像に肉付けする。
	 *@param type 画像指定フィールド
	 ********/
	public void dilate(int type){
		ImageProcessor dilateIp = getIp(type);
		dilateIp.dilate();
	}

	/*******
	パーティクルの端を削る
	 *@param type 画像指定フィールド
	 ********/
	public void erode(int type){
		ImageProcessor erodeIp = getIp(type);
		erodeIp.erode();
	}

	/*******
	画像の重心、面積などを解析する。
	 *@param plot 前回の解析結果（一番大きいパーティクルのみ）。最初は null でよい。
	 *@param type 画像指定フィールド
	 *@return int[パーティクル数][データの種類]。データは 0… x座標 1… y座標 2… 面積 3… パーティクル数
	 ********/
	public synchronized int[][] analyzeParticle(int type, int[] plot){
		ImageProcessor analyzeIp = getIp(type);
		if(plot == null){
			plot = new int[4];
			Arrays.fill(plot, 0);
		}
		OnuParticleAnalyze opa = new OnuParticleAnalyze(plot);
		
		if(type == SUBTRACT_IMG)
			opa.setParameter(Parameter.getInt(Parameter.minSize), Parameter.getInt(Parameter.maxSize));
		else if(type == XOR_IMG)
			opa.setParameter(0, 9999);
		analyzeIp.autoThreshold();
		
		return opa.analyzeParticle(analyzeIp);
	}
	

	/**analyzeParticle とやることは一緒。使用するアルゴリズムが違う。analyzeParticle でエラーが出る場合はこちらを推奨
	 */
	public synchronized int[][] analyzeParticleOrg(int type, int[] plot){
		ImageProcessor analyzeIp = getIp(type);
		if(type == SUBTRACT_IMG)
			org.setParameter(Parameter.getInt(Parameter.minSize), Parameter.getInt(Parameter.maxSize));
		else if(type == XOR_IMG)
			org.setParameter(0, 999999);
		//analyzeIp.autoThreshold();
		return org.analyzeParticle(analyzeIp);
	}


	/*******
	XOR(前回の画像との相違地を塗りつぶす）画像を作成する。
	 *@param xorThreshold xor 画像を作成する際の threshold. subtract 画像を使う場合は関係ない
	 *@param subtractBackground XOR方式の選択。現在の画像をそのまま XOR するか、subtract画像を使用するか。
	 ********/
	public void xorImage(int xorThreshold, boolean subtractBackground){
		if(previousIp == null)
			previousIp = backIp.duplicate();

		if(subtractBackground){
			xorIp = new ImageConverter().copyBits(previousIp,subtractIp, 0, 0, ImageConverter.Mode.XOR);
			previousIp = subtractIp;
		}else{
			ImageProcessor currentTempIp = currentIp.duplicate();
			OnuThresholder ot = new OnuThresholder(xorThreshold, Parameter.getInt(Parameter.maxThreshold));
			ot.applyThreshold(currentTempIp);
			xorIp = new ImageConverter().copyBits(previousIp,currentTempIp, 0, 0, ImageConverter.Mode.XOR);
			previousIp = currentTempIp;
		}
	}


	/*******
	画像のノイズを消す
	 *@param type 画像指定フィールド
	 ********/
	public void reduceNoise(int type){
		ImageProcessor ip = getIp(type);
		ip.medianFilter();
	}

	/*******
	画像を取得する。
	 *@param type 画像指定フィールド
	 ********/
	public ImageProcessor getIp(int type){
		switch(type){
		case SUBTRACT_IMG:	return subtractIp;
		case XOR_IMG:		return xorIp;
		default:	throw new IllegalArgumentException("no such image(" + type + ")");
		}
	}
}