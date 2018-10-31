package behavior.io;

import behavior.io.FileCreate;
import behavior.util.GetDateTime;

/**�f�[�^�𒀎��I�ɕۑ����Ă����B
�f�[�^�́A����X�悱 �̓񎟌��i��d�z��j�̂��́B
����񕪂̃f�[�^�����������A�w�肳�ꂽ�t�@�C���ɓf���o���B
���A���^�C���Ƀf�[�^��ۑ����邱�Ƃ́A�\�����ʃv���O�����̒�~�ɋ����A����I�ł���B
 */

public class SequentialSaver{
	protected String path, head = null;
	protected String[][] data;
	protected int savedLine = -1; //���ݕۑ������̍s�i0�s�ڂ���j
	protected int fixedX = -1;
	protected int[] nextY;

	/**�R���X�g���N�^�B
	�����AxLength = �������i�f�[�^�����������ۑ�����Ă����j*/
	public SequentialSaver(int xLength, int yLength, String savePath){
		path = savePath;
		data = new String[yLength][xLength];
		for(int y = 0; y < yLength; y++)
			for(int x = 0; x < xLength; x++)
				data[y][x] = null;
		nextY = new int[xLength];
		for(int x = 0; x < xLength; x++)
			nextY[x] = 0;
	}

	/**head ���w��B�Ⴆ�� "bin" �Ǝw�肷��ƁA"bin3 0.1 0.5 1.2" �ȂǂƂ����悤�ɁA�����̍ŏ��Ƀw�b�_������*/
	public SequentialSaver(int xLength, int yLength, String savePath, String head){
		this(xLength, yLength, savePath);
		this.head = head;
	}

	/**�w�b�_���L�ځi�t�@�C���̈�ԏ�̍s�Ɂj�B
	�w�b�_�́A"index \t header[0] \t header[1] ..." �Ƃ����悤�ɋL�ڂ����B
	newline = true �Ȃ�����̃f�[�^�̉��ɏ���������Bfalse �Ȃ�����̃f�[�^�͏�����B*/
	public void writeHeader(String[] header, String index, boolean newLine){
		StringBuilder headerString = new StringBuilder(index != null? index + "\t" + header[0] : header[0]);
		for(int i = 1; i < header.length; i++)
			headerString.append("\t" + header[i]);
		headerString.append("\tCurrent Time");
		FileCreate create = new FileCreate();
		create.write(path, headerString.toString(), newLine);
	}

	/**�����̃f�[�^���A�w�肳�ꂽ x, y �ɓ����B0 <= x,y < xLength, yLength�B
	�@�@�f�[�^���~�ς��ꂽ�玩���ŕۑ�*/
	public void setData(int x, int y, String oneData){
		data[y][x] = oneData;
		if(canSave(y))
			save(y);
	}


	/**�����̃f�[�^�i�����j���A�w�肳�ꂽ x, y �ɓ����B*/
	public void setData(int y, String[] dataLine){
		for(int x = 0; x < data[y].length; x++)
			data[y][x] = dataLine[x];
		if(canSave(y))
			save(y);
	}


	/**x�������ɂ��ẮA���Ԗڂɓ���邩���Œ肵�Ă����ꍇ�Ăяo��*/
	public void fixXAxis(int x){
		if(x >= data[0].length)
			throw new IllegalArgumentException("�Œ肵�悤�Ƃ��� X ���W�͔͈͊O�̂��̂ł��B");
		fixedX = x;
	}

	public void setData(int y, String oneData){
		if(fixedX == -1)
			throw new IllegalStateException("���̃��\�b�h�� X ���W���Œ肳��Ă��鎞�̂ݎg���܂�");
		setData(fixedX, y, oneData);
	}

	public void setDataNext(String oneData){
		if(fixedX == -1)
			throw new IllegalStateException("���̃��\�b�h�� X ���W���Œ肳��Ă��鎞�̂ݎg���܂�");
		setData(fixedX, nextY[fixedX], oneData);
		nextY[fixedX]++;
	}



	/**�Z�[�u�ł���󋵂����f�B*/
	protected boolean canSave(int y){
		if(savedLine < y - 1)
			return false;
		boolean filled = true;
		for(int i = 0; i < data[y].length; i++)
			filled &= (data[y][i] != null);
		return filled;
	}

	/**��s�ۑ�*/
	protected void save(int y){
		GetDateTime time = GetDateTime.getInstance();

		String currentTime = time.getDateTimeString();
		StringBuilder saveString = new StringBuilder(head != null? head + (savedLine + 2) + "\t" + data[y][0] : data[y][0]);
		for(int i = 1; i < data[y].length; i++)
			saveString.append("\t" + data[y][i]);
		saveString.append("\t" + currentTime);
		FileCreate create = new FileCreate();
		create.write(path, saveString.toString(), true);
		savedLine++;

		// �ۑ��ς݂̃f�[�^�͉��
		for(int i = 1; i < data[y].length; i++)
			data[y][i] = null;
	}

	/**�r���I���B�ۑ�����Ă��Ȃ�����ۑ�����B�f�[�^��null �ł���΁A�w�肳�ꂽ�����ɒu��������*/
	public void end(String nullString){
		for(int y = savedLine + 1; y < data.length; y++){
			for(int i = 0; i < data[y].length; i++)
				if(data[y][i] == null)
					data[y][i] = nullString;
			StringBuilder saveString = new StringBuilder(head != null? head + (y + 1) + "\t" + data[y][0] : data[y][0]);
			for(int i = 1; i < data[y].length; i++)
				saveString.append("\t" + data[y][i]);
			FileCreate create = new FileCreate();
			create.write(path, saveString.toString(), true);
		}
	}

}