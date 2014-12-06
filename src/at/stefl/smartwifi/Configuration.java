package at.stefl.smartwifi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;

public class Configuration {

	private static final String PREFERENCES_NAME = "SmartWifi";

	private static final String KEY_GROUPS = "groups";
	private static final String KEY_SCAN_CACHE_TIME = "scanCacheTime";
	private static final String KEY_SWITCH_THRESHOLD = "switchThreshold";
	private static final String KEY_BAD_SCORE = "badScore";
	private static final String KEY_MIN_SCAN_INTERVAL = "minScanInterval";
	private static final String KEY_MIN_SWITCH_INTERVAL = "minSwitchInterval";

	private static final double DEFAULT_SCAN_CACHE_TIME = 10;
	private static final double DEFAULT_SWITCH_THRESHOLD = 1.2;
	private static final double DEFAULT_BAD_SCORE = 0.2;
	private static final double DEFAULT_MIN_SCAN_INTERVAL = 20;
	private static final double DEFAULT_MIN_SWITCH_INTERVAL = 10;

	private static final String MEMBER_SEPARATOR = ",";

	private static String[] stringToGroup(String group) {
		return group.split(MEMBER_SEPARATOR);
	}

	private static String groupToString(Set<String> group) {
		StringBuilder builder = new StringBuilder();

		Iterator<String> i = group.iterator();
		builder.append(i.next());
		while (i.hasNext()) {
			builder.append(MEMBER_SEPARATOR);
			builder.append(i.next());
		}

		return builder.toString();
	}

	public static Configuration load(Context context) {
		Configuration configuration = new Configuration();

		SharedPreferences prefs = context.getSharedPreferences(
				PREFERENCES_NAME, Context.MODE_PRIVATE);
		Set<String> groups = prefs.getStringSet(KEY_GROUPS,
				new HashSet<String>());

		for (String group : groups) {
			String[] members = stringToGroup(group);
			for (int i = 0; i < members.length; i++) {
				configuration.group(members[i], members[0]);
			}
		}

		configuration.scanCacheTime = prefs.getFloat(KEY_SCAN_CACHE_TIME,
				(float) DEFAULT_SCAN_CACHE_TIME);
		configuration.scanCacheTime = prefs.getFloat(KEY_SWITCH_THRESHOLD,
				(float) DEFAULT_SWITCH_THRESHOLD);
		configuration.scanCacheTime = prefs.getFloat(KEY_BAD_SCORE,
				(float) DEFAULT_BAD_SCORE);
		configuration.scanCacheTime = prefs.getFloat(KEY_MIN_SCAN_INTERVAL,
				(float) DEFAULT_MIN_SCAN_INTERVAL);
		configuration.scanCacheTime = prefs.getFloat(KEY_MIN_SWITCH_INTERVAL,
				(float) DEFAULT_MIN_SWITCH_INTERVAL);

		return configuration;
	}

	private final Map<String, Set<String>> ssidToGroup;
	private final Set<Set<String>> groups;

	private double scanCacheTime;
	private double switchThreshold;
	private double badScore;
	private double minScanInterval;
	private double minSwitchInterval;

	private Configuration() {
		this.ssidToGroup = new HashMap<String, Set<String>>();
		this.groups = new HashSet<Set<String>>();

		// TODO: remove me
		group("Stefl", "Stefl");
	}

	public double getScanCacheTime() {
		return scanCacheTime;
	}

	public double getSwitchThreshold() {
		return switchThreshold;
	}

	public double getBadScore() {
		return badScore;
	}

	public double getMinScanInterval() {
		return minScanInterval;
	}

	public double getMinSwitchInterval() {
		return minSwitchInterval;
	}

	public void setScanCacheTime(double scanCacheTime) {
		this.scanCacheTime = scanCacheTime;
	}

	public void setSwitchThreshold(double switchThreshold) {
		this.switchThreshold = switchThreshold;
	}

	public void setBadScore(double badScore) {
		this.badScore = badScore;
	}

	public void setMinScanInterval(double minScanInterval) {
		this.minScanInterval = minScanInterval;
	}

	public void setMinSwitchInterval(double minSwitchInterval) {
		this.minSwitchInterval = minSwitchInterval;
	}

	public boolean hasGroup(String ssid) {
		return ssidToGroup.containsKey(ssid);
	}

	public Set<Set<String>> getGroups() {
		Set<Set<String>> result = new HashSet<Set<String>>(groups.size());

		for (Set<String> group : groups) {
			result.add(Collections.unmodifiableSet(group));
		}

		return Collections.unmodifiableSet(result);
	}

	public Set<String> getMembers(String ssid) {
		Set<String> result = ssidToGroup.get(ssid);
		if (result == null)
			return null;
		return Collections.unmodifiableSet(result);
	}

	public void group(String ssid, String groupSsid) {
		removeMember(ssid);

		Set<String> group = ssidToGroup.get(ssid);
		if (group == null) {
			group = new HashSet<String>();
			group.add(groupSsid);
			ssidToGroup.put(ssid, group);
		}
		group.add(ssid);
	}

	public void removeMember(String ssid) {
		Set<String> group = ssidToGroup.get(ssid);
		if (group == null)
			return;
		if (group.size() == 1) {
			ssidToGroup.remove(ssid);
			groups.remove(group);
		} else {
			group.remove(ssid);
		}
	}

	public void save(Context context, Configuration configuration) {
		SharedPreferences.Editor editor = context.getSharedPreferences(
				PREFERENCES_NAME, Context.MODE_PRIVATE).edit();

		Set<String> groups = new HashSet<String>();
		for (Set<String> group : this.groups) {
			String groupString = groupToString(group);
			groups.add(groupString);
		}
		editor.putStringSet(KEY_GROUPS, groups);

		editor.putFloat(KEY_SCAN_CACHE_TIME, (float) scanCacheTime);
		editor.putFloat(KEY_SWITCH_THRESHOLD, (float) switchThreshold);
		editor.putFloat(KEY_BAD_SCORE, (float) badScore);
		editor.putFloat(KEY_MIN_SCAN_INTERVAL, (float) minScanInterval);
		editor.putFloat(KEY_MIN_SWITCH_INTERVAL, (float) minSwitchInterval);

		editor.commit();
	}

}