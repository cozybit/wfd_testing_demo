/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cozybit.wfd.tests;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

	private static String TAG = WiFiDirectBroadcastReceiver.class.getName();
	private WfdTestActivity mActivity;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WfdTestActivity activity) {
        super();
        this.mActivity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "Broadcast receiver -> new intent: " + action);
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            	Log.d(TAG, "Wifi Direct mode is enabled");
            } else {
            	Log.d(TAG, "Wifi Direct mode is NOT enabled");
            }
            Log.d(TAG, "P2P state changed - " + state);
            
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

        	WifiP2pDeviceList list = (WifiP2pDeviceList) intent.getParcelableExtra(WifiP2pManager.EXTRA_P2P_DEVICE_LIST);
        	if(list != null) {
        		Log.d(TAG, "Size of DeviceList: " + list.getDeviceList().size());
	        	for ( WifiP2pDevice dev : list.getDeviceList()) {
					Log.d(TAG, "DEV -> mac: " + dev.deviceAddress + " name: " + dev.deviceName + "status: " + dev.status + 
							" G.O.? " + dev.isGroupOwner() + " WPS-display? " + dev.wpsDisplaySupported() + 
							" WPS-Keypad? " + dev.wpsKeypadSupported() + " WPS-PBC? " + dev.wpsPbcSupported() );
		    		mActivity.setDiscoveredDevList(list);
				}
        	}
            
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
            	Log.d(TAG, "networkInfo says we are CONNECTED");
            } else {
                // It's a disconnect
                //activity.resetData();
            	Log.d(TAG, "networkInfo says we are DISCONNECTED");
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        	WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        	Log.d(TAG, "device changed --> address: " + device.deviceAddress + ", name: " + device.deviceName);
        }
    }
}
