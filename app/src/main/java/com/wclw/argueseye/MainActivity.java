package com.wclw.argueseye;

import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executors;

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
                        tv_cert_Stat.setVisibility(8);
                    }else{
                        tv_cert_Stat.setText("No SSL certificate available (not HTTPS)");
                        tv_cert_Stat.setVisibility(0);
                    }
                }
                else{
                    tv_cert_Stat.setText("Invalid URL");
                    tv_cert_Stat.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void showCertificateDetails(String url){
        SSLVerificationHelper sslVerificationHelper = new SSLVerificationHelper();
        SSLVerificationHelper.CertificateParts certPart = new SSLVerificationHelper.CertificateParts();
        certPart = sslVerificationHelper.checkSSLCertificate(url,this);

        if(certPart != null){

            Toast tts = Toast.makeText(this,"i got somthing",Toast.LENGTH_SHORT);
            tts.show();

        TextView cert_subject = findViewById(R.id.tv_cert_subject);
        TextView cert_issuer = findViewById(R.id.tv_cert_issuer);
        TextView cert_validFrom = findViewById(R.id.tv_cert_valid_from);
        TextView cert_validTill = findViewById(R.id.tv_cert_valid_until);

            cert_subject.setText(certPart.subject != null ? certPart.subject : "N/A");
            cert_issuer.setText(certPart.issuer != null ? certPart.issuer : "N/A");
            cert_validFrom.setText(certPart.validFrom != null ? certPart.validFrom : "N/A");
            cert_validTill.setText(certPart.validUntil != null ? certPart.validUntil : "N/A");

        sslCertLayout.setVisibility(0);
        }
        else sslCertLayout.setVisibility(8);
    }

    public void openBrowserSandBox(View view){
        EditText urlTxt = findViewById(R.id.editTxt_url);

        //TODO:implement sandbox method
    }

    public void blockUrl(View view){
        //TODO:implement url block method
    }
}