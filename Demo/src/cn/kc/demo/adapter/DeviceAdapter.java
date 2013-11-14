package cn.kc.demo.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import cn.kc.demo.R;
import cn.kc.demo.model.DevicesInfoModel;

public class DeviceAdapter extends ArrayListAdapter<DevicesInfoModel> {

	public DeviceAdapter(Activity context) {
		super(context);
	}

	public DeviceAdapter() {
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.devices_adapter_layout, null);
			holder = new ViewHolder();
			holder.devicesStaticButton = (Button) convertView.findViewById(R.id.device_btn);
			holder.devicesNameTextView = (TextView) convertView.findViewById(R.id.devices_name_txt);
			holder.devicesWifiButton = (Button) convertView.findViewById(R.id.device_wifi_btn);
			holder.devicesLinkStaticTextView = (TextView) convertView.findViewById(R.id.device_link_btn);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		DevicesInfoModel model = mList.get(position);
		boolean islink = model.getWifiStatic() == DevicesInfoModel.WIFI_LINK;
		holder.devicesStaticButton.setBackgroundResource(islink ? R.drawable.link_devices_btn : R.drawable.device);
		holder.devicesLinkStaticTextView.setText(islink ? R.string.devices_link : R.string.devices_no_link);
		holder.devicesNameTextView.setText(model.getDevicesName());
		holder.devicesWifiButton.setBackgroundResource(getWifiRes(model.getWifiIntensity()));
		
		convertView.setBackgroundResource(islink ? R.drawable.listview_select_bg : R.drawable.listview_bg);
		return convertView;
	}
	
	private int getWifiRes(int intensity) {
		int id = R.drawable.wifi_one;
		switch (intensity) {
		case DevicesInfoModel.WIFI_INTENSITY_ONE:
			id = R.drawable.wifi_one;
			break;
		case DevicesInfoModel.WIFI_INTENSITY_TWO:
			id = R.drawable.wifi_two;
			break;
		case DevicesInfoModel.WIFI_INTENSITY_THREE:
			id = R.drawable.wifi_three;
			break;
		case DevicesInfoModel.WIFI_INTENSITY_FOUR:
			id = R.drawable.wifi_four;
			break;
		default:
			break;
		}
		return id;
	}

	class ViewHolder {
		Button devicesStaticButton;
		TextView devicesLinkStaticTextView;
		Button devicesWifiButton;
		TextView devicesNameTextView;
	}
}
