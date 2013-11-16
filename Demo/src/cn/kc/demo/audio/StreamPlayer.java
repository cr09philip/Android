package cn.kc.demo.audio;

import android.content.Context;


/**
 * used to play stream audio by transfer or decode
 * @author cr09philip
 *
 */
public class StreamPlayer extends BaseAudioPlayer {
	private static final String TAG = "AudioPlayer";
	private Context mContext;

	public StreamPlayer(Context context) {
		super();
		mContext = context;
	}
	
	public static StreamPlayer instance(Context context){
		if(mAudioPlayer == null)
			mAudioPlayer = new StreamPlayer(context);
		
		return (StreamPlayer) mAudioPlayer;
	}
	@Override
	public void init() {
		int frequency = 0; 
		int	channels = 0;
		int sampBit = 0;
		initAudioTrack(frequency, channels, sampBit);
	}

	@Override
	protected int getBufferToPlay(byte[] buf) {
		return 0;
	}
}