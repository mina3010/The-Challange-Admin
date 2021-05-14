package com.example.shararaadmin.Adapter;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shararaadmin.Model.Question;
import com.example.shararaadmin.UI.QuestionDetailsActivity;
import com.example.shararaadmin.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.shararaadmin.UI.CategoryActivity.catList;
import static com.example.shararaadmin.UI.CategoryActivity.selected_category_index;
import static com.example.shararaadmin.UI.SetsActivity.selected_set_index;
import static com.example.shararaadmin.UI.SetsActivity.setsID;

public class AdapterQuestion extends RecyclerView.Adapter<AdapterQuestion.ViewHolder> {
    private List<Question> question_list=new ArrayList<>();

    public AdapterQuestion(List<Question> question_list) {
        this.question_list = question_list;
    }

    @Override
    public AdapterQuestion.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.question_item, parent, false);
        return new AdapterQuestion.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterQuestion.ViewHolder holder, int position) {

        holder.setData(position,this);
    }

    @Override
    public int getItemCount() {
        return question_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView delete,edit;
        Dialog loadingDialog;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title=itemView.findViewById(R.id.name_question);
            delete=itemView.findViewById(R.id.delete_question);
            edit=itemView.findViewById(R.id.edit_question);

            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        }
        private void setData(final int position, final AdapterQuestion adapter){
            title.setText("QUESTION "+String.valueOf(position +1));

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent =new Intent(itemView.getContext(), QuestionDetailsActivity.class);
                    intent.putExtra("ACTION","EDIT");
                    intent.putExtra("Q_ID",position);
                    itemView.getContext().startActivity(intent);
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog dialog= new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Question")
                            .setMessage("Do You Want to delete this Question ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteQuestion(position,itemView.getContext(),adapter);
                                    //,setID,itemView.getContext(),adapter
                                }
                            }).setNegativeButton("Cancel", null).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        });
                            }

        private void deleteQuestion(final int position, final Context context , final AdapterQuestion adapter) {
            loadingDialog.show();
            final FirebaseFirestore firestore=FirebaseFirestore.getInstance();

            firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                    .collection(setsID.get(selected_set_index)).document(question_list.get(position).getQuesID()).delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            final Map<String , Object> quesDoc =new ArrayMap<>();
                            int index =1;
                            for (int i=0;i<question_list.size();i++){
                                if (i != position){
                                    quesDoc.put("Q"+String.valueOf(index)+"_ID",question_list.get(i).getQuesID());
                                    index++;
                                }
                            }
                            quesDoc.put("COUNT", String.valueOf(index-1));

                            firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                                    .collection(setsID.get(selected_set_index)).document("QUESTIONS_LIST")
                                    .set(quesDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Question Delete Successfully", Toast.LENGTH_SHORT).show();
                                            question_list.remove(position);
                                            adapter.notifyDataSetChanged();
                                            loadingDialog.dismiss();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                            loadingDialog.dismiss();

                                        }
                                    });


                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();

                        }
                    });

        }
        }
    }

