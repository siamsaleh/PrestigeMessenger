package com.prof.prestigemessenger.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.databinding.ActivityIncomingInvitationBinding;
import com.prof.prestigemessenger.databinding.ActivityOutgoingInvitationBinding;
import com.prof.prestigemessenger.network.ApiClient;
import com.prof.prestigemessenger.network.ApiService;
import com.prof.prestigemessenger.utilities.Constants;

import org.jitsi.meet.sdk.JitsiMeetActivity;
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions;
import org.jitsi.meet.sdk.JitsiMeetUserInfo;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomingInvitationActivity extends AppCompatActivity {

    ActivityIncomingInvitationBinding binding;
    String meetingType = null;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomingInvitationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Set call data

        meetingType = getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_TYPE);
        name = getIntent().getStringExtra(Constants.KEY_NAME);
        String phone = getIntent().getStringExtra(Constants.KEY_PHONE);

        if (meetingType != null){
            if (meetingType.equals("video")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_outline_videocam_24);
            }
            if (meetingType.equals("audio")){
                binding.imageMeetingType.setImageResource(R.drawable.ic_outline_call_24);
            }
        }

        if (name != null){
            binding.textFirstName.setText(name.substring(0, 1));
            binding.textName.setText(name);
        }

        if (phone != null){
            binding.textPhone.setText(phone);
        }

        //////////
        binding.imageAccept.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_ACCEPTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));

        binding.imageReject.setOnClickListener(view -> sendInvitationResponse(
                Constants.REMOTE_MSG_INVITATION_REJECTED,
                getIntent().getStringExtra(Constants.REMOTE_MSG_INVITER_TOKEN)
        ));


    }


    ///////////// accept cancel invitation

    private void sendInvitationResponse(String type, String receiverToken){
        try {
            JSONArray tokens = new JSONArray();
            tokens.put(receiverToken);

            JSONObject body = new JSONObject();
            JSONObject data = new JSONObject();

            data.put(Constants.REMOTE_MSG_TYPE, Constants.REMOTE_MSG_INVITATION_RESPONSE);
            data.put(Constants.REMOTE_MSG_INVITATION_RESPONSE, type);

            body.put(Constants.REMOTE_MSG_DATA, data);
            body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

            sendRemoteMessage(body.toString(), type);

        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendRemoteMessage(String remoteMessageBody, String type){
        ApiClient.getClient().create(ApiService.class).sendRemoteMessage(
                Constants.getRemoteMessageHeaders(), remoteMessageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()){
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_ACCEPTED)){
                        //Meeting
                        try {
                            URL serverURL = new URL("https://meet.jit.si");

                            //Audio Video
                            JitsiMeetConferenceOptions.Builder builder = new JitsiMeetConferenceOptions.Builder();
                            builder.setServerURL(serverURL);
                            builder.setWelcomePageEnabled(false);
                            builder.setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM));
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

                                    /*.setFeatureFlag("pip.enabled",false) // <- this line you have to add
                                    .setFeatureFlag("calendar.enabled",false) // optional
                                    .setFeatureFlag("call-integration.enabled",false) // optional*/

                                    .setFeatureFlag("meeting-password.enabled", false);

                            /**
                             * Flag indicating if the toolbox should be enabled
                             * Default: enabled.
                             * export const TOOLBOX_ENABLED = 'toolbox.enabled';
                             /**
                             * Flag indicating if lobby mode button should be enabled.
                             * Default: enabled.
                             * export const LOBBY_MODE_ENABLED = 'lobby-mode.enabled';

                             */

//                            Bundle bundle = new Bundle();
//                            bundle.putString("primary", "#D1B03F");
//
////                            Random rnd = new Random();
////                            int color = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//
//                            builder.setColorScheme(bundle);

                            //Display Name
                            JitsiMeetUserInfo jitsiMeetUserInfo = new JitsiMeetUserInfo();
                            jitsiMeetUserInfo.setDisplayName(name);
                            builder.setUserInfo(jitsiMeetUserInfo);

                            /*JitsiMeetConferenceOptions conferenceOptions =
                                    new JitsiMeetConferenceOptions.Builder()
                                    .setServerURL(serverURL)
                                    .setWelcomePageEnabled(false)
                                    .setRoom(getIntent().getStringExtra(Constants.REMOTE_MSG_MEETING_ROOM))
                                    .build();*/

                            JitsiMeetActivity.launch(IncomingInvitationActivity.this, builder.build());
                            finish();
                        }catch (Exception e){
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                    if (type.equals(Constants.REMOTE_MSG_INVITATION_REJECTED)){
                        Toast.makeText(getApplicationContext(), "Invitation Rejected", Toast.LENGTH_SHORT).show();
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

    private BroadcastReceiver invitationResponseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra(Constants.REMOTE_MSG_INVITATION_RESPONSE);
            if (type != null){
                if (type.equals(Constants.REMOTE_MSG_INVITATION_CANCELLED)){
                    Toast.makeText(getApplicationContext(), "Invitation Cancelled", Toast.LENGTH_SHORT).show();
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