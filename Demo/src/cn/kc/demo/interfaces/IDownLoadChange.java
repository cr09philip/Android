package cn.kc.demo.interfaces;

//下载状态改变，包括进度，下载开始，下载完成
public interface IDownLoadChange {
	//名称，长度，时间
	abstract void beginNewDownload();	
	
	//进度，速度
	abstract void changeDownLoadProgress(int currentProgress);
}
