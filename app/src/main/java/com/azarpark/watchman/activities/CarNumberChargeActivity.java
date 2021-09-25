package com.azarpark.watchman.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.ChargeItemListAdapter;
import com.azarpark.watchman.databinding.ActivityCarNumberChargeBinding;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import ir.sep.android.Service.IProxy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CarNumberChargeActivity extends AppCompatActivity {

    ActivityCarNumberChargeBinding binding;
    private PlateType selectedTab = PlateType.old_aras;
    MyServiceConnection connection;
    IProxy service;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCarNumberChargeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        binding.submit.setOnClickListener(view -> {

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
                charge(
                        binding.amount.getText().toString(),
                        selectedTab,
                        binding.plateSimpleTag1.getText().toString(),
                        binding.plateSimpleTag2.getText().toString(),
                        binding.plateSimpleTag3.getText().toString(),
                        binding.plateSimpleTag4.getText().toString()
                );
            else if (binding.amount.getText().toString().isEmpty())
                Toast.makeText(getApplicationContext(), "مبلغ شارژ را وارد کنید", Toast.LENGTH_SHORT).show();
            else if (!isNumber(binding.amount.getText().toString()))
                Toast.makeText(getApplicationContext(), "مبلغ شارژ را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (selectedTab == PlateType.old_aras)
                charge(
                        binding.amount.getText().toString(),
                        selectedTab,
                        binding.plateOldAras.getText().toString(),
                        "0", "0", "0"


                );
            else
                charge(
                        binding.amount.getText().toString(),
                        selectedTab,
                        binding.plateNewArasTag1.getText().toString(),
                        binding.plateNewArasTag2.getText().toString(),
                        "0", "0"
                );
        });

        binding.plateSimpleTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.plateSimpleTag1.getText().toString().length()==2)     //size is your limit
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

                if(binding.plateSimpleTag2.getText().toString().length()==1)     //size is your limit
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

                if(binding.plateSimpleTag3.getText().toString().length()==3)     //size is your limit
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

                if(binding.plateNewArasTag1.getText().toString().length()==5)     //size is your limit
                {
                    binding.plateNewArasTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        ChargeItemListAdapter adapter = new ChargeItemListAdapter(amount -> {

            binding.amount.setText(NumberFormat.getNumberInstance(Locale.US).format(amount));

        });
        binding.recyclerView.setAdapter(adapter);

        ArrayList<Integer> items = new ArrayList<>();
        items.add(10000);
        items.add(20000);
        items.add(30000);

        adapter.setItems(items);


    }

    private boolean isNumber(String amount) {

        amount.replace(",","");

        try {

            int a = Integer.parseInt(amount);
        }catch (Exception e){

            return false;
        }

        return true;
    }

    private void charge(String amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        pay(Integer.parseInt(amount));

//        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
//        RetrofitAPIRepository repository = new RetrofitAPIRepository();
//        loadingBar.show();
//
//        repository.getCarDebtHistory("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
//                plateType, tag1, tag2, tag3, tag4, limit, offset, new Callback<DebtHistoryResponse>() {
//                    @Override
//                    public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {
//
//                        System.out.println("--------> url : " + response.raw().request().url());
//
//                        loadingBar.dismiss();
//                        if (response.code() == HttpURLConnection.HTTP_OK) {
//
//                            if (response.body().getSuccess() == 1) {
//
//                                binding.balanceTitle.setText(response.body().balance >= 0 ? "اعتبار شما" : "بدهی شما");
//
//                                binding.debtAmount.setText(response.body().balance + " تومان");
//
//                                binding.debtArea.setVisibility(View.VISIBLE);
//                                adapter.addItems(response.body().items);
//
//
//                            } else if (response.body().getSuccess() == 0) {
//
//                                Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
//
//                            }
//
//                        } else {
//
//                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();
//
//                        }
//                    }
//
//                    @Override
//                    public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
//                        loadingBar.dismiss();
//                        Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    private void setSelectedTab(PlateType selectedTab) {

        this.selectedTab = selectedTab;

        if (selectedTab == PlateType.simple) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.selected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_background);

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateSimpleTag1.requestFocus();

        } else if (selectedTab == PlateType.old_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_background);

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateOldAras.requestFocus();

        } else if (selectedTab == PlateType.new_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_background);

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);

            binding.plateNewArasTag1.requestFocus();
        }

    }

    public void myOnBackPressed(View view){

        onBackPressed();

    }

    //------------------------------------------------------------------


    class MyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IProxy.Stub.asInterface((IBinder) boundService);
            Log.i("--------->", "onServiceConnected(): Connected");
            Toast.makeText(CarNumberChargeActivity.this, "AIDLExample Service connected",
                    Toast.LENGTH_LONG).show();
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Log.i("---------->", "onServiceDisconnected(): Disconnected");
            Toast.makeText(CarNumberChargeActivity.this, "AIDLExample Service Connected",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initService() {
        Log.i("TAG", "initService()");
        connection = new MyServiceConnection();
        Intent i = new Intent();
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy");
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i("TAG", "initService() bound value: " + ret);
    }

    private void releaseService() {
        unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseService();
    }

    public void pay(int amount) {

        Intent intent = new Intent();
        intent.putExtra("TransType", 1);
        intent.putExtra("Amount", String.valueOf(amount));
        intent.putExtra("ResNum", UUID.randomUUID().toString());
        intent.putExtra("AppId", String.valueOf(0));


        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));

        startActivityForResult(intent,
                1);

    }

//    int result= service.PrintByBitmap(getBitmapFromView(root));
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
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

        if (resultCode == RESULT_OK && requestCode==1) {

            int state = data.getIntExtra("State", -1); // Response Code Switch

            String refNum = data.getStringExtra("RefNum"); // Reference number
            String resNum = data.getStringExtra("ResNum");
            // you should store the resNum variable and then call verify method
            System.out.println("--------> state : " + state);
            System.out.println("--------> refNum : " + refNum);
            System.out.println("--------> resNum : " + resNum);
            if (state == 0) // successful
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();
//                verify(refNum,resNum);
            } else
                Toast.makeText(getBaseContext(), "Purchase did faild....", Toast.LENGTH_LONG).show();

        }

        else if (resultCode == RESULT_OK && requestCode==2) {
            Toast.makeText(CarNumberChargeActivity.this,data.getStringExtra("ScannerResult"),Toast.LENGTH_LONG).show();
            System.out.println("---------> ScannerResult :" + data.getStringExtra("ScannerResult"));//https://irana.app/how?qr=090YK6
        }
    }

    public void verify(String refNum,String resNum){

        try {
            int verifyResult = service.VerifyTransaction(0, refNum,resNum);
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

}