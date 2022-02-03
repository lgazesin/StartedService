package com.nexis.lab11;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Screen extends AppCompatActivity {

    ProgressBar pBar;
    int progress;

    ImageView appIcon;
    TextView name, rNo, tag;

    Handler handler;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);

        pBar = findViewById(R.id.pBar);
        appIcon = findViewById(R.id.appIcon);

        name = findViewById(R.id.name);
        tag = findViewById(R.id.tag);
        rNo = findViewById(R.id.rNo);

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(Screen.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        };

        startProgressListener();
    }

    @Override
    protected void onStop(){
        super.onStop();
        handler.removeCallbacks(runnable);
    }

    public void startProgressListener() {
        progress = pBar.getProgress();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(progress<80){
                    progress += 1;

                    handler.post((new Runnable() {
                        @Override
                        public void run() {
                            pBar.setProgress(progress);
                            if (progress == 20){
                                name.setText("SE4481");
                            }
                            if (progress == 30){
                                rNo.setText("2021");
                            }
                            if (progress == 40){
                                tag.setText(" ");
                            }
                        }
                    }));
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                handler.postDelayed(runnable, 50);
            }
        }).start();
    }
}
