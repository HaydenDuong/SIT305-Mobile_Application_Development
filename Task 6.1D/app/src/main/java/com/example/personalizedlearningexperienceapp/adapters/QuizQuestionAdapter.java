package com.example.personalizedlearningexperienceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.models.QuizQuestion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizQuestionAdapter extends RecyclerView.Adapter<QuizQuestionAdapter.QuizQuestionViewHolder> {

    private final List<QuizQuestion> questions;
    private final LayoutInflater inflater;
    // To store selected answers: Key = question index (position), Value = selected option text
    private final Map<Integer, String> selectedAnswers;

    public QuizQuestionAdapter(Context context, List<QuizQuestion> questions) {
        this.inflater = LayoutInflater.from(context);
        this.questions = questions;
        this.selectedAnswers = new HashMap<>();
    }

    @NonNull
    @Override
    public QuizQuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_quiz_question, parent, false);
        return new QuizQuestionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizQuestionViewHolder holder, int position) {
        QuizQuestion currentQuestion = questions.get(position);
        holder.bind(currentQuestion, position);
    }

    @Override
    public int getItemCount() {
        return questions == null ? 0 : questions.size();
    }

    public Map<Integer, String> getSelectedAnswers() {
        return selectedAnswers;
    }

    class QuizQuestionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewQuestion;
        private final RadioGroup radioGroupOptions;

        public QuizQuestionViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestion = itemView.findViewById(R.id.textViewItemQuestion);
            radioGroupOptions = itemView.findViewById(R.id.radioGroupItemOptions);
        }

        public void bind(final QuizQuestion question, final int position) {
            textViewQuestion.setText(question.getQuestion());
            radioGroupOptions.removeAllViews(); // Clear old options before adding new ones
            radioGroupOptions.clearCheck();     // Clear previous selection

            List<String> options = question.getOptions();
            if (options != null) {
                for (int i = 0; i < options.size(); i++) {
                    RadioButton radioButton = new RadioButton(itemView.getContext());
                    radioButton.setText(options.get(i));
                    radioButton.setId(View.generateViewId()); // Ensure unique IDs
                    radioGroupOptions.addView(radioButton);

                    // Restore selection if previously answered
                    String previouslySelected = selectedAnswers.get(position);
                    if (previouslySelected != null && previouslySelected.equals(options.get(i))) {
                        radioButton.setChecked(true);
                    }
                }
            }

            radioGroupOptions.setOnCheckedChangeListener((group, checkedId) -> {
                RadioButton selectedRadioButton = group.findViewById(checkedId);
                if (selectedRadioButton != null) {
                    selectedAnswers.put(position, selectedRadioButton.getText().toString());
                } else {
                    // This case might happen if clearCheck() is called while a listener is active
                    // or if an invalid ID is somehow checked.
                    // For safety, you might want to remove the entry or handle as appropriate.
                    selectedAnswers.remove(position);
                }
            });
        }
    }
}