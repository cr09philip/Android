package cn.kc.demo.net.socket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.os.Message;
import android.os.SystemClock;
import android.util.Pair;
import cn.kc.demo.model.DataHeaderModel;
import cn.kc.demo.model.FileHeader;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.model.NetHeaderModel;
import cn.kc.demo.model.SendReSendFileModel;
import cn.kc.demo.model.SendReceiveErrorModel;
import cn.kc.demo.model.SendReceiveOkModel;
import cn.kc.demo.model.SendTimeInfoModel;
import cn.kc.demo.view.VoiceListActivity;

public class KcReceiveMsgThread implements Runnable {
	private Socket mSocket = null;
	private ArrayList<Pair<Short, Integer>> mListError = new ArrayList<Pair<Short, Integer>>();
	private String mAppPath;

	private VoiceListActivity mContext;
	private KcSocketServer mServer;

	private long mBytesToDownload = 0;
	private long mDownloadBytes = 0;
	private long mStartMillis = 0;
	public KcReceiveMsgThread(Context context, KcSocketServer server, Socket socket, String path) {
		mContext = (VoiceListActivity) context;
		mServer = server;
		mSocket = socket;
		mAppPath = path;
	}

	public int getDownLoadSpeed() {
		long curMillis = SystemClock.uptimeMillis();
		return (int) (mDownloadBytes * 1000 / (1024 * (curMillis - mStartMillis)));
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
				if (header == null)
					continue;
				
				mBytesToDownload = header.mLength;
				mDownloadBytes += NetHeaderModel.NET_HEADER_FIXED_SIZE;
				// 做相应的处理
				switch (header.mFunction) {
				case NetHeaderModel.FUNCTION_RECEIVE_LINK:
					// 返回系統信息 FUNCTION_SEND_TIME_INFO
					SendTimeInfoModel timeModel = new SendTimeInfoModel(
							mContext.mSettingsDetails.getCode_type(),
							mContext.mSettingsDetails.getChannel_type());
					outputStream.write(timeModel.toBinStream());
					outputStream.flush();
					
					//是否在这里准备断点续传
					for(MusicInfoModel info : mContext.mListMusicInfoModels){
						if(info.m_isNeedContuinue){
							SendReSendFileModel model = new SendReSendFileModel(info.m_sIndex, info.m_nDownLoadOffset);
							
							outputStream.write(model.toBinStream());
							outputStream.flush();
						}
					}
					break;
				case NetHeaderModel.FUNCTION_RECEIVE_START:
					// 接收并存儲到本地
					DataHeaderModel dataHeader = SocketDataParser.readDataHeader(inputStream);
					if (dataHeader != null) {
						short sFileIndex = 0;
						while (true) {
							mDownloadBytes += DataHeaderModel.DATA_HEADER_FIXED_SIZE;

							File file = new File(mAppPath + "/"+ dataHeader.m_strFileName);
							RandomAccessFile outputWrite = new RandomAccessFile(file,"rw");

							FileHeader fileHeader = new FileHeader(dataHeader);
							
							//发送消息 新下载任务
							MusicInfoModel newInfo = new MusicInfoModel(
									sFileIndex,
									dataHeader.m_strFileName,
									fileHeader.m_nDuration);
							newInfo.m_nDownloadStatus = MusicInfoModel.DOWNLOAD_STATUS_BEGIN;
							newInfo.m_nDownPercent = 0;
							newInfo.m_nDownLoadSpeed = 0;
							newInfo.m_nDownLoadOffset = 0;
							
							Message newMsg = new Message();
							newMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_BEGIN_FLAG;
							newMsg.obj = newInfo;
							mServer.mHandler.sendMessage(newMsg);							

							mStartMillis = SystemClock.uptimeMillis();

							// 写文件头
							outputWrite.write(fileHeader.toBinStream(), 0,FileHeader.FILE_HEADER_SIZE);
//							outputWrite.flush();

							byte buffer[] = new byte[4 * 1024];
							int nRemain = dataHeader.m_nDataLength;

							// 将InputStream当中的数据取出，并写入到文件
							// 以包中的數據長度為依據
							int nDownloadOffsetPerFile = 0;
							while (nRemain > 0) {
								int temp = inputStream.read(buffer);
								if (temp == -1 && nRemain > 0) {
									// 失敗存儲序號到list
									break;
								}

								mDownloadBytes += temp;
								
								outputWrite.seek(FileHeader.File_HEADER_OFFSET_POSTION + nDownloadOffsetPerFile);
								outputWrite.write(buffer, 0, temp);
								//向文件中写入OFFSET,标示当下是否下载完成
								outputWrite.seek(FileHeader.File_HEADER_OFFSET_POSTION);
								outputWrite.writeInt(nDownloadOffsetPerFile);								
//								outputWrite.flush();
								
								nRemain -= temp;
								nDownloadOffsetPerFile += temp;
								
								//发送消息 下载中 
								newInfo.m_nDownloadStatus = MusicInfoModel.DOWNLOAD_STATUS_PROGRESSING;
								newInfo.m_nDownPercent = nDownloadOffsetPerFile *100 / fileHeader.m_nLength;
								newInfo.m_nDownLoadSpeed = getDownLoadSpeed();								
								newInfo.m_nDownLoadOffset = nDownloadOffsetPerFile;
								
								Message tmpMsg = new Message();
								tmpMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_PROGRESSING_FLAG;
								tmpMsg.obj = newInfo;

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
								
								Message finishMsg = new Message();
								finishMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_END_FLAG;
								finishMsg.obj = newInfo;

								mServer.mHandler.sendMessage(finishMsg);
							}else{ //失败处理
								file.delete();
								
								Pair<Short, Integer> pair = new Pair<Short, Integer>(sFileIndex, nDownloadOffsetPerFile);
								mListError.add( pair);
								
								Message errorMsg = new Message();
								errorMsg.what = KcSocketServer.DownloadInfoHandler.DOWNLOAD_ERROR_FLAG;
								errorMsg.obj = newInfo;

								mServer.mHandler.sendMessage(errorMsg);
							}
							
							if (sFileIndex >= dataHeader.m_sFileNum)
								break;

							// 讀下一個文件的文件頭
							dataHeader = SocketDataParser.readDataHeader(inputStream);
						}
					}

					break;
				case NetHeaderModel.FUNCTION_RECEIVE_END:
					// 判斷是否接收完整，并返回接收成功或失敗信息
					if (mListError.isEmpty()) {
						// FUNCTION_FILE_OK
						outputStream.write(new SendReceiveOkModel().toBinStream());
					} else {
						// FUNCTION_FILE_ERROR
						outputStream.write(new SendReceiveErrorModel(mListError).toBinStream());
						
						mListError.clear();
					}

					outputStream.flush();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
		}

		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void recycle() {
		
	}
}
