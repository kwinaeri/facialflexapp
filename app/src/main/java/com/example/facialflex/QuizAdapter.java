package com.example.facialflex;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.QuestionViewHolder> {

    private List<Question> questionList;
    private OnQuestionAnsweredListener onQuestionAnsweredListener;

    public interface OnQuestionAnsweredListener {
        void onQuestionAnswered(int position, String answer);
    }

    public QuizAdapter(List<Question> questionList, OnQuestionAnsweredListener listener) {
        this.questionList = questionList;
        this.onQuestionAnsweredListener = listener;
    }

    @Override
    public QuestionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_question, parent, false);
        return new QuestionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(QuestionViewHolder holder, int position) {
        Question question = questionList.get(position);

        // Set question text
        holder.questionText.setText(question.getQuestionText());

        // Set options text
        holder.option1.setText(question.getOption1());
        holder.option2.setText(question.getOption2());

        // Restore the selected option based on the answer
        if ("yes".equals(question.getAnswer())) {
            holder.option1.setChecked(true);
            holder.option2.setChecked(false);
        } else if ("no".equals(question.getAnswer())) {
            holder.option1.setChecked(false);
            holder.option2.setChecked(true);
        } else {
            holder.option1.setChecked(false);
            holder.option2.setChecked(false);
        }

        // Handle answer selection for option1
        holder.option1.setOnClickListener(v -> {
            // Update the answer in the model
            question.setAnswer("yes");
            onQuestionAnsweredListener.onQuestionAnswered(position, "yes");
            // Ensure only option1 is selected and option2 is not
            holder.option1.setChecked(true);
            holder.option2.setChecked(false);
        });

        // Handle answer selection for option2
        holder.option2.setOnClickListener(v -> {
            // Update the answer in the model
            question.setAnswer("no");
            onQuestionAnsweredListener.onQuestionAnswered(position, "no");
            // Ensure only option2 is selected and option1 is not
            holder.option1.setChecked(false);
            holder.option2.setChecked(true);
        });
    }



    @Override
    public int getItemCount() {
        return questionList.size();
    }

    public static class QuestionViewHolder extends RecyclerView.ViewHolder {
        TextView questionText;
        RadioButton option1, option2;

        public QuestionViewHolder(View itemView) {
            super(itemView);
            questionText = itemView.findViewById(R.id.questionText);
            option1 = itemView.findViewById(R.id.option1);
            option2 = itemView.findViewById(R.id.option2);
        }
    }
}
