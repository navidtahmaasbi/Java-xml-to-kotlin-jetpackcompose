package com.azarpark.watchman.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.DebtListAdapter;
import com.azarpark.watchman.databinding.ActivityDebtCheckBinding;
import com.azarpark.watchman.databinding.ActivityDebtListBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.ParkDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.DebtModel;
import com.azarpark.watchman.payment.MyServiceConnection;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;
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
    MessageDialog messageDialog;
    private int LIMIT = 20;
    int debt = 0;

    MyServiceConnection connection;
    IProxy service;
    SharedPreferencesRepository sh_r;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebtCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

//        binding.scrollView.getViewTreeObserver()
//                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
//                    @Override
//                    public void onScrollChanged() {
//                        if (binding.scrollView.getChildAt(0).getBottom()
//                                <= (binding.scrollView.getHeight() + binding.scrollView.getScrollY())) {
//                            System.out.println("--------> end");
//                            loadData(true);
//                        } else {
//                            //scroll view is not at bottom
//                        }
//                    }
//                });

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

    private void loadData(boolean isLazyLoad) {

        if (!isLazyLoad) {

            adapter.setItems(new ArrayList<>());
            binding.debtArea.setVisibility(View.GONE);
        }


        if (selectedTab == PlateType.simple &&
                (binding.plateSimpleTag1.getText().toString().isEmpty() ||
                        binding.plateSimpleTag2.getText().toString().isEmpty() ||
                        binding.plateSimpleTag3.getText().toString().isEmpty() ||
                        binding.plateSimpleTag4.getText().toString().isEmpty()))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

        else if (selectedTab == PlateType.old_aras &&
                binding.plateOldAras.getText().toString().isEmpty())
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

        else if (selectedTab == PlateType.new_aras &&
                (binding.plateNewArasTag1.getText().toString().isEmpty() ||
                        binding.plateNewArasTag2.getText().toString().isEmpty()))
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
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        loadingBar.show();

        repository.getCarDebtHistory("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, limit, offset, new Callback<DebtHistoryResponse>() {
                    @Override
                    public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.code() == HttpURLConnection.HTTP_OK) {

                            if (response.body().getSuccess() == 1) {

                                if (response.body().balance < 0 )
                                 debt = response.body().balance * -1;

                                binding.balanceTitle.setText(response.body().balance >= 0 ? "اعتبار شما" : "بدهی شما");
                                binding.payment.setVisibility(response.body().balance < 0 ? View.VISIBLE : View.GONE);

                                binding.debtAmount.setText(response.body().balance + " تومان");

                                binding.debtArea.setVisibility(View.VISIBLE);
                                adapter.addItems(response.body().items);


                            } else if (response.body().getSuccess() == 0) {

                                Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                            }

                        } else {

                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
                    }
                });

    }

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

    //-----------------------------------------------------------------------------


    private void initService() {

        Log.i("TAG", "initService()");
        connection = new MyServiceConnection(service);
        Intent i = new Intent();
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy");
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i("TAG", "initService() bound value: " + ret);
    }

    private void releaseService() {
        if (connection != null)
        unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseService();
    }

    public void paymentRequest(int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {

        amount *= 10;
        System.out.println("---------> amount : " + amount);

        sh_r.saveString(SharedPreferencesRepository.PLATE_TYPE, plateType.toString());
        sh_r.saveString(SharedPreferencesRepository.TAG1, tag1);
        sh_r.saveString(SharedPreferencesRepository.TAG2, tag2);
        sh_r.saveString(SharedPreferencesRepository.TAG3, tag3);
        sh_r.saveString(SharedPreferencesRepository.TAG4, tag4);
        sh_r.saveString(SharedPreferencesRepository.TAG4, tag4);
        sh_r.saveString(SharedPreferencesRepository.AMOUNT, String.valueOf(amount));
        sh_r.saveString(SharedPreferencesRepository.PLACE_ID, Integer.toString(placeID));

        Intent intent = new Intent();
        intent.putExtra("TransType", 1);
        intent.putExtra("Amount", String.valueOf(amount));
        intent.putExtra("ResNum", UUID.randomUUID().toString());
        intent.putExtra("AppId", String.valueOf(0));

        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));

        startActivityForResult(intent, 1);

    }

    //    int result= service.PrintByBitmap(getBitmapFromView(root));
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        } else {
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 1) {

            int state = data.getIntExtra("State", -1); // Response Code Switch

            String refNum = data.getStringExtra("RefNum"); // Reference number
            String resNum = data.getStringExtra("ResNum");

            sh_r.saveString(SharedPreferencesRepository.REF_NUM, refNum);
            // you should store the resNum variable and then call verify method
            System.out.println("--------> state : " + state);
            System.out.println("--------> refNum : " + refNum);
            System.out.println("--------> resNum : " + resNum);
            if (state == 0) // successful
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();

                verifyTransaction(
                        PlateType.valueOf(sh_r.getString(SharedPreferencesRepository.PLATE_TYPE)),
                        sh_r.getString(SharedPreferencesRepository.TAG1),
                        sh_r.getString(SharedPreferencesRepository.TAG2, "0"),
                        sh_r.getString(SharedPreferencesRepository.TAG3, "0"),
                        sh_r.getString(SharedPreferencesRepository.TAG4, "0"),
                        sh_r.getString(SharedPreferencesRepository.AMOUNT),
                        refNum,
                        Integer.parseInt(sh_r.getString(SharedPreferencesRepository.PLACE_ID))
                );


            } else
                Toast.makeText(getBaseContext(), "Purchase did faild....", Toast.LENGTH_LONG).show();

        } else if (resultCode == RESULT_OK && requestCode == 2) {
            System.out.println("---------> ScannerResult :" + data.getStringExtra("ScannerResult"));//https://irana.app/how?qr=090YK6
        }
    }

    public void verify(String refNum, String resNum) {

        try {
            int verifyResult = service.VerifyTransaction(0, refNum, resNum);
            if (verifyResult == 0) // sucsess
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();
            } else if (verifyResult == 1)//sucsess but print is faild
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();
                int r = service.PrintByRefNum(refNum);
            } else // faild
            {
                Toast.makeText(getBaseContext(), "Purchase did faild....", Toast.LENGTH_LONG).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    private void verifyTransaction(PlateType plateType, String tag1, String tag2, String tag3, String tag4, String amount, String transaction_id, int placeID) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        loadingBar.show();

        amount = Integer.toString((Integer.parseInt(amount) / 10));

        repository.verifyTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, amount, transaction_id, placeID, new Callback<VerifyTransactionResponse>() {
                    @Override
                    public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.code() == HttpURLConnection.HTTP_OK) {

                            Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                            onBackPressed();


                        } else {

                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
                    }
                });

    }

}