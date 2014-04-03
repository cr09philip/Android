package cn.kc.demo.view;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Spinner;
import cn.kc.demo.CrashApplication;
import cn.kc.demo.R;
import cn.kc.demo.SettingsSp;

public class SettingActivity extends Activity {

	private Button mBackBtn;
	private RadioGroup mChannelTypeRg;
	private Spinner mBandWidthSpinner;
	private Spinner mBitRateSpinner;
	private ArrayAdapter<CharSequence> mBandWidthAdapter;
	private ArrayAdapter<CharSequence> mBitRateAdapter;
	private SettingsSp mSettings = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mSettings = SettingsSp.Instance().init(this);
	    
		setContentView(R.layout.setting_layout);
		setupViews();
		setupEvents();
		initValues();
	}

	private void initValues() {
		if(mSettings == null)
			return;
		
		if( mSettings.getChannel_type() == 0)
			mChannelTypeRg.check(R.id.channel_mono);
		else //default
			mChannelTypeRg.check(R.id.channel_stereo);
		
		if( mSettings.getBand_width_value() == 14)
			mBandWidthSpinner.setSelection(1);
		else //default
			mBandWidthSpinner.setSelection(0);
		
		int bitrate =  mSettings.getBit_rate_value();
		
		if( mSettings.getBit_rate_value() == 24)
			mBitRateSpinner.setSelection(0);
		else if(mSettings.getBit_rate_value() == 48)
			mBitRateSpinner.setSelection(2);
		else
			mBitRateSpinner.setSelection(1);
	}

	private void setupViews(){
		mBackBtn = (Button) findViewById(R.id.back_btn);
		
		mChannelTypeRg = (RadioGroup) findViewById(R.id.channel_type);

		mBandWidthAdapter = ArrayAdapter.createFromResource(this, R.array.band_width_value, R.layout.spinner_item_layout);
		mBandWidthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mBandWidthSpinner = (Spinner) findViewById(R.id.band_width_value);
		mBandWidthSpinner.setAdapter(mBandWidthAdapter);

		mBitRateAdapter = ArrayAdapter.createFromResource(this, R.array.bit_rate_value, R.layout.spinner_item_layout);
		mBitRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mBitRateSpinner = (Spinner) findViewById(R.id.bit_rate_value);
		mBitRateSpinner.setAdapter(mBitRateAdapter);
	}
	
	private void setupEvents() {
		mBackBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
                SettingActivity.this.finish();
			}
		});
		
		mChannelTypeRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(mSettings == null)
					return;
				
				if( checkedId == R.id.channel_mono)
					mSettings.setChannel_type((byte) 0);
				else //default
					mSettings.setChannel_type((byte) 1);
			}
		});
		mBandWidthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(mSettings == null)
					return;
				
				if(position == 1)
					mSettings.setBand_width_value((byte) 14);
				else
					mSettings.setBand_width_value((byte) 7);
			}

			public void onNothingSelected(AdapterView<?> parent) {
				if(mSettings != null)
					mSettings.setBand_width_value((byte) 7);
			}
		});
		
		mBitRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if(mSettings == null)
					return;
				
				if( position == 0)
					mSettings.setBit_rate_value((byte) 24);
				else if( position == 2)
					mSettings.setBit_rate_value((byte) 48);
				else
					mSettings.setBit_rate_value((byte) 32);
				
				int bitrate = mSettings.getBit_rate_value();
			}

			public void onNothingSelected(AdapterView<?> parent) {	
				if(mSettings != null)
					mSettings.setBit_rate_value((byte) 32);		
			}
			
		});
	}

}
