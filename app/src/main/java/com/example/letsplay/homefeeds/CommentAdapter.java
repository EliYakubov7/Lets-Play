package com.example.letsplay.homefeeds;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letsplay.R;
import com.example.letsplay.objects.Comment;
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

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder>  implements Serializable {

    private List<Comment> commentList;
    private Context context;

    //Comment listener to get in to the user profile
    public interface  CommentListener{
        void onUserClick(int position, View v);
    }

    private CommentListener listener;

    public CommentAdapter(List<Comment> commentList, Context context) {
        this.commentList = commentList;
        this.context = context;
    }

    public void setListener(CommentListener listener) {
        this.listener = listener;
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView userProfileIv;
        private TextView nameTv,bodyTv,timeTv,dateTv;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            userProfileIv = itemView.findViewById(R.id.comment_cell_profile_iv);
            nameTv = itemView.findViewById(R.id.comment_cell_name_tv);
            bodyTv = itemView.findViewById(R.id.comment_cell_body_tv);
            timeTv=itemView.findViewById(R.id.comment_cell_time_tv);
            dateTv= itemView.findViewById(R.id.comment_cell_date_tv);

            userProfileIv.setOnClickListener(this);
            nameTv.setOnClickListener(this);
        }
        @Override
        public void onClick(View v) {
            listener.onUserClick(getAdapterPosition(),v);
        }

    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_cell,parent,false);
        CommentViewHolder viewHolder = new CommentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = commentList.get(position);
        holder.userProfileIv.setImageResource(android.R.color.transparent);
        if(UsersManager.getUserByKey(comment.getAuthor()).getProfileImageURL()!=null)
            setImageFromStorage(UsersManager.getUserByKey(comment.getAuthor()),holder.userProfileIv);
        else
            holder.userProfileIv.setImageResource(R.drawable.empty_profile_photo);
        holder.nameTv.setText(UsersManager.getUserByKey(comment.getAuthor()).getFirstName().concat(" "+UsersManager.getUserByKey(comment.getAuthor()).getLastName()));
        holder.bodyTv.setText(comment.getBody());
        holder.timeTv.setText(comment.getTime());
        holder.dateTv.setText(comment.getDate());
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    /*Set the image from the storage*/
    private void setImageFromStorage(User user, final ImageView profilePhoto){
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
