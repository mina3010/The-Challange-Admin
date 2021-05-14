package com.example.shararaadmin.UI;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.shararaadmin.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    EditText ed_email, ed_password;
    Button login;
    CheckBox rememberMe;
    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
//        if (pref.getInt("reg", 0) == 1) {
//            Intent intent = new Intent(MainActivity.this, CategoryActivity.class);
//            startActivity(intent);
////        }

            ed_email = findViewById(R.id.Email);
            ed_password = findViewById(R.id.Password);
            rememberMe = findViewById(R.id.rememberMe);
            login = findViewById(R.id.login_btn);

            firebaseAuth = FirebaseAuth.getInstance();

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    login();
                }
            });
        }

        private void login () {
            String password = ed_password.getText().toString().trim();
            String email = ed_email.getText().toString().trim();


            if (email.isEmpty()) {
                ed_email.setError(" email Is Empty! ");
                ed_email.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                ed_email.setError(" Please Provide valid email! ");
                ed_email.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                ed_password.setError(" Password Is Empty! ");
                ed_password.requestFocus();
                return;
            }
            if (password.length() < 6) {
                ed_password.setError(" Password Min 6 characters! ");
                ed_password.requestFocus();
                return;
            }

            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (rememberMe.isChecked()) {
                            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.putInt("reg", 1).commit();
                            //editor.putString("user",userName.getText().toString()).commit();
                            startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                        } else {
                            startActivity(new Intent(MainActivity.this, CategoryActivity.class));
                        }

                    } else {
                        Toast.makeText(MainActivity.this, "Failed Login, Please check credentials!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
