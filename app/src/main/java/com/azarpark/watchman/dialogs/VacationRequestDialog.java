package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.R;
import com.azarpark.watchman.activities.DebtListActivity;
import com.azarpark.watchman.databinding.ParkInfoDialogBinding;
import com.azarpark.watchman.databinding.VacationRequestDialogBinding;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.AddMobieToPlateResponse;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.EstimateParkPriceResponse;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.time.RadialPickerLayout;
import com.mohamadamin.persianmaterialdatetimepicker.time.TimePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import saman.zamani.persiandate.PersianDate;

public class VacationRequestDialog extends DialogFragment {

    public static final String TAG = "VacationRequestDialog";
    VacationRequestDialogBinding binding;
    Assistant assistant;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public VacationRequestDialog() {
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = VacationRequestDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        binding.date.setOnClickListener(view -> {

            PersianCalendar persianCalendar = new PersianCalendar();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        monthOfYear++;
                        String date = year + "/" + (monthOfYear<10?"0":"") + monthOfYear + "/" + dayOfMonth;
                        binding.date.setText("تاریخ : " + date);
                        binding.date.setTextColor(getContext().getResources().getColor(R.color.black));
                    },
                    persianCalendar.getPersianYear(),
                    persianCalendar.getPersianMonth(),
                    persianCalendar.getPersianDay()
            );
            datePickerDialog.show(getActivity().getFragmentManager(), "Datepickerdialog");

        });

        binding.startTime.setOnClickListener(view -> {

            PersianDate persianDate = new PersianDate();

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                    (TimePickerDialog.OnTimeSetListener) (view12, hourOfDay, minute) -> {
                        String time = hourOfDay + ":" + minute;
                        binding.startTime.setText("ساعت شروع : " + time);
                        binding.startTime.setTextColor(getContext().getResources().getColor(R.color.black));
                    },
                    persianDate.getHour(),
                    persianDate.getMinute(),
                    false
            );

            timePickerDialog.show(getActivity().getFragmentManager(), "timePickerDialog");

        });

        binding.endTime.setOnClickListener(view -> {

            PersianDate persianDate = new PersianDate();

            TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(
                    (TimePickerDialog.OnTimeSetListener) (view12, hourOfDay, minute) -> {
                        String time = hourOfDay + ":" + minute;
                        binding.endTime.setText("ساعت پایان : " + time);
                        binding.endTime.setTextColor(getContext().getResources().getColor(R.color.black));
                    },
                    persianDate.getHour(),
                    persianDate.getMinute(),
                    false
            );

            timePickerDialog.show(getActivity().getFragmentManager(), "timePickerDialog");

        });

        binding.confirm.setOnClickListener(view -> {
            dismiss();
        });

        binding.cancel.setOnClickListener(view -> {
            dismiss();
        });

        return builder.create();
    }

    private void createVacation() {

        Runnable functionRunnable = () -> createVacation();
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        WebService.getClient(getContext()).estimatePArkPrice(SharedPreferencesRepository.getTokenWithPrefix(), 0).enqueue(new Callback<EstimateParkPriceResponse>() {
            @Override
            public void onResponse(@NonNull Call<EstimateParkPriceResponse> call, @NonNull Response<EstimateParkPriceResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;


            }

            @Override
            public void onFailure(@NonNull Call<EstimateParkPriceResponse> call, @NonNull Throwable t) {
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
}
