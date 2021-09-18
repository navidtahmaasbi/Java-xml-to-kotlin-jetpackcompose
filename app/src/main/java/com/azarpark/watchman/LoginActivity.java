package com.azarpark.watchman;

import android.content.Intent;
import android.os.Bundle;

import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.LoginBody;
import com.azarpark.watchman.retrofit_remote.interfaces.Login;
import com.azarpark.watchman.retrofit_remote.responses.LoginResponse;
import com.azarpark.watchman.utils.Assistant;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.azarpark.watchman.databinding.ActivityLoginBinding;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.HTTP;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


    }

    public void onLoginClicked(View view) {

        Assistant assistant = new Assistant();

        if (assistant.isMobile(binding.username.getText().toString()))
            Toast.makeText(getApplicationContext(), "شماره تلفن را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (assistant.isPassword(binding.password.getText().toString()))
            Toast.makeText(getApplicationContext(), "رمز عبور را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else
            login(binding.username.getText().toString(), binding.password.getText().toString());


    }

    private void login(String username, String password) {

        RetrofitAPIRepository repository = new RetrofitAPIRepository();

        repository.login(new LoginBody(username, password), new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {

                if (response.code() == HttpURLConnection.HTTP_OK) {

                    SharedPreferencesRepository sharedPreferencesRepository = new SharedPreferencesRepository(getApplicationContext());

                    sharedPreferencesRepository.saveString(SharedPreferencesRepository.ACCESS_TOKEN, response.body().access_token);
                    sharedPreferencesRepository.saveString(SharedPreferencesRepository.REFRESH_TOKEN, response.body().refresh_token);

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

            }
        });

    }


}