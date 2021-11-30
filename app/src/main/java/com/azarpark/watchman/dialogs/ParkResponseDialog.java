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

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ParkResponseDialogBinding;
import com.azarpark.watchman.interfaces.OnSubmitClicked;
import com.azarpark.watchman.utils.Assistant;

public class ParkResponseDialog extends DialogFragment {

    public static final String TAG = "ParkResponseDialog";
    ParkResponseDialogBinding binding;
    Assistant assistant;

    int placeNumber;
    int parkPrice;
    int carBalance;
    OnSubmitClicked onSubmitClicked;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public ParkResponseDialog(int placeNumber, int parkPrice, int carBalance, OnSubmitClicked onSubmitClicked) {
        this.placeNumber = placeNumber;
        this.parkPrice = parkPrice;
        this.carBalance = carBalance;
        this.onSubmitClicked = onSubmitClicked;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = ParkResponseDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        binding.title.setText("پارک جایگاه " + placeNumber + " شروع شد.");

        binding.status.setText(parkPrice > 0 ? "خیر" : "بله");
        binding.status.setTextColor(getResources().getColor(parkPrice > 0 ? R.color.red : R.color.green));

        binding.balance.setText((carBalance < 0 ? -carBalance : carBalance) + " تومان");
        binding.balance.setTextColor(getResources().getColor(carBalance < 0 ? R.color.red : R.color.green));

        binding.balanceTitle.setText(carBalance < 0 ? "بدهی پلاک" : "اعتبار پلاک");

        binding.submit.setOnClickListener(view -> onSubmitClicked.submit());

        return builder.create();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
