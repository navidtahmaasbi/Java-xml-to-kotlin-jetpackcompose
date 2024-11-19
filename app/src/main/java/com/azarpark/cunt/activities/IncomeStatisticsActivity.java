package com.azarpark.cunt.activities;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.cunt.databinding.ActivityIncomeStatisticsBinding;
import com.azarpark.cunt.databinding.KeyValueItemBinding;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.dialogs.MessageDialog;
import com.azarpark.cunt.models.IncomeStatisticsResponse;
import com.azarpark.cunt.models.KeyValueModel;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IncomeStatisticsActivity extends AppCompatActivity {

    ActivityIncomeStatisticsBinding binding;
    LoadingBar loadingBar;
    WebService webService = new WebService();
    MessageDialog messageDialog;
    boolean messageHasShown = false;

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
        webService.getClient(getApplicationContext()).getIncomeStatistics(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<IncomeStatisticsResponse>() {
            @Override
            public void onResponse(@NonNull Call<IncomeStatisticsResponse> call, @NonNull Response<IncomeStatisticsResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                for (KeyValueModel item : response.body().items)
                    binding.content.addView(getKeyValueItem(item.key, item.value));

                showMessage();
            }

            @Override
            public void onFailure(@NonNull Call<IncomeStatisticsResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void showMessage(){
        StringBuilder message = new StringBuilder();
        message.append("کارکرد شما به صورت جمع موارد زیر حساب میشود");
        message.append("\n\n");
        message.append("- شارژ");
        message.append("\n");
        message.append("- دریافتی + دریافتی بدهی");
        message.append("\n");
        message.append("- کسر شارژ");
        message.append("\n");
        message.append("- ثبت شماره تلفن");
        message.append("\n");
        message.append("- نصب اپلیکیشن با کد معرف شما");
        message.append("\n");
        message.append("- ثبت پارک شهروند");
        message.append("\n");

//        messageDialog = new MessageDialog("توجه", message.toString(), "متوجه شدم", () -> {
//            messageDialog.dismiss();
//        });
        messageDialog = MessageDialog.newInstance(
                "توجه",
                message.toString(),
                "متوجه شدم",
                () -> {
                    // Handle the confirm button click
                    messageDialog.dismiss(); // Dismiss the dialog
                }
        );


        messageDialog.setCancelable(false);
        if(!messageHasShown){
            messageHasShown = true;
            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);
        }
    }

    private View getKeyValueItem(String text, String value) {
        KeyValueItemBinding keyValueItem = KeyValueItemBinding.inflate(getLayoutInflater());
        keyValueItem.key.setText(text);
        keyValueItem.value.setText(value);
        return keyValueItem.getRoot();
    }

    public void myOnBackPressed(View view) {
        onBackPressed();
    }
}