package nocom.common.utils;


import android.util.Log;


public class NumericUtils {
	public static int Str2Int (String s, int radix) {
		try {
			if (null == s) {
				return 0;
			}
			int ret = Integer.parseInt(s, radix);
			return ret;
		} catch (Exception e) {
			Log.println(Log.WARN, "NumericUtils/Str2Int",
				"ERROR: " + e.getMessage());
			return 0;
		}
	}


	public static byte Str2Uint8 (String s, int radix) {
		int ret = NumericUtils.Str2Int(s, radix);
		return (byte) (0xff & ret);
	}
}
