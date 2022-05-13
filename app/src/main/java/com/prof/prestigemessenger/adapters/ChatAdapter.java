package com.prof.prestigemessenger.adapters;

import android.graphics.Bitmap;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.prof.prestigemessenger.databinding.ItemContainerReceivedMessageBinding;
import com.prof.prestigemessenger.databinding.ItemContainerSentMessageBinding;
import com.prof.prestigemessenger.models.ChatMessage;
import com.prof.prestigemessenger.utilities.Constants;

import java.security.MessageDigest;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVE = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage = bitmap;
    }

    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(LayoutInflater.from(parent.getContext()),
                            parent, false
                    )
            );
        }else {
            return new ReceiveMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        }else {
            ((ReceiveMessageViewHolder)holder).setData(chatMessages.get(position), receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (chatMessages.get(position).senderId.equals(senderId))
            return VIEW_TYPE_SENT;
        else
            return VIEW_TYPE_RECEIVE;
    }

    ////////////////////View Holder Receiver Sender//////////////////////////////

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerSentMessageBinding binding;

        public SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage){
            String decryptedMSG = "";
            try {
                decryptedMSG = decrypt(chatMessage.message);
            } catch (Exception e) {
                e.printStackTrace();
            }
            binding.textMessage.setText(decryptedMSG);
            binding.textDateTime.setText(chatMessage.dateTime);
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

    static class ReceiveMessageViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReceivedMessageBinding binding;

        public ReceiveMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage, Bitmap receiverImageProfile){
            String decryptedMSG = "";
            try {
                decryptedMSG = decrypt(chatMessage.message);
            } catch (Exception e) {
                e.printStackTrace();
            }

            binding.textMessage.setText(decryptedMSG);
            //binding.textMessage.setText(chatMessage.message);
            binding.textDateTime.setText(chatMessage.dateTime);
            if (receiverImageProfile != null){
                binding.profileImage.setImageBitmap(receiverImageProfile);
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



}
