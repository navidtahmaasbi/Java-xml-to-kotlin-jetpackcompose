package com.azarpark.watchman.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.databinding.ActivityIncomeStatisticsBinding;
import com.azarpark.watchman.databinding.KeyValueItemBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.IncomeStatisticsResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.ParkedPlatePlaceIDResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomeStatisticsActivity extends AppCompatActivity {

    ActivityIncomeStatisticsBinding binding;
    LoadingBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIncomeStatisticsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingBar = new LoadingBar(this);
        getIncomeStatistics();
    }

    private void getIncomeStatistics() {
        Runnable functionRunnable = this::getIncomeStatistics;
        loadingBar.show();
        Assistant.hideKeyboard(this, binding.getRoot());
        WebService.getClient(getApplicationContext()).getIncomeStatistics(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<IncomeStatisticsResponse>() {
            @Override
            public void onResponse(@NonNull Call<IncomeStatisticsResponse> call, @NonNull Response<IncomeStatisticsResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;
//                binding.content.addView(getKeyValueItem("ماه قبل", "9000000"));
//                binding.content.addView(getKeyValueItem("این ماه", "4000000"));
//                binding.content.addView(getKeyValueItem("دیروز", "500000"));
//                binding.content.addView(getKeyValueItem("امروز", "200000"));
            }
            @Override
            public void onFailure(@NonNull Call<IncomeStatisticsResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

        binding.content.addView(getKeyValueItem("ماه قبل", "9000000"));
        binding.content.addView(getKeyValueItem("این ماه", "4000000"));
        binding.content.addView(getKeyValueItem("دیروز", "500000"));
        binding.content.addView(getKeyValueItem("امروز", "200000"));
    }

    private View getKeyValueItem(String text, String value) {
        KeyValueItemBinding keyValueItem = KeyValueItemBinding.inflate(getLayoutInflater());
        keyValueItem.key.setText(text);
        keyValueItem.value.setText(value);
        return keyValueItem.getRoot();
    }
}