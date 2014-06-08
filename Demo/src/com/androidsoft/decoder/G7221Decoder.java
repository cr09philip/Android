package com.androidsoft.decoder;

public class G7221Decoder {
	static {
        System.loadLibrary("as-decoder-jni");
    }
    
	public G7221Decoder(){
		
	}	
	
	    
    
	/**
	 * 初始化G7221Decoder
	 * 
	 * @param bit_rate： 视频bit_rate
	 * 	("Valid Rates: 48kbps = 48000\n");
     * 	("             32kbps = 32000\n");
     * 	("             24kbps = 24000\n");
     * 
	 * @param bandwidth: 视频bandwidth
	 *  ("Valid Bandwidth:    7kHz  =  7000\n");
     *	("      	          14kHz  = 14000\n");
	 * @return 
	 * 		失败：返回0， 如果传入了无效的参数将会返回失败，失败可以不用调用uninit()
	 * 		成功：返回 number_of_16bit_words_per_frame，
	 * 			 这个大小用于读取input数据，每次读取number_of_16bit_words_per_frame 数据。
	 */
	public native int init(int bit_rate, int bandwidth);
	
	
	
	/**
	 * 解码inBuf内容到outBuf:
	 * 
	 * @param inBuf：   short[]
	 * 		   	inBuf默认分配大小为  MAX_BITS_PER_FRAME/16， MAX_BITS_PER_FRAME = 960
	 * 
	 * @param inBufLen：
	 * 			解码前每次读取 number_of_16bit_words_per_frame（G7221Decoder.init成功的返回值） 到inBuf
	 *  
	 * @param outBuf： short[]
	 * 			outBuf默认分配大小为 MAX_DCT_LENGTH, MAX_DCT_LENGTH = 640
	 * 
	 * @return 解码后数据将会放到outBuf, 有效数据的大小为decode()函数的返回值, 大小为一个frame_size
	 * 		   	如果传入的inBufLen 不是number_of_16bit_words_per_frame， 将不会解码直接返回0
	 * 		   	如果未初始化直接调用会返回0
	 * 			

	 */
	public native int decode(Object inBuf, int inBufLen, Object outBuf);
	
	/**
	 * 释放G7221Decoder
	 *
	 * @return 0：成功， -1：失败
	 */
	public native int uninit();
}
