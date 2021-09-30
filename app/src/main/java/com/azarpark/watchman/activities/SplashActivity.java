package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ActivitySplashBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.SingleSelectDialog;
import com.azarpark.watchman.models.City;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIClient;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    SingleSelectDialog citySelectDialog;
    ArrayList<City> cities;
    SharedPreferencesRepository sh_p;
    ConfirmDialog confirmDialog;
    Activity activity = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        sh_p = new SharedPreferencesRepository(getApplicationContext());

        if (sh_p.getString(SharedPreferencesRepository.SUB_DOMAIN).isEmpty())
            getCities();
        else
            new Handler().postDelayed(() -> {

                RetrofitAPIClient.setBaseUrl("https://" + sh_p.getString(SharedPreferencesRepository.SUB_DOMAIN) + ".backend.iranademo.ir");

                SplashActivity.this.finish();
                if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty()){
                    Log.e("startActivity" , "11111");
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                }
                else{
                    Log.e("startActivity" , "22222");
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                }


            }, 500);
//        getSplash();

    }

    private void getCities() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();

        repository.getCities("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<GetCitiesResponse>() {
            @Override
            public void onResponse(Call<GetCitiesResponse> call, Response<GetCitiesResponse> response) {

                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (response.isSuccessful()) {

                    openCitiesDialog(response.body().items);

                } else APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(),activity, response, () -> getCities());
            }

            @Override
            public void onFailure(Call<GetCitiesResponse> call, Throwable t) {
                binding.loadingBar.setVisibility(View.INVISIBLE);
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(),t, () -> getCities());
            }
        });

    }

    private void getSplash() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();

        repository.getSplashData("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<SplashResponse>() {
            @Override
            public void onResponse(Call<SplashResponse> call, Response<SplashResponse> response) {

                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (response.isSuccessful()) {

                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().qr_url);
                    sh_r.saveString(SharedPreferencesRepository.qr_url, Integer.toString(response.body().refresh_time));
                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().telephone);
                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().pricing);
                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().sms_number);
                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().rules_url);
                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().about_us_url);
                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().guide_url);

                    Log.e("startActivity" , "33333");
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    SplashActivity.this.finish();

                } else APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(),activity, response, () -> getCities());
            }

            @Override
            public void onFailure(Call<SplashResponse> call, Throwable t) {
                binding.loadingBar.setVisibility(View.INVISIBLE);
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(),t, () -> getSplash());
            }
        });

    }

    private void openCitiesDialog(ArrayList<City> cities) {

        ArrayList<String> items = new ArrayList<>();
        for (City city : cities)
            items.add(city.name);

        citySelectDialog = new SingleSelectDialog("انتخاب شهر", "شهر فعالیت خود را انتخاب کنید", items, position -> {

            citySelectDialog.dismiss();

            sh_p.saveString(SharedPreferencesRepository.SUB_DOMAIN, cities.get(position).subdomain);

            RetrofitAPIClient.setBaseUrl("https://" + cities.get(position).subdomain + ".backend.iranademo.ir");
//
            SplashActivity.this.finish();
            if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty()){
                Log.e("startActivity" , "44444");
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            else{
                Log.e("startActivity" , "55555");
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }

        });

        citySelectDialog.show(getSupportFragmentManager(),SingleSelectDialog.TAG);

    }
}