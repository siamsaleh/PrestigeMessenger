package com.prof.prestigemessenger.adapters;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.databinding.ItemContainerRecentConversionBinding;
import com.prof.prestigemessenger.listener.ConversationListener;
import com.prof.prestigemessenger.models.ChatMessage;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.utilities.Constants;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final ConversationListener conversationListener;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversationListener conversationListener) {
        this.chatMessages = chatMessages;
        this.conversationListener = conversationListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversionBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder{
        ItemContainerRecentConversionBinding binding;

        ConversionViewHolder(ItemContainerRecentConversionBinding itemContainerRecentConversionBinding){
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        @SuppressLint("ResourceAsColor")
        void setData(ChatMessage chatMessage){
            String decryptedMSG = "";
            try {
                decryptedMSG = decrypt(chatMessage.message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            binding.recentMessageTxt.setText(decryptedMSG);
            //binding.recentMessageTxt.setText(chatMessage.message);
            binding.itemProfileImage.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.nameTxt.setText(chatMessage.conversionName);
            binding.timeTxt.setText(getReadableDateTime(chatMessage.dateObject));
            binding.getRoot().setOnClickListener(v -> {
                User user = new User();
                user.id = chatMessage.conversionId;
                user.name = chatMessage.conversionName;
                user.image = chatMessage.conversionImage;
                conversationListener.onConversationClicked(user);
            });

            //Senn Unseen
            if (chatMessage.lastMessageId.equals(mAuth.getCurrentUser().getUid())) {
                if (Integer.parseInt(chatMessage.receiverUnseenMessages) != 0) {
                    binding.unseenCountTxt.setVisibility(View.VISIBLE);
                    binding.unseenCountTxt.setText(chatMessage.receiverUnseenMessages);
                    binding.recentMessageTxt.setTextColor(Color.parseColor("#D1B03F"));
                }else {
                    binding.unseenCountTxt.setVisibility(View.INVISIBLE);
                    binding.recentMessageTxt.setTextColor(Color.parseColor("#727272"));
                }
            }

        }

        private String decrypt(String data) throws Exception {
            SecretKeySpec key = generateKey(Constants.API_KEY);
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte [] decVal = Base64.decode(data, Base64.DEFAULT);
            byte [] decVal2 = cipher.doFinal(decVal);
            String decrypterdValue = new String(decVal2);
            return decrypterdValue;
        }
        private SecretKeySpec generateKey(String password) throws  Exception{
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = password.getBytes("UTF-8");
            digest.update(bytes, 0, bytes.length);
            byte[] key = digest.digest();
            SecretKeySpec secretKeySpec =new SecretKeySpec(key, "AES");
            return secretKeySpec;
        }
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
