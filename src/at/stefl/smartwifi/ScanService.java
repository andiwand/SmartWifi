package at.stefl.smartwifi;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.IBinder;

public class ScanService extends Service {

	private WifiManager wifiManager;
	private final WifiSelector wifiSelector;

	public ScanService() {
		this.wifiSelector = new WifiSelector();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);

		registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context c, Intent intent) {
				wifiSelector.reportScan(wifiManager.getScanResults());
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// TODO: deregister receiver
	}
}
