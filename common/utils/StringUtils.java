/** ===========================================================================
 *
 *       Filename:  StringUtils.java
 *
 *    Description:
 *
 * - Mode: 644
 * - NOTE. NOT use "dos"
 * - Updated: 2015-5-9
 *
 *        Version:  1.0.1
 *        Created:  2015-05-12
 *       Revision:  1.0.0
 *       Compiler:  ADT
 *
 *         Author:  Yui Wong, email: yuiwong@126.com
 *   Organization:  ""
 *
 * ===========================================================================
 */
package nocom.common.utils;


import org.apache.http.util.EncodingUtils;


public class StringUtils {

	/**
	 * @return: 0: NOT and arg-fail; < 0: NOT and fail at "-1 * ret - 0xff"; 1:
	 *          YES 2: NOT and pureAZ; 3: NOT and pureNumber
	 */
	public static int isAZAndNumberString (String s) {
		boolean pureAZ = true;
		boolean pureNumber = true;
		if ((null == s) || (s.length() <= 0)) {
			return 0;
		}

		int l = s.length();
		for (int i = 0; i < l; ++i) {
			char c = s.charAt(i);
			if (((c >= 'a') && (c <= 'z'))
				|| ((c >= 'A') && (c <= 'Z'))) {
				pureNumber = false;
			} else if ((c >= '0') && (c <= '9')) {
				pureAZ = false;
			} else {
				return (i + 0xff) * -1;
			}
		}

		if (pureAZ) {
			return 2;
		} else if (pureNumber) {
			return 3;
		} else {
			return 1;
		}
	}


	public static boolean isAsciiString (String string) {
		if (null == string) {
			return false;
		}
		int l = string.length();
		for (int i = 0; i < l; ++i) {
			char c = string.charAt(i);
			if (!(c >= 32 && c <= 126)) {
				return false;
			}
		}
		return true;
	}


	public static String toString (byte[] data) {
		if ((null == data) || (data.length < 0)) {
			return new String("");
		}

		String ret = new String("");
		for (int i = 0; i < data.length; ++i) {
			ret += Integer.toString(0xff & (int) data[i]);
		}

		return ret;
	}


	public static String
		toHexString (byte[] data, int count) {
		if ((null == data) || (data.length < 0)) {
			return new String("");
		}

		if (count > data.length) {
			count = data.length;
		}

		String ret = new String("");
		for (int i = 0; i < count; ++i) {
			byte _B = (byte) data[i];
			int B = 0xff & _B;
			if (((B >> 4) & 0xf) > 9) {
				ret +=
					String
						.valueOf((char) ('A' + (((B >> 4) & 0xf) - 0xA)));
			} else {
				ret +=
					String
						.valueOf((char) ('0' + ((B >> 4) & 0xf)));
			}

			if ((B & 0xf) > 9) {
				ret +=
					String
						.valueOf((char) ('A' + ((B & 0xf) - 0xA)));
			} else {
				ret +=
					String
						.valueOf((char) ('0' + (B & 0xf)));
			}
		}

		return ret;
	}


	public static String toStringOrigin (byte[] data,
		String encoding) {
		/* encoding: e.x.: UTF-8 */
		try {
			if (null == data || null == encoding) {
				return null;
			}

			String ret =
				EncodingUtils.getString(data, encoding);

			return ret;
		} catch (Exception e) {
			return null;
		}
	}
}
