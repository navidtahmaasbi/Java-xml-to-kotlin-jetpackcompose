package com.azarpark.watchman.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.watchman.adapters.TicketListAdapter;
import com.azarpark.watchman.databinding.ActivityTicketsBinding;
import com.azarpark.watchman.dialogs.CreateTicketDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.VacationRequestDialog;
import com.azarpark.watchman.models.GetTicketsResponse;
import com.azarpark.watchman.models.Ticket;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TicketsActivity extends AppCompatActivity {

    ActivityTicketsBinding binding;
    TicketListAdapter listAdapter;
    CreateTicketDialog createTicketDialog;
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityTicketsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        listAdapter = new TicketListAdapter(
                (TicketListAdapter.OnItemClicked) ticket -> startActivity(
                        new Intent(TicketsActivity.this, TicketMessagesActivity.class).putExtra(Constants.TICKET_ID, ticket.id)));
        binding.recyclerView.setAdapter(listAdapter);

        binding.create.setOnClickListener(view -> {

            createTicketDialog = new CreateTicketDialog(() -> {
                createTicketDialog.dismiss();
                getListItems();
            });
            createTicketDialog.show(getSupportFragmentManager(), VacationRequestDialog.TAG);

        });

        binding.back.setOnClickListener(view -> onBackPressed());

        binding.refresh.setOnClickListener(view -> getListItems());

        getListItems();

    }

    private void getListItems() {

        Runnable functionRunnable = this::getListItems;
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).getTickets(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<GetTicketsResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetTicketsResponse> call, @NonNull Response<GetTicketsResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;
                if (response.body() != null)
                    listAdapter.setItems(response.body().tickets);
                binding.placeHolder.setVisibility(response.body().tickets.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(@NonNull Call<GetTicketsResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }
}