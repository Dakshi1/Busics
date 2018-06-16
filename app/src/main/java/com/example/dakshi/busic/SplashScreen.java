package com.example.dakshi.busic;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashScreen extends AppCompatActivity {


    // Splash screen timer
    private static int SPLASH_TIME_OUT = 3500;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        VideoView view = (VideoView)findViewById(R.id.videoView);
        String path = "android.resource://" + getPackageName() + "/" + R.raw.video_file;
        view.setVideoURI(Uri.parse(path));
        view.start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i;
                if(currentUser==null)
                {
                    i=new Intent(SplashScreen.this, LoginActivity.class);
                }
                else
                    i= new Intent(SplashScreen.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
