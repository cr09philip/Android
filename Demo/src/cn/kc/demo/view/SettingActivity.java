package cn.kc.demo.view;

import java.util.ArrayList;

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
import cn.kc.demo.R;
import cn.kc.demo.SettingsSp;
import cn.kc.demo.model.SendTimeInfoModel;

public class SettingActivity extends Activity {

	private Button mBackBtn;
	private RadioGroup mCodeTypeRg;
	private RadioGroup mChannelTypeRg;
	private Spinner mBandWidthSpinner;
	private Spinner mBitRateSpinner;
	private ArrayAdapter<CharSequence> mBandWidthAdapter;
	private ArrayAdapter<CharSequence> mBitRateAdapter;
	private SettingsSp mSettings = null;
	private View mG722SettingLayout;
	private String mBitRate48;
	private Button mBandWidthButton;
	private Button mBitRateButton;
	/**
	 * @return the mSettings
	 */
	public SettingsSp getSettings() {
		if(mSettings == null)
			mSettings = SettingsSp.Instance().init(this);
		
		return mSettings;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    
		setContentView(R.layout.setting_layout);
		setupViews();
		setupEvents();
		initValues();
	}
	private void AdjustBitRateAdapter(){
		if(getSettings().getBand_width_value() == 14){
			if(mBitRateAdapter.getCount() == 2){
				mBitRateAdapter.add(mBitRate48);
			}
		}else{
			if(mBitRateAdapter.getCount() == 3){
				mBitRateAdapter.remove(mBitRate48);
			}
		}
	}
	private void initValues() {
		mBitRate48 = (String) mBitRateAdapter.getItem(mBitRateAdapter.getCount() - 1);
		if( getSettings().getCode_type() == 0){
			mCodeTypeRg.check(R.id.code_adpcm);
			mG722SettingLayout.setVisibility(View.GONE);
		}
		else {//default
			mCodeTypeRg.check(R.id.code_g722);
			mG722SettingLayout.setVisibility(View.VISIBLE);
		}
		
		if( getSettings().getChannel_type() == 0)
			mChannelTypeRg.check(R.id.channel_mono);
		else //default
			mChannelTypeRg.check(R.id.channel_stereo);
		
		if( getSettings().getBand_width_value() == 14)
			mBandWidthSpinner.setSelection(1);
		else //default
			mBandWidthSpinner.setSelection(0);
		
		int bitrate =  getSettings().getBit_rate_value();
		
		if( getSettings().getBit_rate_value() == 24)
			mBitRateSpinner.setSelection(0);
		else if(getSettings().getBit_rate_value() == 48)
			mBitRateSpinner.setSelection(2);
		else
			mBitRateSpinner.setSelection(1);
		
		AdjustBitRateAdapter();
	}

	private void setupViews(){
		mBackBtn = (Button) findViewById(R.id.back_btn);
		
		mCodeTypeRg = (RadioGroup) findViewById(R.id.code_type);
		mChannelTypeRg = (RadioGroup) findViewById(R.id.channel_type);

		mG722SettingLayout = findViewById(R.id.g_722_1_setting);
		
		mBandWidthAdapter = ArrayAdapter.createFromResource(this, R.array.band_width_value, R.layout.spinner_header_layout);
		mBandWidthAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
		mBandWidthSpinner = (Spinner) findViewById(R.id.band_width_value);
		mBandWidthSpinner.setAdapter(mBandWidthAdapter);
		mBandWidthButton = (Button)findViewById(R.id.band_width_static_btn);
		
		String[] ls = getResources().getStringArray(R.array.bit_rate_value);
		ArrayList<CharSequence> list = new ArrayList<CharSequence>();
		for(int i = 0; i < ls.length; i++){
			list.add(ls[i]);
		}
		mBitRateAdapter = new ArrayAdapter<CharSequence>(this, R.layout.spinner_header_layout, list);
//		mBitRateAdapter = ArrayAdapter.createFromResource(this, R.array.bit_rate_value, R.layout.spinner_item_layout);
		mBitRateAdapter.setDropDownViewResource(R.layout.spinner_item_layout);
		mBitRateSpinner = (Spinner) findViewById(R.id.bit_rate_value);
		mBitRateSpinner.setAdapter(mBitRateAdapter);
		mBitRateButton = (Button)findViewById(R.id.bit_rate_static_btn);
	}
	
	private void setupEvents() {
		mBackBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				SendTimeInfoModel timeModel = null;
				if( getSettings().getCode_type() == 1){//g722.1
					timeModel =
							new SendTimeInfoModel(	getSettings().getChannel_type(),
													getSettings().getBand_width_value(),
													getSettings().getBit_rate_value() );
				}else{
					timeModel = new SendTimeInfoModel( getSettings().getChannel_type());
				}
				
				byte[] sz = null;
				sz = timeModel.toBinStream();
				
				
                SettingActivity.this.finish();
			}
		});
		
		mCodeTypeRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if( checkedId == R.id.code_adpcm){
					getSettings().setCode_type((byte) 0);
					mG722SettingLayout.setVisibility(View.GONE);
				}
				else {//default
					getSettings().setCode_type((byte) 1);
					mG722SettingLayout.setVisibility(View.VISIBLE);
				}
				AdjustBitRateAdapter();
			}
		});
		mChannelTypeRg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			public void onCheckedChanged(RadioGroup group, int checkedId) {				
				if( checkedId == R.id.channel_stereo)
					getSettings().setChannel_type((byte) 1);
				else //default
					getSettings().setChannel_type((byte) 0);
			}
		});
		mBandWidthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {				
				if(position == 1)
					getSettings().setBand_width_value((byte) 14);
				else
					getSettings().setBand_width_value((byte) 7);

				AdjustBitRateAdapter();
			}

			public void onNothingSelected(AdapterView<?> parent) {
				getSettings().setBand_width_value((byte) 7);
				AdjustBitRateAdapter();
			}
		});
		
		mBandWidthButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mBandWidthSpinner.performClick();
			}
		});
		mBitRateSpinner.setOnItemSelectedListener(new OnItemSelectedListener(){

			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if( position == 0)
					getSettings().setBit_rate_value((byte) 24);
				else if( position == 2)
					getSettings().setBit_rate_value((byte) 48);
				else
					getSettings().setBit_rate_value((byte) 32);
			}

			public void onNothingSelected(AdapterView<?> parent) {	
				getSettings().setBit_rate_value((byte) 32);		
			}
			
		});
		
		mBitRateButton.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				mBitRateSpinner.performClick();
			}
		});
	}

}
