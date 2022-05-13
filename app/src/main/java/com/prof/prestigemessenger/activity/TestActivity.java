package com.prof.prestigemessenger.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;

import com.prof.prestigemessenger.databinding.ActivityTestBinding;
import com.prof.prestigemessenger.models.Story;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class TestActivity extends AppCompatActivity  {

    List<String> list;
    int counter;
    ActivityTestBinding binding;

    Story story;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_test);
        binding = ActivityTestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        story = (Story) getIntent().getSerializableExtra("story");

        list = new ArrayList<>();

        list.add(story.storyImage);
        list.add("https://images.pexels.com/photos/799443/pexels-photo-799443.jpeg?auto=compress&cs=tinysrgb&dpr=3&h=750&w=1260");
        list.add("https://images.pexels.com/photos/1535162/pexels-photo-1535162.jpeg?auto=compress&cs=tinysrgb&h=750&w=1260");
//        list.add("https://firebasestorage.googleapis.com/v0/b/prestige-messenger.appspot.com/o/story%2FCHpycIYrBVXGw7cWoREv4YTjyos1%2F9AADAF068133480A8A9A3239E0C87455.jpg?alt=media&token=582bbb3b-ef9a-4aaf-bea3-17407f09f020");
        //list.add("https://firebasestorage.googleapis.com/v0/b/prestige-messenger.appspot.com/o/story%2FCHpycIYrBVXGw7cWoREv4YTjyos1%2F9AADAF068133480A8A9A3239E0C87455.jpg?alt=media&token=582bbb3b-ef9a-4aaf-bea3-17407f09f020");

        //storiesProgressView = findViewById(R.id.stories);
        binding.storiesProgressView.setStoriesCount(list.size()); // <- set stories
        binding.storiesProgressView.setStoryDuration(2000); // <- set a story duration
        //storiesProgressView.setStoriesListener(this); // <- set listener
        //storiesProgressView.startStories(); // <- start progress

//        imageView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                storiesProgressView.skip();
//            }
//        });

        counter = 0;

        binding.itemProfileImage.setImageBitmap(getConversionImage(story.senderImage));
        binding.storyNameTxt.setText(story.senderName);
        if (story.dateObject != null) {
            binding.storyDateTxt.setText(getReadableDateTime(story.dateObject));
        }

        binding.itemProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Picasso.get().load(list.get(0)).into(binding.storyImage, new Callback() {
                    @Override
                    public void onSuccess() {

                        binding.storyImage.setVisibility(View.VISIBLE);

                        binding.storiesProgressView.startStories();
                        binding.storiesProgressView.setStoriesListener(new StoriesProgressView.StoriesListener() {
                            @Override
                            public void onNext() {
                                if (counter < list.size()){
                                    counter++;
                                    Picasso.get().load(list.get(counter)).into(binding.storyImage);
                                }
                            }

                            @Override
                            public void onPrev() {

                            }

                            @Override
                            public void onComplete() {
                                counter = 0;
                                binding.storyImage.setVisibility(View.GONE);
                                finish();
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
}