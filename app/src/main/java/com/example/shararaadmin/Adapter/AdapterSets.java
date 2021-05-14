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

import com.example.shararaadmin.UI.QuestionsActivity;
import com.example.shararaadmin.R;
import com.example.shararaadmin.UI.SetsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import static com.example.shararaadmin.UI.CategoryActivity.catList;
import static com.example.shararaadmin.UI.CategoryActivity.selected_category_index;
import static com.example.shararaadmin.UI.SetsActivity.selected_set_index;

public class AdapterSets extends RecyclerView.Adapter<AdapterSets.ViewHolder> {
    public List<String> setsID=new ArrayList<>();

    public AdapterSets(List<String> setsID) {
        this.setsID = setsID;
    }

    @Override
    public AdapterSets.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sets, parent, false);
        return new AdapterSets.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterSets.ViewHolder holder, int position) {

        String setID=setsID.get(position);
        holder.setData(position,setID,this);
    }

    @Override
    public int getItemCount() {
        return setsID.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView setName;
        ImageView delete;
        Dialog loadingDialog;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setName=itemView.findViewById(R.id.setName);
            cardView=itemView.findViewById(R.id.cardView_set);
            delete=itemView.findViewById(R.id.deleteSet);
            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setData(final int pos, final String setID , final AdapterSets adapter) {
            setName.setText("SET" +String.valueOf(pos+1));

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selected_set_index =pos;
                    Intent intent=new Intent(itemView.getContext(), QuestionsActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialog.show();
                    AlertDialog dialog= new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Set")
                            .setMessage("Do You Want to delete this Set ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteSet(pos,setID,itemView.getContext(),adapter);
                                }
                            }).setNegativeButton("Cancel", null).setIcon(android.R.drawable.ic_dialog_alert).show();
                }
            });

        }
        private void deleteSet(final int pos, String setID, final Context context, final AdapterSets adapter){
            loadingDialog.show();
            final FirebaseFirestore firestore=FirebaseFirestore.getInstance();
            firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                    .collection(setID).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    WriteBatch batch = firestore.batch();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots)
                    {
                        batch.delete(doc.getReference());
                    }
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Map<String, Object> catDoc = new ArrayMap<>();
                            int index = 1;
                            for (int i = 0; i < setsID.size(); i++)
                            {
                                if (i != pos)
                                {
                                    catDoc.put("SET" + String.valueOf(index) + "_ID", setsID.get(i));
                                    index++;
                                }
                            }
                            catDoc.put("SETS", index - 1);
                            firestore.collection("Quiz").document(catList.get(selected_category_index).getId())
                                    .update(catDoc)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(context, "Set deleted successfully", Toast.LENGTH_SHORT).show();
                                            SetsActivity.setsID.remove(pos);
                                            catList.get(selected_category_index).setNumOfSets(String.valueOf(SetsActivity.setsID.size()));
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
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                         }


                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }
    }
}
