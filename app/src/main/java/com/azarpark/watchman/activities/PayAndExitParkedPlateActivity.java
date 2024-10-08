package com.azarpark.watchman.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ActivityPayAndExitParkedPlateBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.PaymentService;
import com.azarpark.watchman.payment.ShabaType;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.web_service.responses.ExitParkResponse;
import com.azarpark.watchman.web_service.responses.ParkedPlatePlaceIDResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayAndExitParkedPlateActivity extends AppCompatActivity {

    ActivityPayAndExitParkedPlateBinding binding;
    private PlateType selectedTab = PlateType.simple;
    LoadingBar loadingBar;
    private int LIMIT = 20;
    int debt = 0;
    Activity activity = this;
    PaymentService paymentService;

    Assistant assistant;
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPayAndExitParkedPlateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingBar = new LoadingBar(PayAndExitParkedPlateActivity.this);
        assistant = new Assistant();


        paymentService = new PaymentService.Builder()
                .activity(this)
                .webService(webService)
                .paymentCallback(new PaymentService.OnPaymentCallback() {
                    @Override
                    public void onScanDataReceived(int data) {

                    }

                    @Override
                    public void onTransactionVerified(@NonNull Transaction transaction) {

                    }
                })
                .build();
        paymentService.initialize();

        binding.debtSum.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                String stringValue = charSequence.toString();
                if (stringValue.length() > 9) {
                    binding.debtSum.setText(stringValue.substring(0, 9));
                } else if (assistant.isNumber(stringValue)) {
                    binding.debtSum.removeTextChangedListener(this);
                    stringValue = stringValue.replace(",", "");
                    int integerValue = Integer.parseInt(stringValue);
                    binding.debtSum.setText(assistant.formatAmount(integerValue));
                    binding.debtSum.setSelection(binding.debtSum.getText().length());
                    binding.debtSum.addTextChangedListener(this);
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag1.requestFocus();

        binding.plateSimpleSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.simple);

        });

        binding.plateOldArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.old_aras);

        });

        binding.plateNewArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.new_aras);

        });

        binding.submit.setOnClickListener(view -> getPlaceId());

        binding.plateSimpleTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (binding.plateSimpleTag1.getText().toString().length() == 2)     //size is your limit
                {
                    binding.plateSimpleTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (binding.plateSimpleTag2.getText().toString().length() == 1)     //size is your limit
                {
                    binding.plateSimpleTag3.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (binding.plateSimpleTag3.getText().toString().length() == 3)     //size is your limit
                {
                    binding.plateSimpleTag4.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateNewArasTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if (binding.plateNewArasTag1.getText().toString().length() == 5)     //size is your limit
                {
                    binding.plateNewArasTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        binding.debtArea.setVisibility(View.GONE);
        paymentService.onActivityResultHandler(requestCode, resultCode, data);
    }

    public void myOnBackPressed(View view) {

        onBackPressed();

        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        paymentService.stop();
    }

    //------------------------------------------------------------ view

    private void setSelectedTab(PlateType selectedTab) {

        this.selectedTab = selectedTab;
        resetDate();

        if (selectedTab == PlateType.simple) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.selected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.white));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.black));

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.old_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.white));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.black));

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.new_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.white));

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);
        }

    }

    private void resetDate() {

        binding.debtArea.setVisibility(View.GONE);

    }

    //------------------------------------------------------------ api calls

    private void getPlaceId() {

        Assistant assistant = new Assistant();

        binding.debtArea.setVisibility(View.GONE);

        if (selectedTab == PlateType.simple &&
                (binding.plateSimpleTag1.getText().toString().length() != 2 ||
                        binding.plateSimpleTag2.getText().toString().length() != 1 ||
                        binding.plateSimpleTag3.getText().toString().length() != 3 ||
                        binding.plateSimpleTag4.getText().toString().length() != 2))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.simple &&
                !assistant.isPersianAlphabet(binding.plateSimpleTag2.getText().toString()))
            Toast.makeText(getApplicationContext(), "حرف وسط پلاک باید فارسی باشد", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.old_aras &&
                binding.plateOldAras.getText().toString().length() != 5)
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

        else if (selectedTab == PlateType.new_aras &&
                (binding.plateNewArasTag1.getText().toString().length() != 5 ||
                        binding.plateNewArasTag2.getText().toString().length() != 2))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.simple)
            getPlaceId(
                    selectedTab,
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString()
            );
        else if (selectedTab == PlateType.old_aras)
            getPlaceId(
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0", "0", "0"
            );
        else
            getPlaceId(
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0", "0"
            );

    }

    private void getPlaceId(PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        Runnable functionRunnable = () -> getPlaceId(plateType, tag1, tag2, tag3, tag4);
        loadingBar.show();

        Assistant.hideKeyboard(PayAndExitParkedPlateActivity.this, binding.getRoot());

        webService.getClient(getApplicationContext()).getParkedPlatePlaceId(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4).enqueue(new Callback<ParkedPlatePlaceIDResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkedPlatePlaceIDResponse> call, @NonNull Response<ParkedPlatePlaceIDResponse> response) {


                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext())) {

                    loadingBar.dismiss();
                    return;
                }

                if (response.body().place == null) {
                    loadingBar.dismiss();
                    Toast.makeText(getApplicationContext(), "این پلاک در هیچ جایگاهی پارک نیست", Toast.LENGTH_SHORT).show();
                } else
                    getParkData(response.body().place.id, plateType, tag1, tag2, tag3, tag4);


            }

            @Override
            public void onFailure(@NonNull Call<ParkedPlatePlaceIDResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void getParkData(int placeId, PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        Runnable functionRunnable = () -> getParkData(placeId, plateType, tag1, tag2, tag3, tag4);

        webService.getClient(PayAndExitParkedPlateActivity.this).estimatePArkPrice(SharedPreferencesRepository.getTokenWithPrefix(), placeId).enqueue(new Callback<EstimateParkPriceResponse>() {
            @Override
            public void onResponse(@NonNull Call<EstimateParkPriceResponse> call, @NonNull Response<EstimateParkPriceResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, PayAndExitParkedPlateActivity.this))
                    return;

                EstimateParkPriceResponse parkPriceResponse = response.body();

                int parkPrice = parkPriceResponse.getPrice();
                int carBalance = parkPriceResponse.getCar_balance();
                int debt = carBalance - parkPrice;


                binding.carBalanceTitle.setText(carBalance >= 0 ? "اعتبار پلاک" : "بدهی پلاک");
                binding.carBalance.setTextColor(getResources().getColor(carBalance >= 0 ? R.color.green : R.color.red));
                binding.carBalance.setText((carBalance < 0 ? (carBalance * -1) : carBalance) + " تومان");
                binding.parkPrice.setText(parkPrice + " تومان");
                binding.payment.setVisibility(debt < 0 ? View.VISIBLE : View.GONE);

                if (debt < 0) {

                    binding.debtSum.setText(String.valueOf(debt * -1));
                    binding.payment.setOnClickListener(view -> Toast.makeText(getApplicationContext(), "برای انجام عملیات دکمه را نگه دارید", Toast.LENGTH_SHORT).show());
                    binding.payment.setOnLongClickListener(view -> {

                        String debtSumStringValue = binding.debtSum.getText().toString().replaceAll(",", "");
                        int debtSumIntegerValue = 0;
                        try {
                            debtSumIntegerValue = Integer.parseInt(debtSumStringValue);
                        } catch (Exception ignored) {
                        }
                        if (!assistant.isNumber(debtSumStringValue)) {
                            Toast.makeText(getApplicationContext(), "مبلغ را درست وارد کنید", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (debtSumIntegerValue < Constants.MIN_PRICE_FOR_PAYMENT) {
                            Toast.makeText(getApplicationContext(), "مبلغ شارژ نباید کمتر از " + assistant.formatAmount(Constants.MIN_PRICE_FOR_PAYMENT) + " تومان باشد", Toast.LENGTH_SHORT).show();
                            return true;
                        } else if (debtSumIntegerValue > Constants.MAX_PRICE_FOR_PAYMENT) {
                            Toast.makeText(getApplicationContext(), "مبلغ شارژ نباید بیشتر از " + assistant.formatAmount(Constants.MAX_PRICE_FOR_PAYMENT) + " تومان باشد", Toast.LENGTH_SHORT).show();
                            return true;
                        }


                        loadingBar.show();
                        paymentService.createTransaction(
                                ShabaType.NON_CHARGE, plateType,
                                tag1, tag2, tag3, tag4,
                                debtSumIntegerValue, placeId, Constants.TRANSACTION_TYPE_PARK_PRICE,
                                null, -1, false, null
                        );

                        return true;
                    });

                    binding.debtSumArea.setVisibility(View.VISIBLE);
                    binding.payment.setVisibility(View.VISIBLE);
                    binding.exitPark.setVisibility(View.GONE);
                } else {
                    binding.debtSumArea.setVisibility(View.GONE);
                    binding.payment.setVisibility(View.GONE);
                    binding.exitPark.setVisibility(View.VISIBLE);
                    binding.exitPark.setOnClickListener(view -> Toast.makeText(getApplicationContext(), "برای انجام عملیات دکمه را نگه دارید", Toast.LENGTH_SHORT).show());
                    binding.exitPark.setOnLongClickListener(view -> {
                        exitPark(placeId);
                        return true;
                    });
                }

                binding.debtArea.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(@NonNull Call<EstimateParkPriceResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void exitPark(int placeID) {

        Runnable functionRunnable = () -> exitPark(placeID);
        LoadingBar loadingBar = new LoadingBar(PayAndExitParkedPlateActivity.this);
        loadingBar.show();

        loadingBar.show();

        webService.getClient(getApplicationContext()).exitPark(SharedPreferencesRepository.getTokenWithPrefix(), placeID).enqueue(new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                binding.debtArea.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "خروج با موفقیت ثبت شد", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(Call<ExitParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    @Override
    protected void onStop() {
        loadingBar.dismiss();
        super.onStop();
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------


}