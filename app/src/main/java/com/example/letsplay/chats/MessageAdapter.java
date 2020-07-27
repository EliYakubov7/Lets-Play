package com.example.letsplay.chats;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Outline;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.objects.Message;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.google.firebase.auth.FirebaseAuth;

import java.io.Serializable;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> implements Serializable {

    private List<Message> messageList;
    private Context context;
    private String otherUserFirstName;
    //Define the view type - my side messages or the other side messages
    private final static int TYPE_MY_SIDE = 1, TYPE_OTHER_SIDE = 2;

    public MessageAdapter(List<Message> messageList, Context context,String otherUserFirstName) {
        this.context = context;
        this.messageList = messageList;
        this.otherUserFirstName=otherUserFirstName;
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        private CardView msgCardView;
        private TextView nameTv, msgBodyTv, timeTv, dateTv;
        private LinearLayout linearLayout;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            msgCardView = itemView.findViewById(R.id.message_card_view);
            nameTv = itemView.findViewById(R.id.message_cell_name_tv);
            msgBodyTv = itemView.findViewById(R.id.message_cell_body_tv);
            timeTv = itemView.findViewById(R.id.message_cell_time_tv);
            dateTv = itemView.findViewById(R.id.message_cell_date_tv);
            linearLayout = itemView.findViewById(R.id.chat_cell_linear_layout);
        }
    }


    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_cell, parent, false);
        MessageAdapter.MessageViewHolder viewHolder = new MessageAdapter.MessageViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.nameTv.setText(message.getSender().equals(ProfileActivity.currentSignedInUser.getUid())?
                ProfileActivity.currentSignedInUser.getFirstName() : otherUserFirstName);
        holder.msgBodyTv.setText(message.getMessage());
        holder.timeTv.setText(message.getTime());
        holder.dateTv.setText(message.getDate());

       // LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        if (getItemViewType(position) == TYPE_MY_SIDE) {
            holder.msgBodyTv.setBackground(context.getResources().getDrawable(R.drawable.my_message));
          //  params.gravity = Gravity.START;
            holder.linearLayout.setGravity(Gravity.RIGHT);
        } else {
            holder.msgBodyTv.setBackground(context.getResources().getDrawable(R.drawable.other_message));
        //    params.gravity = Gravity.END;
          //  holder.linearLayout.setLayoutParams(params);
            holder.linearLayout.setGravity(Gravity.LEFT);
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        int type = 0;
        if (auth.getUid() != null) {
            if (messageList.get(position).getSender().equals(auth.getUid()))
                type = TYPE_MY_SIDE;
            else
                type = TYPE_OTHER_SIDE;
        }
        return type;
    }
}
