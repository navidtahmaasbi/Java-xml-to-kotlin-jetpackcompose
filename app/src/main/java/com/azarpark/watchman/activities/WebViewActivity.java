package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.azarpark.watchman.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    ActivityWebViewBinding binding;
    Activity activity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(LayoutInflater.from(getApplicationContext()));
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("url"))
            binding.webView.loadUrl(getIntent().getExtras().getString("url", "https://www.google.com"));


    }
}