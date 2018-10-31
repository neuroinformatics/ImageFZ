package behavior.io;

import behavior.io.FileCreate;
import behavior.io.FileManager;

/**数値データの保存を行うクラス*/

public class DataSaver{
	public static final int TOTAL_RESULT_PATH = 1;
	public static final int BIN_RESULT_PATH = 2;

	protected String path;
	protected String[] XHeader, YHeader;
	protected String[][] data;

	/**コンストラクタ。パスの指定*/
	public DataSaver(String path){
		this.path = path;
	}

	/**フィールドを指定して、よく使われるパスを自動取得。name は、bin result の場合の名前。それ以外は null でよし。*/
	public DataSaver(int fileType, String name){
		if(fileType == TOTAL_RESULT_PATH)
			path = (FileManager.getInstance()).getPath(FileManager.totalResPath);
		else if(fileType == BIN_RESULT_PATH)
			path = (FileManager.getInstance()).getBinResultPath(name);
	}

	/**横方向（ファイルの一行目）に入れるヘッダの指定*/
	public void setXHeader(String[] header){
		XHeader = header;
	}

	/**横方向（ファイルの一行目）に入れるヘッダの簡易指定
	　top	head1	head2	head3	....
	  と書かれる。*/
	public void setSequentialXHeader(String top, String head, int num){
		XHeader = new String[num + 1];
		XHeader[0] = top;
		for(int i = 1; i <= num; i++)
			XHeader[i] = head + i;
	}

	/**縦方向（各行の先頭）に入れるヘッダの指定*/
	public void setYHeader(String[] header){
		YHeader = header;
	}

	/**縦方向（各行の先頭）に入れるヘッダの簡易指定*/
	public void setSequentialYHeader(String head, int num){
		YHeader = new String[num];
		for(int i = 0; i < num; i++)
			YHeader[i] = head + (i + 1);
	}

	/**結果データのセット。data[縦方向（Y軸）座標][横方向座標]*/
	public void setData(String[][] data){
		this.data = data;
	}

	/**結果の保存*/
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
