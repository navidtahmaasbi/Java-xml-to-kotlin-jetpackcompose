package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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
                if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty())
                    startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                else{

                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(0, 0);
                }


            }, 1500);

    }

    private void getCities() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(this);
//        loadingBar.show();

        repository.getCities("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<GetCitiesResponse>() {
            @Override
            public void onResponse(Call<GetCitiesResponse> call, Response<GetCitiesResponse> response) {

//                loadingBar.dismiss();
                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    openCitiesDialog(response.body().items);

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<GetCitiesResponse> call, Throwable t) {
//                loadingBar.dismiss();
                binding.loadingBar.setVisibility(View.INVISIBLE);
                confirmDialog = new ConfirmDialog(
                        getResources().getString(R.string.retry_title),
                        getResources().getString(R.string.retry_text),
                        getResources().getString(R.string.retry_confirm_button),
                        getResources().getString(R.string.retry_cancel_button),
                        new ConfirmDialog.ConfirmButtonClicks() {
                            @Override
                            public void onConfirmClicked() {
                                confirmDialog.dismiss();
                                getCities();
                            }

                            @Override
                            public void onCancelClicked() {
                                confirmDialog.dismiss();
                            }
                        }
                );
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
            if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty())
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            else{

                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }

        });

        citySelectDialog.show(getSupportFragmentManager(),SingleSelectDialog.TAG);

    }
}