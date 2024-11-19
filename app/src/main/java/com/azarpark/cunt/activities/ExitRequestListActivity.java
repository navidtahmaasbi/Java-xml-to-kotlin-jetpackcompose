package com.azarpark.cunt.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;

import com.azarpark.cunt.databinding.ActivityExitRequestListBinding;

public class ExitRequestListActivity extends AppCompatActivity {

    ActivityExitRequestListBinding binding;
    Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExitRequestListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }
}