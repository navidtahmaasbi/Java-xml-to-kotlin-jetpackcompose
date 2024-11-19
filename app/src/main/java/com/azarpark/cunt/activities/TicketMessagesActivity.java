package com.azarpark.cunt.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.azarpark.cunt.adapters.TicketMessagesListAdapter;
import com.azarpark.cunt.databinding.ActivityTicketMessagesBinding;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.models.CreateTicketMessageResponse;
import com.azarpark.cunt.models.GetTicketResponse;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketMessagesActivity extends AppCompatActivity {

    ActivityTicketMessagesBinding binding;
    TicketMessagesListAdapter listAdapter;
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTicketMessagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int ticketId = getIntent().getIntExtra(Constants.TICKET_ID,0);
        if (ticketId == 0)
            onBackPressed();

        listAdapter = new TicketMessagesListAdapter();
        binding.recyclerView.setAdapter(listAdapter);

        binding.back.setOnClickListener(view -> onBackPressed());

        binding.refresh.setOnClickListener(view -> getListItems(ticketId));

        getListItems(ticketId);

        binding.send.setOnClickListener(view -> {

            if (binding.message.getText().toString().isEmpty()){
                Toast.makeText(getApplicationContext(), "پیام را بنویسید", Toast.LENGTH_SHORT).show();
                return;
            }

            createTicket(binding.message.getText().toString(), ticketId);

        });

    }

    private void getListItems(int ticketId) {

        Runnable functionRunnable = () -> getListItems(ticketId);
        LoadingBar loadingBar = new LoadingBar(this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).getTicket(SharedPreferencesRepository.getTokenWithPrefix(), ticketId).enqueue(new Callback<GetTicketResponse>() {
            @Override
            public void onResponse(@NonNull Call<GetTicketResponse> call, @NonNull Response<GetTicketResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;
                if (response.body() != null){

                    listAdapter.setItems(response.body().ticket.watchman_ticket_details);
                    binding.recyclerView.scrollToPosition(response.body().ticket.watchman_ticket_details.size() - 1);
                    binding.message.setText("");
                    binding.title.setText(response.body().ticket.subject);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GetTicketResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void createTicket(String text, int ticketId) {

        Runnable functionRunnable = () -> createTicket(text, ticketId);
        LoadingBar loadingBar = new LoadingBar(TicketMessagesActivity.this);
        loadingBar.show();

        webService.getClient(TicketMessagesActivity.this).createTicketMessage(SharedPreferencesRepository.getTokenWithPrefix(), ticketId, text).enqueue(new Callback<CreateTicketMessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTicketMessageResponse> call, @NonNull Response<CreateTicketMessageResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, TicketMessagesActivity.this))
                    return;

                if (response.body() != null)
                    Toast.makeText(TicketMessagesActivity.this, response.body().msg, Toast.LENGTH_SHORT).show();

                getListItems(ticketId);

            }

            @Override
            public void onFailure(@NonNull Call<CreateTicketMessageResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }
}