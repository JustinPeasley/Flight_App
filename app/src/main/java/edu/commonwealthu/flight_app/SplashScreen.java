package edu.commonwealthu.flight_app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MotionLayout motionLayout = findViewById(R.id.splashLayout);
        motionLayout.transitionToEnd();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Navigate to MainActivity after delay
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(intent);
                finish(); // Finish SplashScreen so it doesn't come back in the stack
            }
        }, 3000); // Show splash for 3 seconds
    }
}