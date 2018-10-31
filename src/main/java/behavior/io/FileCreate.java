package behavior.io;

import java.io.*;

import behavior.util.GetDateTime;

import ij.*;

/**�t�@�C���̍쐬�A�������݂��ȒP�ɍs�����߂̃N���X
 */
public class FileCreate{
	private String path = null;

	public FileCreate(){
	}

	/**�p�X���w�肵�Ă����΁A��Ŏw�肵�Ȃ��Ă��悢
	 */
	public FileCreate(String path){
		this.path = path;
	}

	/**
	 �w�肵��path�Ńt�@�C�����쐬����iWindows�͊g���q�܂Ŏw��j�B
	 ���łɃt�@�C�������݂���ꍇ�͉����s��Ȃ��B
	 */
	public void createFile(String path){
		createFile(path,null,false,false);
	}

	/**
	 �w�肵��path�Ńt�@�C�����쐬����iWindows�͊g���q�܂Ŏw��j�B
	 �t�@�C�������݂��Ă��Ă��A�V�����쐬����i�ȑO�̃t�@�C���͏��������j
	 �i��w�̃t�H���_�����݂��Ȃ���IOException�ɂȂ�B�j
	 */
	public void createNewFile(String path){
		File file = new File(path);
		boolean flag = file.exists();
		if(flag){
			try{
				file.delete();
			}catch(Exception e){
				IJ.error(String.valueOf(e));
			}
		}
		try{
			file.createNewFile();
		}catch(Exception e){
			IJ.error(String.valueOf(e));
		}
	}

	/**�R���X�g���N�^�Ŏw�肳�ꂽ path �̃t�@�C�����쐬����B
	���łɃt�@�C�������݂���ꍇ�͉������Ȃ��B
	 */
	public void createNewFile(){
		if(path == null)
			throw new NullPointerException("path is null");
		createNewFile(path);
	}
	public void createFile(){
		if(path == null)
			throw new NullPointerException("path is null");
		createFile(path);
	}

	/**
	 �w�肵��path�Ńt�@�C�����쐬����Bline�ɕ�������w�肷��Ə������݂��s���B
	 line��null�̎��́A��̃t�@�C�����쐬����BnewLine��true�̏ꍇ�͑}���Afalse�ł͏㏑�����s���B
	 */
	public void createFile(String path, String line, boolean newLine, boolean character){
		File file = new File(path);
		if(file.exists()){
			if(line != null)
				write(path, line, newLine);
			return;
		}
		try{
			file.createNewFile();//�t�@�C�������݂��Ȃ��Ƃ��̂ݐV�K�쐬
		}catch(Exception e){
			IJ.error(String.valueOf(e));
		}
		if(!character){
			write(path,line,newLine);//�����񏑂�����
		}else{
			writeChar(path,line,newLine);
		}
	}

	/**�R���X�g���N�^�Ŏw�肳�ꂽ path �̃t�@�C�����쐬����B�����̈Ӗ��͑��Ɠ���
	 */
	public void createFile(String line, boolean newLine, boolean character){
		if(path == null)
			throw new NullPointerException("path is null");
		createFile(path, line, newLine, character);
	}


	/**
	 path�Ŏw�肵���t�@�C���ɕ�������������ށBnewLine��true�ő}���Afalse�ŏ㏑��(�ȑO�̂����Łj�B
	 */
	public void write(String path,String line,boolean newLine){

		BufferedWriter bw = null;
		try{
			bw = new BufferedWriter(new FileWriter(path,newLine));
			bw.write(line);
			if(newLine){
				bw.newLine();
			}
			bw.flush();
		}catch(Exception e){}
		finally{
			try{
				if(bw != null){
					bw.close();
				}
			}catch(Exception e){}
		}		
	}

	/**�R���X�g���N�^�Ŏw�肳�ꂽ path �̃t�@�C���ɏ������݂�����B�����̈Ӗ��͑��Ɠ���
	 */
	public void write(String line,boolean newLine){
		if(path == null)
			throw new NullPointerException("path is null");
		write(path, line, newLine);
	}

	/**
	 path�Ŏw�肵���t�@�C���ɕ������������ށBnewLine��true�ő}���Afalse�ŏ㏑���B
	 */
	public void writeChar(String path,String character,boolean newLine){
		BufferedWriter bw = null;
		try{

			bw = new BufferedWriter(new FileWriter(path,newLine));
			bw.write(character,0,character.length());
			if(newLine){
				bw.newLine();
			}
			bw.flush();
		}catch(Exception e){}
		finally{
			try{
				if(bw != null){
					bw.close();
				}
			}catch(Exception e){}
		}	
	}

	/*�w�肳�ꂽ path �̃t�@�C���ɁA������ǉ�����*/
	public void writeDate(String path){
		String date = GetDateTime.getInstance().getDateTimeString();
		writeChar(path, date, true);
	}


}
