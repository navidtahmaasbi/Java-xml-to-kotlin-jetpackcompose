package com.azarpark.watchman.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;


import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.web_service.bodies.LoginBody;
import com.azarpark.watchman.web_service.responses.LoginResponse;
import com.azarpark.watchman.utils.Assistant;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;


import com.azarpark.watchman.databinding.ActivityLoginBinding;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    ConfirmDialog confirmDialog;
    Activity activity = this;
    Assistant assistant;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assistant = new Assistant();

    }

    public void onLoginClicked(View view) {

        Assistant assistant = new Assistant();

        if (!assistant.isMobile(binding.username.getText().toString()))
            Toast.makeText(getApplicationContext(), "شماره تلفن را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (!assistant.isPassword(binding.password.getText().toString()))
            Toast.makeText(getApplicationContext(), "رمز عبور را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else
            login(new LoginBody(binding.username.getText().toString(), binding.password.getText().toString()));


    }

    private void login(LoginBody loginBody) {

        Runnable functionRunnable = () -> login(loginBody);
        LoadingBar.start(LoginActivity.this);

        WebService.getClient(getApplicationContext()).login( loginBody).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                LoadingBar.stop();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                SharedPreferencesRepository.setToken(response.body().access_token);
                SharedPreferencesRepository.setValue(Constants.REFRESH_TOKEN,response.body().refresh_token);
                SharedPreferencesRepository.setValue(Constants.USERNAME,response.body().refresh_token);

                Assistant.loginEvent(loginBody.getUsername());

                LoginActivity.this.finish();
                startActivity(new Intent(LoginActivity.this, SplashActivity.class));
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                LoadingBar.stop();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
