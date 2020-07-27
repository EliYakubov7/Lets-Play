package com.example.letsplay.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.objects.User;
import com.google.firebase.database.FirebaseDatabase;

public class ClosingService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        ProfileActivity.currentSignedInUser.setStatus(User.Status.OFFLINE);
        FirebaseDatabase.getInstance().getReference("Users").child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
        Log.d("tests","service down");
    }
}
