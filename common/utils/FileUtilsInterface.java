package nocom.common.utils;


public interface FileUtilsInterface {
	public boolean isExists (String filename,
		boolean prependContextPrefix);


	public String getAbsolutePath (String filename);


	public int write (String file, byte[] buf,
		int startIndex, int count);


	public int append (String file, byte[] buf,
		int startIndex, int count);


	public int read (String file, byte[] buf,
		int startIndex, int maxCount);
}
