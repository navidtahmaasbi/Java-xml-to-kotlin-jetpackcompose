package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.databinding.ConfirmDialogBinding;
import com.azarpark.watchman.databinding.TimeSelectDialogBinding;
import com.azarpark.watchman.models.MyTime;

public class TimeSelectDialog extends DialogFragment {

    public static final String TAG = "ConfirmDialog";
    TimeSelectDialogBinding binding;
    ConfirmButtonClicks confirmButtonClicks;
    String title;
    int startHour, endHour;
    String confirmButtonText;
    String cancelButtonText;
    int selectedHour, selectedMinute = 0;
    MyTime initialTime;

    public TimeSelectDialog(String title, String confirmButtonText, String cancelButtonText, int startHour, int endHour, MyTime initialTime, ConfirmButtonClicks confirmButtonClicks) {
        this.confirmButtonClicks = confirmButtonClicks;
        this.title = title;
        this.startHour = startHour;
        this.endHour = endHour;
        this.initialTime = initialTime;
        this.confirmButtonText = confirmButtonText;
        this.cancelButtonText = cancelButtonText;
        selectedHour = startHour;
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
        binding = TimeSelectDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        binding.title.setText(title);
        binding.confirm.setText(confirmButtonText);
        binding.cancel.setText(cancelButtonText);

        binding.hour.setMinValue(startHour);
        binding.hour.setMaxValue(endHour);
        if (initialTime.hour >= startHour && initialTime.hour <= endHour)
            binding.hour.setValue(initialTime.hour);

        binding.minute.setMinValue(0);
        binding.minute.setMaxValue(59);
        if (initialTime.minute >= 0 && initialTime.minute <= 59)
            binding.minute.setValue(initialTime.minute);

        binding.confirm.setOnClickListener(view -> confirmButtonClicks.onConfirmClicked(binding.hour.getValue(), binding.minute.getValue()));
        binding.cancel.setOnClickListener(view -> dismiss());

        return builder.create();
    }

    public interface ConfirmButtonClicks {
        void onConfirmClicked(int hour, int minute);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
