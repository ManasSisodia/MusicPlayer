package com.example.manas.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {
    private static int SPLASH_TIMER = 2000;

    ImageView ssImage;
    TextView ssText;
    Animation imageAnim,textAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        ssImage = findViewById(R.id.animation_image);
        ssText = findViewById(R.id.animation_text);

       imageAnim = AnimationUtils.loadAnimation(this,R.anim.side_anim);
       textAnim = AnimationUtils.loadAnimation(this,R.anim.text_anim);

       ssImage.setAnimation(imageAnim);
       ssText.setAnimation(textAnim);

       new Handler().postDelayed(new Runnable() {
           @Override
           public void run() {

               Intent intent = new Intent(SplashScreen.this,MainActivity.class);
               startActivity(intent);
               finish();
           }
       },SPLASH_TIMER);
    }
}