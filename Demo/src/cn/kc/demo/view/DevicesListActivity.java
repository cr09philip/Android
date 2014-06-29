package cn.kc.demo.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;
import cn.kc.demo.R;
import cn.kc.demo.adapter.ArrayListAdapter;
import cn.kc.demo.adapter.DeviceAdapter;
import cn.kc.demo.interfaces.IReceiverWifiCallBack;
import cn.kc.demo.model.DevicesInfoModel;
import cn.kc.demo.utils.WifiStateReceiver;
import cn.kc.demo.utils.WifiUtil;

public class DevicesListActivity extends Activity {
	
	protected static final String TAG = "DevicesListActivity";

	private Button mBackButton;
	
	private ListView mDevicesListView;
	private ArrayListAdapter<DevicesInfoModel> mDevicesAdapter;
	private ArrayList<DevicesInfoModel> mDevicesInfoModels;
	private IReceiverWifiCallBack mReceiverWifiCallBack;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.devices_layout);
		initView();
		initData();
		initEvent();
	}
	
	public void initView() {
		mDevicesListView = (ListView) this.findViewById(R.id.devices_list);
		mBackButton = (Button) this.findViewById(R.id.back_btn);
	}
	
	public void initData() {
		mDevicesAdapter = new DeviceAdapter(this);
		mDevicesInfoModels = WifiUtil.getDevicesInfoModels(this);
		mDevicesAdapter.setList(mDevicesInfoModels);
		mDevicesListView.setAdapter(mDevicesAdapter);
		
		initWifiRecever();
	}
	
	public void initEvent() {
		final Context self = this;
		
		mBackButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		mDevicesListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				DevicesInfoModel model = mDevicesInfoModels.get(position);
				if(model.getWifiStatic() == DevicesInfoModel.WIFI_LINK) {
					Intent in = new Intent(DevicesListActivity.this, VoiceListActivity.class);
					in.putExtra(VoiceListActivity.SID, model.getSid());
					startActivity(in);
				} else {
					Toast.makeText(self, "未连接到该设备", Toast.LENGTH_LONG).show();
				}
			}
		});
	}

	private void initWifiRecever() {
		final Context self = this;
		WifiUtil.recriverWifiRecever(this);
		
		mReceiverWifiCallBack = new IReceiverWifiCallBack() {
			public void wifiStateChange() {
//				Log.d(TAG, "wifiStateChange");
				mDevicesInfoModels = WifiUtil.getDevicesInfoModels(self);
				mDevicesAdapter.setList(mDevicesInfoModels);
			}
			
			public void networkStateChange() {
//				Log.d(TAG, "networkStateChange");
				mDevicesInfoModels = WifiUtil.getDevicesInfoModels(self);
				mDevicesAdapter.setList(mDevicesInfoModels);
			}
			
			public void changeRSSI() {
//				Log.d(TAG, "changeRSSI");
				mDevicesInfoModels = WifiUtil.getDevicesInfoModels(self);
//				for (DevicesInfoModel item : mDevicesInfoModels) {
//					Log.d(TAG, "changeRSSI " + item.getWifiIntensity());
//				}
				mDevicesAdapter.setList(mDevicesInfoModels);
			}
		};
		
		WifiStateReceiver.getRecever().addReceverWifiCallBack(mReceiverWifiCallBack);
	}
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		WifiStateReceiver.getRecever().removeReceverWifiCallBack(mReceiverWifiCallBack);
	}
}