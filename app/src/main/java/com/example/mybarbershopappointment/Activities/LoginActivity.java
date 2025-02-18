package com.example.mybarbershopappointment.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.mybarbershopappointment.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private ProgressBar progressBar;
    private FirebaseAuth auth;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        auth = FirebaseAuth.getInstance();
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        loginButton = findViewById(R.id.loginButton);
        progressBar = findViewById(R.id.progressBar);

        loginButton.setOnClickListener(v -> loginUser());
        TextView registerText = findViewById(R.id.registerText);
        registerText.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        Log.d(TAG, "Attempting login with email: " + email);

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful()) {
                Log.d(TAG, "Login successful");

                FirebaseUser user = auth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "User authenticated, UID: " + user.getUid());
                    checkUserRoleAndNavigate(user.getUid());
                } else {
                    Log.e(TAG, "Login failed: User is null");
                    Toast.makeText(this, "Login failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Login failed", task.getException());
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserRoleAndNavigate(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(userId);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Boolean isAdmin = document.getBoolean("isAdmin");

                    Log.d("LoginActivity", "🔍 User Role -> isAdmin: " + isAdmin);

                    Intent intent;
                    if (Boolean.TRUE.equals(isAdmin)) {
                        Log.d("LoginActivity", "✅ Redirecting to Admin Dashboard");
                        intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
                    } else {
                        Log.d("LoginActivity", "✅ Redirecting to User Dashboard");
                        intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
                    }

                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                } else {
                    Log.e("LoginActivity", "⚠️ No user data found for UID: " + userId);
                    Toast.makeText(LoginActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("LoginActivity", "❌ Firestore error: " + task.getException().getMessage());
                Toast.makeText(LoginActivity.this, "Error loading user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
