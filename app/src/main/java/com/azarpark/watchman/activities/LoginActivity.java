package com.azarpark.watchman.activities;

import android.content.Intent;
import android.os.Bundle;


import com.azarpark.watchman.R;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIClient;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.utils.Assistant;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;


import com.azarpark.watchman.databinding.ActivityLoginBinding;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.HTTP;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;
    ConfirmDialog confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        System.out.println("--------> bass url : " + RetrofitAPIClient.getClient().baseUrl());
        System.out.println("--------> bass url : " + RetrofitAPIClient.BASE_URL);

    }

    public void onLoginClicked(View view) {

        Assistant assistant = new Assistant();

        if (!assistant.isMobile(binding.username.getText().toString()))
            Toast.makeText(getApplicationContext(), "شماره تلفن را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (!assistant.isPassword(binding.password.getText().toString()))
            Toast.makeText(getApplicationContext(), "رمز عبور را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else
            login(binding.username.getText().toString(), binding.password.getText().toString());


    }

    private void login(String username, String password) {

        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(LoginActivity.this);
        loadingBar.show();

        repository.login(new LoginBody(username, password), new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    SharedPreferencesRepository sharedPreferencesRepository = new SharedPreferencesRepository(getApplicationContext());

                    sharedPreferencesRepository.saveString(SharedPreferencesRepository.ACCESS_TOKEN, response.body().access_token);
                    sharedPreferencesRepository.saveString(SharedPreferencesRepository.REFRESH_TOKEN, response.body().refresh_token);

                    LoginActivity.this.finish();
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                } else {

                    Toast.makeText(getApplicationContext(), "error " + response.code(), Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                loadingBar.dismiss();
                t.printStackTrace();


                 confirmDialog = new ConfirmDialog(
                        getResources().getString(R.string.retry_title),
                        getResources().getString(R.string.retry_text),
                        getResources().getString(R.string.retry_confirm_button),
                        getResources().getString(R.string.retry_cancel_button),
                        new ConfirmDialog.ConfirmButtonClicks() {
                            @Override
                            public void onConfirmClicked() {
                                login(username,password);
                            }

                            @Override
                            public void onCancelClicked() {
                                confirmDialog.dismiss();
                            }
                        }
                );


                confirmDialog.show(getSupportFragmentManager(),"tag");

            }
        });

    }

}