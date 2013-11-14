package cn.kc.demo.net.socket;

import java.net.ServerSocket;
import java.net.Socket;

import cn.kc.demo.view.VoiceListActivity;

import android.content.Context;

// we need a socket server to receive msg.
public class KcSocketServer implements Runnable {
	public static final int SERVERPORT = 53113;
	private String mAppPath;
	private VoiceListActivity mContext;
	
	public KcSocketServer(Context context, String path){
		mContext = (VoiceListActivity) context;
		mAppPath = path;
	}
	public void run() {
		try{
			System.out.println("S: Connecting...");  
		  
			ServerSocket serverSocket = new ServerSocket(SERVERPORT); 	           
		    while (true) {  
		        // 等待接受客户端请求   
		    	Socket client = serverSocket.accept();  
  
		    	System.out.println("S: Receiving...");
		    	
		    	new Thread(new KcReceiveMsgThread(mContext, client, mAppPath)).start();
		    	}
		}catch(Exception e){
			e.printStackTrace();  
		}
	}

}