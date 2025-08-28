package com.example.mathgame;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Locale;
import java.util.Random;

public class Multipage extends AppCompatActivity {

    // UI Elements
    TextView score, time, life, question;   // Displays score, timer, remaining lives, and current question
    EditText answer;                        // User input for answer
    Button ok, next;                        // Buttons: check answer (ok), go to next question (next)

    // Game logic variables
    Random random = new Random();           // Random number generator
    int number1, number2;                   // Random numbers for the multiplication
    int useranswer, realanswer;             // User’s entered answer and the correct one
    int userscore = 0;                      // Tracks player’s score
    int userLife = 3;                       // Number of lives

    // Control flags
    private boolean isAnswerSubmitted = false;       // Ensures next question isn’t shown until answer given or timer ends
    private boolean hasAnsweredCurrentQuestion = false; // Prevents multiple answers for the same question

    // Timer variables
    private static final long Start_Timer_in_Milis = 10000; // Each round: 10 seconds
    long time_left_in_milis = Start_Timer_in_Milis;
    boolean time_running;
    CountDownTimer timer;

    // Layout containers (used for animations)
    ConstraintLayout outside;
    LinearLayout innerside;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable drawing behind system bars (status/navigation)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addpage);

        // Handle system bar padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Link Java variables with XML layout
        score = findViewById(R.id.textViewScore);
        time = findViewById(R.id.textViewTime);
        life = findViewById(R.id.textViewLife);
        question = findViewById(R.id.textviewQuestion);
        answer = findViewById(R.id.editextAnswer);
        ok = findViewById(R.id.buttonplayagain);
        next = findViewById(R.id.buttonExit);
        outside = findViewById(R.id.outerBox);
        innerside = findViewById(R.id.innerBox);

        // Entrance animation for layouts
        Animation quickFadeRise = AnimationUtils.loadAnimation(this, R.anim.fast_fade_rise);
        outside.startAnimation(quickFadeRise);
        innerside.startAnimation(quickFadeRise);

        // Add ripple + scale animations to buttons
        setupButtonAnimation(ok);
        setupButtonAnimation(next);

        // Start first question
        gameContinue();

        // ✅ Check answer when "OK" is clicked
        ok.setOnClickListener(v -> {
            if (hasAnsweredCurrentQuestion) {
                Toast.makeText(Multipage.this, "You already answered this question!", Toast.LENGTH_SHORT).show();
                return;
            }

            String input = answer.getText().toString();
            if (input.isEmpty()) {
                Toast.makeText(Multipage.this, "Please enter your answer", Toast.LENGTH_SHORT).show();
                return;
            }

            useranswer = Integer.parseInt(input);
            pauseTimer(); // Stop timer once answer is given

            isAnswerSubmitted = true;      // Mark as answered
            hasAnsweredCurrentQuestion = true;

            // Check if answer is correct
            if (useranswer == realanswer) {
                userscore += 10; // Add points
                score.setText("" + userscore);
                question.setText("Correct answer");
            } else {
                userLife -= 1; // Lose a life
                life.setText("" + userLife);
                question.setText("Wrong answer");
            }
        });

        // ✅ Go to next question when "NEXT" is clicked
        next.setOnClickListener(v -> {
            if (!isAnswerSubmitted) {
                Toast.makeText(Multipage.this, "Answer the question or wait for the timer!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Reset input + flags for next question
            answer.setText("");
            resetTimer();
            isAnswerSubmitted = false;
            hasAnsweredCurrentQuestion = false;

            if (userLife <= 0) {
                // Game over → move to result screen
                next.setEnabled(false);

                // Animate screen transition
                View rootView = findViewById(android.R.id.content);
                Animation slideUp = AnimationUtils.loadAnimation(Multipage.this, R.anim.slide_up_and_fade);
                rootView.startAnimation(slideUp);

                new Handler().postDelayed(() -> {
                    Intent intent = new Intent(Multipage.this, Result.class);
                    intent.putExtra("score", userscore); // Pass score to results screen
                    startActivity(intent);
                    overridePendingTransition(0, 0); // No transition animation
                    finish();
                }, 250); // Match animation duration
            } else {
                gameContinue(); // Continue game if lives left
            }
        });
    }

    // 🔹 Adds press animation to buttons
    private void setupButtonAnimation(View button) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_click);
        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(scaleAnimation);
            }
            return false; // Allows normal ripple & click effect
        });
    }

    // 🔹 Generate new multiplication question
    public void gameContinue() {
        number1 = random.nextInt(100); // Random number (0–99)
        number2 = random.nextInt(100);

        realanswer = number1 * number2; // Correct result
        question.setText(number1 + " x " + number2); // Show question

        startTimer(); // Start countdown
    }

    // 🔹 Start 10-second countdown timer
    public void startTimer() {
        timer = new CountDownTimer(time_left_in_milis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left_in_milis = millisUntilFinished;
                updateText(); // Update timer display
            }

            @Override
            public void onFinish() {
                time_running = false;
                pauseTimer();
                resetTimer();
                updateText();

                // Penalize player for timeout
                userLife -= 1;
                life.setText("" + userLife);
                question.setText("Sorry! Time is up!");

                // Mark as answered (so player can go to next question)
                isAnswerSubmitted = true;
                hasAnsweredCurrentQuestion = true;
            }
        }.start();

        time_running = true;
    }

    // 🔹 Update timer display
    public void updateText() {
        int second = (int) (time_left_in_milis / 1000) % 60;
        String time_left = String.format(Locale.getDefault(), "%02d", second);
        time.setText(time_left);
    }

    // 🔹 Pause countdown
    public void pauseTimer() {
        timer.cancel();
        time_running = false;
    }

    // 🔹 Reset timer back to 10 seconds
    public void resetTimer() {
        time_left_in_milis = Start_Timer_in_Milis;
        updateText();
    }
}
