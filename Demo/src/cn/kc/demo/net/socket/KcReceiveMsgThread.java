package cn.kc.demo.net.socket;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import cn.kc.demo.R;
import cn.kc.demo.model.DataHeaderModel;
import cn.kc.demo.model.FileHeader;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.model.NetHeaderModel;
import cn.kc.demo.model.SendReSendFileModel;
import cn.kc.demo.model.SendReceiveErrorModel;
import cn.kc.demo.model.SendReceiveOkModel;
import cn.kc.demo.model.SendTimeInfoModel;
import cn.kc.demo.net.socket.KcSocketServer.ClientInfo;
import cn.kc.demo.net.socket.KcSocketServer.DownloadInfoHandler;
import cn.kc.demo.utils.FileUtil;
import cn.kc.demo.utils.Utils;
import cn.kc.demo.view.VoiceListActivity;

public class KcReceiveMsgThread implements Runnable {
	private static final String TAG = "KcReceiveMsgThread";
	private Socket mSocket = null;
	private ArrayList<Pair<Short, Integer>> mListError = new ArrayList<Pair<Short, Integer>>();
	private String mAppPath;

	public String getAppPath() {
		return mAppPath;
	}

	private VoiceListActivity mContext;
	private KcSocketServer mServer;

	private long mBytesToDownload = 0;
	private long mDownloadBytes = 0;
	private long mStartMillis = 0;
	private Message mDestroyMsg;
	public boolean mIsPause = false;
	public Pair<String, Boolean> mIsNeedRename = new Pair<String, Boolean>(null, false);
	public boolean mIsDeleteSendFile = false;
	public KcReceiveMsgThread(Context context, KcSocketServer server, Socket socket, String path) {
		mContext = (VoiceListActivity) context;
		mServer = server;
		mSocket = socket;
		mAppPath = path;
	}
	public class ReceiveInfo{
		public int total;
		public MusicInfoModel info;
		public ReceiveInfo(int count, MusicInfoModel obj){
			total = count;
			info = obj;
		}
		public ReceiveInfo() {
			// TODO Auto-generated constructor stub
		}
	}
	
	public int getDownLoadSpeed() {
		long curMillis = System.currentTimeMillis();//
//		SystemClock.uptimeMillis();
		int secs = (int) ((curMillis - mStartMillis)/1000);
		secs = secs == 0 ? 1 : secs;
		
		int ret = // (int) (mDownloadBytes/1024/ (1024 * (curMillis - mStartMillis)));
		(int) ((mDownloadBytes/1024 ) / secs);
		
		if(ret/1024 > 100)
		{
			Log.d("SpeedError", "Bytes:" + mDownloadBytes + " millis:" + (curMillis - mStartMillis));
		}
		return ret;
//		(bytes /1024 ) / ( sec /1000)
	}
	
	public int getAllDownLoadPercent() {
		return (int) (mDownloadBytes / mBytesToDownload);
	}

