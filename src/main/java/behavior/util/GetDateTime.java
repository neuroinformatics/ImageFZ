package behavior.util;

import java.util.Calendar;

/**
 * ���݂̓��t�E������񋟂���
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
	 * �C���X�^���X�̎擾
	 * �����Ɏ����̍X�V���s���B
	 */
	public static GetDateTime getInstance(){
		if(getDateTime == null)
			getDateTime = new GetDateTime();
		getDateTime.update();
		return getDateTime;
	}

	/**
	 * �Ăяo���ꂽ���_�̎����ɍX�V�B
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
	 * ���t�E�����̔z��𓾂�B
	 */
	public int[] getDateTimeArray(){
		return dateTime;
	}

	/**
	 * ���t�E�����̕�����𓾂�B
	 * ������ "YYYY/MM/DD H:mm"
	 */
	public String getDateTimeString(){
		return dateTime[YEAR] + "/" +
		(dateTime[MONTH] < 10 ? "0" : "") + dateTime[MONTH] + "/" +
		(dateTime[DAY] < 10 ? "0" : "") + dateTime[DAY] + " " +
		dateTime[HOUR] + ":" +
		(dateTime[MINUTE] < 10 ? "0" : "") + dateTime[MINUTE];
	}

	/**
	 * ���t�̕�����𓾂�B
	 * ������ "MMDD"
	 */
	public String getDateString(){
		return (dateTime[MONTH] < 10 ? "0" : "") + dateTime[MONTH] +
		(dateTime[DAY] < 10 ? "0" : "") + dateTime[DAY];
	}


}
