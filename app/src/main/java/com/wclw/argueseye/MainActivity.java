package com.wclw.argueseye;

import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private View loadingOverlay;
    private TextView loadingMessage;

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

        Button btn = findViewById(R.id.btn_block);
        btn.setActivated(false);

        loadingOverlay = findViewById(R.id.loading_overlay);
        loadingMessage = findViewById(R.id.txt_loading_message);

        showLoading(true, "Initializing security database...");
        buildFiltersAsync();


    }


    public void goToMenu(View view){
        Intent menuIntent = new Intent(MainActivity.this,MenuActivity.class);
        startActivity(menuIntent);
    }

    public void verifyButtonClick(View view){
        Button button = findViewById(R.id.btn_verify);
        TextView tv_prefix = findViewById(R.id.tv_domain_prefix);
        TextView tv_risk = findViewById(R.id.tv_risk_level);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tv_prefix.setText("ww3.");
                tv_risk.setText("Safe I think");

                //ToDo:implement url verification
            }
        });
    }


    private void buildFiltersAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            boolean result = createInitialBaseLineFilters();

            runOnUiThread(() -> {
                showLoading(false, "");
                if (result) {
                    Toast.makeText(this, "Filters ready!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "Failed to create filters!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }


    public boolean createInitialBaseLineFilters() {
        CsvToBloomFilter csvToBloomFilter = new CsvToBloomFilter();

        String trustedSourcesPath   = "raw/trusted/tranco_trusted_initial.csv";
        String untrustedSourcesPath = "raw/untrusted/urlhash_scam_initial.csv";


        boolean trustedOK   = csvToBloomFilter.buildBloomFilter(this, trustedSourcesPath, 1048576, 0.0001);
        boolean untrustedOK = csvToBloomFilter.buildBloomFilter(this, untrustedSourcesPath, 107212, 0.0001);

        return trustedOK && untrustedOK;
    }


    private void showLoading(boolean show, String message) {
        loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        loadingMessage.setText(message);
    }


    public void openBrowserSandBox(View view){
        EditText urlTxt = findViewById(R.id.editTxt_url);

        //TODO:implement sandbox method
    }

    public void blockUrl(View view){
        //TODO:implement url block method
    }
}