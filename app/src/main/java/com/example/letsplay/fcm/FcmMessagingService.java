package com.example.letsplay.fcm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.application.MyApplication;
import com.example.letsplay.chats.ChatWindowFragment;
import com.example.letsplay.objects.Chat;
import com.example.letsplay.objects.Message;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Map;


public class FcmMessagingService extends FirebaseMessagingService {

    private NotificationManager manager;
    private final int NOTIFICATION_ID = 1;
    private int LIKES_NOTIF_ID = 2;


    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FCM Log", "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d("tests", "From: " + remoteMessage.getSenderId());

        Map<String, String> payload = remoteMessage.getData();

        if (remoteMessage.getData().size() > 0) {

            if (payload.containsKey("likerUid")) { // Someone liked a post
                // Keys: likerUid, likedUid , likedUserName
                Log.d("testy", "Message data payload: " + remoteMessage.getData());

                manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = null;
                // set notification channel
                if (Build.VERSION.SDK_INT >= 26) {
                    channelId = "likes_channel";
                    CharSequence channelName = "Likes";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                    manager.createNotificationChannel(notificationChannel);
                }

                Intent resultIntent = new Intent(this, ProfileActivity.class);
                /*Creates new notification*/
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
                builder.setSmallIcon(R.drawable.ic_baseline_thumb_up_24);
                // New like!
                // likerUid has liked your post!
                builder.setContentTitle("New Like!");
                builder.setContentText(payload.get("likedUserName") + " has liked your post!");

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);

                // notification settings and build
                Notification notification = builder.build();
                notification.defaults = Notification.DEFAULT_ALL;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                manager.notify(LIKES_NOTIF_ID, notification);

                if (remoteMessage.getNotification() != null) {
                    Log.d("testy", "Message Notification Body: " + remoteMessage.getNotification());
                }
            } else if (payload.containsKey("commentAuthor")) { // if someone comment on a post
                // keys: commentAuthor, posterUid, commenterName, posterToken

                manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = null;
                // set notification channel
                if (Build.VERSION.SDK_INT >= 26) {
                    channelId = "posts_channel";
                    CharSequence channelName = "Posts";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                    manager.createNotificationChannel(notificationChannel);
                }

                Intent resultIntent = new Intent(this, ProfileActivity.class);
                /*Creates new notification*/
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
                builder.setSmallIcon(R.drawable.ic_baseline_comment_24);
                // New like!
                // likerUid has liked your post!
                builder.setContentTitle("New Comment!");
                builder.setContentText(payload.get("commenterName") + " commented on your post!");

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);

                // notification settings and build
                Notification notification = builder.build();
                notification.defaults = Notification.DEFAULT_ALL;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                manager.notify(LIKES_NOTIF_ID, notification);

