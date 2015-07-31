
package common.wifi;


import java.util.List;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;


public class MyWifiManager implements MyWifiManagerInterface {
	private WifiManager wifiManager = null;

	private List <ScanResult> reachableWifisList = null;

	protected Context savedContext = null;


	@SuppressLint ("InlinedApi")
	public MyWifiManager (Context c) {
		try {
			this.isSignaledExit = false;
			this.wifiManager = (WifiManager) c
				.getSystemService(Context.WIFI_SERVICE);
			// try {
			// if (null != this.wifiManager) {
			// this.wifiInfo = this.wifiManager.getConnectionInfo();
			// }
			// } catch (Exception e) {
			// ;
			// }
			this.savedContext = c;
			this.saveWifiState();

			// MyWifiManager.requestAlwaysScan(savedContext);
			// this.createWifilock(MyWifiManager.DEFAULT_WIFILOCK_TAG);
			// this.acquireWifiLock(true);
		} catch (Exception e) {
			;
		}
	}


	@Override
	public void createWifilock (String tag) {
		this.wifilock = this.wifiManager.createWifiLock(tag);
	}


	/* scan */
	@SuppressLint ("InlinedApi")
	public static void requestAlwaysScan (Context c) {

		Intent intent = new Intent(
			WifiManager.ACTION_REQUEST_SCAN_ALWAYS_AVAILABLE);

		c.startActivity(intent);

	}


	@Override
	public void acquireWifiLock (boolean acquire) {
		if (acquire && this.wifilock.isHeld()) {
			;
		} else if (acquire && !this.wifilock.isHeld()) {
			this.wifilock.acquire();
		} else if (!acquire && this.wifilock.isHeld()) {
			this.wifilock.release();
		} else {
			;
		}
	}


	@Override
	public boolean isWifiConnected () {
		try {
			ConnectivityManager manager = (ConnectivityManager) this.savedContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifiState = manager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
			if (State.CONNECTED == wifiState) {
				return true;
			}
			return false;
		} catch (Exception e) {
			Log.println(Log.ERROR, "NetworkUtils/isWifiConnected",
				e.getMessage());
			return false;
		}
	}


	@Override
	public boolean isWifiConnected (String ssid) {
		try {
			if (null == ssid || ssid.length() < 1) {
				return false;
			}
			ConnectivityManager manager = (ConnectivityManager) this.savedContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
			State wifiState = manager.getNetworkInfo(
				ConnectivityManager.TYPE_WIFI).getState();
			if (State.CONNECTED == wifiState) {
				String currentSsid = this.getWifiSsid();
				Log.println(Log.INFO, "ssid", currentSsid);
				if ((null != currentSsid) && (currentSsid.length() > 0)
					&& (ssid.equals(currentSsid))) {
					return true;
				} else {
					return false;
				}
			}
			return false;
		} catch (Exception e) {
			Log.println(Log.ERROR, "NetworkUtils/isWifiConnected",
				e.getMessage());
			return false;
		}
	}


	public boolean isSignaledExit = false;


	public void signalExit () {
		this.isSignaledExit = true;
	}


