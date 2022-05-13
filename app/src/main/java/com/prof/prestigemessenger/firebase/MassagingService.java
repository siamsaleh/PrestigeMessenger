package com.prof.prestigemessenger.firebase;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.activity.ChatActivity;
import com.prof.prestigemessenger.activity.IncomingInvitationActivity;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.utilities.Constants;

import java.util.Random;

public class MassagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //Log.d("FCM", "Token: "+ token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        //Log.d("FCM", "Message: "+ remoteMessage.getNotification().getBody());

        String type = remoteMessage.getData().get(Constants.REMOTE_MSG_TYPE);

//        Uri sound= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
//        builder.setSound(sound);

        if (type != null){
            if (type.equals(Constants.REMOTE_MSG_INVITATION)){
                Intent intent = new Intent(getApplicationContext(), IncomingInvitationActivity.class);
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_TYPE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_TYPE)
                );
                intent.putExtra(
                        Constants.KEY_NAME,
                        remoteMessage.getData().get(Constants.KEY_NAME)
                );
                intent.putExtra(
                        Constants.REMOTE_MSG_MEETING_ROOM,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_MEETING_ROOM)
                );
                intent.putExtra(
                       Constants.REMOTE_MSG_INVITER_TOKEN,
                       remoteMessage.getData().get(Constants.REMOTE_MSG_INVITER_TOKEN)
                );
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }else if (type.equals(Constants.REMOTE_MSG_INVITATION_RESPONSE)){
                Intent intent = new Intent(Constants.REMOTE_MSG_INVITATION_RESPONSE);
                intent.putExtra(
                        Constants.REMOTE_MSG_INVITATION_RESPONSE,
                        remoteMessage.getData().get(Constants.REMOTE_MSG_INVITATION_RESPONSE)
                );
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
            //Message
            else if (type.equals("msg")){
                User user = new User();
                user.id = remoteMessage.getData().get(Constants.KEY_USER_ID);
                user.name = remoteMessage.getData().get(Constants.KEY_NAME);
                user.fcmToken = remoteMessage.getData().get(Constants.KEY_FCM_TOKEN);

                int notificationId = new Random().nextInt();
                String channelId = "chat_message";

                Intent intent = new Intent(this, ChatActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra(Constants.KEY_USER, user);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
                builder.setSmallIcon(R.drawable.ic_outline_notifications_24);
                builder.setContentTitle(user.name);
                builder.setContentText(remoteMessage.getData().get(Constants.KEY_MESSAGE));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(
                        remoteMessage.getData().get(Constants.KEY_MESSAGE)
                ));
                builder.setPriority(NotificationCompat.PRIORITY_HIGH);
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);

                //Notification sound
                MediaPlayer mMediaPlayer = MediaPlayer.create(this, R.raw.notification);
                mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mMediaPlayer.setLooping(false);
                mMediaPlayer.start();

                //builder.setSound(RingtoneManager.getDefaultUri(R.raw.notification));
                //builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_LIGHTS| Notification.DEFAULT_VIBRATE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    CharSequence charName = "Chat Message";
                    String channelDescription = "This notification channel is used for chat message notification";
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel(channelId, charName, importance);
                    channel.setDescription(channelDescription);
                    NotificationManager notificationManager = getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }

                NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
                notificationManagerCompat.notify(notificationId, builder.build());
            }
        }

    }
}
