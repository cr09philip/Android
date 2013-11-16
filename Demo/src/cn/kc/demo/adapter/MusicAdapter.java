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
			indexView.setText("" + position);
			
			TextView nameView = (TextView) convertView.findViewById(R.id.music_name_txt);
			nameView.setText(info.m_strName);
			
			Button downBtn = (Button) convertView.findViewById(R.id.music_static_btn);
			downBtn.setBackgroundResource(R.drawable.play_satrt_select);
			
			ProgressBar processBar = (ProgressBar)convertView.findViewById(R.id.download_progress_bar);
			processBar.setProgress(100);
			
			TextView statusView = (TextView)convertView.findViewById(R.id.receive_status_view);
			statusView.setText(R.string.receive_success);
			
			nameView.setOnClickListener(mContext);		
			nameView.setTag(info);
		}
		
		return convertView;
	}
}
