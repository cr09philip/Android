package cn.kc.demo.adapter;

import android.app.Activity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import cn.kc.demo.R;
import cn.kc.demo.audio.StreamPlayer;
import cn.kc.demo.audio.FilePlayer;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.view.VoiceListActivity;

public class MusicAdapter extends ArrayListAdapter<MusicInfoModel>{
	private VoiceListActivity mContext;
	private LinearLayout mPlayInfoLayout;
	
//	private FilePlayer mPlayer;
	private String mAppPath;
	
	public MusicAdapter(Activity context) {
		super(context);
		mContext = (VoiceListActivity) context;
		if(mContext != null){
			mPlayInfoLayout = mContext.mPlayInfoLayout;

//			mPlayer = mContext.mPlayer;
			
			mAppPath = mContext.mAppPath;
		}
		
	}

	public MusicAdapter() {
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MusicInfoModel info = mList.get(position);
//		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.voice_adapter_layout, null);
			TextView indexView = (TextView) convertView.findViewById(R.id.voice_index_btn);
			TextView nameView = (TextView) convertView.findViewById(R.id.music_name_txt);
			Button downBtn = (Button) convertView.findViewById(R.id.music_static_btn);
			ProgressBar processBar = (ProgressBar)convertView.findViewById(R.id.download_progress_bar);
			TextView statusView = (TextView)convertView.findViewById(R.id.receive_status_view);
//			statusView.setBackgroundResource(R.drawable.download_progress_success);
			downBtn.setBackgroundResource(R.drawable.play_satrt_select);
			
			indexView.setText("" + position);
			nameView.setText(info.m_strName);
			statusView.setText(R.string.receive_success);
			if( mPlayInfoLayout.getVisibility() != View.GONE){
				
			}
			nameView.setOnClickListener(new OnClickListener() {				
				public void onClick(View arg0) {		
					if(mPlayInfoLayout == null)
						return;
					
					if( mPlayInfoLayout.getVisibility() == View.GONE){
						mPlayInfoLayout.setVisibility(View.VISIBLE);
						
						mContext.RefreshStaticPlayInfo(info);
						//set playinfo settings
					}
					if( mContext.mPlayer.mFilePath == null || !mContext.mPlayer.mFilePath.contains(info.m_strName) )
						mContext.mPlayer.setFilePathAndInitPlayer(mAppPath + "/" + info.m_strName);
					
					mContext.mCurPlayMusicInfo = info;
				}
			});
//			holder = new ViewHolder();
//			convertView.setTag(holder);
			
		} else {
//			holder = (ViewHolder) convertView.getTag();
		}
		return convertView;
	}
	
	class ViewHolder {
		Button devicesStaticButton;
		TextView devicesLinkStaticTextView;
		Button devicesWifiButton;
		TextView devicesNameTextView;
	}
}
