package com.prof.prestigemessenger.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.prof.prestigemessenger.activity.TestActivity;
import com.prof.prestigemessenger.databinding.ActivityHomeBinding;
import com.prof.prestigemessenger.databinding.ItemContainerStoryBinding;
import com.prof.prestigemessenger.models.Story;
import com.prof.prestigemessenger.models.StoryUser;
import com.prof.prestigemessenger.utilities.Constants;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {

    public List<Story> stories;
    public  List<StoryUser> storyUsers;
    //private final StoryListener storyListener;
    Context context;
    ActivityHomeBinding bindingHome;
    int counter;

    boolean playPauseBoolean = true;

//    public StoryAdapter(List<Story> stories, Context context, ActivityHomeBinding bindingHome) {
//        this.stories = stories;
//        this.context = context;
//        this.bindingHome = bindingHome;
//    }

    public StoryAdapter(List<StoryUser> storyUsers, Context context, ActivityHomeBinding bindingHome) {
        this.storyUsers = storyUsers;
        this.context = context;
        this.bindingHome = bindingHome;
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new StoryViewHolder(
                ItemContainerStoryBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, @SuppressLint("RecyclerView") int position) {
        //holder.setData2(stories.get(position), position);
        holder.setData(storyUsers.get(position), position);


        bindingHome.storyImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playPauseBoolean) {
                    bindingHome.storiesProgressView.pause();
                    playPauseBoolean = false;
                }else {
                    bindingHome.storiesProgressView.resume();
                    playPauseBoolean = true;
                }
            }
        });

//        holder.binding.itemProfileImage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                holder.newActivity(stories.get(position));
//            }
//        });




//        Glide.with(context)
//                .load(stories.get(position).storyImage)
//                .placeholder(R.drawable.background_empty)
//                .centerCrop()
//                .into(bindingHome.storyImage);
    }

    @Override
    public int getItemCount() {
        return storyUsers.size();
    }

    class StoryViewHolder extends RecyclerView.ViewHolder{
        ItemContainerStoryBinding binding;

        StoryViewHolder(ItemContainerStoryBinding itemContainerStoryBinding){
            super(itemContainerStoryBinding.getRoot());
            binding = itemContainerStoryBinding;
        }

        void setData(StoryUser storyUser, int position){
            binding.storyNameTxt.setText(storyUser.userStories.get(0).senderName);
            binding.itemProfileImage.setImageBitmap(getConversionImage(storyUser.userStories.get(0).senderImage));
            binding.itemStoryCountTxt.setText(storyUser.userStories.size() + "");

            counter = 0;

            binding.itemProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<String> list = new ArrayList<>();
                    for (int i = 0; i < storyUser.userStories.size(); i++) {
                        list.add(storyUser.userStories.get(i).storyImage);
                        Log.d("NEWWWW", "onClick: "+storyUser.userStories.get(i).storyImage);
                    }

                    bindingHome.otherLayout.setVisibility(View.GONE);
                    bindingHome.storyImageLayout.setVisibility(View.VISIBLE);

                    //Home
                    bindingHome.itemProfileImage.setImageBitmap(getConversionImage(storyUser.userStories.get(0).senderImage));
                    bindingHome.storyNameTxt.setText(storyUser.userStories.get(0).senderName);
                    if (storyUser.userStories.get(0).dateObject != null) {
                        bindingHome.storyDateTxt.setText(getReadableDateTime(storyUser.userStories.get(0).dateObject));
                    }

                    bindingHome.storyImageLayout.setVisibility(View.VISIBLE);
                    bindingHome.otherLayout.setVisibility(View.GONE);
                    bindingHome.storiesProgressView.setStoriesCount(list.size()); // <- set stories
                    bindingHome.storiesProgressView.setStoryDuration(5000); // <- set a story duration

                    Picasso.get().load(list.get(counter)).into(bindingHome.storyImage, new Callback() {
                        @Override
                        public void onSuccess() {

                            bindingHome.storyImageLayout.setVisibility(View.VISIBLE);

                            bindingHome.storiesProgressView.startStories();
                            bindingHome.storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
                                @Override
                                public void onNext() {
                                    if (counter < list.size()){
                                        counter++;
                                        Picasso.get().load(list.get(counter)).into(bindingHome.storyImage);
                                    }
                                }

                                @Override
                                public void onPrev() {

                                }

                                @Override
                                public void onComplete() {
                                    counter = 0;
                                    bindingHome.storyImageLayout.setVisibility(View.GONE);
                                    bindingHome.otherLayout.setVisibility(View.VISIBLE);
                                    bindingHome.itemProfileImage.setImageBitmap(null);

                                    //Seen List
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    final DatabaseReference storySeenRef = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.KEY_COLLECTION_STORY).child(storyUser.userStories.get(0).senderId)
                                            .child(storyUser.userStories.get(0).storyId);

                                    HashMap seen = new HashMap<>();
                                    seen.put(mAuth.getUid(), mAuth.getUid());
                                    storySeenRef.child("storySeen").updateChildren(seen);

                                    storyUsers.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });



                }
            });
        }

        void setData2(Story story, int position){

            //storyListener.onStoryClicked(story);

            binding.storyNameTxt.setText(story.senderName);
            binding.itemProfileImage.setImageBitmap(getConversionImage(story.senderImage));

            counter = 0;

            //Set Image
            binding.itemProfileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    List<String> list = new ArrayList<>();
                    list.add(story.storyImage);

                    bindingHome.otherLayout.setVisibility(View.GONE);
                    bindingHome.storyImageLayout.setVisibility(View.VISIBLE);

                    //Home
                    bindingHome.itemProfileImage.setImageBitmap(getConversionImage(story.senderImage));
                    bindingHome.storyNameTxt.setText(story.senderName);
                    if (story.dateObject != null) {
                        bindingHome.storyDateTxt.setText(getReadableDateTime(story.dateObject));
                    }

                    bindingHome.storyImageLayout.setVisibility(View.VISIBLE);
                    bindingHome.otherLayout.setVisibility(View.GONE);
                    bindingHome.storiesProgressView.setStoriesCount(list.size()); // <- set stories
                    bindingHome.storiesProgressView.setStoryDuration(5000); // <- set a story duration

                    //Date
                    if (story.time != null) {
                        DateFormat dateFormat = new SimpleDateFormat("dd MMM hh:mm aa");
                        DateFormat inputFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        try {
                            Date date = inputFormat.parse(story.time);
                            dateFormat.format(date);
                            bindingHome.storyDateTxt.setText(dateFormat.format(date));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }


                    Picasso.get().load(list.get(0)).into(bindingHome.storyImage, new Callback() {
                        @Override
                        public void onSuccess() {

                            bindingHome.storyImageLayout.setVisibility(View.VISIBLE);

                            bindingHome.storiesProgressView.startStories();
                            bindingHome.storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
                                @Override
                                public void onNext() {
                                    if (counter < list.size()){
                                        counter++;
                                        Picasso.get().load(list.get(counter)).into(bindingHome.storyImage);
                                    }
                                }

                                @Override
                                public void onPrev() {

                                }

                                @Override
                                public void onComplete() {
                                    counter = 0;
                                    bindingHome.storyImageLayout.setVisibility(View.GONE);
                                    bindingHome.otherLayout.setVisibility(View.VISIBLE);
                                    bindingHome.itemProfileImage.setImageBitmap(null);
                                    //Seen List
                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    final DatabaseReference storySeenRef = FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.KEY_COLLECTION_STORY).child(story.storyId);

                                    HashMap seen = new HashMap<>();
                                    seen.put(mAuth.getUid(), mAuth.getUid());
                                    storySeenRef.child("storySeen").updateChildren(seen);

                                    stories.remove(position);

                                    notifyDataSetChanged();
                                }
                            });
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });












