package com.taskplanner.studenttaskplanner.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.taskplanner.studenttaskplanner.R;
import com.taskplanner.studenttaskplanner.adapters.TaskAdapter;
import com.taskplanner.studenttaskplanner.database.DatabaseHelper;
import com.taskplanner.studenttaskplanner.models.Task;

import java.util.List;

public class TaskListActivity extends AppCompatActivity implements TaskAdapter.OnTaskClickListener {

    private static final String TAG = "TaskListActivity";
    private static final String CHANNEL_ID = "task_channel";

    private RecyclerView recyclerView;
    private TaskAdapter taskAdapter;
    private DatabaseHelper dbHelper;
    private TextView tvEmptyMessage, tvWelcome;
    private FloatingActionButton fabAddTask;
    private int currentUserId;

    // BroadcastReceiver to listen for task updates
    private BroadcastReceiver taskUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "Received task update broadcast");
            loadTasks();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);
        Log.d(TAG, "onCreate called");

        // Set up toolbar so the options menu shows
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Tasks");
        }

        SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
        currentUserId = prefs.getInt("userId", -1);
        String userName = prefs.getString("userName", "Student");

        if (currentUserId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        dbHelper = new DatabaseHelper(this);

        tvWelcome = findViewById(R.id.tvWelcome);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);
        recyclerView = findViewById(R.id.recyclerViewTasks);
        fabAddTask = findViewById(R.id.fabAddTask);

        tvWelcome.setText("Hello, " + userName + "!");

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddTask.setOnClickListener(v -> {
            Intent intent = new Intent(TaskListActivity.this, AddTaskActivity.class);
            startActivity(intent);
        });

        // Create notification channel
        createNotificationChannel();

        // Register broadcast receiver for task updates
        IntentFilter filter = new IntentFilter("com.taskplanner.TASK_UPDATED");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(taskUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(taskUpdateReceiver, filter);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume called");
        loadTasks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause called");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy called");
        unregisterReceiver(taskUpdateReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("userId", currentUserId);
        Log.d(TAG, "onSaveInstanceState: saving userId");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        currentUserId = savedInstanceState.getInt("userId", -1);
        Log.d(TAG, "onRestoreInstanceState: restored userId");
    }

    private void loadTasks() {
        List<Task> tasks = dbHelper.getTasksByUserId(currentUserId);

        if (tasks.isEmpty()) {
            tvEmptyMessage.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmptyMessage.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            taskAdapter = new TaskAdapter(this, tasks, this);
            recyclerView.setAdapter(taskAdapter);
        }

        // Show notification if there are pending tasks
        checkPendingTasks(tasks);
    }

    private void checkPendingTasks(List<Task> tasks) {
        int pendingCount = 0;
        for (Task task : tasks) {
            if (!task.isCompleted()) {
                pendingCount++;
            }
        }

        if (pendingCount > 0) {
            showTaskNotification(pendingCount);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for pending tasks");
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private void showTaskNotification(int pendingCount) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Student Task Planner")
                .setContentText("You have " + pendingCount + " pending task(s)")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(1, builder.build());
    }

    @Override
    public void onTaskClick(Task task) {
        Intent intent = new Intent(this, TaskDetailActivity.class);
        intent.putExtra("taskId", task.getId());
        startActivity(intent);
    }

    @Override
    public void onTaskCheckChanged(Task task, boolean isChecked) {
        dbHelper.toggleTaskCompleted(task.getId(), isChecked);

        // Send broadcast that task was updated
        Intent broadcastIntent = new Intent("com.taskplanner.TASK_UPDATED");
        sendBroadcast(broadcastIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            SharedPreferences prefs = getSharedPreferences("UserSession", MODE_PRIVATE);
            prefs.edit().clear().apply();
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        } else if (id == R.id.action_share) {
            // Implicit intent to share app info
            shareAppInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Implicit Intent - share app info via other apps
    private void shareAppInfo() {
        int totalTasks = dbHelper.getTaskCount(currentUserId);
        int completedTasks = dbHelper.getCompletedTaskCount(currentUserId);

        String shareText = "I'm using Student Task Planner to manage my academic tasks!\n"
                + "Progress: " + completedTasks + "/" + totalTasks + " tasks completed.";

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Student Task Planner");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        startActivity(Intent.createChooser(shareIntent, "Share via"));
    }
}
