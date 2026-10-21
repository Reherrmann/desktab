package com.reherrmann.desktab;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {
    static final String URL = "https://reherrmann.github.io/desktab/";
    static final int FOTOS = 1;
    WebView web;
    ValueCallback<Uri[]> escolha;
    final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        web = new WebView(this);
        setContentView(web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW); // Home Assistant em http
        s.setMediaPlaybackRequiresUserGesture(false);
        web.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView v, WebResourceRequest r, WebResourceError e) {
                if (r.isForMainFrame()) {
                    v.loadData("<body style='background:#0d0f12;color:#4ff0ff;font:20px monospace;padding:40px'>"
                            + "Sem conexão. Tentando de novo...</body>", "text/html", "utf-8");
                    h.postDelayed(new Runnable() { public void run() { web.loadUrl(URL); } }, 15000);
                }
            }
        });
        web.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> cb, FileChooserParams p) {
                if (escolha != null) escolha.onReceiveValue(null);
                escolha = cb;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
                        .addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(i, "Fotos"), FOTOS);
                return true;
            }
        });
        if (b == null) web.loadUrl(URL); else web.restoreState(b);
    }

    @Override
    protected void onActivityResult(int req, int res, Intent d) {
        if (req != FOTOS || escolha == null) return;
        Uri[] r = null;
        if (res == RESULT_OK && d != null) {
            if (d.getClipData() != null) {
                r = new Uri[d.getClipData().getItemCount()];
                for (int i = 0; i < r.length; i++) r[i] = d.getClipData().getItemAt(i).getUri();
            } else if (d.getData() != null) {
                r = new Uri[] { d.getData() };
            }
        }
        escolha.onReceiveValue(r);
        escolha = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle b) { super.onSaveInstanceState(b); web.saveState(b); }

    @Override
    public void onWindowFocusChanged(boolean f) {
        super.onWindowFocusChanged(f);
        if (f) getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    @Override
    public void onBackPressed() { if (web.canGoBack()) web.goBack(); }
}
