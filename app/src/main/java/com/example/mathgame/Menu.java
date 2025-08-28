package com.example.mathgame;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
 * Menu Activity (Main Menu Screen of the Math Game)
 * -------------------------------------------------
 * - Shows 3 buttons: Addition, Subtraction, Multiplication
 * - Each button opens its respective game activity
 * - Adds button press effects (scaling + wobble animation)
 * - Adds hover effect (extra wobble for supported devices)
 * - The logo (imageView) pulses continuously
 */
public class Menu extends AppCompatActivity {

    // UI elements
    Button addition;
    Button subtraction;
    Button multiplication;
    ImageView imageView;
    ConstraintLayout outside;
    LinearLayout innerside;

    /**
     * --------------------
     * startWobble(View v)
     * --------------------
     * - Starts a "wobble" animation (back-and-forth rotation)
     * - Uses ValueAnimator to rotate view from -5° to +5°
     * - Loops infinitely until stopped
     */
    private void startWobble(View v) {
        // Retrieve existing animator (if already assigned to this view)
        ValueAnimator wobbleAnimator = (ValueAnimator) v.getTag(R.id.wobble_animator_tag);

        if (wobbleAnimator == null) {
            wobbleAnimator = ValueAnimator.ofFloat(-5f, 5f); // small rotation range
            wobbleAnimator.setDuration(100); // quick wobble speed
            wobbleAnimator.setRepeatCount(ValueAnimator.INFINITE);
            wobbleAnimator.setRepeatMode(ValueAnimator.REVERSE);

            // Apply rotation to the view on every frame
            wobbleAnimator.addUpdateListener(animation -> {
                float rotation = (float) animation.getAnimatedValue();
                v.setRotation(rotation);
            });

            // Save animator to view tag (reuse later)
            v.setTag(R.id.wobble_animator_tag, wobbleAnimator);
        }

        if (!wobbleAnimator.isRunning()) {
            wobbleAnimator.start();
        }
    }

    /**
     * --------------------
     * stopWobble(View v)
     * --------------------
     * - Stops wobble animation if running
     * - Resets rotation to 0 (normal state)
     */
    private void stopWobble(View v) {
        ValueAnimator wobbleAnimator = (ValueAnimator) v.getTag(R.id.wobble_animator_tag);
        if (wobbleAnimator != null && wobbleAnimator.isRunning()) {
            wobbleAnimator.cancel();
            v.setRotation(0f);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu);

        // Apply safe-area padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        addition = findViewById(R.id.buttonAdd);
        subtraction = findViewById(R.id.buttonSub);
        multiplication = findViewById(R.id.buttonMulti);
        outside = findViewById(R.id.outerBox);
        innerside = findViewById(R.id.innerBox);
        imageView = findViewById(R.id.imageMathgame);

        // Entry animation for layouts (fade + rise effect)
        Animation quickFadeRise = AnimationUtils.loadAnimation(this, R.anim.fast_fade_rise);
        outside.startAnimation(quickFadeRise);
        innerside.startAnimation(quickFadeRise);

        /**
         * --------------------
         * Addition Button Click
         * --------------------
         * - Disables button to prevent double tap
         * - Plays slide-up animation for smooth transition
         * - Opens AddGame activity after short delay
         */
        addition.setOnClickListener(v -> {
            addition.setEnabled(false); // disable multiple taps

            // Animate screen upwards before switching
            View rootView = findViewById(android.R.id.content);
            Animation slideUp = AnimationUtils.loadAnimation(Menu.this, R.anim.slide_up_and_fade);
            rootView.startAnimation(slideUp);

            new Handler().postDelayed(() -> {
                Intent intent = new Intent(Menu.this, AddGame.class);
                startActivity(intent);
                overridePendingTransition(0, 0); // remove default transition
                finish(); // close menu
            }, 250); // delay matches animation duration
        });

        /**
         * --------------------
         * Subtraction Button Click
         * --------------------
         * - Directly opens Subpage activity
         */
        subtraction.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, Subpage.class);
            startActivity(intent);
            finish();
        });

        /**
         * --------------------
         * Multiplication Button Click
         * --------------------
         * - Directly opens Multipage activity
         */
        multiplication.setOnClickListener(v -> {
            Intent intent = new Intent(Menu.this, Multipage.class);
            startActivity(intent);
            finish();
        });

        /**
         * --------------------
         * Button Interactivity (Touch + Hover)
         * --------------------
         * - ACTION_DOWN → scales up slightly + starts wobble
         * - ACTION_UP / CANCEL → resets scale + stops wobble
         * - HOVER_ENTER → wobble starts
         * - HOVER_EXIT → wobble stops
         * (Hover works only on devices with pointer support e.g., Chromebooks)
         */
        View[] buttons = {addition, subtraction, multiplication};

        for (View btn : buttons) {
            // Touch effect (press feedback)
            btn.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(1.05f).scaleY(1.05f).setDuration(100).start();
                        startWobble(v);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1f).scaleY(1f).rotation(0f).setDuration(150).start();
                        stopWobble(v);
                        break;
                }
                return false; // keep click listener working
            });

            // Hover effect (extra wobble on hover devices)
            btn.setOnHoverListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_HOVER_ENTER:
                        startWobble(v);
                        break;
                    case MotionEvent.ACTION_HOVER_EXIT:
                        stopWobble(v);
                        break;
                }
                return true;
            });
        }

        /**
         * --------------------
         * Logo Animation (Pulse)
         * --------------------
         * - Continuously scales logo in and out
         * - Gives a "breathing" effect to the game image
         */
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(imageView, "scaleX", 1f, 1.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(imageView, "scaleY", 1f, 1.1f);

        // Infinite loop, reverse after each cycle
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);

        AnimatorSet pulseSet = new AnimatorSet();
        pulseSet.setDuration(800); // each pulse lasts 0.8 sec
        pulseSet.playTogether(scaleX, scaleY);
        pulseSet.start();
    }
}
