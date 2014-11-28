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

	private static String groupString(Set<String> group) {
		StringBuilder builder = new StringBuilder();

		Iterator<String> i = group.iterator();
		builder.append(i.next());
		while (i.hasNext()) {
			builder.append(",");
			builder.append(i.next());
		}

		return builder.toString();
	}

	public static Configuration load(Context context) {
		Configuration configuration = new Configuration();

		SharedPreferences prefs = context.getSharedPreferences(
				PREFERENCES_NAME, Context.MODE_PRIVATE);
		Set<String> groups = prefs.getStringSet("groups", null);

		for (String group : groups) {
			String[] members = group.split(",");
			for (int i = 0; i < members.length; i++) {
				configuration.group(members[i], members[0]);
			}
		}

		return configuration;
	}

	private final Map<String, Set<String>> ssidToGroup;
	private final Set<Set<String>> groups;

	public Configuration() {
		this.ssidToGroup = new HashMap<String, Set<String>>();
		this.groups = new HashSet<Set<String>>();
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
		result = new HashSet<String>(result);
		result.remove(ssid);
		return result;
	}

	public void group(String ssid, String groupSsid) {
		removeMember(ssid);

		Set<String> group = ssidToGroup.get(ssid);
		if (group == null) {
			group = new HashSet<String>();
			group.add(groupSsid);
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
			String groupString = groupString(group);
			groups.add(groupString);
		}
		editor.putStringSet("groups", groups);
		editor.commit();
	}
}