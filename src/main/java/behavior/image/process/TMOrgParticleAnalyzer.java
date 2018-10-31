package behavior.image.process;

import ij.process.ImageProcessor;

import java.awt.Color;
import java.awt.geom.Point2D;

//新しいTMOnlineを作成できたら使用予定。
public class TMOrgParticleAnalyzer{
	protected int[] previousPlot = new int[4]; //一つ前の画像のデータ（XY 座標と面積と Flag)。マウスの位置を正確に測定するために使う

	//以下は、画像解析のためのメンバ
	byte[][] pixel; //画像の生データ
	byte[][] buf; //すでに読み取り済みのドットだけ描かれているデータ
	final int MAX_QUE = 200;
	int[][] que = new int[MAX_QUE][3]; //バッファ。[バッファの数][データの種類]
	int pWidth, pHeight; //画像の大きさ
	int queNum = -1, area, parNum = 0;
	int xAll, yAll;
	final int MAX_PARTICLE = 100;

	int black = 0;

	int minSize = 0, maxSize = 999999;

    private final int X_CENTER = 0;
	private final int Y_CENTER = 1;
	private final int AREA = 2;
	private final int EXIST_FLAG = 3;

	/**パラメータをセット
	 *@param minSize 認識するパーティクルの最小サイズ
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
		int size =ip.getWidth()*ip.getHeight();
		Object pixels = ip.getPixels();
		if(!(pixels instanceof byte[])){ //ip.convertToByteだけでは足りない
			int[] wrongPixel = (int[])pixels;
			byte[] safePixel = new byte[size];
			for(int j = 0; j < wrongPixel.length; j++)
				safePixel[j] = (byte)wrongPixel[j];
			ip = ip.convertToByte(false);
			ip.setPixels(safePixel);
		}

		//前回のデータをクリアする
		que = new int[MAX_QUE][3];
		queNum = -1;
		parNum = 0;

		setBlack(ip);
		byte[] linearPixel = (byte[])ip.getPixels();
		pWidth = ip.getWidth();
		pHeight = ip.getHeight();
		pixel = new byte[pHeight][pWidth];
		for(int h=0; h<pHeight; h++){
			for(int w=0; w<pWidth; w++){
				pixel[h][w] = linearPixel[w+h*pWidth];
			}
		}

		buf = new byte[pHeight][pWidth];
		int[][] particle = new int[MAX_PARTICLE][4];
		for(int h=0; h<pHeight; h++){
			for(int w=0; w<pWidth; w++){
				if(isBlackPixel(w, h)){
					//取り込み済み
					buf[h][w] = 1;

					area = 0;
					xAll = yAll = 0;
					start(w, h);
					if(area >= minSize && area <= maxSize){
						particle[parNum] = new int[4];
						particle[parNum][X_CENTER] = (int)Math.round((double)xAll / area);
						particle[parNum][Y_CENTER] = (int)Math.round((double)yAll / area);
						particle[parNum][AREA] = area;
						if(parNum<MAX_PARTICLE-1){
							parNum++;
						}
					}
				}
			}
		}

		return sort(particle);
	}

	private void setBlack(ImageProcessor ip){
		ImageProcessor temp = ip.duplicate();
		temp.setColor(Color.black);
		temp.drawPixel(0, 0);
		black = (byte)temp.getPixel(0, 0);
	}

	private boolean isBlackPixel(int width, int height){
		if(width<0 || height<0 || width>=pWidth || height>=pHeight)
			return false;
		return (pixel[height][width] == black && buf[height][width] == 0);
	}

	protected void start(int w, int h){
		int i;
		for(i=1; insideT(w+i, h); i++);
		if(queNum < MAX_QUE-1){
			queNum++;
		}
		que[queNum][0] = w;
		que[queNum][1] = w+i-1;
		que[queNum][2] = h;
		while(queNum >= 0){
			calculate();
		}
	}

	/**buf にトレース(T) を残すときはこちら*/
	private boolean insideT(int width, int height){
		boolean ans = isBlackPixel(width, height);
		if(ans){
			buf[height][width] = 1;
		}
		return ans;
	}

