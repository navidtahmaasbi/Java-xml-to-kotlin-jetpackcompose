package com.azarpark.watchman.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
import com.azarpark.watchman.retrofit_remote.RetrofitAPIClient;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.GetCitiesResponse;
import com.azarpark.watchman.retrofit_remote.responses.SplashResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
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
    SharedPreferencesRepository sh_p;
    ConfirmDialog confirmDialog;
    Activity activity = this;
    DownloadController downloadController;
    MessageDialog messageDialog;
    Assistant assistant;
    int versionCode = 0;
    String versionName = "";

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
            getCities02();
        else
            getSplash02();

    }

    public void updateApp(Context context, String url) {


        com.azarpark.watchman.download_utils.DownloadController downloadController = new com.azarpark.watchman.download_utils.DownloadController(context, url);
        downloadController.enqueueDownload();


    }

//    private void getCities() {
//
//
//        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
//        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
//
//        repository.getCities("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<GetCitiesResponse>() {
//            @Override
//            public void onResponse(Call<GetCitiesResponse> call, Response<GetCitiesResponse> response) {
//
//
//                binding.loadingBar.setVisibility(View.INVISIBLE);
//
//                if (response.isSuccessful()) {
//
//
//                    if (response.body() != null && !response.body().success.equals("1")) {
//
//                        Toast.makeText(getApplicationContext(), response.body().description != null ? response.body().description : response.body().msg, Toast.LENGTH_SHORT).show();
//
//                        return;
//                    }
//
//                    openCitiesDialog(response.body().items);
//
//                } else {
//
//                    binding.retry.setVisibility(View.VISIBLE);
//
//                    APIErrorHandler.onResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> getCities());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<GetCitiesResponse> call, Throwable t) {
//                binding.loadingBar.setVisibility(View.INVISIBLE);
//                binding.retry.setVisibility(View.VISIBLE);
//                t.printStackTrace();
//                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> getCities());
//            }
//        });
//
//    }

    private void getCities02() {

        Runnable functionRunnable = this::getCities02;
        binding.loadingBar.setVisibility(View.VISIBLE);

        WebService.getInitialClient().getCities().enqueue(new Callback<GetCitiesResponse>() {
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

    private void getSplash02() {

        Runnable functionRunnable = this::getSplash02;
        binding.loadingBar.setVisibility(View.VISIBLE);
        binding.retry.setVisibility(View.INVISIBLE);

        WebService.getClient(getApplicationContext()).getSplash(SharedPreferencesRepository.getTokenWithPrefix(), versionCode).enqueue(new Callback<SplashResponse>() {
            @Override
            public void onResponse(Call<SplashResponse> call, Response<SplashResponse> response) {

                binding.loadingBar.setVisibility(View.INVISIBLE);
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext())) {
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

//    private void getSplash() {
//
//        binding.loadingBar.setVisibility(View.INVISIBLE);
//
//        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
//        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
//
//        repository.getSplashData("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), versionCode, new Callback<SplashResponse>() {
//            @Override
//            public void onResponse(Call<SplashResponse> call, Response<SplashResponse> response) {
//
//
//                binding.loadingBar.setVisibility(View.INVISIBLE);
//                if (response.isSuccessful()) {
//
//
//
//
//
//                } else {
//                    binding.retry.setVisibility(View.VISIBLE);
//                    APIErrorHandler.onResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> getSplash());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<SplashResponse> call, Throwable t) {
//                binding.loadingBar.setVisibility(View.INVISIBLE);
//                binding.retry.setVisibility(View.VISIBLE);
//                t.printStackTrace();
//                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> getSplash());
//            }
//        });
//
//    }

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

            sh_p.saveString(SharedPreferencesRepository.SUB_DOMAIN, cities.get(position).subdomain);
            sh_p.saveString(SharedPreferencesRepository.CITY_ID, cities.get(position).id + "");

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
