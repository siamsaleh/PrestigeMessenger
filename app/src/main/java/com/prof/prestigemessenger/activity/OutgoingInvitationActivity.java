package com.prof.prestigemessenger.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.databinding.ActivityOutgoingInvitationBinding;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.network.ApiClient;
import com.prof.prestigemessenger.network.ApiService;
import com.prof.prestigemessenger.utilities.Constants;
import com.prof.prestigemessenger.utilities.PreferenceManager;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OutgoingInvitationActivity extends AppCompatActivity {

    ActivityOutgoingInvitationBinding binding;
    private PreferenceManager preferenceManager;
    private String invitationToken = null;
    private User recUser;
    private String meetingRoom = null;
    private String meetingType = null;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOutgoingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        meetingType = getIntent().getStringExtra("type");

        recUser = (User) getIntent().getSerializableExtra("user");
        if (recUser != null){
            binding.textFirstName.setText(recUser.name.substring(0, 1));
            binding.textName.setText(recUser.name);
            binding.textPhone.setText(recUser.phone);
        }

        if (meetingType != null){
            if (meetingType.equals("video")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_outline_videocam_24);
            }
            if (meetingType.equals("audio")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_outline_call_24);
            }
        }

        binding.imageStopInvitation.setOnClickListener(v -> {
            if (recUser != null){
                cancelInvitation(recUser.fcmToken);
            }
        });

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful() && task.getResult() != null){
                    invitationToken = task.getResult().getToken();
                    if (meetingType != null && recUser != null){
                        initiateMeeting(meetingType, recUser.fcmToken);
                    }
                }
            }
        });
    }


    private void initiateMeeting(String meetingType, String receiverToken){
        try {

            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION);
            data.put(Constants.REMOTE_MSG_MEETING_TYPE, meetingType);
//            data.put(Constants.KEY_NAME, recUser.name);
            data.put(Constants.KEY_NAME, getIntent().getStringExtra("myName"));
            data.put(Constants.REMOTE_MSG_INVITER_TOKEN, invitationToken);

            //calling
            meetingRoom =
                    mAuth.getUid() + "_" + UUID.randomUUID().toString().substring(0, 5);
            data.put(Constants.REMOTE_MSG_MEETING_ROOM, meetingRoom);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION);

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                        Toast.makeText(getApplicationContext(), "Invitation Sent Successfully", Toast.LENGTH_SHORT).show();
                    }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                        Toast.makeText(getApplicationContext(), "Invitation Cancelled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }else {
                    Toast.makeText(getApplicationContext(), response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void cancelInvitation(String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, Constants.REMOTE_MSG_INVITATION_CANCELLED);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), Constants.REMOTE_MSG_INVITATION_RESPONSE);

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                    //Meeting
                    try {
                        URL serverURL = new URL("https://meet.jit.si");

                        //Audio
                        JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                        builder.setServerURL(serverURL);
                        builder.setWelcomePageEnabled(false);
                        builder.setRoom(meetingRoom);
                        if (meetingType.equals("audio")){
                            builder.setVideoMuted(true);
                        }
                        builder.setWelcomePageEnabled(false);
                        builder.setSubject("Prestige Messenger");

                        //Flag
                        builder.setFeatureFlag("chat.enabled",false)
                                .setFeatureFlag("invite.enabled",false)
                                .setFeatureFlag("raise-hand.enabled", false)
                                .setFeatureFlag("live-streaming.enabled", false)
                                .setFeatureFlag("recording.enabled", false)
                                .setFeatureFlag("help.enabled", false)
                                .setFeatureFlag("kick-out.enabled", false)
                                .setFeatureFlag("chat.enabled", false)
                                .setFeatureFlag("lobby-mode.enabled", false)

                                .setFeatureFlag("meeting-password.enabled", false);


                        //Display Name
                        JitsiMeetUserInfo jitsiMeetUserInfo = new JitsiMeetUserInfo();
                        jitsiMeetUserInfo.setDisplayName(getIntent().getStringExtra("myName"));
                        builder.setUserInfo(jitsiMeetUserInfo);

                        /*JitsiMeetConferenceOptions conferenceOptions =
                                new JitsiMeetConferenceOptions.Builder()
                                        .setServerURL(serverURL)
                                        .setWelcomePageEnabled(false)
                                        .setRoom(meetingRoom)
                                        .build();*/

                        JitsiMeetActivity.launch(OutgoingInvitationActivity.this, builder.build());
                        finish();
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }else if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                    Toast.makeText(getApplicationContext(), "Invitation Rejected", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(
                invitationResponseReceiver,
                new IntentFilter(Constants.REMOTE_MSG_INVITATION_RESPONSE)
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(
                invitationResponseReceiver
        );
    }
}