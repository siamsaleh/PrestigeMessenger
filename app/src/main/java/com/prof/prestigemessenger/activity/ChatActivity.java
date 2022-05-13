package com.prof.prestigemessenger.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.prof.prestigemessenger.adapters.ChatAdapter;
import com.prof.prestigemessenger.databinding.ActivityChatBinding;
import com.prof.prestigemessenger.models.ChatMessage;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.network.ApiClient;
import com.prof.prestigemessenger.network.ApiService;
import com.prof.prestigemessenger.utilities.Constants;
import com.prof.prestigemessenger.utilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    ActivityChatBinding binding;
    private User receiveUser;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private FirebaseAuth mAuth;
    private String conversationId = null;

    private Boolean isReceiverAvailable = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setListener();
        loadReceiverDetails();
        init();
        listenerMessage();

        call();
    }

    private void init(){
        mAuth = FirebaseAuth.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiveUser.image),
                mAuth.getUid()
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
        binding.receiverName.setText(receiveUser.name);
        try {
            //notifica
            binding.profileImage.setImageBitmap(getConversionImage(receiveUser.image));
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    private void sendMessage(){
        String encryptedMSG = "";
        try {
            encryptedMSG = encrypy(binding.inputMassage.getText().toString().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, mAuth.getUid());
        message.put(Constants.KEY_RECEIVER_ID, receiveUser.id);
        //message.put(Constants.KEY_MESSAGE, binding.inputMassage.getText().toString().trim());
        message.put(Constants.KEY_MESSAGE, encryptedMSG);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message);
        //Last Message
        if (conversationId != null){
            updateConversation(binding.inputMassage.getText().toString());
            updateConversation(encryptedMSG);
        }else {
            HashMap<String, Object> conversation = new HashMap<>();
            conversation.put(Constants.KEY_SENDER_ID, mAuth.getUid());
            conversation.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversation.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversation.put(Constants.KEY_RECEIVER_ID, receiveUser.id);
            conversation.put(Constants.KEY_RECEIVER_NAME, receiveUser.name);
            conversation.put(Constants.KEY_RECEIVER_IMAGE, receiveUser.image);
            conversation.put(Constants.KEY_RECEIVER_UNSEEN_MESSAGES, "1");
            conversation.put(Constants.KEY_LAST_MASSAGE_ID, mAuth.getUid());
            //conversation.put(Constants.KEY_LAST_MASSAGE, binding.inputMassage.getText().toString());
            conversation.put(Constants.KEY_LAST_MASSAGE, encryptedMSG);
            conversation.put(Constants.KEY_TIMESTAMP, new Date());
            addConversations(conversation);
        }
        //Notification
        if (!isReceiverAvailable){
            try {
                JSONArray token = new JSONArray();
                token.put(receiveUser.fcmToken);

                JSONObject data = new JSONObject();
                data.put(Constants.REMOTE_MSG_TYPE, "msg");
                data.put(Constants.KEY_USER_ID, mAuth.getUid());
                data.put(Constants.KEY_NAME, getIntent().getStringExtra("myName"));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.inputMassage.getText().toString());
                //data.put("sound", "notification");

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, token);
                //body.put("sound", "notification");

                sendNotification(body.toString());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        binding.inputMassage.setText(null);
    }

    //Online
    private void listenAvailabilityOfReceiver(){
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                receiveUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) ->{
            if (error != null){
                return;
            }
            if (value != null){
                if (value.getLong(Constants.KEY_AVAILABILITY) != null){
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiveUser.fcmToken = value.getString(Constants.KEY_FCM_TOKEN);
                if(receiveUser.image == null){
                    receiveUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiveUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatMessages.size());
                }
            }
            if (isReceiverAvailable){
                binding.statusTxt.setText("Online");
                binding.statusTxt.setTextColor(Color.parseColor("#D1B03F"));
            }else {
                binding.statusTxt.setText("Offline");
                binding.statusTxt.setTextColor(Color.parseColor("#FD0018"));
            }
        });
    }

    //Notification
    private void sendNotification(String messageBody){
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@Nullable Call<String> call, @Nullable Response<String> response) {
                if (response.isSuccessful()){
                    try {
                        if (response.body() != null){
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1){
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    showToast("N S S");
                }else {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@Nullable Call<String> call, @Nullable Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // get messages
    private void listenerMessage(){
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, mAuth.getUid())
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiveUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CHAT)
                .whereEqualTo(Constants.KEY_SENDER_ID, receiveUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, mAuth.getUid())
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
      if(error != null){
          return ;
      }
      if (value != null){
          int count = chatMessages.size();
          for (DocumentChange documentChange : value.getDocumentChanges()){
              if (documentChange.getType() == DocumentChange.Type.ADDED){
                  ChatMessage chatMessage = new ChatMessage();
                  chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                  chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                  chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                  chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                  chatMessage.dateObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                  chatMessages.add(chatMessage);
              }
          }
          Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
          if (count == 0){
              chatAdapter.notifyDataSetChanged();
          }else {
              chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
              binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
          }
          binding.chatRecyclerView.setVisibility(View.VISIBLE);
      }
      //Last Message
      if (conversationId == null){
          checkForConversations();
      }
    };

    private Bitmap getBitmapFromEncodedString(String encodedImage){
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }else {
            return null;
        }
    }

    private void loadReceiverDetails() {

        //User Image
        receiveUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);

        //Get fcmToken
        database = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = database.document("users/"+receiveUser.id);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String fcmToken = documentSnapshot.getString("fcmToken");
                    receiveUser.fcmToken = fcmToken;
                }
            }
        });
    }

    private void setListener() {
        binding.layoutSend.setOnClickListener(v -> {
            if (!binding.inputMassage.getText().toString().equals("")) {
                sendMessage();
            }
        });
        binding.backIcon.setOnClickListener(v -> onBackPressed());
    }

    private String getReadableDateTime(Date date){
//        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
        if (new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date).equals(new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date()))){
            return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
        }else {
            return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date);
        }
    }

    // Last Message

    private void addConversations(HashMap<String , Object> conversation){
        database.collection(Constants.KEY_COLLATION_CONVERSATIONS)
                .add(conversation)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());
    }

    private void updateConversation(String message){
        //msg count
        DocumentReference userDocRef = database.document("conversations/"+conversationId);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){
                    String count = "0";
                    String receiverUnseenMessages = documentSnapshot.getString("receiverUnseenMessages");
                    count = receiverUnseenMessages;

                    //////////////////Upgrade Conversation///////////////////////////
                    DocumentReference documentReference =
                            database.collection(Constants.KEY_COLLATION_CONVERSATIONS).document(conversationId);
                    documentReference.update(
                            Constants.KEY_LAST_MASSAGE, message,
                            Constants.KEY_TIMESTAMP, new Date(),
                            Constants.KEY_RECEIVER_UNSEEN_MESSAGES, String.valueOf(Integer.valueOf(count) + 1), ////
                            Constants.KEY_LAST_MASSAGE_ID, receiveUser.id //receiver
                    );

                }
            }
        });
    }

    private void checkSeenUnseen(String conversationId) {

        DocumentReference userDocRef = database.document("conversations/"+conversationId);
        userDocRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()){

                    String lastMessageId = documentSnapshot.getString(Constants.KEY_LAST_MASSAGE_ID);

                    if (mAuth.getUid().equals(lastMessageId)){
                        DocumentReference documentReference =
                                database.collection(Constants.KEY_COLLATION_CONVERSATIONS).document(conversationId);
                        documentReference.update(
                                Constants.KEY_RECEIVER_UNSEEN_MESSAGES, String.valueOf(0)
                        );
                    }
                }
            }
        });

    }

    private void checkForConversations(){
        if (chatMessages.size() != 0){
            checkForConversationRemotely(
                    mAuth.getUid(),
                    receiveUser.id
            );
            checkForConversationRemotely(
                    receiveUser.id,
                    mAuth.getUid()
            );
        }
    }

    private void checkForConversationRemotely(String senderId, String receiverId){
        database.collection(Constants.KEY_COLLATION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversationsOnCompleteListener);

    }

    private final OnCompleteListener <QuerySnapshot> conversationsOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0){
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();

            //My Code
            checkSeenUnseen(conversationId);
        }
    };

    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    //For Back Button
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////          Call          ///////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void call() {

        binding.videoCallIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receiveUser.fcmToken == null || receiveUser.fcmToken.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            receiveUser.name + " is not available for calling", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
                    intent.putExtra("user", receiveUser);
                    intent.putExtra("myName", getIntent().getStringExtra("myName"));
                    intent.putExtra("type", "video");
                    startActivity(intent);
                }
            }
        });

        binding.callIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (receiveUser.fcmToken == null || receiveUser.fcmToken.trim().isEmpty()){
                    Toast.makeText(getApplicationContext(),
                            receiveUser.name + " is not available for calling", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent = new Intent(getApplicationContext(), OutgoingInvitationActivity.class);
                    intent.putExtra("user", receiveUser);
                    intent.putExtra("myName", getIntent().getStringExtra("myName"));
                    intent.putExtra("type", "audio");
                    startActivity(intent);
                }
            }
        });

    }



    private String encrypy(String msg) throws Exception{
        SecretKeySpec  key = generateKey(Constants.API_KEY);
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte [] encVal = cipher.doFinal(msg.getBytes());
        String encryptedValue = Base64.encodeToString(encVal, Base64.DEFAULT);
        return encryptedValue;
    }

    private SecretKeySpec generateKey(String password) throws  Exception{
        final MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = password.getBytes("UTF-8");
        digest.update(bytes, 0, bytes.length);
        byte[] key = digest.digest();
        SecretKeySpec secretKeySpec =new SecretKeySpec(key, "AES");
        return secretKeySpec;
    }

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }
}