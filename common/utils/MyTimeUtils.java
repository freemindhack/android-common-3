package nocom.common.utils;

public class MyTimeUtils {

	public static byte[] getUnique16(byte[] prefix, byte[] suffix) {
		/* Date curDate = new Date(System.currentTimeMillis()); */
		byte[] ret;
		if ((null != prefix) && (null == suffix)) {
			ret = new byte[16 + prefix.length];
		} else if ((null == prefix) && (null != suffix)) {
			ret = new byte[16 + suffix.length];
		} else if ((null != prefix) && (null != suffix)) {
			ret = new byte[16 + prefix.length + suffix.length];
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

	public static long makeTimeout(long ms) {
		long currentTimeMillis = System.currentTimeMillis();

		return currentTimeMillis + ms;
	}

	public static boolean isTimeout(long timeoutWhen) {
		long currentTimeMillis = System.currentTimeMillis();

		return currentTimeMillis >= timeoutWhen;
	}
}
