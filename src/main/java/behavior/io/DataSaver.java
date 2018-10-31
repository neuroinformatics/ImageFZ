package behavior.io;

import behavior.io.FileCreate;
import behavior.io.FileManager;

/**���l�f�[�^�̕ۑ����s���N���X*/

public class DataSaver{
	public static final int TOTAL_RESULT_PATH = 1;
	public static final int BIN_RESULT_PATH = 2;

	protected String path;
	protected String[] XHeader, YHeader;
	protected String[][] data;

	/**�R���X�g���N�^�B�p�X�̎w��*/
	public DataSaver(String path){
		this.path = path;
	}

	/**�t�B�[���h���w�肵�āA�悭�g����p�X�������擾�Bname �́Abin result �̏ꍇ�̖��O�B����ȊO�� null �ł悵�B*/
	public DataSaver(int fileType, String name){
		if(fileType == TOTAL_RESULT_PATH)
			path = (FileManager.getInstance()).getPath(FileManager.totalResPath);
		else if(fileType == BIN_RESULT_PATH)
			path = (FileManager.getInstance()).getBinResultPath(name);
	}

	/**�������i�t�@�C���̈�s�ځj�ɓ����w�b�_�̎w��*/
	public void setXHeader(String[] header){
		XHeader = header;
	}

	/**�������i�t�@�C���̈�s�ځj�ɓ����w�b�_�̊ȈՎw��
	�@top	head1	head2	head3	....
	  �Ə������B*/
	public void setSequentialXHeader(String top, String head, int num){
		XHeader = new String[num + 1];
		XHeader[0] = top;
		for(int i = 1; i <= num; i++)
			XHeader[i] = head + i;
	}

	/**�c�����i�e�s�̐擪�j�ɓ����w�b�_�̎w��*/
	public void setYHeader(String[] header){
		YHeader = header;
	}

	/**�c�����i�e�s�̐擪�j�ɓ����w�b�_�̊ȈՎw��*/
	public void setSequentialYHeader(String head, int num){
		YHeader = new String[num];
		for(int i = 0; i < num; i++)
			YHeader[i] = head + (i + 1);
	}

	/**���ʃf�[�^�̃Z�b�g�Bdata[�c�����iY���j���W][���������W]*/
	public void setData(String[][] data){
		this.data = data;
	}

	/**���ʂ̕ۑ�*/
	public void save(){
		FileCreate create = new FileCreate(path);
		if(XHeader != null){
			String header = XHeader[0];
			for(int i = 1; i < XHeader.length; i++)
				header += "\t" + XHeader[i];
			create.write(header, true);
		}
		for(int i = 0; i < data.length; i++){
			String line = YHeader[i];
			for(int j = 0; j < data[0].length; j++)
				line += "\t" + data[i][j];
			create.write(line, true);
		}
	}


}
