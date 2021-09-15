package com.azarpark.watchman;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.databinding.CheckoutDialogBinding;
import com.azarpark.watchman.databinding.ParkDialog02Binding;

public class CheckoutDialog extends DialogFragment {

    public static final String TAG = "CheckoutDialog";
    CheckoutDialogBinding binding;
    OnCheckoutButtonsClicked checkoutButtonsClicked;
    private ParkModel parkModel;

    public CheckoutDialog(OnCheckoutButtonsClicked checkoutButtonsClicked, ParkModel parkModel) {
        this.checkoutButtonsClicked = checkoutButtonsClicked;
        this.parkModel = parkModel;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        return super.onCreateDialog(savedInstanceState);
        binding = CheckoutDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        binding.showDebtList.setOnClickListener(view -> checkoutButtonsClicked.onShowDebtListClicked(parkModel));

        return builder.create();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
