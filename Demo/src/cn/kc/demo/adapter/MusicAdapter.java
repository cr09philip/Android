package cn.kc.demo.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.kc.demo.R;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.view.VoiceListActivity;

public class MusicAdapter extends ArrayListAdapter<MusicInfoModel>{
	private VoiceListActivity mContext;
	
	public MusicAdapter(Activity context) {
		super(context);
		mContext = (VoiceListActivity) context;
	}

	public MusicAdapter() {
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MusicInfoModel info = mList.get(position);
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.voice_adapter_layout, null);
			
			TextView indexView = (TextView) convertView.findViewById(R.id.voice_index_btn);
			indexView.setText("" + info.m_nIndex);
			
			TextView nameView = (TextView) convertView.findViewById(R.id.music_name_txt);
			nameView.setText(info.m_strName);
			
			Button downBtn = (Button) convertView.findViewById(R.id.music_static_btn);
			if( info.m_nDownloadStatus == MusicInfoModel.DOWNLOAD_STATUS_END){
				downBtn.setBackgroundResource(R.drawable.play_satrt_select);
			}else{
				downBtn.setBackgroundResource(R.drawable.download_btn);
			}
			
			ProgressBar processBar = (ProgressBar)convertView.findViewById(R.id.download_progress_bar);
			processBar.setProgress(info.m_nDownPercent);
			
			TextView statusView = (TextView)convertView.findViewById(R.id.receive_status_view);
			switch( info.m_nDownloadStatus){
			case MusicInfoModel.DOWNLOAD_STATUS_BEGIN:
			case MusicInfoModel.DOWNLOAD_STATUS_PROGRESSING:
				String format = mContext.getString(R.string.receive_progress);
				String str = String.format( format, info.m_nDownPercent);
				statusView.setText(str);
				break;
			case MusicInfoModel.DOWNLOAD_STATUS_END:
				statusView.setText(R.string.receive_success);
//				statusView.setBackground(background);
				break;
			}
			
			nameView.setOnClickListener(mContext);		
			nameView.setTag(info);
		}
		
		return convertView;
	}
}
