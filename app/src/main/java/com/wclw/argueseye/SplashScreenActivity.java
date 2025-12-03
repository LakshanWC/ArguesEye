package com.wclw.argueseye;

import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.Executors;

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
            "Untrusted filter built successfully"
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

        first_row = findViewById(R.id.txt_first_row);
        second_row = findViewById(R.id.txt_second_row);
        third_row = findViewById(R.id.txt_third_row);
        fourth_row = findViewById(R.id.txt_fourth_row);
        fifth_row = findViewById(R.id.txt_fifth_row);


        //this is only for development
        tem_skip_button = findViewById(R.id.btn_skip_button);
        build_button = findViewById(R.id.btn_build_button);

//        start_animations();

         findViewById(R.id.btn_skip_button).setOnClickListener(view->skipBuilding());
         findViewById(R.id.btn_build_button).setOnClickListener(view -> skipBuilding());
    }


    public void skipBuilding(){
        Intent i = new Intent(SplashScreenActivity.this,MainActivity.class);
        startActivity(i);
    }

    public void startBuilding(){start_animations();}

    public void start_animations() {

        loadTextByEachCharater(messageList.get(0), first_row, () -> {

            boolean trustedOk = buildTrustedFilter();
            String second = trustedOk ? messageList.get(1) : "Build Failed";

            loadTextByEachCharater(second, second_row, () -> {

                loadTextByEachCharater(messageList.get(2), third_row, () -> {

                    boolean untrustedOk = buildUntrustedFilter();
                    String fourth = untrustedOk ? messageList.get(3) : "Build Failed";

                    loadTextByEachCharater(fourth, fourth_row, () -> {

                        if (trustedOk && untrustedOk) {
                            loadTextByEachCharater(
                                    "All modules operational. Initializing application...",
                                    fifth_row,
                                    () -> {
                                        Intent redirectIntent = new Intent(this, MainActivity.class);
                                        startActivity(redirectIntent);
                                    }
                            );
                        }

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
}