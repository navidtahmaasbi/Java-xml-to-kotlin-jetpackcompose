package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.azarpark.watchman.databinding.ActivitySplashBinding;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        new Handler().postDelayed(() -> {

            SplashActivity.this.finish();
            SharedPreferencesRepository sharedPreferencesRepository = new SharedPreferencesRepository(getApplicationContext());
            if (sharedPreferencesRepository.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty())
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            else
                startActivity(new Intent(SplashActivity.this, MainActivity.class));


        }, 500);

    }
}