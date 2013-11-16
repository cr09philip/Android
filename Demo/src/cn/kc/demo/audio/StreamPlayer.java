package cn.kc.demo.audio;

import android.media.AudioManager;
import android.media.AudioTrack;

/**
 * used to play stream audio by transfer or decode
 * @author cr09philip
 *
 */
public class StreamPlayer extends BaseAudioPlayer {
	private static final String TAG = "AudioPlayer";

	public StreamPlayer() {
		super();
	}

	@Override
	public void init() {
		int frequency = 0; 
		int	channels = 0;
		int sampBit = 0;
		initAudioTrack(frequency, channels, sampBit);
	}
}