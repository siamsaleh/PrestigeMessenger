package com.prof.prestigemessenger.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Story implements Serializable {
    public String senderId, senderImage, time, storyImage, senderName, storyId;
//    public StorySeen storySeen;
    public Date dateObject;
    //public ArrayList<String> storyImageArray;
    public ArrayList<String> storySeenList = new ArrayList<>();
}
