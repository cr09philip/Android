package cn.kc.demo.net.socket;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.net.socket.KcSocketServer.OnDownLoadStateChangedListener;
import cn.kc.demo.view.VoiceListActivity;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

// we need a socket server to receive msg.
public class KcSocketServer implements Runnable {
	public static final int SERVERPORT = 53113;
	private String mAppPath;
	private VoiceListActivity mContext;
	private OnDownLoadStateChangedListener mOnDownLoadStateChangedListener = null;
	private ArrayList<Thread> mSocketList;

	Handler mHandler;
	
	public KcSocketServer(Context context, String path){
		mContext = (VoiceListActivity) context;
		mAppPath = path;
		mSocketList = new ArrayList<Thread>();
		mHandler = new DownloadInfoHandler();
	}
	
	public void run() {
		try{
			System.out.println("S: Connecting...");  
		  
			ServerSocket serverSocket = new ServerSocket(SERVERPORT); 	           
		    while (true) {  
		        // 等待接受客户端请求   
		    	Socket client = serverSocket.accept();  
  
		    	System.out.println("S: Receiving...");
		    	
		    	new Thread(new KcReceiveMsgThread(mContext, KcSocketServer.this, client, mAppPath)).start();
		    	}
		}catch(Exception e){
			e.printStackTrace();  
		}
	}
	
	public interface OnDownLoadStateChangedListener{
		void onDownLoadBegin(MusicInfoModel info);
		void onDownLoadEnd(MusicInfoModel info);
		void onDownloadProgressing(MusicInfoModel info);
	}
	
	public void setOnDownLoadStateChangedListener(OnDownLoadStateChangedListener l){
		mOnDownLoadStateChangedListener  = l;
	}
	
	public class DownloadInfoHandler extends Handler{
		public static final int DOWNLOAD_BEGIN_FLAG = 0;
		public static final int DOWNLOAD_END_FLAG = 1;		
		public final static int DOWNLOAD_PROGRESSING_FLAG = 2;

        public DownloadInfoHandler() {
        }

		public DownloadInfoHandler(Looper L) {
            super(L);
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MusicInfoModel info = (MusicInfoModel) msg.obj;
            switch(msg.what){
            case DOWNLOAD_BEGIN_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownLoadBegin(info);
            	break;
            case DOWNLOAD_END_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownLoadEnd(info);
            	break;
            case DOWNLOAD_PROGRESSING_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownloadProgressing(info);
            	break;
            }
        }
	}
}