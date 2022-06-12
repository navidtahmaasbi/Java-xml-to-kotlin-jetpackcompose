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
import android.widget.RadioGroup;
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
import com.azarpark.watchman.models.CreateImpressedResponse;
import com.azarpark.watchman.models.CreateVacationResponse;
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
    WebService webService = new WebService();
    DialogActions dialogActions;
    String selectedType = "daily";
    String selectedDate = null;
    String selectedStart = null;
    String selectedEnd = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public VacationRequestDialog(DialogActions dialogActions) {
        this.dialogActions = dialogActions;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = VacationRequestDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        binding.date.setOnClickListener(view -> {

            PersianCalendar persianCalendar = new PersianCalendar();
            DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(
                    (view1, year, monthOfYear, dayOfMonth) -> {
                        monthOfYear++;
                        String date = year + "/" + (monthOfYear < 10 ? "0" : "") + monthOfYear + "/" + dayOfMonth;
                        binding.date.setText("تاریخ : " + date);
                        binding.date.setTextColor(getContext().getResources().getColor(R.color.black));
                        selectedDate = assistant.jalaliToMiladi(year, monthOfYear, dayOfMonth);
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
                        selectedStart = time;
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
                        selectedEnd = time;
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
            if (selectedDate == null) {
                Toast.makeText(requireContext(), "تاریخ را انتخاب کنید", Toast.LENGTH_SHORT).show();
            } else if (selectedType.equals("hourly") && selectedStart == null) {
                Toast.makeText(requireContext(), "ساعت شروع را انتخاب کنید", Toast.LENGTH_SHORT).show();
            } else if (selectedType.equals("hourly") && selectedEnd == null) {
                Toast.makeText(requireContext(), "ساعت پایان را انتخاب کنید", Toast.LENGTH_SHORT).show();
            } else {
                if (selectedType.equals("daily")) {
                    selectedStart = "7:00";
                    selectedEnd = "7:00";
                }
                createImpressed(selectedDate, selectedType, selectedStart, selectedEnd);
            }
        });

        binding.cancel.setOnClickListener(view -> {
            dismiss();
        });

        binding.vacationType.setOnCheckedChangeListener((radioGroup, i) -> {
            binding.startTime.setVisibility(i == R.id.hourly ? View.VISIBLE : View.GONE);
            binding.endTime.setVisibility(i == R.id.hourly ? View.VISIBLE : View.GONE);
            selectedType = i == R.id.hourly?"hourly":"daily";
        });

        return builder.create();
    }

    private void createImpressed(String date, String type, String start, String end) {

        Runnable functionRunnable = () -> createImpressed(type, date, start, end);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        webService.getClient(getContext()).createVacation(SharedPreferencesRepository.getTokenWithPrefix(), date, type, start, end).enqueue(new Callback<CreateVacationResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateVacationResponse> call, @NonNull Response<CreateVacationResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;

                Toast.makeText(requireContext(), response.body().description, Toast.LENGTH_SHORT).show();
                dialogActions.intrestCreated();

            }

            @Override
            public void onFailure(@NonNull Call<CreateVacationResponse> call, @NonNull Throwable t) {
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

    public static interface DialogActions {
        public void intrestCreated();
    }
}
