package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.activities.ExitRequestListActivity;
import com.azarpark.watchman.adapters.ParkListAdapter;
import com.azarpark.watchman.databinding.ActivityMainBinding;
import com.azarpark.watchman.dialogs.CheckoutDialog;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.ParkDialog;
import com.azarpark.watchman.dialogs.ParkDialog02;
import com.azarpark.watchman.interfaces.OnCheckoutButtonsClicked;
import com.azarpark.watchman.models.ParkModel;
import com.azarpark.watchman.models.Street;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    boolean menuIsOpen = false;
    PopupWindow popupWindow;
    View popupView;
    ConfirmDialog confirmDialog;
    ParkListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initMenuPopup();

        listeners();

        adapter = new ParkListAdapter(parkModel -> {

            openParkDialog02();

        });
        binding.recyclerView.setAdapter(adapter);

        getPlaces();

    }

    private void getPlaces() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this,MainActivity.this);
        loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    //todo set placeholder to empty street or places list
                    for (Street street : response.body().watchman.streets)
                        adapter.setItems(street.places);


                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openParkDialog() {

        ParkDialog parkDialog = new ParkDialog(parkModel -> {

        }, new ParkModel());
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void openParkDialog02() {

        ParkDialog02 parkDialog = new ParkDialog02(parkModel -> {

            openCheckoutDialog();

        }, new ParkModel());
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void openCheckoutDialog() {

        CheckoutDialog checkoutDialog = new CheckoutDialog(new OnCheckoutButtonsClicked() {
            @Override
            public void onPaymentClicked(ParkModel parkModel) {

            }

            @Override
            public void onDebtClicked(ParkModel parkModel) {

            }

            @Override
            public void onShowDebtListClicked(ParkModel parkModel) {

                startActivity(new Intent(MainActivity.this, DebtListActivity.class));

            }
        }, new ParkModel());
        checkoutDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void listeners() {

        binding.exitRequests.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ExitRequestListActivity.class)));

    }

    private void initMenuPopup() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.menu_popup_window, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupView.findViewById(R.id.exit_request).setOnClickListener(view -> Toast.makeText(getApplicationContext(), "exit_request", Toast.LENGTH_SHORT).show());
        popupView.findViewById(R.id.debt_inquiry).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DebtCheckActivity.class)));
        popupView.findViewById(R.id.car_number_charge).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CarNumberChargeActivity.class)));
        popupView.findViewById(R.id.help).setOnClickListener(view -> {

            Toast.makeText(getApplicationContext(), "help", Toast.LENGTH_SHORT).show();

//            RetrofitAPIRepository repository = new RetrofitAPIRepository();
//
//            repository.test(new Callback<TestResponse>() {
//                @Override
//                public void onResponse(Call<TestResponse> call, Response<TestResponse> response) {
//
//                    Toast.makeText(getApplicationContext(), "post title : " +  response.body().getTitle(), Toast.LENGTH_SHORT).show();
//
//
//
//                }
//
//                @Override
//                public void onFailure(Call<TestResponse> call, Throwable t) {
//
//                    Toast.makeText(getApplicationContext(), "failure", Toast.LENGTH_SHORT).show();
//
//                }
//            },1);

        });
        popupView.findViewById(R.id.about_us).setOnClickListener(view -> Toast.makeText(getApplicationContext(), "about_us", Toast.LENGTH_SHORT).show());
        popupView.findViewById(R.id.rules).setOnClickListener(view -> Toast.makeText(getApplicationContext(), "rules", Toast.LENGTH_SHORT).show());
        popupView.findViewById(R.id.logout).setOnClickListener(view -> {

            confirmDialog = new ConfirmDialog("خروج", "ایا اطمینان دارید؟", "خروج", "لغو", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {

                    SharedPreferencesRepository sh_p = new SharedPreferencesRepository(getApplicationContext());
                    sh_p.saveString(SharedPreferencesRepository.ACCESS_TOKEN, "");
                    sh_p.saveString(SharedPreferencesRepository.REFRESH_TOKEN, "");
                    MainActivity.this.finish();
                    confirmDialog.dismiss();
                }

                @Override
                public void onCancelClicked() {

                    confirmDialog.dismiss();
                }
            });

            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

        });


    }

    public void onMenuToggleClicked(View view) {

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        menuIsOpen = true;

//        popupView.setOnTouchListener((v, event) -> {
//            popupWindow.dismiss();
//            return true;
//        });
    }

    public void onExitRequestIconClicked(View view) {

        Toast.makeText(getApplicationContext(), "onExitRequestIconClicked", Toast.LENGTH_SHORT).show();
    }

    public void onBarcodeIconClicked(View view) {

        Toast.makeText(getApplicationContext(), "onBarcodeIconClicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        if (menuIsOpen) {
            menuIsOpen = false;
            popupWindow.dismiss();
        } else
            super.onBackPressed();
    }
}