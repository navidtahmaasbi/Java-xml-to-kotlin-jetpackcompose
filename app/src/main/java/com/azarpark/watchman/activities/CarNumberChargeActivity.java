package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.ChargeItemListAdapter;
import com.azarpark.watchman.databinding.ActivityCarNumberChargeBinding;
import com.azarpark.watchman.databinding.SamanAfterPaymentPrintTemplateBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.parsian.ParsianPayment;
import com.azarpark.watchman.payment.saman.SamanPayment;
import com.azarpark.watchman.web_service.responses.DebtHistoryResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarNumberChargeActivity extends AppCompatActivity {

    ActivityCarNumberChargeBinding binding;
    private PlateType selectedTab = PlateType.simple;
    LoadingBar loadingBar;
    ChargeItemListAdapter adapter;
    Activity activity = this;
    ParsianPayment parsianPayment;
    SamanPayment samanPayment;
    Assistant assistant;
    private int selectedAmount = 0;
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarNumberChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assistant = new Assistant();
        parsianPayment = new ParsianPayment(binding.printArea, getApplicationContext(), activity, new ParsianPayment.ParsianPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {
//                CarNumberChargeActivity.this.verifyTransaction(transaction);
            }

            @Override
            public void getScannerData(int placeID) {
                //dont need to do any thing in CarNumberActivity
            }

            @Override
            public void onVerifyFinished() {
                //todo print mimii factor
            }
        }, getSupportFragmentManager());
        samanPayment = new SamanPayment(getSupportFragmentManager(), getApplicationContext(), CarNumberChargeActivity.this, new SamanPayment.SamanPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {
//                CarNumberChargeActivity.this.verifyTransaction(transaction);
            }

            @Override
            public void getScannerData(int placeID) {
                //don't need to do anything in CarNumberCharge Activity
            }

            @Override
            public void onVerifyFinished() {
                if (Constants.SELECTED_PAYMENT == Constants.SAMAN) {

                    String tag1 = SharedPreferencesRepository.getValue(Constants.TAG1, "0");
                    String tag2 = SharedPreferencesRepository.getValue(Constants.TAG2, "0");
                    String tag3 = SharedPreferencesRepository.getValue(Constants.TAG3, "0");
                    String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");

                    getCarDebtHistory(assistant.getPlateType(tag1, tag2, tag3, tag4), tag1, tag2, tag3, tag4, 0, 1);

                } else if (Constants.SELECTED_PAYMENT == Constants.NOTHING)
                    Toast.makeText(getApplicationContext(), "این نسخه برای دستگاه پوز نیست لذا امکان اینجام این فرایند وجود ندارد", Toast.LENGTH_LONG).show();
            }
        });
        binding.plateSimpleTag1.requestFocus();

        loadingBar = new LoadingBar(CarNumberChargeActivity.this);

        binding.plateSimpleTag1.requestFocus();

        setSelectedTab(selectedTab);

        binding.plateSimpleSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.simple);

        });

        binding.plateOldArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.old_aras);

        });

        binding.plateNewArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.new_aras);

        });

        binding.submit.setOnClickListener(this::submit);

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

//        binding.amount.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                adapter.clearSelectedItem();
//
//                if (!charSequence.toString().isEmpty()){
//
//                    binding.amount.removeTextChangedListener(this);
//                    String amount = charSequence.toString();
//                    amount = amount.replace(",","");
//                    binding.amount.setText(assistant.formatAmount(Integer.parseInt(amount)));
//
//                    binding.amount.setSelection(binding.amount.getText().length());
//
//                    binding.amount.addTextChangedListener(this);
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        adapter = new ChargeItemListAdapter(amount -> {

            selectedAmount = amount;

//            binding.amount.setText(NumberFormat.getNumberInstance(Locale.US).format(amount));

        }, getApplicationContext());
        binding.recyclerView.setAdapter(adapter);

        ArrayList<Integer> items = new ArrayList<>();

        //todo release
