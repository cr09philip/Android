package cn.kc.demo.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import cn.kc.demo.R;

public class LoginActivity extends Activity {
	private Button mLoginBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_layout);
		
		initView();
		initData();
		initEvent();		
	}

	public void initView() {
		mLoginBtn = (Button) findViewById(R.id.login_btn);
	}
	
	public void initData() {
		
	}
	
	public void initEvent() {
		mLoginBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
//				Intent in = new Intent(LoginActivity.this, VoiceListActivity.class);
//				Intent in = new Intent(LoginActivity.this, DevicesListActivity.class);
				Intent in = new Intent(LoginActivity.this, FolderListActivity.class);
				startActivity(in);
//				finish();
			}
		});
	}
}