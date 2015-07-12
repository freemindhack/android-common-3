
package nocom.common.network;


import android.content.Context;


public interface NetworkUtilsInterface {
	public String getLocalhostIp ();


	public String getWifiSsid (Context c);


	public boolean isWifiConnected (Context c);


	public boolean isValidIPv4 (String toValid);
}
