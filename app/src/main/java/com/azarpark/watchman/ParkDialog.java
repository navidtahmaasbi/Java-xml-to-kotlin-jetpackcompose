package com.azarpark.watchman;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.databinding.ParkDialogBinding;

public class ParkDialog extends DialogFragment {

    public static final String TAG = "ParkDialogTag";
    ParkDialogBinding binding;
    private OnSubmitClicked onSubmitClicked;
    private ParkModel parkModel;

    public ParkDialog(OnSubmitClicked onSubmitClicked, ParkModel parkModel) {
        this.onSubmitClicked = onSubmitClicked;
        this.parkModel = parkModel;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        binding = ParkDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());
        return builder.create();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
