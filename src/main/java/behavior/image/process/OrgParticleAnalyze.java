package behavior.image.process;

import ij.process.ImageProcessor;

import java.awt.Color;

/**�摜��͂̃I���W�i�����@�[�W�����B
��邱�Ƃ́AOnuParticleAnalyze �Ɠ��l�A�p�[�e�B�N���i�����܂�j���ƂɖʐρAXY ���W������o���A����̏����Ŕz��Ɏ��߂ĕԂ��B
OnuParticleAnalyze �ł́AImageJ �ɉ�͂̕�����C���Ă������A�G���[���o�邽�߁A�I���W�i�����쐬�����B*/
public class OrgParticleAnalyze{
	protected int[] previousPlot = new int[4]; //��O�̉摜�̃f�[�^�iXY ���W�Ɩʐς� Flag)�B�}�E�X�̈ʒu�𐳊m�ɑ��肷�邽�߂Ɏg��

	//�ȉ��́A�摜��͂̂��߂̃����o
	byte[][] pixel; //�摜�̐��f�[�^
	byte[][] buf; //���łɓǂݎ��ς݂̃h�b�g�����`����Ă���f�[�^
	final int MAX_QUE = 200;
	int[][] que = new int[MAX_QUE][3]; //�o�b�t�@�B[�o�b�t�@�̐�][�f�[�^�̎��]
	int pWidth, pHeight; //�摜�̑傫��
	int queNum = -1, area, parNum = 0;
	int xAll, yAll;
	final int MAX_PARTICLE = 100;

	int black = 0;

	int minSize = 0, maxSize = 999999;

    private final int X_CENTER = 0;
	private final int Y_CENTER = 1;
	private final int AREA = 2;
	private final int EXIST_FLAG = 3;

	/**�p�����[�^���Z�b�g
	 *@param minSize �F������p�[�e�B�N���̍ŏ��T�C�Y
	 */
	public void setParameter(int minSize, int maxSize){
		this.minSize = minSize;
		this.maxSize = maxSize;
	}

