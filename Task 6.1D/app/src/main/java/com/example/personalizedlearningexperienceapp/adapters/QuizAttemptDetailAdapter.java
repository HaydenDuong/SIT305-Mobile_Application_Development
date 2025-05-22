package com.example.personalizedlearningexperienceapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.personalizedlearningexperienceapp.R;
import com.example.personalizedlearningexperienceapp.data.QuestionResponseEntity; // Room Entity
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken; // For deserializing options list
import java.lang.reflect.Type;
import java.util.List;
import java.util.ArrayList;

public class QuizAttemptDetailAdapter extends RecyclerView.Adapter<QuizAttemptDetailAdapter.DetailViewHolder> {

    private final LayoutInflater inflater;
    private List<QuestionResponseEntity> questionResponses;
    private final Gson gson = new Gson();

    public QuizAttemptDetailAdapter(Context context, List<QuestionResponseEntity> questionResponses) {
        this.inflater = LayoutInflater.from(context);
        this.questionResponses = questionResponses;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = inflater.inflate(R.layout.item_question_response_detail, parent, false);
        return new DetailViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        if (questionResponses != null && !questionResponses.isEmpty()) {
            QuestionResponseEntity currentResponse = questionResponses.get(position);
            holder.bind(currentResponse, position + 1);
        }
    }

    @Override
    public int getItemCount() {
        return questionResponses == null ? 0 : questionResponses.size();
    }

    public void updateData(List<QuestionResponseEntity> newResponses) {
        this.questionResponses = newResponses;
        notifyDataSetChanged(); // Consider using DiffUtil for better performance with large lists
    }

    class DetailViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewQuestionNumberAndText;
        private final TextView textViewYourAnswerText;
        private final TextView textViewCorrectAnswerText;
        private final TextView textViewYourAnswerLabel; // For coloring the label

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewQuestionNumberAndText = itemView.findViewById(R.id.textViewDetailItemQuestionNumberAndText);
            textViewYourAnswerText = itemView.findViewById(R.id.textViewDetailItemYourAnswerText);
            textViewCorrectAnswerText = itemView.findViewById(R.id.textViewDetailItemCorrectAnswerText);
            textViewYourAnswerLabel = itemView.findViewById(R.id.textViewDetailItemYourAnswerLabel);
        }

        public void bind(QuestionResponseEntity response, int questionNumber) {
            // Access fields directly
            textViewQuestionNumberAndText.setText(questionNumber + ". " + response.questionText);

            // User's Answer
            String userAnswerFullText = response.userAnswer;
            if (userAnswerFullText == null || userAnswerFullText.isEmpty()) {
                userAnswerFullText = "Not answered";
            }
            textViewYourAnswerText.setText(userAnswerFullText);

            // Correct Answer
            List<String> optionsList = new ArrayList<>();
            try {
                Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                // Access field directly for options JSON
                if (response.options != null && !response.options.isEmpty()) {
                    optionsList = gson.fromJson(response.options, listType);
                }
            } catch (Exception e) {
                // Log error or handle malformed JSON
                android.util.Log.e("QuizAttemptDetailAdapter", "Error parsing options JSON", e);
            }

            // Access field directly for correct answer letter
            String correctAnswerLetter = response.correctAnswer != null ? response.correctAnswer.trim().toUpperCase() : "";
            int correctAnswerIndex = -1;
            switch (correctAnswerLetter) {
                case "A": correctAnswerIndex = 0; break;
                case "B": correctAnswerIndex = 1; break;
                case "C": correctAnswerIndex = 2; break;
                case "D": correctAnswerIndex = 3; break;
            }

            String correctAnswerFullText = "N/A";
            if (correctAnswerIndex != -1 && !optionsList.isEmpty() && correctAnswerIndex < optionsList.size()) {
                correctAnswerFullText = optionsList.get(correctAnswerIndex);
            } else if (optionsList.isEmpty()) {
                correctAnswerFullText = "Options not available";
            } else {
                correctAnswerFullText = "Error finding correct option text (Letter: " + correctAnswerLetter +")";
            }
            textViewCorrectAnswerText.setText(correctAnswerFullText);


            // Color coding - compare user's full answer text with correct answer's full text
            if (userAnswerFullText.equals(correctAnswerFullText) && !userAnswerFullText.equals("Not answered")) {
                textViewYourAnswerText.setTextColor(itemView.getContext().getColor(R.color.correct_answer_color));
                textViewYourAnswerLabel.setTextColor(itemView.getContext().getColor(R.color.correct_answer_color));
            } else {
                textViewYourAnswerText.setTextColor(itemView.getContext().getColor(R.color.incorrect_answer_color));
                textViewYourAnswerLabel.setTextColor(itemView.getContext().getColor(R.color.incorrect_answer_color));
            }
        }
    }
}