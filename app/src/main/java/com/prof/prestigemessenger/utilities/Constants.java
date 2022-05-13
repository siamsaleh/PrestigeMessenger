package com.prof.prestigemessenger.utilities;

import java.util.HashMap;

public class Constants {
    public static final String KEY_COLLECTION_USERS = "users";
    public static final String KEY_NAME = "name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_PREFERENCE_NAME = "chatAppPreference";
    public static final String KEY_IS_SIGNED_IN = "isSignedIn";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_COUNTRY_CODE = "ccp";
    public static final String KEY_FCM_TOKEN = "fcmToken";
    public static final String KEY_USER = "user";
    public static final String KEY_COLLECTION_CHAT = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIMESTAMP = "time";

    public static final String KEY_COLLATION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "senderName";
    public static final String KEY_RECEIVER_NAME = "receiverName";
    public static final String KEY_SENDER_IMAGE = "senderImage";
    public static final String KEY_RECEIVER_IMAGE = "receiverImage";
    public static final String KEY_LAST_MASSAGE = "lastMessage";
    public static final String KEY_LAST_MASSAGE_ID = "lastMessageId";
    public static final String KEY_RECEIVER_UNSEEN_MESSAGES = "receiverUnseenMessages";

    public static final String KEY_COLLECTION_STORY = "story";
    public static final String KEY_COLLECTION_STORY_ID = "storyId";
    public static final String KEY_STORY_IMAGE = "storyImage";


    public static final String KEY_AVAILABILITY = "availability";



    /////////////////////////////////////////////// CALLING

    public static final String REMOTE_MSG_AUTHORIZATION = "Authorization";
    public static final String REMOTE_MSG_CONTENT_TYPE = "Content-Type";

    public static final String REMOTE_MSG_TYPE = "type";
    public static final String REMOTE_MSG_INVITATION = "invitation";
    public static final String REMOTE_MSG_MEETING_TYPE = "meetingType";
    public static final String REMOTE_MSG_INVITER_TOKEN = "inviterToken";
    public static final String REMOTE_MSG_DATA = "data";
    public static final String REMOTE_MSG_REGISTRATION_IDS = "registration_ids";

    public static final String REMOTE_MSG_INVITATION_RESPONSE = "invitationResponse";

    public static final String REMOTE_MSG_INVITATION_ACCEPTED = "accepted";
    public static final String REMOTE_MSG_INVITATION_REJECTED = "rejected";
    public static final String REMOTE_MSG_INVITATION_CANCELLED = "cancelled";

    public static final String REMOTE_MSG_MEETING_ROOM = "meetingRoom";

    public static HashMap<String, String> getRemoteMessageHeaders(){
        HashMap<String, String> headers = new HashMap<>();
        headers.put(
                Constants.REMOTE_MSG_AUTHORIZATION,
                "key=AAAAGpmJGHU:APA91bExVwru1bk0zocZuaUcVWUS-ew6FzcRzi-eu5e-6-9jruGJHZJCbaKZQ0zObA8xLpxd-MK0EhmBygCKfucG_6mjDUDfKW4LsyJlG_AaH2scqGNNOqSYbMpm3Tn5iROOEhKPt607"
        );
        headers.put(Constants.REMOTE_MSG_CONTENT_TYPE, "application/json");
        return headers;
    }

