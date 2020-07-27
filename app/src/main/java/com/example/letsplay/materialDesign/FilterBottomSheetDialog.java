package com.example.letsplay.materialDesign;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.example.letsplay.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class FilterBottomSheetDialog extends BottomSheetDialogFragment {

    public static FilterBottomSheetDialog newInstance() {
        Bundle args = new Bundle();
        FilterBottomSheetDialog fragment = new FilterBottomSheetDialog();
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
        View v = inflater.inflate(R.layout.filter_bottom_sheet_layout, container, false);
        return v;
    }


}