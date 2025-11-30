package com.wclw.argueseye;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private UrlParser urlParser;
    private EditText editText_url;
    private LinearLayout sslCertLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        urlParser = new UrlParser();


        Button btn = findViewById(R.id.btn_block);
        editText_url = findViewById(R.id.editTxt_url);
        sslCertLayout = findViewById(R.id.ssl_cert_container);
        btn.setActivated(false);


        BloomFilterHelper.initialize(this);

    }


    public void goToMenu(View view){
        Intent menuIntent = new Intent(MainActivity.this,MenuActivity.class);
        startActivity(menuIntent);
    }

    public void verifyButtonClick(View view){
        Button button = findViewById(R.id.btn_verify);
        TextView tv_risk = findViewById(R.id.tv_risk_level);

        TextView tv_domain = findViewById(R.id.tv_domain);
        TextView tv_scheme = findViewById(R.id.tv_scheme);
        TextView tv_subdomain = findViewById(R.id.tv_subdomain);
        TextView tv_tdl = findViewById(R.id.tv_tld);
        TextView tv_path = findViewById(R.id.tv_path);
        TextView tv_query = findViewById(R.id.tv_query);

        TextView tv_cert_Stat = findViewById(R.id.tv_cert_avilability);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                FilterType type = BloomFilterHelper.checkFilter(editText_url.getText().toString());
                UrlParser.Parts parts = urlParser.parseUrl(editText_url.getText().toString());

                switch(type){
                    case TRUSTED:
                        tv_risk.setText("Found in TRUSTED List");
                        break;
                    case UNTRUSTED:
                        tv_risk.setText("Found in Untrusted List");
                        break;
                    case NONE:
                        tv_risk.setText("Not Found");
                        break;
                }

                if(parts != null) {
                    tv_domain.setText(parts.domain+"." + parts.tld);
                    tv_subdomain.setText(parts.subdomain);
                    tv_tdl.setText(parts.tld);
                    tv_path.setText(parts.path);
                    tv_query.setText(parts.query);
                    tv_scheme.setText(parts.scheme);

                    if(parts.scheme.equalsIgnoreCase("https")){
                        showCertificateDetails(editText_url.getText().toString());
                        tv_cert_Stat.setVisibility(View.GONE);
                    }else{
                        tv_cert_Stat.setText("No SSL certificate available (not HTTPS)");
                        tv_cert_Stat.setVisibility(View.VISIBLE);
                    }
                }
                else{
                    tv_cert_Stat.setText("Invalid URL");
                    tv_cert_Stat.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showCertificateDetails(String url) {

        CertificateChecker sslVerificationHelper = new CertificateChecker();

        sslVerificationHelper.checkCertificate(url, this, results -> {

            runOnUiThread(() -> {
                if (results == null || results.error != null) {
                    sslCertLayout.setVisibility(View.GONE);
                    Toast.makeText(this, "Certificate error: " + (results != null ? results.error : "Unknown"), Toast.LENGTH_LONG).show();
                    return;
                }

                sslCertLayout.setVisibility(View.VISIBLE);

                // Find views
                TextView tvSubject = findViewById(R.id.tv_cert_subject);
                TextView tvIssuer = findViewById(R.id.tv_cert_issuer);
                TextView tvFrom = findViewById(R.id.tv_cert_valid_from);
                TextView tvUntil = findViewById(R.id.tv_cert_valid_until);
                TextView tvFingerprint = findViewById(R.id.tv_cert_fingerprint);
                TextView tvSans = findViewById(R.id.tv_cert_sans);
                TextView tvSummary = findViewById(R.id.tv_cert_security_summary);
                View warningBanner = findViewById(R.id.cert_warning_banner);
                View safeBanner = findViewById(R.id.cert_safe_banner);

                // Fill basic fields
                tvSubject.setText(results.subject != null ? results.subject : "N/A");
                tvIssuer.setText(results.issuer != null ? results.issuer : "N/A");
                tvFrom.setText(results.validFrom != null ? results.validFrom : "N/A");
                tvUntil.setText(results.validUntil != null ? results.validUntil : "N/A");
                tvFingerprint.setText(results.fingerprint != null ? results.fingerprint : "N/A");

                // SANs
                String sansText = results.sans.isEmpty() ? "None" : "• " + TextUtils.join("\n• ", results.sans);
                tvSans.setText(sansText);

                // Security summary
                StringBuilder warning = new StringBuilder();
                boolean hasWarning = false;

                if (results.hostnameMismatch) {
                    warning.append("HOSTNAME DOES NOT MATCH\n");
                    hasWarning = true;
                }
                if (results.expired) {
                    warning.append("CERTIFICATE EXPIRED\n");
                    hasWarning = true;
                }
                if (results.selfSigned) {
                    warning.append("SELF-SIGNED CERTIFICATE\n");
                    hasWarning = true;
                }

                if (hasWarning) {
                    tvSummary.setText(warning.toString().trim());
                    warningBanner.setVisibility(View.VISIBLE);
                    safeBanner.setVisibility(View.GONE);
                } else {
                    warningBanner.setVisibility(View.GONE);
                    safeBanner.setVisibility(View.VISIBLE);
                }

                // Make fingerprint easy to copy
                tvFingerprint.setOnLongClickListener(v -> {
                    ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("SHA-256 Fingerprint", results.fingerprint);
                    cm.setPrimaryClip(clip);
                    Toast.makeText(this, "Fingerprint copied!", Toast.LENGTH_SHORT).show();
                    return true;
                });
            });
        });
    }


    public void openBrowserSandBox(View view){
        EditText urlTxt = findViewById(R.id.editTxt_url);

        //TODO:implement sandbox method
    }

    public void blockUrl(View view){
        //TODO:implement url block method
    }
}