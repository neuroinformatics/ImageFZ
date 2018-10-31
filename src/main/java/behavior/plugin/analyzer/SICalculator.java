package behavior.plugin.analyzer;

public class SICalculator {
	//private static final int X = 0;
	//private static final int Y = 1;
	private static final int AREA = 2;
	private int mouseA=0;
	private int mouseB=-1;
	private int[] prevA={0,0};
	private int[] prevB={0,0};
	private int[] beforeinteractionA = {0,0};//interactionする直前のA,Bの座標を保存
	private int[] beforeinteractionB = {0,0};
	private int[][] beforeinteraction = {beforeinteractionA,beforeinteractionB};//それらの配列
	private int nullmouse = -1;
	private boolean interaction=false;
	private int[][] currentplot;
	private int[][] previousplot = new int[2][4];
	private int[] resulttemp=null;
	private int[][] currenttemp= new int[2][4];
	
	public SICalculator(int[][] currentplot){
		this.currentplot= currentplot;


			
	}
	
	public void setOSI(int[][] currentplot){
		this.currentplot= currentplot;

		
		
	}
	
	public int[] calculate(int imagenumber){
		int[] result={0,0,0};//interaction,マウスAの移動距離、マウスBの移動距離の順
		int particles = currentplot[0][3];
		int shor[];
		
		if(particles>=2){//インタラクションしていても尻尾が切れてパーティクル数が2以上になってしまうことがあるので小さいパーティクルは無視
			int[] large2 = largest2(currentplot);
			int secoundlarge = large2[1];
			int largest = large2[0];
			currenttemp[0]=currentplot[largest];
			currenttemp[1]=currentplot[secoundlarge];
			currentplot=currenttemp;
			
			
			if(currentplot[1][2]<16)
				particles=1;
				
			
		}
		if (imagenumber==1){
			if(particles==1) mouseA=largest(currentplot);
			else {
				resulttemp = largest2(currentplot);
				mouseA = resulttemp[0];
				mouseB = resulttemp[1];
				}
		}	
				
		else{
			if(mouseB==nullmouse){
				if(particles == 1) mouseA=largest(currentplot);
				else {
					resulttemp = largest2(currentplot);
					mouseA = resulttemp[0];
					mouseB = resulttemp[1];
				}
			}
			else if (!interaction){
				if(particles == 1){
					beforeinteractionA[0]=currentplot[mouseA][0];
					beforeinteractionA[1]=currentplot[mouseA][1];
					beforeinteractionB[0]=currentplot[mouseB][0];
					beforeinteractionB[1]=currentplot[mouseB][1];
					beforeinteraction[0]=beforeinteractionA;
					beforeinteraction[1]=beforeinteractionB;
					mouseA=largest(currentplot);
					mouseB=largest(currentplot);
					interaction = true;
					result[0]++;
					//System.out.print(imagenumber);
					//System.out.print("\t");
				}
				else{
					shor = shortest2x2(currentplot,previousplot);
					mouseA=shor[0];
					mouseB=shor[1];
				}

			}
			else if (interaction){
				if(particles == 1) {
					result[0]++;
					//System.out.print(imagenumber);
					//System.out.print("\t");
				}
				else {
					interaction = false;
					shor = shortest2x2(currentplot,beforeinteraction);
					mouseA=shor[0];
					mouseB=shor[1];
					
				}
				
				
				
				
			}
				
				
				
				
			}
			
			
			
		System.out.print(""+imagenumber+" A:\t("+currentplot[mouseA][0]+","+currentplot[mouseA][1]+")\t\t");
		System.out.print("B:\t("+currentplot[mouseB][0]+","+currentplot[mouseB][1]+")\n");
			
		
			
			
		prevA[0]=currentplot[mouseA][0];
		prevA[1]=currentplot[mouseA][1];
		prevB[0]=currentplot[mouseB][0];
		prevB[1]=currentplot[mouseB][1];
		previousplot[0] = currentplot[mouseA];
		previousplot[1] = currentplot[mouseB];
		
		return result;
	}
	
	
	 public int getDistance(int[] plot, int[] plot2){
		int p1x=plot[0];
		int p1y=plot[1];
		int p2x=plot2[0];
		int p2y=plot2[1];
		double length=Math.sqrt((p1x-p2x)*(p1x-p2x)+(p1y-p2y)*(p1y-p2y));
		int ret = (int)length;
		return ret;
				
	}
	
	
	//plot の中で一番面積の大きいパーティクルの番号を返却する int largest(int[ ][ ] plot) メソッド
	 public   int largest(int[][] plot){
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
	 public int largestWithoutI(int[][] plot, int i){
		int length = plot.length;

		int number = 0;
		int area = plot[0][AREA];

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
	//（注）getDistance メソッド、nearest2 メソッドがあるものとして作成しています。
	public int[] shortest2x2(int[][] plot, int[][] previous){
		int[] numberA = nearest2(plot, previous[0]); //mouseA の番号
		int[] numberB = nearest2(plot, previous[1]); //mouseB の番号

		if(numberA[0] != numberB[0]){	//両者が違うときは何の問題もない
			int[] ans = { numberA[0], numberB[0] };
			return ans;
		}
		//両者が重なったときは距離が最小になるように

		//A は最小、B は二番目、にした場合の距離合計 patternA
		int patternA = getDistance(plot[numberA[0]], previous[0]) + getDistance(plot[numberB[1]], previous[1]);
		//逆の patternB
		int patternB = getDistance(plot[numberA[1]], previous[0]) + getDistance(plot[numberB[0]], previous[1]);

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
	public int[] largest2(int[][] plot){
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
	}
	
	public int nearest(int[][] plot, int[] previous){
		int length = plot.length;
		int distance = 10000000;
	
		int number = 0;
		int px = plot[number][0];
		int py = plot[number][1];
		int pvx = previous[0];
		int pvy = previous[1];
		int disttmp = (px-pvx)*(px-pvx)+(py-pvy)*(py-pvy);
		for(int k = 1; k < length; k++){
			px = plot[k][0];
			py = plot[k][1];
			distance = (px-pvx)*(px-pvx)+(py-pvy)*(py-pvy);
			if(distance < disttmp){
				number = k;
				disttmp = distance;
				
			}
		}
		return number;
				
	}
	
	public int[] nearest2(int[][] plot, int[] previous){
		int length = plot.length;
		int number1 = 0, number2 = 0;
		int px = plot[0][0];
		int py = plot[0][1];
		int pvx = previous[0];
		int pvy = previous[1];
		int distance = 100000000;
		int disttmp1 = (px-pvx)*(px-pvx)+(py-pvy)*(py-pvy), disttmp2 = 100000000; //area2 は０にしておかないと、0 番目が最大のとき困る

		for(int k = 1; k < length; k++){
			px = plot[k][0];
			py = plot[k][1];
			distance = (px-pvx)*(px-pvx)+(py-pvy)*(py-pvy);
			if(disttmp1 > distance){
		
				number2 = number1;	//今まで一番だったのがその地位を明け渡し、二番になる
				disttmp2 = disttmp1;
				number1 = k;
				disttmp1 = distance;
			}
			else if(disttmp2 > distance){
				number2 = k;
				disttmp2 = distance;
			}
		}
		int[] ans = {number1, number2};
		return ans;
	}




}
