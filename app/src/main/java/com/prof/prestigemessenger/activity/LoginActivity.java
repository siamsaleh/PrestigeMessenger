package com.prof.prestigemessenger.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.prof.prestigemessenger.MainActivity;
import com.prof.prestigemessenger.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {


    //View Binding
//    private ActivityLoginBinding binding;

    CountryCodePicker ccp;
    private EditText etName;
    PhoneAuthCredential phoneAuthCredential1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Initialize Variables
        final EditText inputMobile = findViewById(R.id.inputMobile);
        etName = findViewById(R.id.etName);
        ccp = findViewById(R.id.ccp);
        Button buttonGetOTP = findViewById(R.id.btContinue);
        final ProgressBar progressBar = findViewById(R.id.progressbar);

//        //View Binding
//        binding = ActivityLoginBinding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());
//        setListener();

        String country_code = ccp.getSelectedCountryCode();//880
        Log.d("LOGDDD", country_code);


        buttonGetOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etName.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Enter Name Please", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (inputMobile.getText().toString().trim().isEmpty()){
                    Toast.makeText(LoginActivity.this, "Enter Mobile Number First", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                buttonGetOTP.setVisibility(View.GONE);

                PhoneAuthProvider.getInstance().verifyPhoneNumber(
//                        "+880" + inputMobile.getText().toString(),
                        "+" + ccp.getSelectedCountryCode() + inputMobile.getText().toString(),
                        60,
                        TimeUnit.SECONDS,
                        LoginActivity.this,
                        new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                            @Override
                            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                //For Auto Verification
                                verifyUser(phoneAuthCredential);
                                phoneAuthCredential1 = phoneAuthCredential;
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
//                                Toast.makeText(SentOTPActivity.this, "Complete", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onVerificationFailed(@NonNull FirebaseException e) {
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                                Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onCodeSent(@NonNull String verificationID, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                progressBar.setVisibility(View.GONE);
                                buttonGetOTP.setVisibility(View.VISIBLE);
                                Intent intent = new Intent(LoginActivity.this, VerifyOTPActivity.class);
                                intent.putExtra("mobile", inputMobile.getText().toString());
                                intent.putExtra("verificationID", verificationID);
                                intent.putExtra("name", etName.getText().toString());
                                intent.putExtra("ccp", ccp.getSelectedCountryCode());
                                intent.putExtra("image", "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");
                                startActivity(intent);
                            }
                        }
                );
            }
        });
    }



    private void verifyUser(PhoneAuthCredential phoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }else {
                    Toast.makeText(LoginActivity.this, "The verification code entered is invalid", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }



    ////////////////////////////////////////////////////////////////////////////////////////////

//    private void setListener(){
//
//    }

//    private void addDataToFirebase(){
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> data = new HashMap<>();
//        data.put("first_name", "Siam");
//        data.put("last_name", "Saleh");
//        database.collection("users")
//                .add(data)
//                .addOnSuccessListener(documentReference ->{
//                    Toast.makeText(getApplicationContext(), "DataInserted", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
//                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
//                });
//    }

    ////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }



}