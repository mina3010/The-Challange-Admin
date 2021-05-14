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

import com.example.shararaadmin.Adapter.AdapterSets;
import com.example.shararaadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.shararaadmin.UI.CategoryActivity.catList;
import static com.example.shararaadmin.UI.CategoryActivity.selected_category_index;

public class SetsActivity extends AppCompatActivity {

    public static List<String>setsID=new ArrayList<>();
    public static int selected_set_index=0 ;
    TextView setName;
    Button addNewSet;
    RecyclerView setsRV;
    AdapterSets adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog, addCategoryDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);

        firestore=FirebaseFirestore.getInstance();

        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setName=findViewById(R.id.name_sets);
        addNewSet=findViewById(R.id.addNewSets);
        setsRV=findViewById(R.id.sets_rv);

        String name=getCatName();
        setName.setText(name);

        addNewSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addSets();

            }
        });

        RecyclerView.LayoutManager lm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        setsRV.setLayoutManager(lm);
        setsRV.setHasFixedSize(true);

        loadSets();
    }

    private void addSets() {

        loadingDialog.show();

         final String current_category_id=catList.get(selected_category_index).getId();
         final String current_category_counter=catList.get(selected_category_index).getSetCounter();

        Map<String ,Object> qData =new ArrayMap<>();
        qData.put("COUNT","0");

        firestore.collection("Quiz").document(current_category_id)
                .collection(current_category_counter).document("QUESTIONS_LIST")
                .set(qData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Map<String ,Object>catDoc =new ArrayMap<>();
                    catDoc.put("COUNTER",String.valueOf(Integer.valueOf(current_category_counter)+1));
                    catDoc.put("SET"+ String.valueOf(setsID.size()+1)+"_ID",current_category_counter);
                    catDoc.put("SETS", setsID.size()+1);

                firestore.collection("Quiz").document(current_category_id).update(catDoc)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(SetsActivity.this, "Set Added Successfully", Toast.LENGTH_SHORT).show();

                        setsID.add(current_category_counter);
                        catList.get(selected_category_index).setNumOfSets(String.valueOf(setsID.size()));
                        catList.get(selected_category_index).setSetCounter(String.valueOf(Integer.valueOf(current_category_counter)+1));
                        adapter.notifyItemChanged(setsID.size());
                        adapter.notifyDataSetChanged();
                        loadingDialog.dismiss();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();

                    }
                });


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }

    private void loadSets() {
        setsID.clear();
        loadingDialog.show();

        firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {

                long numOfSets=(long)documentSnapshot.get("SETS");

                for (int i= 1; i<= numOfSets; i++){
                 setsID.add(documentSnapshot.getString("SET"+String.valueOf(i)+"_ID"))   ;
                }

                catList.get(selected_category_index).setSetCounter(documentSnapshot.getString("COUNTER"));
                catList.get(selected_category_index).setNumOfSets(String.valueOf(numOfSets));

                adapter= new AdapterSets(setsID);
                setsRV.setAdapter(adapter);
                adapter.notifyItemChanged(setsID.size());
                loadingDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });

    }

    private String getCatName(){
        Intent i =getIntent();
        String nameCategory = i.getStringExtra("CategoryName");
        return nameCategory;
    }
}