package cn.kc.demo.model;

import java.util.Arrays;

import cn.kc.demo.utils.CodeUtil;

public class FileHeader {
	public static final int FILE_HEADER_SIZE = 24;
	public static final int File_HEADER_OFFSET_POSTION = 16;
	public final int m_nMagicNum = 0xfafafafa;//0
	public byte m_bFileIndex;//4
	public byte m_bFormatTag;//5	FormatTag	1	编码格式类型	00—	IMA-ADPCM；01- g.722.1
							//其他以待扩展
	public byte m_bChannels;//6	nChannels	1	编码通道数目	00—单声道；
							//01—双声道；
	public byte m_bBytesPerSec;//7	BytesPerSec	1	编码后的压缩速率
								//（每秒多少字节数）	以Kbits/s为单位
								//（eg：64Kbps—64）
	public byte m_bSamples; //8	Samples	1	采样率	以KHz为单位（eg：16KHz—16）
	public byte m_bBitsPerSample;//9	BitsPerSample	1	量化单位（每样本数据位数）	以bit为单位（eg：16—16）
	public short m_sBlock;//10	Block	2	每次处理的数据块的大小	以字节为单位，是指adpcm压缩后的（这里定义为：500）
	public int m_nLength;//12	LENGTH	4	数据长度	仅指DATA部分的数据长度，单位是字节
	public int m_nOffset;//16   LENGTH  4   已经接收到的数据长度
	public int m_nDuration; //20        4 
	
	public FileHeader(byte[] header){
//		int index = start;
//		index += 4;//skip magic num
		this.m_bFileIndex = header[4];
		this.m_bFormatTag = header[5];
		this.m_bChannels = header[6];
		this.m_bBytesPerSec = header[7];
		this.m_bSamples = header[8];
		this.m_bBitsPerSample = header[9];
		
		
		this.m_sBlock = CodeUtil.makeShort(header,10);
		
		this.m_nLength = CodeUtil.makeInt(header,12);
		
		this.m_nOffset = CodeUtil.makeInt(header,16);
		
		this.m_nDuration = CodeUtil.makeInt(header,20);
	}
	
	public FileHeader(DataHeaderModel header){
		this.m_bFileIndex = header.m_bFileIndex;
		this.m_bFormatTag = header.m_bFormatTag;
		this.m_bChannels = header.m_bChannels;
		this.m_bBytesPerSec = header.m_bBytesPerSec;
		this.m_bSamples = header.m_bSamples;
		this.m_bBitsPerSample = header.m_bBitsPerSample;
		
		this.m_sBlock = header.m_sBlock;
		this.m_nLength = header.m_nDataLength;
		
		this.m_nOffset = 0;		
		float sizeByBits = this.m_nLength * 8;
		
		float bytesPerSec = (CodeUtil.makeShort((byte) 0, this.m_bBytesPerSec)) * 1024;
		this.m_nDuration = (int) (sizeByBits/bytesPerSec + 0.5f); //this.m_nLength * 8 / (this.m_bBytesPerSec * 1024); // (m_bSamples * 1000 * (m_bChannels + 1) * m_bBitsPerSample/8);
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[FILE_HEADER_SIZE];
		
		byte[] magicBuf = CodeUtil.int2bytes(this.m_nMagicNum, true);
		for(int i = 0; i < magicBuf.length;i++){
			resBuf[i] = magicBuf[i];
		}

		resBuf[4] = this.m_bFileIndex;
		resBuf[5] = this.m_bFormatTag;
		resBuf[6] = this.m_bChannels;
		resBuf[7] = this.m_bBytesPerSec;
		resBuf[8] = this.m_bSamples;
		resBuf[9] = this.m_bBitsPerSample;

		byte[] shortBuf = CodeUtil.short2bytes(this.m_sBlock, true);
		for(int i = 0; i < shortBuf.length;i++){
			resBuf[10+i] = shortBuf[i];
		}
		
		byte[] intBuf = CodeUtil.int2bytes(this.m_nLength, true);
		for(int i = 0; i < intBuf.length;i++){
			resBuf[12+i] = intBuf[i];
		}
		
		byte[] intBuf1 = CodeUtil.int2bytes(this.m_nOffset, true);
		for(int i = 0; i < intBuf1.length;i++){
			resBuf[16+i] = intBuf1[i];
		}
		
		byte[] intBuf2 = CodeUtil.int2bytes(this.m_nDuration, true);
		for(int i = 0; i < intBuf2.length;i++){
			resBuf[20+i] = intBuf2[i];
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
