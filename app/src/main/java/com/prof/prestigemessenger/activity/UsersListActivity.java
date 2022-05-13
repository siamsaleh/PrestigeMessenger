package com.prof.prestigemessenger.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;
import com.prof.prestigemessenger.adapters.PersonRecyclerAdapter;
import com.prof.prestigemessenger.adapters.UserAdapters;
import com.prof.prestigemessenger.databinding.ActivityUsersListBinding;
import com.prof.prestigemessenger.listener.UserListener;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.utilities.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersListActivity extends BaseActivity implements UserListener {

    ActivityUsersListBinding binding;
    FirebaseAuth mAuth;

//    private final FirebaseFirestore mDb = FirebaseFirestore.getInstance();
//    private PersonRecyclerAdapter mAdapter;

    private User user;

    UserAdapters userAdapters;
    List<User> users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUsersListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        user = (User) getIntent().getSerializableExtra("user");

        mAuth = FirebaseAuth.getInstance();

        setListener();
        getUsers();


        binding.searchMassage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (binding.searchMassage.getText().toString().isEmpty()){
                    getUsers();
                }
            }
        });



//        binding.searchMassage.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//                Log.d("SEARCHING", "afterTextChanged: " + binding.searchMassage.getText().toString());
//
//                Query query;
//
//                if (binding.searchMassage.getText().toString().isEmpty()){
//                    query = mDb.collection("users")
//                            .orderBy("name", Query.Direction.ASCENDING);
//                }else {
//                    query = mDb.collection("users")
//                            .whereEqualTo("name", binding.searchMassage.getText().toString())
//                            .orderBy("name", Query.Direction.ASCENDING);
//                }
//                FirestoreRecyclerOptions<User> options = new FirestoreRecyclerOptions.Builder<User>()
//                        .setQuery(query, User.class)
//                        .build();
//                mAdapter.updateOptions(options);
//
//            }
//        });

    }



    private void setListener() {
        binding.layoutSearch.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onClick(View v) {
                //Clear Previous Recycler view
                users.clear();
                userAdapters.notifyDataSetChanged();

                search();
            }
        });
    }

    private void search() {
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->{
                    String currentUserId = mAuth.getUid();
                    if (task.isSuccessful() && task.getResult() != null){
//                        List<User> users = new ArrayList<>();
                        users = new ArrayList<>();
                        binding.userRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (binding.searchMassage.getText().toString().equals(queryDocumentSnapshot.getString(Constants.KEY_NAME))
                                    || binding.searchMassage.getText().toString().equals(queryDocumentSnapshot.getString(Constants.KEY_PHONE)) ) {
                                if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                    continue;
                                }
                                User user = new User();
                                user.ccp = queryDocumentSnapshot.getString(Constants.KEY_COUNTRY_CODE);
                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                user.fcmToken = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                user.id = queryDocumentSnapshot.getId();
                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                users.add(user);
                            }
                        }
                        if (users.size() > 0){
                            //UserAdapters userAdapters = new UserAdapters(users, this);
                            userAdapters = new UserAdapters(users, UsersListActivity.this);
                            binding.userRecyclerview.setAdapter(userAdapters);
                            binding.userRecyclerview.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    private void getUsers(){
        ////////////////
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task ->{
                    String currentUserId = mAuth.getUid();
                    if (task.isSuccessful() && task.getResult() != null){
//                        List<User> users = new ArrayList<>();
                        users = new ArrayList<>();
                        binding.userRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                        for(QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (currentUserId.equals(queryDocumentSnapshot.getId())){
                                continue;
                            }
                            User user = new User();
                            user.ccp = queryDocumentSnapshot.getString(Constants.KEY_COUNTRY_CODE);
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.fcmToken = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0){
                            //UserAdapters userAdapters = new UserAdapters(users, this);
                            userAdapters = new UserAdapters(users, this);
                            binding.userRecyclerview.setAdapter(userAdapters);
                            binding.userRecyclerview.setVisibility(View.VISIBLE);
                        }
                    }
                });

        ///////////


//        Query query = mDb.collection("users")
//                .orderBy("name", Query.Direction.ASCENDING);
//
//        FirestoreRecyclerOptions<User> options= new FirestoreRecyclerOptions.Builder<User>()
//            .setQuery(query, User.class)
//            .build();
//
//        mAdapter = new PersonRecyclerAdapter(options, this, getApplicationContext());
//        binding.userRecyclerview.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
//        binding.userRecyclerview.setAdapter(mAdapter);
//        binding.userRecyclerview.setVisibility(View.VISIBLE);

    }

    @Override
    public void onUserClicked(User user) {
        startActivity(new Intent(UsersListActivity.this, ChatActivity.class).putExtra(Constants.KEY_USER, user));
        finish();
    }

//    @Override
//    protected void onStart() {
//        super.onStart();
//        mAdapter.startListening();
//    }

//    @Override
//    protected void onStop() {
//        super.onStop();
//        if (mAdapter != null) {
//            mAdapter.stopListening();
//        }
//    }



}