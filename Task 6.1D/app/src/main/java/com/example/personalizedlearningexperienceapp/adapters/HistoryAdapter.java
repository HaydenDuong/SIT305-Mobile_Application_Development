package com.example.personalizedlearningexperienceapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<QuizAttemptEntity> quizAttempts;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HistoryAdapter(List<QuizAttemptEntity> quizAttempts) {
        this.quizAttempts = quizAttempts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_attempt, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizAttemptEntity attempt = quizAttempts.get(position);
        holder.topicName.setText(attempt.topicName);
        holder.date.setText(dateFormat.format(new Date(attempt.timestamp)));
        String scoreText = "Score: " + attempt.correctAnswers + "/" + attempt.totalQuestions;
        holder.score.setText(scoreText);
    }

    @Override
    public int getItemCount() {
        return quizAttempts == null ? 0 : quizAttempts.size();
    }

    public void updateData(List<QuizAttemptEntity> newAttempts) {
        this.quizAttempts.clear();
        if (newAttempts != null) {
            this.quizAttempts.addAll(newAttempts);
        }
        notifyDataSetChanged(); // Consider using DiffUtil for better performance
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicName;
        TextView date;
        TextView score;

        ViewHolder(View itemView) {
            super(itemView);
            topicName = itemView.findViewById(R.id.textViewItemTopicName);
            date = itemView.findViewById(R.id.textViewItemDate);
            score = itemView.findViewById(R.id.textViewItemScore);
        }
    }
} 