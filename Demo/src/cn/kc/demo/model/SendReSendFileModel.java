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

		byte[] header = super.toBinStream();
		for (int i = 0; i < header.length; i++){
			resBuf[i] = header[i];
		}
		
		resBuf[10] = m_mode;
		
		byte[] shortBuf = CodeUtil.short2bytes(m_nCount, true);
		for(int i = 0; i < shortBuf.length;i++){
			resBuf[11+i] = shortBuf[i];
		}
		byte[] shortBuf1 = CodeUtil.short2bytes(m_nIndex, true);
		for(int i = 0; i < shortBuf1.length;i++){
			resBuf[13+i] = shortBuf1[i];
		}
		
		byte[] intBuf = CodeUtil.int2bytes(m_nOffset, true);
		for(int i = 0; i < intBuf.length;i++){
			resBuf[15+i] = intBuf[i];
		}

		return resBuf;
	}
}
