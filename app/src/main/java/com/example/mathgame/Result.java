package com.example.mathgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Result Activity
 * ----------------
 * This activity shows the final score after finishing the Math Game.
 * It provides two options to the user:
 *  - Play Again → Go back to the Menu screen.
 *  - Exit → Close the activity (end the game session).
 *
 * Includes simple animations for better UI transitions.
 */
public class Result extends AppCompatActivity {

    // UI Components
    TextView result;          // Displays final score
    Button playagain;         // Button to restart game (go to Menu)
    Button exit;              // Button to exit app
    ConstraintLayout outside; // Outer container layout (for animation)
    LinearLayout innerside;   // Inner container layout (for animation)

    // Game-related variable
    int score; // Stores the score received from previous activity

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables edge-to-edge drawing for UI
        setContentView(R.layout.activity_result);

        // ✅ Handle system window insets (status bar, navigation bar padding)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 🔹 Linking UI elements with layout components
        result = findViewById(R.id.textViewResult);
        playagain = findViewById(R.id.buttonplayagain);
        exit = findViewById(R.id.buttonExit);
        outside = findViewById(R.id.outerBox);
        innerside = findViewById(R.id.innerBox);

        // 🔹 Load animations and apply them
        Animation quickFadeRise = AnimationUtils.loadAnimation(this, R.anim.fast_fade_rise);
        outside.startAnimation(quickFadeRise);
        innerside.startAnimation(quickFadeRise);

        // 🔹 Get score from Intent (sent by Game/Quiz activity)
        Intent intent = getIntent();
        score = intent.getIntExtra("score", 0); // default = 0
        result.setText(String.valueOf(score)); // Show score in TextView

        // 🔹 Play Again button functionality
        playagain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playagain.setEnabled(false); // Prevent multiple clicks

                // Animate the whole screen sliding upwards & fading
                View rootView = findViewById(android.R.id.content);
                Animation slideUp = AnimationUtils.loadAnimation(Result.this, R.anim.slide_up_and_fade);
                rootView.startAnimation(slideUp);

                // After animation delay → open Menu activity
                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(Result.this, Menu.class);
                    startActivity(intent);
                    overridePendingTransition(0, 0); // Remove default transition
                    finish(); // Close Result activity
                }, 250); // Delay matches animation duration
            }
        });

        // 🔹 Exit button functionality → just finish() the activity
        exit.setOnClickListener(v -> finish());
    }
}
