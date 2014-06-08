package cn.kc.demo.model;

import cn.kc.demo.utils.CodeUtil;

public class DataHeaderModel /*extends NetHeaderModel*/ {
	public static final byte FORMAT_IMA_ADPCM  = 0;  //IMA-ADPCM
	public static final byte FORMAT_G_722_1 = 1;  //g.722.1
	
	public static final int FILE_NAME_LENGTH = 35;
	public static final int DATA_HEADER_FIXED_SIZE = 48;
	
	public byte m_bFileIndex;
	public short m_sFileNum;//10	FILENUM	2	文件总数量	
	public byte m_bFormatTag;//12	FormatTag	1	编码格式类型	00—	IMA-ADPCM；01- g.722.1
							//其他以待扩展
	public byte m_bChannels;//13	nChannels	1	编码通道数目	00—单声道；
							//01—双声道；
	public byte m_bBytesPerSec;//	14	BytesPerSec	1	编码后的压缩速率
								//（每秒多少字节数）	以Kbits/s为单位
								//（eg：64Kbps—64）
	public byte m_bSamples; //15	Samples	1	采样率	以KHz为单位（eg：16KHz—16）
	public byte m_bBitsPerSample;//16	BitsPerSample	1	量化单位（每样本数据位数）	以bit为单位（eg：16—16）
	public short m_sBlock;//17	Block	2	每次处理的数据块的大小	以字节为单位，是指adpcm压缩后的（这里定义为：500）
	public String m_strFileName;//19	FILENAME	35	文件名称	文件名称信息：文件序号+文件录音时间+文件录音时长 信息
								//（eg：A000001_20101218_101010_003000.vox）
	public int m_nDataLength;//54	LENGTH	4	数据长度	仅指DATA部分的数据长度，单位是字节
	
						//	58	DATA	不定	信息数据
	
	public DataHeaderModel(byte[] header, int start, int fileIndex){
		this.m_bFileIndex = (byte) fileIndex;
		int index = start;
		this.m_sFileNum = CodeUtil.makeShort(header,index);
		index += 2;
		
		this.m_bFormatTag = header[index++];
		this.m_bChannels = header[index++];
		this.m_bBytesPerSec = header[index++];
		this.m_bSamples = header[index++];
		this.m_bBitsPerSample = header[index++];
		this.m_sBlock = CodeUtil.makeShort(header,index);
		index += 2;

		this.m_strFileName = new String(header, index, DataHeaderModel.FILE_NAME_LENGTH);
		index += DataHeaderModel.FILE_NAME_LENGTH;
		
		this.m_nDataLength = CodeUtil.makeInt(header,index);
		index += 4;
	}
}
