package com.example.personalizedlearningexperienceapp.adapters;

import android.os.Bundle; // Import Bundle
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation; // Import Navigation
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;
import com.example.personalizedlearningexperienceapp.fragments.QuizAttemptDetailFragment; // Import for ARG_QUIZ_ATTEMPT_ID

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<QuizAttemptEntity> quizAttempts;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // Click listener interface (optional but good practice if you need more complex clicks later)
    public interface OnItemClickListener {
        void onItemClick(QuizAttemptEntity attempt);
    }
    private OnItemClickListener listener; // Remove if not using the interface pattern right now

    // Constructor can be updated if you use the interface
    public HistoryAdapter(List<QuizAttemptEntity> quizAttempts) {
        this.quizAttempts = quizAttempts;
    }

    // Optional: setter for a more decoupled click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        // String scoreText = "Score: " + attempt.correctAnswers + "/" + attempt.totalQuestions; // Kept your original formatting
        // Using string resource for "Score: %1$d/%2$d" format
        String scoreText = holder.itemView.getContext().getString(R.string.history_item_score_format, attempt.correctAnswers, attempt.totalQuestions);
        holder.score.setText(scoreText);

        // Set click listener on the itemView
        holder.itemView.setOnClickListener(view -> {
            // If using the listener interface pattern:
            // if (listener != null) {
            //    listener.onItemClick(attempt);
            // } else {
            // Direct navigation:
            Bundle bundle = new Bundle();
            bundle.putInt(QuizAttemptDetailFragment.ARG_QUIZ_ATTEMPT_ID, attempt.id);
            Navigation.findNavController(view).navigate(
                    R.id.action_historyFragment_to_quizAttemptDetailFragment,
                    bundle
            );
            // }
        });
    }

    @Override
    public int getItemCount() {
        return quizAttempts == null ? 0 : quizAttempts.size();
    }

    public void updateData(List<QuizAttemptEntity> newAttempts) {
        // Your existing updateData logic is fine.
        // For a more robust update, consider using DiffUtil if lists get very large.
        this.quizAttempts.clear();
        if (newAttempts != null) {
            this.quizAttempts.addAll(newAttempts);
        }
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicName;
        TextView date;
        TextView score;

        ViewHolder(View itemView) {
            super(itemView);
            // Ensure these IDs match your item_quiz_attempt.xml
            topicName = itemView.findViewById(R.id.textViewHistoryItemTopicName);
            date = itemView.findViewById(R.id.textViewHistoryItemDate);
            score = itemView.findViewById(R.id.textViewHistoryItemScore);
        }
    }
}