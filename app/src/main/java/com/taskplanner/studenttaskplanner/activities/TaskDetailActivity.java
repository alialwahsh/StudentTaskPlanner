package com.taskplanner.studenttaskplanner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.taskplanner.studenttaskplanner.R;
import com.taskplanner.studenttaskplanner.database.DatabaseHelper;
import com.taskplanner.studenttaskplanner.models.Task;

public class TaskDetailActivity extends AppCompatActivity {

    private TextView tvDetailTitle, tvDetailDescription, tvDetailDueDate,
            tvDetailPriority, tvDetailCategory, tvDetailStatus;
    private Button btnEdit, btnDelete, btnToggleStatus;
    private DatabaseHelper dbHelper;
    private int taskId;
    private Task currentTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Task Details");
        }

        dbHelper = new DatabaseHelper(this);
        taskId = getIntent().getIntExtra("taskId", -1);

        tvDetailTitle = findViewById(R.id.tvDetailTitle);
        tvDetailDescription = findViewById(R.id.tvDetailDescription);
        tvDetailDueDate = findViewById(R.id.tvDetailDueDate);
        tvDetailPriority = findViewById(R.id.tvDetailPriority);
        tvDetailCategory = findViewById(R.id.tvDetailCategory);
        tvDetailStatus = findViewById(R.id.tvDetailStatus);
        btnEdit = findViewById(R.id.btnEditTask);
        btnDelete = findViewById(R.id.btnDeleteTask);
        btnToggleStatus = findViewById(R.id.btnToggleStatus);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddTaskActivity.class);
            intent.putExtra("editTaskId", taskId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());

        btnToggleStatus.setOnClickListener(v -> {
            if (currentTask != null) {
                boolean newStatus = !currentTask.isCompleted();
                dbHelper.toggleTaskCompleted(taskId, newStatus);
                currentTask.setCompleted(newStatus);
                updateStatusUI();
                Toast.makeText(this, newStatus ? "Task marked as completed" : "Task marked as pending",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTaskDetails();
    }

    private void loadTaskDetails() {
        currentTask = dbHelper.getTaskById(taskId);

        if (currentTask == null) {
            Toast.makeText(this, "Task not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvDetailTitle.setText(currentTask.getTitle());
        tvDetailDescription.setText(currentTask.getDescription() != null && !currentTask.getDescription().isEmpty()
                ? currentTask.getDescription() : "No description provided");
        tvDetailDueDate.setText("Due: " + currentTask.getDueDate());
        tvDetailPriority.setText("Priority: " + currentTask.getPriority());
        tvDetailCategory.setText("Category: " + currentTask.getCategory());
        updateStatusUI();
    }

    private void updateStatusUI() {
        if (currentTask.isCompleted()) {
            tvDetailStatus.setText("Status: Completed");
            tvDetailStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            btnToggleStatus.setText("Mark as Pending");
        } else {
            tvDetailStatus.setText("Status: Pending");
            tvDetailStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            btnToggleStatus.setText("Mark as Completed");
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Task")
                .setMessage("Are you sure you want to delete this task?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.deleteTask(taskId);
                    Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
