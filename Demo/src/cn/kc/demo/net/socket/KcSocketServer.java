package cn.kc.demo.net.socket;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Pair;
import android.widget.Toast;
import cn.kc.demo.R;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.net.socket.KcReceiveMsgThread.ReceiveInfo;
import cn.kc.demo.utils.FileUtil;
import cn.kc.demo.utils.Utils;
import cn.kc.demo.utils.WifiUtil;
import cn.kc.demo.view.VoiceListActivity;

// we need a socket server to receive msg.
public class KcSocketServer implements Runnable {
	public static final int SERVERPORT = 7788;
	public static final String RENAME_POSTFIX = ".tmp";
	private String mAppPath;
	private VoiceListActivity mContext;
	private OnDownLoadStateChangedListener mOnDownLoadStateChangedListener = null;
	private OnServerSetupListener mOnServerSetupListener = null;
//	private ArrayList<Thread> mListSocket;
//	private ArrayList<KcReceiveMsgThread> mListReceiverMsg;
	private ArrayList<ClientInfo> mListClientInfo = null;
	/**
	 * @return the mListClientInfo
	 */
	public ArrayList<ClientInfo> getListClientInfo() {
		return mListClientInfo;
	}

	ServerSocket mServerSocket;
	Handler mHandler;
	private String mAddr;
	private int mPort;
	private OnConnectStateChanged mOnConnectStateChanged;
	
	public class ClientInfo{
		public String ip;
		public String mac_ip;
		public Thread thread;
		public Socket socket;
		public boolean isConnecting;
	}
	public KcSocketServer(Context context, String path){
		mContext = (VoiceListActivity) context;
		mAppPath = path;
//		mListSocket = new ArrayList<Thread>();
//		mListReceiverMsg = new ArrayList<KcReceiveMsgThread>();
		mListClientInfo = new ArrayList<KcSocketServer.ClientInfo>();
		
		mHandler = new DownloadInfoHandler();
	}
	
	public void run() {
		try{
			System.out.println("S: Connecting...");  
		  
			mServerSocket = new ServerSocket(SERVERPORT); 	  
			
			{
//				InetAddress inetAddr = mServerSocket.getInetAddress();
				
//				InetSocketAddress socketAddr = (InetSocketAddress) mServerSocket.getLocalSocketAddress(); 
//				byte[] addr = socketAddr.getAddress().getAddress();
				
//				WifiAdmin wifiAdmin = new WifiAdmin(mContext);
//				
//				mAddr =  CodeUtil.int2bytes(wifiAdmin.getIPAddress(), false);
				mAddr = WifiUtil.getHostAddress();
				mPort = mServerSocket.getLocalPort();
				mHandler.sendEmptyMessage(DownloadInfoHandler.GET_HOST_IP_ADDRESS_INFO);
			}
		    while (true) {  
		        // 等待接受客户端请求   
		    	Socket client = mServerSocket.accept();  
		    	if( client != null && client.isBound()){
					if( !client.isClosed()){
						Thread thread = new Thread(new KcReceiveMsgThread(mContext, KcSocketServer.this, client, mAppPath));
 
				    	ClientInfo info = new ClientInfo();
				    	info.ip = client.getInetAddress().toString();
				    	
				    	info.mac_ip = Utils.getMacAddressIP(info.ip);
				    	info.socket = client;
				    	info.thread = thread;
				    	info.isConnecting = false;
				    	
				    	thread.start();
						Message msg = new Message();
						msg.what = DownloadInfoHandler.SOCKET_INIT;
						msg.obj = info;
						mHandler.sendMessage(msg);
					}
				}
	    	}
		}catch(Exception e){
			e.printStackTrace();  
		}
	}
	public interface OnServerSetupListener{
		void onReturnServerAddress(String addr, int port);
	}
	public interface OnDownLoadStateChangedListener{
		void onDownLoadBegin(ReceiveInfo info);
		void onDownLoadEnd(ReceiveInfo info);
		void onDownloadProgressing(ReceiveInfo info);
		void onDownloadError(ReceiveInfo info);
	}
	public interface OnConnectStateChanged{
		void onSocketConnect();
		void onSocketDisConnect();
	}
	public void setOnServerSetupListener(OnServerSetupListener l){
		mOnServerSetupListener = l;
	}
	public void setOnDownLoadStateChangedListener(OnDownLoadStateChangedListener l){
		mOnDownLoadStateChangedListener  = l;
	}
	public void setOnConnectStateChanged(OnConnectStateChanged l){
		mOnConnectStateChanged  = l;
	}
	
	public class DownloadInfoHandler extends Handler{
		public static final int DOWNLOAD_BEGIN_FLAG = 0;
		public static final int DOWNLOAD_END_FLAG = 1;		
		public final static int DOWNLOAD_PROGRESSING_FLAG = 2;
		public final static int DOWNLOAD_ERROR_FLAG = 3;
		public final static int GET_HOST_IP_ADDRESS_INFO = 4;
		public final static int SOCKET_INIT = 5;
		public final static int SOCKET_DESTROY = 6;
		public final static int SOCKET_RENAME_FILE = 7;

		public final static int SOCKET_CONNECT = 8;
		public final static int SOCKET_DISCONNECT = 9;
		
		public final static int SOCKET_IS_NEED_DELETE_SEND_FILES = 10;

        public DownloadInfoHandler() {
        }

