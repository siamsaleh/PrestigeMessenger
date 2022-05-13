package com.prof.prestigemessenger.listener;

import com.prof.prestigemessenger.models.User;

public interface UserListenerCall {
    void initiateVideoMeeting(User user);
    void initiateAudioMeeting(User user);
}
