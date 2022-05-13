package com.prof.prestigemessenger.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.utilities.Constants;
import com.prof.prestigemessenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;

public class VerifyOTPActivity extends AppCompatActivity {

    private EditText inputCode1, inputCode2, inputCode3, inputCode4, inputCode5, inputCode6;
    private String verificationID;
    private PreferenceManager preferenceManager;

    private String image = "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";

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);

        preferenceManager = new PreferenceManager(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        TextView textMobile = findViewById(R.id.textMobile);
        //textMobile.setText(String.format("+880-%s", getIntent().getStringExtra("mobile")));
        textMobile.setText(String.format("+"+getIntent().getStringExtra("ccp")+"-%s", getIntent().getStringExtra("mobile")));

        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);

        setupOTPInputs();

        final ProgressBar progressBar = findViewById(R.id.progressbar);
        final Button buttonVerify = findViewById(R.id.buttonVerify);
        verificationID = getIntent().getStringExtra("verificationID");

        buttonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (inputCode1.getText().toString().trim().isEmpty()
                        || inputCode2.getText().toString().trim().isEmpty()
                        || inputCode3.getText().toString().trim().isEmpty()
                        || inputCode4.getText().toString().trim().isEmpty()
                        || inputCode5.getText().toString().trim().isEmpty()
                        || inputCode6.getText().toString().trim().isEmpty()){
                    Toast.makeText(VerifyOTPActivity.this, "Please enter valid code", Toast.LENGTH_SHORT).show();
                    return;
                }

                String code =
                        inputCode1.getText().toString() +
                                inputCode2.getText().toString() +
                                inputCode3.getText().toString() +
                                inputCode4.getText().toString() +
                                inputCode5.getText().toString() +
                                inputCode6.getText().toString();

                if (verificationID != null){
                    progressBar.setVisibility(View.VISIBLE);
                    buttonVerify.setVisibility(View.GONE);
                    PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                            verificationID,
                            code
                    );
                    FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressBar.setVisibility(View.GONE);
                            buttonVerify.setVisibility(View.VISIBLE);
                            if (task.isSuccessful()){
                                userDataSet();
                            }else {
                                Toast.makeText(VerifyOTPActivity.this, "The verification code entered is invalid", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }
        });
    }


    private void userDataSet() {

        storePreviousData();

        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, getIntent().getStringExtra("name"));
        user.put(Constants.KEY_PHONE, String.format("+"+getIntent().getStringExtra("ccp")+"-%s", getIntent().getStringExtra("mobile")));
        user.put(Constants.KEY_COUNTRY_CODE, getIntent().getStringExtra("ccp"));
        user.put(Constants.KEY_IMAGE, getIntent().getStringExtra("image"));


        database.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .set(user, SetOptions.mergeFields(Constants.KEY_NAME, Constants.KEY_PHONE, Constants.KEY_COUNTRY_CODE, Constants.KEY_IMAGE))
                .addOnSuccessListener(documentReference ->{
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    //preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, getIntent().getStringExtra("name"));
//                    preferenceManager.putString(Constants.KEY_USER_ID, mAuth.getUid());
                    Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void storePreviousData() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task ->{
                    if (task.isSuccessful() && task.getResult() != null){
                        preferenceManager.putString(Constants.KEY_COUNTRY_CODE, task.getResult().getString(Constants.KEY_COUNTRY_CODE));
                        preferenceManager.putString(Constants.KEY_NAME, task.getResult().getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_PHONE, task.getResult().getString(Constants.KEY_PHONE));
                        preferenceManager.putString(Constants.KEY_IMAGE, task.getResult().getString(Constants.KEY_IMAGE));
                    }
                });
    }

    private void setupOTPInputs(){
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()){
                    inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}