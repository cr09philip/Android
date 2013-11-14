package cn.kc.demo.model;

import java.util.ArrayList;

import cn.kc.demo.utils.CodeUtil;

//Function FUNCTION_FILE_ERROR   = 2
public class SendReceiveErrorModel extends NetHeaderModel{
	private ArrayList<Short> mList;

	public SendReceiveErrorModel(ArrayList<Short> list) {
		super(2*list.size(), NetHeaderModel.FUNCTION_FILE_ERROR);
		mList = list;
	}

	public byte[] toBinStream(){
		byte[] resBuf = new byte[10+2*mList.size()];
		int nIndex = 0;
		byte[] header = super.toBinStream();
		for (int i = 0; i < header.length; i++){
			resBuf[nIndex++] = header[i];
		}
		
		
		for(int i = 0; i < mList.size(); i++){
			byte[] sBuf = CodeUtil.short2bytes(mList.get(i).shortValue(), true);
			resBuf[nIndex++] = sBuf[0];
			resBuf[nIndex++] = sBuf[1];
		}

		return resBuf;
	}
}
