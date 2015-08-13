
package common.wifi;


import java.util.List;


import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;


public interface MyWifiManagerInterface {
	/**/
	public boolean isWifiConnected ();


	public boolean isWifiConnected (String ssid);


	/**/
	public String getWifiSsid ();


	public void saveWifiState ();


	// public interface
	public void restoreWifiState (boolean ignoreOn);


	public boolean isWifiOpened ();


	public boolean openWifi ();


	public void closeWifi ();


	public int getWifiState ();


	/**
	 * getWifiConfiguration
	 */
	/* XXX-NOTE: get a wifiConfiguration in saved list */
	public WifiConfiguration getWifiConfiguration (String ssid,
		boolean withQuotation);


	public List <WifiConfiguration> getWifiConfigurations ();


	/**/
	public boolean isUrlReachable (String url);


	public boolean startScan (int retry);


	public List <ScanResult> getReachableWifisList ();


	public StringBuilder lookupScan ();


	public String getMacAddress ();


	public String getBSSID ();


	/**
	 * getIPAddress
	 */
	public int getIPAddress ();


	public String getInetIPAddress ();


	public int getNetworkId ();


	public String getWifiInfo ();


	/**
	 * connect2Wifi
	 */
	public boolean connect2Wifi (WifiConfiguration wifiConfiguration,
		boolean disableOthers, boolean pick);


	public boolean connect2Wifi (int whichwifiConfiguration,
		boolean disableOthers, boolean pick);


	public boolean connect2Wifi (String ssid, boolean disableOthers,
		boolean pick);


	/* saved */
	public boolean connect2Wifi (boolean disableOthers, boolean pick);


	public boolean disconnectNetwork ();


	public boolean disableNetwork ();


	/*
	 * removeNetwork
	 */
	public boolean removeNetwork (WifiConfiguration wifiConfiguration);


	public boolean removeNetwork (String ssid);


	/* Wife lock*/
	public void createWifilock (String key);


	public void acquireWifiLock (boolean acquire);


	/**
	 * isWifiExsits...
	 */
	/* XXX-NOTE: check if exists in saved list */
	/* @param: ssid: no '\"' and '\"'. */
	public boolean isWifiExsitsInSavedList (String ssid, boolean withQuotation);


	/* XXX-NOTE: check if exists in saved list(may not reachable) */
	/* ... and reachable list */
	/* @param: ssid: no '\"' and '\"'. */
	public boolean isWifiExsitsInAll (String ssid, boolean withQuotation);


	/* XXX-NOTE: check if exists in reachable list */
	/* @param: ssid: no '\"' and '\"'. */
	public boolean isWifiExsitsInReachableList (String ssid,
		boolean withoutQuotation);


	public boolean disconnectWifi (int netId);


	public boolean disconnectWifi (String ssid);


	/**
	 * createWifiConfiguration
	 */
	/* @param: ssid: no '\"' and '\"'. */
	/* @description: */
	/* - 1: create a wifiConfiguration. */
	/* -.. if old exist and argument removeIfExist true remove old exist network */
	/* ... and then create the new created */
	/* -.. if old exist and argument removeIfExist false return exist */
	public WifiConfiguration createWifiConfiguration (String ssid,
		String password, int type, boolean removeIfExist);
}
