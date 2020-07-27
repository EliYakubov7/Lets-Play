package com.example.letsplay.chats;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.date.DateTimeHelper;
import com.example.letsplay.R;
import com.example.letsplay.objects.Chat;
import com.example.letsplay.objects.Message;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWindowFragment extends Fragment implements Serializable {

    private Context context;
    private MessageAdapter adapter;
    private List<Message> messageList = new ArrayList<>();
    private FirebaseFunctions mFunctions = FirebaseFunctions.getInstance();
    private RecyclerView recyclerView;
    private EditText msgEt;
    //Receiver for the message receive
    private BroadcastReceiver receiver;
    private final String API_KEY = "Your_Api_Key";

    //Check if the user is in this window chat so he won't get a message notification
    public static boolean isActive = false;
    public static String currentWindowChat;

    private RequestQueue queue;

    public ChatWindowFragment() {
    }

    public static ChatWindowFragment newInstance(Chat chat, User otherUser) {
        ChatWindowFragment chatWindowFragment = new ChatWindowFragment();
        Bundle bundle = new Bundle();
        chat.setLastMessageSeen(true);
        bundle.putSerializable("chat", chat);
        FirebaseDatabase.getInstance().getReference("Users")
                .child(ProfileActivity.currentSignedInUser.getUid()).setValue(ProfileActivity.currentSignedInUser);
        bundle.putSerializable("otherUser", otherUser);
        chatWindowFragment.setArguments(bundle);
        return chatWindowFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        //Handle message receiving from a user
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Receive the intent from the service with the message
                String message = intent.getStringExtra("message");
                String senderUserKey = intent.getStringExtra("name and from");
                String[] nameAndFrom = senderUserKey.split(" ");
                senderUserKey = nameAndFrom[1];

                handleMessageReceiving(message, senderUserKey);
            }

        };

        //Register to the action from the service
        IntentFilter filter = new IntentFilter("message_received");
        LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chat_window_fragment, container, false);


        ProfileActivity.bottomNavigationView.getMenu().findItem(R.id.action_nav_chats).setChecked(true);

        //Define the action bar
        Toolbar toolbar = rootView.findViewById(R.id.chat_toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);


        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //adjust with keyboard
        // getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);


        Chat chat = (Chat) getArguments().getSerializable("chat");
        final User otherUser = (User) getArguments().getSerializable("otherUser");

        currentWindowChat = chat.getOtherSideUserKey();
        isActive = true;

        //User profile image - clicking on it opens the user profile's fragment
        CircleImageView profileImageIv = rootView.findViewById(R.id.chat_window_profile_iv);

        //Loads the profile image
        profileImageIv.setImageResource(android.R.color.transparent);
        if (otherUser.getProfileImageURL() != null)
            setImageFromStorage(otherUser, profileImageIv);
        else
            profileImageIv.setImageResource(R.drawable.empty_profile_photo);

        //Opens the "otherUser" profile when clicking on his image
        profileImageIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                        .newInstance(otherUser)).addToBackStack(null).commit();
            }
        });

        //Set user first and last name
        TextView nameTv = rootView.findViewById(R.id.chat_window_name_tv);
        nameTv.setText(otherUser.getFirstName().concat(" " + otherUser.getLastName()));

        //Set user status
        TextView statusTv = rootView.findViewById(R.id.chat_window_status_tv);
        statusTv.setText(otherUser.getStatus() == User.Status.ONLINE ?
                getResources().getString(R.string.chat_window_status_online) :
                getResources().getString(R.string.chat_window_status_offlne));

        //Set the message edit text
        msgEt = rootView.findViewById(R.id.chat_window_msg_et);

        //Set send a message button
        Button sendMsgBtn = rootView.findViewById(R.id.chat_window_send_msg_btn);
        sendMsgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = msgEt.getText().toString();
                if (TextUtils.isEmpty(message))
                    return;
                sendMessage(message);
                msgEt.setText("");
            }
        });

        //Set the recycler view with the adapter
        recyclerView = rootView.findViewById(R.id.messages_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        if (chat.getMessages() != null && chat.getMessages().size() > 0)
            messageList = chat.getMessages();
        else
            chat.setMessages((ArrayList<Message>) messageList);

        adapter = new MessageAdapter(messageList, context, otherUser.getFirstName());
        recyclerView.setAdapter(adapter);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // Move recycler when keyboard opens or message is coming
        recyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v,
                                       int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (bottom < oldBottom) {
                    recyclerView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (recyclerView.getAdapter().getItemCount() > 0) {
                                recyclerView.smoothScrollToPosition(
                                        recyclerView.getAdapter().getItemCount() - 1);
                            }
                        }
                    }, 100);
                }
            }
        });

        queue = Volley.newRequestQueue(context);

        return rootView;
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().onBackPressed();
        }

        return super.onOptionsItemSelected(item);
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

    /*Sends a message to the user*/
    private void sendMessage(String message) {
        Chat chat = (Chat) getArguments().getSerializable("chat");
        User sender = ProfileActivity.currentSignedInUser;

        final JSONObject rootObject = new JSONObject();

        //Sends a request through volley
        try {
            rootObject.put("to", "/topics/" + chat.getOtherSideUserKey());

            String url = "https://fcm.googleapis.com/fcm/send";

            JSONObject messageJSONObject = new JSONObject();
            messageJSONObject.put("message", message);
            messageJSONObject.put("name and from and to",
                    ProfileActivity.currentSignedInUser.getFirstName()
                            .concat(" " + ProfileActivity.currentSignedInUser.getUid()
                                    .concat(" " + chat.getOtherSideUserKey())));
            //messageJSONObject.put("from",chat.getMySideUserKey());

            //messageJSONObject.put("sender",sender);

            rootObject.put("data", messageJSONObject);


           StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("tests", error.toString());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    headers.put("Authorization", "key=" + API_KEY);
                    return headers;
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return rootObject.toString().getBytes();
                }
            };
            queue.add(request);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Updates the message list and updates firebase firebase
        messageList.add(new Message(chat.getMySideUserKey(), message));
        adapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        chat.setMessages((ArrayList<Message>) messageList);

        int index = ProfileActivity.currentSignedInUser.getChats().indexOf(chat);
        if (index > 0) {
            ProfileActivity.currentSignedInUser.getChats().remove(index);
            ProfileActivity.currentSignedInUser.getChats().add(0, chat);
        }

        FirebaseDatabase.getInstance().getReference("Users")
                .child(chat.getMySideUserKey()).child("chats").setValue(ProfileActivity.currentSignedInUser.getChats());

    }

    /*Handle message receiving*/
    private void handleMessageReceiving(String message, String senderUserKey) {

        Chat currentChat = (Chat) getArguments().getSerializable("chat");

        //Check if this message didn't came to this particular chat
        if (!currentChat.getOtherSideUserKey().equals(senderUserKey)) {
            //Check if the chat window exists
            for (Chat chat : ProfileActivity.currentSignedInUser.getChats()) {
                if (chat.getOtherSideUserKey().equals(senderUserKey)) {
                    //If there are no messages yet
                    if (chat.getMessages() == null)
                        chat.setMessages(new ArrayList<Message>());
                    chat.getMessages().add(new Message(senderUserKey, message));
                    chat.setLastMessageSeen(false);
                    int index = ProfileActivity.currentSignedInUser.getChats().indexOf(chat);
                    if (index > 0) {
                        ProfileActivity.currentSignedInUser.getChats().remove(index);
                        ProfileActivity.currentSignedInUser.getChats().add(0, chat);
                    }
                    //Updates the user in firebase
                    FirebaseDatabase.getInstance().getReference("Users")
                            .child(ProfileActivity.currentSignedInUser.getUid()).child("chats").setValue(ProfileActivity.currentSignedInUser.getChats());
                    return;
                }
            }

            //The chat window doesn't exists

            //Check if the user has chat list already, if he doesn't - create a new list
            if (ProfileActivity.currentSignedInUser.getChats() == null)
                ProfileActivity.currentSignedInUser.setChats(new ArrayList<Chat>());
            //Create a new chat window
            Chat chat = new Chat(ProfileActivity.currentSignedInUser.getUid(), senderUserKey);
            chat.setMessages(new ArrayList<Message>());
            chat.getMessages().add(new Message(senderUserKey, message));
            chat.setLastMessageSeen(false);
            ProfileActivity.currentSignedInUser.getChats().add(0, chat);

            /*Collections.swap(ProfileActivity.currentSignedInUser.getChats(),ProfileActivity.currentSignedInUser.getChats().size()-1,0);*/
        } else {//If this message came to this chat window
            messageList.add(new Message(senderUserKey, message));
            adapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);

            int index = ProfileActivity.currentSignedInUser.getChats().indexOf(currentChat);
            if (index > 0) {
                ProfileActivity.currentSignedInUser.getChats().remove(index);
                ProfileActivity.currentSignedInUser.getChats().add(0, currentChat);
            }

        }

        //Updates the user in firebase
        FirebaseDatabase.getInstance().getReference("Users")
                .child(ProfileActivity.currentSignedInUser.getUid()).child("chats").setValue(ProfileActivity.currentSignedInUser.getChats());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isActive = false;
        currentWindowChat = null;
        //Unregister to the action from the service
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }

//    private Task<String> getServerKey() {
//        // Create the arguments to the callable function.
//        return mFunctions
//                .getHttpsCallable("serverKey")
//                .call()
//                .continueWith(new Continuation<HttpsCallableResult, String>() {
//                    @Override
//                    public String then(@NonNull Task<HttpsCallableResult> task) throws Exception {
//                        // This continuation runs on either success or failure, but if the task
//                        // has failed then getResult() will throw an Exception which will be
//                        // propagated down.
//                        Map<String, String> payload = (Map<String, String>) task.getResult().getData();
//                        String key = payload.get("serverKey");
//                        Log.d("testy", key);
//                        serverKey = key;
//                        Log.d("key", serverKey);
//
//                        return key;
//                    }
//                });
//    }
}



