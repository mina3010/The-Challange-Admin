package com.example.shararaadmin.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shararaadmin.Adapter.AdapterCategory;
import com.example.shararaadmin.Model.CategoryModel;
import com.example.shararaadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CategoryActivity extends AppCompatActivity {

    RecyclerView rv_category;
    Button addCat;
    AdapterCategory Adapter;
    private Dialog loadingDialog, addCategoryDialog;
    private EditText catNameDialog;
    private  Button catAddDialog;

    public static List<CategoryModel>catList=new ArrayList<>();
    public static int selected_category_index=0;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        firestore=FirebaseFirestore.getInstance();
        rv_category=findViewById(R.id.rv_category);
        addCat =findViewById(R.id.addNewCategory);

        addCategoryDialog=new Dialog(CategoryActivity.this);
        addCategoryDialog.setContentView(R.layout.add_category_dialog);
        addCategoryDialog.setCancelable(true);
        addCategoryDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        catNameDialog=addCategoryDialog.findViewById(R.id.nameCategoryDialog);
        catAddDialog=addCategoryDialog.findViewById(R.id.addCategoryDialog);

        addCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                catNameDialog.getText().clear();
                addCategoryDialog.show();
            }
        });

        catAddDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (catNameDialog.getText().toString().isEmpty()){
                    catNameDialog.setError("Enter Category Name! ");
                    return;
                }else {
                    addNewCategory(catNameDialog.getText().toString());
                }
            }
        });

        showDialog(true);

//        catList.add("Cat1");
//        catList.add("Cat2");
//        catList.add("Cat3");

//        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,2,GridLayoutManager.VERTICAL,false);
        RecyclerView.LayoutManager lm = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        rv_category.setLayoutManager(lm);
        rv_category.setHasFixedSize(true);

        new Thread(){
            @Override
            public void run() {
                // sleep(3000);
                loadData();

            }
        }.start();
    }

    private void addNewCategory(final String name) {
        addCategoryDialog.dismiss();

        Map<String ,Object> catData =new ArrayMap<>();
        catData.put("NAME",name);
        catData.put("SETS",0);
        catData.put("COUNTER","1");
        //get id
        final String doc_ID= firestore.collection("Quiz").document().getId();
        firestore.collection("Quiz").document(doc_ID).set(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                Map<String ,Object> catDoc =new ArrayMap<>();
                catDoc.put("CAT"+ String.valueOf(catList.size()+1)+ "_NAME",name);
                catDoc.put("CAT"+ String.valueOf(catList.size()+1)+ "_ID",doc_ID);
                catDoc.put("COUNT",catList.size()+1);

                firestore.collection("Quiz").document("Category").update(catDoc)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(CategoryActivity.this, "Category added successfully", Toast.LENGTH_SHORT).show();
                        catList.add(new CategoryModel(doc_ID,name,"0","1"));
                        Adapter.notifyItemInserted(catList.size());

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(CategoryActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void loadData() {
        catList.clear();
        firestore.collection("Quiz").document("Category").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){

                    DocumentSnapshot doc =task.getResult();
                    if (doc.exists()){
                        long count = (long)doc.get("COUNT");
                        for(int i=1 ;i<=count ;i++){
                            String catName= doc.getString("CAT" + String.valueOf(i) + "_NAME");
                            String catID= doc.getString("CAT" + String.valueOf(i) + "_ID");
                            catList.add(new CategoryModel(catID,catName,"0","1"));
                        }
                        Adapter = new AdapterCategory(catList);
                        rv_category.setAdapter(Adapter);


                        showDialog(false);
//                        startActivity(new Intent(SplashActivity.this,CategoryActivity.class));
//                        SplashActivity.this.finish();


                    }else {
                        Toast.makeText(CategoryActivity.this, "No Category Document Exists ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(CategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void showDialog(boolean status){
        if (status==true) {
            loadingDialog = new Dialog(CategoryActivity.this);
            loadingDialog.setContentView(R.layout.loading);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            loadingDialog.show();
        }else{
            loadingDialog.dismiss();
        }

    }
}