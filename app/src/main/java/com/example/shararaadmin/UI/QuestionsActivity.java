package com.example.shararaadmin.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shararaadmin.Adapter.AdapterQuestion;
import com.example.shararaadmin.Model.Question;
import com.example.shararaadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.shararaadmin.UI.CategoryActivity.catList;
import static com.example.shararaadmin.UI.CategoryActivity.selected_category_index;
import static com.example.shararaadmin.UI.SetsActivity.selected_set_index;
import static com.example.shararaadmin.UI.SetsActivity.setsID;

public class QuestionsActivity extends AppCompatActivity {

    public static List<Question> questionList=new ArrayList<>();
    TextView setName;
    Button addQuestion;
    RecyclerView QuestionRV;
    AdapterQuestion adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addCategoryDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questions);

        firestore=FirebaseFirestore.getInstance();

        loadingDialog = new Dialog(QuestionsActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setName=findViewById(R.id.name_sets_question);
        addQuestion=findViewById(R.id.addQuestion);
        QuestionRV=findViewById(R.id.Question_rv);

        addQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent=new Intent(QuestionsActivity.this,QuestionDetailsActivity.class);
                intent.putExtra("ACTION","ADD");
                startActivity(intent);
            }
        });

        LinearLayoutManager layout=new LinearLayoutManager(this);
        layout.setOrientation(layout.VERTICAL);
        QuestionRV.setLayoutManager(layout);
        loadQuestions();
    }

    private void loadQuestions() {
        questionList.clear();
        loadingDialog.show();
        firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                .collection(setsID.get(selected_set_index)).get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        Map<String , QueryDocumentSnapshot> docList=new ArrayMap<>();
                        for(QueryDocumentSnapshot doc: queryDocumentSnapshots){
                            docList.put(doc.getId(),doc);
                        }
                        QueryDocumentSnapshot quesListDoc= docList.get("QUESTIONS_LIST");
                        String count=quesListDoc.getString("COUNT");
                        for(int i=0;i<Integer.valueOf(count);i++){
                            String quesID = quesListDoc.getString("Q"+String.valueOf(i+1)+"_ID");
                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            questionList.add(new Question(
                                    quesID,
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("D"),
                                    Integer.valueOf(quesDoc.getString("ANSWER"))
                            ));
                        }
                        adapter=new AdapterQuestion(questionList);
                        QuestionRV.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                        loadingDialog.dismiss();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter !=null) {
            adapter.notifyDataSetChanged();
        }
    }

}