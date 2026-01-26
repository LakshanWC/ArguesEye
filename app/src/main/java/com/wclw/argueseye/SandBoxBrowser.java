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
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewFeature;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.wclw.argueseye.dto.SandboxSettings;
import com.wclw.argueseye.services.AdBlockerService;

public class SandBoxBrowser extends AppCompatActivity {


    private final SandboxSettings settings = new SandboxSettings();
    private WebView sandBox;
    private SwipeRefreshLayout swipeRefresh;
    private EditText et_current_url;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sand_box_browser);

        sandBox = findViewById(R.id.wv_sandBox);
        et_current_url = findViewById(R.id.et_current_url);

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
            public WebResourceResponse shouldInterceptRequest(
                    WebView webView,
                    WebResourceRequest request){

                if(settings.adBlockerOn) {
                    String url = request.getUrl().toString();

                    if (AdBlockerService.getInstance().isAd(url)) {
                        return new WebResourceResponse(
                                "text/plain",
                                "utf-8",
                                null
                        );
                    }
                    //no instents there for adblocker is off
                    return super.shouldInterceptRequest(webView, request);
                }
                //adblocker is off
                return super.shouldInterceptRequest(webView, request);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                swipeRefresh.setRefreshing(false);
                et_current_url.setText(url);


                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Sandbox • " + url);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String newUrl = request.getUrl().toString();
                et_current_url.setText(newUrl);
                return false;
            }
        });


        WebSettings s = sandBox.getSettings();
        s.setJavaScriptEnabled(settings.jsEnabled);
        s.setLoadsImagesAutomatically(settings.imagesEnabled);
        s.setGeolocationEnabled(settings.locationEnabled);
        s.setDomStorageEnabled(false);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setCacheMode(WebSettings.LOAD_NO_CACHE);//load no cash

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


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (sandBox != null && sandBox.canGoBack()) {
                    sandBox.goBack();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        et_current_url.setText(sandBox.getUrl());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSettingsDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.sandbox_settings, null);

        MaterialSwitch switchJs           = dialogView.findViewById(R.id.switch_js);
        MaterialSwitch switchImages       = dialogView.findViewById(R.id.switch_images);
        MaterialSwitch switchLocation     = dialogView.findViewById(R.id.switch_location);
//        MaterialSwitch switchAdBlocker    = dialogView.findViewById(R.id.switch_adblocker);
        MaterialSwitch switchPopupBlock   = dialogView.findViewById(R.id.switch_popup_block);
        MaterialSwitch switch3rdCookies   = dialogView.findViewById(R.id.switch_third_party_cookies);
        MaterialSwitch switchSpoofUA      = dialogView.findViewById(R.id.switch_spoof_ua);
        MaterialSwitch switchDesktop      = dialogView.findViewById(R.id.switch_desktop);
        MaterialSwitch switchNoReferrer   = dialogView.findViewById(R.id.switch_no_referrer);

        // Load current state
        switchJs.setChecked(settings.jsEnabled);
        switchImages.setChecked(settings.imagesEnabled);
        switchLocation.setChecked(settings.locationEnabled);
//        switchAdBlocker.setChecked(settings.adBlockerOn);
        switchPopupBlock.setChecked(settings.popupBlocked);
        switch3rdCookies.setChecked(settings.blockThirdPartyCookies);
        switchSpoofUA.setChecked(settings.spoofUserAgent);
        switchDesktop.setChecked(settings.desktopSite);
        switchNoReferrer.setChecked(settings.noReferrer);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Sandbox Settings")
                .setView(dialogView)
                .setPositiveButton("Apply", null)
//                .setNeutralButton("Reload", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button btnApply = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button btnReload = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

            View.OnClickListener applyAndDismiss = v -> {
                // Save all settings
                settings.jsEnabled = switchJs.isChecked();
                settings.imagesEnabled = switchImages.isChecked();
                settings.locationEnabled = switchLocation.isChecked();
//                settings.adBlockerOn = switchAdBlocker.isChecked();
                settings.popupBlocked = switchPopupBlock.isChecked();
                settings.blockThirdPartyCookies = switch3rdCookies.isChecked();
                settings.spoofUserAgent = switchSpoofUA.isChecked();
                settings.desktopSite = switchDesktop.isChecked();
                settings.noReferrer = switchNoReferrer.isChecked();

                applySettings();
                Toast.makeText(this, v == btnApply ? "Settings applied → Reloaded" : "Settings applied", Toast.LENGTH_SHORT).show();
//                if (v == btnReload) sandBox.reload();
//                reload after applying automaticly
                sandBox.reload();

                dialog.dismiss();
            };

            btnApply.setOnClickListener(applyAndDismiss);
            btnReload.setOnClickListener(applyAndDismiss);
        });

        dialog.show();

    }

    private void applySettings() {
        WebSettings s = sandBox.getSettings();

        s.setJavaScriptEnabled(settings.jsEnabled);
        s.setLoadsImagesAutomatically(settings.imagesEnabled);
        s.setGeolocationEnabled(settings.locationEnabled);

        CookieManager cookieManager = CookieManager.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            cookieManager.setAcceptThirdPartyCookies(sandBox, !settings.blockThirdPartyCookies);
        }

        cookieManager.setAcceptCookie(!settings.blockThirdPartyCookies);

        // User-Agent spoofing
        String baseUA = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Mobile Safari/537.36";
        String ua = settings.desktopSite
                ? "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124 Safari/537.36"
                : (settings.spoofUserAgent ? baseUA + " ArguesEye/Sandbox" : s.getUserAgentString());
        s.setUserAgentString(ua);



        updatePopupBlocking();
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


        if (settings.popupBlocked) {

            sandBox.setWebChromeClient(new WebChromeClient() {
                @Override
                public boolean onCreateWindow(WebView view, boolean isDialog,
                                              boolean isUserGesture, android.os.Message resultMsg) {
                    if (settings.popupBlocked) {
                        Toast.makeText(SandBoxBrowser.this, "Pop-up blocked", Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
                }

                @Override
                public boolean onJsAlert(WebView view, String url, String message, android.webkit.JsResult result) {
                    if (settings.popupBlocked) {
                        result.cancel();
                        return true;
                    }
                    return super.onJsAlert(view, url, message, result);
                }

                @Override
                public boolean onJsConfirm(WebView view, String url, String message, android.webkit.JsResult result) {
                    if (settings.popupBlocked) {
                        result.cancel();
                        return true;
                    }
                    return super.onJsConfirm(view, url, message, result);
                }

                @Override
                public boolean onJsPrompt(WebView view, String url, String message,
                                          String defaultValue, android.webkit.JsPromptResult result) {
                    if (settings.popupBlocked) {
                        result.cancel();
                        return true;
                    }
                    return super.onJsPrompt(view, url, message, defaultValue, result);
                }
            });


        } else {
            sandBox.setWebChromeClient(new WebChromeClient());
        }
    }


    private boolean isUsingDefaultSecuritySettings() {
        return  !settings.jsEnabled &&
                settings.imagesEnabled &&
                !settings.locationEnabled &&
                settings.popupBlocked &&
                settings.blockThirdPartyCookies &&
                settings.spoofUserAgent &&
                settings.noReferrer;
    }
}