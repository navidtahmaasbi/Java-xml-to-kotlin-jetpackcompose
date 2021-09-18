package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.databinding.ParkDialog02Binding;
import com.azarpark.watchman.databinding.ParkDialogBinding;
import com.azarpark.watchman.interfaces.OnSubmitClicked;
import com.azarpark.watchman.models.ParkModel;

public class ParkDialog02 extends DialogFragment {

    public static final String TAG = "ParkDialog02Tag";
    ParkDialog02Binding binding;
    private OnSubmitClicked onSubmitClicked;
    private ParkModel parkModel;

    public ParkDialog02(OnSubmitClicked onSubmitClicked, ParkModel parkModel) {
        this.onSubmitClicked = onSubmitClicked;
        this.parkModel = parkModel;
    }

    public ParkDialog02(int contentLayoutId, OnSubmitClicked onSubmitClicked, ParkModel parkModel) {
        super(contentLayoutId);
        this.onSubmitClicked = onSubmitClicked;
        this.parkModel = parkModel;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        binding = ParkDialog02Binding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        binding.submit.setOnClickListener(view -> onSubmitClicked.onClick(parkModel));

        return builder.create();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
