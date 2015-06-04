package nocom.common.utils;


import java.text.SimpleDateFormat;
import java.util.Date;


import android.annotation.SuppressLint;
import android.text.format.Time;


public class MyTimeUtils {

	public static byte[] getUnique16 (byte[] prefix,
		byte[] suffix) {
		/* Date curDate = new Date(System.currentTimeMillis()); */
		byte[] ret;
		if ((null != prefix) && (null == suffix)) {
			ret = new byte[16 + prefix.length];
		} else if ((null == prefix) && (null != suffix)) {
			ret = new byte[16 + suffix.length];
		} else if ((null != prefix) && (null != suffix)) {
			ret =
				new byte[16 + prefix.length + suffix.length];
		} else {
			ret = new byte[16];
		}

		int currenIndex = 0;
		if ((null != prefix) && (prefix.length > 0)) {
			for (int i = 0; i < prefix.length; ++i) {
				ret[currenIndex + i] = prefix[i];
			}
			currenIndex += prefix.length;
		}

		long currentTimeMillis = System.currentTimeMillis();
		String unique16 = String.valueOf(currentTimeMillis);
		byte[] _unique16 = unique16.getBytes();
		int left = _unique16.length;
		for (int i = 0; i < left; ++i) {
			ret[currenIndex + i] = _unique16[i];
		}
		currenIndex += left;

		if (left < 16) {
			left = 16 - unique16.length();
		} else {
			left = 0;
		}

		for (int i = 0; i < left; ++i) {
			ret[currenIndex + i] = '0';
		}
		currenIndex += left;

		if ((null != suffix) && (suffix.length > 0)) {
			for (int i = 0; i < suffix.length; ++i) {
				ret[currenIndex + i] = suffix[i];
			}
			currenIndex += suffix.length;
		}

		return ret;
	}


	public static long makeTimeout (long ms) {
		long currentTimeMillis = System.currentTimeMillis();

		return currentTimeMillis + ms;
	}


	public static boolean isTimeout (long timeoutWhen) {
		long currentTimeMillis = System.currentTimeMillis();

		return currentTimeMillis >= timeoutWhen;
	}


	public static long nowMs () {
		return System.currentTimeMillis();
	}


	@SuppressLint ("SimpleDateFormat")
	public static Time nowTime () {
		Date curDate = new Date(System.currentTimeMillis());

		SimpleDateFormat formatter =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String timestampStr = formatter.format(curDate);

		return new Time(timestampStr);
	}


	public static Date nowDate () {
		return new Date(System.currentTimeMillis());
	}


	@SuppressLint ("SimpleDateFormat")
	public static String nowTimestampStr () {
		Date curDate = new Date(System.currentTimeMillis());

		SimpleDateFormat formatter =
			new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return formatter.format(curDate);
	}


	@SuppressLint ("SimpleDateFormat")
	public static String timestampStr (Date datetime) {
		try {
			if (null == datetime) {
				return null;
			}

			SimpleDateFormat formatter =
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			return formatter.format(datetime);
		} catch (Exception e) {
			return null;
		}
	}


	public static boolean isTimeout (long baseMs,
		long nowMs, long timeoutMs) {
		if ((nowMs < baseMs)
			|| ((nowMs - baseMs) >= timeoutMs)) {
			return true;
		} else {
			return false;
		}
	}
}
