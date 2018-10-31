package behavior.image.process;

import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

/**�g���[�X���L�q*/
public class Tracer implements TracerFrame{
	private ImageProcessor traceIp;
	private int prevX=0;
	private int prevY=0;

	/**
	 *@param backIp �o�b�N�O���E���h�摜
	 */
	public Tracer(ImageProcessor backIp){
		traceIp = backIp.duplicate();
		clearTrace();
	}

	/**
	 *@param width �����摜�̑傫��
	 */
	public Tracer(int width, int height){
		traceIp = new ByteProcessor(width, height);
		traceIp.invertLut();
	}

	/**���݂̃g���[�X�摜��^�����ɏ�����
	 */
	public void clearTrace(){
		byte[] pixel = (byte[])traceIp.getPixels();
		for(int num = 0; num < pixel.length; num++)
			pixel[num] = 0;
	}

	public void setPrevXY(int x,int y){
		this.prevX=x;
		this.prevY=y;
	}

	public void writeTrace(int x,int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && prevX != 0 && prevY != 0)
			traceIp.drawLine(prevX, prevY, x, y);
	}

	/**�g���[�X�̒�����`��
	 *@param px �n�_�i�O��̍��W�j
	 *@param x �I�_�i����̉摜�j
	 */
	public void writeTrace(int px, int py, int x, int y){
		traceIp.setValue(255);
		if(x != 0 && y != 0 && px != 0 && py != 0)
			traceIp.drawLine(px, py, x, y);
	}

	public ImageProcessor getTrace(){
		return traceIp;
	}
}