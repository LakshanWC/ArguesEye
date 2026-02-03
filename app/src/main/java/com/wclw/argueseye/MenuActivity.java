package com.wclw.argueseye;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.card.MaterialCardView;
import com.wclw.argueseye.services.BlockList;

public class MenuActivity extends AppCompatActivity {

    private MaterialCardView btn_settings;
    private MaterialCardView btn_exit;
    private MaterialCardView btn_blockList;
    private MaterialCardView btn_about;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btn_settings = findViewById(R.id.btn_settings);
        btn_exit = findViewById(R.id.btn_exit);
        btn_blockList = findViewById(R.id.btn_blocklist);
        btn_about = findViewById(R.id.btn_about);



        btn_exit.setOnClickListener(V->{
            finishAffinity();
        });

        btn_blockList.setOnClickListener(V->{
            Intent intent = new Intent(this, BlockList.class);
            startActivity(intent);
        });

        btn_settings.setOnClickListener(V->{
            Intent intent = new Intent(this,AppSettings.class);
            startActivity(intent);
        });

        btn_about.setOnClickListener(V->{
            Intent intent = new Intent(this,AboutActivity.class);
            startActivity(intent);
        });


    }


    public void goToHome(View view){
        Intent homeIntent = new Intent(MenuActivity.this,MainActivity.class);
        startActivity(homeIntent);
    }

}