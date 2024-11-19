package com.azarpark.cunt.dialogs;


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

import com.azarpark.cunt.databinding.ConfirmDialogBinding;

public class ConfirmDialog extends DialogFragment {

    public static final String TAG = "ConfirmDialog";
    ConfirmDialogBinding binding;
    ConfirmButtonClicks confirmButtonClicks;
    String title;
    String question;
    String confirmButtonText;
    String cancelButtonText;

    public ConfirmDialog(String title, String question, String confirmButtonText, String cancelButtonText, ConfirmButtonClicks confirmButtonClicks) {
        this.confirmButtonClicks = confirmButtonClicks;
        this.title = title;
        this.question = question;
        this.confirmButtonText = confirmButtonText;
        this.cancelButtonText = cancelButtonText;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = ConfirmDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        binding.title.setText(title);
        binding.question.setText(question);
        binding.confirm.setText(confirmButtonText);
        binding.cancel.setText(cancelButtonText);

        binding.confirm.setOnClickListener(view -> confirmButtonClicks.onConfirmClicked());

        binding.cancel.setOnClickListener(view -> confirmButtonClicks.onCancelClicked());

        return builder.create();
    }

    public  interface ConfirmButtonClicks {
        void onConfirmClicked();
        void onCancelClicked();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
