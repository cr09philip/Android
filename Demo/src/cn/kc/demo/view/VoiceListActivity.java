package cn.kc.demo.view;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import cn.kc.demo.audio.BaseAudioPlayer;
import cn.kc.demo.audio.FilePlayer;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.net.socket.KcSocketServer;
import cn.kc.demo.utils.FileUtil;
import cn.kc.demo.utils.VolumeControl;

public class VoiceListActivity extends Activity 
							   implements BaseAudioPlayer.OnPlayStateChangedListener, 
							   KcSocketServer.OnDownLoadStateChangedListener,
							   OnSeekBarChangeListener, OnClickListener,
							   OnItemClickListener{
	public final static String SID = "sid";

	public final static int MSG_NEW_DOWNLOAD_STATUS = 0;
	public final static int MSG_DOWNLOAD_CHANGE_STATUS = 1;
	public final static int MSG_DOWNLOAD_OK_STATUS = 2;
	
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
	
	private int mSeekBarProgress = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_layout);
		
		initView();
		initData();
		initEvent();

		readyToReceive();
		
		mPlayer = FilePlayer.instance(VoiceListActivity.this);
		mPlayer.setOnPlayStateChangedListener(VoiceListActivity.this);
	}
	
	//开启监听线程
	private void readyToReceive(){
		KcSocketServer socket = new KcSocketServer(VoiceListActivity.this, mAppPath);
		socket.setOnDownLoadStateChangedListener(VoiceListActivity.this);
        Thread desktopServerThread = new Thread();  
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
		
		mDownLoadSpeedView.setText(String.format(getString(R.string.download_speed), 0));
		mSuccessProgressView.setText(String.format(getString(R.string.download_progress), 100));

		mMusicInfoModels = new ArrayList<MusicInfoModel>();
		GetFiles(mAppPath, null, false);

		mMusicAdapter = new MusicAdapter(this);
		mMusicAdapter.setList(mMusicInfoModels);
		mVoiceListView.setAdapter(mMusicAdapter);

		RefreshDownInfo(0, 0);
	}

	private void initEvent() {		
		mBackButton.setOnClickListener(VoiceListActivity.this);
		mClosePlayInfoBtn.setOnClickListener(VoiceListActivity.this);
		mSeekBar.setOnSeekBarChangeListener(VoiceListActivity.this);
		mPalyLastBtn.setOnClickListener(VoiceListActivity.this);
		
		mPlayAndPauseBtn.setOnClickListener(VoiceListActivity.this);
		
		mPlayNextBtn.setOnClickListener(VoiceListActivity.this);
	}
	
	public void RefreshAllPlayInfo(MusicInfoModel info){		
		mPlayInfoTxt.setText(info.m_strName);		
		
		if( mPlayer.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
			mPlayStateView.setText(R.string.pause);
			mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_start_btn);
		} else { 
			mPlayStateView.setText(R.string.play);
			mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_pause_btn);
		}
		
		mPlayTimeView.setText(String.format(getString(R.string.play_time), info.m_nDuration/60,info.m_nDuration%60));
		
		RefreshDymanicPlayInfo(mSeekBarProgress , info.m_nDuration);
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
    public MusicInfoModel getItemByName(String name){
    	for(MusicInfoModel info : mMusicInfoModels){
    		if(info.m_strName.equals(name)){
    			return info;
    		}
    	}
		return null;
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
		case R.id.music_name_txt:
//			Toast.makeText(VoiceListActivity.this, "music_name_txt", Toast.LENGTH_LONG).show();
			{
				if( mPlayInfoLayout.getVisibility() == View.GONE){
					mPlayInfoLayout.setVisibility(View.VISIBLE);
					
					if( mCurPlayMusicInfo != null ){
						MusicInfoModel thisInfo = (MusicInfoModel) v.getTag();
						
						if( ! mCurPlayMusicInfo.m_strName.equals(thisInfo.m_strName)){
							mPlayer.setFilePathAndInitPlayer(mAppPath + "/" + mCurPlayMusicInfo.m_strName);
						}
											
						RefreshAllPlayInfo(mCurPlayMusicInfo);
						
						mCurPlayMusicInfo = thisInfo;
					}else{
						mCurPlayMusicInfo = (MusicInfoModel) v.getTag();

						mPlayer.setFilePathAndInitPlayer(mAppPath + "/" + mCurPlayMusicInfo.m_strName);
						RefreshAllPlayInfo(mCurPlayMusicInfo);
					}
				}
			}
			break;
		default:
			break;
		}
	}

	public void onPlayStateChanged(int from, int to) {
		//control
		
	}

	public void onPlayReady() {
		mPlayStateView.setText(R.string.null_str);
		mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_start_btn);
		RefreshDymanicPlayInfo(0, mCurPlayMusicInfo.m_nDuration);
	}

	public void onPlayOver() {
		//1.单曲播放
		onPlayReady();
		mPlayer.setFilePathAndInitPlayer(mAppPath + "/" + mCurPlayMusicInfo.m_strName);
		
		//2.下一曲
//		mPlayStateView.setText(R.string.pause);
//		mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_start_btn);
//		mPlayer.setFilePathAndInitPlayer(nextPath);
//		RefreshDymanicPlayInfo(0, nextPlayMusicInfo.m_nDuration);
	}

	public void onPlayProgressing(int progress) {
		mSeekBarProgress = progress;
		RefreshDymanicPlayInfo(progress, mCurPlayMusicInfo.m_nDuration);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Toast.makeText(VoiceListActivity.this, "OnItemClick", Toast.LENGTH_LONG).show();		
	}

	public void onDownLoadBegin(MusicInfoModel info) {
    	mMusicInfoModels.add(info);
    	mMusicAdapter.setList(mMusicInfoModels);
	}

	public void onDownLoadEnd(MusicInfoModel info) {
		MusicInfoModel item = getItemByName(info.m_strName);
    	item.m_nDownloadStatus = info.m_nDownloadStatus;
    	item.m_nDownPercent = info.m_nDownPercent;
    	item.m_nDownLoadOffset = info.m_nDownLoadOffset;
    	
    	item.m_nDownLoadSpeed = info.m_nDownLoadSpeed;
    	
    	RefreshAllPlayInfo(item);
    	mMusicAdapter.setList(mMusicInfoModels);
	}

	public void onDownloadProgressing(MusicInfoModel info) {
		MusicInfoModel item = getItemByName(info.m_strName);
    	item.m_nDownloadStatus = info.m_nDownloadStatus;
    	item.m_nDownPercent = info.m_nDownPercent;
    	item.m_nDownLoadOffset = info.m_nDownLoadOffset;
    	
    	item.m_nDownLoadSpeed = info.m_nDownLoadSpeed;
    	
    	RefreshAllPlayInfo(item);
    	mMusicAdapter.setList(mMusicInfoModels);
	}

}