//                    bindingHome.storyImageLayout.setVisibility(View.VISIBLE);
//                    bindingHome.otherLayout.setVisibility(View.GONE);
//                    bindingHome.storiesProgressView.setStoriesCount(stories.size()); // <- set stories
//                    bindingHome.storiesProgressView.setStoryDuration(5000); // <- set a story duration
//
//                    List<String> list = new ArrayList<>();
//                    list.add(story.storyImage);
//
//                    counter = 0;
//
//                    bindingHome.storiesProgressView.startStories(); // <- start progress
//                    bindingHome.storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
//                        @Override
//                        public void onNext() {
//                            //if (counter < list.size()){
//                                //Picasso.get().load(list.get(counter)).into(bindingHome.storyImage);
//                                Glide.with(context)
//                                //.load(list.get(0))
//                                .load(list.get(0))
//                                .placeholder(R.drawable.background_empty)
//                                .centerCrop()
//                                .into(bindingHome.storyImage);
//                                //counter++;
//                            //}
//                        }
//
//                        @Override
//                        public void onPrev() {
//
//                        }
//
//                        @Override
//                        public void onComplete() {
//                            counter = 0;
//                            bindingHome.storyImageLayout.setVisibility(View.GONE);
//                            bindingHome.otherLayout.setVisibility(View.VISIBLE);
//                            bindingHome.storyImage.setImageResource(R.drawable.background_empty);
//                            list.clear();
//                            bindingHome.storiesProgressView.destroy();
//                        }
                    //}); // <- set listener

                    //bindingHome.storiesProgressView.destroy();
                }
            });

//            ////play true,
//            bindingHome.playPause.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (bindingHome.playPause.isSelected()){
//                        Toast.makeText(context, "Play", Toast.LENGTH_SHORT).show();
//                    }else {
//                        Toast.makeText(context, "Pause", Toast.LENGTH_SHORT).show();
//                    }
//                    Toast.makeText(context, "Hlw", Toast.LENGTH_SHORT).show();
//                    Log.d("GGGGGG", "onClick: ");
////                    if (bindingHome.playPause.getDrawable().equals(R.drawable.ic_baseline_play_arrow_24)){
////                        bindingHome.playPause.setImageResource(R.drawable.ic_baseline_pause_24);
////                    }
//                }
//            });

//            bindingHome.addStoryProfile.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.d("GGGGGG", "onClick: ");
//                }
//            });













//            Glide.with(context)
//                    .load(story.storyImage)
//                    .placeholder(R.drawable.background_empty)
//                    .centerCrop()
//                    .into(binding.itemStoryImage);

//            binding.itemProfileImage.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    //binding.itemStoryImage.setVisibility(View.VISIBLE);
//                }
//            });




            //binding.itemProfileImage.setImageBitmap(getConversionImage(story.senderImage));

//            binding.nameTxt.setText(chatMessage.conversionName);
//            binding.recentMessageTxt.setText(chatMessage.message);
//            binding.timeTxt.setText(getReadableDateTime(chatMessage.dateObject));
//            binding.getRoot().setOnClickListener(v -> {
//                User user = new User();
//                user.id = chatMessage.conversionId;
//                user.name = chatMessage.conversionName;
//                user.image = chatMessage.conversionImage;
//                conversationListener.onConversationClicked(user);
//            });
        }

        private Bitmap getConversionImage(String encodedImage){
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }

        private String getReadableDateTime(Date date){
            if (new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date).equals(new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date()))){
                return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date);
            }else {
                return new SimpleDateFormat("dd MMM", Locale.getDefault()).format(date);
            }
        }

        public void newActivity(Story story) {
            Intent intent = new Intent(context, TestActivity.class);
            intent.putExtra("story", story);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
