package cn.kc.demo.view;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import cn.kc.demo.CrashHandler;
import cn.kc.demo.R;
import cn.kc.demo.adapter.FolderAdapter;
import cn.kc.demo.model.FolderModel;
import cn.kc.demo.model.MusicInfoModel;
import cn.kc.demo.utils.FileUtil;
import cn.kc.demo.utils.Utils;

public class FolderListActivity extends Activity {
	private static final String TAG = "DevicesListActivity";
	public static final String FOLDER_NAME = "kc_demo";
	private ArrayList<FolderModel> mListFolder = null;
	private String mAppPath;//1st path
	
	private ListView mFolderListView;
	private Button mBackButton;
	private TextView mHeaderTextView;
	private FolderAdapter mFolderListAdapter;

	private boolean isAlertShowed = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.folder_layout);
		
		initView();
		initData();
		initEvent();
	}

	private void initView() {
		mFolderListView = (ListView)findViewById(R.id.folder_list);
		mBackButton = (Button)findViewById(R.id.back_btn);
		mHeaderTextView = (TextView)findViewById(R.id.folder_header_txt);
	}
	private void initData() {
		mListFolder = new ArrayList<FolderModel>();
		
		mAppPath = FileUtil.getStoragePath(FolderListActivity.this) + "/" +  FOLDER_NAME ;
		GetFiles(mAppPath);
		
		mFolderListAdapter = new FolderAdapter(FolderListActivity.this);
		mFolderListAdapter.setList(mListFolder);
		mFolderListView.setAdapter(mFolderListAdapter);
	}
	private void initEvent() {		
		mBackButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				finish();
			}
		});
		
		mFolderListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				FolderModel model = mListFolder.get(position);
				startActivityWithFolder(model);
			}

		});
	}
	
	private void startActivityWithFolder(FolderModel model) {
		Intent in = new Intent(FolderListActivity.this, VoiceListActivity.class);
		in.putExtra(VoiceListActivity.PATH, mAppPath + "/" + model.getFolderName());
		in.putExtra(VoiceListActivity.FOLDER_NAME, model.getFolderName());
		startActivity(in);
	}
	
	private void GetFiles(String Path)  //搜索目录，扩展名，是否进入子文件夹
	{
//    	int nIndex = mListFolder.size();
	    File[] files = new File(Path).listFiles();

        if(files == null)  
            return;
        
        Arrays.sort(files, new Comparator<File>() {

			public int compare(File lhs, File rhs) {
				long diff = lhs.lastModified() - rhs.lastModified();
				
				if(diff<0)
					return 1;
				else if(diff==0)
		  		  	return 0;
				else
					return -1;
			}
		});
        
	    for (int i = 0; i < files.length; i++) {
	        File f = files[i];
	        if (f.isFile() )
	        {	        	
	        	continue;
	        }
	        else if (f.isDirectory() ){  //忽略点文件（隐藏文件/文件夹）
	        	if(f.getPath().indexOf("/.") != -1 || f.getPath().contains(CrashHandler.BUG_FOLDER))
	        		continue;
	        	
	        	FolderModel newInfo = new FolderModel(f.getName());
	        	mListFolder.add(newInfo);
	        } 
	    }
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if(!isAlertShowed){
			isAlertShowed = true;
			LayoutInflater factory = LayoutInflater.from(this);
            final EditText textEntryView = (EditText) factory.inflate(R.layout.alert_create_new_folder_layout, null);
            textEntryView.setText(Utils.getDateString());
            new AlertDialog.Builder(FolderListActivity.this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(R.string.alert_dialog_text_entry)
                .setView(textEntryView)
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        /* User clicked OK so do some stuff */
                    	
                    	String name =  textEntryView.getText().toString();
						String path = mAppPath + "/" + name;
						FolderModel model = new FolderModel(name);
						if( !FileUtil.IsFileExist(path) ){
							FileUtil.CreatSDDir( path );
							mListFolder.add(0,model);
							
							if(mFolderListAdapter != null)
								mFolderListAdapter.notifyDataSetChanged();
						}
						
						startActivityWithFolder(model);
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        /* User clicked cancel so do some stuff */
                    }
                })
                .create()
                .show();
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
