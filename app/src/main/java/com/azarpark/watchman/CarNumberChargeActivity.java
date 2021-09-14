package com.azarpark.watchman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azarpark.watchman.databinding.ActivityCarNumberChargeBinding;

public class CarNumberChargeActivity extends AppCompatActivity {

    ActivityCarNumberChargeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarNumberChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());



    }
}