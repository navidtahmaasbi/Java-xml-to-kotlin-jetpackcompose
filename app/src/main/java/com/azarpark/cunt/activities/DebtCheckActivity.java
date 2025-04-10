package com.azarpark.cunt.activities;

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

import com.azarpark.cunt.R;
import com.azarpark.cunt.adapters.DebtListAdapter;
import com.azarpark.cunt.databinding.ActivityDebtCheckBinding;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.models.Transaction;
import com.azarpark.cunt.payment.PaymentService;
import com.azarpark.cunt.payment.ShabaType;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;
import com.azarpark.cunt.web_service.responses.DebtHistoryResponse;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DebtCheckActivity extends AppCompatActivity {

    ActivityDebtCheckBinding binding;
    private PlateType selectedTab = PlateType.simple;
    DebtListAdapter adapter;
    LoadingBar loadingBar;
    int debt = 0;
    Activity activity = this;
    PaymentService paymentService;
    Assistant assistant;
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebtCheckBinding.inflate(getLayoutInflater());
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

                    }
                })
                .build();
        paymentService.initialize();


        binding.plateSimpleTag1.requestFocus();

        loadingBar = new LoadingBar(DebtCheckActivity.this);
        adapter = new DebtListAdapter();
        binding.recyclerView.setAdapter(adapter);

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

    }

    private void payment(View view) {

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

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        paymentService.onActivityResultHandler(requestCode, resultCode, data);
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
        if (adapter != null)
            adapter.setItems(new ArrayList<>());

    }

    //------------------------------------------------------------ api calls

    private void loadData(boolean isLazyLoad) {

        Assistant assistant = new Assistant();

        if (!isLazyLoad) {

            adapter.setItems(new ArrayList<>());
            binding.debtArea.setVisibility(View.GONE);
        }

        int LIMIT = 20;
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
                    binding.plateSimpleTag4.getText().toString(),
                    LIMIT,
                    adapter.getItemCount()
            );
        else if (selectedTab == PlateType.old_aras)
            getCarDebtHistory02(
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0", "0", "0",
                    LIMIT,
                    adapter.getItemCount()

            );
        else
            getCarDebtHistory02(
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0", "0",
                    LIMIT,
                    adapter.getItemCount()
            );

    }

    private void getCarDebtHistory02(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        Runnable functionRunnable = () -> getCarDebtHistory02(plateType, tag1, tag2, tag3, tag4, limit, offset);
        LoadingBar loadingBar = new LoadingBar(DebtCheckActivity.this);
        loadingBar.show();

        Assistant.hideKeyboard(DebtCheckActivity.this, binding.getRoot());

        webService.getClient(getApplicationContext()).getCarDebtHistory(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4, limit, offset, null,null,0).enqueue(new Callback<DebtHistoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<DebtHistoryResponse> call, @NonNull Response<DebtHistoryResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (response.body().balance < 0)
                    debt = response.body().balance * -1;

                binding.balanceTitle.setText(response.body().balance >= 0 ? "اعتبار پلاک" : "بدهی پلاک");
                binding.debtAmount.setTextColor(getResources().getColor(response.body().balance >= 0 ? R.color.green : R.color.red));
                binding.payment.setVisibility(response.body().balance < 0 ? View.VISIBLE : View.GONE);

                binding.debtAmount.setText(NumberFormat.getNumberInstance(Locale.US).format(response.body().balance < 0 ? (response.body().balance * -1) : response.body().balance) + " تومان");

                binding.debtArea.setVisibility(View.VISIBLE);
                adapter.addItems(response.body().items);


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

        paymentService.createTransaction(
                ShabaType.NON_CHARGE, plateType, tag1, tag2, tag3, tag4,
                amount, -1, Constants.TRANSACTION_TYPE_DEBT,
                () -> {
                    binding.payment.revertAnimation();
                    binding.payment.setOnClickListener(this::payment);
                }, -1, false, null
        );
    }

}