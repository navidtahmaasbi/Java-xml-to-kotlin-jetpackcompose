package com.azarpark.watchman.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.adapters.ImprestListAdapter;
import com.azarpark.watchman.databinding.ActivityImprestBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.ImprestRequestDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.VacationRequestDialog;
import com.azarpark.watchman.models.GetImprestsResponse;
import com.azarpark.watchman.models.RemoveImpressedResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImprestActivity extends AppCompatActivity {

    ActivityImprestBinding binding;
    ImprestListAdapter imprestListAdapter;
    ImprestRequestDialog imprestRequestDialog;
    ConfirmDialog confirmDialog;
    WebService webService = new WebService();
    MessageDialog messageDialog;
    boolean messageHasShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityImprestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imprestListAdapter = new ImprestListAdapter(id -> {
            confirmDialog = new ConfirmDialog("توجه", "ایا از حذف مساعده اطمینان دارید؟", "بله حذف کن", "انصراف", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {
                    removeImprest(id);
                }

                @Override
                public void onCancelClicked() {
                    confirmDialog.dismiss();
                }
            });
            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

        });
        binding.recyclerView.setAdapter(imprestListAdapter);

        binding.create.setOnClickListener(view -> {

            imprestRequestDialog = new ImprestRequestDialog(() -> {

                imprestRequestDialog.dismiss();
                getListItems();
            });
            imprestRequestDialog.show(getSupportFragmentManager(), VacationRequestDialog.TAG);

        });

        binding.back.setOnClickListener(view -> onBackPressed());

        binding.refresh.setOnClickListener(view -> getListItems());

        binding.getList.setOnClickListener(view -> getListItems());

        showMessage();

    }

    private void showMessage() {
        StringBuilder message = new StringBuilder();
        message.append("درخواست مساعده در هر ماه یک بار مجاز میباشد");

        messageDialog = new MessageDialog("توجه", message.toString(), "متوجه شدم", () -> {
            messageDialog.dismiss();
        });

        messageDialog.setCancelable(false);
        if (!messageHasShown){
            messageHasShown = true;
            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);
        }
    }

    private void removeImprest(int id) {

        Runnable functionRunnable = () -> removeImprest(id);
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).deleteImprest(SharedPreferencesRepository.getTokenWithPrefix(), id).enqueue(new Callback<RemoveImpressedResponse>() {
            @Override
            public void onResponse(@NonNull Call<RemoveImpressedResponse> call, @NonNull Response<RemoveImpressedResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                confirmDialog.dismiss();
                Toast.makeText(getApplicationContext(), response.body().description, Toast.LENGTH_SHORT).show();
                getListItems();

            }

            @Override
            public void onFailure(@NonNull Call<RemoveImpressedResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void getListItems() {

        Runnable functionRunnable = () -> getListItems();
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).getImprests(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<GetImprestsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetImprestsResponse> call, @NonNull Response<GetImprestsResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;
                imprestListAdapter.setItems(response.body().imprests);
                binding.placeHolder.setVisibility(response.body().imprests.isEmpty() ? View.VISIBLE : View.GONE);


            }

            @Override
            public void onFailure(@NonNull Call<GetImprestsResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }
}





















