package cn.kc.demo.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MusicInfoModel {
	public static final int DOWNLOAD_STATUS_BEGIN = 0;
	public static final int DOWNLOAD_STATUS_PROGRESSING = 1;
	public static final int DOWNLOAD_STATUS_END = 2;
	public static final int DOWNLOAD_STATUS_LOCAL = 3;
	public static final int DOWNLOAD_STATUS_ERROR = 4;
	
	public short m_sFileNum;
	public int m_nIndex;
	public String m_strName;	//just name under apps path
	
	public int m_nDownloadStatus; // 0 未下载   1 下载中 此时 2下载完成  3本地文件 4失败
	public int m_nDownPercent;
	public int m_nDownLoadOffset;
	
//	public int m_nPlayStatus; //1 未播放 2 播放中 3 放暂停 4停止 
	public int m_nDuration; //总时长
	public int m_nCurProgress; //当前进度
	public int m_nErrorTimes;
	
	public int m_nDownLoadSpeed; // KBytes/s
	
	public boolean m_isNeedContinue;
	public boolean m_isPlaying;
	
	public long mTotalBytes;
	public long mStartNanoSecs;
	
	public int m_nFileLength;
	
	public MusicInfoModel(){
		m_strName = null;
		
		m_sFileNum = (short) 0;
		m_nIndex = 0;
		m_nDownloadStatus =
		m_nDownPercent = 
		m_nDownLoadOffset = 
//		m_nPlayStatus = 
		m_nDuration = 
		m_nCurProgress = 
		m_nDownLoadSpeed = 
		m_nErrorTimes = 0;
		m_isNeedContinue = false;
		m_isPlaying = false;
		
		mTotalBytes = 0;
		mStartNanoSecs = 0;
		
		m_nFileLength = 0;
	}
	
	public MusicInfoModel(int index, short fileNum, String path, int duration ){	
		m_sFileNum = (short) 0;
		m_nIndex = index;
		m_strName = path;
		
		m_nDownloadStatus = 0;
		m_nDownPercent = 0;

		m_nDownLoadOffset = 0;
//		m_nPlayStatus = 0;
		m_nDuration = duration;
		m_nCurProgress = 0;
		
		m_nDownLoadSpeed = 0;
		m_isNeedContinue = false;
		m_isPlaying = false;
		
		mTotalBytes = 0;
		mStartNanoSecs = 0;
		
		m_nFileLength = 0;
	}
	
//	public static int getMusicFileDuration(String fullPath){
//		int nRes = 0;
//		FileInputStream input = null;
//		try {
//			input = new FileInputStream( fullPath);
//			byte[] header = new byte[FileHeader.FILE_HEADER_SIZE];
//			input.read(header, 0, FileHeader.FILE_HEADER_SIZE);
//
//			FileHeader mHeader = new FileHeader(header);
//			nRes = mHeader.m_nDuration;
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch(IOException e){
//			e.printStackTrace();
//		} finally{
//			try {
//				input.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return nRes;
//	}
	public static MusicInfoModel GetMusicInfoModelFromFile(File file){
		MusicInfoModel newInfo = null;
		FileInputStream mInput = null;
		try {
			newInfo = new MusicInfoModel();
			mInput = new FileInputStream( file );
			byte[] header = new byte[FileHeader.FILE_HEADER_SIZE];
			mInput.read(header, 0, FileHeader.FILE_HEADER_SIZE);

			FileHeader fileHeader = new FileHeader(header);
			
			newInfo.m_strName = file.getName();
			
			newInfo.m_sFileNum = fileHeader.m_sFileNum;
			newInfo.m_nIndex = fileHeader.m_nFileIndex;
			newInfo.m_nDownLoadOffset = fileHeader.m_nOffset;
			newInfo.m_nDuration = fileHeader.m_nDuration;
			newInfo.m_nDownLoadSpeed = 0;
			
			newInfo.m_nDownloadStatus = fileHeader.getFileStatus();
			newInfo.m_nDownPercent = fileHeader.getFileDownloadPercent();
			newInfo.m_nFileLength = fileHeader.m_nLength;
			if(fileHeader.m_nLength > fileHeader.m_nOffset)
				newInfo.m_isNeedContinue = true;
			
		} catch (FileNotFoundException e) {
			newInfo = null;
			e.printStackTrace();
		} catch(IOException e){
			newInfo = null;
			e.printStackTrace();
		}finally{
			try {
				mInput.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return newInfo;
	}
}
