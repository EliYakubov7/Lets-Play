package com.example.letsplay.search;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.letsplay.R;
import com.example.letsplay.objects.LastSearchPreferencesSaved;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import java.io.Serializable;

public class SearchFilterFragment extends BottomSheetDialogFragment implements CompoundButton.OnCheckedChangeListener,
        AdapterView.OnItemSelectedListener, Serializable {

    public static final String TAG = "search_filter_fragment";

    private TextView kmTv;
    private FilterSearchListener listener;

    public interface FilterSearchListener {
        void onSeek(int distance);

        void onSearchClick(String name);

        void onItemSelected(AdapterView<?> parent, View view, int position, long id);

        void onItemChecked(CompoundButton buttonView, boolean isChecked);

        void onDismiss();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (SearchFilterFragment.FilterSearchListener) getArguments().getSerializable("listener");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public SearchFilterFragment() {
    }

    public static SearchFilterFragment newInstance(SearchFragment searchFragment, LastSearchPreferencesSaved preferencesSaved) {
        SearchFilterFragment searchFilterFragment = new SearchFilterFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("listener", searchFragment);
        bundle.putSerializable("preferences", preferencesSaved);
        searchFilterFragment.setArguments(bundle);
        return searchFilterFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.filter_bottom_sheet_layout, container, false);

        //adjust with keyboard
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        kmTv = v.findViewById(R.id.search_filter_distance_km_tv);
        LastSearchPreferencesSaved preferencesSaved = (LastSearchPreferencesSaved) getArguments().getSerializable("preferences");

        kmTv = v.findViewById(R.id.search_filter_distance_km_tv);
        kmTv.setText(preferencesSaved.getRadius() + " Km");

        ImageButton closeIb = v.findViewById(R.id.search_filter_close_Ib);
        closeIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel(getDialog());
                dismiss();
            }
        });

        final EditText nameEt = v.findViewById(R.id.search_filter_et);
        ImageButton searchImageBtn = v.findViewById(R.id.search_filter_search_btn);
        searchImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameEt.getText().toString();
                if (name.trim().isEmpty()) {
                    onCancel(getDialog());
                    dismiss();
                    return;
                }
                listener.onSearchClick(name);
                dismiss();
            }
        });

        CheckBox distanceCb = v.findViewById(R.id.search_filter_distance_cb);
        distanceCb.setOnCheckedChangeListener(this);
        distanceCb.setChecked(preferencesSaved.isByDistance());

        CheckBox instrumentCb = v.findViewById(R.id.search_filter_instrument_cb);
        instrumentCb.setOnCheckedChangeListener(this);
        instrumentCb.setChecked(preferencesSaved.isByInstrument());

        CheckBox genresCb = v.findViewById(R.id.search_filter_genres_cb);
        genresCb.setOnCheckedChangeListener(this);
        genresCb.setChecked(preferencesSaved.isByGenre());

        Spinner genresSpinner = v.findViewById(R.id.search_filter_genres_spinner);
        genresSpinner.setOnItemSelectedListener(this);
        genresSpinner.setSelection(preferencesSaved.getGenrePosition());

        Spinner instrumentsSpinner = v.findViewById(R.id.search_filter_instrument_spinner);
        instrumentsSpinner.setOnItemSelectedListener(this);
        instrumentsSpinner.setSelection(preferencesSaved.getInstrumentPosition());

        /*Seek bar handling*/
        IndicatorSeekBar distanceSeekBar = v.findViewById(R.id.search_filter_seekbar);
        distanceSeekBar.setProgress(preferencesSaved.getRadius());
        distanceSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {
                kmTv.setText(String.valueOf(seekParams.progress).concat(" " + getContext().getResources().getString(R.string.label_km)));
                listener.onSeek(seekParams.progress);
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

    /*Checkboxes handling*/
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        listener.onItemChecked(buttonView, isChecked);
    }

    /*Spinners Handling*/
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        listener.onItemSelected(parent, view, position, id);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        listener.onDismiss();
    }

}
