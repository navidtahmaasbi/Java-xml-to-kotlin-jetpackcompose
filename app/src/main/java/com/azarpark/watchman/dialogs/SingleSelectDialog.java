package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.adapters.SingleSelectListAdapter;
import com.azarpark.watchman.databinding.MessageDialogBinding;
import com.azarpark.watchman.databinding.SingleSelectDialogBinding;
import com.azarpark.watchman.models.City;

import java.util.ArrayList;

public class SingleSelectDialog extends DialogFragment {

    public static final String TAG = "SingleSelectDialog";
    SingleSelectDialogBinding binding;
    OnItemSelected onItemSelected;
    String title;
    String description;
    SingleSelectListAdapter adapter;
    ArrayList<String> items;

    public SingleSelectDialog(String title, String description, ArrayList<String> items, OnItemSelected onItemSelected) {
        this.onItemSelected = onItemSelected;
        this.title = title;
        this.description = description;
        this.items = items;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = SingleSelectDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        adapter = new SingleSelectListAdapter(onItemSelected);

        this.setCancelable(false);

        binding.title.setText(title);
        binding.description.setText(description);

        binding.recyclerView.setAdapter(adapter);

        adapter.setItems(items);

        return builder.create();
    }

    public static interface OnItemSelected {

        public void select(int position);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
