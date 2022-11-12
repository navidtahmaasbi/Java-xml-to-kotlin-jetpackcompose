package com.azarpark.watchman.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.azarpark.watchman.databinding.ActivitySplashBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.SingleSelectDialog;
import com.azarpark.watchman.models.City;
import com.azarpark.watchman.models.KeyValueModel;
import com.azarpark.watchman.web_service.responses.GetCitiesResponse;
import com.azarpark.watchman.web_service.responses.SplashResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.DownloadController;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;
    SingleSelectDialog citySelectDialog;
    ArrayList<City> cities;
//    SharedPreferencesRepository sh_p;
    ConfirmDialog confirmDialog;
    Activity activity = this;
    DownloadController downloadController;
    MessageDialog messageDialog;
    Assistant assistant;
    int versionCode = 0;
    String versionName = "";
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
//        sh_p = new SharedPreferencesRepository(getApplicationContext());

        assistant = new Assistant();

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
            versionName = pInfo.versionName;
            binding.version.setText("نسخه " + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        decide();

        binding.retry.setOnClickListener(view -> decide());

    }

    //------------------------------------------------------------------------------------------------------------------------

    private void decide() {

        if (assistant.VPNEnabled(getApplicationContext())) {

            messageDialog = new MessageDialog("عدم دسترسی",
                    "برنامه اذرپارک در خارج از کشور قابل دسترسی نیست درصورت روشن بودن وی پی ان ان را خاموش کرده و دوباره وارد برنامه شوید",
                    "خروج",
                    () -> {
                        SplashActivity.this.finish();
                    });

            messageDialog.setCancelable(false);
            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);

        }
        else if (SharedPreferencesRepository.getToken().isEmpty())
            getCities();
        else
            getSplash();

    }

    public void updateApp(Context context, String url) {


        com.azarpark.watchman.download_utils.DownloadController downloadController = new com.azarpark.watchman.download_utils.DownloadController(context, url);
        downloadController.enqueueDownload();


    }

    private void openUpdateDialog(String url) {

        messageDialog = new MessageDialog("به روز رسانی",
                "برای برنامه به روزرسانی وجود دارد. بدون به روز رسانی قادر به ادامه نخواهید بود",
                "به روز رسانی",
                () -> {
                    updateApp(getApplicationContext(), url);
                    messageDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "بعد از دانلود شدن برنامه آن را نصب کنید", Toast.LENGTH_LONG).show();
                });

        messageDialog.setCancelable(false);
        messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);

    }

    private void openCitiesDialog(ArrayList<City> cities) {

        ArrayList<String> items = new ArrayList<>();
        for (City city : cities)
            items.add(city.name);

        citySelectDialog = new SingleSelectDialog("انتخاب شهر", "شهر فعالیت خود را انتخاب کنید", items, position -> {

            citySelectDialog.dismiss();

            SharedPreferencesRepository.setValue(Constants.SUB_DOMAIN, cities.get(position).subdomain);
            SharedPreferencesRepository.setValue(Constants.CITY_ID, cities.get(position).id + "");

//            WebService.changeClientURL(cities.get(position).subdomain);

            SplashActivity.this.finish();
            if (SharedPreferencesRepository.getToken().isEmpty()) {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
            }

        });

        if (citySelectDialog != null)
            binding.getRoot().post(() -> citySelectDialog.show(getSupportFragmentManager(), SingleSelectDialog.TAG));


    }

    //------------------------------------------------------------------------------------------------------------------------

    private void getCities() {

        Runnable functionRunnable = this::getCities;
        binding.loadingBar.setVisibility(View.VISIBLE);

       webService.getClient(getApplicationContext()).getCities().enqueue(new Callback<GetCitiesResponse>() {
            @Override
            public void onResponse(Call<GetCitiesResponse> call, Response<GetCitiesResponse> response) {

                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                openCitiesDialog(response.body().items);
            }

            @Override
            public void onFailure(Call<GetCitiesResponse> call, Throwable t) {
                binding.loadingBar.setVisibility(View.INVISIBLE);
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    @SuppressLint("HardwareIds")
    private void getSplash() {

        Runnable functionRunnable = this::getSplash;
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.retry.setVisibility(View.INVISIBLE);

        String serial = "1111";//todo release
//        String serial =  android.os.Build.SERIAL;

        webService.getClient(getApplicationContext()).getSplash(SharedPreferencesRepository.getTokenWithPrefix(), versionCode, serial).enqueue(new Callback<SplashResponse>() {
            @Override
            public void onResponse(@NonNull Call<SplashResponse> call, @NonNull Response<SplashResponse> response) {

                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (NewErrorHandler.apiResponseHasError(response, SplashActivity.this)) {
                    binding.retry.setVisibility(View.VISIBLE);
                    return;
                }

                SharedPreferencesRepository.setValue(Constants.qr_url, response.body().qr_url);
                SharedPreferencesRepository.setValue(Constants.refresh_time, Integer.toString(response.body().refresh_time));
                SharedPreferencesRepository.setValue(Constants.telephone, response.body().telephone);
                SharedPreferencesRepository.setValue(Constants.pricing, response.body().pricing);
                SharedPreferencesRepository.setValue(Constants.sms_number, response.body().sms_number);
                SharedPreferencesRepository.setValue(Constants.rules_url, response.body().rules_url);
                SharedPreferencesRepository.setValue(Constants.about_us_url, response.body().about_us_url);
                SharedPreferencesRepository.setValue(Constants.guide_url, response.body().guide_url);
                SharedPreferencesRepository.setValue(Constants.print_description_2, response.body().print_description2);

                for (KeyValueModel keyValue : response.body().watchman_detail) {
                    if (keyValue.key.equals(Constants.cardNumber)){
                        SharedPreferencesRepository.setValue(Constants.cardNumber, keyValue.value);
                    }else if (keyValue.key.equals(Constants.shabaNumber)){
                        SharedPreferencesRepository.setValue(Constants.shabaNumber, keyValue.value);
                    }else if (keyValue.key.equals(Constants.accountNumber)){
                        SharedPreferencesRepository.setValue(Constants.accountNumber, keyValue.value);
                    }
                }

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
            }

            @Override
            public void onFailure(Call<SplashResponse> call, Throwable t) {
                binding.loadingBar.setVisibility(View.INVISIBLE);
                binding.retry.setVisibility(View.VISIBLE);
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }



}
