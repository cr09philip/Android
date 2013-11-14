package cn.kc.demo.audio;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import cn.kc.demo.model.FileHeader;
import cn.kc.demo.view.VoiceListActivity;
import cn.kc.demo.view.VoiceListActivity.PlayInfoHandler;

public class FilePlayer implements Runnable{
	public static final String TAG = "FilePlayer";

	private static FilePlayer mFilePlayer = null;
	
	AudioTrack mAudioTrack;
	int mFrequency; // 采样率
	int mChannel; // 声道
	int mSampBit; // 采样精度
	FileHeader mHeader;
	private int mMinBufSize;
	FileInputStream mInput;
	
	public ArrayList<String> mListFile;
	public String mFilePath;
	private AudioManager mAudioManager;
	
	private float mMaxVolume;
	private float mMinVolume;
	private float mCurVolume;
	
	private VoiceListActivity mContext;
	
	private boolean mThreadRunning = false;

	private Thread mThread;
	private Timer mTimer;

	private OnPlayStateChangedListener mOnPlayStateChangedListener = null;
	
	private FilePlayer(Context context) {
		mContext = (VoiceListActivity) context;
		
		mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		
		mMaxVolume = AudioTrack.getMaxVolume();
		mMinVolume = AudioTrack.getMinVolume();
		
		mListFile = new ArrayList<String>();
	}
	
	public static FilePlayer instance(Context context){
		if(mFilePlayer == null)
			mFilePlayer = new FilePlayer(context);
		
		return mFilePlayer;
	}

	public void init(String file) {
		mFilePath = file;
		mListFile.add(file);
		try {
			mInput = new FileInputStream( new File(file) );
			byte[] header = new byte[FileHeader.FILE_HEADER_SIZE];
			mInput.read(header, 0, FileHeader.FILE_HEADER_SIZE);

			mHeader = new FileHeader(header, 0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
		
		if(mHeader != null){
			mFrequency = mHeader.m_bSamples * 1000;
			mChannel = mHeader.m_bChannels == 0? AudioFormat.CHANNEL_OUT_MONO:AudioFormat.CHANNEL_OUT_STEREO;
			mSampBit = mHeader.m_bBitsPerSample == 16?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT;
		}
		
		if (mAudioTrack != null) {
			release();
		}

		// 获得构建对象的最小缓冲区大小
		mMinBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency,
				mChannel, mSampBit, mMinBufSize, AudioTrack.MODE_STREAM);
		
		if( mThread != null){
			mThread.interrupt();
			mThread = null;
		}
		
		mThread = new Thread(this);
		mThread.start();
		
		mContext.mPlayInfoHander.sendEmptyMessage(PlayInfoHandler.PLAY_OVER_FLAG);		
	}
	
	public void init(int frequency, int channels,int sampBit) {
		mThreadRunning = false;
		mFrequency = frequency;
		mChannel = channels;
		mSampBit = sampBit;
		
		if (mAudioTrack != null) {
			release();
		}

		// 获得构建对象的最小缓冲区大小
		mMinBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel, mSampBit);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency,
				mChannel, mSampBit, mMinBufSize, AudioTrack.MODE_STREAM);

		if( mThread != null){
			mThread.interrupt();
			mThread = null;
		}
		
		mThread = new Thread(this);
		mThread.start();

