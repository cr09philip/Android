/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.hellojni;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.nio.channels.FileChannel;

import com.androidsoft.decoder.G7221Decoder;

import android.app.Activity;
import android.util.Log;
import android.widget.TextView;
import android.os.Bundle;

public class HelloJni extends Activity {
	private String TAG = "HelloJni";

	private AdpcmPlayer mPlayer = null;
	private G7221Decoder mG7221Decoder = null;

	private Thread mG7221DecoerThread;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/*
		 * Create a TextView and set its content. the text is retrieved by
		 * calling a native function.
		 */
		TextView tv = new TextView(this);
		// tv.setText( stringFromJNI() );
		setContentView(tv);

		mPlayer = new AdpcmPlayer("/mnt/sdcard/1.vox");

		mG7221DecoerThread = new Thread(new DecodeThreadRunnable(),
				"G7721DecodeThread");
		mG7221DecoerThread.start();
	}

	private final int MAX_BITS_PER_FRAME = 960;
	private final int MAX_DCT_LENGTH = 640;

	private final int INPUT_BUFFSIZE = MAX_BITS_PER_FRAME / 16;
	private final int OUTPUT_BUFFSIZE = MAX_DCT_LENGTH;

	private class DecodeThreadRunnable implements Runnable {
		public DecodeThreadRunnable() {
		}

		public void run() {
			String filePath = "/storage/sdcard0/g7221.vox";

			mG7221Decoder = new G7221Decoder();
			int number_of_16bit_words_per_frame = mG7221Decoder.init(32000,
					7000);
			if (number_of_16bit_words_per_frame == 0) {
				Log.v(TAG, "init g7221 decoder fail");
				return;
			}

			int inBufValidLen = number_of_16bit_words_per_frame * 2;
			Log.v(TAG, "number_of_16bit_words_per_frame = " + inBufValidLen);

			// short inBuf = new short[INPUT_BUFFSIZE];
			// short outBuf = new short[OUTPUT_BUFFSIZE];
			byte[] inBuf = new byte[INPUT_BUFFSIZE * 2];
			byte[] outBuf = new byte[OUTPUT_BUFFSIZE * 2];

			File f = new File(filePath);
			if (!f.exists()) {
				mG7221Decoder.uninit();
				return;
			}

			BufferedInputStream in = null;
			BufferedOutputStream out = null;

			try {
				in = new BufferedInputStream(new FileInputStream(f));

				FileOutputStream outSTr = new FileOutputStream(new File(
						"/storage/sdcard0/mytest.pcm"));
				out = new BufferedOutputStream(outSTr);

				int len = 0;
				while (-1 != (len = in.read(inBuf, 0, inBufValidLen))) {
					Log.v(TAG, "read len = " + len);
					int outputLen = mG7221Decoder.decode(inBuf,
							number_of_16bit_words_per_frame, outBuf);
					Log.v(TAG, "decoded frame len by short = " + outputLen);
					out.write(outBuf, 0, outputLen * 2);				
				}
			} catch (IOException e) {
				e.printStackTrace();
				// throw e;
			} finally {
				try {
					in.close();

					out.flush();
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			mG7221Decoder.uninit();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		// mPlayer.stop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		// mPlayer.start();
	}

	/*
	 * A native method that is implemented by the 'hello-jni' native library,
	 * which is packaged with this application.
	 */
	// public native String stringFromJNI();

	/*
	 * This is another native method declaration that is *not* implemented by
	 * 'hello-jni'. This is simply to show that you can declare as many native
	 * methods in your Java code as you want, their implementation is searched
	 * in the currently loaded native libraries only the first time you call
	 * them.
	 * 
	 * Trying to call this function will result in a
	 * java.lang.UnsatisfiedLinkError exception !
	 */
	// public native String unimplementedStringFromJNI();

	/*
	 * this is used to load the 'hello-jni' library on application startup. The
	 * library has already been unpacked into
	 * /data/data/com.example.hellojni/lib/libhello-jni.so at installation time
	 * by the package manager.
	 */
	// static {
	// System.loadLibrary("hello-jni");
	// }
}
