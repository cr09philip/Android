package cn.kc.demo.model;

import cn.kc.demo.utils.CodeUtil;

//FUNCTION_RESEND_INFO   = 3;  //文件续传
public class SendReSendFileModel extends NetHeaderModel {
	
	private static final int RESEND_CMD_HEADER_FIXED_SIZE = NetHeaderModel.NET_HEADER_FIXED_SIZE + 9;
	byte m_mode = 0;
	short m_nCount;
	short m_nIndex = 0;
	int m_nOffset = 0;
	public SendReSendFileModel(byte mode, short count, short index, int offset){
		super(6, NetHeaderModel.FUNCTION_RESEND_INFO);
		m_mode = mode;
		m_nCount = count;
		m_nIndex = index;
		m_nOffset = offset;
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[RESEND_CMD_HEADER_FIXED_SIZE];
		int nIndex = 0;
		byte[] header = super.toBinStream();
		System.arraycopy(header, 0, resBuf, nIndex, header.length);
		nIndex += header.length;
		
		resBuf[nIndex++] = m_mode;
		
		System.arraycopy(CodeUtil.short2bytes(m_nCount, true), 0, resBuf, nIndex, 2);
		nIndex += 2;

		System.arraycopy(CodeUtil.short2bytes(m_nIndex, true), 0, resBuf, nIndex, 2);
		nIndex += 2;
		
		System.arraycopy(CodeUtil.int2bytes(m_nOffset, true), 0, resBuf, nIndex, 4);
		nIndex += 4;

		return resBuf;
	}
}
