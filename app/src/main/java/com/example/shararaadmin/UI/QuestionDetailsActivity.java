package com.example.shararaadmin.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shararaadmin.Model.Question;
import com.example.shararaadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import static com.example.shararaadmin.UI.CategoryActivity.catList;
import static com.example.shararaadmin.UI.CategoryActivity.selected_category_index;
import static com.example.shararaadmin.UI.QuestionsActivity.questionList;
import static com.example.shararaadmin.UI.SetsActivity.selected_set_index;
import static com.example.shararaadmin.UI.SetsActivity.setsID;

public class QuestionDetailsActivity extends AppCompatActivity {

    Button add;
    EditText Ques,op_A,op_B,op_C,op_D,ans;
    private String quesStr,aStr,bStr,cStr,dStr,ansStr;
    private String action;
    private int qID;
    FirebaseFirestore firestore;
    private Dialog loadingDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);

        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore=FirebaseFirestore.getInstance();
        Ques=findViewById(R.id.Question);
        op_A=findViewById(R.id.option_1);
        op_B=findViewById(R.id.option_2);
        op_C=findViewById(R.id.option_3);
        op_D=findViewById(R.id.option_4);
        ans=findViewById(R.id.Answer);
        add=findViewById(R.id.add);

        action=getIntent().getStringExtra("ACTION");
        if (action.compareTo("EDIT") == 0)
        {
            qID = getIntent().getIntExtra("Q_ID",0);
            loadDataUpdate(qID);
            add.setText("UPDATE");
        }
        else
        {
            add.setText("ADD");

        }

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                quesStr = Ques.getText().toString();
                aStr = op_A.getText().toString();
                bStr = op_B.getText().toString();
                cStr = op_C.getText().toString();
                dStr = op_D.getText().toString();
                if (ans.getText().toString().trim().equals("1") || ans.getText().toString().trim() .equals("2") || ans.getText().toString().trim().equals("3") || ans.getText().toString().trim().equals("4"))
                {
                    ansStr = ans.getText().toString().trim();

                if (quesStr.isEmpty()){
                    Ques.setError("Enter Question");
                    return;
                }
                if (aStr.isEmpty()){
                    op_A.setError("Enter Option A");
                    return;
                }
                if (bStr.isEmpty()){
                    op_B.setError("Enter Option B");
                    return;
                }
                if (cStr.isEmpty()){
                    op_C.setError("Enter Option C");
                    return;
                }
                if (dStr.isEmpty()){
                    op_D.setError("Enter Option D");
                    return;
                }
                if (ansStr.isEmpty()){
                    ans.setError("Enter Option Answer");
                    return;
                }

                if (action.compareTo("EDIT") == 0)
                {
                    editQuestion();
                }
                else
                {
                    addNewQuestion();

                }
                }
                else{
                    Toast.makeText(QuestionDetailsActivity.this, "error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void editQuestion() {
        loadingDialog.show();
        Map<String ,Object> quesData=new ArrayMap<>();

        quesData.put("QUESTION",quesStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);

        firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                .collection(setsID.get(selected_set_index)).document(questionList.get(qID).getQuesID())
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(QuestionDetailsActivity.this, "Question Update Successfully", Toast.LENGTH_SHORT).show();
                        questionList.get(qID).setQuestion(quesStr);
                        questionList.get(qID).setOption_1(aStr);
                        questionList.get(qID).setOption_2(bStr);
                        questionList.get(qID).setOption_3(cStr);
                        questionList.get(qID).setOption_4(dStr);
                        questionList.get(qID).setCorrectAns(Integer.valueOf(ansStr));

                        loadingDialog.dismiss();
                        QuestionDetailsActivity.this.finish();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                QuestionDetailsActivity.this.finish();
            }
        });
    }

    private void loadDataUpdate(int id) {
        Ques.setText(questionList.get(id).getQuestion());
        op_A.setText(questionList.get(id).getOption_1());
        op_B.setText(questionList.get(id).getOption_2());
        op_C.setText(questionList.get(id).getOption_3());
        op_D.setText(questionList.get(id).getOption_4());
        ans.setText(String.valueOf(questionList.get(id).getCorrectAns()));

    }

    private void addNewQuestion() {
        loadingDialog.show();
        Map<String ,Object> quesData=new ArrayMap<>();

        quesData.put("QUESTION",quesStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("D",dStr);
        quesData.put("ANSWER",ansStr);

        final String doc_id= firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                .collection(setsID.get(selected_set_index)).document().getId();
        firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                .collection(setsID.get(selected_set_index)).document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String ,Object> quesDoc =new ArrayMap<>();

                        quesDoc.put("Q"+String.valueOf(questionList.size() +1)+ "_ID",doc_id);
                        quesDoc.put("COUNT",String.valueOf(questionList.size()+1));

                        firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                                .collection(setsID.get(selected_set_index)).document("QUESTIONS_LIST")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(QuestionDetailsActivity.this, "Question Added Successfully", Toast.LENGTH_SHORT).show();
                                        questionList.add(new Question(doc_id,quesStr,aStr,bStr,cStr,dStr,Integer.valueOf(ansStr)));
                                        loadingDialog.dismiss();
                                        QuestionDetailsActivity.this.finish();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();
                                QuestionDetailsActivity.this.finish();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
                QuestionDetailsActivity.this.finish();
            }
        });
    }

}