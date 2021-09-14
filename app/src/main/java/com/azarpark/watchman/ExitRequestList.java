package com.azarpark.watchman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azarpark.watchman.databinding.ActivityExitRequestListBinding;

public class ExitRequestList extends AppCompatActivity {

    ActivityExitRequestListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExitRequestListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}