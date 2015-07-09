package nocom.common.utils;


import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


import android.util.Log;


public class Md5sumUtils implements Md5sumUtilsInterface {

	@Override
	public byte[] md5sumString (byte[] byteString,
		int count, boolean to32Bytes)
		throws UnsupportedEncodingException {
		try {
			byte[] hash = null;

			try {
				if ((null == byteString) || (count < 0)
					|| (byteString.length <= 0)) {
					count = 0;
				} else if (count > byteString.length) {
					count = byteString.length;
				}
				byte[] data = new byte[count];

				for (int i = 0; i < count; ++i) {
					data[i] = byteString[i];
				}

				hash =
					MessageDigest.getInstance("MD5")
						.digest(data);
			} catch (NoSuchAlgorithmException e) {
				Log.e(TAG + ":Md5sumUtils",
					"ERROR: " + e.getMessage());

				throw new RuntimeException(
					"Huh, MD5 should be supported?", e);
			}

			if (!to32Bytes) {
				return hash;
			} else {
				byte[] ret = new byte[32];
				for (int i = 0; i < 32; i += 2) {
					ret[i] =
						(byte) (0xf & (hash[i / 2] >> 4));
					ret[i + 1] = (byte) (0xf & hash[i / 2]);
				}

				return ret;
			}
		} catch (Exception e) {
			Log.e(TAG + ":Md5sumUtils",
				"ERROR: " + e.getMessage());

			return null;
		}
	}


	@Override
	public byte[] md5sumString (String string) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String md5sumStringHexString (byte[] byteString,
		int count) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String md5sumStringHexString (String string) {
		// TODO Auto-generated method stub
		return null;
	}


	private final String TAG = "Md5sumUtils";

}