		public DownloadInfoHandler(Looper L) {
            super(L);
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
            case DOWNLOAD_BEGIN_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownLoadBegin((ReceiveInfo) msg.obj);
            	break;
            case DOWNLOAD_END_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownLoadEnd((ReceiveInfo) msg.obj);
            	break;
            case DOWNLOAD_PROGRESSING_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownloadProgressing((ReceiveInfo) msg.obj);
            	break;
            case DOWNLOAD_ERROR_FLAG:
            	if( mOnDownLoadStateChangedListener != null)
            		mOnDownLoadStateChangedListener.onDownloadError((ReceiveInfo) msg.obj);
            	break;
            case GET_HOST_IP_ADDRESS_INFO:
            	if( mOnServerSetupListener != null)
            		mOnServerSetupListener.onReturnServerAddress(mAddr, mPort);
            	break;
            	
            case SOCKET_INIT:
		    	mListClientInfo.add((ClientInfo) msg.obj);
            	break;
            case SOCKET_DESTROY:
            	if(mOnConnectStateChanged != null)
            		mOnConnectStateChanged.onSocketDisConnect();
            	
            	Toast.makeText(mContext, "连接中断", Toast.LENGTH_LONG).show();
            	
            	mListClientInfo.remove(getClientInfoBySocket((Socket) msg.obj));
            	break;
            case SOCKET_CONNECT:
            	if(mOnConnectStateChanged != null)
            		mOnConnectStateChanged.onSocketConnect();
            	
            	getClientInfoBySocket((Socket) msg.obj).isConnecting = true;
            	break;
            case SOCKET_DISCONNECT:
            	if(mOnConnectStateChanged != null)
            		mOnConnectStateChanged.onSocketDisConnect();
            	
            	getClientInfoBySocket((Socket) msg.obj).isConnecting = false;
            	break;
            case SOCKET_RENAME_FILE:
	            {
	            	final Pair<KcReceiveMsgThread, String> info = (Pair<KcReceiveMsgThread, String>) msg.obj;
	            	String title = String.format(mContext.getResources().getString(R.string.alert_dialog_recover_title), info.second);
	            	new AlertDialog.Builder(mContext)
	                .setIconAttribute(android.R.attr.alertDialogIcon)
	                .setTitle(title)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        /* User clicked OK so do some stuff */
	                    	//delete file 
	                    	File f = new File(info.first.getAppPath() + "/" + info.second);
	                    	f.delete();
	                    	
	                    	//delete item in the list
	                    	MusicInfoModel music = mContext.getMusicInfoModelByName(info.second);
	                    	mContext.mListMusicInfoModels.remove(music);
	                    	
	                    	//refresh the listview
	                    	mContext.refresh();
	                    	
	                    	//set value to reload this
	                    	info.first.mIsNeedRename = new Pair<String, Boolean>(info.second, false);
	                    	
	                    	info.first.mIsPause = false;
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        /* User clicked Cancel so do some stuff */
	                    	//set value to ignor this
	                    	info.first.mIsNeedRename = new Pair<String, Boolean>(info.second, true);
	                    	
	                    	info.first.mIsPause = false;
	                    }
	                })
	                .create().show();
	            }
            	break;
            case SOCKET_IS_NEED_DELETE_SEND_FILES:
	            {
	            	final KcReceiveMsgThread threadObj = (KcReceiveMsgThread) msg.obj;
	            	String title = mContext.getResources().getString(R.string.alert_dialog_delete_send_files);
	            	new AlertDialog.Builder(mContext)
	                .setIconAttribute(android.R.attr.alertDialogIcon)
	                .setTitle(title)
	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        /* User clicked OK so do some stuff */
	                    	String title = mContext.getResources().getString(R.string.alert_dialog_sure_delete_send_files);
	    	            	new AlertDialog.Builder(mContext)
	    	                .setIconAttribute(android.R.attr.alertDialogIcon)
	    	                .setTitle(title)
	    	                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
	    	                    public void onClick(DialogInterface dialog, int whichButton) {
	    	                        /* User clicked OK so do some stuff */
	    	                    	//delete file 
	    	                    	threadObj.mIsDeleteSendFile = true;
	    	                    	
	    	                    	threadObj.mIsPause = false;
	    	                    }
	    	                })
	    	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	    	                    public void onClick(DialogInterface dialog, int whichButton) {
	    	                        /* User clicked Cancel so do some stuff */
	    	                    	threadObj.mIsDeleteSendFile = false;
	    	                    	
	    	                    	threadObj.mIsPause = false;
	    	                    }
	    	                })
	    	                .create().show();
	                    }
	                })
	                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
	                    public void onClick(DialogInterface dialog, int whichButton) {
	                        /* User clicked Cancel so do some stuff */
	                    	threadObj.mIsDeleteSendFile = false;
	                    	
	                    	threadObj.mIsPause = false;
	                    }
	                })
	                .create().show();
	            }
            	break;
            }
        }
	}

	private ClientInfo getClientInfoBySocket(Socket socket){
		ClientInfo ret = null;
		if(mListClientInfo != null){
			for (ClientInfo it : mListClientInfo) {
				if(it.socket.equals(socket)){
					ret = it;
					break;
				}
			}
		}
		return ret;
	}
	public void recycle() {
		try {
			mServerSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(mListClientInfo != null){
			for(ClientInfo info : mListClientInfo){
				if(info.thread != null){
					info.thread.interrupt();
				}
			}
		}
		
		mListClientInfo.clear();
//		if( mListSocket != null){
//			for( Thread thread : mListSocket){
//				if(thread != null){
//					thread.interrupt();
//				}
//			}
//		}
//		
//		if( mListReceiverMsg != null){
//			for( KcReceiveMsgThread receive : mListReceiverMsg){
//				if( receive != null){
//					receive.recycle();
//				}
//			}
//		}
	}
	

}