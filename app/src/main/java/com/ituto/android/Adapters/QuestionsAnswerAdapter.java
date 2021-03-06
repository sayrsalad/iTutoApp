package com.ituto.android.Adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.ituto.android.Models.Question;
import com.ituto.android.R;

import java.util.ArrayList;

public class QuestionsAnswerAdapter extends RecyclerView.Adapter<QuestionsAnswerAdapter.AnswerQuestionHolder>{

    private Context context;
    private ArrayList<Question> questionArrayList;
    private SharedPreferences sharedPreferences;

    public QuestionsAnswerAdapter(Context context, ArrayList<Question> questionArrayList) {
        this.context = context;
        this.questionArrayList = questionArrayList;
        this.sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
    }

    @NonNull
    @Override
    public AnswerQuestionHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_question_answer, parent, false);
        return new AnswerQuestionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerQuestionHolder holder, @SuppressLint("RecyclerView") int position) {
        Question question = questionArrayList.get(position);
        ArrayList<String> choices = question.getChoices();

        holder.txtAnswer.setText(questionArrayList.get(position).getTuteeAnswer());
        if (sharedPreferences.getString("loggedInAs", "").equals("TUTOR")) {
            holder.txtAnswer.setEnabled(false);
        }
        holder.txtItemNum.setText(String.valueOf(position + 1) + ".");
        holder.txtQuestion.setText(question.getQuestion());
        holder.txtChoiceA.setText("A. " + choices.get(0));
        holder.txtChoiceB.setText("B. " + choices.get(1));
        holder.txtChoiceC.setText("C. " + choices.get(2));
        holder.txtChoiceD.setText("D. " + choices.get(3));

    }

    class AnswerQuestionHolder extends RecyclerView.ViewHolder {

        private SwipeRevealLayout swipeQuestionLayout;
        private EditText txtAnswer;
        private TextView txtItemNum, txtQuestion, txtChoiceA, txtChoiceB, txtChoiceC, txtChoiceD;
        private ImageView btnDeleteQuestion;

        public AnswerQuestionHolder(@NonNull View itemView) {
            super(itemView);
            txtAnswer = itemView.findViewById(R.id.txtAnswer);
            txtItemNum = itemView.findViewById(R.id.txtItemNum);
            txtQuestion = itemView.findViewById(R.id.txtQuestion);
            txtChoiceA = itemView.findViewById(R.id.txtChoiceA);
            txtChoiceB = itemView.findViewById(R.id.txtChoiceB);
            txtChoiceC = itemView.findViewById(R.id.txtChoiceC);
            txtChoiceD = itemView.findViewById(R.id.txtChoiceD);

            txtAnswer.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    questionArrayList.get(getAbsoluteAdapterPosition()).setTuteeAnswer(txtAnswer.getText().toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return questionArrayList.size();
    }

//    private String showAnswerLetter(int index) {
//        String answer = "";
//
//        if (index == 0) {
//            answer = "A";
//        }
//
//        if (index == 1) {
//            answer = "B";
//        }
//
//        if (index == 2) {
//            answer = "C";
//        }
//
//        if (index == 3) {
//            answer = "D";
//        }
//
//        return answer;
//    }
}
