package at.stefl.smartwifi;

import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiSelector {

	private static double calculateLevel(int rssi) {
		return WifiManager.calculateSignalLevel(rssi, 101) / 100d;
	}

	private static double getTime(long t1, long t2) {
		return (t2 - t1) / 1000000000d;
	}

	private static class ScanData {
		long timestamp;
		double level;
	}

	private WifiManager wifiManager;
	private Configuration configuration;

	private long lastSwitch = -1;

	private final Deque<Long> lastScans;
	private final Map<String, Deque<ScanData>> lastBssidScans;
	// TODO: free memory
	private final Map<String, Set<String>> ssidToBssids;
	private final Map<String, String> bssidToSsids;

	public WifiSelector() {
		this.lastScans = new LinkedList<Long>();
		this.lastBssidScans = new HashMap<String, Deque<ScanData>>();
		this.ssidToBssids = new HashMap<String, Set<String>>();
		this.bssidToSsids = new HashMap<String, String>();
	}

	public void init(Context context) {
		this.wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);

		this.configuration = Configuration.load(context);
	}

	// TODO: remove suppress
	private void addScan(ScanResult scan) {
		long timestamp = System.nanoTime();
		double level = calculateLevel(scan.level);
		addScan(scan.BSSID, scan.SSID, level, timestamp);
	}

	private void addScan(String bssid, String ssid, double level, long timestamp) {
		Deque<ScanData> scans = lastBssidScans.get(bssid);
		if (scans == null) {
			scans = new LinkedList<ScanData>();
			lastBssidScans.put(bssid, scans);
		}

		ScanData scanData = new ScanData();
		scanData.timestamp = timestamp;
		scanData.level = level;
		scans.add(scanData);

		Set<String> bssids = ssidToBssids.get(ssid);
		if (bssids == null) {
			bssids = new HashSet<String>();
			ssidToBssids.put(ssid, bssids);
		}
		bssids.add(bssid);
		bssidToSsids.put(bssid, ssid);
	}

	private void filterScans() {
		long timestamp = System.nanoTime();

		Iterator<Long> i = lastScans.iterator();
		while (i.hasNext()) {
			double time = getTime(i.next(), timestamp);
			if (time <= configuration.getScanCacheTime())
				break;
			i.remove();
		}

		Iterator<Entry<String, Deque<ScanData>>> j = lastBssidScans.entrySet()
				.iterator();
		while (j.hasNext()) {
			Entry<String, Deque<ScanData>> entry = j.next();
			Deque<ScanData> scans = entry.getValue();

			Iterator<ScanData> k = scans.iterator();
			while (k.hasNext()) {
				ScanData scan = k.next();
				double time = getTime(scan.timestamp, timestamp);
				if (time <= configuration.getScanCacheTime())
					break;
				k.remove();
			}

			if (scans.isEmpty())
				j.remove();
		}
	}

	private double timeWeight(double time) {
		if (time < 0)
			return 1;
		if (time > configuration.getScanCacheTime())
			return 0;
		return 1 - time / configuration.getScanCacheTime();
	}

	private double calculateScore(String bssid) {
		Deque<ScanData> scanData = lastBssidScans.get(bssid);
		if (scanData == null)
			return 0;
		long timestamp = System.nanoTime();
		double numerator = 0;
		double denominator = 0;

		for (ScanData scan : scanData) {
			double time = getTime(scan.timestamp, timestamp);
			double weight = timeWeight(time);
			numerator += scan.level * weight;
			denominator += weight;
		}

		if (denominator == 0)
			return 0;
		return numerator / denominator;
	}

	public void reportScan(List<ScanResult> scans) {
		Log.d(Constants.TAG, "scan reported");

		long timestamp = System.nanoTime();
		lastScans.add(timestamp);

		for (ScanResult scan : scans) {
			addScan(scan);
		}

		filterScans();

		// TODO: remove
		select();
	}

	private boolean decideScan(double score) {
		if (score > configuration.getBadScore())
			return false;
		if (lastScans.isEmpty())
			return true;

		long timestamp = System.nanoTime();
		double time = getTime(lastScans.getLast(), timestamp);
		if (time < configuration.getMinScanInterval())
			return false;

		return true;
	}

	public void reportRssi(int rssi) {
		Log.d(Constants.TAG, "rssi reported");

		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null)
			return;

		String bssid = wifiInfo.getBSSID();
		String ssid = Util.stringToSsid(wifiInfo.getSSID());
		double level = calculateLevel(rssi);
		long timestamp = System.nanoTime();

		addScan(bssid, ssid, level, timestamp);

		double score = calculateScore(bssid);
		if (decideScan(score)) {
			Log.d(Constants.TAG, "start scan");
			wifiManager.startScan();
		}
	}

	private boolean decideSwitch(double current, double other) {
		if (lastSwitch > 0) {
			long timestamp = System.nanoTime();
			double time = getTime(lastSwitch, timestamp);
			if (time < configuration.getSwitchThreshold())
				return false;
		}

		double quotient = other / current;
		return quotient >= configuration.getSwitchThreshold();
	}

	private void select() {
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		if (wifiInfo == null)
			return;
		String currentSsid = Util.stringToSsid(wifiInfo.getSSID());
		String currentBssid = wifiInfo.getBSSID();
		Set<String> members = configuration.getMembers(currentSsid);
		if (members == null)
			return;

		String bestSsid = null;
		String bestBssid = null;
		double bestScore = 0;
		double currentScore = 0;
		for (String ssid : members) {
			Set<String> bssids = ssidToBssids.get(ssid);
			if (bssids == null)
				continue;
			for (String bssid : bssids) {
				double score = calculateScore(bssid);

				if (score > bestScore) {
					bestSsid = ssid;
					bestBssid = bssid;
					bestScore = score;
				}

				if (bssid.equals(currentBssid)) {
					currentScore = score;
				}
			}
		}

		Log.d(Constants.TAG, "best network: " + bestSsid + ", " + bestBssid);
		Log.d(Constants.TAG, "current network: " + currentBssid);
		if ((bestBssid == null) || (bestBssid.equals(currentBssid)))
			return;

		if (!decideSwitch(currentScore, bestScore))
			return;

		Log.d(Constants.TAG, "switch");
		Util.connectSsid(bestSsid, wifiManager);
		lastSwitch = System.nanoTime();
	}

}