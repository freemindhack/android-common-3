package nocom.common.utils;


public interface FileUtilsInterface {
	public boolean isExists (String filename,
		boolean prependContextPrefix);


	public int rmFile (String filename,
		boolean prependContextPrefix);


	public int chmod (String filename,
		boolean prependContextPrefix, String mode);


	public String getAbsolutePath (String filename);


	public String getContextPrefix ();


	public int write (String file, byte[] buf,
		int startIndex, int count);


	public int append (String file, byte[] buf,
		int startIndex, int count);


	public int read (String file, byte[] buf,
		int startIndex, int maxCount);
}
