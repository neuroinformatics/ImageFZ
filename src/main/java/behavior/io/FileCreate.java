package behavior.io;

import java.io.*;

import behavior.util.GetDateTime;

import ij.*;

/**ファイルの作成、書き込みを簡単に行うためのクラス
 */
public class FileCreate{
	private String path = null;

	public FileCreate(){
	}

	/**パスを指定しておけば、後で指定しなくてもよい
	 */
	public FileCreate(String path){
		this.path = path;
	}

	/**
	 指定したpathでファイルを作成する（Windowsは拡張子まで指定）。
	 すでにファイルが存在する場合は何も行わない。
	 */
	public void createFile(String path){
		createFile(path,null,false,false);
	}

	/**
	 指定したpathでファイルを作成する（Windowsは拡張子まで指定）。
	 ファイルが存在していても、新しく作成する（以前のファイルは消去される）
	 （上層のフォルダが存在しないとIOExceptionになる。）
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

	/**コンストラクタで指定された path のファイルを作成する。
	すでにファイルが存在する場合は何もしない。
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
	 指定したpathでファイルを作成する。lineに文字列を指定すると書き込みを行う。
	 lineがnullの時は、空のファイルを作成する。newLineがtrueの場合は挿入、falseでは上書きを行う。
	 */
	public void createFile(String path, String line, boolean newLine, boolean character){
		File file = new File(path);
		if(file.exists()){
			if(line != null)
				write(path, line, newLine);
			return;
		}
		try{
			file.createNewFile();//ファイルが存在しないときのみ新規作成
		}catch(Exception e){
			IJ.error(String.valueOf(e));
		}
		if(!character){
			write(path,line,newLine);//文字列書き込み
		}else{
			writeChar(path,line,newLine);
		}
	}

	/**コンストラクタで指定された path のファイルを作成する。引数の意味は他と同じ
	 */
	public void createFile(String line, boolean newLine, boolean character){
		if(path == null)
			throw new NullPointerException("path is null");
		createFile(path, line, newLine, character);
	}


	/**
	 pathで指定したファイルに文字列を書き込む。newLineがtrueで挿入、falseで上書き(以前のが消滅）。
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

	/**コンストラクタで指定された path のファイルに書き込みをする。引数の意味は他と同じ
	 */
	public void write(String line,boolean newLine){
		if(path == null)
			throw new NullPointerException("path is null");
		write(path, line, newLine);
	}

	/**
	 pathで指定したファイルに文字を書き込む。newLineがtrueで挿入、falseで上書き。
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

	/*指定された path のファイルに、日時を追加する*/
	public void writeDate(String path){
		String date = GetDateTime.getInstance().getDateTimeString();
		writeChar(path, date, true);
	}


}
