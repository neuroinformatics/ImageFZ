package behavior.io;

import behavior.io.FileCreate;
import behavior.util.GetDateTime;

/**データを逐次的に保存していく。
データは、たてXよこ の二次元（二重配列）のもの。
横一列分のデータがそろったら、指定されたファイルに吐き出す。
リアルタイムにデータを保存することは、予期せぬプログラムの停止に強く、安定的である。
 */

public class SequentialSaver{
	protected String path, head = null;
	protected String[][] data;
	protected int savedLine = -1; //現在保存完了の行（0行目から）
	protected int fixedX = -1;
	protected int[] nextY;

	/**コンストラクタ。
	引数、xLength = 横方向（データがそろったら保存されていく）*/
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

	/**head を指定。例えば "bin" と指定すると、"bin3 0.1 0.5 1.2" などというように、横軸の最初にヘッダが入る*/
	public SequentialSaver(int xLength, int yLength, String savePath, String head){
		this(xLength, yLength, savePath);
		this.head = head;
	}

	/**ヘッダを記載（ファイルの一番上の行に）。
	ヘッダは、"index \t header[0] \t header[1] ..." というように記載される。
	newline = true なら既存のデータの下に書き加える。false なら既存のデータは消える。*/
	public void writeHeader(String[] header, String index, boolean newLine){
		StringBuilder headerString = new StringBuilder(index != null? index + "\t" + header[0] : header[0]);
		for(int i = 1; i < header.length; i++)
			headerString.append("\t" + header[i]);
		headerString.append("\tCurrent Time");
		FileCreate create = new FileCreate();
		create.write(path, headerString.toString(), newLine);
	}

	/**引数のデータを、指定された x, y に入れる。0 <= x,y < xLength, yLength。
	　　データが蓄積されたら自動で保存*/
	public void setData(int x, int y, String oneData){
		data[y][x] = oneData;
		if(canSave(y))
			save(y);
	}


	/**引数のデータ（複数）を、指定された x, y に入れる。*/
	public void setData(int y, String[] dataLine){
		for(int x = 0; x < data[y].length; x++)
			data[y][x] = dataLine[x];
		if(canSave(y))
			save(y);
	}


	/**x軸方向については、何番目に入れるかを固定しておく場合呼び出す*/
	public void fixXAxis(int x){
		if(x >= data[0].length)
			throw new IllegalArgumentException("固定しようとした X 座標は範囲外のものです。");
		fixedX = x;
	}

	public void setData(int y, String oneData){
		if(fixedX == -1)
			throw new IllegalStateException("このメソッドは X 座標が固定されている時のみ使えます");
		setData(fixedX, y, oneData);
	}

	public void setDataNext(String oneData){
		if(fixedX == -1)
			throw new IllegalStateException("このメソッドは X 座標が固定されている時のみ使えます");
		setData(fixedX, nextY[fixedX], oneData);
		nextY[fixedX]++;
	}



	/**セーブできる状況か判断。*/
	protected boolean canSave(int y){
		if(savedLine < y - 1)
			return false;
		boolean filled = true;
		for(int i = 0; i < data[y].length; i++)
			filled &= (data[y][i] != null);
		return filled;
	}

	/**一行保存*/
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

		// 保存済みのデータは解放
		for(int i = 1; i < data[y].length; i++)
			data[y][i] = null;
	}

	/**途中終了。保存されていない分を保存する。データがnull であれば、指定された文字に置き換える*/
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