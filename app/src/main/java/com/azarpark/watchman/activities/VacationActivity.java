package com.azarpark.watchman.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azarpark.watchman.adapters.VacationListAdapter;
import com.azarpark.watchman.databinding.ActivityVacationBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.VacationRequestDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.GetVacationsResponse;
import com.azarpark.watchman.models.Vacation;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.ExitRequestResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VacationActivity extends AppCompatActivity {

    ActivityVacationBinding binding;
    VacationListAdapter vacationListAdapter;
    VacationRequestDialog vacationRequestDialog;
    ConfirmDialog confirmDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityVacationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        vacationListAdapter = new VacationListAdapter(id -> {

            confirmDialog = new ConfirmDialog("توجه", "ایا از حذف مرخصی اطمینان دارید؟", "بله حذف کن", "انصراف", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {
                    confirmDialog.dismiss();
                }

                @Override
                public void onCancelClicked() {
                    confirmDialog.dismiss();
                }
            });
            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

        });
        binding.recyclerView.setAdapter(vacationListAdapter);
        getListItems();

        binding.fab.setOnClickListener(view -> {

            vacationRequestDialog = new VacationRequestDialog();
            vacationRequestDialog.show(getSupportFragmentManager(), VacationRequestDialog.TAG);

        });

        binding.back.setOnClickListener(view -> onBackPressed());

    }

    private void getListItems() {

        Runnable functionRunnable = () -> getListItems();
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();


        WebService.getClient(getApplicationContext()).getVacations(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<GetVacationsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetVacationsResponse> call, @NonNull Response<GetVacationsResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                //todo implement
//                vacationListAdapter.setItems(response.body().items);
            }

            @Override
            public void onFailure(@NonNull Call<GetVacationsResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

        //todo remove these after implement api call
        ArrayList<Vacation> vacationList = new ArrayList<>();
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationList.add(new Vacation());
        vacationListAdapter.setItems(vacationList);

    }
}





