	public void run() {
		while (true) {
			try {
				//网络读写的流
				InputStream inputStream = mSocket.getInputStream();
				OutputStream outputStream = mSocket.getOutputStream();

				// 接收消息，并解析
				NetHeaderModel header = SocketDataParser.readNetHeader(inputStream);
				if (header == null){
					break;
				}
				
				mBytesToDownload = header.mLength;
				mDownloadBytes = NetHeaderModel.NET_HEADER_FIXED_SIZE;
				mStartMillis = System.nanoTime();//System.currentTimeMillis();//SystemClock.uptimeMillis();
				short sFileIndex = 0;
				
				// 做相应的处理
				switch (header.mFunction) {
				case NetHeaderModel.FUNCTION_RECEIVE_LINK:
					// 返回系統信息 FUNCTION_SEND_TIME_INFO
					mServer.mHandler.sendMessage(getConnectMsg());	
										
					if( !checkIfNeedContinue(outputStream)){
						SendTimeInfoModel timeModel = null;
						if( mContext.getSettingsDetails().getCode_type() == 1){//g722.1
							timeModel =
									new SendTimeInfoModel(	mContext.getSettingsDetails().getChannel_type(),
															mContext.getSettingsDetails().getBand_width_value(),
															mContext.getSettingsDetails().getBit_rate_value() );
						}else{
							timeModel = new SendTimeInfoModel(mContext.getSettingsDetails().getChannel_type());
						}
						
						outputStream.write(timeModel.toBinStream());
						outputStream.flush();
					}
					
					mServer.mHandler.sendMessageDelayed(getDisConnectMsg(), 1000);	
					break;
				case NetHeaderModel.FUNCTION_RECEIVE_START:
					mServer.mHandler.sendMessage(getConnectMsg());
					
					// 接收并存儲到本地
					DataHeaderModel dataHeader = SocketDataParser.readDataHeader(inputStream, sFileIndex);
					ReceiveInfo info = new ReceiveInfo();

					int nDownloadOffsetPerFile = 0;
					if (dataHeader != null) {
						mDownloadBytes += DataHeaderModel.DATA_HEADER_FIXED_SIZE;
						String oldname = mAppPath + "/"+ dataHeader.m_strFileName;
						if(FileUtil.IsFileExist(oldname)){
							MusicInfoModel music = mContext.getMusicInfoModelByName(dataHeader.m_strFileName);
							if(music != null){
								nDownloadOffsetPerFile = music.m_nDownLoadOffset;
								if( !music.m_isNeedContuinue){
									mIsPause = true;
									Message msg = new Message();
									msg.what = KcSocketServer.DownloadInfoHandler.SOCKET_RENAME_FILE;
									msg.obj = new Pair<KcReceiveMsgThread, String>(KcReceiveMsgThread.this, dataHeader.m_strFileName);
									mServer.mHandler.sendMessage(msg);	
								}
							}
						}
						while (mIsPause) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								Thread.sleep(500);
								e.printStackTrace();
							}
						}
						if (mIsNeedRename != null && mIsNeedRename.first != null) {
							if(mIsNeedRename.first.hashCode() == dataHeader.m_strFileName.hashCode()
									&& mIsNeedRename.second == true){
//									do{
//										dataHeader.m_strFileName += "(1)";
//										oldname = mAppPath + "/"+ dataHeader.m_strFileName;
//									}while(FileUtil.IsFileExist(oldname));
								
								//skip
								int toskip = dataHeader.m_nDataLength;
								do{
									toskip -= inputStream.skip(toskip);
								}while(toskip > 0);
								
								// 讀下一個文件的文件頭
//								dataHeader = SocketDataParser.readDataHeader(inputStream);
								break;
							}
						}
						
						File file = new File(oldname);
						RandomAccessFile outputWrite = new RandomAccessFile(file,"rw");

						FileHeader fileHeader = new FileHeader(dataHeader);
						
						//发送消息 新下载任务
						MusicInfoModel newInfo = getMusicModel(sFileIndex, dataHeader.m_strFileName, fileHeader.m_nDuration);
						
						newInfo.m_nDownloadStatus = MusicInfoModel.DOWNLOAD_STATUS_BEGIN;
						newInfo.m_nDownPercent = 0;
						newInfo.m_nDownLoadSpeed = 0;
						newInfo.m_nDownLoadOffset = 0;
						newInfo.mTotalBytes = mDownloadBytes;
						newInfo.mStartNanoSecs =  mStartMillis;
						
						
						info.total = dataHeader.m_sFileNum;
						info.info = newInfo;
						Message newMsg = new Message();
						newMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_BEGIN_FLAG;
						newMsg.obj = info;
						mServer.mHandler.sendMessage(newMsg);							

						// 写文件头
						outputWrite.write(fileHeader.toBinStream(), 0,FileHeader.FILE_HEADER_SIZE);
//							outputWrite.flush();

						byte buffer[] = new byte[4 * 1024];
						int nRemain = dataHeader.m_nDataLength;

						// 将InputStream当中的数据取出，并写入到文件
						// 以包中的數據長度為依據
						
						while (nRemain > 0) {
//								int temp = inputStream.read(buffer);
							int size2read = nRemain > 4*1024 ? 4*1024 : nRemain;
							if(size2read != 4096){
								Log.d("", "");
							}
							int temp = inputStream.read(buffer, 0, size2read);
							if (temp == -1 && nRemain > 0) {
								// 失敗存儲序號到list
								Log.d(TAG, "SOCKET Buffer read error");
								break;
							}

							mDownloadBytes += temp;
							
							outputWrite.seek(FileHeader.FILE_HEADER_SIZE + nDownloadOffsetPerFile);
							outputWrite.write(buffer, 0, temp);
							
							nRemain -= temp;
							nDownloadOffsetPerFile += temp;

							//向文件中写入OFFSET,标示当下是否下载完成
							outputWrite.seek(FileHeader.File_HEADER_OFFSET_POSTION);
							outputWrite.writeInt(nDownloadOffsetPerFile);								
//								outputWrite.flush();
							//发送消息 下载中 
							newInfo.m_nDownloadStatus = MusicInfoModel.DOWNLOAD_STATUS_PROGRESSING;
							newInfo.m_nDownPercent = calcPercent(fileHeader.m_nLength, nDownloadOffsetPerFile);
							
							newInfo.m_nDownLoadSpeed = 0;								
							newInfo.m_nDownLoadOffset = nDownloadOffsetPerFile;
							newInfo.mTotalBytes = mDownloadBytes;
							newInfo.mStartNanoSecs =  mStartMillis;

							info.total = dataHeader.m_sFileNum;
							info.info = newInfo;
							Message tmpMsg = new Message();
							tmpMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_PROGRESSING_FLAG;
							tmpMsg.obj = info;

							mServer.mHandler.sendMessage(tmpMsg);
						}

						outputWrite.close();
						sFileIndex++;
						
						// 此时发送该文件接收完成消息，即时更新界面
						if(nDownloadOffsetPerFile == fileHeader.m_nLength){
							
							newInfo.m_nDownloadStatus = MusicInfoModel.DOWNLOAD_STATUS_END;
							newInfo.m_nDownPercent = 100;
							newInfo.m_nDownLoadSpeed = 0;								
							newInfo.m_nDownLoadOffset = nDownloadOffsetPerFile;
							newInfo.mTotalBytes = mDownloadBytes;
							newInfo.mStartNanoSecs = mStartMillis;
														
							info.total = dataHeader.m_sFileNum;
							info.info = newInfo;
							Message finishMsg = new Message();
							finishMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_END_FLAG;
							finishMsg.obj = info;

							mServer.mHandler.sendMessage(finishMsg);
						}else{ //失败处理
							file.delete();
							
							Pair<Short, Integer> pair = new Pair<Short, Integer>(sFileIndex, nDownloadOffsetPerFile);
							mListError.add( pair);

							info.total = dataHeader.m_sFileNum;
							info.info = newInfo;
							Message errorMsg = new Message();
							errorMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_ERROR_FLAG;
							errorMsg.obj = info;

							mServer.mHandler.sendMessage(errorMsg);
						}
					}

					break;
				case NetHeaderModel.FUNCTION_RECEIVE_END:
					// 判斷是否接收完整，并返回接收成功或失敗信息
					if (mListError.isEmpty()) {
						// FUNCTION_FILE_OK
						if(true){//IS NEED DELETE
							mIsPause = true;
							Message msg = new Message();
							msg.what = KcSocketServer.DownloadInfoHandler.SOCKET_IS_NEED_DELETE_SEND_FILES;
							msg.obj = KcReceiveMsgThread.this;
							mServer.mHandler.sendMessage(msg);	
						}
						while (mIsPause) {
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								Thread.sleep(500);
								e.printStackTrace();
							}
						}
						if ( mIsDeleteSendFile ) {
							outputStream.write(new SendReceiveOkModel(true).toBinStream());
						}else{
							outputStream.write(new SendReceiveOkModel(false).toBinStream());
						}
						
						mIsDeleteSendFile = false;
					} else {
						// FUNCTION_FILE_ERROR
						outputStream.write(new SendReceiveErrorModel(mListError).toBinStream());
						
						mListError.clear();
					}
					
					sFileIndex = 0;

					mServer.mHandler.sendMessage(getDisConnectMsg());

					outputStream.flush();
					break;
				default:
						Log.d("", "");
				}
			} catch (Exception e) {
				e.printStackTrace();
				break;
			}
		}

		try {
			mSocket.close();
			mServer.mHandler.sendMessage(getDestroyMsg());	
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean checkIfNeedContinue(OutputStream outputStream) {
		//是否在这里准备断点续传
		ArrayList<Pair<Integer,Integer>> array = new ArrayList<Pair<Integer,Integer>>();
		for(MusicInfoModel info : mContext.mListMusicInfoModels){
			if(info.m_isNeedContuinue){
				array.add(new Pair<Integer, Integer>((int) info.m_sIndex, info.m_nDownLoadOffset));
			}
		}
		
		if(array.size() != 0){
			Collections.sort(array, new Comparator<Pair<Integer, Integer>>() {

				public int compare(Pair<Integer, Integer> lhs,
						Pair<Integer, Integer> rhs) {
					if(lhs.first > rhs.first)
						return 1;
					return 0;
				}
			});

			Pair<Integer, Integer> begin = array.get(0);
			if(array.size() > 3){				
				if((mContext.mListMusicInfoModels.size() -1 - begin.first)/2 > array.size() -1){
					
					for(Pair<Integer, Integer> item : array){
						SendReSendFileModel model = new SendReSendFileModel((byte)1, item.first.shortValue(), item.second);
						
						try {
							outputStream.write(model.toBinStream());
							outputStream.flush();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					return true;
				}
			}

			//send resend 0 ,resend all after first
			SendReSendFileModel model = new SendReSendFileModel((byte)0, begin.first.shortValue(), begin.second);
			
			try {
				outputStream.write(model.toBinStream());
				outputStream.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return true;
		}
		
		return false;
	}

	private MusicInfoModel getMusicModel(short sFileIndex, String fileName, int duration) {
		MusicInfoModel ret = mContext.getMusicInfoModelByName(fileName);
		if(ret == null)
			ret = new MusicInfoModel(sFileIndex, fileName, duration);
		
		return ret;
	}

	private int calcPercent(int totalLength, int nDownloadOffsetPerFile) {
		return (int) ((float)nDownloadOffsetPerFile / (float)totalLength *100);
	}

	private Message getDisConnectMsg() {
		Message msg = new Message();
		msg.what = DownloadInfoHandler.SOCKET_DISCONNECT;
		msg.obj = mSocket;
		
		return msg;
	}
	
	private Message getConnectMsg() {
		Message msg = new Message();
		msg.what = DownloadInfoHandler.SOCKET_CONNECT;
		msg.obj = mSocket;
		
		return msg;
	}
	
	private Message getDestroyMsg() {
		if(mDestroyMsg == null){
			mDestroyMsg = new Message();
		}
		mDestroyMsg.what = DownloadInfoHandler.SOCKET_DESTROY;
		mDestroyMsg.obj = mSocket;
		
		return mDestroyMsg;
	}

	public void recycle() {
		
	}
}
