
package common.utils;


import java.io.UnsupportedEncodingException;


public interface Md5sumUtilsInterface {
	public byte[] md5sumString (byte[] byteString, int count,
		boolean to32Bytes) throws UnsupportedEncodingException;


	public byte[] md5sumString (String string);


	public String md5sumStringHexString (byte[] byteString, int count);


	public String md5sumStringHexString (String string);
}
