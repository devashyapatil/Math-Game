package com.example.mathgame;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * MainActivity (Landing Screen of the Math Game)
 * --------------------------------------------------
 * - Shows the game logo/image with a pulsing animation
 * - Fades and bounces the "Start" button to make it interactive
 * - Handles button touch effects (bounce-in & bounce-out)
 * - Starts the Menu activity with a slide-up transition
 */
public class MainActivity extends AppCompatActivity {

    // UI elements
    Button start;            // Start button for beginning the game
    ImageView imageView;     // Logo / Game image

    ConstraintLayout outside; // Outer container (background layout)
    LinearLayout innerside;   // Inner container (foreground box)

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // Enables fullscreen immersive UI
        setContentView(R.layout.activity_main);

        // Handle safe-area padding for status/navigation bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI references
        outside = findViewById(R.id.outerBox);
        innerside = findViewById(R.id.innerBox);
        imageView = findViewById(R.id.imageMathgame);
        start = findViewById(R.id.Start);

        // Load "fade-rise" animation (used on entry)
        Animation quickFadeRise = AnimationUtils.loadAnimation(this, R.anim.fast_fade_rise);

        // Run entry animation on both containers
        outside.startAnimation(quickFadeRise);
        innerside.startAnimation(quickFadeRise);

        /**
         * --------------------
         * ImageView Animation
         * --------------------
         * Creates a slow, continuous "pulse" effect by scaling
         * the image slightly up & down repeatedly.
         */
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.06f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.06f);

        scaleX.setDuration(2000); // 2 seconds
        scaleY.setDuration(2000);

        // Loop forever in reverse (grow → shrink → grow)
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        // Play both animations together (X and Y)
        AnimatorSet slowPulse = new AnimatorSet();
        slowPulse.playTogether(scaleX, scaleY);
        slowPulse.start();

        /**
         * --------------------
         * Start Button Animation
         * --------------------
         * 1. First, fade-rise animation (entry effect)
         * 2. Then, switch to continuous bounce-loop after delay
         */
        start.startAnimation(quickFadeRise);

        // After fade-rise ends, start bouncing animation
        new Handler().postDelayed(() -> {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce_loop);
            start.startAnimation(bounce);
        }, quickFadeRise.getDuration() + 100); // small buffer after fade

        /**
         * --------------------
         * Start Button Click
         * --------------------
         * - Disables multiple taps
         * - Plays "slide-up & fade" animation on screen
         * - Switches to Menu activity after animation ends
         */
        start.setOnClickListener(v -> {
            start.setEnabled(false); // prevent double tap

            // Animate screen upwards
            View rootView = findViewById(android.R.id.content);
            Animation slideUp = AnimationUtils.loadAnimation(MainActivity.this, R.anim.slide_up_and_fade);
            rootView.startAnimation(slideUp);

            // Switch activity after animation delay
            new Handler().postDelayed(() -> {
                Intent intent = new Intent(MainActivity.this, Menu.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // no default Android transition
                finish(); // close this activity
            }, 250); // match animation duration
        });

        /**
         * --------------------
         * Button Touch Effect
         * --------------------
         * - ACTION_DOWN: Scales button up (bounce)
         * - ACTION_UP / CANCEL: Restores to normal size
         * - Improves "press feedback" for better UX
         */
        start.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate()
                            .scaleX(1.1f)   // grow slightly
                            .scaleY(1.1f)
                            .translationZ(12f) // lift shadow
                            .setDuration(150)
                            .setInterpolator(new OvershootInterpolator()) // smooth bounce
                            .start();
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate()
                            .scaleX(1f)    // restore
                            .scaleY(1f)
                            .translationZ(0f)
                            .setDuration(150)
                            .setInterpolator(new DecelerateInterpolator()) // natural return
                            .start();
                    break;
            }
            return false; // return false so normal click still works
        });
    }
}