	@SuppressWarnings ("static-access")
	@Override
	public boolean openWifi () {
		try {
			int wifiCardState = this.wifiManager.getWifiState();
			if (this.wifiManager.isWifiEnabled()) {
				return true;
			}
			if ((!this.wifiManager.isWifiEnabled())
				&& (WifiManager.WIFI_STATE_ENABLING != wifiCardState)
				&& (WifiManager.WIFI_STATE_DISABLING != wifiCardState)) {
				try {
					this.wifiManager.setWifiEnabled(true);
					Thread.currentThread().sleep(500, 0);
				} catch (InterruptedException e) {
					;
				}
			}
			int to = 50;
			while ((to > 0)
				&& (WifiManager.WIFI_STATE_DISABLING == this.wifiManager
					.getWifiState())) {
				try {
					Thread.currentThread().sleep(10, 0);
				} catch (InterruptedException e) {
					;
				}
				wifiCardState = this.wifiManager.getWifiState();
				if (WifiManager.WIFI_STATE_DISABLING != this.wifiManager
					.getWifiState()) {
					break;
				} else {
					--to;
				}
			}
			wifiCardState = this.wifiManager.getWifiState();
			if (WifiManager.WIFI_STATE_DISABLING == this.wifiManager
				.getWifiState()) {
				// return false;/* timeout */
				;// no op ignore
			}
			wifiCardState = this.wifiManager.getWifiState();
			if ((!this.wifiManager.isWifiEnabled())
				&& (WifiManager.WIFI_STATE_ENABLING != wifiCardState)
				&& (WifiManager.WIFI_STATE_DISABLING != wifiCardState)) {
				this.wifiManager.setWifiEnabled(true);
				try {
					this.wifiManager.setWifiEnabled(true);
					Thread.currentThread().sleep(500, 0);
				} catch (InterruptedException e) {
					;
				}
			}
			to = 50;
			while ((to > 0)
				&& (WifiManager.WIFI_STATE_ENABLING == this.wifiManager
					.getWifiState())) {
				try {
					Thread.currentThread().sleep(10, 0);
				} catch (InterruptedException e) {
					Log.println(Log.ERROR, "MyWifiManager/openWifi",
						"sleep: " + e.getMessage());
				}
				wifiCardState = this.wifiManager.getWifiState();
				if (WifiManager.WIFI_STATE_ENABLING != this.wifiManager
					.getWifiState()) {
					break;
				} else {
					--to;
				}
			}
			wifiCardState = this.wifiManager.getWifiState();
			if (WifiManager.WIFI_STATE_ENABLED != wifiCardState) {
				try {
					this.wifiManager.setWifiEnabled(true);
					this.wifiManager.setWifiEnabled(true);
					Thread.currentThread().sleep(500, 0);
				} catch (Exception e) {
					;
				}
				return this.wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED;
			} else {
				return true;
			}
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/openWifi", e.getMessage());
			// this.closeWifi();
			return false;
		}
	} /* end of openWifi */


	@Override
	public void closeWifi () {
		try {
			if (this.wifiManager.isWifiEnabled()) {
				this.wifiManager.setWifiEnabled(false);
			}
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/closeWifi", e.getMessage());
		}
	} /* end of closeWifi */


