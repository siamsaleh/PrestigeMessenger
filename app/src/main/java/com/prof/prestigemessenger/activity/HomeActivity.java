package com.prof.prestigemessenger.activity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.prof.prestigemessenger.BuildConfig;
import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.adapters.RecentConversationAdapter;
import com.prof.prestigemessenger.adapters.StoryAdapter;
import com.prof.prestigemessenger.databinding.ActivityHomeBinding;
import com.prof.prestigemessenger.listener.ConversationListener;
import com.prof.prestigemessenger.listener.StoryListener;
import com.prof.prestigemessenger.models.ChatMessage;
import com.prof.prestigemessenger.models.Story;
import com.prof.prestigemessenger.models.StorySeen;
import com.prof.prestigemessenger.models.StoryUser;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.utilities.Constants;
import com.prof.prestigemessenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class HomeActivity extends BaseActivity implements ConversationListener, StoryListener {

    private ActivityHomeBinding binding;
    private String encodeImage;

    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;

    //Story RecyclerView /
    private List<Story> storyList;
    private List<StoryUser> storyUserList;
    private StoryAdapter storyAdapter;

    private FirebaseAuth mAuth;
    private FirebaseFirestore database;
    private StorageReference storyImageRef;

    private ProgressDialog loadingBar;

    final static int GALLERY_PICK = 1;

    String KEY = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();

    private User user;
    private String myName;

    //Background Task
    private int REQUEST_CODE_BATTERY_OPTIMIZATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_home);*/
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        preferenceManager = new PreferenceManager(getApplicationContext());
        loadingUserDetails();
        getToken();
        listenerConversation();
        showStory();
        //listenerStory();

        DocumentReference userDocRef = database.document("users/"+mAuth.getUid());
        //userDocRef.get("name")
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String name = documentSnapshot.getString("name");
                    user = new User();
                    user.name = name;
                    myName = name;
                }
            }
        });

        setListener();


//        userDocRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
//            @Override
//            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
//
//                String image = value.getString("image");
//                String name = value.getString("name");
//                String phone = value.getString("phone");
//
//                Toast.makeText(getApplicationContext(), name, Toast.LENGTH_SHORT).show();
//
//                user = new User();
//                user.image = image;
//                user.id = mAuth.getUid();
//                user.name = name;
//                user.phone = phone;
//            }
//        });


//        database.collection(Constants.KEY_COLLATION_CONVERSATIONS)
//                .whereEqualTo(Constants.KEY_USER, mAuth.getUid())
//                .addSnapshotListener(eventListener);

        /*checkForBatteryOptimization();*/
    }

    private void init(){
        mAuth = FirebaseAuth.getInstance();
        conversations = new ArrayList<>();
        storyList = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        //storyAdapter = new StoryAdapter(storyList, this, HomeActivity.this);
        binding.conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        binding.conversationsRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
        loadingBar = new ProgressDialog(this);
        loadingBar.setCancelable(false);
        storyImageRef = FirebaseStorage.getInstance().getReference().child("story").child(mAuth.getCurrentUser().getUid());
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //last message
    private void listenerConversation(){
        database.collection(Constants.KEY_COLLATION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, mAuth.getUid())
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLATION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, mAuth.getUid())
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if(error != null){
            return;
        }
        if(value != null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType() == DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;
                    if (mAuth.getCurrentUser().getUid().equals(senderId)){
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }else{
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MASSAGE);
                    chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);

                    //Mycode
                    chatMessage.receiverUnseenMessages = documentChange.getDocument().getString(Constants.KEY_RECEIVER_UNSEEN_MESSAGES);
                    chatMessage.lastMessageId = documentChange.getDocument().getString(Constants.KEY_LAST_MASSAGE_ID);

                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MASSAGE);
                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            //Mycode
                            conversations.get(i).receiverUnseenMessages = documentChange.getDocument().getString(Constants.KEY_RECEIVER_UNSEEN_MESSAGES);
                            conversations.get(i).lastMessageId = documentChange.getDocument().getString(Constants.KEY_LAST_MASSAGE_ID);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
        }
    };

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(mAuth.getCurrentUser().getUid());
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    private void loadingUserDetails() {
        try{
            byte [] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            binding.profileImage.setImageBitmap(bitmap);
            binding.addStoryProfile.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void setListener(){
        binding.profileImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
        binding.signOut.setOnClickListener(v -> showPopup(binding.signOut));
        binding.addUser.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, UsersListActivity.class).putExtra("user", user));
        });

        //add story
        binding.addStoryProfile.setOnClickListener(v->addStory());

    }

    private void updateProfile(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> image = new HashMap<>();
        image.put(Constants.KEY_IMAGE, encodeImage);

        database.collection("users")
                .document(mAuth.getCurrentUser().getUid())
                .set(image, SetOptions.mergeFields("image"))
                .addOnSuccessListener(documentReference ->{
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private  final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getData() != null){
                    Uri imageUri = result.getData().getData();
                    try{
                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        binding.profileImage.setImageBitmap(bitmap);
                        encodeImage = encodeImage(bitmap);
                        updateProfile();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                }
            }
    );

    private void signOut(){
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(mAuth.getCurrentUser().getUid());
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferenceManager.clear();
                    mAuth.signOut();
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast(e.getMessage()));
    }

    private void showPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.signout_menu, popup.getMenu());
        popup.show();


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.sign_out)
                    signOut();
