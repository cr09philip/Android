package cn.kc.demo.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.example.hellojni.AdpcmDecoder;
import com.example.hellojni.G7221Decoder;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.kc.demo.model.FileHeader;
/**
 * @author cr09philip
 * 
 * uesd to play the pcm buffers, with add some callback to control and notify progress 
 *  during playing
 */
public class AudioPlayer implements Runnable{
	private static final String TAG = "FilePlayer";
	
	protected static AudioPlayer mAudioPlayer = null;
	public AudioTrack mAudioTrack;
	
	private FileHeader mHeader;
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

	private G7221Decoder mG7221Decoder;

	public AudioPlayer(Context context){		
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
			byte[] header = new byte[FileHeader.FILE_HEADER_SIZE];
			mInput.read(header, 0, FileHeader.FILE_HEADER_SIZE);
			mHeader = new FileHeader(header, 0);
			
			switch (mHeader.m_bFormatTag) {
			case 0://ADPCM
				if(mAdpcmDecoder == null){
					mAdpcmDecoder = new AdpcmDecoder(mHeader.getSampleRate(), mHeader.isStereo(), mHeader.is16Bit(), mHeader.getBlockSize());
				}else{
					mAdpcmDecoder.initDecoder(mHeader.getSampleRate(), mHeader.isStereo(), mHeader.is16Bit(), mHeader.getBlockSize());
				}
				break;
			case 1://G.722.1
				if( mG7221Decoder != null){
					mG7221Decoder.uninit();
					mG7221Decoder = null;
				}
				
				mG7221Decoder = new G7221Decoder();
				
				mG7221Decoder.init(mHeader.getSampleRate(), mHeader.getBlockSize());
				break;
			default:
				break;
			}
			mIsInited = true;
		} catch (FileNotFoundException e) {
			mIsInited = false;
			e.printStackTrace();
		} catch (IOException e) {
			mIsInited = false;
			e.printStackTrace();
		}
		
		if(mHeader != null){
			mFrequency = mHeader.m_bSamples * 1000;
			mChannel = mHeader.m_bChannels == 0? AudioFormat.CHANNEL_OUT_MONO:AudioFormat.CHANNEL_OUT_STEREO;
			mSampBit = mHeader.m_bBitsPerSample == 16?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT;
		}
		
		initAudioTrack();
	}

	protected int getBufferToPlay(byte[] buf, int flag){
		int nRes = 0;
		try {
			byte[] tmpBuf = new byte[mHeader.m_sBlock];
			int len = mInput.read(tmpBuf, 0, mHeader.m_sBlock);

			//解码
			switch (mHeader.m_bFormatTag) {
			case 0://ADPCM
				if(mAdpcmDecoder != null){
					if(mHeader.isStereo()){
						nRes = mAdpcmDecoder.decodeStereo(tmpBuf, len, buf, flag);
					}
					else{
						nRes = mAdpcmDecoder.decodeMono(tmpBuf, len, buf, flag);
					}
				}
				break;
			case 1://G.722.1
				if( mG7221Decoder != null){
					mG7221Decoder.decode(tmpBuf, buf);
				}
				break;
			default:
				for(int i = 0; i < mHeader.m_sBlock; i++)
					buf[i] = tmpBuf[i];
				
				nRes = mHeader.m_sBlock;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nRes;
	}
	

	public static AudioPlayer instance(Context context) {
		if(mAudioPlayer == null){
			mAudioPlayer = new AudioPlayer(context);
		}
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
		
		mAudioTrack.setStereoVolume(mMaxAudioVolume, mMaxAudioVolume);
	}

	// run in play thread
	private void doFilePlay(){
		mThreadRunning = true;
		byte[] buf = new byte[mHeader.m_sBlock * 4];
		int flag = 1;
		while(mThreadRunning){
			if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
				int temp = 0;
				temp = getBufferToPlay(buf, flag++);
				//播放完成
				if(temp == -1)
					break;
				
				audioWriteByteBuffer(buf, temp);
//				mAudioTrack.write(buf, 0, temp);
				if( mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
					int nPos = mAudioTrack.getPlaybackHeadPosition();
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

    private void audioWriteByteBuffer(byte[] buffer, int validLen) {
    	if(buffer == null || mAudioTrack == null)
    		return;
    	
    	validLen = validLen > buffer.length ? buffer.length : validLen; 
    	for (int i = 0; i < validLen; ) {        	
            int result = mAudioTrack.write(buffer, i, validLen - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                return;
            }
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
			Log.d(TAG, "PlaybackHeadPosition:" + mAudioTrack.getPlaybackHeadPosition());

			mAudioTrack.pause();
			
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
