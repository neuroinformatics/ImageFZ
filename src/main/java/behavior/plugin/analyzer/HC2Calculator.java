package behavior.plugin.analyzer;
import behavior.setup.parameter.Parameter;
import java.util.Arrays;

/**
 * HC2の計算をするクラス
 */
public class HC2Calculator{
	private static final int X = 0;
	private static final int Y = 1;
	private static final int AREA = 2;
	private int particle_number; //current picture の Particle Number
	private int prevParticle_number; //previous pictureにあるパーティクルの数
	private int[][] plot;        //current picture の plot
	private int[][] previous;    //previous picture の plot
	private int[] mouseA_plot = new int[4];   //current picture の mouseA
	private int[] prev_mouseA_plot = new int[4];//previous picture の mouseA
	private int[] mouseB_plot = new int[4];   //current picture の mouseB
	private int[] prev_mouseB_plot = new int[4];//previous picture の mouseB
	private int[][] mouseplot = new int[2][4];
	private int mouseA_number;  //plotにおけるmouseAの在り処
	private int mouseB_number;  //plotにおけるmouseBの在り処
	private boolean Interact = false;

	private int pixelWidth;
	private int pixelHeight;


	public HC2Calculator(int width,int height){
		pixelWidth = width;
		pixelHeight= height;
	}
	public void initializeArrays(){
		for(int i = 0; i < 2; i++)
			Arrays.fill(mouseplot[i], 0);
	}
//	**計算用
	//二点間の距離を返却する int getDistance(int[] plot, int[] plot2) メソッド
	private double getDistance(int[] plot, int[] plot2){
		if(plot[X] + plot[Y] == 0 || plot2[X] + plot2[Y] == 0)
			return 0.0;
		if(pixelWidth == 0 || pixelHeight == 0)
			throw new IllegalStateException("Analyze.setImageSize(int, int) was not called");
		float distX = plot[X] - plot2[X];
		float distY = plot[Y] - plot2[Y];
		double lengthPerPixelHor = (double)Parameter.getInt(Parameter.frameWidth) / pixelWidth;
		double lengthPerPixelVer = (double)Parameter.getInt(Parameter.frameHeight) / pixelHeight;
		double distance = Math.sqrt(Math.pow(distX * lengthPerPixelHor, 2) + Math.pow(distY * lengthPerPixelVer, 2));
		return distance;
	}

	//plot の中で一番面積の大きいパーティクルの番号を返却する int largest(int[ ][ ] plot) メソッド
	private int largest(int[][] plot){
		int length = plot.length;

		int number = 0;
		int area = plot[number][AREA];

		for(int k = 1; k < length; k++){
			if(area < plot[k][AREA]){
				number = k;
				area = plot[k][AREA];
			}
		}
		return number;
	}

	//plot の中で選択された番号を除いて、一番面積の大きいのを返す int largestWithoutI(int[ ][ ] plot, int i) 
	private int largestWithoutI(int[][] plot, int i){
		int length = plot.length;

		int number = 0;
		int area = plot[number][AREA];

		for(int k = 1; k < length; k++){
			if(k == i) continue;

			if(area < plot[k][AREA]){
				number = k;
				area = plot[k][AREA];
			}
		}
		return number;
	}

	//前回も今回も複数パーティクルのとき、距離の合計が最小になるような番号をかえす int[ ] shortest2x2(int[ ][ ] plot, int[ ][ ] previous)
	private int[] shortest2x2(int[][] plot, int[][] previous){
		int[] numberA = nearest2(plot, previous[0]); //mouseA の番号
		int[] numberB = nearest2(plot, previous[1]); //mouseB の番号

		if(numberA[0] != numberB[0]){	//両者が違うときは何の問題もない
			int[] ans = { numberA[0], numberB[0] };
			return ans;
		}
		//両者が重なったときは距離が最小になるように

		//A は最小、B は二番目、にした場合の距離合計 patternA
		int patternA = (int)(getDistance(plot[numberA[0]], previous[0]) + getDistance(plot[numberB[1]], previous[1]));
		//逆の patternB
		int patternB = (int)(getDistance(plot[numberA[1]], previous[0]) + getDistance(plot[numberB[0]], previous[1]));

		if(patternA <= patternB){
			int[] ans = { numberA[0], numberB[1] };
			return ans;
		}
		else{
			int[] ans = { numberA[1], numberB[0] };
			return ans;
		}
	}

	//plot の中で一番大きいものと二番目に大きいものを返す int[ ] largest2(int[ ][ ] plot) メソッド
	//plot の length が１のときは {0, 0} を返す
	/*	private int[] largest2(int[][] plot){
		int length = plot.length;

		int number1 = 0, number2 = 0;
		int area1 = plot[number1][AREA], area2 = 0; //area2 は０にしておかないと、0 番目が最大のとき困る

		for(int k = 1; k < length; k++){
			if(area1 < plot[k][AREA]){
				number2 = number1;	//今まで一番だったのがその地位を明け渡し、二番になる
				area2 = area1;
				number1 = k;
				area1 = plot[k][AREA];
			}
			else if(area2 < plot[k][AREA]){
				number2 = k;
				area2 = plot[k][AREA];
			}
		}
		int[] ans = {number1, number2};
		return ans;
	}*/



