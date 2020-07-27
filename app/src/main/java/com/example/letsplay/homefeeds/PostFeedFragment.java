package com.example.letsplay.homefeeds;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.letsplay.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class PostFeedFragment extends BottomSheetDialogFragment {


    public static String TAG = "post_feed_fragment";

    public interface PostListener {
        void onPost(String post);
    }

    private PostListener listener;

    public void setListener(PostListener listener) {
        this.listener = listener;
    }


    public static PostFeedFragment newInstance() {
        Bundle args = new Bundle();
        PostFeedFragment fragment = new PostFeedFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.feed_post_bottom_sheet_layout, container, false);

        ImageButton closeIb = v.findViewById(R.id.feed_post_close_Ib);
        closeIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        @SuppressLint("CutPasteId") final EditText postEt = v.findViewById(R.id.post_feed_et);
        @SuppressLint("CutPasteId") final Button postBtn = v.findViewById(R.id.post_feed_btn);

        postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String post = postEt.getText().toString();
                if (post.trim().isEmpty())
                    return;
                listener.onPost(post);
                postEt.setText("");
                dismiss();
            }
        });


        return v;
    }
}