    //Message
    public static  HashMap<String, String> remoteMsgHeaders = null;
    public static HashMap<String, String> getRemoteMsgHeaders(){

        if (remoteMsgHeaders == null) {
             remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_AUTHORIZATION,
                    "key=AAAAGpmJGHU:APA91bExVwru1bk0zocZuaUcVWUS-ew6FzcRzi-eu5e-6-9jruGJHZJCbaKZQ0zObA8xLpxd-MK0EhmBygCKfucG_6mjDUDfKW4LsyJlG_AaH2scqGNNOqSYbMpm3Tn5iROOEhKPt607"
            );
            remoteMsgHeaders.put(
                    Constants.REMOTE_MSG_CONTENT_TYPE,
                    "application/json"
            );
        }
        return remoteMsgHeaders;
    }


    public static final String API_KEY = "VcGhQdTS*7k-dgeCW5gVj38dHFnjAV_gk54vS2x5DLu%=FWq+$R&cgaJ-bh=y58snZ2msf_74^qQ9w$C39k$PYW#nD4&KV?5gG5JvMVBX=HbS8nYx?jfa7ufZc3zfW%s#agDYxjwWb=j9ZMcMeNd5H2+bbX97uXgUr8=BHEqcw3V-Z!+gEn*m4NzZS*Y=CpWtM9*k?U9d^zLh77R=_rj+$!+A2WK8uJb9g8v*#aC4wBaV#JXcV#RScR%RHWvFHYtey3P4+hkCu#-W$P=t_MENPFhF?2LS*^tKhx-ug_cdU_6sp7n3vvypD*XSM2Zd&hE-+BrLMGJPfMbyU3dHQFBVrf!HMYsRy9V$Wh-yJa=wEwe*4aWHy&HadM^m68NS3PEf$9p8hs$cEh8bmTV7JXEebUY=R!gJL44tum+yc**m_2JaJ*_GeE_YcxR7JfxKdHydGSr!?ZRX#^vp_2S3mDADXtZ3u9P8=rjrmEMHCVHFENp+cnPgV9D3mxvb%ZE*McN2JjySWC#D_F*8TYf#*4*MVQB*WQ-TYTX*7%5Z7jtZq_J7r$_M354h4+*ER4shBVzuAVpguaXASv9NXG463Y3CBw3nYPv2m69k2zRb4T8EqnN$Cs4zvXAqHvKkn6c+n$ZQrBuGj_6-cmaw4vCpvufE!9RZXGpw^FU3SKm#gsC-6qPx4&7jb33=Ms-5M_Xcv-RtLPhRjjgb-ShLFeF3cEDB^w_sRgwTV&a9BG@22d@QV$3dB^unBFD6j-YSbJUHrWWhBGPrkvZBgaYgyJvK#K2_xkYZaUKAw4CvWA_3wZxgzkJ?RBUqw5E_u7U&^t=$avpuepaPc33g8NEwYKY*+SsEHB_Afe8MzB76s*@gb39P_ks+R5RNC%Nw#dn7qr8%m5fH=X=K-h_Zc&jdSBFYU#*qDgNHaNpqFWjXrrQGZ#qz%6#QE%fDXHeA?tzSa6mqwX&xbNet&&&W94UpRaMVu5RXqtL4jxUt-?$*xhK9Jj3vD&XSU+p7sh=yEy4@JXzwKUP^bH=3ABE=W_N^w*GUGMP6R&vxhUCh$Qu#A&mA8Z=RZ=YZkM$s9F@j^tpH8$&^JUcsJs2=tYXQ7an6&8mpJD%+$*BZ@gNd_H=umpsn=g7TV-#Ckb&#R?-nWL^G+4jtNQ&yfZ-xt*rWt9J3mUe%UJTXK^_zJM=5G%gymXH#a!3$KN=&F&Wgms&+KKh9xdbRu8xXuW!89P!7=FTK=-sCw6ehrB8nGf-T5@c9%GPGp&?Rdrkt43K3XrTgFV_Sz?rScVt=+Rzb542jNBGBE&8R+MvZn22tFX#HFR+n#Y3Cq_jnvAFhq4^bWun==%cgsQNGX@EjUJ8L!jzA$k!#b%?b3B*q2JPxf%#VH7hHjKg2eBm6=Ag4Z^wKcp=gmfULqGPhU$43teu!EuBZzd*K_ypUu-hn#vt@rPuXw#DTg%3!fd&32P?qCaxxsgCsV5_ZTf=gAKYL+9p3TLKE?bqY-LA58%XZjA*_s*xF9aW^!9?_ShKM55eG+FZsD9GDXBZ8V7b=suqqz^T!DH4c?Em?GYuNT?4p&&#avxjFdPD%AR8&&-n^dZUQXpxc4+D65u9=dM?eVRnp4n#53y9eUC4pNVce$Pezb#tD$v2XUxb5=@Q$Q9Y4n2em=xc_S-nMQGhF%5tCR+kc!NBah=5?sHCXVTN&7!q4bVs*!BbB@yMPAWzU5v$rNn@2NL_=+@?Y%Svc&!2VRuEy&29v-7b_5DcmC8HC-K^Eft!z9Zn?6PCywd4jg55%mygd-Qu-GEy7W6#=B?h8FDThSxmqX_j6M72EMKN-J=r3cNxY88h+MtP9JXgeddDVPeTL!AT^@Z%YZW!?FH%bwWPaWn*EnNGKALxncj8X@hfYa*GLvj??nQm@f?@3EbksY9EC*_!w^DvjPd7dyv8Q^m2cF_Mr#$ff7Y!g*p-gWH!pLULYxTQDcyy*TX$ewUF3m+ZSJH8?!u=Q?@Lm=Su-LEZRueGyF_bBKv#xx8F9%j-SNU_A*787k2aXWMs_4S4s%C_3e-nNwaAYB!d4@TEap+T";


}
