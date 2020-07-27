package com.example.letsplay.objects;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersManager {

    private static ArrayList<User> users = new ArrayList<>();
    private static String TAG = "tests";

    private static LoadListener listener;

    public interface LoadListener{
        void onFinishLoad();
    }

    public static void setListener(LoadListener listener) {
        UsersManager.listener = listener;
    }

    public static void loadUsers(){
        users.clear();
        FirebaseDatabase.getInstance().getReference("Users").orderByKey().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
               if(snapshot.exists()){
                   for(DataSnapshot user:snapshot.getChildren())
                       users.add(user.getValue(User.class));
                   listener.onFinishLoad();
               }
               else
                  listener.onFinishLoad();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG,error.getDetails());
            }
        });
    }

    public static User getUserByKey(String key){
        for(User user: users){
            if(user.getUid().equals(key))
                return user;

        }
        return null;
    }

    public static ArrayList<User> getUsers() {
        return users;
    }

}
