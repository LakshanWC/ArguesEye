package com.wclw.argueseye;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

public class AppSettings extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private Spinner protocol_spinner ;
    public boolean isHttp = false;
    private List<String> protocolList = List.of("HTTP","HTTPS");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_app_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        protocol_spinner = findViewById(R.id.spin_protocols);
        initializeSpinner();


    }


    private void initializeSpinner(){
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_dropdown_item,protocolList);

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        protocol_spinner.setAdapter(arrayAdapter);
        protocol_spinner.setOnItemSelectedListener(this);
        protocol_spinner.setSelection(isHttp? 0:1);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        isHttp = "HTTP".equals(item);
        ApplicationSettings.getInstance().setHttpProtocol(this,isHttp);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {}
}