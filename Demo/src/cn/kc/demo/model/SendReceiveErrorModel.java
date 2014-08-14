package cn.kc.demo.model;

import java.util.ArrayList;

import android.util.Pair;

import cn.kc.demo.utils.CodeUtil;

//Function FUNCTION_FILE_ERROR   = 2
public class SendReceiveErrorModel extends NetHeaderModel{
	private ArrayList<Pair<Integer, Integer>> mList;

	public SendReceiveErrorModel(ArrayList<Pair<Integer, Integer>> list) {
		super(2*list.size(), NetHeaderModel.FUNCTION_FILE_ERROR);
		mList = list;
	}

	public byte[] toBinStream(){
		byte[] resBuf = new byte[10+2*mList.size()];
		int nIndex = 0;
		byte[] header = super.toBinStream();
		System.arraycopy(header, 0, resBuf, nIndex, header.length);
		nIndex += header.length;
		
		for(int i = 0; i < mList.size(); i++){
			System.arraycopy( 	CodeUtil.short2bytes(mList.get(i).first.shortValue(), true),
								0, 
								resBuf, 
								nIndex, 
								2);
			
			nIndex += 2;
		}

		return resBuf;
	}
}
