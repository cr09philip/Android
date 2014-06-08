package cn.kc.demo.model;

import cn.kc.demo.utils.CodeUtil;

//FUNCTION_RESEND_INFO   = 3;  //文件续传
public class SendReSendFileModel extends NetHeaderModel {
	byte m_mode = 0;
	short m_nIndex = 0;
	int m_nOffset = 0;
	public SendReSendFileModel(byte mode, short index, int offset){
		super(6, NetHeaderModel.FUNCTION_RESEND_INFO);
		m_mode = mode;
		m_nIndex = index;
		m_nOffset = offset;
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[16];

		byte[] header = super.toBinStream();
		for (int i = 0; i < header.length; i++){
			resBuf[i] = header[i];
		}
		
		resBuf[10] = m_mode;
		
		byte[] shortBuf = CodeUtil.short2bytes(m_nIndex, true);
		for(int i = 0; i < shortBuf.length;i++){
			resBuf[11+i] = shortBuf[i];
		}
		
		byte[] intBuf = CodeUtil.int2bytes(m_nOffset, true);
		for(int i = 0; i < intBuf.length;i++){
			resBuf[13+i] = intBuf[i];
		}

		return resBuf;
	}
}
