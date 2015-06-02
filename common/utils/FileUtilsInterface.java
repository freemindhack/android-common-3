package nocom.common.utils;


public interface FileUtilsInterface {
	public boolean isExists (String filename,
		boolean prependContextPrefix);


	public String getAbsolutePath (String filename);
}
