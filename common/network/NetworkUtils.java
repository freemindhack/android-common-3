package nocom.common.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class NetworkUtils implements NetworkUtilsInterface {

	@Override
	public String getLocalhostIp() {
		String ipString = "";
		try {
			Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces();
			while (en.hasMoreElements()) {
				NetworkInterface nif = en.nextElement();
				Enumeration<InetAddress> inet = nif.getInetAddresses();

				while (inet.hasMoreElements()) {
					InetAddress ip = inet.nextElement();
					if (!ip.isLoopbackAddress()
							&& InetAddressUtils.isIPv4Address(ip
									.getHostAddress())) {
						return ipString = ip.getHostAddress();
					}
				}
			}
		} catch (SocketException e) {
			Log.println(Log.ERROR, "NetworkUtils/getLocalhostIp",
					e.getMessage());
			return "";
		}

		return ipString;
	}

	@Override
	public String getWifiSsid(Context c) {
		try {
			WifiManager wifiManager = (WifiManager) c
					.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			return wifiInfo.getSSID();
		} catch (Exception e) {
			Log.println(Log.ERROR, "NetworkUtils/getWifiSSID", e.getMessage());
			return "";
		}
	}

	@Override
	public boolean isWifiConnected(Context c) {
		try {
			ConnectivityManager manager = (ConnectivityManager) c
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
}
