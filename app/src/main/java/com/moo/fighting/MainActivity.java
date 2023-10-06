package com.moo.fighting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.moo.fighting.Calendar.MainActivity2;
import com.moo.fighting.Walk.Walk;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView community = (ImageView) findViewById(R.id.main_button1);
        ImageView calendar = (ImageView) findViewById(R.id.main_button2);
        ImageView map = (ImageView) findViewById(R.id.main_button3);
        ImageView profile = (ImageView) findViewById(R.id.main_button4);

        community.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), y_communityMain.class);
                startActivity(intent);
            }

        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity2.class);
                startActivity(intent);
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Walk.class);
                startActivity(intent);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), profileactivity.class);
                startActivity(intent);
            }
        });

    }
}