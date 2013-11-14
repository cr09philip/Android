package cn.kc.demo.utils;

import android.content.Context;
import android.media.AudioManager;

public class VolumeControl {
	private static final String TAG = "VolumeControl";
	private Context mContext;
	private AudioManager mAudioManager;

	private int mMaxVolume;
	private int mMinVolume;
	private int mCurVolume;
	
	private int mVolumeMode;
	
	public VolumeControl(Context context, int mode){
		mContext = context;
		mVolumeMode = mode;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		
		mMaxVolume = mAudioManager.getStreamMaxVolume(mVolumeMode);
		mMinVolume = 0;
		mCurVolume = mAudioManager.getStreamVolume(mVolumeMode);		
	}
	

	public void addVolume(){
		//增加音量，调出系统音量控制
		mAudioManager.adjustStreamVolume(mVolumeMode, AudioManager.ADJUST_RAISE,
		                            AudioManager.FX_FOCUS_NAVIGATION_UP);
	}
	
	public void subVolume(){
		//降低音量，调出系统音量控制
		mAudioManager.adjustStreamVolume(mVolumeMode, AudioManager.ADJUST_LOWER,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
	}
	
	public int getVolume(){	
		return mCurVolume = mAudioManager.getStreamVolume(mVolumeMode);
	}
	
}
