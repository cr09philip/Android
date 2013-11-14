package cn.kc.demo.view;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.kc.demo.R;
import cn.kc.demo.adapter.ArrayListAdapter;
import cn.kc.demo.adapter.MusicAdapter;
import cn.kc.demo.audio.FilePlayer;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.net.socket.KcSocketServer;
import cn.kc.demo.utils.FileUtil;
import cn.kc.demo.utils.VolumeControl;

public class VoiceListActivity extends Activity implements OnSeekBarChangeListener, OnClickListener{
	public final static String SID = "sid";

	public final static int MSG_NEW_DOWNLOAD_STATUS = 0;
	public final static int MSG_DOWNLOAD_CHANGE_STATUS = 1;
	public final static int MSG_DOWNLOAD_OK_STATUS = 2;
	
	public final static int MSG_START_PLAYER_STATUS = 0;
	public static final int MSG_STATUS_CHANGE_STATUS = 1;
	public static final int MSG_START_PAUSE_STATUS = 2;
	public final static int MSG_PLAY_OVER_STATUS = 3;

	private ArrayListAdapter<MusicInfoModel> mMusicAdapter;
	public ArrayList<MusicInfoModel> mMusicInfoModels;
	
	public MusicInfoModel mCurPlayMusicInfo;
	private Button mBackButton;

	private TextView mSuccessProgressView;
	private TextView mDownLoadSpeedView;
	
	private ListView mVoiceListView;
	
	public LinearLayout mPlayInfoLayout;

	private Button mClosePlayInfoBtn;

	private TextView mPlayInfoTxt;
	private TextView mPlayCurrentTimeView;
	private TextView mPlayStateView;
	private TextView mPlayTimeView;
	
	private SeekBar mSeekBar;	
	
	private Button mPalyLastBtn;
	private Button mPlayAndPauseBtn;
	private Button mPlayNextBtn;
		
	public FilePlayer mPlayer;
	private int mPlayPosition = 0;
	public String mAppPath = null;
	
	public int mCurVoiceIndex;
	
	public DownloadInfoHandler mDownLoadHander;
	public class DownloadInfoHandler extends Handler {
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
            case MSG_NEW_DOWNLOAD_STATUS:
            	mMusicInfoModels.add(info);
            	break;
            case MSG_DOWNLOAD_CHANGE_STATUS:
            case MSG_DOWNLOAD_OK_STATUS:
            	MusicInfoModel item = getItemByName(info.m_strName);
            	item.m_nDownloadStatus = info.m_nDownloadStatus;
            	item.m_nDownPercent = info.m_nDownPercent;
            	item.m_nDownLoadOffset = info.m_nDownLoadOffset;
            	
            	item.m_nDownLoadSpeed = info.m_nDownLoadSpeed;
            	
