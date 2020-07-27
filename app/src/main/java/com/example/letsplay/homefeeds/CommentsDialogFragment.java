package com.example.letsplay.homefeeds;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.letsplay.R;
import com.example.letsplay.activities.ProfileActivity;
import com.example.letsplay.objects.Comment;
import com.example.letsplay.objects.Feed;
import com.example.letsplay.objects.User;
import com.example.letsplay.objects.UsersManager;
import com.example.letsplay.profile.OtherUserProfileFragment;
import com.example.letsplay.profile.UserProfileFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommentsDialogFragment extends BottomSheetDialogFragment implements Serializable {

    public static final String TAG = "comments_fragment";

    private Context context;
    private List<Comment> commentList = new ArrayList<>();
    private List<User> commentUsers = new ArrayList<>();
    private CommentAdapter adapter;
    private RecyclerView recyclerView;
    private String key;;

    /*Listener to change the number of comments in live*/
    public interface CommentListener {
        void onComment(int position);
    }

    private CommentListener listener;

    public void setListener(CommentListener listener) {
        this.listener = listener;
    }

    public CommentsDialogFragment() {
    }

    public static CommentsDialogFragment newInstance(Feed feed, String key, int position) {
        CommentsDialogFragment commentsDialogFragment = new CommentsDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("feed", feed);
        bundle.putString("key", key);
        bundle.putInt("position", position);
        commentsDialogFragment.setArguments(bundle);
        return commentsDialogFragment;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.comments_bottom_sheet_layout, container, false);

        recyclerView = rootView.findViewById(R.id.comments_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ImageButton closeBtn = rootView.findViewById(R.id.comments_close_Ib);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        final EditText commentEt = rootView.findViewById(R.id.comment_et);
        Button sendCommentBtn = rootView.findViewById(R.id.comment_send_comment_btn);
        sendCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment = commentEt.getText().toString();

                if (comment.trim().isEmpty())
                    return;


               final Feed feed = (Feed) getArguments().getSerializable("feed");

               /*Ensure that there are no overwrite of data*/
                FirebaseDatabase.getInstance().getReference("Feeds").orderByKey()
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            for(DataSnapshot dataSnapshot:snapshot.getChildren()){
                                Feed feedToCompare=dataSnapshot.getValue(Feed.class);
                                if(feed.getAuthor().equals(feedToCompare.getAuthor())&&
                                        feed.getBody().equals(feedToCompare.getBody())&&
                                        feed.getTime().equals(feedToCompare.getTime())&&
                                        feed.getDate().equals(feedToCompare.getDate())){
                                    commentList.clear();
                                    commentList.addAll(feedToCompare.getComments());
                                    feed.setNoOfComments(feedToCompare.getNoOfComments());
                                    feed.setLikes(feedToCompare.getLikes());
                                    key=dataSnapshot.getKey();
                                    comment(feed,comment);
                                    commentEt.setText("");
                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });

        Feed feed = (Feed) getArguments().getSerializable("feed");
        if (feed != null && feed.getComments() != null)
            commentList = feed.getComments();

        /*Loads the users that commented*/
        /*Loads all users from firebase, and takes the users that commented in this feed */
        UsersManager.loadUsers();
        UsersManager.setListener(new UsersManager.LoadListener() {
            @Override
            public void onFinishLoad() {
                for (User user : UsersManager.getUsers()) {
                    for (Comment comment : commentList)
                        if (comment.getAuthor().equals(user.getUid()))
                            commentUsers.add(user);
                }
                loadList();
            }
        });

        return rootView;
    }

    /*Comment to the post realtime*/
    private void comment(Feed feed,String comment) {

        commentList.add(new Comment(ProfileActivity.currentSignedInUser.getUid(), comment));

        /*Update this feed in firebase after comment*/
        feed.setComments((ArrayList<Comment>) commentList);
        feed.setNoOfComments(feed.getNoOfComments() + 1);

        FirebaseDatabase.getInstance().getReference("Feeds").child(key).setValue(feed);

        adapter.notifyItemInserted(commentList.size() - 1);

        recyclerView.scrollToPosition(commentList.size()-1);

        /*Tell the listener to update the number of comments*/
        int position=getArguments().getInt("position");
        listener.onComment(position);

    }

    /*Loads the adapter and set it with the recycler*/
    private void loadList() {
        adapter = new CommentAdapter(commentList, context);
        adapter.setListener(new CommentAdapter.CommentListener() {
            @Override
            public void onUserClick(int position, View v) {
                loadProfileFragment(position);
                dismiss();
            }
        });
        recyclerView.setAdapter(adapter);

        if(commentList.size()>0)
            recyclerView.scrollToPosition(commentList.size()-1);
    }

    /*Loads user profile fragment*/
    private void loadProfileFragment(int position) {
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        //Open my user profile
        if (commentList.get(position).getAuthor().equals(ProfileActivity.currentSignedInUser.getUid())) {
            ProfileActivity.bottomNavigationView.setSelectedItemId(R.id.action_nav_profile);
            transaction.replace(R.id.fragment_container, UserProfileFragment.newInstance()).addToBackStack(null).commit();
        }
        //Open other user profile
        else {
            ProfileActivity.bottomNavigationView.setSelectedItemId(R.id.uncheckedItem);
            transaction.replace(R.id.fragment_container, OtherUserProfileFragment
                    .newInstance(UsersManager.getUserByKey(commentList.get(position).getAuthor()))).addToBackStack(null).commit();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog bottomSheetDialog=(BottomSheetDialog)super.onCreateDialog(savedInstanceState);
        bottomSheetDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dia) {
                BottomSheetDialog dialog = (BottomSheetDialog) dia;
                FrameLayout bottomSheet =  dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
                BottomSheetBehavior.from(bottomSheet).setState(BottomSheetBehavior.STATE_EXPANDED);
                BottomSheetBehavior.from(bottomSheet).setSkipCollapsed(true);
                BottomSheetBehavior.from(bottomSheet).setHideable(true);
            }
        });
        return bottomSheetDialog;
    }
}
