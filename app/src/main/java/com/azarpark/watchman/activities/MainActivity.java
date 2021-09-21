package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.ParkListAdapter;
import com.azarpark.watchman.databinding.ActivityMainBinding;
import com.azarpark.watchman.dialogs.CheckoutDialog;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.ParkDialog;
import com.azarpark.watchman.dialogs.ParkInfoDialog;
import com.azarpark.watchman.enums.PlaceStatus;
import com.azarpark.watchman.interfaces.OnCheckoutButtonsClicked;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.ParkModel;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.models.Street;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.google.gson.Gson;

import java.net.HttpURLConnection;

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
    ParkDialog parkDialog;
    ParkInfoDialog parkInfoDialog;


        @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        initMenuPopup();

        listeners();

        adapter = new ParkListAdapter(place -> {

            if(place.status.equals(PlaceStatus.free.toString()) || 
                    place.status.equals(PlaceStatus.free_by_user.toString()) || 
                    place.status.equals(PlaceStatus.free_by_watchman.toString())){

                openParkDialog(place);

            }
            else
                getParkData(place);
            

        });
        binding.recyclerView.setAdapter(adapter);

        getPlaces();

    }

    private void getParkData(Place place) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this,MainActivity.this);
        loadingBar.show();

        repository.estimateParkPrice("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),place.id, new Callback<EstimateParkPriceResponse>() {
            @Override
            public void onResponse(Call<EstimateParkPriceResponse> call, Response<EstimateParkPriceResponse> response) {

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1){

//                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        openParkInfoDialog(place,response.body());
                    }else if (response.body().getSuccess() == 0){

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<EstimateParkPriceResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getPlaces() {

        System.out.println("---------> getPlaces");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this,MainActivity.this);
        loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

                System.out.println("---------> response");
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
                System.out.println("---------> onFailure");
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openParkDialog(Place place) {

         parkDialog = new ParkDialog(this::parkCar, place);
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void parkCar(ParkBody parkBody) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this,MainActivity.this);
        loadingBar.show();

        repository.park("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),parkBody, new Callback<ParkResponse>() {
            @Override
            public void onResponse(Call<ParkResponse> call, Response<ParkResponse> response) {

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1){

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        parkDialog.dismiss();
                        getPlaces();
                    }else if (response.body().getSuccess() == 0){

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openParkInfoDialog(Place place, EstimateParkPriceResponse parkPriceResponse) {

        parkInfoDialog = new ParkInfoDialog(new OnGetInfoClicked() {
            @Override
            public void pay(int price, int placeID) {

                Toast.makeText(getApplicationContext(), "go to payment app and then refresh list then open this dialog again", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void payAsDebt(Place place) {

                exitPark(place);

            }
        }, place, parkPriceResponse);
        parkInfoDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void exitPark(Place place) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this,MainActivity.this);
        loadingBar.show();

        repository.exitPark("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),place.id, new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {


                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1){

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        parkInfoDialog.dismiss();
                        getPlaces();
                    }else if (response.body().getSuccess() == 0){

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ExitParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void openCheckoutDialog(Place place) {

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

        binding.filterEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                adapter.filterItems(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void initMenuPopup() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.menu_popup_window02, null);
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

        ((TextView)popupView.findViewById(R.id.text)).setText(Html.fromHtml(getResources().getString(R.string.lorem)));


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