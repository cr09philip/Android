package cn.kc.demo.net.socket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.os.Message;
import android.os.SystemClock;
import cn.kc.demo.model.DataHeaderModel;
import cn.kc.demo.model.FileHeader;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.model.NetHeaderModel;
import cn.kc.demo.model.SendReceiveErrorModel;
import cn.kc.demo.model.SendReceiveOkModel;
import cn.kc.demo.model.SendTimeInfoModel;
import cn.kc.demo.view.VoiceListActivity;

public class KcReceiveMsgThread implements Runnable {
	private Socket mSocket = null;
	private ArrayList<Short> mListError = new ArrayList<Short>();
	private String mAppPath;

	private VoiceListActivity mContext;
	private long mReceiveCount = 0;
	private long mStartMillis = 0;

	public KcReceiveMsgThread(Context context, Socket socket, String path) {
		mContext = (VoiceListActivity) context;
		mSocket = socket;
		mAppPath = path;
	}

	public int getDownLoadSpeed() {
		long curMillis = SystemClock.uptimeMillis();
		return (int) (mReceiveCount * 1000 / (1024 * (curMillis - mStartMillis)));
	}

	public void run() {
		while (true) {
			try {
				InputStream inputStream = mSocket.getInputStream();
				OutputStream outputStream = mSocket.getOutputStream();

				// 接收消息，并解析
				NetHeaderModel header = SocketDataParser
						.readNetHeader(inputStream);
				if (header == null)
					continue;

				mReceiveCount += NetHeaderModel.NET_HEADER_FIXED_SIZE;
				// 做相应的处理
				switch (header.mFunction) {
				case NetHeaderModel.FUNCTION_RECEIVE_LINK:
					// 返回系統信息 FUNCTION_SEND_TIME_INFO
					outputStream.write(new SendTimeInfoModel((byte) 0)
							.toBinStream());
					outputStream.flush();
					break;
				case NetHeaderModel.FUNCTION_RECEIVE_START:
					// 接收并存儲到本地
					DataHeaderModel dataHeader = SocketDataParser
							.readDataHeader(inputStream);
					if (dataHeader != null) {
						int nFileIndex = 0;
						while (true) {
							mReceiveCount += DataHeaderModel.DATA_HEADER_FIXED_SIZE;

							File file = new File(mAppPath + "/"
									+ dataHeader.m_strFileName);
							FileOutputStream outputWrite = new FileOutputStream(
									file);

							FileHeader fileHeader = new FileHeader(dataHeader);
							// 这里应该更新listAdapter的数据了，新加项
							MusicInfoModel newInfo = new MusicInfoModel(
									mContext.mMusicInfoModels.size(),
									dataHeader.m_strFileName,
									fileHeader.m_nDuration);
							Message newMsg = new Message();
							newMsg.what = VoiceListActivity.MSG_NEW_DOWNLOAD_STATUS;
							newMsg.obj = newInfo;

							mContext.mDownLoadHander.sendMessage(newMsg);							

							mStartMillis = SystemClock.uptimeMillis();

							// 写文件头
							outputWrite.write(fileHeader.toBinStream(), 0,
									FileHeader.FILE_HEADER_SIZE);
							outputWrite.flush();

							byte buffer[] = new byte[4 * 1024];
							int nRemain = dataHeader.m_nDataLength;

							// 将InputStream当中的数据取出，并写入到文件
							// 以包中的數據長度為依據
							int nDownloadOffset = 0;
							while (nRemain > 0) {
								int temp = inputStream.read(buffer);
								if (temp == -1 && nRemain > 0) {
									// 失敗存儲序號到list
									mListError.add(new Short((short) nFileIndex));
									break;
								}

								mReceiveCount += temp;

								outputWrite.write(buffer, 0, temp);
								outputWrite.flush();
								nRemain -= temp;
								nDownloadOffset += temp;
								
								// 此时是否应该发消息即时更新界面
								MusicInfoModel tmpInfo = new MusicInfoModel(
										0,
										dataHeader.m_strFileName,
										fileHeader.m_nDuration);

								tmpInfo.m_nDownloadStatus = 1;
								tmpInfo.m_nDownPercent = nDownloadOffset *100 / fileHeader.m_nLength;
								tmpInfo.m_nDownLoadOffset = nDownloadOffset;
								tmpInfo.m_nDownLoadSpeed = getDownLoadSpeed();
								Message tmpMsg = new Message();
								tmpMsg.what = VoiceListActivity.MSG_DOWNLOAD_CHANGE_STATUS;
								tmpMsg.obj = tmpInfo;

								mContext.mDownLoadHander.sendMessage(tmpMsg);
							}
							outputWrite.close();
							nFileIndex++;

							// 此时发送该文件接收完成消息，即时更新界面
							MusicInfoModel finishInfo = new MusicInfoModel(
									mContext.mMusicInfoModels.size(),
									dataHeader.m_strFileName,
									0);
							
							finishInfo.m_nDownloadStatus = 2;
							finishInfo.m_nDownPercent = 100;
							finishInfo.m_nDownLoadOffset = nDownloadOffset;
							Message finishMsg = new Message();
							finishMsg.what = VoiceListActivity.MSG_DOWNLOAD_OK_STATUS;
							finishMsg.obj = finishInfo;

							mContext.mDownLoadHander.sendMessage(newMsg);

							if (nFileIndex >= dataHeader.m_sFileNum)
								break;

							// 讀下一個文件的文件頭
							dataHeader = SocketDataParser
									.readDataHeader(inputStream);
						}
					}

					break;
				case NetHeaderModel.FUNCTION_RECEIVE_END:
					// 判斷是否接收完整，并返回接收成功或失敗信息
					if (mListError.isEmpty()) {
						// FUNCTION_FILE_OK
						outputStream.write(new SendReceiveOkModel()
								.toBinStream());
					} else {
						// FUNCTION_FILE_ERROR
						outputStream
								.write(new SendReceiveErrorModel(mListError)
										.toBinStream());
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
}
