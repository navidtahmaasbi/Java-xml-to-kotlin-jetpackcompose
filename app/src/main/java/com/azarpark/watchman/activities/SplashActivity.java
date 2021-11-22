package com.azarpark.watchman.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.UpdateApp;
import com.azarpark.watchman.databinding.ActivitySplashBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.SingleSelectDialog;
import com.azarpark.watchman.models.City;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIClient;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.DownloadController;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

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
    DownloadController downloadController;
    MessageDialog messageDialog;
    Assistant assistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        sh_p = new SharedPreferencesRepository(getApplicationContext());

        assistant = new Assistant();


        if (assistant.VPNEnabled(getApplicationContext())){

            messageDialog = new MessageDialog("عدم دسترسی",
                    "برنامه اذرپارک در خارج از کشور قابل دسترسی نیست درصورت روشن بودن وی پی ان ان را خاموش کرده و دوباره وارد برنامه شوید",
                    "خروج",
                    () -> {
                        SplashActivity.this.finish();
                    });

            messageDialog.setCancelable(false);
            messageDialog.show(getSupportFragmentManager(),MessageDialog.TAG);

        }
        else if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty())
            getCities();
        else
            getSplash();

        binding.retry.setOnClickListener(view -> {

            if (assistant.VPNEnabled(getApplicationContext())){

                messageDialog = new MessageDialog("عدم دسترسی",
                        "برنامه اذرپارک در خارج از کشور قابل دسترسی نیست درصورت روشن بودن وی پی ان ان را خاموش کرده و دوباره وارد برنامه شوید",
                        "خروج",
                        () -> {
                            SplashActivity.this.finish();
                        });

                messageDialog.setCancelable(false);
                messageDialog.show(getSupportFragmentManager(),MessageDialog.TAG);

            }
            else if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty())
                getCities();
            else
                getSplash();

        });


    }

    public void updateApp(Context context, String url) {


        com.azarpark.watchman.download_utils.DownloadController downloadController = new com.azarpark.watchman.download_utils.DownloadController(context, url);
        downloadController.enqueueDownload();


    }

    private void getCities() {


        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());

        repository.getCities("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<GetCitiesResponse>() {
            @Override
            public void onResponse(Call<GetCitiesResponse> call, Response<GetCitiesResponse> response) {


                binding.loadingBar.setVisibility(View.INVISIBLE);

                if (response.isSuccessful()) {

                    if (!response.body().success.equals("1")){

                        Toast.makeText(getApplicationContext(), response.body().description, Toast.LENGTH_LONG).show();
                        return;
                    }

                    openCitiesDialog(response.body().items);

                } else{

                    binding.retry.setVisibility(View.VISIBLE);

                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> getCities());
                }
            }

            @Override
            public void onFailure(Call<GetCitiesResponse> call, Throwable t) {
                binding.loadingBar.setVisibility(View.INVISIBLE);
                binding.retry.setVisibility(View.VISIBLE);
                t.printStackTrace();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> getCities());
            }
        });

    }

    private void getSplash() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());

        repository.getSplashData("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<SplashResponse>() {
            @Override
            public void onResponse(Call<SplashResponse> call, Response<SplashResponse> response) {


                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (response.isSuccessful()) {

                    sh_r.saveString(SharedPreferencesRepository.qr_url, response.body().qr_url);
                    sh_r.saveString(SharedPreferencesRepository.refresh_time, Integer.toString(response.body().refresh_time));
                    sh_r.saveString(SharedPreferencesRepository.telephone, response.body().telephone);
                    sh_r.saveString(SharedPreferencesRepository.pricing, response.body().pricing);
                    sh_r.saveString(SharedPreferencesRepository.sms_number, response.body().sms_number);
                    sh_r.saveString(SharedPreferencesRepository.rules_url, response.body().rules_url);
                    sh_r.saveString(SharedPreferencesRepository.about_us_url, response.body().about_us_url);
                    sh_r.saveString(SharedPreferencesRepository.guide_url, response.body().guide_url);

                    try {
                        PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                        int version = pInfo.versionCode;


                        if (response.body().update.last_version > version)
                            openUpdateDialog(response.body().update.update_link);
                        else {

                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            SplashActivity.this.finish();
                        }

                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        binding.retry.setVisibility(View.VISIBLE);

                    }


                } else{
                    binding.retry.setVisibility(View.VISIBLE);
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> getCities());
                }
            }

            @Override
            public void onFailure(Call<SplashResponse> call, Throwable t) {
                binding.loadingBar.setVisibility(View.INVISIBLE);
                binding.retry.setVisibility(View.VISIBLE);
                t.printStackTrace();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> getSplash());
            }
        });

    }

    private void openUpdateDialog( String url){

        messageDialog = new MessageDialog("به روز رسانی",
                "برای برنامه به روزرسانی وجود دارد. بدون به روز رسانی قادر به ادامه نخواهید بود",
                "به روز رسانی",
                () -> {
                    updateApp(getApplicationContext(), url);
                    messageDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "بعد از دانلود شدن برنامه آن را نصب کنید", Toast.LENGTH_LONG).show();
                });

        messageDialog.setCancelable(false);
        messageDialog.show(getSupportFragmentManager(),MessageDialog.TAG);

    }

    private void openCitiesDialog(ArrayList<City> cities) {

        ArrayList<String> items = new ArrayList<>();
        for (City city : cities)
            items.add(city.name);

        citySelectDialog = new SingleSelectDialog("انتخاب شهر", "شهر فعالیت خود را انتخاب کنید", items, position -> {

            citySelectDialog.dismiss();

            sh_p.saveString(SharedPreferencesRepository.SUB_DOMAIN, cities.get(position).subdomain);
            sh_p.saveString(SharedPreferencesRepository.CITY_ID, cities.get(position).id+"");

            RetrofitAPIClient.setBaseUrl("https://" + cities.get(position).subdomain + ".backend1.azarpark.irana.app");
//            RetrofitAPIClient.setBaseUrl("https://" + cities.get(position).subdomain + ".backend.iranademo.ir");

            SplashActivity.this.finish();
            if (sh_p.getString(SharedPreferencesRepository.ACCESS_TOKEN).isEmpty()) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }

        });

        if (citySelectDialog != null)
            binding.getRoot().post(() -> citySelectDialog.show(getSupportFragmentManager(), SingleSelectDialog.TAG));



    }

}
