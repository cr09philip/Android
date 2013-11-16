package cn.kc.demo.audio;

import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public abstract class BaseAudioPlayer implements Runnable{
	private static final String TAG = "FilePlayer";
	
	protected static BaseAudioPlayer mAudioPlayer = null;
	private AudioTrack mAudioTrack;
	private int mFrequency; // 采样率
	private int mChannel; // 声道
	private int mSampBit; // 采样精度

	private int mMinBufSize; 
	
	private Thread mThread;
	private Timer mTimer;
	private Handler mHandler;
	private OnPlayStateChangedListener mOnPlayStateChangedListener = null;
	private OnNeedBufferInputListener mOnNeedBufferInputListener = null;
	private boolean mThreadRunning = false;
	
	private float mMaxAudioVolume;
	private float mMinAudioVolume;
	private float mCurAudioVolume = 1.0f; //defalut volume is no Attenuation
	private boolean mIsAudioVolumeMute = false;

	public BaseAudioPlayer(){		
		mMaxAudioVolume = AudioTrack.getMaxVolume();
		mMinAudioVolume = AudioTrack.getMinVolume();
		
		mHandler = new PlayerInfoHandler();
	}
	private class PlayerInfoHandler extends Handler{
		public static final int PLAY_OVER_FLAG = -1;
		public static final int PLAY_READY_FLAG = 0;
        public PlayerInfoHandler() {
        }

		public PlayerInfoHandler(Looper L) {
            super(L);
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        	if( mOnPlayStateChangedListener != null){
        		switch( msg.what ){
        		case PLAY_OVER_FLAG:
    				mOnPlayStateChangedListener.onPlayOver();
    				break;
        		case PLAY_READY_FLAG:
        		default:
            		mOnPlayStateChangedListener.onPlayProgressing(msg.what);  
            		break;
        		}
        	}
        }	
	}
	public interface OnPlayStateChangedListener{
		void onPlayStateChanged(int from, int to);
		void onPlayReady();
		void onPlayOver();
		void onPlayProgressing(int progress);
	}
	public void setOnPlayStateChangedListener(OnPlayStateChangedListener l){
		mOnPlayStateChangedListener  = l;
	}
	
	/**
	 * @author cr09philip
	 * 
	 * used to supply buffers to play
	 * though this will run in new thread,
	 * please just commit your audio buf to play without doing other things like ui
	 */
	public interface OnNeedBufferInputListener{
		int onNeedPlayingBuffer(byte[] buf);
	}
	public void OnNeedBufferInputListener(OnNeedBufferInputListener l){
		mOnNeedBufferInputListener  = l;
	}
	
	/**
	 * int this method, u showld call void initAudioTrack(int frequency, int channels,int sampBit)
	 * it used to new a AudioTrack and start a thread to play audio
	 */
	abstract public void init();

	protected void initAudioTrack(int frequency, int channels,int sampBit) {
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

		if(mOnPlayStateChangedListener != null)
			mOnPlayStateChangedListener.onPlayReady();	
	}

	// run in play thread
	private void doFilePlay(){
		mThreadRunning = true;
		byte[] buf = new byte[mMinBufSize * 2];
		while(mThreadRunning){
			if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
				if( mOnNeedBufferInputListener != null){
					int temp = 0;
					temp = mOnNeedBufferInputListener.onNeedPlayingBuffer(buf);
					if(temp != -1)
						mAudioTrack.write(buf, 0, temp);
				}
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
			mHandler.sendEmptyMessage(PlayerInfoHandler.PLAY_OVER_FLAG);
		}
	}
	
	public void release() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
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
					mHandler.sendEmptyMessage(count);
				}
			}
		}, 0, 1000);

		//播放音频
		doFilePlay();
	}

	public int getPlayState(){
		if(mAudioTrack != null)
			return mAudioTrack.getPlayState();
		
		return -1;
	}
	public void play(){
		int from = mAudioTrack.getPlayState();
		if( from != AudioTrack.PLAYSTATE_PLAYING){
			mAudioTrack.play();
			Log.d(TAG, "play---PlaybackHeadPosition:" + mAudioTrack.getPlaybackHeadPosition());
			
			if(mOnPlayStateChangedListener != null)
				mOnPlayStateChangedListener.onPlayStateChanged(from, mAudioTrack.getPlayState());		
		}
	}
	
	public void stop(){
		int from = mAudioTrack.getPlayState();
		if( from != AudioTrack.PLAYSTATE_STOPPED){
			mAudioTrack.stop();
			Log.d(TAG, "stop---PlaybackHeadPosition:" + mAudioTrack.getPlaybackHeadPosition());
			
			mThreadRunning = false;

			if(mOnPlayStateChangedListener != null)
				mOnPlayStateChangedListener.onPlayStateChanged(from, mAudioTrack.getPlayState());
			
			if( mThread != null){
				mThread.interrupt();
				mThread = null;
			}
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
		int n = mAudioTrack.getPlaybackHeadPosition();
		
		mAudioTrack.setPlaybackHeadPosition(n+5*mMinBufSize);
	}
	public void fastBackward(){
		int n = mAudioTrack.getPlaybackHeadPosition();
		
		mAudioTrack.setPlaybackHeadPosition(n - 5*mMinBufSize);
	}
	
	public void addAudioVolume(){
		if( mIsAudioVolumeMute){
			if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(mCurAudioVolume, mCurAudioVolume)){
				mIsAudioVolumeMute = false;
			}
		}
		
		if( mCurAudioVolume < mMaxAudioVolume){
			Log.d(TAG,"addVolume");
			if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(mCurAudioVolume + 0.1f, mCurAudioVolume + 0.1f)){
				Log.d(TAG,"addVolume success");			
				mCurAudioVolume += 0.1f;
			}
		}
	}
	
	public void subAudioVolume(){
		if( mIsAudioVolumeMute){
			if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(mCurAudioVolume, mCurAudioVolume)){
				mIsAudioVolumeMute = false;
			}
		}
		if( mCurAudioVolume > mMinAudioVolume){
			Log.d(TAG,"addVolume");
			if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(mCurAudioVolume + 0.1f, mCurAudioVolume + 0.1f)){
				Log.d(TAG,"addVolume success");			
				mCurAudioVolume += 0.1f;
			}
		}
	}
	public void muteAudioVolume(){	
		Log.d(TAG,"muteVolume");	
		if( AudioTrack.SUCCESS == mAudioTrack.setStereoVolume(0.0f, 0.0f)){
			Log.d(TAG,"muteVolume success");
			mIsAudioVolumeMute = true;
		}
	}
}
