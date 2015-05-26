package nocom.common.wifi;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class MyWifiManager implements MyWifiManagerInterface {
	private WifiManager wifiManager = null;
	// private WifiInfo wifiInfo = null;
	private List<ScanResult> reachableWifisList = null;
	protected Context savedContext = null;

	public MyWifiManager(Context c) {
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
		} catch (Exception e) {
			;
		}
	}

	@Override
	public boolean isWifiConnected() {
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
	public boolean isWifiConnected(String ssid) {
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

	public void signalExit() {
		this.isSignaledExit = true;
	}

	@SuppressWarnings("static-access")
	@Override
	public boolean openWifi() {
		try { /* for all of this func */
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
					Log.println(Log.ERROR, "MyWifiManager/openWifi", "sleep: "
							+ e.getMessage());
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
		} /* end of all of this func */
	} /* end of openWifi */

	@Override
	public void closeWifi() {
		try { /* for all of this func */
			if (this.wifiManager.isWifiEnabled()) {
				this.wifiManager.setWifiEnabled(false);
			}
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/closeWifi", e.getMessage());
		} /* end of all of this func */
	} /* end of closeWifi */

	@Override
	public int getWifiState() {
		try { /* for all of this func */
			return this.wifiManager.getWifiState();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/checkWifiState",
					e.getMessage());
			return -1;/* error */
		} /* end of all of this func */
	} /* end of getWifiState */

	@Override
	public boolean startScan(int retry) {
		try { /* for all of this func */
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
		} /* end of all of this func */
	} /* end of startScan */

	@Override
	public List<ScanResult> getReachableWifisList() {
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

	@SuppressLint("UseValueOf")
	@Override
	public StringBuilder lookupScan() {
		try { /* for all of this func */

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
		} /* end of all of this func */
	} /* end of lookupScan */

	@Override
	public String getMacAddress() {
		try { /* for all of this func */
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? "0:0:0:0:0:0" : wifiInfo
					.getMacAddress();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getMacAddress",
					e.getMessage());
			return "0:0:0:0:0:0";/* error not got */
		} /* end of all of this func */
	} /* end of getMacAddress */

	@Override
	public String getBSSID() {
		try { /* for all of this func */
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? "" : wifiInfo.getBSSID();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getBSSID", e.getMessage());
			return "";/* error not got */
		} /* end of all of this func */
	} /* end of getBSSID */

	@Override
	public int getIPAddress() {
		try { /* for all of this func */
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? 0 : wifiInfo.getIpAddress();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getIPAddress", e.getMessage());
			return 0;/* error not got */
		} /* end of all of this func */
	} /* end of getIPAddress */

	@Override
	public String getInetIPAddress() {
		try { /* for all of this func */
			int intIp = this.getIPAddress();
			return (intIp & 0xFF) + "." + ((intIp >> 8) & 0xFF) + "."
					+ ((intIp >> 16) & 0xFF) + "." + ((intIp >> 24) & 0xFF);
		} catch (Exception e) {
			return "";
		} /* end of all of this func */
	}

	@Override
	public int getNetworkId() {
		try { /* for all of this func */
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? 0 : wifiInfo.getNetworkId();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getNetworkId", e.getMessage());
			return 0;/* error not got */
		} /* end of all of this func */
	} /* end of getNetworkId */

	@Override
	public String getWifiInfo() {
		try { /* for all of this func */
			WifiInfo wifiInfo = this.wifiManager.getConnectionInfo();
			return (wifiInfo == null) ? "" : wifiInfo.toString();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/getWifiInfo", e.getMessage());
			return "";/* error not got */
		} /* end of all of this func */
	} /* getWifiInfo */

	/**
	 * isWifiExsits...
	 */
	/* XXX-NOTE: check if exists in saved list */
	/* @param: ssid: no '\"' and '\"'. */
	@Override
	public boolean isWifiExsitsInSavedList(String ssid, boolean withQuotation) {
		try { /* for all of this func */
			List<WifiConfiguration> savedConfigs = this.wifiManager
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
		} /* end of all of this func */
	} /* end of isWifiExsitsInSavedList */

	/* XXX-NOTE: check if exists in saved list(may not reachable) */
	/* ... and reachable list */
	/* @param: ssid: no '\"' and '\"'. */
	@Override
	public boolean isWifiExsitsInAll(String ssid, boolean withQuotation) {
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
	public boolean isWifiExsitsInReachableList(String ssid,
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
	public boolean connect2Wifi(WifiConfiguration wifiConfiguration,
			boolean disableOthers, boolean removeWhenExist) {
		try { /* for all of this func */
			if (null == wifiConfiguration) {
				return false;
			}

			boolean ret = this.isWifiExsitsInSavedList(wifiConfiguration.SSID,
					true);

			if (ret && removeWhenExist) {
				this.wifiManager.disableNetwork(wifiConfiguration.networkId);
				this.removeNetwork(wifiConfiguration);
			}
			int netId;

			if (!ret) {
				netId = this.wifiManager.addNetwork(wifiConfiguration);
			} else {
				netId = wifiConfiguration.networkId;
			}

			Log.println(Log.INFO, "connect2Wifi", "ssid: "
					+ wifiConfiguration.SSID);

			ret = this.wifiManager.enableNetwork(netId, disableOthers);
			Log.println(Log.VERBOSE, "MyWifiManager/connect2Wifi", "netId: "
					+ netId + "; " + "enabled: " + ret);
			return ret;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/connect2Wifi", e.getMessage());
			return false;/* error */
		} /* end of all of this func */
	} /* connect2Wifi */

	@Override
	public boolean connect2Wifi(boolean oldEnabled, String oldssid,
			String newssid, boolean disableOthers, boolean removeWhenExist) {
		try { /* for all of this func */
			WifiConfiguration cfg = null;
			boolean ret;
			int oldid = 0;
			if ((null != oldssid) && (oldssid.length() > 0)) {
				cfg = this.getWifiConfiguration(oldssid, false);
				// oldid = this.wifiManager.addNetwork(cfg);
				oldid = cfg.networkId;
				ret = this.wifiManager.disableNetwork(oldid);

				if (!ret) {
					if (oldEnabled) {
						this.wifiManager.enableNetwork(oldid, false);
					}
					return false;
				}
			}

			cfg = this.getWifiConfiguration(newssid, false);
			if (null == cfg) {
				return false;
			}

			ret = this.isWifiExsitsInSavedList(cfg.SSID, true);

			if (ret && removeWhenExist) {
				this.wifiManager.disableNetwork(cfg.networkId);
				this.removeNetwork(cfg);
			}

			int newid;
			if (!ret) {
				newid = this.wifiManager.addNetwork(cfg);
			} else {
				newid = cfg.networkId;
			}

			ret = this.wifiManager.enableNetwork(newid, disableOthers);

			if (!ret) {
				if (oldEnabled && (null != oldssid) && (oldssid.length() > 0)) {
					this.wifiManager.enableNetwork(oldid, disableOthers);
				}
				return false;
			}

			return true;
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/connect2Wifi", e.getMessage());
			return false;/* error */
		} /* end of all of this func */
	} /* end of connect2Wifi */

	@Override
	public boolean connect2Wifi(int whichwifiConfiguration,
			boolean disableOthers, boolean removeWhenExist) {
		try { /* for all of this func */
			List<WifiConfiguration> savedConfigs = this.wifiManager
					.getConfiguredNetworks();
			if ((whichwifiConfiguration < 0) || (null == savedConfigs)
					|| (whichwifiConfiguration > savedConfigs.size())) {
				return false;
			}
			return this.wifiManager.enableNetwork(
					savedConfigs.get(whichwifiConfiguration).networkId,
					disableOthers);
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/connect2Wifi", e.getMessage());
			return false;/* error */
		} /* end of all of this func */
	} /* connect2Wifi */

	/**
	 * removeNetwork
	 */
	@Override
	public boolean removeNetwork(WifiConfiguration wifiConfiguration) {
		try {
			try {
				this.wifiManager.disableNetwork(wifiConfiguration.networkId);
			} catch (Exception e) {
				;
			}
			return this.wifiManager.removeNetwork(wifiConfiguration.networkId);
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public boolean removeNetwork(String ssid, boolean restoreSaved,
			boolean disableOthers) {
		try {
			try {
				WifiConfiguration cfg = null;

				cfg = this.getWifiConfiguration(ssid, false);
				if (null != cfg) {
					this.removeNetwork(cfg);
				}
			} catch (Exception e) {
				;
			}

			if (restoreSaved) {
				this.restoreWifiState(disableOthers);
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
	public boolean disconnectWifi(int netId) {
		try { /* for all of this func */
			this.wifiManager.disableNetwork(netId);
			return this.wifiManager.disconnect();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/disconnectWifi",
					e.getMessage());
			return false;/* error */
		} /* end of all of this func */
	} /* end of disconnectWifi */

	@Override
	public boolean disconnectWifi(String ssid) {
		try { /* for all of this func */
			WifiConfiguration cfg = this.getWifiConfiguration(ssid, false);
			int id = this.wifiManager.addNetwork(cfg);
			this.wifiManager.disableNetwork(id);
			return this.wifiManager.disconnect();
		} catch (Exception e) {
			Log.println(Log.ERROR, "MyWifiManager/disconnectWifi",
					e.getMessage());
			return false;/* error */
		} /* end of all of this func */
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
	public WifiConfiguration createWifiConfiguration(String ssid,
			String password, int type, boolean removeIfExist) {
		try { /* for all of this func */
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
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
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
				config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
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
		} /* end of all of this func */
	} /* end of createWifiConfiguration */

	/**
	 * getWifiConfiguration
	 * 
	 * @param: ssid: no '\"' and '\"'
	 */
	@Override
	public WifiConfiguration getWifiConfiguration(String ssid,
			boolean withQuotation) {
		try { /* for all of this func */
			List<WifiConfiguration> savedConfigs = this.wifiManager
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
		} /* end of all of this func */
	} /* end of getWifiConfiguration */

	@Override
	public List<WifiConfiguration> getWifiConfigurations() {
		try {
			return this.wifiManager.getConfiguredNetworks();
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public String getWifiSsid() {
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
	public boolean isWifiOpened() {
		return this.wifiManager.isWifiEnabled();
	}

	private boolean savedWifiState = false;
	private String savedStaticNetowrk = "";
	private WifiConfiguration savedWifiConfiguration = null;

	@SuppressWarnings("deprecation")
	@Override
	public void saveWifiState() {
		try {
			this.savedWifiState = this.wifiManager.isWifiEnabled();
			this.savedStaticNetowrk = android.provider.Settings.System
					.getString(this.savedContext.getContentResolver(),
							android.provider.Settings.System.WIFI_USE_STATIC_IP);
			this.savedWifiConfiguration = this.getWifiConfiguration(
					this.getWifiSsid(), true);
			Log.println(Log.VERBOSE, "saveWifiState", "WIFI_USE_STATIC_IP: "
					+ this.savedStaticNetowrk);
		} catch (Exception e) {
			;
		}
	}

	@SuppressWarnings({ "deprecation", "static-access" })
	@Override
	public void restoreWifiState(boolean disableOthers) {
		try {
			Log.println(Log.INFO, "restoreWifiState", "restoreWifiState");

			if (this.savedWifiState) {
				Log.println(Log.INFO, "restoreWifiState", "open");

				try {
					// this.closeWifi();
					this.openWifi();
					// try {
					// Thread.currentThread().sleep(2000);
					// } catch (Exception e) {
					// ;
					// }
				} catch (Exception e) {
					;
				}
				try {
					Thread.currentThread().sleep(20);
				} catch (Exception e) {
					;
				}

				if (null == this.savedWifiConfiguration) {
					return;
				}

				Log.println(Log.INFO, "restoreWifiState", "open: ssid: "
						+ this.savedWifiConfiguration.SSID);

				this.connect2Wifi(this.savedWifiConfiguration, disableOthers,
						false);

			} else {
				Log.println(Log.INFO, "restoreWifiState", "close");

				this.closeWifi();
			}

			if (null != savedStaticNetowrk && savedStaticNetowrk.length() > 0) {
				android.provider.Settings.System.putString(
						this.savedContext.getContentResolver(),
						android.provider.Settings.System.WIFI_USE_STATIC_IP,
						savedStaticNetowrk);
			}
		} catch (Exception e) {
			Log.println(Log.WARN, "restoreWifiState", e.getMessage());
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setStaticNetwork(String ip, String gateway, String netmask,
			List<String> dns) {
		try {
			android.provider.Settings.System.putString(
					this.savedContext.getContentResolver(),
					android.provider.Settings.System.WIFI_USE_STATIC_IP, "1");

			if ((null != dns) && (dns.size() > 0)) {
				android.provider.Settings.System.putString(
						this.savedContext.getContentResolver(),
						android.provider.Settings.System.WIFI_STATIC_DNS1,
						dns.get(0));
				if (dns.size() > 1) {
					android.provider.Settings.System.putString(
							this.savedContext.getContentResolver(),
							android.provider.Settings.System.WIFI_STATIC_DNS2,
							dns.get(1));
				}
			}

			if ((null != gateway) && (gateway.length() > 0)) {
				android.provider.Settings.System.putString(
						this.savedContext.getContentResolver(),
						android.provider.Settings.System.WIFI_STATIC_GATEWAY,
						gateway);
			}

			if ((null != netmask) && (netmask.length() > 0)) {
				android.provider.Settings.System.putString(
						this.savedContext.getContentResolver(),
						android.provider.Settings.System.WIFI_STATIC_NETMASK,
						netmask);
			}

			if ((null != ip) && (ip.length() > 0)) {
				android.provider.Settings.System.putString(
						this.savedContext.getContentResolver(),
						android.provider.Settings.System.WIFI_STATIC_IP, ip);
			}
		} catch (Exception e) {
			;
		}
	}

	@Override
	public boolean isUrlReachable(String url) {
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
}
