package com.taskplanner.studenttaskplanner.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.taskplanner.studenttaskplanner.R;
import com.taskplanner.studenttaskplanner.database.DatabaseHelper;
import com.taskplanner.studenttaskplanner.models.User;

public class ProfileActivity extends AppCompatActivity {

    private EditText etProfileName, etProfileEmail;
    private TextView tvTotalTasks, tvCompletedTasks, tvPendingTasks;
    private Button btnUpdateProfile;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Profile");
        }

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        etProfileName = findViewById(R.id.etProfileName);
        etProfileEmail = findViewById(R.id.etProfileEmail);
        tvTotalTasks = findViewById(R.id.tvTotalTasks);
        tvCompletedTasks = findViewById(R.id.tvCompletedTasks);
        tvPendingTasks = findViewById(R.id.tvPendingTasks);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);

        loadProfile();
        loadStats();

        btnUpdateProfile.setOnClickListener(v -> updateProfile());
    }

    private void loadProfile() {
        User user = dbHelper.getUserById(currentUserId);
        if (user != null) {
            etProfileName.setText(user.getFullName());
            etProfileEmail.setText(user.getEmail());
        }
    }

    private void loadStats() {
        int total = dbHelper.getTaskCount(currentUserId);
        int completed = dbHelper.getCompletedTaskCount(currentUserId);
        int pending = total - completed;

        tvTotalTasks.setText("Total Tasks: " + total);
        tvCompletedTasks.setText("Completed: " + completed);
        tvPendingTasks.setText("Pending: " + pending);
    }

    private void updateProfile() {
        String name = etProfileName.getText().toString().trim();
        String email = etProfileEmail.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        User user = new User();
        user.setId(currentUserId);
        user.setFullName(name);
        user.setEmail(email);

        int result = dbHelper.updateUser(user);
        if (result > 0) {
            // Update session
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            prefs.edit().putString("userName", name).putString("userEmail", email).apply();
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
