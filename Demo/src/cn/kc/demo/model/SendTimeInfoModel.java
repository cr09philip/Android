package cn.kc.demo.model;

import java.util.Calendar;

//Function FUNCTION_SEND_TIME_INFO  = 0
public class SendTimeInfoModel extends NetHeaderModel{

	public byte m_bChannels;
	public byte m_bCodeType;
	
	public SendTimeInfoModel(byte code_type, byte channels){
		super(7, NetHeaderModel.FUNCTION_SEND_TIME_INFO);
		
		m_bChannels = channels;
		m_bCodeType = code_type;
	}
	
	public byte[] toBinStream(){
		byte[] resBuf = new byte[17];
		int nIndex = 0;
		byte[] header = super.toBinStream();
		for (int i = 0; i < header.length; i++){
			resBuf[nIndex++] = header[i];
		}

		final Calendar c = Calendar.getInstance();
		
		resBuf[nIndex++] = (byte) (c.get(Calendar.YEAR) - 2000); //获取当前年份 );
		resBuf[nIndex++] = (byte) (c.get(Calendar.MONTH) + 1);//获取当前月份
		resBuf[nIndex++] = (byte) c.get(Calendar.DAY_OF_MONTH);//获取当前月份的日期号码
		resBuf[nIndex++] = (byte) c.get(Calendar.HOUR_OF_DAY);//获取当前的小时数 
		resBuf[nIndex++] = (byte) c.get(Calendar.MINUTE);//获取当前的分钟数
		resBuf[nIndex++] = (byte) c.get(Calendar.SECOND);//获取当前的分钟数

		resBuf[nIndex++] = (byte) (m_bChannels | (m_bCodeType << 4));
		
		return resBuf;
	}
}
