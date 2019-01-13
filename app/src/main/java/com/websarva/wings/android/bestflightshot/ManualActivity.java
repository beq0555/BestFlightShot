package com.websarva.wings.android.bestflightshot;

import android.content.ClipData;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ManualActivity extends AppCompatActivity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual);

        webView=findViewById(R.id.web_view);
        //javascriptの有効化
        webView.getSettings().setJavaScriptEnabled(true);
        //標準ブラウザへのジャンプを防ぐ
        webView.setWebViewClient(new WebViewClient());
        //URLの読み込み
        webView.loadUrl("https://tokyo:challenge@miyazaki-seminar.securesite.jp/classof2020/private/groupE/BestFlightShot/web_manual/index.html");

    }
}
