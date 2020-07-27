package com.example.letsplay.chats;

import android.content.Context;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.letsplay.R;
import com.example.letsplay.objects.Chat;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> implements Serializable, Filterable {

    private List<Chat> chatList;
    private List<Chat> chatListFull;
    private Context context;

    //Chat click listener
    interface ChatListener {
        void onChatClick(int position, View v);
    }

    private ChatListener chatListener;

    public ChatAdapter(List<Chat> chatList, Context context) {
        this.chatList = chatList;
        this.context = context;

        chatListFull = new ArrayList<>();
        chatListFull.addAll(chatList);
    }

    public void setChatListener(ChatListener chatListener) {
        this.chatListener = chatListener;
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView nameTv, lastMsgTv;


        public ChatViewHolder(@NonNull final View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.chat_cell_iv);
            nameTv = itemView.findViewById(R.id.chat_cell_name_tv);
            lastMsgTv = itemView.findViewById(R.id.chat_cell_last_msg_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chatListener.onChatClick(getAdapterPosition(), v);
                }
            });
        }
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_cell, parent, false);
        ChatViewHolder viewHolder = new ChatViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = chatList.get(position);
        holder.imageView.setImageResource(android.R.color.transparent);
        if (UsersManager.getUserByKey(chat.getOtherSideUserKey()).getProfileImageURL() != null)
            setImageFromStorage(UsersManager.getUserByKey(chat.getOtherSideUserKey()), holder.imageView);
        else
            holder.imageView.setImageResource(R.drawable.empty_profile_photo);
        holder.nameTv.setText(UsersManager.getUserByKey(chat.getOtherSideUserKey()).getFirstName().concat(" " + UsersManager.getUserByKey(chat.getOtherSideUserKey()).getLastName()));
        String lastMsg = "";
        if (chat.getMessages() != null && chat.getMessages().size() > 0)
            lastMsg = chat.getMessages().get(chat.getMessages().size() - 1).getMessage();

        if (!chat.isLastMessageSeen())
            holder.lastMsgTv.setTypeface(null, Typeface.BOLD);
        else
            holder.lastMsgTv.setTypeface(null, Typeface.NORMAL);
        holder.lastMsgTv.setText(lastMsg.length() > 30 ? lastMsg.substring(0, 31) + "..." : lastMsg);
    }


    @Override
    public int getItemCount() {
        return chatList.size();
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

    /*Methods to filter the users*/
    @Override
    public Filter getFilter() {
        return chatsFilter;
    }

    private Filter chatsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Chat> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(chatListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                for (Chat chat : chatListFull) {
                    if (UsersManager.getUserByKey(chat.getOtherSideUserKey()).getFirstName().toLowerCase(Locale.getDefault()).contains(filterPattern)
                            || UsersManager.getUserByKey(chat.getOtherSideUserKey()).getLastName().toLowerCase(Locale.getDefault()).contains(filterPattern))
                        filteredList.add(chat);
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chatList.clear();
            chatList.addAll((List<Chat>) results.values);
            if (chatList.size() < 3) {
                try {
                    Thread.sleep(280);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            notifyDataSetChanged();
        }
    };
}
