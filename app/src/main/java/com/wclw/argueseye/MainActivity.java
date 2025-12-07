package com.wclw.argueseye;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.common.base.MoreObjects;
import com.wclw.argueseye.dto.DomainTimeData;
import com.wclw.argueseye.dto.RdapRespose;
import com.wclw.argueseye.helpers.BloomFilterHelper;
import com.wclw.argueseye.helpers.CertificateChecker;
import com.wclw.argueseye.helpers.UrlInspectorHelper;
import com.wclw.argueseye.helpers.UrlParser;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private UrlParser urlParser;
    private EditText editText_url;

    //containers
    private LinearLayout urlDetailsLayout;
    private LinearLayout domainInfoLayout;
    private LinearLayout sslCertLayout;

    //container titles
    private TextView urlDetailsTV;
    private TextView domainInfoTV;
    private TextView sslCertificateStatusTV;


    private boolean isUrlDetailsVisible = false;
    private boolean isDomainInfoVisible = false;
    private boolean isSSLDetailsVisible = false;

    // Cache these views once (used a lot)
    private TextView tv_domain, tv_subdomain, tv_tld, tv_path, tv_query, tv_scheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EdgeToEdge.enable(this);

        urlParser = new UrlParser();

        editText_url = findViewById(R.id.editTxt_url);
        urlDetailsLayout = findViewById(R.id.domain_details_container);
        domainInfoLayout = findViewById(R.id.domain_info_container);
        sslCertLayout = findViewById(R.id.ssl_cert_container);

        urlDetailsTV = findViewById(R.id.tv_url_details_status);
        domainInfoTV = findViewById(R.id.tv_domain_info_status);
        sslCertificateStatusTV = findViewById(R.id.tv_ssl_certificate_status);


        tv_domain = findViewById(R.id.tv_domain);
        tv_subdomain = findViewById(R.id.tv_subdomain);
        tv_tld = findViewById(R.id.tv_tld);
        tv_path = findViewById(R.id.tv_path);
        tv_query = findViewById(R.id.tv_query);
        tv_scheme = findViewById(R.id.tv_scheme);


        setupExpandableSections();

        findViewById(R.id.btn_verify).setOnClickListener(v -> verifyUrl());
        findViewById(R.id.btn_continue).setOnClickListener(v->continueToBrowser());
        findViewById(R.id.btn_open_sandbox).setOnClickListener(v->openBrowserSandBox());

