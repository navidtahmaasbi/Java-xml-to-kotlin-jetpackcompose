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
import com.azarpark.watchman.databinding.ImpressedRequestDialogBinding;
import com.azarpark.watchman.models.CreateImpressedResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImpressedRequestDialog extends DialogFragment {

    public static final String TAG = "ImpressedRequestDialog";
    ImpressedRequestDialogBinding binding;
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

    public ImpressedRequestDialog(DialogActions dialogActions) {
        this.dialogActions = dialogActions;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = ImpressedRequestDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        binding.confirm.setOnClickListener(view -> {
            final String amount = binding.amount.getText().toString();
            if (amount.isEmpty()){
                Toast.makeText(requireContext(), "مبلغ را وارد کنید", Toast.LENGTH_SHORT).show();
            }else
                createImpressed(amount);
        });

        binding.cancel.setOnClickListener(view -> dismiss());

        return builder.create();
    }

    private void createImpressed(String amount) {

        Runnable functionRunnable = () -> createImpressed(amount);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        webService.getClient(getContext()).createImprest(SharedPreferencesRepository.getTokenWithPrefix(), amount).enqueue(new Callback<CreateImpressedResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateImpressedResponse> call, @NonNull Response<CreateImpressedResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;

                Toast.makeText(requireContext(), response.body().description, Toast.LENGTH_SHORT).show();
                dialogActions.intrestCreated();

            }

            @Override
            public void onFailure(@NonNull Call<CreateImpressedResponse> call, @NonNull Throwable t) {
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

    public static interface DialogActions{
        public void intrestCreated();
    }

}
