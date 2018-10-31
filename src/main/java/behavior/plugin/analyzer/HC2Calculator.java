package behavior.plugin.analyzer;
import behavior.setup.parameter.Parameter;
import java.util.Arrays;

/**
 * HC2�̌v�Z������N���X
 */
public class HC2Calculator{
	private static final int X = 0;
	private static final int Y = 1;
	private static final int AREA = 2;
	private int particle_number; //current picture �� Particle Number
	private int prevParticle_number; //previous picture�ɂ���p�[�e�B�N���̐�
	private int[][] plot;        //current picture �� plot
	private int[][] previous;    //previous picture �� plot
	private int[] mouseA_plot = new int[4];   //current picture �� mouseA
	private int[] prev_mouseA_plot = new int[4];//previous picture �� mouseA
	private int[] mouseB_plot = new int[4];   //current picture �� mouseB
	private int[] prev_mouseB_plot = new int[4];//previous picture �� mouseB
	private int[][] mouseplot = new int[2][4];
	private int mouseA_number;  //plot�ɂ�����mouseA�݂̍菈
	private int mouseB_number;  //plot�ɂ�����mouseB�݂̍菈
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
//	**�v�Z�p
	//��_�Ԃ̋�����ԋp���� int getDistance(int[] plot, int[] plot2) ���\�b�h
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

	//plot �̒��ň�Ԗʐς̑傫���p�[�e�B�N���̔ԍ���ԋp���� int largest(int[ ][ ] plot) ���\�b�h
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

	//plot �̒��őI�����ꂽ�ԍ��������āA��Ԗʐς̑傫���̂�Ԃ� int largestWithoutI(int[ ][ ] plot, int i) 
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

