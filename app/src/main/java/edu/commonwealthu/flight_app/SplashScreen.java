package edu.commonwealthu.flight_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);

        MotionLayout motionLayout = findViewById(R.id.splashLayout);
        if (motionLayout != null) {
            motionLayout.transitionToEnd();
        } else {
            Log.e("SplashScreen", "MotionLayout reference is null!");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Navigate to MainActivity after delay
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
                finish(); // Finish SplashScreen so it doesn't come back in the stack
            }
        }, 2000); // Show splash for 3 seconds
    }
}