	//前回のパーティクルと一番近いものの番号を返す int nearest(int[ ][ ] plot, int[ ] previous)
	/*	private int nearest(int[][] plot, int[] previous){
		int length = plot.length;
		int[] dist = new int[length];
		int number = 0;

		for (int i = 0; i < length; i++){
			dist[i] =(int)getDistance(plot[i],previous);
			if (dist[0] > dist[i]){
				number = i;
				dist[0] = dist[i];
			}
		}
		return number;
	}*/

	//前回のパーティクルから一番近いのと二番目の番号を返す int[ ] nearest2(int[ ][ ] plot, int[ ] previous)
	private int[] nearest2(int[][] plot, int[] previous){
		int length = plot.length;
		int shortest = Integer.MAX_VALUE;
		int[] dist = new int[length];
		int[] number = new int[2];

		for (int i = 0; i < length; i++){
			dist[i] = (int)getDistance(plot[i],previous);
			if (shortest > dist[i]){
				number[0] = i;
				shortest = dist[i];

			}
		}

		shortest = Integer.MAX_VALUE;
		for (int i = 0;i < length; i++){
			if(i == number[0]) continue;
			if (shortest > dist[i]){
				number[1] = i;
				shortest = dist[i];
			}
		}
		return number;
	}



	void setPlot(int[][] pl){  //HC2analyzeからパーティクルデータをもらう
		plot = pl;
		particle_number = plot.length;
		judgePict();
	}

	void judgePict(){ //一枚目かどうかの判断
		if (previous == null)
			firsPictCalc();
		else secPictCalc();
	}

	void firsPictCalc(){ 
		if (particle_number == 1){  //パーティクルが一つの場合(Interacting)
			Interact = true;
			mouseA_number = mouseB_number = 0;
			mouseA_plot = mouseB_plot = plot[mouseA_number];

		}else {                     //パーティクルが二つの場合
			mouseA_number = largest(plot);
			mouseA_plot = plot[mouseA_number];
			mouseB_number = largestWithoutI(plot,mouseA_number);
			mouseB_plot = plot[mouseB_number];
		}
		previous = plot;
		prevParticle_number = particle_number;

	}

	void secPictCalc(){
		if (prevParticle_number != 1 && particle_number != 1) // prevはパーティクルが2つで、currentでもパーティクルが2つのとき
			calc2_2();
		else if (prevParticle_number != 1 && particle_number == 1) //prevはパーティクルが2つで、currentでinteractingしているとき
			calcCurrentInteracting();  
		else if (prevParticle_number == 1 && particle_number != 1)  //prevはinteractingで、currentで離れたとき
			calc1_2(); 
		else if (prevParticle_number == 1 && particle_number == 1)  //prevはinteractingで、currentで引き続きinteractingのとき
			calcCurrentInteracting();
		previous = plot;
		prevParticle_number = particle_number;

		mouseplot[0] = mouseA_plot;
		mouseplot[1] = mouseB_plot;

		prev_mouseA_plot = mouseA_plot;
		prev_mouseB_plot = mouseB_plot;

	}

	void calc2_2(){//前回二匹のマウスが確認され,パーティクル数が２の場合
		int[][] prev = new int[2][];
		prev[0] = prev_mouseA_plot;
		prev[1] = prev_mouseB_plot;
		int[] num = shortest2x2(plot,prev);
		mouseA_number = num[0];
		mouseB_number = num[1];
		mouseA_plot = plot[mouseA_number];
		mouseB_plot = plot[mouseB_number];

	}

	void calcCurrentInteracting(){//current でinteractingの場合
		Interact = true;
		mouseA_number = mouseB_number = 0; 
		mouseA_plot = mouseB_plot = plot[mouseA_number];

	}

	void calc1_2(){//前回interacting と判断され,パーティクルが離れた場合
		Interact = false;
		int[] num = nearest2(plot,mouseA_plot);
		mouseA_number = num[0];
		mouseB_number = num[1];
		mouseA_plot = plot[mouseA_number];
		mouseB_plot = plot[mouseB_number];

	}

	public int[][] returnplot(){
		return mouseplot;
	}

	/**xy data として表示するための plot を返却。
	mouseA, mouseB の plot を先頭に持ってくる。 by okazawa*/
	public int[][] returnplotForXYData(){
		if(plot == null)
			throw new IllegalStateException("plot is null");

		int[][] ansPlot = new int[plot.length][]; //return する plot. これに plot を上手く入れ替えつつデータを入れていく。
		ansPlot[0] = mouseA_plot; //1番目は否応なしに、mouseA が入るはず。
		if(plot.length > 1)
			ansPlot[1] = mouseB_plot; //2つ以上パーティクルがあれば、2番目は mouseB になるはず。
		if(plot.length > 2){
			int num = 2;
			for(int i = 0; i < plot.length; i++){
				//mouseA でも B でもなければ。 XY座標が違えば、明らかに A, B でないことが分かる
				if((plot[i][0] != mouseA_plot[0] || plot[i][1] != mouseA_plot[1]) && (plot[i][0] != mouseB_plot[0] || plot[i][1] != mouseB_plot[1])){
					if(num >= ansPlot.length)
						continue; //応急処置
					ansPlot[num++] = plot[i];
				}
			}
		}
		return ansPlot;
	}

	public int getParticleNumber(){ //current pictureのParticle Numberを返す。メンバー変数Interactの真偽で判断
		int particle;
		if (!Interact) particle = 2;
		else particle = 1;
		return particle;
	}

}


