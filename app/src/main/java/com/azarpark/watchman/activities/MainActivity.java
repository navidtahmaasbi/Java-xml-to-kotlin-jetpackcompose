package com.azarpark.watchman.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.print.PrintManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.ParkListAdapter;
import com.azarpark.watchman.databinding.ActivityMainBinding;
import com.azarpark.watchman.databinding.PrintTemplateBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.ParkDialog;
import com.azarpark.watchman.dialogs.ParkInfoDialog;
import com.azarpark.watchman.dialogs.PlateChargeDialog;
import com.azarpark.watchman.enums.PlaceStatus;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.models.Street;
import com.azarpark.watchman.payment.parsian.ParsianPayment;
import com.azarpark.watchman.payment.saman.SamanPayment;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.LogoutResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

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
    LoadingBar loadingBar;
    MessageDialog messageDialog;
    int exitRequestCount = 0;
    TextView watchManName;
    boolean updatePopUpIsShowed = false;
    int version = 0;
    SharedPreferencesRepository sh_r;
    PlateChargeDialog plateChargeDialog;
    boolean placesLoadedForFirstTime = true;
    Activity activity = this;
    int refresh_time = 10;
    String qr_url, telephone, pricing, sms_number, rules_url, about_us_url, guide_url;
    Timer timer;
    ParsianPayment parsianPayment;
    SamanPayment samanPayment;
    Assistant assistant;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUIForKeyboardHideOnOutsideTouch(binding.getRoot());

        assistant = new Assistant();
        parsianPayment = new ParsianPayment(getApplicationContext(),activity, new ParsianPayment.ParsianPaymentCallBack() {
            @Override
            public void verifyTransaction(String amount, String our_token, String bank_token, int placeID) {
                MainActivity.this.verifyTransaction(amount, our_token, bank_token, placeID);
            }

            @Override
            public void getScannerData(int placeID) {
                handleScannedPlaceID(placeID);
            }
        }, getSupportFragmentManager());
        samanPayment = new SamanPayment(getApplicationContext(), MainActivity.this, new SamanPayment.SamanPaymentCallBack() {
            @Override
            public void verifyTransaction(String amount, String our_token, String bank_token, int placeID) {
                MainActivity.this.verifyTransaction(amount, our_token, bank_token, placeID);
            }

            @Override
            public void getScannerData(int placeID) {
                handleScannedPlaceID(placeID);
            }
        });
        sh_r = new SharedPreferencesRepository(getApplicationContext());

        qr_url = sh_r.getString(SharedPreferencesRepository.qr_url, "");
        refresh_time = Integer.parseInt(sh_r.getString(SharedPreferencesRepository.refresh_time, "10"));
        telephone = sh_r.getString(SharedPreferencesRepository.telephone, "");
        pricing = sh_r.getString(SharedPreferencesRepository.pricing, "");
        sms_number = sh_r.getString(SharedPreferencesRepository.sms_number, "");
        rules_url = sh_r.getString(SharedPreferencesRepository.rules_url, "");
        about_us_url = sh_r.getString(SharedPreferencesRepository.about_us_url, "");
        guide_url = sh_r.getString(SharedPreferencesRepository.guide_url, "");


        System.out.println("----------> qr_url : " + qr_url);
        System.out.println("----------> refresh_time : " + refresh_time);
        System.out.println("----------> telephone : " + telephone);
        System.out.println("----------> pricing : " + pricing);
        System.out.println("----------> sms_number : " + sms_number);
        System.out.println("----------> rules_url : " + rules_url);
        System.out.println("----------> about_us_url : " + about_us_url);
        System.out.println("----------> guide_url : " + guide_url);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        loadingBar = new LoadingBar(MainActivity.this);

        initMenuPopup();

        listeners();

        binding.filterEdittext.clearFocus();

        adapter = new ParkListAdapter(place -> {

            if (place.status.equals(PlaceStatus.free.toString()) ||
                    place.status.equals(PlaceStatus.free_by_user.toString()) ||
                    place.status.equals(PlaceStatus.free_by_watchman.toString())) {

                openParkDialog(place);

            } else
                openParkInfoDialog(place);

            binding.filterEdittext.setText("");


        });
        binding.recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();


        Log.e("getPlaces", "onResume");
        getPlaces();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        parsianPayment.handleResult(requestCode, resultCode, data);

        samanPayment.handleResult(requestCode, resultCode, data);


    }

    @Override
    public void onBackPressed() {
        if (menuIsOpen) {
            menuIsOpen = false;
            popupWindow.dismiss();
        } else
            super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (samanPayment != null)
            samanPayment.releaseService();
        if (timer != null)
            timer.cancel();
        timer = null;
    }

    //-------------------------------------------------------- initialize

    private void initMenuPopup() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.menu_popup_window03, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupView.findViewById(R.id.exit_request).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ExitRequestActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.debt_inquiry).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, DebtCheckActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.car_number_charge).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CarNumberChargeActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.help).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//            intent.putExtra("url","");
            startActivity(intent);
            popupWindow.dismiss();

        });
        watchManName = popupView.findViewById(R.id.name);
        popupView.findViewById(R.id.about_us).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//            intent.putExtra("url","");
            startActivity(intent);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.rules).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//            intent.putExtra("url","");
            startActivity(intent);
            popupWindow.dismiss();

        });
        popupView.findViewById(R.id.root).setOnClickListener(view -> {

            popupWindow.dismiss();

        });
        popupView.findViewById(R.id.logout).setOnClickListener(view -> {

            confirmDialog = new ConfirmDialog("خروج", "ایا اطمینان دارید؟", "خروج", "لغو", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {

                    logout();

                }

                @Override
                public void onCancelClicked() {

                    confirmDialog.dismiss();
                }
            });

            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

            popupWindow.dismiss();

        });


    }

    private void setTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(() -> getPlaces());
                }
            }, 0, refresh_time * 1000);
        }
    }

    public void setupUIForKeyboardHideOnOutsideTouch(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    assistant.hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUIForKeyboardHideOnOutsideTouch(innerView);
            }
        }
    }

    //-------------------------------------------------------- listeners

    private void listeners() {

        binding.refresh.setOnClickListener(view -> getPlaces());

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

    public void onMenuToggleClicked(View view) {

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        menuIsOpen = true;

    }

    public void onExitRequestIconClicked(View view) {

        if (exitRequestCount > 0) {

            adapter.showExitRequestItems(!adapter.isShowExitRequestItems());


            if (adapter.isShowExitRequestItems())
                binding.exitRequests.setBackgroundColor(getResources().getColor(R.color.red));
            else
                binding.exitRequests.setBackgroundColor(getResources().getColor(R.color.transparent));

        } else
            Toast.makeText(getApplicationContext(), "درخواست خروج ندارید", Toast.LENGTH_SHORT).show();
    }

    public void onBarcodeIconClicked(View view) {

        if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN) {

            startActivityForResult(new Intent(MainActivity.this, QRScanerActivity.class), ParsianPayment.QR_SCANER_REQUEST_CODE);

        } else {

            Intent intent = new Intent();
            intent.setComponent(new ComponentName("ir.sep.android.smartpos",
                    "ir.sep.android.smartpos.ScannerActivity"));
            startActivityForResult(intent, 2);
        }
    }

    public void handleScannedPlaceID(int placeID) {

        Place place = adapter.getItemWithID(placeID);
        if (place != null)
            openParkInfoDialog(place);
        else {

            confirmDialog = new ConfirmDialog("درخواست خروج", " آیا برای درخواست خروج اطمینان دارید؟", "بله", "خیر", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {

                    exitPark(placeID);

                }

                @Override
                public void onCancelClicked() {

                    confirmDialog.dismiss();

                }
            });

            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

        }

    }

    //-------------------------------------------------------- dialogs

    private void openParkDialog(Place place) {

        parkDialog = new ParkDialog(this::parkCar, place);
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);

        assistant.showSoftKeyboard(MainActivity.this);

    }

    private void openParkInfoDialog(Place place) {

        parkInfoDialog = new ParkInfoDialog(new OnGetInfoClicked() {
            @Override
            public void pay(int price, Place place) {

                PlateType selectedPlateType = PlateType.simple;

                if (place.tag2 == null || place.tag2.isEmpty())
                    selectedPlateType = PlateType.old_aras;
                else if (place.tag3 == null || place.tag3.isEmpty())
                    selectedPlateType = PlateType.new_aras;

                long res_num = Assistant.generateResNum();

                if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
                    parsianPayment.paymentRequest(price, res_num, MainActivity.this, selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, place.id);
                else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
                    samanPayment.paymentRequest(UUID.randomUUID().toString(), price, selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, place.id);

            }

            @Override
            public void payAsDebt(Place place) {

                exitPark(place.id);

            }

            @Override
            public void removeExitRequest(Place place1) {

                deleteExitRequest(place1.id);

            }

            @Override
            public void charge(PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

                parkInfoDialog.dismiss();

                plateChargeDialog = new PlateChargeDialog(amount -> {

                    System.out.println("---------> PlateChargeDialog : " + amount);

                    if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
                        parsianPayment.createTransaction(plateType, tag1, tag2, tag3, tag4, amount, -1);
                    else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
                        samanPayment.createTransaction(plateType, tag1, tag2, tag3, tag4,amount, -1);

                    plateChargeDialog.dismiss();

                }, place);

                plateChargeDialog.show(getSupportFragmentManager(), PlateChargeDialog.TAG);

            }

            @Override
            public void print(String startTime, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {


                PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), binding.printArea, true);

                printTemplateBinding.placeId.setText(placeID + "");

//                String time = startTime.split(" ")[]

                printTemplateBinding.startTime.setText(startTime);
                printTemplateBinding.prices.setText(pricing);
                printTemplateBinding.supportPhone.setText(telephone);
                printTemplateBinding.description.setText("در صورت عدم حضور پارکیار عدد " + placeID + " را به شماره " + sms_number + " ارسال کنید");

                printTemplateBinding.qrcode.setImageBitmap(assistant.qrGenerator(qr_url + placeID));

                if (place.tag4 != null && !place.tag4.isEmpty()) {

                    printTemplateBinding.plateSimpleArea.setVisibility(View.VISIBLE);
                    printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
                    printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);

                    printTemplateBinding.plateSimpleTag1.setText(place.tag1);
                    printTemplateBinding.plateSimpleTag2.setText(place.tag2);
                    printTemplateBinding.plateSimpleTag3.setText(place.tag3);
                    printTemplateBinding.plateSimpleTag4.setText(place.tag4);

                } else if (place.tag2 == null || place.tag2.isEmpty()) {

                    printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
                    printTemplateBinding.plateOldArasArea.setVisibility(View.VISIBLE);
                    printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);

                    printTemplateBinding.plateOldArasTag1En.setText(place.tag1);
                    printTemplateBinding.plateOldArasTag1Fa.setText(place.tag1);

                } else {

                    printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
                    printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
                    printTemplateBinding.plateNewArasArea.setVisibility(View.VISIBLE);

                    printTemplateBinding.plateNewArasTag1En.setText(place.tag1);
                    printTemplateBinding.plateNewArasTag1Fa.setText(place.tag1);
                    printTemplateBinding.plateNewArasTag2En.setText(place.tag2);
                    printTemplateBinding.plateNewArasTag2Fa.setText(place.tag2);

                }

                new Handler().postDelayed(() -> {

                    if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
                        parsianPayment.printParkInfo(startTime, place.tag1, place.tag2, place.tag3, place.tag4, place.id, binding.printArea, pricing, telephone, sms_number, qr_url);
                    else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
                        samanPayment.printParkInfo(startTime, place.tag1, place.tag2, place.tag3, place.tag4, place.id, binding.aaabbb, pricing, telephone, sms_number, qr_url);

//                    printParkInfo(startTime, tag1, tag2, tag3, tag4, placeID, binding.printArea, pricing, telephone, sms_number, qr_url);

                }, 500);

            }
        }, place);
        parkInfoDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    //-------------------------------------------------------- API calls

    private void getPlaces() {


//        Log.e("getPlaces", "sending ... ");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        if (placesLoadedForFirstTime)
            loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

//                Log.e("getPlaces", "Response : " + response.code());

                if (placesLoadedForFirstTime)
                    loadingBar.dismiss();
                if (response.isSuccessful()) {

                    placesLoadedForFirstTime = false;

//                    qr_url = response.body().qr_url;
//                    refresh_time = response.body().refresh_time;
//                    telephone = response.body().telephone;
//                    pricing = response.body().pricing;
//                    sms_number = response.body().sms_number;

                    if (!updatePopUpIsShowed && version != 0 && response.body().update.last_version > version) {

                        updatePopUpIsShowed = true;

                        if (response.body().update.is_forced == 1) {

                            messageDialog = new MessageDialog("به روز رسانی", "به روز رسانی اجباری برای آذرپارک موجود است.", "به روز رسانی", () -> {

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().update.update_link));
                                startActivity(browserIntent);

                            });

                        }

                    } else {

                        if (watchManName != null)
                            watchManName.setText(response.body().watchman.name);

                        exitRequestCount = 0;

                        ArrayList<Place> myPlaces = new ArrayList<>();

                        for (Street street : response.body().watchman.streets) {

                            myPlaces.addAll(street.places);
                            for (Place place : street.places)
                                if (place.exit_request != null)
                                    exitRequestCount++;

                        }

                        adapter.setItems(myPlaces);

                        binding.placeholder.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

                        binding.exitRequestCount.setText(Integer.toString(exitRequestCount));

                    }
                    setTimer();

                } else
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> getPlaces());

            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                if (placesLoadedForFirstTime)
                    loadingBar.dismiss();

                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> getPlaces());


            }
        });

    }

    private void parkCar(ParkBody parkBody, boolean printFactor) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.park("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), parkBody, new Callback<ParkResponse>() {
            @Override
            public void onResponse(Call<ParkResponse> call, Response<ParkResponse> response) {

                loadingBar.dismiss();
                if (response.isSuccessful()) {

                    if (response.body().getSuccess() == 1) {

                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();
                        parkDialog.dismiss();

//                        PersianDateFormat pdformater = new PersianDateFormat();
//                        pdformater.format(pdate);

                        if (printFactor)
                            if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
                                parsianPayment.printParkInfo("19:20", parkBody.getTag1(), parkBody.getTag2(), parkBody.getTag3(), parkBody.getTag4(), parkBody.getPlace_id(), binding.printArea, pricing, telephone, sms_number, qr_url);
                            else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
                                samanPayment.printParkInfo("19:20", parkBody.getTag1(), parkBody.getTag2(), parkBody.getTag3(), parkBody.getTag4(), parkBody.getPlace_id(), binding.aaabbb, pricing, telephone, sms_number, qr_url);

                        getPlaces();

                    } else
                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                } else
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> parkCar(parkBody,printFactor));
            }

            @Override
            public void onFailure(Call<ParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> parkCar(parkBody,printFactor));
            }
        });

    }

    private void exitPark(int placeID) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.exitPark("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), placeID, new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {


                loadingBar.dismiss();
                if (response.isSuccessful()) {

                    if (response.body().getSuccess() == 1) {

                        if (parkInfoDialog != null)
                            parkInfoDialog.dismiss();
                        getPlaces();

                    }
                    Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_LONG).show();


                } else
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> exitPark(placeID));
            }

            @Override
            public void onFailure(Call<ExitParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> exitPark(placeID));
            }
        });

    }

    private void deleteExitRequest(int place_id) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        repository.deleteExitRequest("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                place_id, new Callback<DeleteExitRequestResponse>() {
                    @Override
                    public void onResponse(Call<DeleteExitRequestResponse> call, Response<DeleteExitRequestResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            getPlaces();
                            if (parkInfoDialog != null)
                                parkInfoDialog.dismiss();

                        } else
                            APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> deleteExitRequest(place_id));
                    }

                    @Override
                    public void onFailure(Call<DeleteExitRequestResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> deleteExitRequest(place_id));
                    }
                });

    }

    private void verifyTransaction(String amount, String our_token, String bank_token, int placeID) {

        Log.d("verifyTransaction", "started ...");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        amount = Integer.toString((Integer.parseInt(amount) / 10));

        String finalAmount = amount;
        repository.verifyTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                amount, our_token, bank_token, placeID, new Callback<VerifyTransactionResponse>() {
                    @Override
                    public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            if (parkInfoDialog != null)
                                parkInfoDialog.dismiss();

                            Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                            getPlaces();


                        } else
                            APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> verifyTransaction(finalAmount, our_token, bank_token, placeID));
                    }

                    @Override
                    public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        t.printStackTrace();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> verifyTransaction(finalAmount, our_token, bank_token, placeID));
                    }
                });

    }

    private void logout() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        repository.logout("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {


                loadingBar.dismiss();
                if (response.isSuccessful()) {

                    SharedPreferencesRepository sh_p = new SharedPreferencesRepository(getApplicationContext());
                    sh_p.saveString(SharedPreferencesRepository.ACCESS_TOKEN, "");
                    sh_p.saveString(SharedPreferencesRepository.REFRESH_TOKEN, "");
                    sh_p.saveString(SharedPreferencesRepository.SUB_DOMAIN, "");
                    startActivity(new Intent(MainActivity.this, SplashActivity.class));
                    MainActivity.this.finish();
                    confirmDialog.dismiss();

                } else
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> logout());
            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                loadingBar.dismiss();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> logout());
            }
        });

    }

    public void printParkInfo(String startTime, String tag1, String tag2, String tag3, String tag4,
                              int placeID, ViewGroup viewGroupForBindFactor, String pricing, String telephone, String sms_number,
                              String qr_url) {


        PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), viewGroupForBindFactor, true);

        printTemplateBinding.placeId.setText(placeID + "");

