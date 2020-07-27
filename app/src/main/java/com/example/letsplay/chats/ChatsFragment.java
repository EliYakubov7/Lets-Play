package com.example.letsplay.chats;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.objects.Chat;
import com.example.letsplay.objects.Message;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatsFragment extends Fragment implements Serializable {

    private Context context;
    private ChatAdapter adapter;
    private List<Chat> chatList = new ArrayList<>();
    private List<User> usersOfChatList = new ArrayList<>();
    private RecyclerView recyclerView;
    private TextView noResultTv;
    private SearchView chatSearchView;
    private ProgressBar progressBar;


    //Receiver for the message receive
    private BroadcastReceiver receiver;

    public ChatsFragment() {
    }

    public static ChatsFragment newInstance() {
        ChatsFragment chatsFragment = new ChatsFragment();
        Bundle bundle = new Bundle();
        chatsFragment.setArguments(bundle);
        return chatsFragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Handle message receiving from a user
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Check if the chat window fragment is active to prevent duplicate message receiving
                if (ChatWindowFragment.isActive)
                    return;

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
        View rootView = inflater.inflate(R.layout.chats_fragment, container, false);

        ProfileActivity.bottomNavigationView.getMenu().findItem(R.id.action_nav_chats).setChecked(true);

        //Define the action bar
        Toolbar toolbar = rootView.findViewById(R.id.chats_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        progressBar = rootView.findViewById(R.id.chat_fragment_progress_bar);
        noResultTv = rootView.findViewById(R.id.chat_fragment_no_chat);

        recyclerView = rootView.findViewById(R.id.chats_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        //Search view
        chatSearchView = rootView.findViewById(R.id.chats_search_view);
        chatSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            //TODO: handle search view and adapter filtering options
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });


        UsersManager.loadUsers();
        UsersManager.setListener(new UsersManager.LoadListener() {
            @Override
            public void onFinishLoad() {
                for (User user : UsersManager.getUsers()) {
                    for (Chat chat : chatList) {
                        if (user.getUid().equals(chat.getOtherSideUserKey()))
                            usersOfChatList.add(user);
                    }
                    if (user.getUid().equals(ProfileActivity.currentSignedInUser.getUid())) {
                        if (user.getChats() != null && user.getChats().size() > 0)
                            chatList = user.getChats();
                        else
                            user.setChats((ArrayList<Chat>) chatList);
                        ProfileActivity.currentSignedInUser.setChats((ArrayList<Chat>) chatList);
                        FirebaseDatabase.getInstance().getReference("Users").child(user.getUid()).child("chats").setValue(chatList);
                    }
                }
                loadAdapterAndRecycler();
            }
        });

        return rootView;
    }

    /*Loads the adapter and the recycler*/
    private void loadAdapterAndRecycler() {
        adapter = new ChatAdapter(chatList, context);
        adapter.setChatListener(new ChatAdapter.ChatListener() {
            @Override
            public void onChatClick(int position, View v) {
                chatSearchView.setQuery("", false);

                FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container,
                        ChatWindowFragment.newInstance(chatList.get(position),
                                UsersManager.getUserByKey(chatList.get(position).getOtherSideUserKey())))
                        .addToBackStack(null).commit();
            }
        });

        if (chatList.size() == 0) {
            noResultTv.setText(R.string.no_chats_yet);
        } else
            noResultTv.setText("");

        progressBar.setVisibility(View.GONE);
        recyclerView.setAdapter(adapter);
    }

    /*Handle message receiving*/
    private void handleMessageReceiving(String message, String senderUserKey) {
        noResultTv.setText("");

        //Check if the chat window exists
        for (Chat chat : chatList) {
            if (chat.getOtherSideUserKey().equals(senderUserKey)) {
                //If there are no messages yet
                if (chat.getMessages() == null)
                    chat.setMessages(new ArrayList<Message>());
                chat.getMessages().add(new Message(senderUserKey, message));
                chat.setLastMessageSeen(false);
                int index = chatList.indexOf(chat);

                if (index > 0) {
                    chatList.remove(index);
                    chatList.add(0, chat);
                }
                adapter.notifyDataSetChanged();

                ProfileActivity.currentSignedInUser.setChats((ArrayList<Chat>) chatList);
                //Updates the user in firebase
                FirebaseDatabase.getInstance().getReference("Users")
                        .child(ProfileActivity.currentSignedInUser.getUid()).child("chats").setValue(chatList);
                return;
            }
        }

        //The chat window doesn't exists

        //Check if the user has chat list already, if he doesn't - create a new list
        if (ProfileActivity.currentSignedInUser.getChats() == null)
            ProfileActivity.currentSignedInUser.setChats((ArrayList<Chat>) chatList);
        //Create a new chat window
        Chat chat = new Chat(ProfileActivity.currentSignedInUser.getUid(), senderUserKey);
        chat.setMessages(new ArrayList<Message>());
        chat.getMessages().add(new Message(senderUserKey, message));
        chat.setLastMessageSeen(false);
        chatList.add(0, chat);
        adapter.notifyDataSetChanged();

        ProfileActivity.currentSignedInUser.setChats((ArrayList<Chat>) chatList);
        //Updates the user in firebase
        FirebaseDatabase.getInstance().getReference("Users")
                .child(ProfileActivity.currentSignedInUser.getUid()).child("chats").setValue(chatList);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Unregister to the action from the service
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
    }
}