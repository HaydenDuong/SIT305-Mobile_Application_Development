package com.example.personalizedlearningexperienceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.models.QuizQuestion;
import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {

    private final LayoutInflater inflater;
    private List<QuizQuestion> questionsList;

    public ResultAdapter(Context context, List<QuizQuestion> questionsList) {
        this.inflater = LayoutInflater.from(context);
        this.questionsList = questionsList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_question_result, parent, false);
        return new ResultViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        if (questionsList != null && !questionsList.isEmpty()) {
            QuizQuestion currentQuestion = questionsList.get(position);
            holder.bind(currentQuestion, position + 1);
        }
    }

    @Override
    public int getItemCount() {
        return questionsList == null ? 0 : questionsList.size();
    }

    public void updateData(List<QuizQuestion> newQuestionsList) {
        this.questionsList = newQuestionsList;
        notifyDataSetChanged();
    }

    class ResultViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewQuestionNumberAndText;
        private final TextView textViewCorrectAnswerText;
        private final TextView textViewYourAnswerText;
        private final TextView textViewYourAnswerLabel;

        public ResultViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestionNumberAndText = itemView.findViewById(R.id.textViewResultItemQuestionNumberAndText);
            textViewCorrectAnswerText = itemView.findViewById(R.id.textViewResultItemCorrectAnswerText);
            textViewYourAnswerText = itemView.findViewById(R.id.textViewResultItemYourAnswerText);
            textViewYourAnswerLabel = itemView.findViewById(R.id.textViewResultItemYourAnswerLabel);
        }

        public void bind(QuizQuestion question, int questionNumber) {
            textViewQuestionNumberAndText.setText(questionNumber + ". " + question.getQuestion());

            String userSelectedAnswerFullText = question.getUserSelectedAnswer();
            if (userSelectedAnswerFullText == null || userSelectedAnswerFullText.isEmpty()) {
                userSelectedAnswerFullText = "Not answered";
            }
            textViewYourAnswerText.setText(userSelectedAnswerFullText);

            String correctAnswerLetter = question.getCorrectAnswer().trim().toUpperCase();
            int correctAnswerIndex = -1;
            switch (correctAnswerLetter) {
                case "A": correctAnswerIndex = 0; break;
                case "B": correctAnswerIndex = 1; break;
                case "C": correctAnswerIndex = 2; break;
                case "D": correctAnswerIndex = 3; break;
            }

            String correctAnswerFullText = "N/A (Error parsing correct answer letter: " + correctAnswerLetter + ")";
            if (correctAnswerIndex != -1 && correctAnswerIndex < question.getOptions().size()) {
                correctAnswerFullText = question.getOptions().get(correctAnswerIndex);
            }
            textViewCorrectAnswerText.setText(correctAnswerFullText);

            if (userSelectedAnswerFullText.equals(correctAnswerFullText) && !userSelectedAnswerFullText.equals("Not answered")) {
                // User's answer is correct
                textViewYourAnswerText.setTextColor(itemView.getContext().getColor(R.color.correct_answer_color));
                textViewYourAnswerLabel.setTextColor(itemView.getContext().getColor(R.color.correct_answer_color));
            } else {
                // User's answer is incorrect or not answered
                textViewYourAnswerText.setTextColor(itemView.getContext().getColor(R.color.incorrect_answer_color));
                textViewYourAnswerLabel.setTextColor(itemView.getContext().getColor(R.color.incorrect_answer_color));
            }
        }
    }
}