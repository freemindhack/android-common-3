package nocom.common.utils;


public class ByteUtils {
	public static boolean isascii (int v) {
		return ((v >= 32) && (v <= 126));
	}


	public static boolean isxdigitChar (int v) {
		if (v >= '0' && v <= '9') {
			return true;
		}
		if (v >= 'a' && v <= 'f') {
			return true;
		}
		if (v >= 'A' && v <= 'A') {
			return true;
		}
		return false;
	}


	public static int xdigitChar2Number (int xdigitChar) {
		boolean valid = ByteUtils.isxdigitChar(xdigitChar);
		if (!valid) {
			return -1;
		} else {
			if (xdigitChar >= '0' && xdigitChar <= '9') {
				return xdigitChar - '0';
			}
			if (xdigitChar >= 'a' && xdigitChar <= 'f') {
				return xdigitChar - 'a';
			}
			if (xdigitChar >= 'A' && xdigitChar <= 'A') {
				return xdigitChar - 'A';
			}
			return -1;
		}
	}
}
