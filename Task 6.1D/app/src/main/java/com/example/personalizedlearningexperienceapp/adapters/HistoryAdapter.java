package com.example.personalizedlearningexperienceapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.QuizAttemptEntity;
import com.example.personalizedlearningexperienceapp.fragments.QuizAttemptDetailFragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<QuizAttemptEntity> quizAttempts;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    private final Context context;

    public interface OnDeleteInteractionListener {
        void onDeleteAttemptClicked(QuizAttemptEntity attempt);
    }
    private OnDeleteInteractionListener deleteListener;

    public HistoryAdapter(Context context, List<QuizAttemptEntity> quizAttempts, OnDeleteInteractionListener deleteListener) {
        this.context = context;
        this.quizAttempts = quizAttempts;
        this.deleteListener = deleteListener;
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
        String scoreText = holder.itemView.getContext().getString(R.string.history_item_score_format, attempt.correctAnswers, attempt.totalQuestions);
        holder.score.setText(scoreText);

        // Set click listener on the itemView
        holder.itemView.setOnClickListener(view -> {
            Bundle bundle = new Bundle();
            bundle.putInt(QuizAttemptDetailFragment.ARG_QUIZ_ATTEMPT_ID, attempt.id);
            Navigation.findNavController(view).navigate(
                    R.id.action_historyFragment_to_quizAttemptDetailFragment,
                    bundle
            );
        });

        // Click listener for the delete button
        holder.deleteButton.setOnClickListener(view -> {
            if (deleteListener != null) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete Quiz Attempt")
                        .setMessage("Are you sure you want to delete this quiz attempt? This action cannot be undone.")
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            deleteListener.onDeleteAttemptClicked(attempt);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(R.drawable.ic_delete)
                        .show();
            }
        });
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
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView topicName;
        TextView date;
        TextView score;
        ImageButton deleteButton;

        ViewHolder(View itemView) {
            super(itemView);

            topicName = itemView.findViewById(R.id.textViewHistoryItemTopic);
            date = itemView.findViewById(R.id.textViewHistoryItemDate);
            score = itemView.findViewById(R.id.textViewHistoryItemScore);
            deleteButton = itemView.findViewById(R.id.imageButtonDeleteAttempt);
        }
    }
}