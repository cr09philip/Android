package cn.kc.demo.interfaces;

public interface IReceiverWifiCallBack {
	abstract public void changeRSSI();
	abstract public void networkStateChange();
	abstract public void wifiStateChange();
}
