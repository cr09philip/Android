package cn.kc.demo.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import cn.kc.demo.R;
import cn.kc.demo.model.FolderModel;

public class FolderAdapter extends ArrayListAdapter<FolderModel>  {
	class ViewHolder {
		TextView foldersNameTextView;
	}
	public FolderAdapter(Activity context) {
		super(context);
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.folder_item, null);
			holder = new ViewHolder();
			holder.foldersNameTextView = (TextView) convertView.findViewById(R.id.folder_name);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		FolderModel model = mList.get(position);
		holder.foldersNameTextView.setText(model.getFolderName());
		
		convertView.setBackgroundResource(R.drawable.listview_bg);// R.drawable.listview_select_bg);// : 
		return convertView;
	}
}
