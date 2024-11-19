package com.azarpark.cunt.dialogs;


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

import com.azarpark.cunt.R;
import com.azarpark.cunt.databinding.VacationRequestDialogBinding;
import com.azarpark.cunt.models.CreateVacationResponse;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;
import com.mohamadamin.persianmaterialdatetimepicker.date.DatePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.time.TimePickerDialog;
import com.mohamadamin.persianmaterialdatetimepicker.utils.PersianCalendar;

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
    String selectedVacationType = Constants.ESTEHGAGI;
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
                    (view12, hourOfDay, minute) -> {
                        String time = hourOfDay + ":" + minute;
                        selectedEnd = time;
                        binding.endTime.setText("ساعت پایان : " + time);
                        binding.endTime.setTextColor(getContext().getResources().getColor(R.color.black));
                    },
                    persianDate.getHour(),
                    persianDate.getMinute(),
                    false
            );
            timePickerDialog.setStartTime(10,0);
            timePickerDialog.show(getActivity().getFragmentManager(), "timePickerDialog");

        });

        binding.confirm.setOnClickListener(view -> {
            if (selectedDate == null) {
                Toast.makeText(requireContext(), "تاریخ را انتخاب کنید", Toast.LENGTH_SHORT).show();
            } else if (selectedType.equals("hourly") && selectedStart == null) {
                Toast.makeText(requireContext(), "ساعت شروع را انتخاب کنید", Toast.LENGTH_SHORT).show();
            } else if (selectedType.equals("hourly") && selectedEnd == null) {
                Toast.makeText(requireContext(), "ساعت پایان را انتخاب کنید", Toast.LENGTH_SHORT).show();
            } else  {
                if (selectedType.equals("daily")) {
                    selectedStart = "7:00";
                    selectedEnd = "7:00";
                }
                createImprest(selectedDate, selectedType, selectedStart, selectedEnd, selectedVacationType, binding.beduneHugugReason.getText().toString());
            }
        });

        binding.cancel.setOnClickListener(view -> {
            dismiss();
        });

        binding.type.setOnCheckedChangeListener((radioGroup, i) -> {
            binding.startTime.setVisibility(i == R.id.hourly ? View.VISIBLE : View.GONE);
            binding.endTime.setVisibility(i == R.id.hourly ? View.VISIBLE : View.GONE);
            selectedType = i == R.id.hourly?"hourly":"daily";
        });

        binding.vacationType.setOnCheckedChangeListener((radioGroup, i) -> {
            selectedVacationType = i == R.id.estehgagi? Constants.ESTEHGAGI :i == R.id.estelaji?Constants.ESTELAJI:i == R.id.tashvigi?Constants.TASHVIGI:Constants.BEDUNE_HUGUG;
        });

        return builder.create();
    }

    private void createImprest(String date, String type, String start, String end, String vacationType, String description) {

        Runnable functionRunnable = () -> createImprest(date,type, start, end, vacationType, description);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        webService.getClient(getContext()).createVacation(SharedPreferencesRepository.getTokenWithPrefix(), date, type, start, end, vacationType, description).enqueue(new Callback<CreateVacationResponse>() {
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