//                if (menuItem.getItemId() == R.id.refresh)
//                    onCreate(savedInstanceState);
                return true;
            }
        });
    }

//    @Override
//    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.sign_out:
//                signOut();
//                return true;
//            default:
//                return super.onOptionsItemSelected(item);
//        }
//    }

    //Interface

    @Override
    public void onConversationClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        intent.putExtra("myName", myName);
        startActivity(intent);
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////Story////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void showStory(){
        //Product RecyclerView

        binding.storyRecyclerview.setLayoutManager(
                new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        storyUserList = new ArrayList<>();

        //getting stories from firebase
        final DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference(Constants.KEY_COLLECTION_STORY);
        storyRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    for(DataSnapshot userSnapshot : snapshot.getChildren()){
                        StoryUser storyUser = new StoryUser();
                        for (DataSnapshot storySnapshot : userSnapshot.getChildren()){
                             Story story = storySnapshot.getValue(Story.class);

                             //Add Story Seen
                             if (storySnapshot.hasChild("storySeen")){
                                 for (DataSnapshot seenSnapshot : storySnapshot.child("storySeen").getChildren()){
                                     story.storySeenList.add(seenSnapshot.getValue().toString());
                                     //Log.d("NEWSTORY", "onDataChange: "+ seenSnapshot.getValue().toString());
                                 }
                             }

                            //24h before or not
                            if (story.time != null){
                                try {
                                    if (date24hBefore(story.time)){
                                        storyUser.userStories.add(story);
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }

                        }

                        //Check Seen Un seen Then Add list
                        if (storyUser.userStories.size()!=0) {
                            //Log.d("NEWSTORY", "onDataChange: "+ storyUser.userStories.get(0).time);
                            //seenCheck
                            if (storyUser.userStories.get(0).storySeenList != null && storyUser.userStories.get(0).storySeenList.size() != 0){
                                int count = 0;
                                for (int i = 0; i < storyUser.userStories.get(0).storySeenList.size(); i++) {
                                    if (storyUser.userStories.get(0).storySeenList.get(i).equals(mAuth.getCurrentUser().getUid())){
                                        count++;
                                    }
                                }
                                if (count == 0){
                                    //Add Story in list
                                    storyUserList.add(storyUser);
                                }
                            }else {
                                //is seen list empty Add Story in list
                                storyUserList.add(storyUser);
                            }
                        }

                    }
                    storyAdapter = new StoryAdapter(storyUserList, getApplicationContext(), binding);
                    //storyAdapter.stories = storyList;
                    binding.storyRecyclerview.setAdapter(storyAdapter);






//                    for (DataSnapshot npsnapshot : snapshot.getChildren()){
//                        Story story = npsnapshot.getValue(Story.class);
//
//                        if (npsnapshot.hasChild("seen")) {
//                            DataSnapshot seen = npsnapshot.child("seen");
//
//                            StorySeen storySeen = seen.getValue(StorySeen.class);
//                            assert story != null;
//                            story.storySeen = storySeen;
//
//
////                            if (story.getSellerId().equals(mAuth.getCurrentUser().getUid())) {
//
////                                Log.d("TESTING", "onDataChange: "+l.getProductImageArray().img0);
////                            }
//                            //Progress Bar
////                            if (progressBar.getVisibility() == View.VISIBLE) {
////                                progressBar.setVisibility(View.GONE);
////
////                            }
//                        }
//                        //storyAdapter.stories = storyList;
////                        storyList.add(story);
//
//                        //24h before or not
//                        if (story.time != null){
//                            try {
//                                if (date24hBefore(story.time)){
//                                    storyList.add(story);
//                                }
//                            } catch (ParseException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                    }
//                    storyAdapter = new StoryAdapter(storyList, getApplicationContext(), binding);
//                    //storyAdapter.stories = storyList;
//                    binding.storyRecyclerview.setAdapter(storyAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addStory(){
        //Pick Image
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_PICK);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK && data!=null ){

            Uri imageUri = data.getData();

            loadingBar.setTitle("Story");
            loadingBar.setMessage("Please wait, while we updating your image...");
            loadingBar.show();

            final StorageReference filepath = storyImageRef.child(KEY+".jpg");
            final DatabaseReference storyRef = FirebaseDatabase.getInstance().getReference().child(Constants.KEY_COLLECTION_STORY).child(mAuth.getUid());

            filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()){

                        //get Image Uri
                        filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
//                                //get Image Uri


                                HashMap <String, Object> story = new HashMap<>();
                                story.put(Constants.KEY_SENDER_ID, mAuth.getUid());
                                story.put(Constants.KEY_STORY_IMAGE, uri.toString());
                                story.put(Constants.KEY_SENDER_NAME, user.name);
                                story.put(Constants.KEY_COLLECTION_STORY_ID, KEY);
                                //story.put(Constants.KEY_TIMESTAMP, new Date());
                                story.put(Constants.KEY_TIMESTAMP, getDateTime());

                                story.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));

                                storyRef.child(KEY).updateChildren(story).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        KEY = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
                                        loadingBar.dismiss();
                                    }
                                });
                                loadingBar.dismiss();