		mContext.mPlayInfoHander.sendEmptyMessage(PlayInfoHandler.PLAY_OVER_FLAG);		
	}
	
	private void doFilePlay(){
		try {
			byte[] buf = new byte[mMinBufSize * 2];
			while(mThreadRunning){
				if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
					int temp = 0;
					temp = mInput.read(buf);
					mAudioTrack.write(buf, 0, temp);
				}else {  
                    try {  
                        Thread.sleep(1000);  
                    } catch (InterruptedException e) { 
                    	mThreadRunning = false;
                        e.printStackTrace();  
                    }  
                }
			}

			//正常播放完毕
			if( mThreadRunning){
				mContext.mPlayInfoHander.sendEmptyMessage(PlayInfoHandler.PLAY_OVER_FLAG);
			}
				
		} catch (IOException e) {
			Log.d(TAG,"playFile IOException");
			e.printStackTrace();
		}
	}
	
	public void release() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
		}
	}	
	
	public interface OnPlayStateChangedListener{
		void onPlayStateChanged(int from, int to);
	}
	
	public void setOnPlayStateChangedListener(OnPlayStateChangedListener l){
		mOnPlayStateChangedListener  = l;
	}
	
	public int getPlayState(){
		if(mAudioTrack == null)
			return -1;
		return mAudioTrack.getPlayState();
	}
	public void play(){
		int from = mAudioTrack.getPlayState();
		if( from != AudioTrack.PLAYSTATE_PLAYING){
			Log.d(TAG,"play");
			mAudioTrack.play();
			Log.d(TAG, "PlaybackHeadPosition:" + mAudioTrack.getPlaybackHeadPosition());
			
			if(mOnPlayStateChangedListener != null)
				mOnPlayStateChangedListener.onPlayStateChanged(from, mAudioTrack.getPlayState());		
		}
	}
	
	public void stop(){
		int from = mAudioTrack.getPlayState();
		if( from != AudioTrack.PLAYSTATE_STOPPED){
			Log.d(TAG,"stop");
			mAudioTrack.stop();
			Log.d(TAG, "PlaybackHeadPosition:" + mAudioTrack.getPlaybackHeadPosition());
			
			mThreadRunning = false;

			if(mOnPlayStateChangedListener != null)
				mOnPlayStateChangedListener.onPlayStateChanged(from, mAudioTrack.getPlayState());
			
//			if( mThread.getState() != null)
			mThread.interrupt();
			mThread = null;
		}
	}
	
	public void pause(){
		if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
			Log.d(TAG,"pause");
			mAudioTrack.pause();
			Log.d(TAG, "PlaybackHeadPosition:" + mAudioTrack.getPlaybackHeadPosition());

			if(mOnPlayStateChangedListener != null)
				mOnPlayStateChangedListener.onPlayStateChanged(AudioTrack.PLAYSTATE_PLAYING, mAudioTrack.getPlayState());
		}
	}
	
	public void seekto(int pos){
//		mAudioTrack.setPlaybackHeadPosition(pos);
	}
	
	public void NextAudio(){
		
	}
	public void PrevAudio(){
		
	}
	public void fastForward(){
		
	}
	public void fastBackward(){
		
	}
/*
	public void addVolume(){
		float volume = mCurVolume/(mMaxVolume - mMinVolume);
		
		volume += 0.1f;
		Log.d(TAG,"addVolume");
		if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(volume, volume)){
			Log.d(TAG,"addVolume success");			
			mCurVolume = (mMaxVolume - mMinVolume)*volume;
		}
	}
	
	public void subVolume(){
		float volume = mCurVolume/(mMaxVolume - mMinVolume);
		
		volume -= 0.1f;
		Log.d(TAG,"subVolume");			
		if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(volume, volume)){
			mCurVolume = (mMaxVolume - mMinVolume)*volume;
			Log.d(TAG,"subVolume success");
		}
	}
	*/
	public void muteVolume(){	
		Log.d(TAG,"muteVolume");	
		if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(0.0f, 0.0f)){
			Log.d(TAG,"muteVolume success");
		}
	}
	
	public void run() {
		//计时器设置
		if(mTimer != null){
			mTimer.cancel();
		}

		mTimer = new Timer();
		
		mTimer.schedule(new TimerTask() {
			private int count = 0;
			@Override
			public void run() {
				if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
					count++;
					mContext.mPlayInfoHander.sendEmptyMessage(count);
				}
			}
		}, 0, 1000);

		//播放音频
		mThreadRunning = true;
		doFilePlay();
	}
}
