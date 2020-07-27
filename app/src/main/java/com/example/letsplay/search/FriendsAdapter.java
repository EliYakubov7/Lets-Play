package com.example.letsplay.search;

import android.content.Context;
import android.content.res.Resources;
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
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.example.letsplay.R;
import com.example.letsplay.chats.ChatAdapter;
import com.example.letsplay.objects.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder> implements Serializable, Filterable {

    private List<User> friendList;
    private List<User> friendListFull;
    private Context context;
    //Friend click listener
    interface FriendsListener{
        void onFriendClick(int position, View v);
    }
    private FriendsListener listener;

    public void setListener(FriendsListener listener) {
        this.listener = listener;
    }

    public FriendsAdapter(List<User> friendList, Context context) {
        this.friendList = friendList;
        this.context = context;

        friendListFull = new ArrayList<>(friendList);
    }

    public void setFriendListFull(List<User> friendList) {
        friendListFull=null;
        friendListFull = new ArrayList<>(friendList);
        removeDuplicates();
    }

    public class FriendsViewHolder extends RecyclerView.ViewHolder{

        private CircleImageView profileIv;
        private TextView nameTv,ageTv,genderTv,mainInstrumentTv,secondaryInstrumentTv,locationTv;
        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            profileIv = itemView.findViewById(R.id.friend_cell_iv);
            nameTv=itemView.findViewById(R.id.friend_cell_name_tv);
            ageTv = itemView.findViewById(R.id.friend_cell_age_tv);
            genderTv = itemView.findViewById(R.id.friend_cell_gender_tv);
            mainInstrumentTv = itemView.findViewById(R.id.friend_cell_main_instrument_tv);
            locationTv = itemView.findViewById(R.id.friend_cell_location_tv);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onFriendClick(getAdapterPosition(),v);
                }
            });
        }
    }

    @NonNull
    @Override
    public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_cell,parent,false);
        FriendsViewHolder viewHolder = new FriendsViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsViewHolder holder, int position) {
        User friend = friendList.get(position);
        holder.profileIv.setImageURI(null);

        if(friend.getProfileImageURL()!=null)
            setImageFromStorage(friend,holder.profileIv);
        else
            holder.profileIv.setImageResource(R.drawable.empty_profile_photo);
        holder.nameTv.setText(friend.getFirstName().concat(" "+friend.getLastName()));
        holder.ageTv.setText(String.valueOf(friend.getAge())+ ",");
        holder.genderTv.setText(((friend.getGender().equals("Male")||(friend.getGender().equals("זכר")))?
                context.getResources().getString(R.string.gender_male):context.getResources().getString(R.string.gender_female)));
        if(friend.getMainInstrument()!=null)
            holder.mainInstrumentTv.setText(context.getResources().getString(friend.getMainInstrument().getInstrumentResId()));
        else
            holder.mainInstrumentTv.setText("");
        holder.locationTv.setText(friend.getLocation());
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }


    /*Methods to filter the users*/
    @Override
    public Filter getFilter() {
        return friendsFilter;
    }

    private Filter friendsFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<User> filteredList = new ArrayList<>();
            if(constraint==null||constraint.length()==0){
                filteredList.addAll(friendListFull);
            }
            else {
                String filterPattern = constraint.toString().toLowerCase(Locale.getDefault()).trim();
                for(User user: friendListFull){
                    if(user.getFirstName().toLowerCase(Locale.getDefault()).contains(filterPattern)
                            ||user.getLastName().toLowerCase(Locale.getDefault()).contains(filterPattern))
                        filteredList.add(user);
                }
            }
            FilterResults results = new FilterResults();
            results.values=filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            friendList.clear();
            friendList.addAll((List<User>)results.values);
            if(friendList.size()<3) {
                try {
                    Thread.sleep(280);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
                notifyDataSetChanged();
        }
    };

    /*Removes duplicates when filtering results*/
    private void removeDuplicates(){
        for(int i=0;i<friendListFull.size();i++)
            for(int j=0;j<friendListFull.size();j++)
                if(i!=j)
                    if(friendListFull.get(i).getUid().equals(friendListFull.get(j).getUid()))
                        friendListFull.remove(j);
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

    public void addToFriendListFull(User user){
        friendListFull.add(user);
        removeDuplicates();
    }


}
