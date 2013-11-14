package cn.kc.demo.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MusicInfoModel {
	public int m_nIndex;
	public String m_strName;	//just name under apps path
	
	public int m_nDownloadStatus; // 0 未下载   1 下载中 此时 2下载完成  
	public int m_nDownPercent;
	public int m_nDownLoadOffset;
	
//	public int m_nPlayStatus; //1 未播放 2 播放中 3 放暂停 4停止 
	public int m_nDuration; //总时长
//	public int m_nCurPosition; //当前进度
	
	public int m_nDownLoadSpeed; // KBytes/s
	
	public MusicInfoModel(){
		m_strName = null;
		
		m_nIndex = 
		m_nDownloadStatus =
		m_nDownPercent = 
//		m_nPlayStatus = 
		m_nDuration = 
//		m_nCurPosition = 
		m_nDownLoadSpeed = 0;
	}
	
	public MusicInfoModel(int index, String path, int duration ){		
		m_nIndex = index;
		m_strName = path;
		m_nDownloadStatus = 0;
		m_nDownPercent = 0;
		
//		m_nPlayStatus = 0;
		m_nDuration = duration;
//		m_nCurPosition = 0;
		
		m_nDownLoadSpeed = 0;
	}
	
	public static int getMusicFileDuration(String fullPath){
		int nRes = 0;
		FileInputStream input = null;
		try {
			input = new FileInputStream( fullPath);
			byte[] header = new byte[FileHeader.FILE_HEADER_SIZE];
			input.read(header, 0, FileHeader.FILE_HEADER_SIZE);

			FileHeader mHeader = new FileHeader(header, 0);
			nRes = mHeader.m_nDuration;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch(IOException e){
			e.printStackTrace();
		} finally{
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return nRes;
	}
}
