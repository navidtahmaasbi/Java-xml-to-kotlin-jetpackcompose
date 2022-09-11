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

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.VacationRequestDialog02Binding;
import com.azarpark.watchman.models.CreateVacationResponse;
import com.azarpark.watchman.models.MyDate;
import com.azarpark.watchman.models.MyTime;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VacationRequestDialog02 extends DialogFragment {

    public static final String TAG = "VacationRequestDialog02";
    VacationRequestDialog02Binding binding;
    Assistant assistant;
    WebService webService = new WebService();
    DialogActions dialogActions;
    String selectedType = "daily";
    String selectedVacationType = Constants.ESTEHGAGI;
    String selectedDate = null;
    String selectedShamsiDate = null;
    String selectedStart = null;
    String selectedEnd = null;
    TimeSelectDialog timeSelectDialog;
    DateSelectDialog dateSelectDialog;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null && getDialog().getWindow() != null)
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public VacationRequestDialog02(DialogActions dialogActions) {
        this.dialogActions = dialogActions;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        binding = VacationRequestDialog02Binding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        binding.date.setOnClickListener(view -> {

            MyDate _date;
            if (selectedShamsiDate != null)
                _date = stringToMyDate(selectedShamsiDate);
            else
                _date = Assistant.getDate();

            dateSelectDialog = new DateSelectDialog("تاریخ را اتخاب کنید", "تایید", "لغو", _date, (year, month, day) -> {
                if (dateIsBeforeToday(new MyDate(year, month, day))) {
                    Toast.makeText(requireContext(), "تاریخ انتخاب شده نمیتواند قبل از امروز باشد", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedType.equals("daily") && dateIsToday(new MyDate(year, month, day))) {
                    Toast.makeText(requireContext(), "مرخصی روزانه باید یک روز قبل تا ساعت 4 اعلام شود شما امکان انتخاب این تاریخ را ندارید", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (selectedType.equals("daily") && dateIsTomorrow(new MyDate(year, month, day)) && isAfter4pm()) {
                    Toast.makeText(requireContext(), "مرخصی روزانه باید یک روز قبل تا ساعت 4 اعلام شود شما امکان انتخاب این تاریخ را ندارید", Toast.LENGTH_SHORT).show();
                    return;
                }
                String date = year + "/" + (month < 10 ? "0" : "") + month + "/" + day;
                binding.date.setText(String.format("تاریخ : %s", date));
                binding.date.setTextColor(requireContext().getResources().getColor(R.color.black));
                selectedDate = assistant.jalaliToMiladi(year, month, day);
                selectedShamsiDate = year + "-" + month + "-" + day;
                dateSelectDialog.dismiss();
            });

            dateSelectDialog.show(getParentFragmentManager(), DateSelectDialog.TAG);

        });

        binding.startTime.setOnClickListener(view -> {

            MyTime _time;
            if (selectedStart != null)
                _time = stringToMyTime(selectedStart);
            else
                _time = Assistant.getMyTime();

            timeSelectDialog = new TimeSelectDialog("ساعت شروع را انتخاب کنید", "تایید", "لغو", 7, 21, _time, (hour, minute) -> {
                if (dateIsToday(selectedShamsiDate) && timeIsBeforeNow(hour, minute)) {
                    Toast.makeText(requireContext(), "ساعت انتخاب شده نمیتواند قبل از الان باشد", Toast.LENGTH_SHORT).show();
                    return;
                }
                String time = hour + ":" + (minute < 10 ? "0" : "") + minute;
                selectedStart = time;
                binding.startTime.setText(String.format("ساعت شروع : %s", time));
                binding.startTime.setTextColor(requireContext().getResources().getColor(R.color.black));
                timeSelectDialog.dismiss();
            });

            if (selectedDate == null)
                Toast.makeText(requireContext(), "ابتدا تاریخ را وارد کنید", Toast.LENGTH_SHORT).show();
            else
                timeSelectDialog.show(getParentFragmentManager(), TimeSelectDialog.TAG);

        });

        binding.endTime.setOnClickListener(view -> {

            MyTime _time;
            if (selectedStart != null)
                _time = stringToMyTime(selectedStart);
            else
                _time = Assistant.getMyTime();

            timeSelectDialog = new TimeSelectDialog("ساعت پایان را انتخاب کنید", "تایید", "لغو", 7, 21, _time, (hour, minute) -> {
                if (dateIsToday(selectedShamsiDate) && timeIsBeforeNow(hour, minute)) {
                    Toast.makeText(requireContext(), "ساعت انتخاب شده نمیتواند قبل از الان باشد", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (differenceIsMoreThan3Hours(selectedStart, hour, minute)) {
                    Toast.makeText(requireContext(), "مرخصی روزانه نمیتواند بیشتر از 3 ساعت باشد لطفا میزان کمتری انتخاب کنید", Toast.LENGTH_SHORT).show();
                    return;
                }
                String time = hour + ":" + minute;
                selectedEnd = time;
                binding.endTime.setText(String.format("ساعت پایان : %s", time));
                binding.endTime.setTextColor(requireContext().getResources().getColor(R.color.black));
                timeSelectDialog.dismiss();
            });

            if (selectedStart == null)
                Toast.makeText(requireContext(), "ابتدا ساعت شروع را وارد کنید", Toast.LENGTH_SHORT).show();
            else
                timeSelectDialog.show(getParentFragmentManager(), TimeSelectDialog.TAG);

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
                createImprest(selectedDate, selectedType, selectedStart, selectedEnd, selectedVacationType, binding.beduneHugugReason.getText().toString());
            }
        });

        binding.cancel.setOnClickListener(view -> dismiss());

        binding.type.setOnCheckedChangeListener((radioGroup, i) -> {
            binding.startTime.setVisibility(i == R.id.hourly ? View.VISIBLE : View.GONE);
            binding.endTime.setVisibility(i == R.id.hourly ? View.VISIBLE : View.GONE);
            selectedType = i == R.id.hourly ? "hourly" : "daily";
        });

        binding.vacationType.setOnCheckedChangeListener((radioGroup, i) -> selectedVacationType = i == R.id.estehgagi ? Constants.ESTEHGAGI : i == R.id.estelaji ? Constants.ESTELAJI : i == R.id.tashvigi ? Constants.TASHVIGI : Constants.BEDUNE_HUGUG);

        return builder.create();
    }

    private boolean differenceIsMoreThan3Hours(String selectedStart, int endHour, int endMinute) {
        int startHour = Integer.parseInt(selectedStart.split(":")[0]);
        int startMinute = Integer.parseInt(selectedStart.split(":")[1]);
        int startMinutes = startHour * 60 + startMinute;
        int endMinutes = endHour * 60 + endMinute;
        return endMinutes - startMinutes > 180;
    }

    private MyTime stringToMyTime(String time) {
        return new MyTime(
                Integer.parseInt(time.split(":")[0]),
                Integer.parseInt(time.split(":")[1])
        );
    }

    private MyDate stringToMyDate(String date) {
        return new MyDate(
                Integer.parseInt(date.split("-")[0]),
                Integer.parseInt(date.split("-")[1]),
                Integer.parseInt(date.split("-")[2])
        );
    }

    private boolean dateIsTomorrow(MyDate date) {
        MyDate now = Assistant.getDate();
        return now.year == date.year && now.month == date.month && now.day + 1 == date.day;
    }

    private boolean dateIsToday(String date) {
        MyDate now = Assistant.getDate();
        int year = Integer.parseInt(date.split("-")[0]);
        int month = Integer.parseInt(date.split("-")[1]);
        int day = Integer.parseInt(date.split("-")[2]);
        return now.year == year && now.month == month && now.day == day;
    }

    private boolean dateIsToday(MyDate date) {
        MyDate now = Assistant.getDate();
        return now.year == date.year && now.month == date.month && now.day == date.day;
    }

    private boolean dateIsBeforeToday(MyDate date) {
        MyDate now = Assistant.getDate();
        return date.year < now.year || (date.year == now.year && date.month < now.month) || (date.year == now.year && date.month == now.month && date.day < now.day);
    }

    private boolean timeIsBeforeNow(int hour, int minute) {
        MyTime now = Assistant.getMyTime();
        return hour < now.hour || (hour == now.hour && minute < now.minute);

    }

    private boolean isAfter4pm() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour > 16;
    }

    private void createImprest(String date, String type, String start, String end, String vacationType, String description) {

        Runnable functionRunnable = () -> createImprest(date, type, start, end, vacationType, description);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        webService.getClient(getContext()).createVacation(SharedPreferencesRepository.getTokenWithPrefix(), date, type, start, end, vacationType, description).enqueue(new Callback<CreateVacationResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateVacationResponse> call, @NonNull Response<CreateVacationResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;

                if (response.body() != null)
                    Toast.makeText(requireContext(), response.body().description, Toast.LENGTH_SHORT).show();
                dialogActions.imtrestCreated();

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

    public interface DialogActions {
        void imtrestCreated();
    }
}
