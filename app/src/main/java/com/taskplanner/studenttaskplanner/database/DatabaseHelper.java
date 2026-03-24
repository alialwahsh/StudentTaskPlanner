package com.taskplanner.studenttaskplanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.taskplanner.studenttaskplanner.models.Task;
import com.taskplanner.studenttaskplanner.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "StudentTaskPlanner.db";
    private static final int DATABASE_VERSION = 1;

    // User table
    private static final String TABLE_USERS = "users";
    private static final String COL_USER_ID = "id";
    private static final String COL_USER_FULLNAME = "full_name";
    private static final String COL_USER_EMAIL = "email";
    private static final String COL_USER_PASSWORD = "password";

    // Task table
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "id";
    private static final String COL_TASK_USER_ID = "user_id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_DESCRIPTION = "description";
    private static final String COL_TASK_DUE_DATE = "due_date";
    private static final String COL_TASK_PRIORITY = "priority";
    private static final String COL_TASK_CATEGORY = "category";
    private static final String COL_TASK_COMPLETED = "is_completed";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " ("
                + COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_USER_FULLNAME + " TEXT NOT NULL, "
                + COL_USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                + COL_USER_PASSWORD + " TEXT NOT NULL)";

        String createTasksTable = "CREATE TABLE " + TABLE_TASKS + " ("
                + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TASK_USER_ID + " INTEGER NOT NULL, "
                + COL_TASK_TITLE + " TEXT NOT NULL, "
                + COL_TASK_DESCRIPTION + " TEXT, "
                + COL_TASK_DUE_DATE + " TEXT, "
                + COL_TASK_PRIORITY + " TEXT DEFAULT 'Medium', "
                + COL_TASK_CATEGORY + " TEXT DEFAULT 'Other', "
                + COL_TASK_COMPLETED + " INTEGER DEFAULT 0, "
                + "FOREIGN KEY(" + COL_TASK_USER_ID + ") REFERENCES "
                + TABLE_USERS + "(" + COL_USER_ID + "))";

        db.execSQL(createUsersTable);
        db.execSQL(createTasksTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // ==================== User Operations ====================

    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_FULLNAME, user.getFullName());
        values.put(COL_USER_EMAIL, user.getEmail());
        values.put(COL_USER_PASSWORD, user.getPassword());
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result;
    }

    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + "=? AND " + COL_USER_PASSWORD + "=?",
                new String[]{email, password}, null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULLNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_PASSWORD)));
        }
        cursor.close();
        db.close();
        return user;
    }

    public boolean isEmailRegistered(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_EMAIL + "=?", new String[]{email},
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COL_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, null);

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_USER_ID)));
            user.setFullName(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_FULLNAME)));
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_EMAIL)));
        }
        cursor.close();
        db.close();
        return user;
    }

    public int updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_USER_FULLNAME, user.getFullName());
        values.put(COL_USER_EMAIL, user.getEmail());
        int rows = db.update(TABLE_USERS, values,
                COL_USER_ID + "=?", new String[]{String.valueOf(user.getId())});
        db.close();
        return rows;
    }

    // ==================== Task Operations ====================

    public long addTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_USER_ID, task.getUserId());
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_DESCRIPTION, task.getDescription());
        values.put(COL_TASK_DUE_DATE, task.getDueDate());
        values.put(COL_TASK_PRIORITY, task.getPriority());
        values.put(COL_TASK_CATEGORY, task.getCategory());
        values.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
        long result = db.insert(TABLE_TASKS, null, values);
        db.close();
        return result;
    }

    public List<Task> getTasksByUserId(int userId) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_TASK_USER_ID + "=?", new String[]{String.valueOf(userId)},
                null, null, COL_TASK_DUE_DATE + " ASC");

        while (cursor.moveToNext()) {
            Task task = cursorToTask(cursor);
            taskList.add(task);
        }
        cursor.close();
        db.close();
        return taskList;
    }

    public Task getTaskById(int taskId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TASKS, null,
                COL_TASK_ID + "=?", new String[]{String.valueOf(taskId)},
                null, null, null);

        Task task = null;
        if (cursor.moveToFirst()) {
            task = cursorToTask(cursor);
        }
        cursor.close();
        db.close();
        return task;
    }

    public int updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_DESCRIPTION, task.getDescription());
        values.put(COL_TASK_DUE_DATE, task.getDueDate());
        values.put(COL_TASK_PRIORITY, task.getPriority());
        values.put(COL_TASK_CATEGORY, task.getCategory());
        values.put(COL_TASK_COMPLETED, task.isCompleted() ? 1 : 0);
        int rows = db.update(TABLE_TASKS, values,
                COL_TASK_ID + "=?", new String[]{String.valueOf(task.getId())});
        db.close();
        return rows;
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + "=?",
                new String[]{String.valueOf(taskId)});
        db.close();
    }

    public void toggleTaskCompleted(int taskId, boolean completed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_COMPLETED, completed ? 1 : 0);
        db.update(TABLE_TASKS, values,
                COL_TASK_ID + "=?", new String[]{String.valueOf(taskId)});
        db.close();
    }

    public int getTaskCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_TASK_USER_ID + "=?", new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    public int getCompletedTaskCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_TASKS
                + " WHERE " + COL_TASK_USER_ID + "=? AND " + COL_TASK_COMPLETED + "=1",
                new String[]{String.valueOf(userId)});
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return count;
    }

    private Task cursorToTask(Cursor cursor) {
        Task task = new Task();
        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_ID)));
        task.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_USER_ID)));
        task.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_TITLE)));
        task.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DESCRIPTION)));
        task.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_DUE_DATE)));
        task.setPriority(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_PRIORITY)));
        task.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_TASK_CATEGORY)));
        task.setCompleted(cursor.getInt(cursor.getColumnIndexOrThrow(COL_TASK_COMPLETED)) == 1);
        return task;
    }
}
