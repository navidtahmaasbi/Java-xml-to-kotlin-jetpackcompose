package com.azarpark.watchman.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;


import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
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
            login02(new LoginBody(binding.username.getText().toString(), binding.password.getText().toString()));


    }

//    private void login(String username, String password) {
//
//        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
//        LoadingBar loadingBar = new LoadingBar(LoginActivity.this);
//        loadingBar.show();
//
//        repository.login(new LoginBody(username, password), new Callback<LoginResponse>() {
//            @Override
//            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
//
//                loadingBar.dismiss();
//                if (response.isSuccessful()) {
//
//
//
//                    SharedPreferencesRepository sh_p = new SharedPreferencesRepository(getApplicationContext());
//
//                    SharedPreferencesRepository.setToken(response.body().access_token);
//                    sh_p.saveString(SharedPreferencesRepository.ACCESS_TOKEN, response.body().access_token);
//                    sh_p.saveString(SharedPreferencesRepository.REFRESH_TOKEN, response.body().refresh_token);
//                    sh_p.saveString(SharedPreferencesRepository.USERNAME, username);
//
//                    assistant.loginEvent(username);
//
//                    LoginActivity.this.finish();
//                    startActivity(new Intent(LoginActivity.this, SplashActivity.class));
//
//                } else
//                    APIErrorHandler.onResponseErrorHandler(getSupportFragmentManager(),activity, response, () -> login(username, password));
//
//            }
//
//            @Override
//            public void onFailure(Call<LoginResponse> call, Throwable t) {
//                loadingBar.dismiss();
//                t.printStackTrace();
//
//                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(),t, () -> login(username, password));
//
//
//            }
//        });
//
//    }

    private void login02(LoginBody loginBody) {

        Runnable functionRunnable = () -> login02(loginBody);
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
