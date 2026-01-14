package com.wclw.argueseye;

//import io.github.edsuns.adfilter.AdFilter;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wclw.argueseye.helpers.AdFilterDownloadHelper;
import com.wclw.argueseye.helpers.CsvToBloomFilter;
import com.wclw.argueseye.services.AdBlockerService;
import com.wclw.argueseye.services.EasyListParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SplashScreenActivity extends AppCompatActivity {

    //this is for development only
    private boolean temp_skipbutton = false;
    private Button tem_skip_button;
    private Button build_button;


    private final int RETRY_COUNT = 2;
    private CsvToBloomFilter csvToBloomFilter = new CsvToBloomFilter();
    private View loadingOverlay;
    private TextView loadingMessage;
    private TextView first_row;
    private TextView second_row;
    private TextView third_row;
    private TextView fourth_row;
    private TextView fifth_row;

    private List<String> messageList = new ArrayList<>(Arrays.asList(
            "Preparing Bloom filters for trusted websites...",
            "Trusted filter built successfully",
            "Preparing Bloom filters for untrusted websites...",
            "Untrusted filter built successfully",
            "Initializing ad-blocking engine...",
            "Ad-blocking engine ready"
    ));


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        //load saved application settings
        ApplicationSettings.getInstance().load(this);


        first_row = findViewById(R.id.txt_first_row);
        second_row = findViewById(R.id.txt_second_row);
        third_row = findViewById(R.id.txt_third_row);
        fourth_row = findViewById(R.id.txt_fourth_row);
        fifth_row = findViewById(R.id.txt_fifth_row);


        //this is only for development
        tem_skip_button = findViewById(R.id.btn_skip_button);
        build_button = findViewById(R.id.btn_build_button);

//        start_animations();

        // FOR DEBUGING
         findViewById(R.id.btn_skip_button).setOnClickListener(view->skipBuilding());
//         findViewById(R.id.btn_build_button).setOnClickListener(view -> startBuilding());
    }


    public void skipBuilding(){
        Intent i = new Intent(SplashScreenActivity.this,MainActivity.class);
        startActivity(i);
    }

    public void startBuilding(){start_animations();}

    public void start_animations() {

        loadTextByEachCharater(messageList.get(0), first_row, () -> {

            boolean trustedOk = buildTrustedFilter();
            String second = trustedOk ? messageList.get(1) : "Trusted filter build failed";

            loadTextByEachCharater(second, second_row, () -> {

                loadTextByEachCharater(messageList.get(2), third_row, () -> {

                    boolean untrustedOk = buildUntrustedFilter();
                    String fourth = untrustedOk ? messageList.get(3) : "Untrusted filter build failed";

                    loadTextByEachCharater(fourth, fourth_row, () -> {

                        loadTextByEachCharater(messageList.get(4), fifth_row, () -> {

//                            buildAdBlockBloomFilters(
//                                    () -> {
//                                        loadTextByEachCharater(
//                                                messageList.get(5),
//                                                fifth_row,
//                                                () -> startActivity(
//                                                        new Intent(this, MainActivity.class)
//                                                )
//                                        );
//                                    },
//                                    () -> {
//                                        loadTextByEachCharater(
//                                                "Ad-blocking engine failed to initialize",
//                                                fifth_row,
//                                                null
//                                        );
//                                    }
//                            );

                        });
                    });
                });
            });
        });
    }


    private void loadTextByEachCharater(String message,TextView textView,Runnable onComplete){
        textView.setText("");
        final int length = message.length();
        final android.os.Handler handler = new android.os.Handler();
        final int[] index={0};

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                textView.append(String.valueOf(message.charAt(index[0])));
                index[0]++;
                if(index[0]<length){
                    handler.postDelayed(this,200);
                }
                else{
                    if(onComplete != null) onComplete.run();
                }
            }
        };
        handler.post(runnable);
    }


    private boolean buildTrustedFilter() {
        try {
            String trustedPath = "raw/trusted/tranco_trusted_initial.csv";
            return csvToBloomFilter.buildBloomFilter(this, trustedPath, 1048576, 0.0001);  // your actual code here
        } catch (Exception e) {
            return false;
        }
    }

    private boolean buildUntrustedFilter() {
        try {
            String untrustedPath = "raw/untrusted/urlhash_scam_initial.csv";
            return csvToBloomFilter.buildBloomFilter(this, untrustedPath, 107212, 0.0001);
        } catch (Exception e) {
            return false;
        }
    }

    private void buildAdBlockBloomFilters(Runnable onSuccess,Runnable onFailure){
        new Thread(()->{
            try{
                String lists = AdFilterDownloadHelper.downloadAll();
                List<AdRule> rules = EasyListParser.parse(lists);
                AdBlockerService.getInstance().init(rules);
                Log.d("SplashScreen","adbockFilter build success");
                runOnUiThread(onSuccess);
            } catch (Exception e) {
                Log.d("SplashScreen","Error"+e.getMessage());
                runOnUiThread(onFailure);
            }
        }).start();
    }
}