	/**
	 1�̉�ʂɂ��A��͂��s���B�����́A��͂��s���摜�A�摜�v���Z�b�T�A�g�p����ResultsTable�A
	 Particle�F�����s���ŏ��l�A�ő�l�A���ۂ̕��̉����A�c���BplotAll���e�FplotAll[0][]�ɐ^��Particle�������B
	 Particle�������F�����ꂽ�ꍇ�AplotAll[1][]�ȍ~�ɋU��Particle�������BplotAll[n][0]=X���W�AplotAll[n][1]=Y���W�A
	 plotAll[n][2]=�ʐρAplotAll[n][3]=Particle�@Flag�inull�̂Ƃ�0x00,1�̂Ƃ�0x01�A2�ȏ�̂Ƃ�0x10�j�B
	 */
	public int[][] analyzeParticle(ImageProcessor ip){
		int size =ip.getWidth()*ip.getHeight();
		Object pixels = ip.getPixels();
		if(!(pixels instanceof byte[])){ //ip.convertToByte�����ł͑���Ȃ�
			int[] wrongPixel = (int[])pixels;
			byte[] safePixel = new byte[size];
			for(int j = 0; j < wrongPixel.length; j++)
				safePixel[j] = (byte)wrongPixel[j];
			ip = ip.convertToByte(false);
			ip.setPixels(safePixel);
		}

		//�O��̃f�[�^���N���A����
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
					//��荞�ݍς�
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

	/**buf �Ƀg���[�X(T) ���c���Ƃ��͂�����*/
	private boolean insideT(int width, int height){
		boolean ans = isBlackPixel(width, height);
		if(ans){
			buf[height][width] = 1;
		}
		return ans;
	}

	protected void calculate(){
		int i;
		/*�܂��́A���݂̃L���[�̏��Ɋւ��ẮA�ʐρAXY ���W���v�Z*/
		int w1 = que[queNum][0];
		int we1 = que[queNum][1];
		int h1 = que[queNum--][2];
		int length = we1-w1+1;
		area += length;
		xAll += ((we1+w1)/2)*length;
		yAll += length*h1;
		/*���ɁA�L���[�̏㑤�ɑ��݂��� inside �������o���ăL���[�ɉ�����*/
		if(h1 > 0){
			i = 1;
			/*�L���[���[��肳��ɏ㍶�� inside ���Ȃ������m�F*/
			if(insideT(w1, h1-1)){
				if(queNum < MAX_QUE-1) queNum++;
				que[queNum][2] = h1-1;
				for(i = 1; insideT(w1-i, h1-1); i++);
				que[queNum][0] = w1-i+1;
				for(i = 1; insideT(w1+i, h1-1); i++);
				que[queNum][1] = w1+i-1;
			}
			/*�L���[�̏㑤�� inside ���Ȃ������`�F�b�N*/
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
		/*�L���[�̉����ɂ��Ă����l�̃`�F�b�N*/
		if(h1 < pHeight - 1){
			i = 1;
			/*�L���[���[��肳��ɉ����� inside ���Ȃ������m�F*/
			if(insideT(w1, h1 + 1)){
				if(queNum < MAX_QUE - 1) queNum++;
				que[queNum][2] = h1 + 1;
				for(i = 1; insideT(w1 - i, h1 + 1); i++);
				que[queNum][0] = w1 - i + 1;
				for(i = 1; insideT(w1 + i, h1 + 1); i++);
				que[queNum][1] = w1 + i - 1;
			}
			/*�L���[�̉����� inside ���Ȃ������`�F�b�N*/
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

	//�擾�����p�[�e�B�N���̔z������ɍ��킹�ĕ��בւ���B��Ԗʐς��傫���p�[�e�B�N�����őO��Ɏ����Ă���
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
				//��uParticle�𑨂��Ȃ������ꍇ�ɍ��W����Ԃ̂�h������
				allParticle[0] = previousPlot;
				allParticle[0][AREA] = 0; //�ʐς�0�ɂ���
				allParticle[0][EXIST_FLAG] = 0; //flag��0�ɂ���
			}
			return allParticle;
		}

		int[][] sortedParticle = new int[parNum][4];
		for(int i=0; i<parNum; i++){
			sortedParticle[i] = particle[i];
		}
		
		/*�ŏ��Ƃ���Ȍ�ŕ������HC1�ŕ����̃p�[�e�B�N�����F�����ꂽ�ꍇ�ɐ��������삵�Ȃ��̂�
		 *��Ɉ�Ԗʐς̑傫���p�[�e�B�N����擪�Ɏ����Ă���悤�ɂ����B
		 *HC�Q�ȂǂŖ�肪����悤�Ȃ�HC�P�Ƃ���ȊO�ł��̕�����ʁX�ɏ����B
		 */
		
		/*�ŏ��̉摜�ł́A��Ԗʐς̑傫���p�[�e�B�N����擪�Ɏ����Ă���*/
		//if(previousPlot == null || (previousPlot[0] == 0 && previousPlot[1] == 0)){
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
		//}
		/*����Ȍ�́A�O�̍��W�Ɉ�ԋ߂��p�[�e�B�N����擪�Ɏ����Ă���*/
		/*else{
			int shortest = 0;
			int shortestDistance = Integer.MAX_VALUE;
			for(int i = 0; i < parNum; i++){
				int dist;
				if((dist = distance(previousPlot[0], previousPlot[1], allParticle[i][0], allParticle[i][1])) < shortestDistance){
					shortestDistance = dist;
					shortest = i;
				}
			}
			for(int i = 0; i < 4; i++){
				int buffer = allParticle[0][i];
				allParticle[0][i] = allParticle[shortest][i];
				allParticle[shortest][i] = buffer;
			}
		}*/
		sortedParticle[0][EXIST_FLAG] = parNum==1 ? 1 : 2;

		for(int i=0; i<4; i++){
			previousPlot[i] = sortedParticle[0][i];
		}

		return sortedParticle;
	}
}