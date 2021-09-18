package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azarpark.watchman.adapters.ChargeItemListAdapter;
import com.azarpark.watchman.databinding.ActivityCarNumberChargeBinding;

import java.text.NumberFormat;
import java.util.ArrayList;

public class CarNumberChargeActivity extends AppCompatActivity {

    ActivityCarNumberChargeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarNumberChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ChargeItemListAdapter adapter = new ChargeItemListAdapter(amount -> {

            binding.amount.setText(Double.toString(amount));

        });
        binding.recyclerView.setAdapter(adapter);

        ArrayList<Double> items = new ArrayList<>();
        items.add(10000d);
        items.add(20000d);
        items.add(30000d);

        adapter.setItems(items);


    }
}