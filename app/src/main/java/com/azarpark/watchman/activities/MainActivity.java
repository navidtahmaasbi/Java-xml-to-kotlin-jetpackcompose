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
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.google.zxing.WriterException;

import java.net.HttpURLConnection;
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
    boolean placesLoadedForFirstTime = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(binding.getRoot());

        sh_r = new SharedPreferencesRepository(getApplicationContext());

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

        getPlaces(!placesLoadedForFirstTime);

    }

    private void getPlaces(boolean showLoadingBar) {

        System.out.println("----------> getPlaces");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        if (showLoadingBar)
            loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

                if (showLoadingBar)
                    loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    placesLoadedForFirstTime = true;

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
                        //todo set placeholder to empty street or places list
                        for (Street street : response.body().watchman.streets) {

                            adapter.setItems(street.places);
                            for (Place place : street.places)
                                if (place.exit_request != null)
                                    exitRequestCount++;

                        }

                        binding.exitRequestCount.setText(Integer.toString(exitRequestCount));

                        new Handler().postDelayed(() -> {
                            getPlaces(false);
                        }, 5000);

                    }

                } else {

                    System.out.println("---------> msg : " + response.raw().toString());

                    Toast.makeText(getApplicationContext(), "error : " + response.code(), Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                if (showLoadingBar)
                    loadingBar.dismiss();


                confirmDialog = new ConfirmDialog(
                    getResources().getString(R.string.retry_title),
                    getResources().getString(R.string.retry_text),
                    getResources().getString(R.string.retry_confirm_button),
                    getResources().getString(R.string.retry_cancel_button),
                    new ConfirmDialog.ConfirmButtonClicks() {
                        @Override
                        public void onConfirmClicked() {
                            confirmDialog.dismiss();
                            getPlaces(showLoadingBar);
                        }

                        @Override
                        public void onCancelClicked() {
                            confirmDialog.dismiss();
                        }
                    }
                );

                if (!confirmDialog.isAdded())
                    confirmDialog.show(getSupportFragmentManager(), "tag");
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
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1) {

                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();
                        parkDialog.dismiss();
                        getPlaces(true);
                    } else if (response.body().getSuccess() == 0) {

                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

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

                exitPark(place);

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

                System.out.println("----------> 2222");

                PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()));

                printTemplateBinding.startTime.setText(startTime);
                printTemplateBinding.description.setText("در صورت عدم حضور پارکیار عدد " + placeID + " را به شماره ۱۰۰۰۴۴۰۸۸ ارسال کنید");

                printTemplateBinding.qrcode.setImageBitmap(QRGenerator("12345"));

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

    private void exitPark(Place place) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.exitPark("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), place.id, new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {


                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1) {

                        parkInfoDialog.dismiss();
                        getPlaces(true);
                    } else if (response.body().getSuccess() == 0) {

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

        binding.refresh.setOnClickListener(view -> getPlaces(true));

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
                    if (response.code() == HttpURLConnection.HTTP_OK) {

                        getPlaces(true);
                        if (parkInfoDialog != null)
                            parkInfoDialog.dismiss();

                    } else {

                        Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                    }
                }

                @Override
                public void onFailure(Call<DeleteExitRequestResponse> call, Throwable t) {
                    loadingBar.dismiss();
                    Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
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

                System.out.println("--------> url : " + response.raw().request().url());

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    SharedPreferencesRepository sh_p = new SharedPreferencesRepository(getApplicationContext());
                    sh_p.saveString(SharedPreferencesRepository.ACCESS_TOKEN, "");
                    sh_p.saveString(SharedPreferencesRepository.REFRESH_TOKEN, "");
                    MainActivity.this.finish();
                    confirmDialog.dismiss();

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
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

        if (exitRequestCount > 0)
            adapter.showExitRequestItems(!adapter.isShowExitRequestItems());
        else
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

                        if (response.body().getSuccess() == 1)
                            parkInfoDialog.dismiss();

                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                        getPlaces(true);


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
