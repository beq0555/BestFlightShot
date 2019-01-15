package com.websarva.wings.android.bestflightshot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ManualActivity extends AppCompatActivity {

    private WebView webView;
    private WebSettings webSettings;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);
        setTitle("Manual");

        webView=findViewById(R.id.web_view);
        webSettings=webView.getSettings();


        //URLの読み込み
        webView.loadUrl("file:///android_asset/manual/index.html");
        //javascriptの有効化
        webView.getSettings().setJavaScriptEnabled(true);
        //ピンチ操作による拡大・縮小の有効化
        webSettings.setBuiltInZoomControls(true);
        //標準ブラウザへのジャンプを防ぐ
        webView.setWebViewClient(new WebViewClient());

    }

    public void reload_click(View view){
        webView.reload();
    }

    public boolean back_click(View view){
        webView.goBack();
        return true;
    }

    public boolean next_click(View view){
        webView.goForward();
        return  true;
    }

    public void close_click(View view){
        Intent intent=new Intent(ManualActivity.this,OpeningActivity.class);
        startActivity(intent);
    }
}
