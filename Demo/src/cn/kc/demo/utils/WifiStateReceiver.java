package cn.kc.demo.utils;

import java.util.ArrayList;
import java.util.List;

import cn.kc.demo.interfaces.IReceiverWifiCallBack;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class WifiStateReceiver extends BroadcastReceiver {

	private static List<IReceiverWifiCallBack> mReceverWifiCallBackList = new ArrayList<IReceiverWifiCallBack>();
	private static WifiStateReceiver mReceiver = new WifiStateReceiver();
	
	private WifiStateReceiver() {
	}
	
	public static WifiStateReceiver getRecever() {
		return mReceiver;
	}
	
	public static WifiStateReceiver getRecever(IReceiverWifiCallBack receverWifiCallBack) {
		mReceverWifiCallBackList.add(receverWifiCallBack);
		return mReceiver;
	}

	public List<IReceiverWifiCallBack> getReceverWifiCallBack() {
		return mReceverWifiCallBackList;
	}

	public void setReceverWifiCallBack(List<IReceiverWifiCallBack> receverWifiCallBack) {
		mReceverWifiCallBackList = receverWifiCallBack;
	}
	
	public void setReceverWifiCallBack(IReceiverWifiCallBack receverWifiCallBack) {
		mReceverWifiCallBackList.removeAll(null);
		mReceverWifiCallBackList.add(receverWifiCallBack);
	}
	
	public void addReceverWifiCallBack(IReceiverWifiCallBack receverWifiCallBack) {
		mReceverWifiCallBackList.add(receverWifiCallBack);
	}
	
	public void removeReceverWifiCallBack(IReceiverWifiCallBack receverWifiCallBack) {
		mReceverWifiCallBackList.remove(receverWifiCallBack);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println(intent.getAction());
		if (intent.getAction().equals(WifiManager.RSSI_CHANGED_ACTION)) {
			for (IReceiverWifiCallBack item : mReceverWifiCallBackList) {
				item.changeRSSI();
			}
		} else if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
			NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
			if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {// 如果断开连接
				for (IReceiverWifiCallBack item : mReceverWifiCallBackList) {
					item.networkStateChange();
				}
			}
		} else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
			// WIFI开关
			int wifistate = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_DISABLED);
			if (wifistate == WifiManager.WIFI_STATE_DISABLED) {// 如果关闭
				for (IReceiverWifiCallBack item : mReceverWifiCallBackList) {
					item.wifiStateChange();
				}
			}
		}
	}
}
