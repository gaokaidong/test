package com.example.webviewapp;

import android.graphics.Bitmap;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by root on 2017/11/23.
 */

public class myWebViewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        view.loadUrl(request.getUrl().toString());
        return true;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        //Log.e("gaokaidong","intercept Request url:"+request.getUrl().toString());
        return super.shouldInterceptRequest(view, request);
    }
}
