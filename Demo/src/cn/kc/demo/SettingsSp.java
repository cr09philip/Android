package cn.kc.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SettingsSp {
	private static final String CODE_TYPE = "code_type";
	private static final String CHANNEL_TYPE = "channel_type";
	private static final String BAND_WIDTH_VALUE = "band_width_value";
	private static final String BIT_RATE_VALUE = "bit_rate_value";
	private static final String TAG_KEY = "Settings";
	private static SettingsSp instance;
	
	private SharedPreferences sp;
	private Editor ed;
	static synchronized public SettingsSp Instance(){
		if (instance == null) {
			instance = new SettingsSp();
		}
		
		return instance;
	}
	
	public SettingsSp init(Context context){
		sp = context.getSharedPreferences(TAG_KEY, 3);
		ed = sp.edit();
		
		return instance;
	}
	public byte getCode_type() {
		return (byte) sp.getInt(CODE_TYPE, 1);
	}
	public void setCode_type(byte type) {
		if(type != 0 && type != 1)
			ed.putInt(CODE_TYPE, 1);
		else
			ed.putInt(CODE_TYPE, type);
		
		ed.commit();
	}
	public byte getChannel_type() {
		return (byte) sp.getInt(CHANNEL_TYPE, 0);
	}
	public void setChannel_type(byte type) {
		if(type != 0 && type != 1)
			ed.putInt(CHANNEL_TYPE, 0);
		else
			ed.putInt(CHANNEL_TYPE, type);
		
		ed.commit();
	}
	public byte getBand_width_value() {
		return (byte) sp.getInt(BAND_WIDTH_VALUE, 7);
	}
	public void setBand_width_value(byte value) {
		if(value != 7 && value != 14)
			ed.putInt(BAND_WIDTH_VALUE, 7);		
		else
			ed.putInt(BAND_WIDTH_VALUE, value);
		
		ed.commit();
	}
	public byte getBit_rate_value() {
		return (byte) sp.getInt(BIT_RATE_VALUE, 32);
	}
	public void setBit_rate_value(byte value) {
		if(value != 24 && value != 32 && value != 48)
			ed.putInt(BIT_RATE_VALUE, 32);
		else
			ed.putInt(BIT_RATE_VALUE, value);
		
		ed.commit();
	}
}