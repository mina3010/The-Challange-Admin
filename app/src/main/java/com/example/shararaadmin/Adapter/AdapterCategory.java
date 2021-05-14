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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.shararaadmin.UI.CategoryActivity;
import com.example.shararaadmin.Model.CategoryModel;
import com.example.shararaadmin.R;
import com.example.shararaadmin.UI.SetsActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class AdapterCategory extends RecyclerView.Adapter<AdapterCategory.ViewHolder> {

    List<CategoryModel>catList;

    public AdapterCategory(List<CategoryModel> catList) {
        this.catList = catList;
    }

    @Override
    public AdapterCategory.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item, parent, false);
        return new AdapterCategory.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterCategory.ViewHolder holder, int position) {

//        holder.name.setText(catList.get(position).getName());
        //TODO get id !!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        String title = catList.get(position).getName();
        holder.setData(title,position,this);

    }

    @Override
    public int getItemCount() {
        return catList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        CardView cardView;
        ImageView deleteCategory;
        private Dialog loadingDialog;
        private Dialog editDialog;
        private EditText edt_catName;
        private Button update;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            deleteCategory = itemView.findViewById(R.id.deleteCategory);
            cardView = itemView.findViewById(R.id.cardView_category);


            loadingDialog=new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


            editDialog=new Dialog(itemView.getContext());
            editDialog.setContentView(R.layout.edit_category);
            editDialog.setCancelable(true);
            editDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

            edt_catName= editDialog.findViewById(R.id.edt_nameCategoryDialog);
            update= editDialog.findViewById(R.id.edt_addCategoryDialog);
        }

        private void setData(final String title, final int pos, final AdapterCategory adapter){
            name.setText(title.toString());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CategoryActivity.selected_category_index= pos;
                    Intent i =new Intent(itemView.getContext(), SetsActivity.class);
                    i.putExtra("CategoryName",title);
                    itemView.getContext().startActivity(i);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    edt_catName.setText(catList.get(pos).getName());
                    loadingDialog.show();
                    editDialog.show();
                    loadingDialog.dismiss();

                    return false;
                }
            });

            update.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (edt_catName.getText().toString().isEmpty()||edt_catName==null){
                        edt_catName.setError("Enter Category Name! ...");
                        return;
                    }
                    updateCategory(edt_catName.getText().toString(),pos,itemView.getContext(),adapter);
                }
            });

            deleteCategory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadingDialog.show();
                    AlertDialog dialog= new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category")
                            .setMessage("D You Want to delete this category ?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteCat(pos,itemView.getContext(), adapter);
                                }
                            }).setNegativeButton("Cancel", null).setIcon(android.R.drawable.ic_dialog_alert).show();
                    loadingDialog.dismiss();
                }
            });
        }
        private void updateCategory(final String catNewName, final int pos, final Context context, final AdapterCategory adapter){

            editDialog.dismiss();
            loadingDialog.show();
            final Map<String ,Object>catData= new ArrayMap<>();
            catData.put("NAME",catNewName);
            final FirebaseFirestore firestore= FirebaseFirestore.getInstance();

            firestore.collection("Quiz").document(catList.get(pos).getId()).update(catData).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                    Map<String ,Object>catDoc= new ArrayMap<>();

                    catDoc.put("CAT"+String.valueOf(pos+1)+"_NAME",catNewName);

                    firestore.collection("Quiz").document("Category")
                            .update(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Category Name Changed successfully", Toast.LENGTH_SHORT).show();

                            CategoryActivity.catList.get(pos).setName(catNewName);
                            adapter.notifyDataSetChanged();
                            loadingDialog.dismiss();

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
                            loadingDialog.dismiss();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }
        private void deleteCat(final int id, final Context context, final AdapterCategory adapter) {
            loadingDialog.show();
            FirebaseFirestore firestore= FirebaseFirestore.getInstance();
            Map<String ,Object>catDoc= new ArrayMap<>();
            int index =1;
            for (int i= 0; i < catList.size(); i++){
                if(i != id){
                    catDoc.put("CAT"+String.valueOf(index)+"_ID",catList.get(i).getId());
                    catDoc.put("CAT"+String.valueOf(index)+"_NAME",catList.get(i).getName());
                    index++;
                }
            }
            catDoc.put("COUNT",index-1);
            firestore.collection("Quiz").document("Category")
                    .set(catDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Toast.makeText(context, "Category deleted successfully", Toast.LENGTH_SHORT).show();
                    CategoryActivity.catList.remove(id);
                    adapter.notifyDataSetChanged();
                    loadingDialog.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();

                }
            });
        }
    }


}
