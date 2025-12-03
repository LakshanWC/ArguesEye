package com.wclw.argueseye;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;

public class SandBoxBrowser extends AppCompatActivity {

    private WebView sandBox;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvStatus;

    // Add these member variables!
    private boolean jsEnabled = false;
    private boolean imagesEnabled = true;
    private boolean locationEnabled = false;
    private boolean popupBlocked = true;

    private boolean blockThirdPartyCookies = true;
    private boolean spoofUserAgent = true;
    private boolean desktopSite = false;
    private boolean noReferrer = true;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sand_box_browser);

        View header = findViewById(R.id.topHeader);

        ViewCompat.setOnApplyWindowInsetsListener(header, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return insets;
        });


        tvStatus = findViewById(R.id.tv_security_mode);

        sandBox = findViewById(R.id.wv_sandBox);

        //events
        findViewById(R.id.btn_settings).setOnClickListener(v -> showSettingsDialog());
        findViewById(R.id.btn_back).setOnClickListener(v -> {
            if (sandBox.canGoBack()) sandBox.goBack();
        });
        findViewById(R.id.btn_exit).setOnClickListener(v -> goHome());

        swipeRefresh = findViewById(R.id.swipeRefresh);
        findViewById(R.id.swipeRefresh).setOnClickListener(v->sandBox.reload());

        sandBox.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefresh.setRefreshing(false);
            }
        });




        WebSettings settings = sandBox.getSettings();
        settings.setJavaScriptEnabled(jsEnabled);
        settings.setLoadsImagesAutomatically(imagesEnabled);
        settings.setGeolocationEnabled(locationEnabled);
        settings.setDomStorageEnabled(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        sandBox.setWebViewClient(new WebViewClient());
        updatePopupBlocking();

        String url = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "No URL provided", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String finalUrl = url.trim();
        if (!finalUrl.startsWith("http://") && !finalUrl.startsWith("https://")) {
            finalUrl = "https://" + finalUrl;
        }

        sandBox.loadUrl(finalUrl);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Sandbox • " + finalUrl);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (sandBox != null && sandBox.canGoBack()) {
            sandBox.goBack();
        } else {
            super.onBackPressed();
        }
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.sandbox_settings, null);

        MaterialSwitch switchJs           = dialogView.findViewById(R.id.switch_js);
        MaterialSwitch switchImages       = dialogView.findViewById(R.id.switch_images);
        MaterialSwitch switchLocation     = dialogView.findViewById(R.id.switch_location);
        MaterialSwitch switchPopupBlock   = dialogView.findViewById(R.id.switch_popup_block);
        MaterialSwitch switch3rdCookies   = dialogView.findViewById(R.id.switch_third_party_cookies);
        MaterialSwitch switchSpoofUA      = dialogView.findViewById(R.id.switch_spoof_ua);
        MaterialSwitch switchDesktop      = dialogView.findViewById(R.id.switch_desktop);
        MaterialSwitch switchNoReferrer   = dialogView.findViewById(R.id.switch_no_referrer);

        // Load current state
        switchJs.setChecked(jsEnabled);
        switchImages.setChecked(imagesEnabled);
        switchLocation.setChecked(locationEnabled);
        switchPopupBlock.setChecked(popupBlocked);
        switch3rdCookies.setChecked(blockThirdPartyCookies);
        switchSpoofUA.setChecked(spoofUserAgent);
        switchDesktop.setChecked(desktopSite);
        switchNoReferrer.setChecked(noReferrer);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sandbox Settings")
                .setView(dialogView)
                .setPositiveButton("Apply", null)
                .setNeutralButton("Reload", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnApply = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnReload = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            View.OnClickListener applyAndDismiss = v -> {
                // Save all settings
                jsEnabled = switchJs.isChecked();
                imagesEnabled = switchImages.isChecked();
                locationEnabled = switchLocation.isChecked();
                popupBlocked = switchPopupBlock.isChecked();
                blockThirdPartyCookies = switch3rdCookies.isChecked();
                spoofUserAgent = switchSpoofUA.isChecked();
                desktopSite = switchDesktop.isChecked();
                noReferrer = switchNoReferrer.isChecked();

                applySettings();
                Toast.makeText(this, v == btnReload ? "Settings applied → Reloaded" : "Settings applied", Toast.LENGTH_SHORT).show();
                if (v == btnReload) sandBox.reload();
                dialog.dismiss();
            };

            btnApply.setOnClickListener(applyAndDismiss);
            btnReload.setOnClickListener(applyAndDismiss);
        });

        dialog.show();

    }

    private void applySettings() {
        WebSettings s = sandBox.getSettings();

        s.setJavaScriptEnabled(jsEnabled);
        s.setLoadsImagesAutomatically(imagesEnabled);
        s.setGeolocationEnabled(locationEnabled);

        CookieManager cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(sandBox, !blockThirdPartyCookies);
        }

        cookieManager.setAcceptCookie(!blockThirdPartyCookies);

        // User-Agent spoofing
        String baseUA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36";
        String ua = desktopSite
                ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Safari/537.36"
                : (spoofUserAgent ? baseUA + " ArguesEye/Sandbox" : s.getUserAgentString());
        s.setUserAgentString(ua);


        //there seems to be a error related to these there for temp commented these lines
        // No-referrer (AndroidX API)
//        if (WebViewFeature.isFeatureSupported(WebViewFeature.REFERRER_POLICY) && noReferrer) {
//            WebSettingsCompat.setReferrerPolicy(
//                    s,
//                    WebSettingsCompat.REFERRER_POLICY_NO_REFERRER
//            );
//        }

        updatePopupBlocking();
        updateSecurityStatus();
    }


    // Improved clear on exit
    private void goHome() {
        sandBox.clearCache(true);
        sandBox.clearHistory();
        sandBox.clearFormData();

        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookies(null);
        cookieManager.flush();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    //handel blocking popup
    private void updatePopupBlocking() {
        if (popupBlocked) {
            sandBox.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog,
                                              boolean isUserGesture, android.os.Message resultMsg) {
                    Toast.makeText(SandBoxBrowser.this, "Pop-up blocked", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
        } else {
            sandBox.setWebChromeClient(new WebChromeClient());
        }
    }


    private boolean isUsingDefaultSecuritySettings() {
        return  !jsEnabled &&
                imagesEnabled &&
                !locationEnabled &&
                popupBlocked &&
                blockThirdPartyCookies &&
                spoofUserAgent &&
                noReferrer;
    }

    private void updateSecurityStatus() {

        if (isUsingDefaultSecuritySettings()) {
            tvStatus.setText("Security Mode: Strict");
        } else {
            tvStatus.setText("Security Mode: Custom");
        }
    }
}