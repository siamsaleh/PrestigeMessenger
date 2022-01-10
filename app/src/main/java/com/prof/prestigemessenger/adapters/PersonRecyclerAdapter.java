package com.prof.prestigemessenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.prof.prestigemessenger.R;
import com.prof.prestigemessenger.activity.ChatActivity;
import com.prof.prestigemessenger.activity.UsersListActivity;
import com.prof.prestigemessenger.listener.UserListener;
import com.prof.prestigemessenger.models.User;
import com.prof.prestigemessenger.utilities.Constants;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonRecyclerAdapter extends FirestoreRecyclerAdapter<User, PersonRecyclerAdapter.PersonViewHolder> {

    private final UserListener userListener;
    Context context;

    public PersonRecyclerAdapter(FirestoreRecyclerOptions<User> options, UserListener userListener, Context context) {
        super(options);
        this.userListener = userListener;
        this.context = context;
    }

    class PersonViewHolder extends RecyclerView.ViewHolder {
        final CardView view;
        final TextView name;
        final CircleImageView imageView;

        PersonViewHolder(CardView v) {
            super(v);
            view = v;
            name = v.findViewById(R.id.name_txt);
            imageView = v.findViewById(R.id.item_profile_image);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final PersonViewHolder holder, @NonNull int position, @NonNull final User person) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.name.setText(person.name);
        holder.imageView.setImageBitmap(getUserImage(person.image));

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, ChatActivity.class).putExtra(Constants.KEY_USER, person)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });

//        if (userListener != null) {
//            holder.view.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    userListener.onUserClicked(person);
//                }
//            });
//        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_user, parent, false);

        return new PersonViewHolder(v);
    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }
}



