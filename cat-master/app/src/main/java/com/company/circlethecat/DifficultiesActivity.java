package com.company.circlethecat;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class DifficultiesActivity extends Activity {

    Button easy;
    Button medium;
    Button hard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_difficulties);

         easy = (Button) findViewById(R.id.easy);
        easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DifficultiesActivity.this, MainActivity.class);
                intent.putExtra("BoardSize", 13);
                startActivity(intent);
            }
        });

         medium = (Button) findViewById(R.id.medium);
        medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DifficultiesActivity.this, MainActivity.class);
                intent.putExtra("BoardSize", 11);
                startActivity(intent);
            }
        });

         hard = (Button) findViewById(R.id.hard);
        hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DifficultiesActivity.this, MainActivity.class);
                intent.putExtra("BoardSize", 9);
                startActivity(intent);
            }
        });
    }
}