            	RefreshStaticPlayInfo(item);
            	break;
            }
        	mMusicAdapter.setList(mMusicInfoModels);
        }	
        
        public MusicInfoModel getItemByName(String name){
        	for(MusicInfoModel info : mMusicInfoModels){
        		if(info.m_strName.equals(name)){
        			return info;
        		}
        	}
			return null;
        }
    }
	
	public class PlayInfoHandler extends Handler {
		public static final int PLAY_OVER_FLAG = -1;
		public static final int PLAY_READY_FLAG = 0;
        public PlayInfoHandler() {
        }
        
        public PlayInfoHandler(Looper L) {
            super(L);
        }
        
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if( msg.what == PLAY_OVER_FLAG){
            	
            }else
            	RefreshDymanicPlayInfo( msg.what, mCurPlayMusicInfo.m_nDuration);
        }	
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_layout);
		
		initView();
		initData();
		initEvent();

		readyToReceive();
		
		mPlayer = FilePlayer.instance(VoiceListActivity.this);
	}
	
	private void readyToReceive(){
        Thread desktopServerThread = new Thread(new KcSocketServer(VoiceListActivity.this, mAppPath));  
        desktopServerThread.start();  
	}
	
	private void initView() {
		mBackButton = (Button) this.findViewById(R.id.back_btn);

		mSuccessProgressView = (TextView) findViewById(R.id.success_progress_txt);
		mDownLoadSpeedView = (TextView) findViewById(R.id.download_speed_txt);
		
		mVoiceListView = (ListView) this.findViewById(R.id.music_list);
		
		mPlayInfoLayout = (LinearLayout) findViewById(R.id.play_info_layout);

		mClosePlayInfoBtn = (Button) findViewById(R.id.close_playinfo_btn);
		
		mPlayInfoTxt = (TextView) findViewById(R.id.play_info_txt);		
		
		mPlayCurrentTimeView = (TextView) findViewById(R.id.play_start_time_view);
		mPlayStateView = (TextView) findViewById(R.id.play_name_view);
		mPlayTimeView = (TextView) findViewById(R.id.play_end_time_view);

		mSeekBar = (SeekBar) findViewById(R.id.play_progress_view);
		mSeekBar.setEnabled(false);
		
		mPalyLastBtn = (Button) findViewById(R.id.play_last_btn);
		mPlayAndPauseBtn = (Button) findViewById(R.id.play_startorpause_view);
		mPlayNextBtn = (Button) findViewById(R.id.play_next_view);
	}

	private void initData() {
		mAppPath = FileUtil.getStoragePath(VoiceListActivity.this) + "/kc_demo";
		if( !FileUtil.IsFileExist(mAppPath) ){
			 FileUtil.CreatSDDir( mAppPath );
		}
		
		mMusicInfoModels = new ArrayList<MusicInfoModel>();
		GetFiles(mAppPath, null, false);

		mMusicAdapter = new MusicAdapter(this);
		mMusicAdapter.setList(mMusicInfoModels);
		mVoiceListView.setAdapter(mMusicAdapter);

		mDownLoadSpeedView.setText(String.format(getString(R.string.download_speed), 0));
		mSuccessProgressView.setText(String.format(getString(R.string.download_progress), 100));
	}

	private void initEvent() {
		mDownLoadHander = new DownloadInfoHandler();
		mBackButton.setOnClickListener(VoiceListActivity.this);

		mClosePlayInfoBtn.setOnClickListener(VoiceListActivity.this);
		mSeekBar.setOnSeekBarChangeListener(VoiceListActivity.this);
		mPalyLastBtn.setOnClickListener(VoiceListActivity.this);
		
		mPlayAndPauseBtn.setOnClickListener(VoiceListActivity.this);
		
		mPlayNextBtn.setOnClickListener(VoiceListActivity.this);
	}
	
	public void RefreshStaticPlayInfo(MusicInfoModel info){		
		mPlayInfoTxt.setText(info.m_strName);		
		
		if( mPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			mPlayStateView.setText(R.string.pause);
			mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_start_btn);
		} else { 
			mPlayStateView.setText(R.string.play);
			mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_pause_btn);
		}
		
		mPlayTimeView.setText(String.format(getString(R.string.play_time), info.m_nDuration/60,info.m_nDuration%60));
		
		RefreshDymanicPlayInfo(mSeekBar.getProgress(), info.m_nDuration);
		RefreshDownInfo(info.m_nDownPercent, info.m_nDownLoadSpeed);
	}
	
	public void RefreshDymanicPlayInfo(int curPos, int duration){
		mPlayCurrentTimeView.setText(String.format(getString(R.string.play_time), curPos/60, curPos%60));
		
		if( duration != 0)
			mSeekBar.setProgress(curPos*100/duration);
	}
	public void RefreshDownInfo(int percent, int speed){
		mSuccessProgressView.setText(String.format(getString(R.string.download_progress), percent));
		mDownLoadSpeedView.setText(String.format(getString(R.string.download_speed), speed));
	}
	
	private void GetFiles(String Path, String Extension, boolean IsIterative)  //搜索目录，扩展名，是否进入子文件夹
	{
    	int nIndex = mMusicInfoModels.size();
	    File[] files = new File(Path).listFiles();

        if(files == null)  
            return;
        
	    for (int i = 0; i < files.length; i++) {
	        File f = files[i];
	        if (f.isFile())
	        {
	        	if( Extension != null){
		            if ( !f.getName().contains(Extension) )  //判断扩展名
		                break;
	        	}
	        	
	        	mMusicInfoModels.add(new MusicInfoModel(nIndex, f.getName(),
	        			MusicInfoModel.getMusicFileDuration(f.getPath())));
	 
	        }
	        else if (f.isDirectory() ){  //忽略点文件（隐藏文件/文件夹）
	        	if(f.getPath().indexOf("/.") == -1)
	        		break;
	        	
	        	if(IsIterative)
	        		GetFiles(f.getPath(), Extension, IsIterative);
	        } 
	    }
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		VolumeControl volCtl = new VolumeControl(VoiceListActivity.this, AudioManager.STREAM_MUSIC);
		
		switch(keyCode){
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			volCtl.subVolume();
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			volCtl.addVolume();
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		
		return true;
	}

	
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
//		mPlayPosition = progress*info.m_nDuration/100;
//		mPlayCurrentTimeView.setText(String.format(getString(R.string.play_time), mPlayPosition/60, mPlayPosition%60));
		
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	public void onStopTrackingTouch(SeekBar seekBar) {	
		//set player to play the nCurPos
//		mPlayPosition = seekBar.getProgress()*info.m_nDuration/100;
//		mPlayer.seek(mPlayPosition);
		
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.back_btn:
			finish();
			break;
		case R.id.close_playinfo_btn:
			if( mPlayInfoLayout.getVisibility() != View.GONE){
				mPlayInfoLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.play_last_btn:
			Toast.makeText(VoiceListActivity.this, "上一曲", Toast.LENGTH_LONG).show();
			break;
		case R.id.play_startorpause_view:
			if( mPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
				mPlayStateView.setText(R.string.play);
				
				mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_pause_btn);					

				mPlayer.play();
			} else { 
				mPlayStateView.setText(R.string.pause);
				mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_start_btn);
				mPlayer.pause();
			}
			break;
		case R.id.play_next_view:
			Toast.makeText(VoiceListActivity.this, "下一曲", Toast.LENGTH_LONG).show();
			break;
		}
		
	}

}