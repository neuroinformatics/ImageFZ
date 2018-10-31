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
	 コンストラクタに何も指定しない場合、1つの画面を扱う。
	 */
	public OnuParticleAnalyze(){
		nIp = 1;
	}

	/**
	 引数に指定された画面数を扱う。
	 */
	public OnuParticleAnalyze(int nIp){
		this.nIp = nIp;
	}

	/**
	 解析の元になる値を引数で指定。画面数は1。
	 */
	public OnuParticleAnalyze(int[] prevPlot){
		nIp = 1;
		this.prevPlot = prevPlot;
	}

	/**前回の plot をセット
	 */
	public void setPlot(int[] prevPlot){
		this.prevPlot = prevPlot;
	}

	/**パラメータをセット
	 *@param minSize 認識する最小パーティクル
	 */
	public void setParameter(int minSize, int maxSize){
		this.minSize = minSize;
		this.maxSize = maxSize;
	}


	/**
	 1つの画面につき、解析を行う。引数は、解析を行う画像、画像プロセッサ、使用するResultsTable、
	 Particle認識を行う最小値、最大値、実際の物体横幅、縦幅。plotAll内容：plotAll[0][]に真のParticle情報を代入。
	 Particleが複数認識された場合、plotAll[1][]以降に偽のParticle情報を代入。plotAll[n][0]=X座標、plotAll[n][1]=Y座標、
	 plotAll[n][2]=面積、plotAll[n][3]=Particle　Flag（nullのとき0x00,1のとき0x01、2以上のとき0x10）。
	 */
	public int[][] analyzeParticle(ImageProcessor ip){
		if(rt == null)
			rt = new ResultsTable();
		rt.reset();
		ParticleAnalyzer centerPa = new ParticleAnalyzer(options,centerMeasure,rt,minSize,maxSize);
		ip = ip.convertToByte(false);
		centerPa.analyze(new ImagePlus("", ip), ip);
//		int nParticles = rt.getCounter();//パーティクル数
		float[] dx = rt.getColumn(ResultsTable.X_CENTROID);//全パーティクルの真ん中のX座標
		float[] dy = rt.getColumn(ResultsTable.Y_CENTROID);//全パーティクルの真ん中のY座標
		rt.reset();
		ParticleAnalyzer areaPa = new ParticleAnalyzer(options,areaMeasure,rt,minSize,maxSize);
		areaPa.analyze(new ImagePlus("", ip), ip);
		float[] dArea = rt.getColumn(ResultsTable.AREA);//全パーティクル各面積
		int index;
		if(dx == null){//パーティクルが認識されない場合、前のパーティクル情報をそのまま代入。
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
				//複数パーティクルの場合、前のパーティクル座標が(0,0)ならば（すなわち初めてパーティクル認識されるとき）、面積が最大のものを真とする。
				if(prevPlot[0]==0 && prevPlot[1]==0){
					index = maxArea(dArea);//パーティクル面積の配列で、面積が最大となるindexを求める。
				}else{
					//複数パーティクルで前のパーティクル座標が(0,0)でないときは、前のパーティクルから最も近い場所にいるパーティクルを真とする。
					double[] distance = new double[dx.length];
					for(int n=0;n<dx.length;n++){
						distance[n] = calculateDistance(prevPlot[0],prevPlot[1],Math.round(dx[n]),Math.round(dy[n]));//新しく認識されたパーティクルと前のパーティクルの距離を求める。
					}
					index = minDistance(distance);//距離の配列で、値が最小となるindexを求める。
				}

				//X座標、Y座標、面積の配列を入れ替える。真のパーティクル情報([index])を[0]へ、[0]に格納されていた情報を[index]へ。
				float tempX,tempY,tempArea;
				tempX = dx[0];//一時退避。
				tempY = dy[0];
				tempArea = dArea[0];
				dx[0] = dx[index];
				dx[index] = tempX;
				dy[0] = dy[index];
				dy[index] = tempY;
				dArea[0] = dArea[index];
				dArea[index] = tempArea;
				//配列plotAllに結果を代入する。
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
	 引数に指定された配列のうち、値が最小のもののindex値を返す。
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
	 引数に指定された配列のうち、値が最大のもののindex値を返す。
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



