
package common.utils;


public interface VersionUtilsInterface {
	public String getVersionName ();


	public String getVersionString ();


	public int getVersionCode ();


	public String fourcc2string (int fourccValue);


	public int string2fourcc (String fourccString);


	public boolean isNew (String fourccString);
}
