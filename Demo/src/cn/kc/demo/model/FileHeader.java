package cn.kc.demo.model;

import cn.kc.demo.utils.CodeUtil;

public class FileHeader {
	public static final int FILE_HEADER_SIZE = 32;
	public static final int File_HEADER_OFFSET_POSTION = 24;
	public final int m_nMagicNum = 0xfafafafa;//0
	public int m_nFileIndex;//4
	public short m_sFileNum;//8
	public byte m_bFormatTag;//10	FormatTag	1	编码格式类型	00—	IMA-ADPCM；01- g.722.1
							//其他以待扩展
	public byte m_bChannels;//11	nChannels	1	编码通道数目	00—单声道； 01—双声道；
	public byte m_bBytesPerSec;//12	BytesPerSec	1	编码后的压缩速率
								//（每秒多少字节数）	以Kbits/s为单位
								//（eg：64Kbps—64）
	public byte m_bSamples; //13	Samples	1	采样率	以KHz为单位（eg：16KHz—16）
	public byte m_bBitsPerSample;//14	BitsPerSample	1	量化单位（每样本数据位数）	以bit为单位（eg：16—16）
	private byte m_bReserved;//15
	public short m_sBlock;//16	Block	2	每次处理的数据块的大小	以字节为单位，是指adpcm压缩后的（这里定义为：500）
	private short m_sReserved;//18
	public int m_nLength;//20	LENGTH	4	数据长度	仅指DATA部分的数据长度，单位是字节
	public int m_nOffset;//24   LENGTH  4   已经接收到的数据长度
	public int m_nDuration; //28        4 
	
	public FileHeader(byte[] header){
		int index = 4; //skip magic num
		this.m_nFileIndex 		=  CodeUtil.makeInt(header,index);
		index += 4;
		this.m_sFileNum 		= CodeUtil.makeShort(header,index); 
		index += 2;
		
		this.m_bFormatTag 		= header[index];	index++;
		this.m_bChannels		= header[index];	index++;
		this.m_bBytesPerSec 	= header[index];	index++;
		this.m_bSamples 		= header[index];	index++;
		this.m_bBitsPerSample 	= header[index];	index++;
		this.m_bReserved		= header[index];	index++;
		
		this.m_sBlock = CodeUtil.makeShort(header,index);	
		index += 2;
		
		this.m_sReserved = CodeUtil.makeShort(header,index);	
		index += 2;
		
		this.m_nLength = CodeUtil.makeInt(header,index);
		index += 4;
		
		this.m_nOffset = CodeUtil.makeInt(header,index);
		index += 4;
		
		this.m_nDuration = CodeUtil.makeInt(header,index);
		index += 4;
	}
	
	public FileHeader(DataHeaderModel header){
		this.m_nFileIndex = header.m_nFileIndex;
		this.m_sFileNum = header.m_sFileNum;
		this.m_bFormatTag = header.m_bFormatTag;
		this.m_bChannels = header.m_bChannels;
		this.m_bBytesPerSec = header.m_bBytesPerSec;
		this.m_bSamples = header.m_bSamples;
		this.m_bBitsPerSample = header.m_bBitsPerSample;
		this.m_bReserved = 0;
		this.m_sBlock = header.m_sBlock;
		this.m_sReserved = 0;
		this.m_nLength = header.m_nDataLength;
		
		this.m_nOffset = 0;		
		float sizeByBits = this.m_nLength * 8;
		
		float bytesPerSec = (CodeUtil.makeShort((byte) 0, this.m_bBytesPerSec)) * 1024;
		this.m_nDuration = (int) (sizeByBits/bytesPerSec + 0.5f); //this.m_nLength * 8 / (this.m_bBytesPerSec * 1024); // (m_bSamples * 1000 * (m_bChannels + 1) * m_bBitsPerSample/8);
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[FILE_HEADER_SIZE];
		int index = 0;
		
		System.arraycopy(CodeUtil.int2bytes(this.m_nMagicNum, true), 0, resBuf, index, 4);
		index += 4;
		
		System.arraycopy(CodeUtil.int2bytes(this.m_nFileIndex, true), 0, resBuf, index, 4);
		index += 4;
		
		System.arraycopy(CodeUtil.short2bytes(this.m_sFileNum, true), 0, resBuf, index, 2);
		index += 2;

		resBuf[index++] = this.m_bFormatTag;
		resBuf[index++] = this.m_bChannels;
		resBuf[index++] = this.m_bBytesPerSec;
		resBuf[index++] = this.m_bSamples;
		resBuf[index++] = this.m_bBitsPerSample;
		resBuf[index++] = this.m_bReserved;
		
		System.arraycopy(CodeUtil.short2bytes(this.m_sBlock, true), 0, resBuf, index, 2);
		index += 2;

		System.arraycopy(CodeUtil.short2bytes(this.m_sReserved, true), 0, resBuf, index, 2);
		index += 2;

		System.arraycopy(CodeUtil.int2bytes(this.m_nLength, true), 0, resBuf, index, 4);
		index += 4;
		
		System.arraycopy(CodeUtil.int2bytes(this.m_nOffset, true), 0, resBuf, index, 4);
		index += 4;
		
		System.arraycopy(CodeUtil.int2bytes(this.m_nDuration, true), 0, resBuf, index, 4);
		index += 4;
		
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
		if(m_nOffset == 0 || m_nLength == 0)
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
