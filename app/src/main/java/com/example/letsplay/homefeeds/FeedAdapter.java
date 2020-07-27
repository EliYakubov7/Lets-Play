package com.example.letsplay.homefeeds;

import android.content.Context;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.chats.MessageAdapter;
import com.example.letsplay.objects.Feed;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> implements Serializable {

    private List<Feed> feedList;
    private Context context;

    //Listener for like and comment events
    interface FeedListener {
        void onCommentClick(int position, View v);

        void onLikeClick(int position, View v);

        void onProfileImageClick(int position, View v);
    }

    private FeedListener listener;

    public FeedAdapter(List<Feed> feedList, Context context) {
        this.feedList = feedList;
        this.context = context;
    }

    public void setFeedListener(FeedListener listener) {
        this.listener = listener;
    }

    public class FeedViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView profileImageIv;
        private LinearLayout likeIv, commentIv;
        private ImageButton likeIb;
        private TextView nameTv, bodyTv, timeTv, dateTv, locationTv, noOfLikesTv, noOfCommentsTv;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageIv = itemView.findViewById(R.id.feed_cell_profile_iv);
            likeIv = itemView.findViewById(R.id.feed_cell_like_iv);
            likeIb = itemView.findViewById(R.id.feed_cell_like_ib);
            commentIv = itemView.findViewById(R.id.feed_cell_comment_iv);
            nameTv = itemView.findViewById(R.id.feed_cell_name_tv);
            bodyTv = itemView.findViewById(R.id.feed_cell_body_tv);
            timeTv = itemView.findViewById(R.id.feed_cell_time_tv);
            dateTv = itemView.findViewById(R.id.feed_cell_date_tv);
            locationTv = itemView.findViewById(R.id.feed_cell_location_tv);
            noOfLikesTv = itemView.findViewById(R.id.feed_cell_number_of_likes_tv);
            noOfCommentsTv = itemView.findViewById(R.id.feed_cell_number_of_comments_tv);

            likeIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onLikeClick(getAdapterPosition(), v);
                }
            });

            commentIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCommentClick(getAdapterPosition(), v);
                }
            });

            profileImageIv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onProfileImageClick(getAdapterPosition(), v);
                }
            });

            nameTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onProfileImageClick(getAdapterPosition(), v);
                }
            });
        }
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.feed_cell, parent, false);
        FeedAdapter.FeedViewHolder viewHolder = new FeedViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        Feed feed = feedList.get(position);
        holder.profileImageIv.setImageResource(android.R.color.transparent);
        if (UsersManager.getUserByKey(feed.getAuthor()).getProfileImageURL() != null)
            setImageFromStorage(UsersManager.getUserByKey(feed.getAuthor()), holder.profileImageIv);
        else
            holder.profileImageIv.setImageResource(R.drawable.empty_profile_photo);
        holder.nameTv.setText(UsersManager.getUserByKey(feed.getAuthor()).getFirstName().concat(" " + UsersManager.getUserByKey(feed.getAuthor()).getLastName()));
        holder.bodyTv.setText(feed.getBody());
        holder.timeTv.setText(feed.getTime());
        holder.dateTv.setText(feed.getDate() + ", ");
        holder.locationTv.setText(UsersManager.getUserByKey(feed.getAuthor()).getLocation());
        holder.noOfLikesTv.setText(String.valueOf(feed.getLikes()));
        holder.noOfCommentsTv.setText(String.valueOf(feed.getNoOfComments()));


        // Set like button color
        if(!ProfileActivity.isAGuest) {
            if (feed.getLikesControl().containsKey(ProfileActivity.currentSignedInUser.getUid())) {
                //if the user liked the post before
                if (feed.getLikesControl().get(ProfileActivity.currentSignedInUser.getUid()))
                    ImageViewCompat.setImageTintList(holder.likeIb, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorAccent)));
                else
                    ImageViewCompat.setImageTintList(holder.likeIb, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGrayLike)));
            }
            else
                ImageViewCompat.setImageTintList(holder.likeIb, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGrayLike)));
        }
        else {
            ImageViewCompat.setImageTintList(holder.likeIb, ColorStateList.valueOf(ContextCompat.getColor(context, R.color.colorGrayLike)));
        }

    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    /*Set the image from the storage*/
    private void setImageFromStorage(User user, final ImageView profilePhoto) {
        //Upload profile photo from storage
        if (user.getProfileImageURL() != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReference().child(user.getProfileImageURL());
            photoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(context).load(uri).into(profilePhoto);
                }
            });
        }
    }

}
