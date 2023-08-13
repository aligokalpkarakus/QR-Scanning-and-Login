package com.aligokalpkarakus.qrandlogin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.aligokalpkarakus.qrandlogin.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private FirebaseAuth auth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        progressBar = findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();

    }

    public void signInClicked(View view) {

        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();

        //TAMS kullanıcısı, ERAS admini, ve 3 tane de ERAS admini tarafından oluşturulacak kullanıcılar
        //ERAS admini TAMS kullanıcısı tarafından TamsActivity'de oluşturulacak firebase'e kaydolacak
        //ERAS admini kendi kullanıcılarını da ErasActivity'de oluşturacak firebase'e kaydolacak
        //ERAS admini tarafından oluşturulan kullanıcılar hesaplarıyla giriş yaptıklarında QR okuma aktivitesine geçecekler
        //TAMS kullanıcısı bilgileri tamsadmin@gmail.com tamsadmin
        //ERAS admin bilgileri erasadmin@gmail.com erasadmin
        //ERAS kullanıcıları kullanici1@gmail.com kullanici1, kullanici2@gmail.com kullanici2, kullanici3@gmail.com kullanici3

        if (email.equals("") || password.equals("")) {
            Toast.makeText(this, "Enter email and password", Toast.LENGTH_SHORT).show();
        } else if (email.equals("tamsadmin@gmail.com") && password.equals("tamsadmin")) {
            Toast.makeText(this, "TAMS Admini olarak giriş yapıldı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, TamsActivity.class);
            startActivity(intent);
            finish();
        } else if (email.equals("erasadmin@gmail.com") && password.equals("erasadmin")) {
            Toast.makeText(this, "ERAS Admini olarak giriş yapıldı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, ErasActivity.class);
            startActivity(intent);
            finish();
        }else{

            auth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    progressBar.setVisibility(View.GONE);
                    if(task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this,QRActivity.class);
                        startActivity(intent);
                        finish();
                    }else{
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }


    }

    public void signUpClicked(View view){

        String email = binding.emailText.getText().toString();
        String password = binding.passwordText.getText().toString();

        if(email.equals("") || password.equals("")){
            Toast.makeText(this,"Enter email and password",Toast.LENGTH_LONG).show();
        }else{
            auth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            if(task.isSuccessful()){
                                Toast.makeText(MainActivity.this, "Account created", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(MainActivity.this,QRActivity.class);
                                startActivity(intent);
                                finish();
                            }else{
                                Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }


    }


}