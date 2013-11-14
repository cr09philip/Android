package cn.kc.demo.model;

public class DevicesInfoModel {

	public static final int WIFI_LINK = 0x0000;     //已连接
	public static final int WIFI_NOT_LINK = 0x0001; //未连接
	
	public static final int WIFI_INTENSITY_ONE   = 0x0000; //wifi信号强度1
	public static final int WIFI_INTENSITY_TWO   = 0x0001; //wifi信号强度2
	public static final int WIFI_INTENSITY_THREE = 0x0002; //wifi信号强度3
	public static final int WIFI_INTENSITY_FOUR  = 0x0003; //wifi信号强度4
	
	private int mWifiStatic = WIFI_NOT_LINK;           //wifi链接状态
	private int mWifiIntensity = WIFI_INTENSITY_ONE;   //wifi强度
	private String mDevicesName = "";                  //wifi名称
	private String mBssid = "";                        //wifi站点的MAC地址
	private String mSid ="XX";                         //发射端的id

	public int getWifiStatic() {
		return mWifiStatic;
	}

	public void setWifiStatic(int wifiStatic) {
		this.mWifiStatic = wifiStatic;
	}

	public int getWifiIntensity() {
		return mWifiIntensity;
	}

	public void setWifiIntensity(int wifiIntensity) {
		this.mWifiIntensity = wifiIntensity;
	}

	public String getDevicesName() {
		return mDevicesName;
	}

	public void setDevicesName(String devicesName) {
		this.mDevicesName = devicesName;
	}

	public String getBssid() {
		return mBssid;
	}

	public void setBssid(String bssid) {
		this.mBssid = bssid;
	}

	public String getSid() {
		return mSid;
	}

	public void setSid(String sid) {
		this.mSid = sid;
	}
}
