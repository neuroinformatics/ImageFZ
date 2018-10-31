package behavior.util;

import java.util.Calendar;

/**
 * 現在の日付・時刻を提供する
 */
public class GetDateTime {
	public static final int YEAR = 0;
	public static final int MONTH = 1;
	public static final int DAY = 2;
	public static final int HOUR = 3;
	public static final int MINUTE = 4;
	public static final int SECOND = 5;

	private int isAM;
	private int[] dateTime;

	private static GetDateTime getDateTime;

	private GetDateTime(){
	}

	/**
	 * インスタンスの取得
	 * 同時に時刻の更新も行う。
	 */
	public static GetDateTime getInstance(){
		if(getDateTime == null)
			getDateTime = new GetDateTime();
		getDateTime.update();
		return getDateTime;
	}

	/**
	 * 呼び出された時点の時刻に更新。
	 */
	private void update(){
		dateTime = new int[6];
		Calendar cale = Calendar.getInstance();
		isAM = cale.get(Calendar.AM_PM);
		dateTime[YEAR] = cale.get(Calendar.YEAR);
		dateTime[MONTH] = cale.get(Calendar.MONTH) + 1;
		dateTime[DAY] = cale.get(Calendar.DAY_OF_MONTH);
		dateTime[HOUR] = (isAM == Calendar.AM) ? cale.get(Calendar.HOUR) : cale.get(Calendar.HOUR) + 12;
		dateTime[MINUTE] = cale.get(Calendar.MINUTE);
		dateTime[SECOND] = cale.get(Calendar.SECOND);
	}

	/**
	 * 日付・時刻の配列を得る。
	 */
	public int[] getDateTimeArray(){
		return dateTime;
	}

	/**
	 * 日付・時刻の文字列を得る。
	 * 書式は "YYYY/MM/DD H:mm"
	 */
	public String getDateTimeString(){
		return dateTime[YEAR] + "/" +
		(dateTime[MONTH] < 10 ? "0" : "") + dateTime[MONTH] + "/" +
		(dateTime[DAY] < 10 ? "0" : "") + dateTime[DAY] + " " +
		dateTime[HOUR] + ":" +
		(dateTime[MINUTE] < 10 ? "0" : "") + dateTime[MINUTE];
	}

	/**
	 * 日付の文字列を得る。
	 * 書式は "MMDD"
	 */
	public String getDateString(){
		return (dateTime[MONTH] < 10 ? "0" : "") + dateTime[MONTH] +
		(dateTime[DAY] < 10 ? "0" : "") + dateTime[DAY];
	}


}
