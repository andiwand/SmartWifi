package at.stefl.smartwifi;

import android.net.wifi.WifiConfiguration;

public class WifiConfigurationUtil {

	public static String getSsid(WifiConfiguration configuration) {
		String ssid = configuration.SSID;
		return ssid.substring(1, ssid.length() - 1);
	}

	private WifiConfigurationUtil() {
	}

}