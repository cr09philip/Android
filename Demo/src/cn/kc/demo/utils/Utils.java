package cn.kc.demo.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Utils {
	public static String getMacAddressIP(String remotePcIP) {
		String str = "";
		String macAddress = "";
		try {
			Process pp = Runtime.getRuntime().exec("nbtstat -A" + remotePcIP);
			InputStreamReader ir = new InputStreamReader(pp.getInputStream());
			LineNumberReader input = new LineNumberReader(ir);
			for (int i = 1; i < 100; i++) {
				str = input.readLine();
				if (str != null) {
					if (str.indexOf("MAC Address") > 1) {
						macAddress = str.substring(
								str.indexOf("MAC Address") + 14, str.length());
						break;
					}
				}
			}
		} catch (IOException ex) {
		}
		return macAddress;
	}	
	
	public static String getDateString(){
		SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyyMMddHHmm");     
		Date   curDate   =   new   Date(System.currentTimeMillis());//获取当前时间     
//		String   str   =   formatter.format(curDate);
		return formatter.format(curDate);
	}

}
