package com.azarpark.cunt.dialogs;


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

import com.azarpark.cunt.adapters.DiscountListAdapter;
import com.azarpark.cunt.databinding.PlateDiscountDialogBinding;
import com.azarpark.cunt.models.Place;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;
import com.azarpark.cunt.web_service.responses.Discount;
import com.azarpark.cunt.web_service.responses.DiscountsResponse;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlateDiscountDialog extends DialogFragment {

    public static final String TAG = "PlateDiscountDialog";
    PlateDiscountDialogBinding binding;
    DiscountListAdapter adapter;
    OnPayClicked onPayClicked;
    Place place;
    Discount selectedItem = null;
    boolean hasMobile = false;

    WebService webService = new WebService();

    public PlateDiscountDialog(OnPayClicked onPayClicked, Place place, boolean hasMobile) {
        this.onPayClicked = onPayClicked;
        this.place = place;
        this.hasMobile = hasMobile;
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
        binding = PlateDiscountDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        getDiscounts();

        adapter = new DiscountListAdapter(discount -> {

            selectedItem = discount;

        }, getContext());
        binding.recyclerView.setAdapter(adapter);

        binding.submit.setOnClickListener(view -> {


            if(selectedItem == null){

                Toast.makeText(getContext(), "یک مورد را انتخاب کنید", Toast.LENGTH_SHORT).show();

            } else {
                onPayClicked.pay(selectedItem);
            }


        });

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

        return builder.create();
    }

    private boolean isNumber(String amount) {

        amount = amount.replace(",", "");

        try {

            int a = Integer.parseInt(amount);
        } catch (Exception e) {

            return false;
        }

        return true;
    }

    public interface OnPayClicked {

        void pay(Discount discount);

    }

    private void getDiscounts() {

        Runnable functionRunnable = this::getDiscounts;

        binding.progressBar.setVisibility(View.VISIBLE);

        webService.getClient(requireContext()).getDiscounts(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<DiscountsResponse>() {
            @Override
            public void onResponse(@NonNull Call<DiscountsResponse> call, @NonNull Response<DiscountsResponse> response) {

                binding.progressBar.setVisibility(View.GONE);

                if (NewErrorHandler.apiResponseHasError(response, requireContext()))
                    return;

                if (response.body() != null) {
                    ArrayList<Discount> items = response.body().items;
                    adapter.setItems(items);
                }

            }

            @Override
            public void onFailure(@NonNull Call<DiscountsResponse> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                NewErrorHandler.apiFailureErrorHandler(call, t, getParentFragmentManager(), functionRunnable);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
