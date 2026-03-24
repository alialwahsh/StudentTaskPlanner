package com.taskplanner.studenttaskplanner.adapters;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.taskplanner.studenttaskplanner.R;
import com.taskplanner.studenttaskplanner.models.Task;

import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private Context context;
    private List<Task> taskList;
    private OnTaskClickListener listener;

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
        void onTaskCheckChanged(Task task, boolean isChecked);
    }

    public TaskAdapter(Context context, List<Task> taskList, OnTaskClickListener listener) {
        this.context = context;
        this.taskList = taskList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTaskTitle.setText(task.getTitle());
        holder.tvTaskDueDate.setText("Due: " + task.getDueDate());
        holder.tvTaskCategory.setText(task.getCategory());
        holder.cbCompleted.setChecked(task.isCompleted());

        // Strikethrough if completed
        if (task.isCompleted()) {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.tvTaskTitle.setPaintFlags(holder.tvTaskTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }

        // Priority color indicator
        switch (task.getPriority()) {
            case "High":
                holder.tvPriorityIndicator.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark));
                holder.tvPriorityIndicator.setText("HIGH");
                break;
            case "Medium":
                holder.tvPriorityIndicator.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_orange_dark));
                holder.tvPriorityIndicator.setText("MED");
                break;
            case "Low":
                holder.tvPriorityIndicator.setBackgroundColor(
                        ContextCompat.getColor(context, android.R.color.holo_green_dark));
                holder.tvPriorityIndicator.setText("LOW");
                break;
        }

        holder.cardView.setOnClickListener(v -> listener.onTaskClick(task));

        holder.cbCompleted.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) {
                listener.onTaskCheckChanged(task, isChecked);
                task.setCompleted(isChecked);
                notifyItemChanged(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvTaskTitle, tvTaskDueDate, tvTaskCategory, tvPriorityIndicator;
        CheckBox cbCompleted;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardTask);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvTaskDueDate = itemView.findViewById(R.id.tvTaskDueDate);
            tvTaskCategory = itemView.findViewById(R.id.tvTaskCategory);
            tvPriorityIndicator = itemView.findViewById(R.id.tvPriorityIndicator);
            cbCompleted = itemView.findViewById(R.id.cbCompleted);
        }
    }
}