	protected void calculate(){
		int i;
		/*まずは、現在のキューの情報に関しての、面積、XY 座標を計算*/
		int w1 = que[queNum][0];
		int we1 = que[queNum][1];
		int h1 = que[queNum--][2];
		int length = we1-w1+1;
		area += length;
		xAll += ((we1+w1)/2)*length;
		yAll += length*h1;
		/*次に、キューの上側に存在する inside を見つけ出してキューに加える*/
		if(h1 > 0){
			i = 1;
			/*キュー左端よりさらに上左に inside がないかも確認*/
			if(insideT(w1, h1-1)){
				if(queNum < MAX_QUE-1) queNum++;
				que[queNum][2] = h1-1;
				for(i = 1; insideT(w1-i, h1-1); i++);
				que[queNum][0] = w1-i+1;
				for(i = 1; insideT(w1+i, h1-1); i++);
				que[queNum][1] = w1+i-1;
			}
			/*キューの上側に inside がないかをチェック*/
			for(; w1+i<=we1; i++){
				if(insideT(w1+i, h1-1)){
					if(queNum < MAX_QUE-1) queNum++;
					que[queNum][0] = w1+i++;
					for(; insideT(w1+i, h1-1); i++);
					que[queNum][1] = w1+i-1;
					que[queNum][2] = h1-1;
				}
			}
		}
		/*キューの下側についても同様のチェック*/
		if(h1 < pHeight - 1){
			i = 1;
			/*キュー左端よりさらに下左に inside がないかも確認*/
			if(insideT(w1, h1 + 1)){
				if(queNum < MAX_QUE - 1) queNum++;
				que[queNum][2] = h1 + 1;
				for(i = 1; insideT(w1 - i, h1 + 1); i++);
				que[queNum][0] = w1 - i + 1;
				for(i = 1; insideT(w1 + i, h1 + 1); i++);
				que[queNum][1] = w1 + i - 1;
			}
			/*キューの下側に inside がないかをチェック*/
			for(; w1 + i <= we1; i++){
				if(insideT(w1 + i, h1 + 1)){
					if(queNum < MAX_QUE - 1) queNum++;
					que[queNum][0] = w1 + i++;
					for(; insideT(w1 + i, h1 + 1); i++);
					que[queNum][1] = w1 + i - 1;
					que[queNum][2] = h1 + 1;
				}
			}
		}
	}

	//取得したパーティクルの配列を情報に合わせて並べ替える。一番面積が大きいパーティクルを最前列に持ってくる
	protected int[][] sort(int[][] particle){
		if(parNum == 0){
			int[][] allParticle = new int[1][4];
			if(previousPlot == null){
				allParticle[0][X_CENTER] = 0;
				allParticle[0][Y_CENTER] = 0;
				allParticle[0][AREA] = 0;
				allParticle[0][EXIST_FLAG] = 0;
				previousPlot = allParticle[0];
			}else{
				//一瞬Particleを捉えなかった場合に座標が飛ぶのを防ぐため
				allParticle[0] = previousPlot;
				allParticle[0][AREA] = 0; //面積は0にする
				allParticle[0][EXIST_FLAG] = 0; //flagも0にする
			}
			return allParticle;
		}

		int[][] sortedParticle = new int[parNum][4];
		for(int i=0; i<parNum; i++){
			sortedParticle[i] = particle[i];
		}
		
		/*最初の画像では、一番面積の大きいパーティクルを先頭に持ってくる*/
		
		if(previousPlot == null || (previousPlot[0] == 0 && previousPlot[1] == 0)){
			int largest = 0;
			int largestArea = 0;
			for(int i=0; i<parNum; i++){
				if(sortedParticle[i][AREA] > largestArea){
					largestArea = sortedParticle[i][AREA];
					largest = i;
				}
			}
			for(int i=0; i<4; i++){
				int buffer = sortedParticle[0][i];
				sortedParticle[0][i] = sortedParticle[largest][i];
				sortedParticle[largest][i] = buffer;
			}
		}else{
			double[] dist = new double[parNum];
			for(int i=0;i<parNum;i++){
				dist[i] =  Point2D.distance(previousPlot[0], previousPlot[1], sortedParticle[i][0], sortedParticle[i][1]);
			}

			int shortest = 0;
			double n = dist[0];
			for(int i=0;i<parNum;i++){
				if(dist[i]<n){
					n=dist[i];
					shortest = i;
				}
			}

			dist[shortest] = Integer.MAX_VALUE;
			int secondryShortest = 0;
			double m = dist[0];
			for(int i=0;i<parNum;i++){
				if(dist[i]<m){
					m=dist[i];
					secondryShortest = i;
				}
			}

			if((sortedParticle[shortest][2]-previousPlot[2]) <= (sortedParticle[secondryShortest][2]-previousPlot[2])){
				for(int i=0; i<4; i++){
				    int buffer = sortedParticle[0][i];
				    sortedParticle[0][i] = sortedParticle[shortest][i];
				    sortedParticle[shortest][i] = buffer;
				}
			}else{
				for(int i=0; i<4; i++){
				    int buffer = sortedParticle[0][i];
				    sortedParticle[0][i] = sortedParticle[secondryShortest][i];
				    sortedParticle[secondryShortest][i] = buffer;
				}
			}
		}

		sortedParticle[0][EXIST_FLAG] = parNum==1 ? 1 : 2;

		for(int i=0; i<4; i++){
			previousPlot[i] = sortedParticle[0][i];
		}

		return sortedParticle;
	}
}