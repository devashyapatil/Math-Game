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

public class AddGame extends AppCompatActivity {

    // UI elements
    TextView score, time, life, question;
    EditText answer;
    Button ok, next;
    ConstraintLayout outside;
    LinearLayout innerside;

    // Game variables
    Random random = new Random();
    int number1, number2;       // two numbers in question
    int useranswer, realanswer; // player’s answer vs correct answer
    int userscore = 0;          // tracks score
    int userLife = 3;           // lives remaining

    // State flags
    private boolean isAnswerSubmitted = false;     // true if OK was pressed or time ran out
    private boolean hasAnsweredCurrentQuestion = false; // prevents answering same question multiple times

    // Timer variables
    private static final long Start_Timer_in_Milis = 10000; // 10 seconds per question
    long time_left_in_milis = Start_Timer_in_Milis;
    boolean time_running;
    CountDownTimer timer;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_addpage);

        // ✅ Handle system UI padding (status bar, navigation bar)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Linking UI with XML
        score = findViewById(R.id.textViewScore);
        time = findViewById(R.id.textViewTime);
        life = findViewById(R.id.textViewLife);
        question = findViewById(R.id.textviewQuestion);
        answer = findViewById(R.id.editextAnswer);
        ok = findViewById(R.id.buttonplayagain);
        next = findViewById(R.id.buttonExit);
        outside = findViewById(R.id.outerBox);
        innerside = findViewById(R.id.innerBox);

        // ✅ Entry animation for UI
        Animation quickFadeRise = AnimationUtils.loadAnimation(this, R.anim.fast_fade_rise);
        outside.startAnimation(quickFadeRise);
        innerside.startAnimation(quickFadeRise);

        // ✅ Apply ripple + scale animation when pressing buttons
        setupButtonAnimation(ok);
        setupButtonAnimation(next);

        // ✅ Start first question
        gameContinue();

        // =========================
        // "OK" Button (Submit Answer)
        // =========================
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Prevent answering the same question twice
                if (hasAnsweredCurrentQuestion) {
                    Toast.makeText(AddGame.this, "You already answered this question!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate input
                String input = answer.getText().toString();
                if (input.isEmpty()) {
                    Toast.makeText(AddGame.this, "Please enter your answer", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Parse user’s answer
                useranswer = Integer.parseInt(input);

                // Stop timer when answer is submitted
                pauseTimer();

                // Mark state flags
                isAnswerSubmitted = true;
                hasAnsweredCurrentQuestion = true;

                // Check correctness
                if(useranswer == realanswer){
                    userscore += 10; // +10 points
                    score.setText("" + userscore);
                    question.setText("Correct answer");
                }
                else{
                    userLife -= 1; // lose one life
                    life.setText("" + userLife);
                    question.setText("Wrong answer");
                }
            }
        });

        // =========================
        // "NEXT" Button (Go to next question OR End game)
        // =========================
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Prevent skipping without answering
                if (!isAnswerSubmitted) {
                    Toast.makeText(AddGame.this, "Answer the question or wait for the timer!", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Reset input
                answer.setText("");
                resetTimer();

                // Reset state for new question
                isAnswerSubmitted = false;
                hasAnsweredCurrentQuestion = false;

                // If no lives left → go to Result screen
                if(userLife <= 0){

                    next.setEnabled(false);

                    // Animate screen transition
                    View rootView = findViewById(android.R.id.content);
                    Animation slideUp = AnimationUtils.loadAnimation(AddGame.this, R.anim.slide_up_and_fade);
                    rootView.startAnimation(slideUp);

                    // Delay transition until animation ends
                    new Handler().postDelayed(() -> {
                        Intent intent = new Intent(AddGame.this, Result.class);
                        intent.putExtra("score", userscore); // pass final score
                        startActivity(intent);
                        overridePendingTransition(0, 0);
                        finish();
                    }, 250);

                }
                else{
                    // Continue with new math problem
                    gameContinue();
                }
            }
        });
    }

    // =========================
    // Apply scaling animation when button pressed
    // =========================
    private void setupButtonAnimation(View button) {
        Animation scaleAnimation = AnimationUtils.loadAnimation(this, R.anim.scale_click);

        button.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                v.startAnimation(scaleAnimation);
            }
            return false; // Let ripple & click work normally
        });
    }

    // =========================
    // Generate a new math problem
    // =========================
    public void gameContinue(){
        number1 = random.nextInt(100); // 0–99
        number2 = random.nextInt(100);

        realanswer = number1 + number2; // correct answer

        question.setText(number1 + " + " + number2);

        startTimer();
    }

    // =========================
    // Start countdown timer
    // =========================
    public void startTimer(){
        timer = new CountDownTimer(time_left_in_milis,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                time_left_in_milis = millisUntilFinished;
                updateText(); // update UI each second
            }

            @Override
            public void onFinish() {
                // Timer finished
                time_running = false;
                pauseTimer();
                resetTimer();
                updateText();

                // Lose one life
                userLife -= 1;
                life.setText("" + userLife);
                question.setText("Sorry! Time is up!");

                // Mark state as answered (so NEXT can be pressed)
                isAnswerSubmitted = true;
                hasAnsweredCurrentQuestion = true;
            }

        }.start();

        time_running = true;
    }

    // =========================
    // Update timer text display
    // =========================
    public void updateText(){
        int second = (int)(time_left_in_milis / 1000) % 60;
        String time_left = String.format(Locale.getDefault(), "%02d", second);
        time.setText(time_left);
    }

    // =========================
    // Pause the timer
    // =========================
    public void pauseTimer(){
        timer.cancel();
        time_running = false;
    }

    // =========================
    // Reset timer back to 10s
    // =========================
    public void resetTimer(){
        time_left_in_milis = Start_Timer_in_Milis;
        updateText();
    }
}