//        BloomFilterHelper.initialize(this);
    }

    private void setupExpandableSections() {
        urlDetailsTV.setOnClickListener(v -> {
            isUrlDetailsVisible = !isUrlDetailsVisible;
            urlDetailsLayout.setVisibility(isUrlDetailsVisible ? View.VISIBLE : View.GONE);
            updateArrow(urlDetailsTV);
        });

        domainInfoTV.setOnClickListener(v->{
            isDomainInfoVisible = !isDomainInfoVisible;
            domainInfoLayout.setVisibility(isDomainInfoVisible? View.VISIBLE : View.GONE);
            updateArrow(domainInfoTV);
        });

        sslCertificateStatusTV.setOnClickListener(v -> {
            isSSLDetailsVisible = !isSSLDetailsVisible;
            sslCertLayout.setVisibility(isSSLDetailsVisible ? View.VISIBLE : View.GONE);
            updateArrow(sslCertificateStatusTV);
        });

        // Start collapsed
        urlDetailsLayout.setVisibility(View.GONE);
        domainInfoLayout.setVisibility(View.GONE);
        sslCertLayout.setVisibility(View.GONE);

    }

    private void updateArrow(TextView textView) {
        String text = textView.getText().toString();
        if (text.contains("▼")) {
            textView.setText(text.replace("▼", "▲"));
        } else {
            textView.setText(text.replace("▲", "▼"));
        }
    }

    private void verifyUrl() {
        Button btnVerifiy = findViewById(R.id.btn_verify);
        btnVerifiy.setClickable(false);

        BloomFilterHelper.initialize(this);
        String url = editText_url.getText().toString().trim();


        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Please enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }


        FilterType type = BloomFilterHelper.checkFilter(url);
        UrlParser.Parts parts = urlParser.parseUrl(url);

        TextView tv_risk = findViewById(R.id.tv_risk_level);
        TextView tv_cert_Stat = findViewById(R.id.tv_cert_avilability);

        // Update risk level
        switch (type) {
            case TRUSTED :
                tv_risk.setText("Found in TRUSTED List");
                break;
            case UNTRUSTED :
                tv_risk.setText("Found in Untrusted List");
                break;
            case NONE :
                tv_risk.setText("Not Found");
                break;
        }

        if (parts != null) {
            // Fill URL parts
            tv_domain.setText(parts.domain + "." + parts.tld);
            tv_subdomain.setText(TextUtils.isEmpty(parts.subdomain) ? "None" : parts.subdomain);
            tv_tld.setText(parts.tld);
            tv_path.setText(TextUtils.isEmpty(parts.path) ? "/" : parts.path);
            tv_query.setText(TextUtils.isEmpty(parts.query) ? "None" : parts.query);
            tv_scheme.setText(parts.scheme);

            // Reset expandable sections every time
            urlDetailsTV.setText("URL Details ▼");
            isUrlDetailsVisible = false;
            urlDetailsLayout.setVisibility(View.GONE);

            loadRdapData(url);
            domainInfoTV.setText("Domain Information ▼");
            domainInfoLayout.setVisibility(View.GONE);

            if (parts.scheme.equalsIgnoreCase("https")) {
                tv_cert_Stat.setVisibility(View.GONE);
                sslCertificateStatusTV.setText("SSL Certificate ▼");
                isSSLDetailsVisible = false;
                showCertificateDetails(url);  // This will fill SSL data
            } else {
                tv_cert_Stat.setText("No SSL certificate (not HTTPS)");
                tv_cert_Stat.setVisibility(View.VISIBLE);
                sslCertificateStatusTV.setText("SSL Certificate (Not Available)");
                sslCertLayout.setVisibility(View.GONE);
            }
        } else {
            tv_risk.setText("Invalid URL Format");
            tv_cert_Stat.setText("Invalid URL");
            tv_cert_Stat.setVisibility(View.VISIBLE);
        }

        btnVerifiy.setClickable(true);
    }

    private void showCertificateDetails(String url) {
        new CertificateChecker().checkCertificate(url, this, results -> runOnUiThread(() -> {
            if (results == null || results.error != null) {
                sslCertLayout.setVisibility(View.GONE);
                sslCertificateStatusTV.setText("SSL Certificate (Error)");
                Toast.makeText(this, "SSL Error: " + (results != null ? results.error : "Failed"), Toast.LENGTH_LONG).show();
                return;
            }

            sslCertificateStatusTV.setText("SSL Certificate  ▼");
            isSSLDetailsVisible = false;
            sslCertLayout.setVisibility(View.GONE); // collapsed by default

            // Fill certificate info
            ((TextView) findViewById(R.id.tv_cert_subject)).setText(results.subject != null ? results.subject : "N/A");
            ((TextView) findViewById(R.id.tv_cert_issuer)).setText(results.issuer != null ? results.issuer : "N/A");
            ((TextView) findViewById(R.id.tv_cert_valid_from)).setText(results.validFrom != null ? results.validFrom : "N/A");
            ((TextView) findViewById(R.id.tv_cert_valid_until)).setText(results.validUntil != null ? results.validUntil : "N/A");
            ((TextView) findViewById(R.id.tv_cert_fingerprint)).setText(results.fingerprint != null ? results.fingerprint : "N/A");

            String sans = results.sans.isEmpty() ? "None" : "• " + TextUtils.join("\n• ", results.sans);
            ((TextView) findViewById(R.id.tv_cert_sans)).setText(sans);

            // Warning / Safe banner
            TextView tvSummary = findViewById(R.id.tv_cert_security_summary);
            View warningBanner = findViewById(R.id.cert_warning_banner);
            View safeBanner = findViewById(R.id.cert_safe_banner);

            StringBuilder warning = new StringBuilder();
            boolean hasIssue = false;

            if (results.hostnameMismatch) { warning.append("HOSTNAME MISMATCH\n"); hasIssue = true; }
            if (results.expired) { warning.append("CERTIFICATE EXPIRED\n"); hasIssue = true; }
            if (results.selfSigned) { warning.append("SELF-SIGNED CERTIFICATE\n"); hasIssue = true; }

            if (hasIssue) {
                tvSummary.setText(warning.toString().trim());
                warningBanner.setVisibility(View.VISIBLE);
                safeBanner.setVisibility(View.GONE);
            } else {
                warningBanner.setVisibility(View.GONE);
                safeBanner.setVisibility(View.VISIBLE);
            }

            // Copy fingerprint on long press
            findViewById(R.id.tv_cert_fingerprint).setOnLongClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText("Fingerprint", results.fingerprint));
                Toast.makeText(this, "Fingerprint copied!", Toast.LENGTH_SHORT).show();
                return true;
            });
        }));
    }

    //get domainage details from retro
    private void loadRdapData(String url) {
        UrlParser.Parts urlParts = urlParser.parseUrl(url);
        String domain = urlParts.domain + "." + urlParts.tld;

        UrlInspectorHelper.getInstance().fetchWhois(this,domain, new Callback<RdapRespose>() {
            @Override
            public void onResponse(Call<RdapRespose> call, Response<RdapRespose> response) {
                TextView tv_registor = findViewById(R.id.tv_registration_date);
                TextView tv_expior = findViewById(R.id.tv_expire_date);
                TextView tv_last_changed = findViewById(R.id.tv_last_changed);
                TextView tv_domain_age = findViewById(R.id.tv_domain_age);
                TextView tv_is_expired = findViewById(R.id.tv_is_expired);
                TextView tv_safety_note = findViewById(R.id.tv_safety_note);
                TextView tv_summery = findViewById(R.id.tv_risk_summery);


                if (response.isSuccessful() && response.body() != null) {
                    RdapRespose rdap = response.body();

                    //fetch dates
                    String registrationDate = UrlInspectorHelper.getInstance()
                            .findEventDate(rdap.events, "registration");
                    String expirationDate = UrlInspectorHelper.getInstance()
                            .findEventDate(rdap.events, "expiration");
                    String lastUpdateDate = UrlInspectorHelper.getInstance()
                            .findEventDate(rdap.events,"last changed");

                    //clean dates
                    registrationDate = UrlInspectorHelper.getInstance().cleanTimeStamp(registrationDate);
                    expirationDate = UrlInspectorHelper.getInstance().cleanTimeStamp(expirationDate);
                    lastUpdateDate = UrlInspectorHelper.getInstance().cleanTimeStamp(lastUpdateDate);

                    //get age and safety note
                    DomainTimeData domainTimeData = UrlInspectorHelper.getInstance()
                            .domainAgeCheck(registrationDate,expirationDate);

                    //set data to textViews
                    tv_registor.setText(!registrationDate.equals("Unknown") ? registrationDate : "N/A");
                    tv_expior.setText(!expirationDate.equals("Unknown") ? expirationDate : "N/A");
                    tv_last_changed.setText(!lastUpdateDate.equals("Unknown")?lastUpdateDate:"N/A");
                    tv_domain_age.setText(domainTimeData.domainAge);
                    if(domainTimeData.isExpired){
                        tv_is_expired.setTextColor(getResources().getColor(R.color.danger_red));
                    }
                    tv_is_expired.setText(domainTimeData.isExpired? "True":"False");
                    tv_safety_note.setText(domainTimeData.message);
                    tv_summery.setText(domainTimeData.message);

                } else {
                    tv_registor.setText("N/A");
                    tv_expior.setText("N/A");
                    tv_last_changed.setText("N/A");

                    Toast.makeText(MainActivity.this, "RDAP response empty or failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<RdapRespose> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to fetch RDAP: " + t.getMessage(), Toast.LENGTH_LONG).show();
                t.printStackTrace();
            }
        });
    }




    private void continueToBrowser() {
        String url = editText_url.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Enter a URL first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add https:// if no scheme
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://" + url;
        }

        if (!android.webkit.URLUtil.isValidUrl(url)) {
            Toast.makeText(this, "Invalid URL", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri uri = Uri.parse(url);
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);

        Intent chooser = Intent.createChooser(browserIntent, "Open with");

        // check if there is atlest 1 browser
        if (chooser.resolveActivity(getPackageManager()) != null) {
            startActivity(chooser);
        } else {
            Toast.makeText(this, "No web browser installed!", Toast.LENGTH_LONG).show();
        }
    }


    public void goToMenu(View view) {
        startActivity(new Intent(this, MenuActivity.class));
    }


    public void openBrowserSandBox() {
        String url = editText_url.getText().toString().trim();

        if (TextUtils.isEmpty(url)) {
            Toast.makeText(this, "Enter a URL first", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent webViewIntent = new Intent(this,SandBoxBrowser.class);
        webViewIntent.putExtra("url",url);
        startActivity(webViewIntent);
    }
    public void blockUrl(View view) { /* TODO */ }
}