package com.example.hellojni;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.androidsoft.decoder.AdpcmDecoder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class AdpcmPlayer {
	private final String TAG = "AdpcmPlayer";
	//音频参数
	private int mSampleRate = 0;
	private boolean mIsStereo = false;
	private boolean mIs16Bit = false;
	private int mBlockSize = 0;
	
	//音频文件
	private String mAudioFile = null;
	
	//adpcm解码器
	private AdpcmDecoder mAdpcmDecoder = null;
		
	private AudioTrack mAudioTrack = null;  
	private Thread mAudioThread = null;
	
	private Thread mAudioSourceThread = null;
	
	private Object mInBuf = null;
	private Object mOutBuf = null;
	
	private int mDecodeIndex = 1;
	
	public AdpcmPlayer(String audioFile){
		mSampleRate = 16000;
		mIsStereo = false;
		mIs16Bit = true;
		mBlockSize = 500;
		
		mInBuf = new byte[mBlockSize];
		mOutBuf = new byte[mBlockSize * 4];
		/*
		if (mIs16Bit) {
			mOutBuf = new short[mBlockSize * (mIsStereo ? 2 : 1)];
		} else {
			
			mOutBuf = new byte[mBlockSize * (mIsStereo ? 2 : 1)];
		}
		*/				
		
		mAudioFile = audioFile;
		
		mAdpcmDecoder = new AdpcmDecoder(mSampleRate, mIsStereo, mIs16Bit, mBlockSize);
	}
	
	public void start() {
		audioTrackStart();
		
		mAudioSourceThread = new Thread(new Runnable() {
			public void run() {				
				File f = new File(mAudioFile);
				if (!f.exists())
					return;

				BufferedInputStream in = null;				
				try {
					in = new BufferedInputStream(new FileInputStream(f));
					int len = 0;
					int decodedLen = 0;
					while (-1 != (len = in.read((byte [])mInBuf, 0, mBlockSize))) {
						Log.v(TAG, "read len = " + len);
						if(mIsStereo){
							decodedLen = mAdpcmDecoder.decodeStereo(mInBuf, len, mOutBuf, mDecodeIndex++);													
						}else{
							decodedLen = mAdpcmDecoder.decodeMono(mInBuf, len,  mOutBuf, mDecodeIndex++);
						}
						audioWriteByteBuffer((byte [])mOutBuf, decodedLen);
					}
				} catch (IOException e) {
					e.printStackTrace();
					//throw e;
				} finally {
					try {
						in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});

		mAudioSourceThread.setPriority(Thread.NORM_PRIORITY);
		mAudioSourceThread.start();
		
	}
	
	public void stop(){
		//audioTrackQuit();
	}

	private void audioTrackStart() {
		int channelConfig = mIsStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO
				: AudioFormat.CHANNEL_CONFIGURATION_MONO;
		int audioFormat = mIs16Bit ? AudioFormat.ENCODING_PCM_16BIT
				: AudioFormat.ENCODING_PCM_8BIT;
		int frameSize = (mIsStereo ? 2 : 1) * (mIs16Bit ? 2 : 1);

		int desiredFrames = Math.max(mBlockSize,
				(AudioTrack.getMinBufferSize(mSampleRate, channelConfig,
				audioFormat) + frameSize - 1)/ frameSize);

		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mSampleRate,
				channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);
		
        mAudioThread = new Thread(new Runnable() {
            public void run() {
                mAudioTrack.play();
            }
        });        

        mAudioThread.setPriority(Thread.MAX_PRIORITY);
        mAudioThread.start();
	}
	
	/*
	private void audioWriteShortBuffer(short[] buffer) {
    	if(buffer == null || mAudioTrack == null)
    		return;
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
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
    */
    

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
    
    private void audioTrackQuit() {
        if (mAudioThread != null) {
            try {
                mAudioThread.join();
                //Log.v(TAG, "audio thread exit");
            } catch(Exception e) {
                //Log.v(TAG, "Problem stopping audio thread: " + e);
            }
            mAudioThread = null;
        }

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }

}
