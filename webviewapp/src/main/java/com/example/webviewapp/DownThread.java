package com.example.webviewapp;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.helper.HttpConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by root on 2017/12/9.
 */

public class DownThread extends Thread {
    private String url;
    private Context context;
    public DownThread(Context context,String url){
        this.context =context;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            URL HttpUrl = new URL(url);
            String fileName=url.substring(url.lastIndexOf("/")+1);
            HttpURLConnection connection = (HttpURLConnection) HttpUrl.openConnection();
            InputStream inputStream = connection.getInputStream();
            File DownFile;
            File SDFile;
            FileOutputStream fileOutputStream = null;
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                DownFile = Environment.getExternalStorageDirectory();
                Log.e("gaokaidong","Downfile:"+DownFile.toString());
                Log.e("gaokaidong","filename:"+fileName);
                SDFile =new File(DownFile,fileName);
                fileOutputStream = new FileOutputStream(SDFile);
            }
            byte [] b = new byte[6*1024];
            int len;
            while ((len=inputStream.read(b))!=-1){
                if (fileOutputStream!=null){
                    fileOutputStream.write(b,0,len);
                }
            }
            if (fileOutputStream!=null){
                fileOutputStream.close();
            }
            if (inputStream!=null){
                inputStream.close();
            }
            Log.e("gaokaidong","下载完成");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
