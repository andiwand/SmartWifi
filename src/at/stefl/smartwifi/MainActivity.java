package at.stefl.smartwifi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import at.stefl.wifi.R;

public class MainActivity extends Activity {

	public static final String TAG = "SmartWifi";

	private static class WifiData {
		WifiConfiguration configuration;
		ScanResult lastScan;

		@Override
		public String toString() {
			return configuration.toString() + "; " + lastScan.toString();
		}
	}

	private static String parseSsid(String ssid) {
		return ssid.substring(1, ssid.length() - 1);
	}

	private static List<WifiData> merge(List<WifiConfiguration> configurations,
			List<ScanResult> scans) {
		List<WifiData> result = new LinkedList<WifiData>();

		for (WifiConfiguration configuration : configurations) {
			String ssid = parseSsid(configuration.SSID);
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ListView listView = (ListView) findViewById(R.id.listView1);
		List<String> networks = new LinkedList<String>();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				networks);
		listView.setAdapter(adapter);
		registerForContextMenu(listView);

		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);

		List<WifiConfiguration> configurations = wifiManager
				.getConfiguredNetworks();
		List<ScanResult> scans = wifiManager.getScanResults();
		List<WifiData> data = merge(configurations, scans);

		for (WifiConfiguration configuration : configurations) {
			String ssid = parseSsid(configuration.SSID);
			networks.add(ssid);
		}
		Collections.sort(networks);

		adapter.notifyDataSetChanged();

		WifiData best = null;
		for (WifiData d : data) {
			if ((best == null) || (d.lastScan.level > best.lastScan.level)) {
				best = d;
			}
		}

		Log.d(TAG, data.toString());

		if (best == null) {
			return;
		}

		Log.d(TAG, best.toString());

		wifiManager.disconnect();
		wifiManager.enableNetwork(best.configuration.networkId, true);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.listView1) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle((String) ((ListView) v).getAdapter().getItem(
					info.position));
			menu.add(Menu.NONE, 0, 0, "Add to group...");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Toast.makeText(this, "Added.", Toast.LENGTH_SHORT).show();
		return true;
	}

}
