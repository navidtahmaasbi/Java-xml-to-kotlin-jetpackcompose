package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.content.Intent;
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
import com.azarpark.watchman.activities.DebtListActivity;
import com.azarpark.watchman.activities.MainActivity;
import com.azarpark.watchman.activities.SplashActivity;
import com.azarpark.watchman.databinding.ParkInfoDialogBinding;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.AddMobieToPlateResponse;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.web_service.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.LogoutResponse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParkInfoDialog extends DialogFragment {

    public static final String TAG = "ParkInfoDialog";
    ParkInfoDialogBinding binding;
    private OnGetInfoClicked onGetInfoClicked;
    private Place place;
    int totalPrice = 0;
    int debt = 0, balance = 0;
    Assistant assistant;
    boolean hasMobile = false;
    ConfirmDialog confirmDialog;
    String printDescription;
    int printCommand = 1;
    WebService webService = new WebService();

    public ParkInfoDialog() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public ParkInfoDialog(OnGetInfoClicked onGetInfoClicked, Place place) {
        this.onGetInfoClicked = onGetInfoClicked;
        this.place = place;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = ParkInfoDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        assistant = new Assistant();

        getParkData02(place);

        binding.newPark.setOnClickListener(view -> onGetInfoClicked.newPark(place));
        binding.newPark.setVisibility(View.VISIBLE);

        binding.placeNumber.setText(place.number + "");
        try {


            Date now = new Date();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date startDate = simpleDateFormat.parse(place.start);

            String startTime = place.start;
            startTime = startTime.split(" ")[1];

            String s = assistant.getTimeDifference(startDate, now);

            binding.startTime.setText(startTime + " - " + s);


        } catch (Exception e) {
            e.printStackTrace();
        }


        binding.acceptExitRequest.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
        binding.acceptExitRequest.setOnLongClickListener(view -> {

            onGetInfoClicked.payAsDebt(place);
            return false;
        });

        binding.declineExitRequest.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
        binding.declineExitRequest.setOnLongClickListener(view -> {
            onGetInfoClicked.removeExitRequest(place);
            return false;
        });

        if (assistant.getPlateType(place) == PlateType.simple) {

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldArasArea.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateSimpleTag1.setText(place.tag1);
            binding.plateSimpleTag2.setText(place.tag2);
            binding.plateSimpleTag3.setText(place.tag3);
            binding.plateSimpleTag4.setText(place.tag4);

        } else if (assistant.getPlateType(place) == PlateType.old_aras) {

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldArasArea.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateOldArasTag1En.setText(place.tag1);
            binding.plateOldArasTag1Fa.setText(place.tag1);

        } else {

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldArasArea.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);

            binding.plateNewArasTag1En.setText(place.tag1);
            binding.plateNewArasTag1Fa.setText(place.tag1);
            binding.plateNewArasTag2En.setText(place.tag2);
            binding.plateNewArasTag2Fa.setText(place.tag2);

        }

        binding.showDebtList.setOnClickListener(view -> {

            PlateType selectedPlateType = PlateType.simple;

            if (place.tag2 == null || place.tag2.isEmpty())
                selectedPlateType = PlateType.old_aras;
            else if (place.tag3 == null || place.tag3.isEmpty())
                selectedPlateType = PlateType.new_aras;

            Intent intent = new Intent(getActivity(), DebtListActivity.class);
            intent.putExtra("plateType", selectedPlateType.toString());
            intent.putExtra("tag1", place.tag1);
            intent.putExtra("tag2", place.tag2);
            intent.putExtra("tag3", place.tag3);
            intent.putExtra("tag4", place.tag4);
            startActivity(intent);
        });

        binding.payAsDebt.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
        binding.payAsDebt.setOnLongClickListener(view -> {

            onGetInfoClicked.payAsDebt(place);
            return false;
        });

        binding.increaseBalance.setOnClickListener(view -> {

            PlateType selectedPlateType = PlateType.simple;

            if (place.tag2 == null || place.tag2.isEmpty())
                selectedPlateType = PlateType.old_aras;
            else if (place.tag3 == null || place.tag3.isEmpty())
                selectedPlateType = PlateType.new_aras;

            onGetInfoClicked.charge(selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, hasMobile);

        });

        binding.print.setOnClickListener(view -> {

            if (printDescription == null || printDescription.isEmpty()) {

                Toast.makeText(getContext(), "کمی صبر کنید و دوباره امتحان کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            PlateType selectedPlateType = PlateType.simple;

            if (place.tag2 == null || place.tag2.isEmpty())
                selectedPlateType = PlateType.old_aras;
            else if (place.tag3 == null || place.tag3.isEmpty())
                selectedPlateType = PlateType.new_aras;

//            String startTime = place.start;
//            try {
//                startTime = startTime.split(" ")[1];
//
//            } catch (Exception e) {
//                System.out.println("---------> split exception");
//            }

            onGetInfoClicked.print(place.start, selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, place.id, debt, balance, printDescription, printCommand);

        });

        binding.submitMobile.setOnClickListener(view -> {

            String mobile = binding.mobile.getText().toString();

            if (!assistant.isMobile(mobile))
                Toast.makeText(getContext(), "موبایل را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (hasMobile) {

                Assistant.hideKeyboard(getActivity(), binding.getRoot());

                confirmDialog = new ConfirmDialog("توجه", "این پلاک دارای شماره تلفن می باشد! آیا از اضافه کردن شماره جدید اطمینان دارید؟", "بله اضافه کن", "انصراف", new ConfirmDialog.ConfirmButtonClicks() {
                    @Override
                    public void onConfirmClicked() {
                        addMobile(mobile, place.tag1, place.tag2, place.tag3, place.tag4);
                        confirmDialog.dismiss();
                    }

                    @Override
                    public void onCancelClicked() {
                        binding.mobile.setText("");
                        confirmDialog.dismiss();
                    }
                });

                confirmDialog.show(getParentFragmentManager(), ConfirmDialog.TAG);
            } else {

                Assistant.hideKeyboard(getActivity(), binding.getRoot());
                addMobile(mobile, place.tag1, place.tag2, place.tag3, place.tag4);
            }

        });

        return builder.create();
    }

    private void addMobile(String mobile, String tag1, String tag2, String tag3, String tag4) {


        Runnable functionRunnable = () -> addMobile(mobile, tag1, tag2, tag3, tag4);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

        webService.getClient(getContext()).addMobileToPlate(SharedPreferencesRepository.getTokenWithPrefix(), assistant.getPlateType(tag1, tag2, tag3, tag4).toString(), tag1 != null ? tag1 : "0", tag2!= null ? tag2 : "0", tag3!= null ? tag3 : "0", tag4!= null ? tag4 : "0", mobile).enqueue(new Callback<AddMobieToPlateResponse>() {
            @Override
            public void onResponse(Call<AddMobieToPlateResponse> call, Response<AddMobieToPlateResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;

                if (response.body() != null)
                    Toast.makeText(getContext(), response.body().description, Toast.LENGTH_SHORT).show();
                binding.mobile.setText("");


            }

            @Override
            public void onFailure(Call<AddMobieToPlateResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getParentFragmentManager(), functionRunnable);
            }
        });


    }

    private void getParkData02(Place place) {

        Runnable functionRunnable = () -> getParkData02(place);
        LoadingBar loadingBar = new LoadingBar(getActivity());
        loadingBar.show();

       webService.getClient(getContext()).estimatePArkPrice(SharedPreferencesRepository.getTokenWithPrefix(), place.id).enqueue(new Callback<EstimateParkPriceResponse>() {
            @Override
            public void onResponse(Call<EstimateParkPriceResponse> call, Response<EstimateParkPriceResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getContext()))
                    return;

                if (place.exit_request != null) {

                    binding.exitRequestArea.setVisibility(View.VISIBLE);
                    binding.paymentArea.setVisibility(View.GONE);
                    binding.chargeArea.setVisibility(View.GONE);
                    binding.pay.setVisibility(View.GONE);

                } else {

                    binding.exitRequestArea.setVisibility(View.GONE);
                    binding.paymentArea.setVisibility(View.VISIBLE);

                }

                EstimateParkPriceResponse parkPriceResponse = response.body();

                int parkPrice = parkPriceResponse.getPrice();
                int carBalance = parkPriceResponse.getCar_balance();
                balance = carBalance;

                binding.hasMobileStatus.setText(response.body().getUsers_count() == 0 ? "ثبت نشده" : "ثبت شده");
                binding.hasMobileStatus.setTextColor(getContext().getResources().getColor(response.body().getUsers_count() == 0 ? R.color.red : R.color.dark_green));

                hasMobile = response.body().getUsers_count() > 0;

                binding.paymentArea.setVisibility(View.VISIBLE);

                printDescription = response.body().getPrint_description();
                printCommand = response.body().print_command;

                if (printCommand == 0) {

                    binding.print.setVisibility(View.GONE);
                    binding.printPlaceholder.setVisibility(View.VISIBLE);

                }


                if (carBalance < 0) {

                    debt = -carBalance;

                    binding.carBalanceTitle.setText("بدهی شما");
                    binding.carBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(-carBalance) + " تومان");

                    binding.carBalance.setTextColor(getResources().getColor(R.color.red));

                    binding.showDebtList.setVisibility(View.VISIBLE);

                    binding.parkPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(parkPrice) + " تومان");
                    totalPrice = parkPrice - carBalance;
                    binding.totalPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice) + " تومان");

                    binding.balanceCheckbox.setVisibility(View.VISIBLE);
                    binding.balanceIcon.setVisibility(View.GONE);


                    binding.balanceCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {

                        totalPrice = b ? parkPrice - carBalance : parkPrice;
                        binding.totalPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice) + " تومان");

                    });

                    binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
                    binding.pay.setOnLongClickListener(view -> {

                        onGetInfoClicked.pay(totalPrice, place);
                        return false;
                    });

                } else {

                    binding.carBalanceTitle.setText("اعتبار پلاک");
                    binding.carBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(carBalance) + " تومان");

                    binding.showDebtList.setVisibility(View.GONE);

                    binding.carBalance.setTextColor(getResources().getColor(R.color.dark_green));


                    if (parkPrice == 0) {

                        totalPrice = parkPrice;

                        binding.pay.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_5_bg));
                        binding.pay.setText("خروج از پارک");

                        binding.payAsDebt.setVisibility(View.GONE);

                        binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
                        binding.pay.setOnLongClickListener(view -> {

                            onGetInfoClicked.payAsDebt(place);
                            return false;
                        });

                    } else if (carBalance >= parkPrice) {

                        totalPrice = parkPrice;

                        binding.pay.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_5_bg));
                        binding.pay.setText("کسر از اعتبار");

                        binding.payAsDebt.setVisibility(View.GONE);

                        binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
                        binding.pay.setOnLongClickListener(view -> {

                            onGetInfoClicked.payAsDebt(place);
                            return false;
                        });

                    } else {

                        totalPrice = parkPrice - carBalance;

                        binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());

                        binding.pay.setOnLongClickListener(view -> {

                            onGetInfoClicked.pay(totalPrice, place);
                            return false;
                        });

                    }


                    binding.totalPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice) + " تومان");

                    binding.parkPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(parkPrice) + " تومان");

                    binding.balanceCheckbox.setVisibility(View.GONE);
                    binding.balanceIcon.setVisibility(View.VISIBLE);

                }

            }

            @Override
            public void onFailure(Call<EstimateParkPriceResponse> call, Throwable t) {
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
