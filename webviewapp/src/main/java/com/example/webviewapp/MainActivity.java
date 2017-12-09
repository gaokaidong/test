package com.example.webviewapp;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.input.InputManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private WebView mWebView;
    private WebSettings mWebSettings;
    private WebChromeClient mWebChromeClient;
    private WebViewClient mWebViewClient;
    private EditText mEditText;
    private Button mButton;
    private InputMethodManager mInputMethodManager;
    private ConnectivityManager mConnectivityManager;
    private ProgressDialog mProgressDialog;
    private static final String [] PERMISSION =new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int REQUEST_PERMISSIONS_CODE = 10086;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRequestPermissions();
    }
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void checkAndRequestPermissions(){
        List<String> permissions = new ArrayList<>();
        for(String permission:PERMISSION){
            if(checkSelfPermission(permission)!= PackageManager.PERMISSION_GRANTED){
                permissions.add(permission);
            }
        }
        if(permissions.size()>0){
            requestPermissions( permissions.toArray(new String[permissions.size()]),REQUEST_PERMISSIONS_CODE);
        }
    }
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_PERMISSIONS_CODE){
            List<String> denypermissions = new ArrayList<>();       //获取到一个权限被拒绝的列表
            for (int i=0;i<grantResults.length;i++){
                if(grantResults[i]!=PackageManager.PERMISSION_GRANTED){
                    denypermissions.add(permissions[i]);
                }
            }
            if(denypermissions.size()>0){

                if(denypermissions.contains(Manifest.permission.WRITE_EXTERNAL_STORAGE)&&!ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){                                            //如果权限被拒绝且下次不在提醒，弹出对话框，说明必须需要该权限
                    Toast.makeText(this,"hha",Toast.LENGTH_SHORT).show();
                    this.finish();
                }
                new AlertDialog.Builder(MainActivity.this).setTitle("请给予权限")
                        .setMessage("这个权限是很有用的"+denypermissions)
                        .setNegativeButton("取消",new DialogInterface.OnClickListener(){

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setPositiveButton("进入设置开启权限", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent();
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);     //这段代码是为了跳转到setting设置下该apk的详细界面来开启权限。
                                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                                intent.setData(Uri.fromParts("package", getPackageName(), null));
                                startActivity(intent);
                            }
                        })
                        .show();
            }
        }
    }



    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (msg.what==2){
                Toast.makeText(MainActivity.this,"未联网",Toast.LENGTH_LONG).show();
            }else if(msg.what==1){
                Toast.makeText(MainActivity.this,"网络需要确认",Toast.LENGTH_LONG).show();
            }else if (msg.what==0){
                Log.e("gaokaidong","网络已连接");
            }
        }
    };

    private void initView(){
        mWebView = (WebView)findViewById(R.id.mWebview);
        mWebSettings = mWebView.getSettings();
        mWebChromeClient = new myWebChromeClient();
        mWebViewClient = new myWebViewClient();
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        mEditText =(EditText)findViewById(R.id.url);
        mButton =(Button)findViewById(R.id.load);
        mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        mConnectivityManager =(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        configureWeb();
        onclick();
    }
    public void onclick(){
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //parseWithJsoup();
                Log.e("gaokaidong","start to click");
                load();
            }
        });
    }

    @Override
    protected void onResume() {
        isNetworkConnected();
        super.onResume();
    }

    public boolean isNetworkAvailable(){
        if (mConnectivityManager!=null){
            return mConnectivityManager.getActiveNetworkInfo().isAvailable();
        }
        return false;
    }
    public void isNetworkConnected(){

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Runtime runtime = Runtime.getRuntime();
                    java.lang.Process p = runtime.exec("ping -c 3 www.baidu.com");
                    int ret = p.waitFor();
                    Log.e("gaokaidong","ret:"+ret);
                    Message msg =handler.obtainMessage();
                    msg.what=ret;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        t.start();

    }

    public void load(){
        String url = mEditText.getText().toString();
        Log.e("gaokaidong","url:"+url);
        if (url!=null && url.length()>0){
            Log.e("gaokaidong","start load");
            if (url.startsWith("http://")||url.startsWith("https://")){
                Log.e("gaokaidong","url"+url);
            }else{
                url="https://"+url;
                Log.e("gaokaidong","https : url"+url);
            }

        }else{
            url="https://www.baidu.com";
            Log.e("gaokaidong","load home page");
        }
        mInputMethodManager.hideSoftInputFromWindow(mEditText.getWindowToken(),0);
        mEditText.setText(url);
        mWebView.loadUrl(url);
    }
    public void parseWithJsoup(){
        new Thread(){
            @Override
            public void run() {
                Log.e("gaokaidong","click");
                Document document = null;
                try {
                    document = Jsoup.parse(new URL(
                                    "https://www.baidu.com/"),
                            5000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (document!=null){
                    Element text = document.getElementById("endText");
                    String value =document.html();

                    Log.e("gaokaidong","value:"+value);
                }

                //String value = text.html();

            }
        }.start();
    }
    private void configureWeb(){
        mWebView.setWebChromeClient(mWebChromeClient);
        mWebView.setWebViewClient(mWebViewClient);
        mWebView.setDownloadListener(new Down());
        mWebSettings.setJavaScriptEnabled(true);
        mWebSettings.setSupportZoom(true);
        if (isNetworkAvailable()){
            mWebSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        }else {
            mWebSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if (mWebView.canGoBack()){
                mWebView.goBack();
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        if (mWebView!=null){
            mWebView.destroy();
        }
        super.onDestroy();
    }

    public void showProgressDialog(){
        if (mProgressDialog==null){
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setMessage("下载中...");
            mProgressDialog.setCancelable(true);
            mProgressDialog.show();
        }
    }

    public void dismissProgressDialog(){
        if (mProgressDialog!=null){
            mProgressDialog.dismiss();
            mProgressDialog =null;
        }
    }


    class Down implements DownloadListener{

        @Override
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Log.e("gaokaidong","down url"+url);
            Log.e("gaokaidong","down contentDisposition"+contentDisposition);
            //new DownThread(MainActivity.this,url).start();
            new DownTask().execute(url);
        }
    }

    class DownTask extends AsyncTask<String,Integer,String>{
        @Override
        protected String doInBackground(String... params) {
            Log.e("gaokaidong","start down background Thread:"+Thread.currentThread().getName());
            try {
                String url = params[0];
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
                long  total = connection.getContentLength();
                Log.e("hahah","total:"+total);
                byte [] b = new byte[6*1024];
                int len;
                int count =0;
                while ((len=inputStream.read(b))!=-1){
                    if (fileOutputStream!=null){
                        fileOutputStream.write(b,0,len);
                        count +=len;
                        //Log.e("hahah","count:"+count);
                        int value = (int) ((count / (float) total) * 100);
                        publishProgress(value);
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
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.e("hahah","values:"+values[0]);
            if (mProgressDialog!=null){
                mProgressDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            super.finalize();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            dismissProgressDialog();
            if (mWebView.canGoBack()){
                mWebView.goBack();
            }
        }
    }
}
