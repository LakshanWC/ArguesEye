package com.wclw.argueseye;

import android.content.Intent;
import android.drm.DrmStore;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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

    private EditText editText_url;

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
        editText_url = findViewById(R.id.editTxt_url);
        btn.setActivated(false);

        BloomFilterHelper.initialize(this);

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


                FilterType type = BloomFilterHelper.checkFilter(editText_url.getText().toString());

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

                //ToDo:implement url verification
            }
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