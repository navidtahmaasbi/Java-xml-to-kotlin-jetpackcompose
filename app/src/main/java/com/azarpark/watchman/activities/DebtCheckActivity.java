package com.azarpark.watchman.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.DebtListAdapter;
import com.azarpark.watchman.databinding.ActivityDebtCheckBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.parsian.ParsianPayment;
import com.azarpark.watchman.payment.saman.MyServiceConnection;
import com.azarpark.watchman.payment.saman.SamanPayment;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.util.ArrayList;
import java.util.UUID;

import ir.sep.android.Service.IProxy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DebtCheckActivity extends AppCompatActivity {

    ActivityDebtCheckBinding binding;
    private PlateType selectedTab = PlateType.simple;
    DebtListAdapter adapter;
    LoadingBar loadingBar;
    private int LIMIT = 20;
    int debt = 0;
    SharedPreferencesRepository sh_r;
    Activity activity = this;
    ParsianPayment parsianPayment;
    SamanPayment samanPayment;
    Assistant assistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebtCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assistant = new Assistant();
        parsianPayment = new ParsianPayment(getApplicationContext(),activity,new ParsianPayment.ParsianPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {
                DebtCheckActivity.this.verifyTransaction(transaction);
            }
            @Override
            public void getScannerData(int placeID) {

            }
        },getSupportFragmentManager());
        samanPayment = new SamanPayment(getApplicationContext(), DebtCheckActivity.this, new SamanPayment.SamanPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {
                DebtCheckActivity.this.verifyTransaction(transaction);
            }

            @Override
            public void getScannerData(int placeID) {
                //don't need to do anything in DebtCheck Activity
            }
        });
        sh_r = new SharedPreferencesRepository(getApplicationContext());

        binding.plateSimpleTag1.requestFocus();

        loadingBar = new LoadingBar(DebtCheckActivity.this);
        adapter = new DebtListAdapter();
        binding.recyclerView.setAdapter(adapter);

        binding.plateSimpleSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.simple);

        });

        binding.plateOldArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.old_aras);

        });

        binding.plateNewArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.new_aras);

        });

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

        binding.payment.setOnClickListener(view -> {

            if (selectedTab == PlateType.simple)
                paymentRequest(debt,
                        selectedTab,
                        binding.plateSimpleTag1.getText().toString(),
                        binding.plateSimpleTag2.getText().toString(),
                        binding.plateSimpleTag3.getText().toString(),
                        binding.plateSimpleTag4.getText().toString(),
                        -1
                        );
            else if (selectedTab == PlateType.old_aras)
                paymentRequest(debt,
                        selectedTab,
                        binding.plateOldAras.getText().toString(),
                        "0",
                        "0",
                        "0",
                        -1
                );
            else if (selectedTab == PlateType.new_aras)
                paymentRequest(debt,
                        selectedTab,
                        binding.plateNewArasTag1.getText().toString(),
                        binding.plateNewArasTag2.getText().toString(),
                        "0",
                        "0",
                        -1
                );

        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        parsianPayment.handleResult(requestCode, resultCode, data);

        samanPayment.handleResult(requestCode, resultCode, data);

        loadData(false);
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
        if (samanPayment != null)
            samanPayment.releaseService();
    }

    //------------------------------------------------------------ view

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

    //------------------------------------------------------------ api calls

    private void loadData(boolean isLazyLoad) {

        Assistant assistant = new Assistant();

        if (!isLazyLoad) {

            adapter.setItems(new ArrayList<>());
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
            getCarDebtHistory(
                    selectedTab,
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString(),
                    LIMIT,
                    adapter.getItemCount()
            );
        else if (selectedTab == PlateType.old_aras)
            getCarDebtHistory(
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0", "0", "0",
                    LIMIT,
                    adapter.getItemCount()

            );
        else
            getCarDebtHistory(
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0", "0",
                    LIMIT,
                    adapter.getItemCount()
            );

    }

    private void getCarDebtHistory(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        repository.getCarDebtHistory("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, limit, offset, new Callback<DebtHistoryResponse>() {
                    @Override
                    public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            if (response.body().getSuccess() == 1) {

                                if (response.body().balance < 0 )
                                 debt = response.body().balance * -1;

                                binding.balanceTitle.setText(response.body().balance >= 0 ? "اعتبار پلاک" : "بدهی پلاک");
                                binding.debtAmount.setTextColor(getResources().getColor(response.body().balance >= 0 ?R.color.green:R.color.red));
                                binding.payment.setVisibility(response.body().balance < 0 ? View.VISIBLE : View.GONE);

                                binding.debtAmount.setText(response.body().balance + " تومان");

                                binding.debtArea.setVisibility(View.VISIBLE);
                                adapter.addItems(response.body().items);


                            } else
                                Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();


                        } else APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(),activity, response, () -> getCarDebtHistory(plateType,tag1,tag2,tag3,tag4,limit,offset));
                    }

                    @Override
                    public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(),t, () -> getCarDebtHistory(plateType,tag1,tag2,tag3,tag4,limit,offset));
                    }
                });

    }

    public void paymentRequest(int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {

        long res_num = Assistant.generateResNum();

        if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
            parsianPayment.createTransaction(plateType, tag1, tag2, tag3, tag4,amount, -1);
        else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
            samanPayment.createTransaction(plateType, tag1, tag2, tag3, tag4,amount, -1);

    }

    private void verifyTransaction(Transaction transaction) {

        Log.d("verifyTransaction", "started ...");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        transaction.devideAmountByTen();

        repository.verifyTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                transaction, new Callback<VerifyTransactionResponse>() {
                    @Override
                    public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.isSuccessful()){

                            sh_r.removeFromTransactions(transaction);

                            Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();
                        }
                        else APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(),activity, response, () -> verifyTransaction(transaction));
                    }

                    @Override
                    public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        t.printStackTrace();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> verifyTransaction(transaction));
                    }
                });

    }
}