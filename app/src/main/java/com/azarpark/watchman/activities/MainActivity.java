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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.azarpark.watchman.payment.MyServiceConnection;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.LogoutResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.google.zxing.WriterException;

import java.net.HttpURLConnection;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import ir.sep.android.Service.IProxy;
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
    MyServiceConnection connection;
    IProxy service;
    SharedPreferencesRepository sh_r;
    PlateChargeDialog plateChargeDialog;
    boolean placesLoadedForFirstTime = true;
    Activity activity = this;
    int refresh_time = 10;
    String qr_url, telephone, pricing, sms_number, rules_url, about_us_url, guide_url;
    Timer timer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(binding.getRoot());

        sh_r = new SharedPreferencesRepository(getApplicationContext());

//        qr_url = sh_r.getString(SharedPreferencesRepository.qr_url, "");
//        refresh_time = Integer.parseInt(sh_r.getString(SharedPreferencesRepository.refresh_time, "10"));
//        telephone = sh_r.getString(SharedPreferencesRepository.telephone, "");
//        pricing = sh_r.getString(SharedPreferencesRepository.pricing, "");
//        sms_number = sh_r.getString(SharedPreferencesRepository.sms_number, "");
//        rules_url = sh_r.getString(SharedPreferencesRepository.rules_url, "");
//        about_us_url = sh_r.getString(SharedPreferencesRepository.about_us_url, "");
//        guide_url = sh_r.getString(SharedPreferencesRepository.guide_url, "");

        initService();

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



    private void setTimer(){
        if (timer == null){
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(() -> getPlaces());
                }
            }, 0, refresh_time * 1000);
        }
    }

    private void getPlaces() {


        Log.e("getPlaces", "sending ... ");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        if (placesLoadedForFirstTime)
            loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

                Log.e("getPlaces", "Response : " + response.code());

                if (placesLoadedForFirstTime)
                    loadingBar.dismiss();
                if (response.isSuccessful()) {

                    placesLoadedForFirstTime = false;

                    qr_url = response.body().qr_url;
                    refresh_time = response.body().refresh_time;
                    telephone = response.body().telephone;
                    pricing = response.body().pricing;
                    sms_number = response.body().sms_number;

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

    private void openParkDialog(Place place) {

        parkDialog = new ParkDialog(this::parkCar, place);
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void parkCar(ParkBody parkBody) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
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
                        getPlaces();

                    } else
                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                } else
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> parkCar(parkBody));
            }

            @Override
            public void onFailure(Call<ParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> parkCar(parkBody));
            }
        });

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

                paymentRequest(price, selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, place.id);

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

                    paymentRequest(amount, plateType, tag1, tag2, tag3, tag4, -1);
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

                printTemplateBinding.qrcode.setImageBitmap(QRGenerator(qr_url + placeID));

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

                    connection.print(getViewBitmap(printTemplateBinding.getRoot()));

                }, 1000);

            }
        }, place);
        parkInfoDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    public Bitmap QRGenerator(String value) {

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(value, null, QRGContents.Type.TEXT, 512);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        Bitmap bitmap = qrgEncoder.getBitmap();

        return bitmap;


    }

    private void exitPark(int placeID) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.exitPark("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), placeID, new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {


                loadingBar.dismiss();
                if (response.isSuccessful()) {

                    if (response.body().getSuccess() == 1) {

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

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

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

    private void deleteExitRequest(int place_id) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
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



    private void logout() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
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

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("ir.sep.android.smartpos",
                "ir.sep.android.smartpos.ScannerActivity"));
        startActivityForResult(intent, 2);
    }

    @Override
    public void onBackPressed() {
        if (menuIsOpen) {
            menuIsOpen = false;
            popupWindow.dismiss();
        } else
            super.onBackPressed();
    }

    //---------------------------------------------------------------

    private void initService() {

        Log.i("TAG", "initService()");
        connection = new MyServiceConnection(service);
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
        if (timer != null)
            timer.cancel();
        timer = null;
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

    /**
     * Draw the view into a bitmap.
     */
    public static Bitmap getViewBitmap(View v) {
        v.clearFocus();
        v.setPressed(false);

        boolean willNotCache = v.willNotCacheDrawing();
        v.setWillNotCacheDrawing(false);

        // Reset the drawing cache background color to fully transparent
        // for the duration of this operation
        int color = v.getDrawingCacheBackgroundColor();
        v.setDrawingCacheBackgroundColor(0);

        if (color != 0) {
            v.destroyDrawingCache();
        }
        v.buildDrawingCache();
        Bitmap cacheBitmap = v.getDrawingCache();
        if (cacheBitmap == null) {
            Log.e(TAG, "failed getViewBitmap(" + v + ")", new RuntimeException());
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(cacheBitmap);

        // Restore the view
        v.destroyDrawingCache();
        v.setWillNotCacheDrawing(willNotCache);
        v.setDrawingCacheBackgroundColor(color);

        return bitmap;
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
                Toast.makeText(getBaseContext(), "Purchase did failed....", Toast.LENGTH_LONG).show();

        } else if (resultCode == RESULT_OK && requestCode == 2) {

            String url = data.getStringExtra("ScannerResult");
            int placeId = Integer.parseInt(url.split("=")[url.split("=").length - 1]);

            Place place = adapter.getItemWithID(placeId);

            if (place != null)
                openParkInfoDialog(place);
            else {

                confirmDialog = new ConfirmDialog("درخواست خروج", " آیا برای درخواست خروج اطمینان دارید؟", "بله", "خیر", new ConfirmDialog.ConfirmButtonClicks() {
                    @Override
                    public void onConfirmClicked() {

                        exitPark(placeId);

                    }

                    @Override
                    public void onCancelClicked() {

                        confirmDialog.dismiss();

                    }
                });

                confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

            }

            System.out.println("---------> ScannerResult : " + url);//https://irana.app/how?qr=090YK6
            System.out.println("---------> placeId : " + placeId);
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

        String finalAmount = amount;
        repository.verifyTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, amount, transaction_id, placeID, new Callback<VerifyTransactionResponse>() {
                    @Override
                    public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            if (response.body().getSuccess() == 1)
                                parkInfoDialog.dismiss();

                            Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                            getPlaces();


                        } else
                            APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> verifyTransaction(plateType, tag1, tag2, tag3, tag4, finalAmount, transaction_id, placeID));
                    }

                    @Override
                    public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> verifyTransaction(plateType, tag1, tag2, tag3, tag4, finalAmount, transaction_id, placeID));
                    }
                });

    }


}
