package cn.kc.demo.model;

import cn.kc.demo.utils.CodeUtil;

public class NetHeaderModel {
	
	public static final byte FUNCTION_RECEIVE_LINK  = 0;  //�?��握手
	public static final byte FUNCTION_RECEIVE_START = 1;  //文件传输�?��
	public static final byte FUNCTION_RECEIVE_END   = 2;  //文件传输结束
	
	public static final byte FUNCTION_SEND_TIME_INFO  = 0;  //收端发送开机信息
	public static final byte FUNCTION_FILE_OK = 1;  //接收文件正确
	public static final byte FUNCTION_FILE_ERROR   = 2;  //接收文件错误
	public static final int NET_HEADER_FIXED_SIZE = 10;
	
	public short mStif;      // 信息�?��标志�?固定数据�?XFAFA --2字节    --offset 0
	public int mLength;    // DATA数据信息的长�?            --4字节     --offset 2
	public byte mSid;    // 发射端ID                      --1字节    --offset 6
	public byte mDid;    // 接收端ID                      --1字节    --offset 7
	public byte mFunction;  // 功能定义                                           		--1字节    --offset 8
	public byte mVer;       // 版本�?                    --1字节    --offset 9

	public NetHeaderModel(int len, byte func){
		mStif = (short)0xfafa;
		mLength = len;
		mSid = 0;
		mDid = (byte) 0xA0;
		mFunction = func;
		mVer = 0;
	}
	public NetHeaderModel(){
		
	}
	public NetHeaderModel(byte[] header, int start){
		int index = start;
		this.mStif = CodeUtil.makeShort(header,index);
		index += 2;
		
		this.mLength = CodeUtil.makeInt(header,index);
		index += 4;
		
		this.mSid = header[index++];
		this.mDid = header[index++];
		this.mFunction = header[index++];
		this.mVer = header[index++];
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[10];
		int nIndex = 0;
		
		byte[] shortBuf = CodeUtil.short2bytes(mStif, true);
		for(int i = 0; i < shortBuf.length;i++){
			resBuf[nIndex++] = shortBuf[i];
		}
		
		byte[] intBuf = CodeUtil.int2bytes(mLength, true);
		for(int i = 0; i < intBuf.length;i++){
			resBuf[nIndex++] = intBuf[i];
		}
		
		resBuf[nIndex++] = mSid;
		resBuf[nIndex++] = mDid;
		resBuf[nIndex++] = mFunction;
		resBuf[nIndex++] = mVer;
		
		return resBuf;
	}
}