//                String time = startTime.split(" ")[]

        printTemplateBinding.startTime.setText(startTime);
        printTemplateBinding.prices.setText(pricing);
        printTemplateBinding.supportPhone.setText(telephone);
        printTemplateBinding.description.setText("در صورت عدم حضور پارکیار عدد " + placeID + " را به شماره " + sms_number + " ارسال کنید");

        printTemplateBinding.qrcode.setImageBitmap(assistant.qrGenerator(qr_url + placeID));

        if (tag4 != null && !tag4.isEmpty()) {

            printTemplateBinding.plateSimpleArea.setVisibility(View.VISIBLE);
            printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
            printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);

            printTemplateBinding.plateSimpleTag1.setText(tag1);
            printTemplateBinding.plateSimpleTag2.setText(tag2);
            printTemplateBinding.plateSimpleTag3.setText(tag3);
            printTemplateBinding.plateSimpleTag4.setText(tag4);

        } else if (tag2 == null || tag2.isEmpty()) {

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

        try {

            samanPayment.connection.print(getViewBitmap(binding.printArea));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static Bitmap getViewBitmap(View view) {

        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;

//        v.clearFocus();
//        v.setPressed(false);
//
//        boolean willNotCache = v.willNotCacheDrawing();
//        v.setWillNotCacheDrawing(false);
//
//        // Reset the drawing cache background color to fully transparent
//        // for the duration of this operation
//        int color = v.getDrawingCacheBackgroundColor();
//        v.setDrawingCacheBackgroundColor(0);
//
//        if (color != 0) {
//            v.destroyDrawingCache();
//        }
//        v.buildDrawingCache();
//        Bitmap cacheBitmap = v.getDrawingCache();
//        if (cacheBitmap == null) {
//            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
//            return null;
//        }
//
//        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);
//
//        // Restore the view
//        v.destroyDrawingCache();
//        v.setWillNotCacheDrawing(willNotCache);
//        v.setDrawingCacheBackgroundColor(color);
//
//        return bitmap;
    }


}
