package com.azarpark.watchman.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ActivityChangePlateBinding;
import com.azarpark.watchman.databinding.DebtClearedPrintTemplateBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.AddMobieToPlateResponse;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.PaymentService;
import com.azarpark.watchman.payment.ShabaType;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.Logger;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.DebtHistoryResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePlateActivity extends AppCompatActivity {

    ActivityChangePlateBinding binding;
    private PlateType selectedTab = PlateType.simple;
    LoadingBar loadingBar;
    PaymentService paymentService;
    Assistant assistant;
    WebService webService = new WebService();
    MessageDialog messageDialog;
    int wagePrice = 0;
    int totalPrice = 0;

    int balance = 0;
    String ptag1, ptag2, ptag3, ptag4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePlateBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
                        printMiniFactor(ptag1, ptag2, ptag3, ptag4);
                        resetData();
                    }
                })
                .build();
        paymentService.initialize();


        binding.plateSimpleTag1.requestFocus();

        loadingBar = new LoadingBar(ChangePlateActivity.this);

        binding.plateSimpleSelector.setOnClickListener(view -> setSelectedTab(PlateType.simple));

        binding.plateOldArasSelector.setOnClickListener(view -> setSelectedTab(PlateType.old_aras));

        binding.plateNewArasSelector.setOnClickListener(view -> setSelectedTab(PlateType.new_aras));

        binding.submit.setOnClickListener(view -> loadData(false));

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

        binding.payment.setOnClickListener(this::payment);


        wagePrice = getWagePrice();
    }

    private void payment(View view) {
        String mobile = binding.mobile.getText().toString();
        if (mobile.isEmpty() || !assistant.isMobile(mobile)) {
            messageDialog = new MessageDialog("خطا",
                    "ثبت شماره موبایل الزامی می باشد",
                    "ثبت شماره",
                    () -> messageDialog.dismiss());

            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);
            return;
        }


        if (selectedTab == PlateType.simple)
            paymentRequest(totalPrice,
                    selectedTab,
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString(),
                    -1
            );
        else if (selectedTab == PlateType.old_aras)
            paymentRequest(totalPrice,
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0",
                    "0",
                    "0",
                    -1
            );
        else if (selectedTab == PlateType.new_aras)
            paymentRequest(totalPrice,
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0",
                    "0",
                    -1
            );

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        paymentService.onActivityResultHandler(requestCode, resultCode, data);
//        loadData(false);
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
        resetData();

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

    private void resetData() {
        binding.printArea.setVisibility(View.GONE);
        binding.debtArea.setVisibility(View.GONE);
        binding.plateSimpleTag1.setText("");
        binding.plateSimpleTag2.setText("");
        binding.plateSimpleTag3.setText("");
        binding.plateSimpleTag4.setText("");
        binding.plateOldAras.setText("");
        binding.plateNewArasTag1.setText("");
        binding.plateNewArasTag2.setText("");
        binding.mobile.setText("");

        totalPrice = 0;
        ptag1 = "";
        ptag2 = "";
        ptag3 = "";
        ptag4 = "";
        balance = 0;
    }

    //------------------------------------------------------------ api calls

    private void loadData(boolean isLazyLoad) {

        Assistant assistant = new Assistant();

        if (!isLazyLoad) {
            binding.debtArea.setVisibility(View.GONE);
        }

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
            getCarDebtHistory02(
                    selectedTab,
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString()
            );
        else if (selectedTab == PlateType.old_aras)
            getCarDebtHistory02(
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0", "0", "0"
            );
        else
            getCarDebtHistory02(
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0", "0"
            );

    }

    private void getCarDebtHistory02(PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        Runnable functionRunnable = () -> getCarDebtHistory02(plateType, tag1, tag2, tag3, tag4);
        LoadingBar loadingBar = new LoadingBar(ChangePlateActivity.this);
        loadingBar.show();

        Assistant.hideKeyboard(ChangePlateActivity.this, binding.getRoot());

        webService.getClient(getApplicationContext()).getCarDebtHistory(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4, 0, 0).enqueue(new Callback<DebtHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<DebtHistoryResponse> call, @NonNull Response<DebtHistoryResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                totalPrice = calculateTotalPrice(response.body().balance);
                ptag1 = tag1;
                ptag2 = tag2;
                ptag3 = tag3;
                ptag4 = tag4;
                balance = response.body().balance;

                binding.totalPriceTv.setText(totalPrice + " تومان");

                binding.debtArea.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(@NonNull Call<DebtHistoryResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }


    public void paymentRequest(int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {
        binding.payment.startAnimation();
        binding.payment.setOnClickListener(null);
        Runnable retryFunction = () -> paymentRequest(amount, plateType, tag1, tag2, tag3, tag4, placeID);

        String mobile = binding.mobile.getText().toString();

        webService.getClient(this).addMobileToPlate(SharedPreferencesRepository.getTokenWithPrefix(), assistant.getPlateType(tag1, tag2, tag3, tag4).toString(), tag1 != null ? tag1 : "0", tag2 != null ? tag2 : "0", tag3 != null ? tag3 : "0", tag4 != null ? tag4 : "0", mobile, 1).enqueue(new Callback<AddMobieToPlateResponse>() {
            @Override
            public void onResponse(Call<AddMobieToPlateResponse> call, Response<AddMobieToPlateResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, ChangePlateActivity.this)) {
                    binding.payment.revertAnimation();
                    binding.payment.setOnClickListener(ChangePlateActivity.this::payment);
                    return;
                }

                PaymentService.OnTransactionCreated finalAction = () -> {
                    binding.payment.revertAnimation();
                    binding.payment.setOnClickListener(ChangePlateActivity.this::payment);
                };

                if(amount == 0){
                    finalAction.onCreateTransactionFinished();
                    printMiniFactor(ptag1, ptag2, ptag3, ptag4);
                }
                else {
                    paymentService.createTransaction(
                            ShabaType.NON_CHARGE, plateType, tag1, tag2, tag3, tag4,
                            amount, -1, Constants.TRANSACTION_TYPE_DEBT,
                            finalAction, -1
                    );
                }
            }

            @Override
            public void onFailure(Call<AddMobieToPlateResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), retryFunction);
            }
        });
    }

    private void printMiniFactor(String tag1, String tag2, String tag3, String tag4) {
        binding.printArea.removeAllViews();

        DebtClearedPrintTemplateBinding printTemplateBinding = DebtClearedPrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), binding.printArea, true);

        if (assistant.getPlateType(tag1, tag2, tag3, tag4) == PlateType.simple) {

            printTemplateBinding.plateSimpleArea.setVisibility(View.VISIBLE);
            printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
            printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);

            printTemplateBinding.plateSimpleTag1.setText(tag1);
            printTemplateBinding.plateSimpleTag2.setText(tag2);
            printTemplateBinding.plateSimpleTag3.setText(tag3);
            printTemplateBinding.plateSimpleTag4.setText(tag4);

        } else if (assistant.getPlateType(tag1, tag2, tag3, tag4) == PlateType.old_aras) {

            printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
            printTemplateBinding.plateOldArasArea.setVisibility(View.VISIBLE);
            printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);

            printTemplateBinding.plateOldArasTag1En.setText(tag1);
            printTemplateBinding.plateOldArasTag1Fa.setText(tag1);

        } else {

            printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
            printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
            printTemplateBinding.plateNewArasArea.setVisibility(View.VISIBLE);

            printTemplateBinding.plateNewArasTag1En.setText(tag1);
            printTemplateBinding.plateNewArasTag1Fa.setText(tag1);
            printTemplateBinding.plateNewArasTag2En.setText(tag2);
            printTemplateBinding.plateNewArasTag2Fa.setText(tag2);

        }

        printTemplateBinding.text.setText("\n.\n.\n.");

        paymentService.print(binding.printArea, 1500, this::resetData);
    }

    private int calculateTotalPrice(int balance) {
        return balance < 0 ? ((balance * -1) + wagePrice) : 0;
    }

    private int getWagePrice() {
        int price = 0;

        try {
            price = Integer.parseInt(SharedPreferencesRepository.getWagePrice());
        } catch (Exception e) {
            Logger.e("Error: No valid wage price received");
        }

        return price;
    }
}