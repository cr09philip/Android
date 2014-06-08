package cn.kc.demo.model;

import java.util.Arrays;

import cn.kc.demo.R.array;

//Function FUNCTION_FILE_OK = 1
public class SendReceiveOkModel extends NetHeaderModel{
	boolean mIsNeedDeleteSendFile = false;
	public SendReceiveOkModel(boolean b) {
		super(1, NetHeaderModel.FUNCTION_FILE_OK);
		
		mIsNeedDeleteSendFile = b;
	}

	public byte[] toBinStream(){
		byte[] stream = new byte[NetHeaderModel.NET_HEADER_FIXED_SIZE + 1];
		System.arraycopy( super.toBinStream(), 0, stream, 0, NetHeaderModel.NET_HEADER_FIXED_SIZE);
		
		stream[NetHeaderModel.NET_HEADER_FIXED_SIZE] = (byte) (mIsNeedDeleteSendFile?1:0);
		
		return stream;		 
	}
}
