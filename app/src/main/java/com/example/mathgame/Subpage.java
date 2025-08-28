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

public class Subpage extends AppCompatActivity {

    // UI components
    TextView score, time, life, question;
    EditText answer;
    Button ok, next;

    // Random number generator for subtraction problems
    Random random = new Random();

    // Game variables
    int number1, number2, useranswer, realanswer, userscore = 0, userLife = 3;

    // To prevent multiple answers on same question
    private boolean isAnswerSubmitted = false;
    private boolean hasAnsweredCurrentQuestion = false;

    // Timer variables
    private static final long Start_Timer_in_Milis = 10000; // 10 sec per question
    long time_left_in_milis = Start_Timer_in_Milis;
    boolean time_running;
    CountDownTimer timer;

    // Layout containers for animations
    ConstraintLayout outside;
    LinearLayout innerside;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enables edge-to-edge UI
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addpage);

        // Handle system bar insets (safe areas)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        score = findViewById(R.id.textViewScore);
        time = findViewById(R.id.textViewTime);
        life = findViewById(R.id.textViewLife);
        question = findViewById(R.id.textviewQuestion);
        answer = findViewById(R.id.editextAnswer);
        ok = findViewById(R.id.buttonplayagain);
        next = findViewById(R.id.buttonExit);
        outside = findViewById(R.id.outerBox);
        innerside = findViewById(R.id.innerBox);

        // Apply fade-in + rise animation to game container
        Animation quickFadeRise = AnimationUtils.loadAnimation(this, R.anim.fast_fade_rise);
        outside.startAnimation(quickFadeRise);
        innerside.startAnimation(quickFadeRise);

        // Add ripple + scale animation when touching buttons
        setupButtonAnimation(ok);
        setupButtonAnimation(next);

        // Start the first subtraction question
        gameContinue();

        // ✅ OK button → checks user’s answer
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Prevent answering multiple times
                if (hasAnsweredCurrentQuestion) {
                    Toast.makeText(Subpage.this, "You already answered this question!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate input
                String input = answer.getText().toString();
                if (input.isEmpty()) {
                    Toast.makeText(Subpage.this, "Please enter your answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                useranswer = Integer.parseInt(input);

                // Stop timer once user submits
                pauseTimer();

                // Mark this question as answered
                isAnswerSubmitted = true;
                hasAnsweredCurrentQuestion = true;

                // ✅ Check correctness
                if(useranswer==realanswer){
                    userscore += 10; // +10 points for correct answer
                    score.setText("" + userscore);
                    question.setText("Correct answer");
                }
                else{
                    userLife -= 1; // Lose 1 life
                    life.setText("" + userLife);
                    question.setText("Wrong answer");
                }
            }
        });

        // ✅ NEXT button → goes to next question or Result page if game over
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Must answer first (or timeout)
                if (!isAnswerSubmitted) {
                    Toast.makeText(Subpage.this, "Answer the question or wait for the timer!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Reset input field + timer
                answer.setText("");
                resetTimer();
                isAnswerSubmitted = false;
                hasAnsweredCurrentQuestion = false;

                // ✅ Game Over condition
                if(userLife <= 0){

                    next.setEnabled(false); // Prevent spam click

                    // Animate full screen upwards
                    View rootView = findViewById(android.R.id.content);
                    Animation slideUp = AnimationUtils.loadAnimation(Subpage.this, R.anim.slide_up_and_fade);
                    rootView.startAnimation(slideUp);

                    // Delay before moving to result screen
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(Subpage.this,Result.class);
                        intent.putExtra("score",userscore);
                        startActivity(intent);
                        overridePendingTransition(0, 0); // Remove default transition
                        finish();
                    }, 250); // match animation duration

                }
                else{
                    // Continue with new question
                    gameContinue();
                }
            }
        });
    }

    // 🔹 Adds touch animation effect for buttons
    private void setupButtonAnimation(View button) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_click);

        button.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.startAnimation(scaleAnimation); // Play animation when pressed
                    break;
            }
            return false; // Keep ripple + click effect working
        });
    }

    // 🔹 Generates new subtraction question
    public void gameContinue(){
        number1 = random.nextInt(100); // Random number 0-99
        number2 = random.nextInt(100);

        realanswer = number1 - number2; // Correct answer

        question.setText(number1 + " - " + number2); // Show question

        startTimer(); // Start 10-sec countdown
    }

    // 🔹 Starts countdown timer
    public void startTimer(){
        timer = new CountDownTimer(time_left_in_milis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left_in_milis = millisUntilFinished;
                updateText(); // Update UI with remaining time
            }

            @Override
            public void onFinish() {
                // Time’s up → lose 1 life
                time_running = false;
                pauseTimer();
                resetTimer();
                updateText();
                userLife -= 1;
                life.setText("" + userLife);
                question.setText("Sorry! Time is up!");

                // Mark as answered → must press Next
                isAnswerSubmitted = true;
                hasAnsweredCurrentQuestion = true;
            }
        }.start();

        time_running = true;
    }

    // 🔹 Updates timer text on screen
    public void updateText(){
        int second = (int)(time_left_in_milis/1000) % 60;
        String time_left = String.format(Locale.getDefault(),"%02d",second);
        time.setText(time_left);
    }

    // 🔹 Stops timer
    public void pauseTimer(){
        timer.cancel();
        time_running=false;
    }

    // 🔹 Resets timer back to 10 seconds
    public void resetTimer(){
        time_left_in_milis = Start_Timer_in_Milis;
        updateText();
    }
}
