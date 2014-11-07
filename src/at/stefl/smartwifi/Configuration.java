package at.stefl.smartwifi;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Configuration {

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

}