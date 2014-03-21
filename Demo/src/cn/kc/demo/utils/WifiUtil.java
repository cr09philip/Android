package cn.kc.demo.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import cn.kc.demo.model.DevicesInfoModel;

public class WifiUtil {
	
	public static ArrayList<DevicesInfoModel> getDevicesInfoModels(Context context) {
		ArrayList<DevicesInfoModel> devicesInfoModels = new ArrayList<DevicesInfoModel>();
		WifiAdmin wifiAdmin = new WifiAdmin(context);
		wifiAdmin.startScan();
		
		String counterBSSID = wifiAdmin.getBSSID();
		
        List<ScanResult> results = wifiAdmin.getWifiList();  
        for (ScanResult result : results) {    
            DevicesInfoModel model = new DevicesInfoModel();
            model.setDevicesName(result.SSID);
            model.setBssid(result.BSSID);
            model.setWifiIntensity(WifiManager.calculateSignalLevel(result.level, 4));
            model.setWifiStatic(counterBSSID != null && counterBSSID.endsWith(result.BSSID) ? DevicesInfoModel.WIFI_LINK : DevicesInfoModel.WIFI_NOT_LINK);
            model.setSid(result.BSSID.replace(':', '_'));
            devicesInfoModels.add(model);
        }  
		return devicesInfoModels;
	}
	
	public static void recriverWifiRecever(Context context) {
		//WIFI状态接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		context.registerReceiver(WifiStateReceiver.getRecever(), filter);
	}
	
	public static void unregisterWifiReceiver(Context context, WifiStateReceiver wifiReceiver) {
		context.unregisterReceiver(WifiStateReceiver.getRecever());
	}
}
