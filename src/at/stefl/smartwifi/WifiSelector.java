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

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

public class WifiSelector {

	private static class ScanData {
		int level;
		long timestamp;
	}

	// TODO: use scan count?
	private double scanCacheTime;

	private final Deque<Long> lastScans;
	private final Map<String, Deque<ScanData>> lastBssidScans;
	// TODO: free memory
	private final Map<String, Set<String>> bssidToSsids;

	public WifiSelector() {
		this.lastScans = new LinkedList<Long>();
		this.lastBssidScans = new HashMap<String, Deque<ScanData>>();
		this.bssidToSsids = new HashMap<String, Set<String>>();
	}

	private void addScan(ScanResult scan) {
		long timestamp = System.nanoTime();
		lastScans.add(timestamp);

		Deque<ScanData> scans = lastBssidScans.get(scan.BSSID);
		if (scans == null) {
			scans = new LinkedList<ScanData>();
			lastBssidScans.put(scan.BSSID, scans);
		}

		ScanData scanData = new ScanData();
		scanData.timestamp = timestamp;
		scanData.level = scan.level;
		scans.add(scanData);

		Set<String> bssids = bssidToSsids.get(scan.SSID);
		if (bssids == null) {
			bssids = new HashSet<String>();
			bssidToSsids.put(scan.SSID, bssids);
		}

		bssids.add(scan.BSSID);
	}

	private void filterScans() {
		long timestamp = System.nanoTime();

		Iterator<Long> i = lastScans.iterator();
		while (i.hasNext()) {
			double time = (timestamp - i.next()) / 1000000000d;
			if (time <= scanCacheTime)
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
				double time = (timestamp - scan.timestamp) / 1000000000d;
				if (time <= scanCacheTime)
					break;
				k.remove();
			}

			if (scans.isEmpty())
				j.remove();
		}
	}

	private Map<String, Double> getCurrentScores() {
		Map<String, Double> result = new HashMap<String, Double>();

		for (Entry<String, Deque<ScanData>> entry : lastBssidScans.entrySet()) {
			double score = 0;

			for (ScanData scan : entry.getValue()) {
				double level = WifiManager
						.calculateSignalLevel(scan.level, 101) / 100d;
				score += level;
			}

			score /= lastScans.size();
			result.put(entry.getKey(), score);
		}

		return result;
	}

	public void reportScan(List<ScanResult> scans) {
		for (ScanResult scan : scans) {
			addScan(scan);
		}

		filterScans();
	}

	public void select() {

	}

}