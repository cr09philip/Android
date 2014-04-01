package cn.kc.demo.view;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import cn.kc.demo.R;
import cn.kc.demo.utils.CodeUtil;
import cn.kc.demo.utils.WifiAdmin;

public class LoginActivity extends Activity {
	private Button mLoginBtn;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.login_layout);
		
		/*
		new Timer().schedule(new TimerTask() {			
			@Override
			public void run() {
				String path = FileUtil.getStoragePath(LoginActivity.this) + "/kc_demo";
				if( !FileUtil.IsFileExist(path) ){
					 FileUtil.CreatSDDir( path );
				}
				
				String str = path + "/sh.vox";
				if( !FileUtil.IsFileExist(str) ){
					File file = new File(path + "/sh.vox");
					FileOutputStream outputWrite = null;
					try {
						outputWrite = new FileOutputStream(file);
						AssetManager am = LoginActivity.this.getAssets();
						InputStream in = am.open("sh.vox");

						byte[] buffer = new byte[4096];
						while( in.read(buffer) != -1){
							outputWrite.write(buffer);
							outputWrite.flush();
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					finally{
						try {
							outputWrite.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}					
				}
			}
		}, 0);
		*/
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
				Intent in = new Intent(LoginActivity.this, DevicesListActivity.class);
				startActivity(in);
				finish();
			}
		});
	}
}