	@Override
	public int getWifiState () {
		try {
			return this.wifiManager.getWifiState();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/checkWifiState",
				e.getMessage());
			return -1;/* error */
		}
	} /* end of getWifiState */


	@Override
	public boolean startScan (int retry) {
		try {
			if (retry <= 0) {
				retry = 1;
			}
			boolean started = false;
			for (int i = 0; i < retry; ++i) {
				boolean ret = this.wifiManager.startScan();
				if (ret) {
					started = true;
					break;
				}
			}
			if (!started) {
				return false;
			}
			this.reachableWifisList = this.wifiManager.getScanResults();
			return true;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/startScan", e.getMessage());
			return false;/* error */
		}
	} /* end of startScan */


	@Override
	public List <ScanResult> getReachableWifisList () {
		try {
			boolean ret = this.startScan(64);
			if (!ret) {
				return null;
			}
			return this.reachableWifisList;
		} catch (Exception e) {
			return null;
		}
	}


	@SuppressLint ("UseValueOf")
	@Override
	public StringBuilder lookupScan () {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < this.reachableWifisList.size(); i++) {
				stringBuilder.append("Index_" + new Integer(i + 1).toString()
					+ ":");
				stringBuilder.append((this.reachableWifisList.get(i))
					.toString());
				stringBuilder.append("/n");
			}
			return stringBuilder;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/lookupScan", e.getMessage());
			return null;/* error */
		}
	} /* end of lookupScan */


	@Override
	public String getMacAddress () {
		try {
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? "0:0:0:0:0:0" : wifiInfo
				.getMacAddress();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getMacAddress",
				e.getMessage());
			return "0:0:0:0:0:0";/* error not got */
		}
	} /* end of getMacAddress */


	@Override
	public String getBSSID () {
		try {
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? "" : wifiInfo.getBSSID();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getBSSID", e.getMessage());
			return "";/* error not got */
		}
	} /* end of getBSSID */


	@Override
	public int getIPAddress () {
		try {
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? 0 : wifiInfo.getIpAddress();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getIPAddress",
				e.getMessage());
			return 0;/* error not got */
		}
	} /* end of getIPAddress */


	@Override
	public String getInetIPAddress () {
		try {
			int intIp = this.getIPAddress();
			return (intIp & 0xFF) + "." + ((intIp >> 8) & 0xFF) + "."
				+ ((intIp >> 16) & 0xFF) + "." + ((intIp >> 24) & 0xFF);
		} catch (Exception e) {
			return "";
		}
	}


	@Override
	public int getNetworkId () {
		try {
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? 0 : wifiInfo.getNetworkId();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getNetworkId",
				e.getMessage());
			return 0;/* error not got */
		}
	} /* end of getNetworkId */


	@Override
	public String getWifiInfo () {
		try {
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? "" : wifiInfo.toString();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getWifiInfo",
				e.getMessage());
			return "";/* error not got */
		}
	} /* getWifiInfo */


	/**
	 * isWifiExsits...
	 */
	/* XXX-NOTE: check if exists in saved list */
	/* @param: ssid: no '\"' and '\"'. */
	@Override
	public boolean isWifiExsitsInSavedList (String ssid, boolean withQuotation) {
		try {
			List <WifiConfiguration> savedConfigs = this.wifiManager
				.getConfiguredNetworks();
			for (WifiConfiguration savedConfig : savedConfigs) {
				Log.println(Log.VERBOSE, "isWifiExsitsInSavedList", "ssid: "
					+ savedConfig.SSID);
				if (!withQuotation) {
					if (savedConfig.SSID.equals("\"" + ssid + "\"")) {
						return true;
					}
				} else {
					if (savedConfig.SSID.equals(ssid)) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/isWifiExsitsInSavedList",
				e.getMessage());
			return false;/* error */
		}
	} /* end of isWifiExsitsInSavedList */


	/* XXX-NOTE: check if exists in saved list(may not reachable) */
	/* ... and reachable list */
	/* @param: ssid: no '\"' and '\"'. */
	@Override
	public boolean isWifiExsitsInAll (String ssid, boolean withQuotation) {
		try {
			boolean ret = this.isWifiExsitsInSavedList(ssid, withQuotation);
			if (ret) {
				return true;
			}
			return this.isWifiExsitsInReachableList(ssid, withQuotation);
		} catch (Exception e) {
			return false;
		}
	} /* isWifiExsitsInAll */


	/* XXX-NOTE: check if exists in reachable list */
	/* @param: ssid: no '\"' and '\"'. */
	@Override
	public boolean isWifiExsitsInReachableList (String ssid,
		boolean withoutQuotation) {
		try {
			boolean ret = this.startScan(64);
			if (!ret) {
				return ret;
			}
			for (ScanResult reachableConfig : this.reachableWifisList) {
				Log.println(Log.VERBOSE, "isWifiExsitsInReachableList",
					"ssid: " + reachableConfig.SSID);
				/* XXX-NOTE: here no '\"' .. need */
				if (withoutQuotation) {
					if (reachableConfig.SSID.equals(ssid)) {
						return true;
					}
				} else {
					if (ssid.equals("\"" + reachableConfig.SSID + "\n")) {
						return true;
					}
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	} /* isWifiExsitsInReachableList */


	/**
	 * connect2Wifi
	 */
	@Override
	public boolean connect2Wifi (boolean disableOthers, boolean pick) {
		return this.connect2Wifi(this.savedWifiConfiguration, disableOthers,
			pick);
	}


	@Override
	public boolean connect2Wifi (WifiConfiguration wifiConfiguration,
		boolean disableOthers, boolean pick) {
		try {
			if (null == wifiConfiguration) {
				if (pick) {
					Intent intent = new Intent(
						WifiManager.ACTION_PICK_WIFI_NETWORK);
					this.savedContext.startActivity(intent);
					return true;
				} else {
					return false;
				}
			}
			boolean ret = this.isWifiExsitsInSavedList(
				wifiConfiguration.SSID, true);

			int netId;
			if (!ret) {
				netId = this.wifiManager.addNetwork(wifiConfiguration);
			} else {
				netId = wifiConfiguration.networkId;
			}
			Log.v(TAG + "connect2Wifi", "ssid: " + wifiConfiguration.SSID);

			return this.doConnect2Wifi(wifiConfiguration.SSID, netId,
				disableOthers, pick);

		} catch (Exception e) {
			Log.e(TAG + ":connect2Wifi", "E: " + e.getMessage());
			return false;/* error */
		}
	} /* connect2Wifi */


	private boolean doConnect2Wifi (String ssid, int netId,
		boolean disableOthers, boolean pick) {
		boolean ret;

		ret = this.wifiManager.enableNetwork(netId, disableOthers);
		Log.v(TAG + ":doConnect2Wifi", disableOthers + ": netId: " + netId
			+ ": " + "enabled: " + ret);

		if (ret) {
			for (int i = 0; i < 10; ++i) {
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					;
				}
			}

			for (int i = 0; i < 20 * 5; ++i) {
				if (this.isWifiConnected()) {
					Log.v(TAG + ":doConnect2Wifi", "connected");
					break;
				}
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					;
				}
			}

			for (int i = 0; i < 20 * 5; ++i) {
				String ip = this.getInetIPAddress();
				if (null != ip && ip.length() > 0 && !ip.equals("0.0.0.0")) {
					Log.v(TAG + ":doConnect2Wifi", "got ip: " + ip);
					break;
				}
				try {
					Thread.sleep(50);
				} catch (Exception e) {
					;
				}
			}

			if (this.isWifiConnected(ssid)) {
				Log.v(TAG + ":doConnect2Wifi", "connected: " + ssid);
				return true;
			}
		}

		for (int i = 0; i < 10; ++i) {
			try {
				Thread.sleep(50);
			} catch (Exception e) {
				;
			}
		}

		if (this.isWifiConnected(ssid)) {
			Log.v(TAG + ":doConnect2Wifi", "connected: " + ssid);
			return true;
		} else if (pick) {
			Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
			this.savedContext.startActivity(intent);
			return true;
		} else {
			return false;
		}
	}


	@Override
	public boolean connect2Wifi (String ssid, boolean disableOthers,
		boolean pick) {
		try {
			WifiConfiguration cfg = null;
			boolean ret;

			cfg = this.getWifiConfiguration(ssid, false);
			if (null == cfg) {
				Log.w(TAG + ":connect2Wifi",
					"failed: no saved conf for ssid: " + ssid);
				return false;
			}

			ret = this.isWifiExsitsInSavedList(cfg.SSID, true);
			int netId;
			if (!ret) {
				netId = this.wifiManager.addNetwork(cfg);
			} else {
				netId = cfg.networkId;
			}

			return this.doConnect2Wifi(cfg.SSID, netId, disableOthers, pick);
		} catch (Exception e) {
			Log.e(TAG + "connect2Wifi", "E: " + e.getMessage());
			return false;/* error */
		}
	} /* end of connect2Wifi */


	@Override
	public boolean connect2Wifi (int whichwifiConfiguration,
		boolean disableOthers, boolean pick) {
		try {
			List <WifiConfiguration> savedConfigs = this.wifiManager
				.getConfiguredNetworks();
			if ((whichwifiConfiguration < 0) || (null == savedConfigs)
				|| (whichwifiConfiguration > savedConfigs.size())) {
				if (pick) {
					Intent intent = new Intent(
						WifiManager.ACTION_PICK_WIFI_NETWORK);
					this.savedContext.startActivity(intent);
					return true;
				} else {
					return false;
				}
			}

			return this.doConnect2Wifi(
				savedConfigs.get(whichwifiConfiguration).SSID,
				savedConfigs.get(whichwifiConfiguration).networkId,
				disableOthers, pick);
		} catch (Exception e) {
			Log.e(TAG + "connect2Wifi", "E: " + e.getMessage());
			return false;/* error */
		}
	} /* connect2Wifi */


	@Override
	public boolean disconnectNetwork () {
		try {

			return this.wifiManager.disconnect();

		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public boolean disableNetwork () {
		try {

			return this.wifiManager
				.disableNetwork(this.savedWifiConfiguration.networkId);

		} catch (Exception e) {
			return false;
		}
	}


	/**
	 * removeNetwork
	 */
	@Override
	public boolean removeNetwork (WifiConfiguration wifiConfiguration) {
		try {
			try {
				this.wifiManager.disconnect();
				this.wifiManager.disableNetwork(wifiConfiguration.networkId);
			} catch (Exception e) {
				;
			}
			return this.wifiManager
				.removeNetwork(wifiConfiguration.networkId);
		} catch (Exception e) {
			return false;
		}
	}


	@Override
	public boolean removeNetwork (String ssid) {
		try {
			try {
				WifiConfiguration cfg = null;
				cfg = this.getWifiConfiguration(ssid, false);
				if (null != cfg) {
					try {
						this.disconnectWifi(ssid);
					} catch (Exception e) {
						;
					}
					this.removeNetwork(cfg);
				}
			} catch (Exception e) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}


	/*
	 * end of removeNetwork
	 */
	@Override
	public boolean disconnectWifi (int netId) {
		try {
			this.wifiManager.disableNetwork(netId);
			return this.wifiManager.disconnect();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/disconnectWifi",
				e.getMessage());
			return false;/* error */
		}
	} /* end of disconnectWifi */


	@Override
	public boolean disconnectWifi (String ssid) {
		try {
			WifiConfiguration cfg = this.getWifiConfiguration(ssid, false);
			if (null == cfg) {
				return false;
			}
			int id = this.wifiManager.addNetwork(cfg);
			this.wifiManager.disableNetwork(id);
			return this.wifiManager.disconnect();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/disconnectWifi",
				e.getMessage());
			return false;/* error */
		}
	} /* end of disconnectWifi */


	/**
	 * createWifiConfiguration
	 */
	/* @param: ssid: no '\"' and '\"'. */
	/* @description: */
	/* - 1: create a wifiConfiguration. */
	/* -.. if old exist and argument removeIfExist true remove old exist network */
	/* ... and then create the new created */
	/* -.. if old exist and argument removeIfExist false return exist */
	@Override
	public WifiConfiguration createWifiConfiguration (String ssid,
		String password, int type, boolean removeIfExist) {
		try {
			WifiConfiguration config = new WifiConfiguration();
			config.allowedAuthAlgorithms.clear();
			config.allowedGroupCiphers.clear();
			config.allowedKeyManagement.clear();
			config.allowedPairwiseCiphers.clear();
			config.allowedProtocols.clear();
			config.SSID = "\"" + ssid + "\"";
			Log.println(Log.VERBOSE, "createWifiInfo", "ssid: " + config.SSID);
			WifiConfiguration cfg = this.getWifiConfiguration(ssid, false);
			if (cfg != null) {
				if (removeIfExist) {
					this.wifiManager.removeNetwork(cfg.networkId);
				} else {
					return cfg;
				}
			}
			if (type == 1) // WIFICIPHER_NOPASS
			{
				config.wepKeys[0] = "";
				config.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.NONE);
				config.wepTxKeyIndex = 0;
			}
			if (type == 2) // WIFICIPHER_WEP
			{
				config.hiddenSSID = true;
				config.wepKeys[0] = "\"" + password + "\"";
				config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
				config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP40);
				config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
				config.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.NONE);
				config.wepTxKeyIndex = 0;
			}
			if (type == 3) // WIFICIPHER_WPA
			{
				config.preSharedKey = "\"" + password + "\"";
				config.hiddenSSID = true;
				config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
				config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.TKIP);
				config.allowedKeyManagement
					.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
				// config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
				config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.CCMP);
				config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
				config.status = WifiConfiguration.Status.ENABLED;
			}
			return config;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/WifiConfiguration",
				e.getMessage());
			return null;/* error */
		}
	} /* end of createWifiConfiguration */


	/**
	 * getWifiConfiguration
	 * 
	 * @param: ssid: no '\"' and '\"'
	 */
	@Override
	public WifiConfiguration getWifiConfiguration (String ssid,
		boolean withQuotation) {
		try {
			List <WifiConfiguration> savedConfigs = this.wifiManager
				.getConfiguredNetworks();
			for (WifiConfiguration saveConfig : savedConfigs) {
				if (!withQuotation) {
					if (saveConfig.SSID.equals("\"" + ssid + "\"")) {
						return saveConfig;
					}
				} else {
					if (saveConfig.SSID.equals(ssid)) {
						return saveConfig;
					}
				}
			}
			return null;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getWifiConfiguration",
				"ABORT");
			return null;/* error */
		}
	} /* end of getWifiConfiguration */


	@Override
	public List <WifiConfiguration> getWifiConfigurations () {
		try {
			return this.wifiManager.getConfiguredNetworks();
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public String getWifiSsid () {
		try {
			WifiManager wifiManager = (WifiManager) this.savedContext
				.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			return wifiInfo.getSSID();
		} catch (Exception e) {
			Log.println(Log.ERROR, "NetworkUtils/getWifiSSID", e.getMessage());
			return "";
		}
	}


	@Override
	public boolean isWifiOpened () {
		return this.wifiManager.isWifiEnabled();
	}


	private boolean savedWifiState = false;

	private WifiConfiguration savedWifiConfiguration = null;


	@Override
	public void saveWifiState () {
		try {
			this.savedWifiState = this.wifiManager.isWifiEnabled();

			this.savedWifiConfiguration = this.getWifiConfiguration(
				this.getWifiSsid(), true);
		} catch (Exception e) {
			;
		}
	}


	public WifiConfiguration getSavedWifiConfiguration () {
		return this.savedWifiConfiguration;
	}


	@Override
	public void restoreWifiState (boolean ignoreOn) {
		try {
			Log.v(TAG, "restoreWifiState");

			new RestoreWifiThread(this, MyWifiManager.this.savedWifiState,
				MyWifiManager.this.savedWifiConfiguration, ignoreOn).start();
		} catch (Exception e) {
			Log.w("restoreWifiState", "E: " + e.getMessage());
		}
	}


	private class RestoreWifiThread extends Thread {
		public RestoreWifiThread (MyWifiManager ii,
			boolean dumpSavedWifiState,
			WifiConfiguration dumpSavedWifiConfiguration, boolean ignoreOn) {
			this.ii = ii;
			this.dumpSavedWifiState = dumpSavedWifiState;
			this.dumpSavedWifiConfiguration = dumpSavedWifiConfiguration;
			this.ignoreOn = ignoreOn;
		}


		private MyWifiManager ii = null;

		private boolean dumpSavedWifiState = false;

		private WifiConfiguration dumpSavedWifiConfiguration = null;

		private boolean ignoreOn = false;


		@Override
		public void run () {
			try {
				Log.v(TAG + ":RestoreWifiThread", "run");

				if (null == ii) {
					Log.v(TAG + ":RestoreWifiThread:run",
						"no instance: all done");
					super.run();
					return;
				}

				if (dumpSavedWifiState) {
					Log.v(TAG + ":RestoreWifiThread:run", "saved is open");

					try {
						boolean ret = ii.openWifi();
						if (ret) {
							Log.v(TAG + ":RestoreWifiThread:run",
								"opening ...");
							for (int i = 0; i < 10; ++i) {
								try {
									Thread.sleep(200);
								} catch (Exception e) {
									;
								}
							}
						} else {
							Log.v(TAG + ":RestoreWifiThread:run",
								"start opening failed");
						}
					} catch (Exception e) {
						Log.w(TAG + ":RestoreWifiThread:run", "E: openWifi: "
							+ e.getMessage());
					}
					try {
						Thread.sleep(20);
					} catch (Exception e) {
						;
					}
					if (null == dumpSavedWifiConfiguration) {
						Log.v(TAG + ":RestoreWifiThread:run",
							"no saved wifi conf: all done");

						super.run();
						return;
					}

					Log.v(TAG + ":restoreWifiState:Thread:run",
						"check: ssid: " + dumpSavedWifiConfiguration.SSID);
					boolean c = ii
						.isWifiConnected(dumpSavedWifiConfiguration.SSID);
					if (c) {
						Log.v(TAG + ":RestoreWifiThread:run",
							"connected: ssid: "
								+ dumpSavedWifiConfiguration.SSID
								+ ": all done");
						// ii.connect2Wifi(dumpSavedWifiConfiguration, false,
						// true);//
						// ii.connect2Wifi(dumpSavedWifiConfiguration, true,
						// true);//
						super.run();
						return;
					} else {
						// c = ii.connect2Wifi(dumpSavedWifiConfiguration,
						// false, true);
						c = ii.connect2Wifi(dumpSavedWifiConfiguration, true,
							true);//
						if (c) {
							Log.v(TAG + ":RestoreWifiThread:run",
								"connected: ssid: "
									+ dumpSavedWifiConfiguration.SSID
									+ ": all done");
						} else {
							Log.w(TAG + ":RestoreWifiThread:run",
								"connecte: ssid: "
									+ dumpSavedWifiConfiguration.SSID
									+ ": failed: all done");
						}

						super.run();
						return;
					}
				} else if (!ignoreOn) {
					Log.v(TAG + ":RestoreWifiThread:run", "saved is closed");
					ii.closeWifi();
				}

				super.run();
			} catch (Exception e) {
				Log.e(TAG + ":RestoreWifiThread:run", "E: " + e.getMessage());
				super.run();
			}
		}
	}


	@Override
	public boolean isUrlReachable (String url) {
		try {
			if ((null == url) || (url.length() < 1)) {
				return false;
			}
			Process p = Runtime.getRuntime().exec("ping -c 1 " + url);
			int status = p.waitFor();
			if (status == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			return false;/* abort */
		}
	}


	private static final String TAG = MyWifiManager.class.getSimpleName();

	// private static final String DEFAULT_WIFILOCK_TAG =
	// "DEFAULT_WIFILOCK_TAG";

	private WifiLock wifilock;
}
