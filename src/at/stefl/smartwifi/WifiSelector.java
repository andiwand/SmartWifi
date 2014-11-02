package at.stefl.smartwifi;

import java.util.LinkedList;
import java.util.List;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;

public class WifiSelector {

	private static class WifiData {
		WifiConfiguration configuration;
		ScanResult lastScan;

		@Override
		public String toString() {
			return configuration.toString() + "; " + lastScan.toString();
		}
	}

	private static List<WifiData> merge(List<WifiConfiguration> configurations,
			List<ScanResult> scans) {
		List<WifiData> result = new LinkedList<WifiData>();

		for (WifiConfiguration configuration : configurations) {
			String ssid = WifiConfigurationUtil.getSsid(configuration);
			for (ScanResult scan : scans) {
				if (scan.BSSID.equals(configuration.BSSID)
						|| ssid.equals(scan.SSID)) {
					WifiData data = new WifiData();
					data.configuration = configuration;
					data.lastScan = scan;
					result.add(data);
				}
			}
		}

		return result;
	}

	private WifiManager wifiManager;

	public WifiSelector() {

	}

	public void init(WifiManager wifiManager) {
		this.wifiManager = wifiManager;
	}

	public void reportScan(List<ScanResult> scans) {

	}

	public void select() {
		List<WifiConfiguration> configurations = wifiManager
				.getConfiguredNetworks();
		List<ScanResult> scans = wifiManager.getScanResults();
		List<WifiData> data = merge(configurations, scans);

		WifiData best = null;
		for (WifiData d : data) {
			if ((best == null) || (d.lastScan.level > best.lastScan.level)) {
				best = d;
			}
		}

		if (best == null) {
			return;
		}

		wifiManager.disconnect();
		wifiManager.enableNetwork(best.configuration.networkId, true);
	}

}