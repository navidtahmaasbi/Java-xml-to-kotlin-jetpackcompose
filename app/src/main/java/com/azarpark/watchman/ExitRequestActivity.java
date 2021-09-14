package com.azarpark.watchman;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azarpark.watchman.databinding.ActivityExitRequestBinding;

public class ExitRequestActivity extends AppCompatActivity {

    ActivityExitRequestBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExitRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}