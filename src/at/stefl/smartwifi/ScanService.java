package at.stefl.smartwifi;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

public class ScanService extends Service {

	private HandlerThread thread;
	private Looper looper;
	private Handler handler;

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

		thread = new HandlerThread(ScanService.class.getName(),
				android.os.Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		looper = thread.getLooper();
		handler = new Handler(looper);

		wifiSelector.init(this);
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context c, Intent intent) {
				wifiSelector.reportScan(wifiManager.getScanResults());

				// TODO: remove
				// handler.postDelayed(new Runnable() {
				// public void run() {
				// wifiManager.startScan();
				// }
				// }, 1000);
			}
		}, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

		registerReceiver(new BroadcastReceiver() {
			public void onReceive(Context context, Intent intent) {
				int defaultValue = Integer.MIN_VALUE;
				int rssi = intent.getIntExtra(WifiManager.EXTRA_NEW_RSSI,
						defaultValue);
				if (rssi == defaultValue)
					return;
				wifiSelector.reportRssi(rssi);
			}
		}, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));

		// TODO: remove
		// wifiManager.startScan();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// TODO: deregister all
	}

}