package at.stefl.smartwifi;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Util {

	public static String stringToSsid(String string) {
		return string.substring(1, string.length() - 1);
	}

	public static String ssidToString(String ssid) {
		return "\"" + ssid + "\"";
	}

	public static void connectSsid(String ssid, WifiManager wifiManager) {
		for (WifiConfiguration configuration : wifiManager
				.getConfiguredNetworks()) {
			if ((configuration.SSID == null)
					|| !configuration.SSID.equals(Util.ssidToString(ssid)))
				continue;

			Log.d(Constants.TAG, "network id " + configuration.networkId);
			wifiManager.disconnect();
			wifiManager.enableNetwork(configuration.networkId, true);
			wifiManager.reconnect();

			break;
		}
	}

	private Util() {
	}

}