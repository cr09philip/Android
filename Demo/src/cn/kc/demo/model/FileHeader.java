package cn.kc.demo.model;

import cn.kc.demo.utils.CodeUtil;

public class FileHeader {
	public static final int FILE_HEADER_SIZE = 19;
	public static final int File_HEADER_OFFSET_POSTION = 11;
	//int magicNum;
	public byte m_bFormatTag;//0	FormatTag	1	编码格式类型	00—	IMA-ADPCM；01- g.722.1
							//其他以待扩展
	public byte m_bChannels;//1	nChannels	1	编码通道数目	00—单声道；
							//01—双声道；
	public byte m_bBytesPerSec;//2	BytesPerSec	1	编码后的压缩速率
								//（每秒多少字节数）	以Kbits/s为单位
								//（eg：64Kbps—64）
	public byte m_bSamples; //3	Samples	1	采样率	以KHz为单位（eg：16KHz—16）
	public byte m_bBitsPerSample;//4	BitsPerSample	1	量化单位（每样本数据位数）	以bit为单位（eg：16—16）
	//byte m_resver;
	public short m_sBlock;//5	Block	2	每次处理的数据块的大小	以字节为单位，是指adpcm压缩后的（这里定义为：500）
	public int m_nLength;//7	LENGTH	4	数据长度	仅指DATA部分的数据长度，单位是字节
	public int m_nOffset;//11   LENGTH  4   已经接收到的数据长度
	public int m_nDuration; //15        4 
	
	public FileHeader(byte[] header, int start){
		int index = start;
		this.m_bFormatTag = header[index++];
		this.m_bChannels = header[index++];
		this.m_bBytesPerSec = header[index++];
		this.m_bSamples = header[index++];
		this.m_bBitsPerSample = header[index++];
		
		
		this.m_sBlock = CodeUtil.makeShort(header,index);
		index += 2;
		
		this.m_nLength = CodeUtil.makeInt(header,index);
		index += 4;
		
		this.m_nOffset = CodeUtil.makeInt(header,index);
		index += 4;
		
		this.m_nDuration = CodeUtil.makeInt(header,index);
		index += 4;
	}
	
	public FileHeader(DataHeaderModel header){
		this.m_bFormatTag = header.m_bFormatTag;
		this.m_bChannels = header.m_bChannels;
		this.m_bBytesPerSec = header.m_bBytesPerSec;
		this.m_bSamples = header.m_bSamples;
		this.m_bBitsPerSample = header.m_bBitsPerSample;
		
		this.m_sBlock = header.m_sBlock;
		this.m_nLength = header.m_nDataLength;
		
		this.m_nOffset = 0;		
		this.m_nDuration = this.m_nLength / (m_bSamples * 1000 *(m_bChannels + 1) * m_bBitsPerSample/8);
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[FILE_HEADER_SIZE];
		int nIndex = 0;

		resBuf[nIndex++] = this.m_bFormatTag;
		resBuf[nIndex++] = this.m_bChannels;
		resBuf[nIndex++] = this.m_bBytesPerSec;
		resBuf[nIndex++] = this.m_bSamples;
		resBuf[nIndex++] = this.m_bBitsPerSample;

		byte[] shortBuf = CodeUtil.short2bytes(this.m_sBlock, true);
		for(int i = 0; i < shortBuf.length;i++){
			resBuf[nIndex++] = shortBuf[i];
		}
		
		byte[] intBuf = CodeUtil.int2bytes(this.m_nLength, true);
		for(int i = 0; i < intBuf.length;i++){
			resBuf[nIndex++] = intBuf[i];
		}
		
		byte[] intBuf1 = CodeUtil.int2bytes(this.m_nOffset, true);
		for(int i = 0; i < intBuf1.length;i++){
			resBuf[nIndex++] = intBuf1[i];
		}
		
		byte[] intBuf2 = CodeUtil.int2bytes(this.m_nDuration, true);
		for(int i = 0; i < intBuf2.length;i++){
			resBuf[nIndex++] = intBuf2[i];
		}
		
		return resBuf;
	}
	
	public int getFileStatus(){
		if(m_nOffset == 0 )
			return MusicInfoModel.DOWNLOAD_STATUS_BEGIN;
		
		if( m_nOffset != m_nLength)
			return MusicInfoModel.DOWNLOAD_STATUS_PROGRESSING;
		
		return MusicInfoModel.DOWNLOAD_STATUS_END;
	}
	
	public int getFileDownloadPercent(){
		if(m_nOffset == 0 )
			return 0;
		
		if( m_nOffset != m_nLength)
			return m_nOffset*100/m_nLength;
		
		return 100;		
	}
	
	public int getSampleRate(){
		return (int)m_bSamples * 1000;
	}
	
	public boolean isStereo(){
		return m_bChannels == 1;
	}
	
	public boolean is16Bit(){
		return m_bBitsPerSample == 16;
	}
	
	public int getBlockSize(){
		return (int)m_sBlock;
	}
}
