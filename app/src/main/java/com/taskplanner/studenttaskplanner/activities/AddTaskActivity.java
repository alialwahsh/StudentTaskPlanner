package com.taskplanner.studenttaskplanner.activities;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.taskplanner.studenttaskplanner.R;
import com.taskplanner.studenttaskplanner.database.DatabaseHelper;
import com.taskplanner.studenttaskplanner.models.Task;

import java.util.Calendar;

public class AddTaskActivity extends AppCompatActivity {

    private EditText etTitle, etDescription, etDueDate;
    private Spinner spinnerPriority, spinnerCategory;
    private Button btnSaveTask;
    private DatabaseHelper dbHelper;
    private int currentUserId;
    private int editTaskId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);

        etTitle = findViewById(R.id.etTaskTitle);
        etDescription = findViewById(R.id.etTaskDescription);
        etDueDate = findViewById(R.id.etDueDate);
        spinnerPriority = findViewById(R.id.spinnerPriority);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        btnSaveTask = findViewById(R.id.btnSaveTask);

        // Setup spinners
        ArrayAdapter<String> priorityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"High", "Medium", "Low"});
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default to Medium

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"Assignment", "Exam", "Project", "Lab", "Other"});
        spinnerCategory.setAdapter(categoryAdapter);

        // Date picker
        etDueDate.setOnClickListener(v -> showDatePicker());
        etDueDate.setFocusable(false);

        // Check if editing existing task
        editTaskId = getIntent().getIntExtra("editTaskId", -1);
        if (editTaskId != -1) {
            setTitle("Edit Task");
            loadTaskForEditing();
        } else {
            setTitle("Add New Task");
        }

        btnSaveTask.setOnClickListener(v -> saveTask());
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    etDueDate.setText(date);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void loadTaskForEditing() {
        Task task = dbHelper.getTaskById(editTaskId);
        if (task != null) {
            etTitle.setText(task.getTitle());
            etDescription.setText(task.getDescription());
            etDueDate.setText(task.getDueDate());

            // Set priority spinner
            String[] priorities = {"High", "Medium", "Low"};
            for (int i = 0; i < priorities.length; i++) {
                if (priorities[i].equals(task.getPriority())) {
                    spinnerPriority.setSelection(i);
                    break;
                }
            }

            // Set category spinner
            String[] categories = {"Assignment", "Exam", "Project", "Lab", "Other"};
            for (int i = 0; i < categories.length; i++) {
                if (categories[i].equals(task.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }

            btnSaveTask.setText("Update Task");
        }
    }

    private void saveTask() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String dueDate = etDueDate.getText().toString().trim();
        String priority = spinnerPriority.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a task title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dueDate.isEmpty()) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (editTaskId != -1) {
            // Update existing task
            Task task = dbHelper.getTaskById(editTaskId);
            task.setTitle(title);
            task.setDescription(description);
            task.setDueDate(dueDate);
            task.setPriority(priority);
            task.setCategory(category);
            dbHelper.updateTask(task);
            Toast.makeText(this, "Task updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Create new task
            Task task = new Task(currentUserId, title, description, dueDate, priority, category);
            dbHelper.addTask(task);
            Toast.makeText(this, "Task added successfully", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
