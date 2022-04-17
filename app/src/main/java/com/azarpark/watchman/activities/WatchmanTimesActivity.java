package com.azarpark.watchman.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.MyLocationManager;
import com.azarpark.watchman.databinding.ActivityWatchmanTimesBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.models.WatchmanTimeResponse;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.DebtHistoryResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WatchmanTimesActivity extends AppCompatActivity {

    ActivityWatchmanTimesBinding binding;
    MyLocationManager myLocationManager;
    LoadingBar loadingBar;
    ConfirmDialog confirmDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWatchmanTimesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingBar = new LoadingBar(this);
        myLocationManager = new MyLocationManager(this, this);

        binding.startWork.setOnClickListener(view -> {
            confirmDialog = new ConfirmDialog("ثبت شروع کار", "برای شروع کار باید در محدوده خیابانی که محل کار شماست باشید. ایا به شروع کار مطمین هستید؟", "بله", "انصراف", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {
                    loadingBar.show();
                    myLocationManager.requestCurrentLocation(((lat, lon) -> setTime(Constants.START_WORK, lat, lon)));
                    confirmDialog.dismiss();
                }

                @Override
                public void onCancelClicked() {
                    confirmDialog.dismiss();
                }
            });
            confirmDialog.show(getSupportFragmentManager(), "start_time");
        });
        binding.finishWork.setOnClickListener(view -> {
            confirmDialog = new ConfirmDialog("ثبت اتمام کار", "برای اتمام کار باید در محدوده خیابانی که محل کار شماست باشید. ایا به اتمام کار مطمین هستید؟", "بله", "انصراف", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {
                    loadingBar.show();
                    myLocationManager.requestCurrentLocation(((lat, lon) -> setTime(Constants.END_WORK, lat, lon)));
                    confirmDialog.dismiss();
                }

                @Override
                public void onCancelClicked() {
                    confirmDialog.dismiss();
                }
            });
            confirmDialog.show(getSupportFragmentManager(), "finish_time");
        });

        binding.back.setOnClickListener(view -> onBackPressed());
    }

    public void setTime(String type, double latitude, double longitude) {
        Runnable functionRunnable = () -> setTime(type, latitude, longitude);
        WebService.getClient(getApplicationContext()).setWatchmanTime(SharedPreferencesRepository.getTokenWithPrefix(), type, Double.toString(latitude), Double.toString(longitude))
                .enqueue(new Callback<WatchmanTimeResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WatchmanTimeResponse> call, @NonNull Response<WatchmanTimeResponse> response) {

                        loadingBar.dismiss();
                        if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                            return;

                        Toast.makeText(WatchmanTimesActivity.this, response.body().description, Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(@NonNull Call<WatchmanTimeResponse> call, @NonNull Throwable t) {
                        loadingBar.dismiss();
                        NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
                    }
                });
    }
}