package com.example.letsplay.homefeeds;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.letsplay.R;
import com.example.letsplay.objects.LastHomeFeedsPreferencesSaved;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.io.Serializable;

public class FeedFilterFragment extends BottomSheetDialogFragment implements Serializable {

    private TextView kmTv;
    private Button searchBtn;
    private OnSeekAndCheckListener listener;
    public static final String TAG= "feed_filter_fragment";

    public FeedFilterFragment(){}

    public static FeedFilterFragment newInstance(HomeFeedsFragment homeFeedsFragment,
                                                 LastHomeFeedsPreferencesSaved preferencesSaved){
        Bundle bundle = new Bundle();
        bundle.putSerializable("listener",homeFeedsFragment);
        bundle.putSerializable("preferences",preferencesSaved);
        FeedFilterFragment feedFilterFragment= new FeedFilterFragment();
        feedFilterFragment.setArguments(bundle);
        return feedFilterFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (OnSeekAndCheckListener) getArguments().getSerializable("listener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }

    public interface OnSeekAndCheckListener {
        void onItemChecked(int checkedId);
        void onSeeking(int distance);
        void onDismiss();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.feed_bottom_sheet_layout, container, false);

        LastHomeFeedsPreferencesSaved preferencesSaved = (LastHomeFeedsPreferencesSaved)getArguments().getSerializable("preferences");

        kmTv=v.findViewById(R.id.distance_km_tv);
        kmTv.setText(String.valueOf(preferencesSaved.getRadius()).concat(" "+getContext().getResources().getString(R.string.label_km)));

        ImageButton closeIb = v.findViewById(R.id.feed_filter_close_Ib);
        closeIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        searchBtn = v.findViewById(R.id.home_bottom_filter_btn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDismiss();
                dismiss();
            }
        });

        RadioGroup filterRg = v.findViewById(R.id.feed_filter_rg);
        filterRg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
               listener.onItemChecked(checkedId);
            }
        });

        RadioButton friendsRb =v.findViewById(R.id.friends_rb);
        friendsRb.setChecked(preferencesSaved.isByFriends());

        RadioButton mostRecentRb =v.findViewById(R.id.most_recent_rb);
        mostRecentRb.setChecked(preferencesSaved.isByMostRecent());

        RadioButton myPostsRb =v.findViewById(R.id.my_posts_rb);
        myPostsRb.setChecked(preferencesSaved.isByMyPosts());

        RadioButton distanceRb =v.findViewById(R.id.distance_rb);
        distanceRb.setChecked(preferencesSaved.isByDistance());


        IndicatorSeekBar distanceSeekBar = v.findViewById(R.id.feed_seekbar);
        distanceSeekBar.setProgress(preferencesSaved.getRadius());
         distanceSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
             @Override
             public void onSeeking(SeekParams seekParams) {
                kmTv.setText(String.valueOf(seekParams.progress).concat(" "+getContext().getResources().getString(R.string.label_km)));
                listener.onSeeking(seekParams.progress);
             }

             @Override
             public void onStartTrackingTouch(IndicatorSeekBar seekBar) {

             }

             @Override
             public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

             }
         });

        return v;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onDismiss();
    }
}
