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
import com.azarpark.watchman.databinding.ParkInfoDialogBinding;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.web_service.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        binding.placeNumber.setText(place.number + "");
        try {


            Date now = Calendar.getInstance().getTime();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-M-dd hh:mm:ss");
            Date startDate = simpleDateFormat.parse(place.start);

            String startTime = place.start;
            startTime = startTime.split(" ")[1];
            binding.startTime.setText(startTime + " - " + assistant.getTimeDifference(startDate, now));


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

            onGetInfoClicked.charge(selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4);

        });

        binding.print.setOnClickListener(view -> {


            PlateType selectedPlateType = PlateType.simple;

            if (place.tag2 == null || place.tag2.isEmpty())
                selectedPlateType = PlateType.old_aras;
            else if (place.tag3 == null || place.tag3.isEmpty())
                selectedPlateType = PlateType.new_aras;


            onGetInfoClicked.print(place.start, selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, place.id, debt, balance);

        });

        return builder.create();
    }

//    private void getParkData(Place place) {
//
//        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getContext());
//        RetrofitAPIRepository repository = new RetrofitAPIRepository(getContext());
//        LoadingBar loadingBar = new LoadingBar(getActivity());
//
//        repository.estimateParkPrice("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), place.id, new Callback<EstimateParkPriceResponse>() {
//            @SuppressLint("SetTextI18n")
//            @Override
//            public void onResponse(Call<EstimateParkPriceResponse> call, Response<EstimateParkPriceResponse> response) {
//
//                try {
//
//                    if (place.exit_request != null) {
//
//                        binding.exitRequestArea.setVisibility(View.VISIBLE);
//                        binding.paymentArea.setVisibility(View.GONE);
//                        binding.chargeArea.setVisibility(View.GONE);
//                        binding.pay.setVisibility(View.GONE);
//
//                    } else {
//
//                        binding.exitRequestArea.setVisibility(View.GONE);
//                        binding.paymentArea.setVisibility(View.VISIBLE);
//
//                    }
//
//                    if (response.isSuccessful()) {
//
//                        if (response.body() != null && response.body().getSuccess() != 1) {
//
//                            try {
//                                Toast.makeText(getContext(), response.body().description != null ? response.body().description : response.body().getMsg(), Toast.LENGTH_SHORT).show();
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//
//                            return;
//                        }
//
//                        if (response.body().getSuccess() == 1) {
//
//                            EstimateParkPriceResponse parkPriceResponse = response.body();
//
////                            binding.startTime.setText(binding.startTime.getText() + "  " + (parkPriceResponse.getHours() + 1) + " ساعت");
//
//                            int parkPrice = parkPriceResponse.getPrice();
//                            int carBalance = parkPriceResponse.getCar_balance();
//                            balance = carBalance;
//
//                            binding.paymentArea.setVisibility(View.VISIBLE);
//
//                            if (carBalance < 0) {
//
//                                debt = -carBalance;
//
//                                binding.carBalanceTitle.setText("بدهی شما");
//                                binding.carBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(-carBalance) + " تومان");
//
//                                binding.carBalance.setTextColor(getResources().getColor(R.color.red));
//
//                                binding.showDebtList.setVisibility(View.VISIBLE);
//
//                                binding.parkPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(parkPrice) + " تومان");
//                                totalPrice = parkPrice - carBalance;
//                                binding.totalPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice) + " تومان");
//
//                                binding.balanceCheckbox.setVisibility(View.VISIBLE);
//                                binding.balanceIcon.setVisibility(View.GONE);
//
//
//                                binding.balanceCheckbox.setOnCheckedChangeListener((compoundButton, b) -> {
//
//                                    totalPrice = b ? parkPrice - carBalance : parkPrice;
//                                    binding.totalPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice) + " تومان");
//
//                                });
//
//                                binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
//                                binding.pay.setOnLongClickListener(view -> {
//                                    onGetInfoClicked.pay(totalPrice, place);
//                                    return false;
//                                });
//
//                            } else {
//
//                                binding.carBalanceTitle.setText("اعتبار پلاک");
//                                binding.carBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(carBalance) + " تومان");
//
//                                binding.showDebtList.setVisibility(View.GONE);
//
//                                binding.carBalance.setTextColor(getResources().getColor(R.color.dark_green));
//
//
//                                if (parkPrice == 0) {
//
//                                    totalPrice = parkPrice;
//
//                                    binding.pay.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_5_bg));
//                                    binding.pay.setText("خروج از پارک");
//
//                                    binding.payAsDebt.setVisibility(View.GONE);
//
//                                    binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
//                                    binding.pay.setOnLongClickListener(view -> {
//                                        onGetInfoClicked.payAsDebt(place);
//                                        return false;
//                                    });
//
//                                } else if (carBalance >= parkPrice) {
//
//                                    totalPrice = parkPrice;
//
//                                    binding.pay.setBackgroundDrawable(getResources().getDrawable(R.drawable.green_5_bg));
//                                    binding.pay.setText("کسر از اعتبار");
//
//                                    binding.payAsDebt.setVisibility(View.GONE);
//
//                                    binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
//                                    binding.pay.setOnLongClickListener(view -> {
//                                        onGetInfoClicked.payAsDebt(place);
//                                        return false;
//                                    });
//
//                                } else {
//
//                                    totalPrice = parkPrice - carBalance;
//
//                                    binding.pay.setOnClickListener(view -> Toast.makeText(getContext(), "برای انجام عملیات روی دکمه نگه دارید", Toast.LENGTH_SHORT).show());
//                                    binding.pay.setOnLongClickListener(view -> {
//                                        onGetInfoClicked.pay(totalPrice, place);
//                                        return false;
//                                    });
//
//                                }
//
//
//                                binding.totalPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(totalPrice) + " تومان");
//
//                                binding.parkPrice.setText(NumberFormat.getNumberInstance(Locale.US).format(parkPrice) + " تومان");
//
//                                binding.balanceCheckbox.setVisibility(View.GONE);
//                                binding.balanceIcon.setVisibility(View.VISIBLE);
//
//                            }
//
//                        } else
//                            Toast.makeText(getContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
//
//                    } else {
//                        try {
//                            APIErrorHandler.onResponseErrorHandler(getParentFragmentManager(), getActivity(), response, () -> getParkData(place));
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//
//                } catch (Exception e) {
//
//                    try {
//                        APIErrorHandler.onResponseErrorHandler(getParentFragmentManager(), getActivity(), response, () -> getParkData(place));
//
//                    } catch (Exception e2) {
//                        e2.printStackTrace();
//                    }
//
//                }
//
//
//            }
//
//            @Override
//            public void onFailure(Call<EstimateParkPriceResponse> call, Throwable t) {
//                APIErrorHandler.onFailureErrorHandler(getParentFragmentManager(), t, () -> getParkData(place));
//
//            }
//        });
//
//    }

    private void getParkData02(Place place) {

        Runnable functionRunnable = () -> getParkData02(place);
        LoadingBar.start(getContext());

        WebService.getClient(getContext()).estimatePArkPrice(SharedPreferencesRepository.getTokenWithPrefix(), place.id).enqueue(new Callback<EstimateParkPriceResponse>() {
            @Override
            public void onResponse(Call<EstimateParkPriceResponse> call, Response<EstimateParkPriceResponse> response) {

                LoadingBar.stop();
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

                binding.paymentArea.setVisibility(View.VISIBLE);

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

                }
                else {

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
                LoadingBar.stop();
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
