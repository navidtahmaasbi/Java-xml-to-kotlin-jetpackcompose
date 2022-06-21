package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.webkit.WebViewClient;

import com.azarpark.watchman.databinding.ActivityWebViewBinding;

public class WebViewActivity extends AppCompatActivity {

    ActivityWebViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWebViewBinding.inflate(LayoutInflater.from(getApplicationContext()));
        setContentView(binding.getRoot());

        if (getIntent().hasExtra("url")){

            binding.webView.getSettings().setJavaScriptEnabled(true);
            binding.webView.setWebViewClient(new WebViewClient());
            binding.webView.getSettings().setDomStorageEnabled(true);
            binding.webView.loadUrl(getIntent().getExtras().getString("url", getIntent().getStringExtra("url")));
        }

    }

    @Override
    public void onBackPressed() {

        if (binding.webView.canGoBack())
            binding.webView.goBack();
        else
            super.onBackPressed();
    }
}