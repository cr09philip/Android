package cn.kc.demo.model;

import java.util.Calendar;

//Function FUNCTION_SEND_TIME_INFO  = 0
public class SendTimeInfoModel extends NetHeaderModel{

	public byte m_bCodeType;
	public byte m_bChannels;
	public byte m_bBandWitdh;
	public byte m_bBitRate;
	
	public SendTimeInfoModel(byte channels){//for adpcm
		super(7, NetHeaderModel.FUNCTION_SEND_TIME_INFO);

		m_bCodeType = 0;
		m_bChannels = channels;
	}
	
	public SendTimeInfoModel(byte channels, byte band_width, byte bit_rate){//for g722.1
		super(7, NetHeaderModel.FUNCTION_SEND_TIME_INFO);

		m_bCodeType = 1;
		m_bChannels = channels;
		m_bBandWitdh = band_width;
		m_bBitRate = bit_rate;
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

		int code_type = 0;
		if(m_bCodeType == 1){// for g722.1
			if(m_bBandWitdh == 14){
				if(m_bBitRate == 24){
					code_type = 0x0b;//1011;
				}else if(m_bBitRate == 48){
					code_type = 0x0d;//1101;
				}else{ //default 32
					code_type = 9;//1001
				}
			}
			else{//default 7
				if(m_bBitRate == 24){
					code_type = 3;//0011
				}else{// default 32
					code_type = 1;//0001
				}
			}
		}else{ // for adpcm
			code_type = m_bCodeType;
		}
		
		resBuf[nIndex++] = (byte) (m_bChannels | (code_type << 4));
		
		return resBuf;
	}
}
