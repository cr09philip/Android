package cn.kc.demo.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.kc.demo.R;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.view.VoiceListActivity;

public class MusicAdapter extends ArrayListAdapter<MusicInfoModel>{
	private VoiceListActivity mContext;
	
	public class ViewHolder{
		TextView indexView;
		TextView nameView;
		Button downBtn;
		ProgressBar processBar;
		TextView statusView;
	}
	public MusicAdapter(Activity context) {
		super(context);
		mContext = (VoiceListActivity) context;
	}

	public MusicAdapter() {
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final MusicInfoModel info = mList.get(position);
		ViewHolder holder = null;
		
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.voice_adapter_layout, null);

			holder = new ViewHolder();
			holder.indexView = (TextView) convertView.findViewById(R.id.voice_index_btn);			
			holder.nameView = (TextView) convertView.findViewById(R.id.music_name_txt);			
			holder.downBtn = (Button) convertView.findViewById(R.id.music_static_btn);			
			holder.processBar = (ProgressBar)convertView.findViewById(R.id.download_progress_bar);			
			holder.statusView = (TextView)convertView.findViewById(R.id.receive_status_view);
			
//			holder.nameView.setOnClickListener(mContext);
			convertView.setOnClickListener(mContext);
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.indexView.setText("" + info.m_nIndex);		
		holder.nameView.setText(info.m_strName);
		
		if( info.m_nDownloadStatus == MusicInfoModel.DOWNLOAD_STATUS_END){
			holder.downBtn.setBackgroundResource(R.drawable.play_satrt_select);
		}else{
			holder.downBtn.setBackgroundResource(R.drawable.download_btn);
		}
		
		holder.processBar.setProgress(info.m_nDownPercent);
		
		switch( info.m_nDownloadStatus){
		case MusicInfoModel.DOWNLOAD_STATUS_BEGIN:
		case MusicInfoModel.DOWNLOAD_STATUS_PROGRESSING:
			String format = mContext.getString(R.string.receive_progress);
			String str = String.format( format, info.m_nDownPercent);
			holder.statusView.setText(str);
			break;
		case MusicInfoModel.DOWNLOAD_STATUS_END:
			holder.statusView.setText(R.string.receive_success);
//			statusView.setBackground(background);
			break;
		}
		return convertView;
	}
}
