package at.stefl.smartwifi;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
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

	private WifiManager wifiManager;

	public MainActivity() {
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

		List<WifiConfiguration> configurations = wifiManager
				.getConfiguredNetworks();

		for (WifiConfiguration configuration : configurations) {
			String ssid = WifiConfigurationUtil.getSsid(configuration);
			networks.add(ssid);
		}
		Collections.sort(networks);

		adapter.notifyDataSetChanged();
		
		startService(new Intent(this, ScanService.class));
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
