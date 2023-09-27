package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.adapters.ChargeItemListAdapter;
import com.azarpark.watchman.databinding.PlateChargeDialogBinding;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.web_service.WebService;

import java.util.ArrayList;

public class PlateChargeDialog extends DialogFragment {

    public static final String TAG = "PlateChargeDialog";
    PlateChargeDialogBinding binding;
    ChargeItemListAdapter adapter;
    OnPayClicked onPayClicked;
    Place place;
    int selectedItem = 0;
    boolean hasMobile = false;


    public PlateChargeDialog(OnPayClicked onPayClicked, Place place, boolean hasMobile) {
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
        binding = PlateChargeDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        Assistant assistant = new Assistant();

        binding.amount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                adapter.clearSelectedItem();

                String stringValue = charSequence.toString();
                if (stringValue.length() > 9) {
                    binding.amount.setText(stringValue.substring(0, 9));
                } else if (assistant.isNumber(stringValue)) {
                    binding.amount.removeTextChangedListener(this);
                    stringValue = stringValue.replace(",", "");
                    int integerValue = Integer.parseInt(stringValue);
                    binding.amount.setText(assistant.formatAmount(integerValue));
                    binding.amount.setSelection(binding.amount.getText().length());
                    selectedItem = integerValue;
                    binding.amount.addTextChangedListener(this);
                }

                binding.amountInWords.setText(Assistant.translateToTomanInWords(binding.amount.getText().toString()));

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        adapter = new ChargeItemListAdapter(chargeOrDiscount -> {

            selectedItem = chargeOrDiscount;

//            binding.amount.setText(NumberFormat.getNumberInstance(Locale.US).format(selectedItem));

        }, getContext());
        binding.recyclerView.setAdapter(adapter);

        ArrayList<Integer> items = new ArrayList<>();

        items.add(1000);
        items.add(10000);
        items.add(20000);
        items.add(30000);
        items.add(50000);
        items.add(70000);
        items.add(100000);

        adapter.setItems(items);
        binding.submit.setOnClickListener(view -> {


                if (selectedItem == 0)
                    Toast.makeText(getContext(), "مبلغ شارژ را انتخاب کنید", Toast.LENGTH_SHORT).show();
                else if (!isNumber(Integer.toString(selectedItem)))
                    Toast.makeText(getContext(), "مبلغ شارژ را درست وارد کنید", Toast.LENGTH_SHORT).show();
                else if (selectedItem < Constants.MIN_PRICE_FOR_PAYMENT)
                    Toast.makeText(getContext(), "مبلغ شارژ نباید کمتر از " + assistant.formatAmount(Constants.MIN_PRICE_FOR_PAYMENT) + " تومان باشد", Toast.LENGTH_SHORT).show();
                else if (selectedItem > Constants.MAX_PRICE_FOR_PAYMENT)
                    Toast.makeText(getContext(), "مبلغ شارژ نباید بیشتر از " + assistant.formatAmount(Constants.MAX_PRICE_FOR_PAYMENT) + " تومان باشد", Toast.LENGTH_SHORT).show();
                else if (selectedItem % 100 != 0)
                    Toast.makeText(getContext(), "مبلغ رند انتخاب کنید", Toast.LENGTH_SHORT).show();
                else {
                    binding.submit.startAnimation();
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

        void pay(int amount);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
