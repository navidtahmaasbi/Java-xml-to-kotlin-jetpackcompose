package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.databinding.NewTicketDialogBinding;
import com.azarpark.watchman.models.CreateTicketResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateTicketDialog extends DialogFragment {

    public static final String TAG = "ImprestRequestDialog";
    NewTicketDialogBinding binding;
    Assistant assistant;
    WebService webService = new WebService();
    DialogActions dialogActions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public CreateTicketDialog(DialogActions dialogActions) {
        this.dialogActions = dialogActions;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = NewTicketDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        binding.confirm.setOnClickListener(view -> {
            final String title = binding.title.getText().toString();
            final String description = binding.description.getText().toString();
            if (title.isEmpty()) {
                Toast.makeText(requireContext(), "عنوان را وارد کنید", Toast.LENGTH_SHORT).show();
            } else if (description.isEmpty()) {
                Toast.makeText(requireContext(), "متن پیام را وارد کنید", Toast.LENGTH_SHORT).show();
            } else {
                createTicket(title, description);
            }
        });

        binding.cancel.setOnClickListener(view -> dismiss());

        return builder.create();
    }

    private void createTicket(String title, String description) {

        Runnable functionRunnable = () -> createTicket(title, description);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        webService.getClient(getContext()).createTicket(SharedPreferencesRepository.getTokenWithPrefix(), title, description).enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTicketResponse> call, @NonNull Response<CreateTicketResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;

                if (response.body() != null)
                    Toast.makeText(requireContext(), response.body().msg, Toast.LENGTH_SHORT).show();
                dialogActions.ticketCreated();

            }

            @Override
            public void onFailure(@NonNull Call<CreateTicketResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getParentFragmentManager(), functionRunnable);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    public interface DialogActions {
        void ticketCreated();
    }

}