//        items.add(1000);
        items.add(10000);
        items.add(20000);
        items.add(30000);
        items.add(50000);
        items.add(70000);
        items.add(100000);

        adapter.setItems(items);


    }

    private void submit(View view){
            if (selectedTab == PlateType.simple && !assistant.simplePlateIsValid(
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString()
            ))
                Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (selectedTab == PlateType.old_aras &&
                    binding.plateOldAras.getText().toString().length() != 5)
                Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

            else if (selectedTab == PlateType.new_aras &&
                    (binding.plateNewArasTag1.getText().toString().length() != 5 ||
                            binding.plateNewArasTag2.getText().toString().length() != 2))
                Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (selectedAmount == 0)
                Toast.makeText(getApplicationContext(), "مبلغ شارژ را انتخاب کنید", Toast.LENGTH_SHORT).show();
            else if (selectedTab == PlateType.simple)
                charge(
                        Integer.toString(selectedAmount),
                        selectedTab,
                        binding.plateSimpleTag1.getText().toString(),
                        binding.plateSimpleTag2.getText().toString(),
                        binding.plateSimpleTag3.getText().toString(),
                        binding.plateSimpleTag4.getText().toString()
                );
            else if (!assistant.isNumber(Integer.toString(selectedAmount)))
                Toast.makeText(getApplicationContext(), "مبلغ شارژ را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (selectedAmount < Constants.MIN_PRICE_FOR_PAYMENT)
                Toast.makeText(getApplicationContext(), "مبلغ شارژ نباید کمتر از " + Constants.MIN_PRICE_FOR_PAYMENT + " تومان باشد", Toast.LENGTH_SHORT).show();
            else if (selectedTab == PlateType.old_aras)
                charge(
                        Integer.toString(selectedAmount),
                        selectedTab,
                        binding.plateOldAras.getText().toString(),
                        "0", "0", "0"


                );
            else
                charge(
                        Integer.toString(selectedAmount),
                        selectedTab,
                        binding.plateNewArasTag1.getText().toString(),
                        binding.plateNewArasTag2.getText().toString(),
                        "0", "0"
                );
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        parsianPayment.handleResult(requestCode, resultCode, data);

        samanPayment.handleResult(requestCode, resultCode, data);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (samanPayment != null)
            samanPayment.releaseService();
    }

    //------------------------------------------------------------------ view

    private void setSelectedTab(PlateType selectedTab) {

        this.selectedTab = selectedTab;

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

    public void myOnBackPressed(View view) {

        onBackPressed();

        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

    }

    @SuppressLint("SetTextI18n")
    private void printFactor(String tag1, String tag2, String tag3, String tag4, int balance) {

        if (Constants.SELECTED_PAYMENT == Constants.SAMAN) {

            binding.printArea.removeAllViews();

            SamanAfterPaymentPrintTemplateBinding printTemplateBinding = SamanAfterPaymentPrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), binding.printArea, true);

            printTemplateBinding.balanceTitle.setText(balance < 0 ? "بدهی پلاک" : "شارژ پلاک");

            printTemplateBinding.balance.setText(balance + " تومان");

//            printTemplateBinding.prices.setText(pricing);

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

            new Handler().postDelayed(() -> {

                samanPayment.printParkInfo(binding.printArea);

            }, 500);


        } else if (Constants.SELECTED_PAYMENT == Constants.NOTHING)
            Toast.makeText(getApplicationContext(), "این نسخه برای دستگاه پوز نیست لذا امکان اینجام این فرایند وجود ندارد", Toast.LENGTH_LONG).show();


    }

    //------------------------------------------------------------------ api calls

    private void charge(String amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        binding.submit.setOnClickListener(null);
        binding.submit.startAnimation();
        amount = amount.replace(",", "");


        if (Constants.SELECTED_PAYMENT == Constants.PASRIAN)
            parsianPayment.createTransaction(plateType, tag1, tag2, tag3, tag4, Integer.parseInt(amount), -1, Constants.TRANSACTION_TYPE_CHAREG, () -> {
                binding.submit.revertAnimation();
                binding.submit.setOnClickListener(this::submit);
            });
        else if (Constants.SELECTED_PAYMENT == Constants.SAMAN)
            samanPayment.createTransaction(Constants.CHARGE_SHABA, plateType, tag1, tag2, tag3, tag4, Integer.parseInt(amount), -1, Constants.TRANSACTION_TYPE_CHAREG , ()->{
                binding.submit.revertAnimation();
                binding.submit.setOnClickListener(this::submit);
            });
        else if (Constants.SELECTED_PAYMENT == Constants.NOTHING)
            Toast.makeText(getApplicationContext(), "این نسخه برای دستگاه پوز نیست لذا امکان اینجام این فرایند وجود ندارد", Toast.LENGTH_LONG).show();
    }

    private void getCarDebtHistory(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        Runnable functionRunnable = () -> getCarDebtHistory(plateType, tag1, tag2, tag3, tag4, limit, offset);
        LoadingBar loadingBar = new LoadingBar(CarNumberChargeActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).getCarDebtHistory(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4, limit, offset).enqueue(new Callback<DebtHistoryResponse>() {
            @Override
            public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                Assistant.hideKeyboard(CarNumberChargeActivity.this, binding.getRoot());

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;


                if (selectedTab == PlateType.simple)
                    printFactor(tag1,
                            tag2,
                            tag3,
                            tag4, response.body().balance);
                else if (selectedTab == PlateType.old_aras)
                    printFactor(tag1, "0", "0", "0", response.body().balance);
                else
                    printFactor(tag1, tag2, "0", "0", response.body().balance);


            }

            @Override
            public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }


}
