package cn.kc.demo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.apache.http.util.EncodingUtils;

import android.content.Context;

public class FileUtil {
	public final static String DEF_PATH_ROOT  = "QS";
	public final static String DEF_PATH_IMAGE = "Image";
	public final static String DEF_PATH_APK   = "APK";
	public final static String DEF_PATH_DB    = "DB";
	public final static String DEF_PATH_CATCH = "Catch";
	public static String getExternalStoragePath(Context context) {
     
        String state = android.os.Environment.getExternalStorageState();
       
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
           if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
                   return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
           }
        }
        return getStoragePath(context);
	}
	public static String getStoragePath(Context context) {
     
        String state = android.os.Environment.getExternalStorageState();
      
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
           if (android.os.Environment.getExternalStorageDirectory().canWrite()) {
                   return android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
           }
        }
        
        return context.getFilesDir().getAbsolutePath();
	}
	public static String getUUID(){ 
        String s = UUID.randomUUID().toString(); 
        
        return s.substring(0,8)+s.substring(9,13)+s.substring(14,18)+s.substring(19,23)+s.substring(24); 
    } 
	
	public static File CreatSDFile(String fileName) throws IOException {
		File file = new File(fileName);
		file.createNewFile();
		return file;
	}
	
	public static File CreatSDDir(String dirName) {
		File dir = new File(dirName);
		dir.mkdirs();
		return dir;
	}

	
	public static boolean IsFileExist(String fileName){
		File file = new File(fileName);
		return file.exists();
	}


	public static String GetFileNameByUrl(String astrUrl){
		String lstrFileName = "";
		String lstr[] = astrUrl.split("/");
		if(lstr != null&&lstr.length>0)
		{
			lstrFileName = lstr[lstr.length - 1];
		}
		return lstrFileName;
	}
	//
	public static File write2SDFromInput(String path,String fullfileName,InputStream input){
		File file = null;
		OutputStream output = null;
		try{
			CreatSDDir(path);
			file = new File(fullfileName);
			file.createNewFile();
			output = new FileOutputStream(file);
			byte buffer [] = new byte[4 * 1024];
			
			int size = 0;
			while ( (size = input.read(buffer)) != -1)
				output.write(buffer, 0, size);
/*
			while((input.read(buffer)) != -1){
				output.write(buffer);
			}
			*/
			output.flush();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		finally{
			try{
				output.close();
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
		return file;
	}
	
	public static int WriteStringToFile(String astrPath,String astrFileName,String aValue){
		
		 try
		 { 
			 CreatSDDir(astrPath);
		     File file = new File(astrPath+ astrFileName);
		     if(file.exists()) {
		          file.delete();
		     }
			 
		     FileOutputStream fout = new FileOutputStream(astrPath + astrFileName);

		     byte [] bytes = aValue.getBytes(); 

		     fout.write(bytes); 

		     fout.close(); 

		 } 
		 catch(Exception e)
		 { 
			 e.printStackTrace(); 
		 } 
		 return 0;
	}
	public static String ReadStringFromFile(String astrFilePath)
	{
		  String res=""; 

	      try
	      { 
	    	  
	    	  File file = new File(astrFilePath);
			     if(!file.exists()) {
			         return "";
			     }

	         FileInputStream fin = new FileInputStream(astrFilePath); 
	         int length = fin.available(); 
	         byte [] buffer = new byte[length]; 
	         fin.read(buffer);     
	         res = EncodingUtils.getString(buffer, "UTF-8"); 
	         fin.close();     
	      } 

	      catch(Exception e)
	      { 
	    	  e.printStackTrace(); 
	      } 
	      return res; 
	}
	
	
	
}
