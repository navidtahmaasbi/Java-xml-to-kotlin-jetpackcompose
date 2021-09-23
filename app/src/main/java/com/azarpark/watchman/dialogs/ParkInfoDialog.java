package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.R;
import com.azarpark.watchman.activities.DebtListActivity;
import com.azarpark.watchman.databinding.ParkInfoDialogBinding;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.retrofit_remote.responses.EstimateParkPriceResponse;

import java.text.NumberFormat;
import java.util.Locale;

public class ParkInfoDialog extends DialogFragment {

    public static final String TAG = "ParkInfoDialog";
    ParkInfoDialogBinding binding;
    private OnGetInfoClicked onGetInfoClicked;
    private Place place;
    EstimateParkPriceResponse parkPriceResponse;

    public ParkInfoDialog(OnGetInfoClicked onGetInfoClicked, Place place, EstimateParkPriceResponse parkPriceResponse) {
        this.onGetInfoClicked = onGetInfoClicked;
        this.parkPriceResponse = parkPriceResponse;
        this.place = place;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = ParkInfoDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        binding.placeNumber.setText(place.number + "");
        binding.startTime.setText(place.start);
        binding.parkPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(parkPriceResponse.getPrice()) + " تومان");
        binding.parkTime.setText(parkPriceResponse.getHours() + " ساعت و" + parkPriceResponse.getMinutes() + " دقیقه");

        binding.carBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(parkPriceResponse.getCar_balance()) + " تومان");

        if (parkPriceResponse.getCar_balance() < 0)
            binding.carBalanceTitle.setText("اعتبار شما");
        else
            binding.carBalanceTitle.setText("بدهی شما");

        if (parkPriceResponse.getCar_balance() >= 0)

            binding.showDebtList.setVisibility(View.GONE);

        else {

            binding.carBalance.setTextColor(getResources().getColor(R.color.red));
            binding.showDebtList.setVisibility(View.VISIBLE);

        }

        if (place.exit_request != null) {

            binding.exitRequestArea.setVisibility(View.VISIBLE);
            binding.paymentArea.setVisibility(View.GONE);

        } else {

            binding.exitRequestArea.setVisibility(View.GONE);
            binding.paymentArea.setVisibility(View.VISIBLE);

        }

        binding.acceptExitRequest.setOnClickListener(view -> onGetInfoClicked.payAsDebt(place));

        binding.declineExitRequest.setOnClickListener(view -> onGetInfoClicked.removeExitRequest(place));


        if (place.tag4 != null && !place.tag4.isEmpty()) {

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldArasArea.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateSimpleTag1.setText(place.tag1);
            binding.plateSimpleTag2.setText(place.tag2);
            binding.plateSimpleTag3.setText(place.tag3);
            binding.plateSimpleTag4.setText(place.tag4);

        } else if (place.tag2 == null || place.tag2.isEmpty()) {

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldArasArea.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateOldArasTag1En.setText(place.tag1);
            binding.plateOldArasTag1Fa.setText(place.tag1);

        } else {

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldArasArea.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);

            binding.plateNewArasTag1En.setText(place.tag1);
            binding.plateNewArasTag1Fa.setText(place.tag1);
            binding.plateNewArasTag2En.setText(place.tag2);
            binding.plateNewArasTag2Fa.setText(place.tag2);

        }

        binding.showDebtList.setOnClickListener(view -> startActivity(new Intent(getActivity(), DebtListActivity.class)));

        binding.pay.setOnClickListener(view -> onGetInfoClicked.pay(parkPriceResponse.getPrice(), place.id));

        binding.payAsDebt.setOnClickListener(view -> onGetInfoClicked.payAsDebt(place));

        return builder.create();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
