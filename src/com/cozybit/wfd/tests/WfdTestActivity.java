package com.cozybit.wfd.tests;

import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class WfdTestActivity extends Activity {
	
	private static String TAG = WfdTestActivity.class.getName();

	private Button mStartGO;
	private Button mStartDiscovery;
	private Button mStartCli;;
	
	private WifiP2pManager mWfdManager;
	private Channel mWfdCh;
	private IntentFilter mIntentFilter;
	private WiFiDirectBroadcastReceiver mWfdReciver;
	
	private WifiP2pDeviceList mDiscoveredDevList;
	
	private boolean mGoEnabled = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mStartGO = (Button) findViewById(R.id.startGoButton);
		mStartDiscovery = (Button) findViewById(R.id.startDiscovery);
		mStartCli = (Button) findViewById(R.id.startCliButton);

		mStartGO.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				toggleGo();
			}
		});
		
		mStartDiscovery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startDiscovery();
			}
		});

		mStartCli.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				WifiP2pConfig wfdConfig = new WifiP2pConfig();
				if( mDiscoveredDevList == null ) {
					//N5: c6:43:8f:f2:d2:9a (?????)
					//N5: c6:43:8f:f2:52:9a (VALID)
					wfdConfig.deviceAddress = "c6:43:8f:f2:52:9a";
					wfdConfig.wps.setup = WpsInfo.PBC;
				} else {
					for (WifiP2pDevice dev : mDiscoveredDevList.getDeviceList()) {
						wfdConfig.deviceAddress = dev.deviceAddress;
						wfdConfig.wps.setup = WpsInfo.PBC;
					}
				}
				connect(wfdConfig);
			}
		});
		
		mWfdManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		mWfdCh = mWfdManager.initialize(this, getMainLooper(), new ChannelListener() {
			@Override
			public void onChannelDisconnected() {
				Log.d(TAG, "onChannelDisconnected()!!");
			}
		});
		
		
		mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
	}
	
    /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        mWfdReciver = new WiFiDirectBroadcastReceiver(this);
        registerReceiver(mWfdReciver, mIntentFilter);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mWfdReciver);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void setDiscoveredDevList(WifiP2pDeviceList list) {
		mDiscoveredDevList = list;
}	
	
	private void toggleGo() {
		
		if(!mGoEnabled) {
			mWfdManager.createGroup(mWfdCh, new ActionListener() {
				@Override
				public void onSuccess() {
					Log.d(TAG, "GO created succesfully!!!");
					mGoEnabled = true;
				}
				
				@Override
				public void onFailure(int reason) {
					Log.d(TAG, "GO creating FAILED!!! Error: " + reason);
				}
			});
		} else {
			mWfdManager.removeGroup(mWfdCh, new ActionListener() {
				@Override
				public void onSuccess() {
					Log.d(TAG, "GO removed succesfully!!!");
					mGoEnabled = false;
				}
				
				@Override
				public void onFailure(int reason) {
					Log.d(TAG, "GO removed FAILED!!! Error: " + reason);
				}
			});
			
		}
	}
	
	private void connect(WifiP2pConfig config) {	
		mWfdManager.connect(mWfdCh, config, new ActionListener() {
			@Override
			public void onSuccess() {
				Log.d(TAG, "Cli starting to connect!!");
			}
			
			@Override
			public void onFailure(int reason) {
				Log.d(TAG, "Cli FAILED initiating connection!! Error: " + reason);
			}
		});
	}
	
	private void startDiscovery() {
		mWfdManager.discoverPeers(mWfdCh, new ActionListener() {
	        @Override
	        public void onSuccess() {
	        	Log.d(TAG, "WFD Discovery started succesfully");
	        }

	        @Override
	        public void onFailure(int reasonCode) {
	        	Log.d(TAG, "WFD Discovery FAILED starting. Error: " + reasonCode);
	        }
		});		
	}
	
	/*private void requestConnectionInfo() {
		mWfdManager.requestConnectionInfo(mWfdCh, new ConnectionInfoListener() {
			
			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) {
				Log.d(TAG, "")
				info.
				// TODO Auto-generated method stub
				
			}
		})
	}*/
	
}
