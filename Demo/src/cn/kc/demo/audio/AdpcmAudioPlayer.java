package cn.kc.demo.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.androidsoft.decoder.AdpcmDecoder;
/**
 * @author cr09philip
 * 
 * uesd to play the pcm buffers, with add some callback to control and notify progress 
 *  during playing
 */
public class AdpcmAudioPlayer implements Runnable{
	private static final String TAG = "FilePlayer";
	
	protected static AdpcmAudioPlayer mAudioPlayer = null;
	public AudioTrack mAudioTrack;
	
	private int mFrequency; // 采样率
	private int mChannel; // 声道
	private int mSampBit; // 采样精度

	private int mMinBufSize; 
	
	private Thread mThread;
	private Handler mHandler;
	private OnPlayStateChangedListener mOnPlayStateChangedListener = null;
	private boolean mThreadRunning = false;
	
	private float mMaxAudioVolume;
	private float mMinAudioVolume;
	private float mCurAudioVolume = 1.0f; //defalut volume is no Attenuation
	private boolean mIsAudioVolumeMute = false;

	private FileInputStream mInput;
	
	private boolean mIsInited = false;

	private AdpcmDecoder mAdpcmDecoder;

	private int mSampleRate;

	private boolean mIsStereo;

	private boolean mIs16Bit;

	private int mBlockSize;

	public AdpcmAudioPlayer(Context context){		
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
	 * int this method, u showld call void initAudioTrack(int frequency, int channels,int sampBit)
	 * it used to new a AudioTrack and start a thread to play audio
	 */
	public void init(String path){		
		try {
			mInput = new FileInputStream( new File(path) );
			
			mSampleRate = 16000;
			mIsStereo = false;
			mIs16Bit = true;
			mBlockSize = 500;
			
			mAdpcmDecoder = new AdpcmDecoder(mSampleRate, mIsStereo, mIs16Bit, mBlockSize);

			mIsInited = true;
		} catch (FileNotFoundException e) {
			mIsInited = false;
			e.printStackTrace();
		} catch (IOException e) {
			mIsInited = false;
			e.printStackTrace();
		}

		mFrequency = mSampleRate;
		mChannel = mIsStereo ? AudioFormat.CHANNEL_OUT_STEREO:AudioFormat.CHANNEL_OUT_MONO;
		mSampBit = mIs16Bit ?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT;
		
		
		initAudioTrack();
	}

	protected int getBufferToPlay(byte[] buf, int flag){
		int nRes = 0;
		try {
			byte[] tmpBuf = new byte[mMinBufSize * 2];
			nRes = mInput.read(tmpBuf, 0, getMinBufSize() * 2);

			//解码
			if(mAdpcmDecoder != null){
				if( mIsStereo){
					mAdpcmDecoder.decodeStereo(tmpBuf, nRes, buf, flag);
				}
				else{
					mAdpcmDecoder.decodeMono(tmpBuf, nRes, buf, flag);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nRes;
	}
	

	public static AdpcmAudioPlayer instance(Context context) {
		if(mAudioPlayer == null)
			mAudioPlayer = new AdpcmAudioPlayer(context);
		
		return mAudioPlayer;
	}
	protected void initAudioTrack() {
		mThreadRunning = false;
		
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
		int index = 0;
		while(mThreadRunning){
			if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
				int temp = 0;
				temp = getBufferToPlay(buf, index++);
				//播放完成
				if(temp == -1)
					break;
				
				mAudioTrack.write(buf, 0, temp);
				int nPos = mAudioTrack.getPlaybackHeadPosition();
				if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
					mHandler.sendEmptyMessage(nPos / mFrequency);
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
	
	private void release() {
		if (mAudioTrack != null) {
			if( mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED)
				mAudioTrack.stop();
			
			mAudioTrack.release();
		}
	}	
	
	public void recycle(){
		release();
		if( mThread != null)
			mThread.interrupt();
	}
	
	public void run() {
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
	
//	//it seems we call do it only in the  MODE_STATIC mode
//	public void seekto(int pos){
//		mAudioTrack.setPlaybackHeadPosition(pos);
//	}
//	
//	public void NextAudio(){
//		this.init();
//		this.play();
//	}
//	public void PrevAudio(){
//		this.init();
//		this.play();
//	}
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

	public int getMinBufSize() {
		return mMinBufSize;
	}

}
