package com.azarpark.cunt.activities;

import android.content.Intent;
import android.os.Bundle;


import com.azarpark.cunt.dialogs.ConfirmDialog;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.dialogs.MessageDialog;
import com.azarpark.cunt.web_service.bodies.LoginBody;
import com.azarpark.cunt.web_service.responses.LoginResponse;
import com.azarpark.cunt.utils.Assistant;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;


import com.azarpark.cunt.databinding.ActivityLoginBinding;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    Assistant assistant;
    WebService webService = new WebService();
    ConfirmDialog confirmDialog;

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
            showConfirmation();


    }

    private void showConfirmation() {

        confirmDialog = new ConfirmDialog("پذیرش قوانین", Constants.rules, "تایید", "عدم تایید", new ConfirmDialog.ConfirmButtonClicks() {
            @Override
            public void onConfirmClicked() {
                login(new LoginBody(binding.username.getText().toString(), binding.password.getText().toString()), binding.username.getText().toString());
            }

            @Override
            public void onCancelClicked() {
                confirmDialog.dismiss();
            }
        });
        confirmDialog.show(getSupportFragmentManager(),MessageDialog.TAG);


    }

    private void login(LoginBody loginBody, String mobile) {

        Runnable functionRunnable = () -> login(loginBody, mobile);
        LoadingBar loadingBar = new LoadingBar(LoginActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).login(loginBody).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call, @NonNull Response<LoginResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (response.body() != null)
                    SharedPreferencesRepository.setToken(response.body().access_token);
                SharedPreferencesRepository.setValue(Constants.REFRESH_TOKEN, response.body().refresh_token);
                SharedPreferencesRepository.setValue(Constants.USERNAME, mobile);

                Assistant.loginEvent(loginBody.getUsername());

                LoginActivity.this.finish();
                startActivity(new Intent(LoginActivity.this, SplashActivity.class));
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
