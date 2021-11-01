package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.databinding.GetValueDialogBinding;
import com.azarpark.watchman.databinding.MessageDialogBinding;

public class GetValueDialog extends DialogFragment {

    public static final String TAG = "MessageDialog";
    GetValueDialogBinding binding;
    ConfirmButtonClicks confirmButtonClicks;
    String title;
    String message;
    String confirmButtonText;
    String cancelButtonText;

    public GetValueDialog(String title, String message, String confirmButtonText, ConfirmButtonClicks confirmButtonClicks) {
        this.confirmButtonClicks = confirmButtonClicks;
        this.title = title;
        this.message = message;
        this.confirmButtonText = confirmButtonText;
        this.cancelButtonText = cancelButtonText;
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
        binding = GetValueDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());


        binding.title.setText(title);
        binding.message.setText(message);
        binding.confirm.setText(confirmButtonText);

        binding.confirm.setOnClickListener(view -> {

            if (binding.value.getText().toString().isEmpty())
                Toast.makeText(getContext(), "مقدار را وارد کنید", Toast.LENGTH_SHORT).show();
            else
                confirmButtonClicks.onConfirmClicked(binding.value.getText().toString());

        });

        return builder.create();
    }

    public static interface ConfirmButtonClicks {

        public void onConfirmClicked(String s);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