//                                //Story
//                                HashMap<String, Object> story = new HashMap<>();
//                                story.put(Constants.KEY_SENDER_ID, mAuth.getUid());
//                                story.put(Constants.KEY_STORY_IMAGE, uri.toString());
//                                story.put(Constants.KEY_TIMESTAMP, new Date());
//                                story.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
//                                database.collection(Constants.KEY_COLLECTION_STORY).add(story).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
//                                    @Override
//                                    public void onComplete(@NonNull Task<DocumentReference> task) {
//                                        KEY = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
//                                        loadingBar.dismiss();
//                                    }
//                                });
//                                loadingBar.dismiss();

                            }
                        });

                    }else{
                        loadingBar.dismiss();
                        Toast.makeText(HomeActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (requestCode == REQUEST_CODE_BATTERY_OPTIMIZATION){
            checkForBatteryOptimization();
        }
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
    }

    private boolean date24hBefore(String dateB) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date dateStory = sdf.parse(dateB);

        if (dateStory.compareTo(new Date(System.currentTimeMillis() - 86400 * 1000 * 2)) > 0){
            return true;
        }else {
            return false;
        }
    }

    //story
//    private void listenerStory(){
//        database.collection(Constants.KEY_COLLECTION_STORY)
//                .whereEqualTo(Constants.KEY_SENDER_ID, mAuth.getUid())
//                .addSnapshotListener(eventListenerStory);
//    }
//
//    private final EventListener<QuerySnapshot> eventListenerStory = (value, error) -> {
//        if(error != null){
//            return;
//        }
//        if(value != null){
//            for (DocumentChange documentChange : value.getDocumentChanges()){
//                if (documentChange.getType() == DocumentChange.Type.ADDED){
//                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
//                    String senderImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
//                    String storyImage = documentChange.getDocument().getString(Constants.KEY_STORY_IMAGE);
//
//                    Story story = new Story();
//                    story.senderId = senderId;
//                    story.senderImage = senderImage;
//                    story.storyImage = storyImage;
//
///*                    if (mAuth.getCurrentUser().getUid().equals(senderId)){
//                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
//                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
//                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
//                    }else{
//                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
//                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
//                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
//                    }*/
//
//                    story.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
//                    storyList.add(story);
//                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED){
//                    /*for (int i = 0; i < conversations.size(); i++) {
//                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
//                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
//                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
//                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MASSAGE);
//                            conversations.get(i).dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
//                            break;
//                        }
//                    }*/
//                }
//            }
//            //Collections.sort(storyList, (obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
//            storyAdapter.notifyDataSetChanged();
//            binding.storyRecyclerview.setVisibility(View.VISIBLE);
//            binding.storyRecyclerview.smoothScrollToPosition(0);
//        }
//    };



    @Override
    public void onStoryClicked(Story story) {
        Toast.makeText(getApplicationContext(), story.senderId, Toast.LENGTH_SHORT).show();
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////Story////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////

//    FirebaseFirestore rootRef = FirebaseFirestore.getInstance();
//    DocumentReference docIdRef = rootRef.collection("yourCollection").document(docId);
//    docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//        @Override
//        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//            if (task.isSuccessful()) {
//                DocumentSnapshot document = task.getResult();
//                if (document.exists()) {
//                    Log.d(TAG, "Document exists!");
//                } else {
//                    Log.d(TAG, "Document does not exist!");
//                }
//            } else {
//                Log.d(TAG, "Failed with: ", task.getException());
//            }
//        }
//    });



    //Optimization
    private void checkForBatteryOptimization(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if (!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("Warning");
                builder.setMessage("Battery optimization is enabled. It can interrupt running background services.");
                builder.setPositiveButton("Disable", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                        startActivityForResult(intent, REQUEST_CODE_BATTERY_OPTIMIZATION);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        }
    }



}