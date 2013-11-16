package cn.kc.demo.audio;

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
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.kc.demo.model.FileHeader;
import cn.kc.demo.view.VoiceListActivity;

public class FilePlayer extends BaseAudioPlayer {
	public static final String TAG = "FilePlayer";

	private VoiceListActivity mContext; //context
	
	FileHeader mHeader;
	FileInputStream mInput;
	
	public ArrayList<String> mListFile;
	public String mFilePath;

	private FilePlayer(Context context) {
		super();
		
		mContext = (VoiceListActivity) context;
				
		mListFile = new ArrayList<String>();
	}
		
	public void setFilePathAndInitPlayer(String path) {
		this.mFilePath = path;
		if( mAudioPlayer != null)
			mAudioPlayer.recycle();
		init();
	}
	
	public static FilePlayer instance(Context context){
		if(mAudioPlayer == null)
			mAudioPlayer = new FilePlayer(context);
		
		return (FilePlayer) mAudioPlayer;
	}

	@Override
	protected void init() {
		mListFile.add(mFilePath);
		try {
			mInput = new FileInputStream( new File(mFilePath) );
			byte[] header = new byte[FileHeader.FILE_HEADER_SIZE];
			mInput.read(header, 0, FileHeader.FILE_HEADER_SIZE);

			mHeader = new FileHeader(header, 0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		}
		
		if(mHeader != null){
			int nFrequency = mHeader.m_bSamples * 1000;
			int nChannel = mHeader.m_bChannels == 0? AudioFormat.CHANNEL_OUT_MONO:AudioFormat.CHANNEL_OUT_STEREO;
			int nSampBit = mHeader.m_bBitsPerSample == 16?AudioFormat.ENCODING_PCM_16BIT:AudioFormat.ENCODING_PCM_8BIT;
			
			initAudioTrack(nFrequency, nChannel, nSampBit);
		}
	}

	@Override
	protected int getBufferToPlay(byte[] buf) {
		int nRes = 0;
		try {
			nRes = mInput.read(buf, 0, super.getMinBufSize() * 2);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return nRes;
	}
}
