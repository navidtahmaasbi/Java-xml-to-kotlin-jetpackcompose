package com.azarpark.cunt.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.cunt.adapters.VacationListAdapter;
import com.azarpark.cunt.databinding.ActivityVacationsBinding;
import com.azarpark.cunt.dialogs.ConfirmDialog;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.dialogs.MessageDialog;
import com.azarpark.cunt.dialogs.VacationRequestDialog;
import com.azarpark.cunt.dialogs.VacationRequestDialog02;
import com.azarpark.cunt.models.GetVacationsResponse;
import com.azarpark.cunt.models.RemoveVacationResponse;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VacationsActivity extends AppCompatActivity {

    ActivityVacationsBinding binding;
    VacationListAdapter vacationListAdapter;
    VacationRequestDialog02 vacationRequestDialog;
    ConfirmDialog confirmDialog;
    WebService webService = new WebService();
    MessageDialog messageDialog;
    boolean messageHasShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVacationsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vacationListAdapter = new VacationListAdapter(id -> {

            confirmDialog = new ConfirmDialog("توجه", "ایا از حذف مرخصی اطمینان دارید؟", "بله حذف کن", "انصراف", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {
                    removeVacation(id);

                }

                @Override
                public void onCancelClicked() {
                    confirmDialog.dismiss();
                }
            });
            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

        });
        binding.recyclerView.setAdapter(vacationListAdapter);

        binding.getList.setOnClickListener(view -> getListItems());

        binding.create.setOnClickListener(view -> {

            vacationRequestDialog = new VacationRequestDialog02(() -> {

                vacationRequestDialog.dismiss();
                getListItems();
            });
            vacationRequestDialog.show(getSupportFragmentManager(), VacationRequestDialog.TAG);

        });

        binding.back.setOnClickListener(view -> onBackPressed());

        binding.refresh.setOnClickListener(view -> getListItems());

        showMessage();

    }

//    private void showMessage() {
//
//        String message = SharedPreferencesRepository.getValue(Constants.VACATION_TITLE);
//        messageDialog = new MessageDialog("توجه", message, "متوجه شدم", () -> messageDialog.dismiss());
//        messageDialog.setCancelable(false);
//        if (!messageHasShown) {
//            messageHasShown = true;
//            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);
//        }
//    }
private void showMessage() {
    String message = SharedPreferencesRepository.getValue(Constants.VACATION_TITLE);
    messageDialog = MessageDialog.newInstance(
            "توجه",
            message,
            "متوجه شدم",
            () -> messageDialog.dismiss()
    );
    messageDialog.setCancelable(false);
    if (!messageHasShown) {
        messageHasShown = true;
        messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);
    }
}


    private void removeVacation(int id) {

        Runnable functionRunnable = () -> removeVacation(id);
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).deleteVacation(SharedPreferencesRepository.getTokenWithPrefix(), id).enqueue(new Callback<RemoveVacationResponse>() {
            @Override
            public void onResponse(@NonNull Call<RemoveVacationResponse> call, @NonNull Response<RemoveVacationResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                confirmDialog.dismiss();
                if (response.body() != null)
                    Toast.makeText(getApplicationContext(), response.body().description, Toast.LENGTH_SHORT).show();
                getListItems();

            }

            @Override
            public void onFailure(@NonNull Call<RemoveVacationResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void getListItems() {

        Runnable functionRunnable = this::getListItems;
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).getVacations(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<GetVacationsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetVacationsResponse> call, @NonNull Response<GetVacationsResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;
                if (response.body() != null)
                    vacationListAdapter.setItems(response.body().vacations);
                binding.placeHolder.setVisibility(response.body().vacations.isEmpty() ? View.VISIBLE : View.GONE);

            }

            @Override
            public void onFailure(@NonNull Call<GetVacationsResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }
}