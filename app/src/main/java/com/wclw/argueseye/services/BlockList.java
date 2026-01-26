package com.wclw.argueseye.services;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.wclw.argueseye.R;
import com.wclw.argueseye.helpers.DatabaseHelper;

public class BlockList extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_block_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseHelper = DatabaseHelper.getInstance(this);
        container = findViewById(R.id.table_details_container);
        loadDbdata();
    }

    //Load Data from the BlockedList table
    private void loadDbdata(){
        try {
            Cursor cursor = databaseHelper.getBlockList();

            if (cursor == null || cursor.getCount() == 0) {
                TextView empty = new TextView(this);
                empty.setText("No blocked Urls");
                container.addView(empty);
                Log.d("BlockList","Nothing");
                return;
            }

            while (cursor.moveToNext()) {
                String url = cursor.getString(
                        cursor.getColumnIndexOrThrow("url")
                );

                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow("date")
                );


                // Card-like container
                LinearLayout card = new LinearLayout(this);
                card.setOrientation(LinearLayout.VERTICAL);
                card.setPadding(24, 24, 24, 24);

                // URL text
                TextView tvUrl = new TextView(this);
                tvUrl.setText(url);
                tvUrl.setTextSize(16);
                tvUrl.setTypeface(null, Typeface.BOLD);

                // Date text
                TextView tvDate = new TextView(this);
                tvDate.setText("Blocked on: " + date);
                tvDate.setTextSize(12);

                card.addView(tvUrl);
                card.addView(tvDate);

                container.addView(card);
            }
            cursor.close();

        }catch (Exception e){
            Log.d("BlockList","error" + e.getMessage());
        }
    }
}