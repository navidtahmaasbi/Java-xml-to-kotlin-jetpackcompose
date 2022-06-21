package com.azarpark.watchman.dialogs;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.azarpark.watchman.databinding.DateSelectDialogBinding;
import com.azarpark.watchman.models.MyDate;
public class DateSelectDialog extends DialogFragment {

    public static final String TAG = "ConfirmDialog";
    DateSelectDialogBinding binding;
    ConfirmButtonClicks confirmButtonClicks;
    String title;
    String confirmButtonText;
    String cancelButtonText;
    MyDate initialDate;

    public DateSelectDialog(String title, String confirmButtonText, String cancelButtonText, MyDate initialDate, ConfirmButtonClicks confirmButtonClicks) {
        this.confirmButtonClicks = confirmButtonClicks;
        this.title = title;
        this.initialDate = initialDate;
        this.confirmButtonText = confirmButtonText;
        this.cancelButtonText = cancelButtonText;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = DateSelectDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setView(binding.getRoot());

        binding.title.setText(title);
        binding.confirm.setText(confirmButtonText);
        binding.cancel.setText(cancelButtonText);

        binding.year.setMinValue(initialDate.year);
        binding.year.setMaxValue(initialDate.year);
        binding.year.setValue(initialDate.year);

        binding.month.setMinValue(0);
        binding.month.setMaxValue(11);
        binding.month.setValue(initialDate.month-1);
        binding.month.setDisplayedValues( new String[] { "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور", "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند" } );
        binding.month.setOnValueChangedListener((numberPicker, i, i1) -> binding.day.setMaxValue(i1<6?31:30));

        binding.day.setMinValue(1);
        binding.day.setMaxValue(31);
        binding.day.setValue(initialDate.day);

        binding.confirm.setOnClickListener(view -> confirmButtonClicks.onConfirmClicked(binding.year.getValue(), binding.month.getValue()+1, binding.day.getValue()));
        binding.cancel.setOnClickListener(view -> dismiss());

        return builder.create();
    }

    public interface ConfirmButtonClicks {
        void onConfirmClicked(int year, int month, int day);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
