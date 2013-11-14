package cn.kc.demo.model;

//Function FUNCTION_FILE_OK = 1
public class SendReceiveOkModel extends NetHeaderModel{
	public SendReceiveOkModel() {
		super(0, NetHeaderModel.FUNCTION_FILE_OK);
	}

	public byte[] toBinStream(){
		return super.toBinStream();
	}
}
