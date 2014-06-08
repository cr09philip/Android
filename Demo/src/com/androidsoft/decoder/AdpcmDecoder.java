package com.androidsoft.decoder;

public class AdpcmDecoder {
	private int mSampleRate = 0;
	private boolean mIsStereo = false;
	private boolean mIs16Bit = false;
	private int mBlockSize = 0;
	
	
    static {
        System.loadLibrary("as-decoder-jni");
    }
    
	public AdpcmDecoder(int sampleRate, boolean isStereo, boolean is16Bit, int blockSize){
		mSampleRate = sampleRate;
		mIsStereo = isStereo;
		mIs16Bit = is16Bit;
		mBlockSize = blockSize;
	}
	
	public void initDecoder(int sampleRate, boolean isStereo, boolean is16Bit, int blockSize){
		mSampleRate = sampleRate;
		mIsStereo = isStereo;
		mIs16Bit = is16Bit;
		mBlockSize = blockSize;
	}
	
	public native int decodeMono(Object inBuf, int inBufValidLen, Object outBuf, int flag);
	public native int decodeStereo(Object inBuf, int inBufValidLen, Object outBuf, int flag);	
}
