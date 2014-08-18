package cn.kc.demo.net.socket;

import java.io.IOException;
import java.io.InputStream;

import cn.kc.demo.model.DataHeaderModel;
import cn.kc.demo.model.NetHeaderModel;

public class SocketDataParser {		
	/*
	public static NetHeaderModel parserNetHeader(byte [] header, int start) {
		NetHeaderModel headerModel = new NetHeaderModel();
		
		int index = start;
		headerModel.mStif = CodeUtil.makeShort(header,index);
		index += 2;
		
		headerModel.mLength = CodeUtil.makeInt(header,index);
		index += 4;
		
		headerModel.mSid = header[index++];
		headerModel.mDid = header[index++];
		headerModel.mFunction = header[index++];
		headerModel.mVer = header[index++];
		
		return headerModel;
	}
	
	public static DataHeaderModel parserDataHeader(byte [] header, int start){
		DataHeaderModel dataHeader= new DataHeaderModel();
		
		int index = start;
		dataHeader.m_sFileNum = CodeUtil.makeShort(header,index);
		index += 2;
		
		dataHeader.m_bFormatTag = header[index++];
		dataHeader.m_bChannels = header[index++];
		dataHeader.m_bBytesPerSec = header[index++];
		dataHeader.m_bSamples = header[index++];
		dataHeader.m_bBitsPerSample = header[index++];
		dataHeader.m_sBlock = CodeUtil.makeShort(header,index);
		index += 2;

		dataHeader.m_strFileName = new String(header, index, DataHeaderModel.FILE_NAME_LENGTH);
		index += DataHeaderModel.FILE_NAME_LENGTH;
		
		dataHeader.m_nDataLength = CodeUtil.makeInt(header,index);
		index += 4;
		
		return dataHeader;
	}	
	*/
	public static NetHeaderModel readNetHeader(InputStream in) throws IOException{
		byte[] buffer = new byte[NetHeaderModel.NET_HEADER_FIXED_SIZE];
		if( NetHeaderModel.NET_HEADER_FIXED_SIZE != in.read(buffer, 0, NetHeaderModel.NET_HEADER_FIXED_SIZE))
			return null;
		
		NetHeaderModel netHeader = new NetHeaderModel(buffer, 0);
		if(netHeader.mStif != (short)0xfafa)
			return null;
		
		return netHeader;
	}
	
	public static DataHeaderModel readDataHeader(InputStream in) throws IOException{
		byte[] buffer = new byte[DataHeaderModel.DATA_HEADER_FIXED_SIZE];
		if( DataHeaderModel.DATA_HEADER_FIXED_SIZE != in.read(buffer, 0, DataHeaderModel.DATA_HEADER_FIXED_SIZE))
			return null;
		
		return new DataHeaderModel(buffer, 0);
	}
}
