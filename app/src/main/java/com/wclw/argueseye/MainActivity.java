package com.wclw.argueseye;

import android.app.Notification;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private UrlParser urlParser;
    private EditText editText_url;

    private LinearLayout domainDetails;
    private LinearLayout sslCertLayout;

    private TextView domainDetailsTV;
    private TextView sslCertificateStatusTV;

    private boolean isDomainDetailsVisible = false;
    private boolean isSSLDetailsVisible = false;

    // Cache these views once (used a lot)
    private TextView tv_domain, tv_subdomain, tv_tld, tv_path, tv_query, tv_scheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        urlParser = new UrlParser();


        editText_url = findViewById(R.id.editTxt_url);
        domainDetails = findViewById(R.id.domain_details_container);
        sslCertLayout = findViewById(R.id.ssl_cert_container);

        domainDetailsTV = findViewById(R.id.tv_domain_details_status);
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
        domainDetailsTV.setOnClickListener(v -> {
            isDomainDetailsVisible = !isDomainDetailsVisible;
            domainDetails.setVisibility(isDomainDetailsVisible ? View.VISIBLE : View.GONE);
            updateArrow(domainDetailsTV);
        });

        sslCertificateStatusTV.setOnClickListener(v -> {
            isSSLDetailsVisible = !isSSLDetailsVisible;
            sslCertLayout.setVisibility(isSSLDetailsVisible ? View.VISIBLE : View.GONE);
            updateArrow(sslCertificateStatusTV);
        });

        // Start collapsed
        domainDetails.setVisibility(View.GONE);
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
            domainDetailsTV.setText("Domain Details ▼");
            isDomainDetailsVisible = false;
            domainDetails.setVisibility(View.GONE);

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


    // Menu & other buttons
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