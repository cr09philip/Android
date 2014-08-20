package cn.kc.demo.view;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import cn.kc.demo.R;
import cn.kc.demo.SettingsSp;
import cn.kc.demo.adapter.MusicAdapter;
import cn.kc.demo.adapter.MusicAdapter.ViewHolder;
import cn.kc.demo.audio.AudioPlayer;
import cn.kc.demo.model.FileHeader;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.net.socket.KcReceiveMsgThread.ReceiveInfo;
import cn.kc.demo.net.socket.KcSocketServer;
import cn.kc.demo.utils.VolumeControl;

public class VoiceListActivity extends Activity 
							   implements AudioPlayer.OnPlayStateChangedListener, //AdpcmAudioPlayer.OnPlayStateChangedListener, //
							   KcSocketServer.OnDownLoadStateChangedListener,
							   KcSocketServer.OnServerSetupListener,
							   OnSeekBarChangeListener, OnClickListener,
							   KcSocketServer.OnConnectStateChanged,
							   OnItemClickListener{
	private static final String TAG = "VoiceListActivity";
	protected static final String SID = "sid";
//	public static final String FOLDER_NAME = "kc_demo";
	public static final String SETTINGS_PARAMS = "Settings";
	private static final String SP_LAST_FILE_NUM = "kc_last_file_num";
	private static final String CONFIG_FILE_NAME = "kc_config_file";
	protected static final String PATH = "folder_path";
	public static final String FOLDER_NAME = "minor_folder_name";
	
	private String mSid;
	
	private MusicAdapter mMusicAdapter;
	public ArrayList<MusicInfoModel> mListMusicInfoModels;
	
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
		
	AudioPlayer mPlayer;
//	AdpcmAudioPlayer mPlayer;
	public String mFolderPath = null;
	
	public int mCurVoiceIndex;

	private Thread mSocketThread;
	private KcSocketServer mSocketServer;
	private Button mSettingBtn;
	private long mBeginTime;
//	private Thread mMonitorThread;
//	public Handler mHandler = new Handler(){
//
//		@Override
//		public void handleMessage(Message msg) {
//			Socket socket = (Socket) msg.obj;
//			switch(msg.what){
//			case 0://unconnect
//				mConnectStateView.setImageResource(R.drawable.device_unconnect);
//				break;
//			case 1://connect
//				mConnectStateView.setImageResource(R.drawable.device_connect);
//				break;
//			}
//			super.handleMessage(msg);
//		}
//		
//	};
	private ImageView mConnectStateView;
	private int mLastFileNum;
	private SharedPreferences mSharedPerferences;
	private TextView mFolderNameTextView;
	private String mFolderName;
	private boolean mIsReceving;
	private long mLastRefreshTime;
	private int mLastStateBeforeTracking;
	
	public SettingsSp getSettingsDetails() {
		return SettingsSp.Instance().init(this);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG,"onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_layout);
		
		mSid = this.getIntent().getStringExtra(VoiceListActivity.SID);
		mFolderPath = this.getIntent().getStringExtra(VoiceListActivity.PATH);
		mFolderName = this.getIntent().getStringExtra(VoiceListActivity.FOLDER_NAME);
		
		initView();
		initData();
		initEvent();

		readyToReceive();
		
		mPlayer = AudioPlayer.instance(VoiceListActivity.this);
//		mPlayer = AdpcmAudioPlayer.instance(VoiceListActivity.this);
		
		mPlayer.setOnPlayStateChangedListener(VoiceListActivity.this);
	}
	//开启监听线程
	private void readyToReceive(){
		mSocketServer = new KcSocketServer(VoiceListActivity.this, mFolderPath);
		mSocketServer.setOnDownLoadStateChangedListener(VoiceListActivity.this);
        mSocketServer.setOnServerSetupListener(VoiceListActivity.this);
        mSocketServer.setOnConnectStateChanged(VoiceListActivity.this);
		
		mSocketThread = new Thread(mSocketServer);
        mSocketThread.start();  
	}
	
	private void initView() {
		mBackButton = (Button) this.findViewById(R.id.back_btn);
		mFolderNameTextView = (TextView) findViewById(R.id.voice_list);
		mConnectStateView = (ImageView) this.findViewById(R.id.connect_state);

		mSuccessProgressView = (TextView) findViewById(R.id.success_progress_txt);
		mDownLoadSpeedView = (TextView) findViewById(R.id.download_speed_txt);
		
		mVoiceListView = (ListView) this.findViewById(R.id.music_list);
		
		mPlayInfoLayout = (LinearLayout) findViewById(R.id.playinfo_view);
//		mPlayInfoLayout = (LinearLayout) findViewById(R.id.play_info_layout);

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
		
		mSettingBtn = (Button) findViewById(R.id.settings);
	}

	private void initData() {		
//		mAppPath = FileUtil.getStoragePath(VoiceListActivity.this) + "/" +  FOLDER_NAME ;
//		if( !FileUtil.IsFileExist(mAppPath) ){
//			 FileUtil.CreatSDDir( mAppPath );
//		}
		String str = String.format(getResources().getString(R.string.voice_list), mFolderName);
		mFolderNameTextView.setText(str);
		mSharedPerferences = VoiceListActivity.this.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
		
		mLastFileNum = mSharedPerferences.getInt(SP_LAST_FILE_NUM, 0);

		mListMusicInfoModels = new ArrayList<MusicInfoModel>();
		GetFiles(mFolderPath, null, false);

		mMusicAdapter = new MusicAdapter(this);
		mMusicAdapter.setList(mListMusicInfoModels);
		mVoiceListView.setAdapter(mMusicAdapter);

		RefreshDownInfo(0, 0, 0, 0);
	}

	private void initEvent() {		
		mBackButton.setOnClickListener(VoiceListActivity.this);
		mClosePlayInfoBtn.setOnClickListener(VoiceListActivity.this);
		mSeekBar.setOnSeekBarChangeListener(VoiceListActivity.this);
		mPalyLastBtn.setOnClickListener(VoiceListActivity.this);
		
		mPlayAndPauseBtn.setOnClickListener(VoiceListActivity.this);
		
		mPlayNextBtn.setOnClickListener(VoiceListActivity.this);
		
		mSettingBtn.setOnClickListener(VoiceListActivity.this);
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
		
		RefreshDymanicPlayInfo(info.m_nCurProgress , info.m_nDuration);
	}
	
	public void RefreshDymanicPlayInfo(int curPos, int duration){
		mPlayCurrentTimeView.setText(String.format(getString(R.string.play_time), curPos/60, curPos%60));
		
		if( duration != 0)
			mSeekBar.setProgress(curPos*100/duration);
	}
	public void RefreshDownInfo(int percent, float speed, int index, int nums){
		mLastRefreshTime = System.currentTimeMillis();
		System.nanoTime();
		
		mLastFileNum = nums;
		mSharedPerferences.edit().putInt(SP_LAST_FILE_NUM, mLastFileNum);
		
		String str = String.format(getString(R.string.download_total_nums), nums);

		mSuccessProgressView.setText(str);
		mDownLoadSpeedView.setText(String.format(getString(R.string.download_speed), speed));
	}
    public MusicInfoModel getItemByName(String name){
    	for(MusicInfoModel info : mListMusicInfoModels){
    		if(info.m_strName.equals(name)){
    			return info;
    		}
    	}
		return null;
    }
	private void GetFiles(String Path, String Extension, boolean IsIterative)  //搜索目录，扩展名，是否进入子文件夹
	{
//    	int nIndex = mListMusicInfoModels.size();
	    File[] files = new File(Path).listFiles();

        if(files == null)  
            return;
        
	    for (int i = 0; i < files.length; i++) {
	        File f = files[i];
	        if (f.isFile() )
	        {
	        	if( Extension != null){
		            if ( !f.getName().contains(Extension) )  //判断扩展名
		                continue;
	        	}
	        	
	        	MusicInfoModel newInfo = MusicInfoModel.GetMusicInfoModelFromFile(f);
	        	newInfo.m_nDownloadStatus = MusicInfoModel.DOWNLOAD_STATUS_LOCAL;
	        	mListMusicInfoModels.add(newInfo);
	        }
	        else if (f.isDirectory() ){  //忽略点文件（隐藏文件/文件夹）
	        	if(f.getPath().indexOf("/.") == -1)
	        		continue;
	        	
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
		case KeyEvent.KEYCODE_BACK:
			if(isCanBack()){
				release();
				finish();
			}
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		
		return true;
	}
	
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if(mCurPlayMusicInfo != null && mPlayer.isInited() && mPlayer.mDecoderBlockSize != 0){
			int playPosition = progress * mCurPlayMusicInfo.m_nDuration/100;
			mPlayCurrentTimeView.setText(String.format(getString(R.string.play_time), playPosition/60, playPosition%60));
		}
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		mLastStateBeforeTracking = mPlayer.getPlayState();
		if( mLastStateBeforeTracking == AudioTrack.PLAYSTATE_PLAYING )
			mPlayer.pause();
	}

	public void onStopTrackingTouch(SeekBar seekBar) {	
		//set player to play the nCurPos
		if(mCurPlayMusicInfo != null && mPlayer.isInited() && mPlayer.mDecoderBlockSize != 0){
			long playOffset = seekBar.getProgress()*mCurPlayMusicInfo.m_nFileLength/100;
			
			playOffset = playOffset - playOffset % mPlayer.mDecoderBlockSize;
	
			mPlayer.setPlayOffset(playOffset + FileHeader.FILE_HEADER_SIZE);
			if(mLastStateBeforeTracking == AudioTrack.PLAYSTATE_PLAYING)
				mPlayer.play();
		}
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.back_btn:
		{
			if(isCanBack()){
				release();
				finish();
			}
			break;
		}
		case R.id.close_playinfo_btn:
			if( mPlayInfoLayout.getVisibility() != View.GONE){
				mPlayInfoLayout.setVisibility(View.GONE);
			}
			break;
		case R.id.play_last_btn:
//			Toast.makeText(VoiceListActivity.this, "上一曲", Toast.LENGTH_LONG).show();
			if(mCurPlayMusicInfo != null && mListMusicInfoModels != null){
				int pos = mListMusicInfoModels.indexOf(mCurPlayMusicInfo);
				playMusicAtIndexOfList( pos -1);
			}else{
				Toast.makeText(VoiceListActivity.this, getResources().getString(R.string.select_nothing), Toast.LENGTH_LONG).show();
			}

			break;
		case R.id.play_startorpause_view:
			if(mPlayer == null || !mPlayer.isInited()){
				Toast.makeText(VoiceListActivity.this, getResources().getString(R.string.select_nothing), Toast.LENGTH_LONG).show();
				break;
			}
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
//			Toast.makeText(VoiceListActivity.this, "下一曲", Toast.LENGTH_LONG).show();
			
			if(mCurPlayMusicInfo != null && mListMusicInfoModels != null){
				int pos = mListMusicInfoModels.indexOf(mCurPlayMusicInfo);
				playMusicAtIndexOfList( pos + 1);
			}else{
				Toast.makeText(VoiceListActivity.this, getResources().getString(R.string.select_nothing), Toast.LENGTH_LONG).show();
			}
			
			break;
//		case R.id.music_name_txt:
//			Toast.makeText(VoiceListActivity.this, "music_name_txt", Toast.LENGTH_LONG).show();
		case R.id.list_item_layout:
			{
				if( mPlayInfoLayout.getVisibility() == View.GONE){
					mPlayInfoLayout.setVisibility(View.VISIBLE);
				}

				playMusicAtIndexOfList(((ViewHolder) v.getTag()).position );
			}
			break;
		case R.id.settings:
			Intent in = new Intent(VoiceListActivity.this, SettingActivity.class);
			startActivity(in);
//			in.putExtra(SETTINGS_PARAMS, mSettingsDetails);
//			startActivityForResult(in, SETTINGS_REQUEST_CODE);
			break;
		default:
			break;
		}
	}

	private void playMusicAtIndexOfList(int position) {
		if(position < 0 || position > mListMusicInfoModels.size()-1){
			return;
		}
			
		if( mCurPlayMusicInfo == null ){
			mCurPlayMusicInfo = mListMusicInfoModels.get(position);
		}else{				
			MusicInfoModel thisInfo = mListMusicInfoModels.get(position);
			
			if( mCurPlayMusicInfo.m_strName.equals(thisInfo.m_strName)){
				if( mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
					return;
				}
			}else{
				if( mPlayer.getPlayState() != AudioTrack.PLAYSTATE_STOPPED){
					mPlayer.stop();
					thisInfo.m_nCurProgress = 0;
				}
				
				mCurPlayMusicInfo = thisInfo;
			}
		}

		if(mPlayer.init(mFolderPath + "/" + mCurPlayMusicInfo.m_strName)){
			RefreshAllPlayInfo(mCurPlayMusicInfo);
			mSeekBar.setEnabled(true);
		}
		else{
			Toast.makeText(VoiceListActivity.this, "文件不存在", Toast.LENGTH_LONG).show();	
		}
		
		mPlayStateView.setText(R.string.play);
		mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_pause_btn);					

		mPlayer.play();
	}
	private boolean isCanBack() {
		if( mPlayer.getPlayState() == AudioTrack.PLAYSTATE_PLAYING || mIsReceving )
			return false;
		
		return true;
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
//		onPlayReady();
//		if(!mPlayer.init(mFolderPath + "/" + mCurPlayMusicInfo.m_strName))
//			Toast.makeText(VoiceListActivity.this, "文件不存在", Toast.LENGTH_LONG).show();	
		
		//2.下一曲
		if(mCurPlayMusicInfo != null && mListMusicInfoModels != null){
			int pos = mListMusicInfoModels.indexOf(mCurPlayMusicInfo);
			
			if((pos + 1) < 0 || (pos + 1) > mListMusicInfoModels.size()-1){
				mPlayer.init(mFolderPath + "/" + mCurPlayMusicInfo.m_strName);
			}else{
				playMusicAtIndexOfList( pos + 1);
			}
		}
	}

	public void onPlayProgressing(int progress) {
		mCurPlayMusicInfo.m_nCurProgress = progress;
		RefreshDymanicPlayInfo(progress, mCurPlayMusicInfo.m_nDuration);
	}

	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		//Toast.makeText(VoiceListActivity.this, "OnItemClick", Toast.LENGTH_LONG).show();		
	}

	public void onDownLoadBegin(ReceiveInfo obj) {
		mIsReceving = true;
		mBeginTime = System.nanoTime();

		//		info.m_sIndex = getListMusicInfoSize();
		if( !mListMusicInfoModels.contains(obj.info)){
			mListMusicInfoModels.add(obj.info);
    		mMusicAdapter.setList(mListMusicInfoModels);
		}
    	if(mCurPlayMusicInfo == null){
    		mCurPlayMusicInfo = obj.info;
    	}
    	else if(obj.info.equals(mCurPlayMusicInfo)){
    		RefreshAllPlayInfo(obj.info);
    	}
    	
		RefreshDownInfo(obj.info.m_nDownPercent, obj.info.m_nDownLoadSpeed, obj.info.m_nIndex, obj.total);
	}

	public void onDownLoadEnd(ReceiveInfo obj) {
		mIsReceving = false;

		MusicInfoModel item = getItemByName(obj.info.m_strName);
    	item.m_nDownloadStatus = obj.info.m_nDownloadStatus;
    	item.m_nDownPercent = obj.info.m_nDownPercent;
    	item.m_nDownLoadOffset = obj.info.m_nDownLoadOffset;
    	
    	item.m_nDownLoadSpeed = obj.info.m_nDownLoadSpeed;

    	if(item.equals(mCurPlayMusicInfo))
    		RefreshAllPlayInfo(item);
    	mMusicAdapter.notifyDataSetChanged();

    	mBeginTime = 0;
    	RefreshDownInfo(obj.info.m_nDownPercent, obj.info.m_nDownLoadSpeed, obj.info.m_nIndex, obj.total);
    	
//    	mMusicAdapter.setList(mListMusicInfoModels);
	}

	public void onDownloadProgressing(ReceiveInfo obj) {
//		mIsReceving = true;
		MusicInfoModel item = getItemByName(obj.info.m_strName);
    	item.m_nDownloadStatus = obj.info.m_nDownloadStatus;
    	item.m_nDownPercent = obj.info.m_nDownPercent;
    	item.m_nDownLoadOffset = obj.info.m_nDownLoadOffset;    	
    	item.m_nDownLoadSpeed = obj.info.m_nDownLoadSpeed;

    	long mill = System.currentTimeMillis() - mLastRefreshTime;
    	if(mill > 1000){
        	mMusicAdapter.notifyDataSetChanged();
        	if(item.equals(mCurPlayMusicInfo))
        		RefreshAllPlayInfo(item);
        	
        	obj.info.m_nDownLoadSpeed = (int) (((float)(item.mTotalBytes/item.mStartNanoSecs))/1024);
        	
        	Log.d(TAG, "size: " + item.mTotalBytes + "sec:" + System.nanoTime() + "start:"
        			+ item.mStartNanoSecs);
        	float fSpeed = item.mTotalBytes*1000000000/(System.nanoTime() - item.mStartNanoSecs);
        	fSpeed /= (1024*128);
//        	float fSpeed = (float) (9.0f + Math.random());
        	RefreshDownInfo(obj.info.m_nDownPercent, fSpeed, obj.info.m_nIndex, obj.total);
    	}
    	
//    	mMusicAdapter.setList(mListMusicInfoModels);
	}
	public void onDownloadError(ReceiveInfo obj) {
		mIsReceving = false;
		MusicInfoModel item = getItemByName(obj.info.m_strName);
		if(item == null){
			return;
		}

    	item.m_nDownloadStatus = obj.info.m_nDownloadStatus;
    	item.m_nDownPercent = obj.info.m_nDownPercent;
    	
    	item.m_nDownLoadOffset = obj.info.m_nDownLoadOffset;
    	item.m_nDownLoadSpeed = obj.info.m_nDownLoadSpeed;
    	
    	if(item.equals(mCurPlayMusicInfo))
    		RefreshAllPlayInfo(item);
    	
    	mMusicAdapter.notifyDataSetChanged();
    	RefreshDownInfo(obj.info.m_nDownPercent, obj.info.m_nDownLoadSpeed, obj.info.m_nIndex, obj.total);
	}
	
	public int getListMusicInfoSize(){
		return mListMusicInfoModels.size();
	}
	@Override
	protected void onRestart() {
		Log.d(TAG,"onRestart");
		super.onRestart();
	}
	@Override
	protected void onStart() {
		Log.d(TAG,"onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.d(TAG,"onResume");
//		if( mPlayer != null && mPlayer.mAudioTrack != null && mPlayer.mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED)
//			mPlayer.play();
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.d(TAG,"onPause");
//		if( mPlayer != null && mPlayer.mAudioTrack != null && mPlayer.mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
//			mPlayStateView.setText(R.string.pause);
//			mPlayAndPauseBtn.setBackgroundResource(R.drawable.play_start_btn);
//			mPlayer.pause();
//		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		Log.d(TAG,"onStop");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG,"onDestroy");
		release();
		super.onDestroy();
	}

	public void release(){
		mPlayer.recycle();
		if( mSocketThread != null )
			mSocketThread.interrupt();
		if( mSocketServer != null )
			mSocketServer.recycle();
	}

	public void onReturnServerAddress(String addr, int port) {
		Toast.makeText(VoiceListActivity.this, addr + " : " + port, Toast.LENGTH_LONG).show();
	}
	public void onSocketConnect() {
		mConnectStateView.setImageResource(R.drawable.device_connect);
	}
	public void onSocketDisConnect() {
		mConnectStateView.setImageResource(R.drawable.device_unconnect);
		mIsReceving = false;
	}
	public MusicInfoModel getMusicInfoModelByName(String name){
		if(mListMusicInfoModels != null){
			for(MusicInfoModel info : mListMusicInfoModels){
				if(info.m_strName.hashCode() == name.hashCode()){
					return info;
				}
			}
		}
		
		return null;
	}
	public void refresh(){
		if(mMusicAdapter != null)
			mMusicAdapter.notifyDataSetChanged();
	}
}