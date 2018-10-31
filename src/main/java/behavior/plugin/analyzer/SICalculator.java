package behavior.plugin.analyzer;

public class SICalculator {
	//private static final int X = 0;
	//private static final int Y = 1;
	private static final int AREA = 2;
	private int mouseA=0;
	private int mouseB=-1;
	private int[] prevA={0,0};
	private int[] prevB={0,0};
	private int[] beforeinteractionA = {0,0};//interaction���钼�O��A,B�̍��W��ۑ�
	private int[] beforeinteractionB = {0,0};
	private int[][] beforeinteraction = {beforeinteractionA,beforeinteractionB};//�����̔z��
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
		int[] result={0,0,0};//interaction,�}�E�XA�̈ړ������A�}�E�XB�̈ړ������̏�
		int particles = currentplot[0][3];
		int shor[];
		
		if(particles>=2){//�C���^���N�V�������Ă��Ă��K�����؂�ăp�[�e�B�N������2�ȏ�ɂȂ��Ă��܂����Ƃ�����̂ŏ������p�[�e�B�N���͖���
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
	
	
	//plot �̒��ň�Ԗʐς̑傫���p�[�e�B�N���̔ԍ���ԋp���� int largest(int[ ][ ] plot) ���\�b�h
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

	//plot �̒��őI�����ꂽ�ԍ��������āA��Ԗʐς̑傫���̂�Ԃ� int largestWithoutI(int[ ][ ] plot, int i) 
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

	//�O�������������p�[�e�B�N���̂Ƃ��A�����̍��v���ŏ��ɂȂ�悤�Ȕԍ��������� int[ ] shortest2x2(int[ ][ ] plot, int[ ][ ] previous)
	//�i���jgetDistance ���\�b�h�Anearest2 ���\�b�h��������̂Ƃ��č쐬���Ă��܂��B
	public int[] shortest2x2(int[][] plot, int[][] previous){
		int[] numberA = nearest2(plot, previous[0]); //mouseA �̔ԍ�
		int[] numberB = nearest2(plot, previous[1]); //mouseB �̔ԍ�

		if(numberA[0] != numberB[0]){	//���҂��Ⴄ�Ƃ��͉��̖����Ȃ�
			int[] ans = { numberA[0], numberB[0] };
			return ans;
		}
		//���҂��d�Ȃ����Ƃ��͋������ŏ��ɂȂ�悤��

		//A �͍ŏ��AB �͓�ԖځA�ɂ����ꍇ�̋������v patternA
		int patternA = getDistance(plot[numberA[0]], previous[0]) + getDistance(plot[numberB[1]], previous[1]);
		//�t�� patternB
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

	//plot �̒��ň�ԑ傫�����̂Ɠ�Ԗڂɑ傫�����̂�Ԃ� int[ ] largest2(int[ ][ ] plot) ���\�b�h
	//plot �� length ���P�̂Ƃ��� {0, 0} ��Ԃ�
	public int[] largest2(int[][] plot){
		int length = plot.length;

		int number1 = 0, number2 = 0;
		int area1 = plot[number1][AREA], area2 = 0; //area2 �͂O�ɂ��Ă����Ȃ��ƁA0 �Ԗڂ��ő�̂Ƃ�����

		for(int k = 1; k < length; k++){
			if(area1 < plot[k][AREA]){
				number2 = number1;	//���܂ň�Ԃ������̂����̒n�ʂ𖾂��n���A��ԂɂȂ�
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
		int disttmp1 = (px-pvx)*(px-pvx)+(py-pvy)*(py-pvy), disttmp2 = 100000000; //area2 �͂O�ɂ��Ă����Ȃ��ƁA0 �Ԗڂ��ő�̂Ƃ�����

		for(int k = 1; k < length; k++){
			px = plot[k][0];
			py = plot[k][1];
			distance = (px-pvx)*(px-pvx)+(py-pvy)*(py-pvy);
			if(disttmp1 > distance){
		
				number2 = number1;	//���܂ň�Ԃ������̂����̒n�ʂ𖾂��n���A��ԂɂȂ�
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