	//�O�������������p�[�e�B�N���̂Ƃ��A�����̍��v���ŏ��ɂȂ�悤�Ȕԍ��������� int[ ] shortest2x2(int[ ][ ] plot, int[ ][ ] previous)
	private int[] shortest2x2(int[][] plot, int[][] previous){
		int[] numberA = nearest2(plot, previous[0]); //mouseA �̔ԍ�
		int[] numberB = nearest2(plot, previous[1]); //mouseB �̔ԍ�

		if(numberA[0] != numberB[0]){	//���҂��Ⴄ�Ƃ��͉��̖����Ȃ�
			int[] ans = { numberA[0], numberB[0] };
			return ans;
		}
		//���҂��d�Ȃ����Ƃ��͋������ŏ��ɂȂ�悤��

		//A �͍ŏ��AB �͓�ԖځA�ɂ����ꍇ�̋������v patternA
		int patternA = (int)(getDistance(plot[numberA[0]], previous[0]) + getDistance(plot[numberB[1]], previous[1]));
		//�t�� patternB
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

	//plot �̒��ň�ԑ傫�����̂Ɠ�Ԗڂɑ傫�����̂�Ԃ� int[ ] largest2(int[ ][ ] plot) ���\�b�h
	//plot �� length ���P�̂Ƃ��� {0, 0} ��Ԃ�
	/*	private int[] largest2(int[][] plot){
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
	}*/



	//�O��̃p�[�e�B�N���ƈ�ԋ߂����̂̔ԍ���Ԃ� int nearest(int[ ][ ] plot, int[ ] previous)
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

	//�O��̃p�[�e�B�N�������ԋ߂��̂Ɠ�Ԗڂ̔ԍ���Ԃ� int[ ] nearest2(int[ ][ ] plot, int[ ] previous)
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



	void setPlot(int[][] pl){  //HC2analyze����p�[�e�B�N���f�[�^�����炤
		plot = pl;
		particle_number = plot.length;
		judgePict();
	}

	void judgePict(){ //�ꖇ�ڂ��ǂ����̔��f
		if (previous == null)
			firsPictCalc();
		else secPictCalc();
	}

	void firsPictCalc(){ 
		if (particle_number == 1){  //�p�[�e�B�N������̏ꍇ(Interacting)
			Interact = true;
			mouseA_number = mouseB_number = 0;
			mouseA_plot = mouseB_plot = plot[mouseA_number];

		}else {                     //�p�[�e�B�N������̏ꍇ
			mouseA_number = largest(plot);
			mouseA_plot = plot[mouseA_number];
			mouseB_number = largestWithoutI(plot,mouseA_number);
			mouseB_plot = plot[mouseB_number];
		}
		previous = plot;
		prevParticle_number = particle_number;

	}

	void secPictCalc(){
		if (prevParticle_number != 1 && particle_number != 1) // prev�̓p�[�e�B�N����2�ŁAcurrent�ł��p�[�e�B�N����2�̂Ƃ�
			calc2_2();
		else if (prevParticle_number != 1 && particle_number == 1) //prev�̓p�[�e�B�N����2�ŁAcurrent��interacting���Ă���Ƃ�
			calcCurrentInteracting();  
		else if (prevParticle_number == 1 && particle_number != 1)  //prev��interacting�ŁAcurrent�ŗ��ꂽ�Ƃ�
			calc1_2(); 
		else if (prevParticle_number == 1 && particle_number == 1)  //prev��interacting�ŁAcurrent�ň�������interacting�̂Ƃ�
			calcCurrentInteracting();
		previous = plot;
		prevParticle_number = particle_number;

		mouseplot[0] = mouseA_plot;
		mouseplot[1] = mouseB_plot;

		prev_mouseA_plot = mouseA_plot;
		prev_mouseB_plot = mouseB_plot;

	}

	void calc2_2(){//�O���C�̃}�E�X���m�F����,�p�[�e�B�N�������Q�̏ꍇ
		int[][] prev = new int[2][];
		prev[0] = prev_mouseA_plot;
		prev[1] = prev_mouseB_plot;
		int[] num = shortest2x2(plot,prev);
		mouseA_number = num[0];
		mouseB_number = num[1];
		mouseA_plot = plot[mouseA_number];
		mouseB_plot = plot[mouseB_number];

	}

	void calcCurrentInteracting(){//current ��interacting�̏ꍇ
		Interact = true;
		mouseA_number = mouseB_number = 0; 
		mouseA_plot = mouseB_plot = plot[mouseA_number];

	}

	void calc1_2(){//�O��interacting �Ɣ��f����,�p�[�e�B�N�������ꂽ�ꍇ
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

	/**xy data �Ƃ��ĕ\�����邽�߂� plot ��ԋp�B
	mouseA, mouseB �� plot ��擪�Ɏ����Ă���B by okazawa*/
	public int[][] returnplotForXYData(){
		if(plot == null)
			throw new IllegalStateException("plot is null");

		int[][] ansPlot = new int[plot.length][]; //return ���� plot. ����� plot ����肭����ւ��f�[�^�����Ă����B
		ansPlot[0] = mouseA_plot; //1�Ԗڂ͔ۉ��Ȃ��ɁAmouseA ������͂��B
		if(plot.length > 1)
			ansPlot[1] = mouseB_plot; //2�ȏ�p�[�e�B�N��������΁A2�Ԗڂ� mouseB �ɂȂ�͂��B
		if(plot.length > 2){
			int num = 2;
			for(int i = 0; i < plot.length; i++){
				//mouseA �ł� B �ł��Ȃ���΁B XY���W���Ⴆ�΁A���炩�� A, B �łȂ����Ƃ�������
				if((plot[i][0] != mouseA_plot[0] || plot[i][1] != mouseA_plot[1]) && (plot[i][0] != mouseB_plot[0] || plot[i][1] != mouseB_plot[1])){
					if(num >= ansPlot.length)
						continue; //���}���u
					ansPlot[num++] = plot[i];
				}
			}
		}
		return ansPlot;
	}

	public int getParticleNumber(){ //current picture��Particle Number��Ԃ��B�����o�[�ϐ�Interact�̐^�U�Ŕ��f
		int particle;
		if (!Interact) particle = 2;
		else particle = 1;
		return particle;
	}

}


