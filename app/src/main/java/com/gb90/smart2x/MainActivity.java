package com.gb90.smart2x;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WebView web;
    private TextView tv;
    private Db db;

    private static final String URL = "https://golfbet90.mex.com";
    private static final int REQ_NOTIF = 1001;

    @SuppressLint({"SetJavaScriptEnabled"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.tvStatus);
        Button btnClear = findViewById(R.id.btnClear);

        db = new Db(this);

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQ_NOTIF);
            }
        }

        web = findViewById(R.id.web);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(true);

        web.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectWatcher();
            }
        });
        web.setWebChromeClient(new WebChromeClient());

        web.addJavascriptInterface(new Bridge(), "GB90");

        btnClear.setOnClickListener(v -> {
            db.clearAll();
            tv.setText("پاک شد. 0 رکورد");
        });

        web.loadUrl(URL);
        updateStatusLine();
    }

    private void updateStatusLine(){
        long c = db.count();
        List<Double> last = db.lastN(300);
        Stats.Result r = Stats.evaluate(last);
        String line = "رکورد: " + c + " | آخرین <2 streak: " + r.streakLt2 + " | P(≥2)~" + String.format("%.1f", r.p2Base*100) + "%";
        tv.setText(line);
    }

    private void maybeNotify(){
        List<Double> last = db.lastN(300);
        Stats.Result r = Stats.evaluate(last);
        if (r.entry2){
            Notifier.notifyEntry2(this, "GB90: مناسب ورود 2", "streak<2="+r.streakLt2+" | P(≥2)~"+String.format("%.0f", r.p2Base*100)+"%" );
        }
    }

    private void injectWatcher(){
        String js = "(function(){\n"+
                "  if(window.__gb90WatcherInstalled) return;\n"+
                "  window.__gb90WatcherInstalled = true;\n"+
                "  function extract(){\n"+
                "    try{\n"+
                "      var txt = document.body ? document.body.innerText : '';\n"+
                "      if(!txt) return;\n"+
                "      // Persian close text often contains: بسته شد @ 1.27x\n"+
                "      var m = txt.match(/@\\s*(\\d+(?:\\.\\d+)?)x/);\n"+
                "      if(!m) return;\n"+
                "      var val = m[1];\n"+
                "      if(window.__gb90LastSent === val) return;\n"+
                "      // only send if " +
                "      // close banner is visible somewhere\n"+
                "      if(txt.indexOf('بسته شد') === -1) return;\n"+
                "      window.__gb90LastSent = val;\n"+
                "      GB90.onMultiplier(val);\n"+
                "    }catch(e){}\n"+
                "  }\n"+
                "  var mo = new MutationObserver(function(){ extract(); });\n"+
                "  mo.observe(document.documentElement, {subtree:true, childList:true, characterData:true});\n"+
                "  setInterval(extract, 1000);\n"+
                "})();";
        web.evaluateJavascript(js, null);
    }

    public class Bridge {
        @JavascriptInterface
        public void onMultiplier(String val){
            try{
                double v = Double.parseDouble(val);
                db.insert(v, System.currentTimeMillis());
                runOnUiThread(() -> {
                    updateStatusLine();
                    maybeNotify();
                });
            }catch(Exception ignored){}
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // no-op
    }
}