                if (remoteMessage.getNotification() != null) {
                    Log.d("testy", "Message Notification Body: " + remoteMessage.getNotification());
                }

            } else if (payload.containsKey("addedFriendUid")) { //if someone added a friend
                // keys: addedFriendUid, adderUid, adderName, addedFriendToken
                manager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                String channelId = null;
                // set notification channel
                if (Build.VERSION.SDK_INT >= 26) {
                    channelId = "friends_channel";
                    CharSequence channelName = "Friends";
                    int importance = NotificationManager.IMPORTANCE_HIGH;
                    NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
                    manager.createNotificationChannel(notificationChannel);
                }

                Intent resultIntent = new Intent(this, ProfileActivity.class);
                resultIntent.putExtra("addedFriendNotification",true);
                resultIntent.putExtra("otherUserUid",payload.get("adderUid"));
                /*Creates new notification*/
                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
                builder.setSmallIcon(R.drawable.ic_baseline_person_add_24);
                // New like!
                // likerUid has liked your post!
                builder.setContentTitle("New friend!!");
                builder.setContentText(payload.get("adderName") + " Added you as a friend!\n Click here to find out who it is!");

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(resultPendingIntent);

                // notification settings and build
                Notification notification = builder.build();
                notification.defaults = Notification.DEFAULT_ALL;
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                manager.notify(LIKES_NOTIF_ID, notification);

                if (remoteMessage.getNotification() != null) {
                    Log.d("testy", "Message Notification Body: " + remoteMessage.getNotification());
                }

            } else { // handle chat services
                Log.d("tests", "Message data payload: " + remoteMessage.getData());

                String[] nameFromTo = remoteMessage.getData().get("name and from and to").split(" ");
                Log.d("tests", nameFromTo.toString());

                String name = nameFromTo[0];
                String from = nameFromTo[1];
                String to = nameFromTo[2];
                String message = remoteMessage.getData().get("message");


                //Check whether the ProfileActivity alive or not
                if (MyApplication.isActivityAlive()) {
                    //Send an intent to the places that receive this action to handle message receive
                    Intent intent = new Intent("message_received");
                    intent.putExtra("name and from", nameFromTo[0].concat(" " + nameFromTo[1]));
                    intent.putExtra("message", message);
                    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
                } else
                updateFireBase(from, message, to);

                //Check if the user is in this chat window so he won't get a message notification
                if (ChatWindowFragment.isActive && ChatWindowFragment.currentWindowChat != null
                        && ChatWindowFragment.currentWindowChat.equals(from))
                    return;


                sendNotification(name, message, from);


                if (remoteMessage.getNotification() != null) {
                    Log.d("tests", "Message Notification Body: " + remoteMessage.getNotification().getBody());
                }
            }

        }
    }


    /*Send message notification to the user*/
    private void sendNotification(String name, String message, String from) {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channel_id = null;
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("channel_id", "Messages", NotificationManager.IMPORTANCE_HIGH);
            assert manager != null;
            manager.createNotificationChannel(channel);
            channel_id = "channel_id";
        }

        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra("open chat window", true);
        intent.putExtra("uid", from);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channel_id);
        String title = getBaseContext().getResources().getString(R.string.label_new_message_from)
                .concat(" " + name);
        builder.setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_baseline_message_24)
                .setContentIntent(pendingIntent);
        assert manager != null;
        manager.notify(NOTIFICATION_ID, builder.build());
    }

    /*Updated firebase chats to the user when the activity is not alive*/
    private void updateFireBase(final String fromUid, final String message, String toUid) {
        FirebaseDatabase.getInstance().getReference("Users").child(toUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    handleMessageReceiving(user, message, fromUid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("tests", error.getMessage());
            }
        });
    }

    /*Handle message receiving*/
    private void handleMessageReceiving(User user, String message, String senderUserKey) {
        //Check if the chat window exists
        for (Chat chat : user.getChats()) {
            if (chat.getOtherSideUserKey().equals(senderUserKey)) {
                //If there are no messages yet
                if (chat.getMessages() == null)
                    chat.setMessages(new ArrayList<Message>());
                chat.getMessages().add(new Message(senderUserKey, message));
                chat.setLastMessageSeen(false);
                user.getChats().remove(chat);
                user.getChats().add(0, chat);
                //Updates the user in firebase
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(user.getUid()).child("chats")
                        .setValue(user.getChats());
                return;
            }
        }

        //The chat window doesn't exists

        //Check if the user has chat list already, if he doesn't - create a new list
        if (user.getChats() == null)
            user.setChats(new ArrayList<Chat>());
        //Create a new chat window
        Chat chat = new Chat(user.getUid(), senderUserKey);
        chat.setMessages(new ArrayList<Message>());
        chat.getMessages().add(new Message(senderUserKey, message));
        chat.setLastMessageSeen(false);
        user.getChats().add(0, chat);

        FirebaseDatabase.getInstance().getReference("Users")
                .child(user.getUid()).child("chats")
                .setValue(user.getChats());
    }

}