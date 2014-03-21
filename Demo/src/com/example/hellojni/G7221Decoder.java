package com.example.hellojni;

public class G7221Decoder {
	static {
        System.loadLibrary("hello-jni");
    }
    
	public G7221Decoder(){
		
	}	
	
	public native int init(int bit_rate, int bindwidth);
	public native int decode(Object inBuf, Object outBuf);
	public native int uninit();
}
