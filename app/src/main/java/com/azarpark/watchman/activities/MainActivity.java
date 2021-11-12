package com.azarpark.watchman.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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
import com.azarpark.watchman.databinding.SamanAfterPaymentPrintTemplateBinding;
import com.azarpark.watchman.databinding.SamanPrintTemplateBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.ParkDialog;
import com.azarpark.watchman.dialogs.ParkInfoDialog;
import com.azarpark.watchman.dialogs.ParkResponseDialog;
import com.azarpark.watchman.dialogs.PlateChargeDialog;
import com.azarpark.watchman.download_utils.DownloadController;
import com.azarpark.watchman.enums.PlaceStatus;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.interfaces.OnSubmitClicked;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.models.Street;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.parsian.ParsianPayment;
import com.azarpark.watchman.payment.saman.SamanPayment;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.LogoutResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.google.gson.Gson;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    int debt = 0;
    ParkResponseDialog parkResponseDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
//        setupUIForKeyboardHideOnOutsideTouch(binding.getRoot());

        sh_r = new SharedPreferencesRepository(getApplicationContext());

        verifyUnverifiedTransactions();

        assistant = new Assistant();
        parsianPayment = new ParsianPayment(getApplicationContext(), activity, new ParsianPayment.ParsianPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {
                MainActivity.this.verifyTransaction(transaction);
            }

            @Override
            public void getScannerData(int placeID) {
                handleScannedPlaceID(placeID);
            }
        }, getSupportFragmentManager());
        samanPayment = new SamanPayment(getApplicationContext(), MainActivity.this, new SamanPayment.SamanPaymentCallBack() {
            @Override
            public void verifyTransaction(Transaction transaction) {
                MainActivity.this.verifyTransaction(transaction);
            }

            @Override
            public void getScannerData(int placeID) {
                handleScannedPlaceID(placeID);
            }
        });

        qr_url = sh_r.getString(SharedPreferencesRepository.qr_url, "");
        refresh_time = Integer.parseInt(sh_r.getString(SharedPreferencesRepository.refresh_time, "10"));
        telephone = sh_r.getString(SharedPreferencesRepository.telephone, "");
        pricing = sh_r.getString(SharedPreferencesRepository.pricing, "");
        sms_number = sh_r.getString(SharedPreferencesRepository.sms_number, "");
        rules_url = sh_r.getString(SharedPreferencesRepository.rules_url, "");
        about_us_url = sh_r.getString(SharedPreferencesRepository.about_us_url, "");
        guide_url = sh_r.getString(SharedPreferencesRepository.guide_url, "");

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

        adapter = new ParkListAdapter(getApplicationContext(), place -> {

            if (place.status.contains(PlaceStatus.free.toString())) {

                openParkDialog(place);

            } else {

                openParkInfoDialog(place);
            }

            binding.filterEdittext.setText("");


        });
        binding.recyclerView.setAdapter(adapter);

    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.e("getPlaces", "onResume");
        getPlaces();
        setTimer();
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
        }
        else if (adapter.isShowExitRequestItems()) {

            binding.filterEdittext.setText("");
            adapter.showExitRequestItems(!adapter.isShowExitRequestItems());
            binding.exitRequests.setBackgroundColor(getResources().getColor(R.color.transparent));

        } else {
            startActivity(new Intent(this, StarterActivity.class));
            MainActivity.this.finish();
        }
//            super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (samanPayment != null)
            samanPayment.releaseService();

        if (timer != null) {

            timer.cancel();
            timer = null;
        }

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
            intent.putExtra("url", sh_r.getString(SharedPreferencesRepository.guide_url));
            startActivity(intent);
            popupWindow.dismiss();

        });
        watchManName = popupView.findViewById(R.id.name);
        popupView.findViewById(R.id.about_us).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra("url", sh_r.getString(SharedPreferencesRepository.about_us_url));
            startActivity(intent);
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.rules).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra("url", sh_r.getString(SharedPreferencesRepository.rules_url));
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
                    MainActivity.this.runOnUiThread(() -> {
                        getPlaces();
                        verifyUnverifiedTransactions();
                    });
                }
            }, 0, refresh_time * 1000);
        }
    }

    private void verifyUnverifiedTransactions() {

        for (Transaction transaction : sh_r.getTransactions())
            verifyUnverifiedTransaction(transaction);

    }

    //-------------------------------------------------------- listeners

    private void listeners() {

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

        binding.refreshLayout.setOnRefreshListener(() -> {

            getPlaces();

        });

    }

    public void onMenuToggleClicked(View view) {


        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        menuIsOpen = true;

    }

    public void onExitRequestIconClicked(View view) {

//        if (exitRequestCount > 0) {

        adapter.showExitRequestItems(!adapter.isShowExitRequestItems());
        binding.filterEdittext.setText("");
        if (adapter.isShowExitRequestItems())
            binding.exitRequests.setBackgroundColor(getResources().getColor(R.color.red));
        else
            binding.exitRequests.setBackgroundColor(getResources().getColor(R.color.transparent));

//        } else
//            Toast.makeText(getApplicationContext(), "درخواست خروج ندارید", Toast.LENGTH_SHORT).show();
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
        if (place != null && (place.status.equals("full_by_user") || place.status.equals("full_by_watchman") || place.status.equals("full")))
            openParkInfoDialog(place);
        else {

            Toast.makeText(getApplicationContext(), "جایگاه خالی شده است", Toast.LENGTH_SHORT).show();

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

                assistant.saveTags(place.tag1,place.tag2,place.tag3,place.tag4, getApplicationContext());

                if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
                    parsianPayment.createTransaction(selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, price, place.id);
                else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
                    samanPayment.createTransaction(Assistant.NON_CHARGE_SHABA, selectedPlateType, place.tag1, place.tag2, place.tag3, place.tag4, price, place.id);

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


                    if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN)
                        parsianPayment.createTransaction(plateType, tag1, tag2, tag3, tag4, amount, -1);
                    else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN)
                        samanPayment.createTransaction(Assistant.CHARGE_SHABA, plateType, tag1, tag2, tag3, tag4, amount, -1);

                    plateChargeDialog.dismiss();

                }, place);

                plateChargeDialog.show(getSupportFragmentManager(), PlateChargeDialog.TAG);

            }

            @Override
            public void print(String startTime, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID, int debt, int balance) {

                printFactor(placeID, startTime, balance, place);

            }
        }, place);
        parkInfoDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    @SuppressLint("SetTextI18n")
    private void printFactor(int placeID, String startTime, int balance, Place place) {

        if (Assistant.SELECTED_PAYMENT == Assistant.PASRIAN) {

//            binding.printArea.removeAllViews();
//
//            SamanPrintTemplateBinding printTemplateBinding = SamanPrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), binding.printArea, true);
//
//            printTemplateBinding.placeId.setText(place.number + "");
//
//            printTemplateBinding.startTime.setText(assistant.toJalali(startTime));
//            printTemplateBinding.prices.setText(pricing);
//            printTemplateBinding.supportPhone.setText(telephone);
//            printTemplateBinding.debt.setText((-1 * balance) + "تومان");
//            String cityID = sh_r.getString(SharedPreferencesRepository.CITY_ID);
//
//            String sb = "در صورت عدم حضور پارکیار برای خروج عدد " +
//                    cityID +
//                    place.number +
//                    " را به شماره " +
//                    sms_number +
//                    " ارسال کنید" +
//                    "\n ." +
//                    "\n .";
//            printTemplateBinding.description.setText(sb);
//            if (balance > 0)
//                printTemplateBinding.description2.setText("شهروند گرامی؛از این که جز مشتریان خوش حساب ما هستید سپاسگزاریم");
//            else if (balance < 0)
//                printTemplateBinding.description2.setText("اخطار: شهروند گرامی؛بدهی پلاک شما بیش از حد مجاز میباشد در صورت عدم پرداخت بدهی مشمول جریمه پارک ممنوع خواهید شد");
//            else
//                printTemplateBinding.description2.setText("شهروند گرامی در صورت عدم پرداخت هزینه پارک مشمول جریمه پارک ممنوع خواهید شد");
//
//            printTemplateBinding.debtArea.setVisibility(balance < 0 ? View.VISIBLE : View.GONE);
//
//            printTemplateBinding.qrcode.setImageBitmap(assistant.qrGenerator(qr_url + placeID));
//
//            Gson gson = new Gson();
//
//            System.out.println("---------> placeee : " + gson.toJson(place));
//
//            if (assistant.getPlateType(place) == PlateType.simple) {
//
//                printTemplateBinding.plateSimpleArea.setVisibility(View.VISIBLE);
//                printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
//                printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);
//
//                printTemplateBinding.plateSimpleTag1.setText(place.tag1);
//                printTemplateBinding.plateSimpleTag2.setText(place.tag2);
//                printTemplateBinding.plateSimpleTag3.setText(place.tag3);
//                printTemplateBinding.plateSimpleTag4.setText(place.tag4);
//
//            } else if (assistant.getPlateType(place) == PlateType.old_aras) {
//
//                printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
//                printTemplateBinding.plateOldArasArea.setVisibility(View.VISIBLE);
//                printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);
//
//                printTemplateBinding.plateOldArasTag1En.setText(place.tag1);
//                printTemplateBinding.plateOldArasTag1Fa.setText(place.tag1);
//
//            } else {
//
//                printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
//                printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
//                printTemplateBinding.plateNewArasArea.setVisibility(View.VISIBLE);
//
//                printTemplateBinding.plateNewArasTag1En.setText(place.tag1);
//                printTemplateBinding.plateNewArasTag1Fa.setText(place.tag1);
//                printTemplateBinding.plateNewArasTag2En.setText(place.tag2);
//                printTemplateBinding.plateNewArasTag2Fa.setText(place.tag2);
//
//            }

            binding.printArea.removeAllViews();

            new Handler().postDelayed(() -> {

                parsianPayment.printParkInfo(place, place.id, binding.printArea, pricing, telephone, sms_number, qr_url, balance);

            }, 500);


        } else if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN) {

            binding.printArea.removeAllViews();

            SamanPrintTemplateBinding printTemplateBinding = SamanPrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), binding.printArea, true);

            printTemplateBinding.placeId.setText(place.number + "");

            printTemplateBinding.startTime.setText(assistant.toJalali(startTime));
            printTemplateBinding.prices.setText(pricing);
            printTemplateBinding.supportPhone.setText(telephone);
            printTemplateBinding.debt.setText((-1 * balance) + "تومان");
            String cityID = sh_r.getString(SharedPreferencesRepository.CITY_ID);

            String sb = "در صورت عدم حضور پارکیار برای خروج عدد " +
                    cityID +
                    place.number +
                    " را به شماره " +
                    sms_number +
                    " ارسال کنید" +
                    "\n ." +
                    "\n .";
            printTemplateBinding.description.setText(sb);
            if (balance > 0)
                printTemplateBinding.description2.setText("شهروند گرامی؛از این که جز مشتریان خوش حساب ما هستید سپاسگزاریم");
            else if (balance < 0)
                printTemplateBinding.description2.setText("اخطار: شهروند گرامی؛بدهی پلاک شما بیش از حد مجاز میباشد در صورت عدم پرداخت بدهی مشمول جریمه پارک ممنوع خواهید شد");
            else
                printTemplateBinding.description2.setText("شهروند گرامی در صورت عدم پرداخت هزینه پارک مشمول جریمه پارک ممنوع خواهید شد");

            printTemplateBinding.debtArea.setVisibility(balance < 0 ? View.VISIBLE : View.GONE);

            printTemplateBinding.qrcode.setImageBitmap(assistant.qrGenerator(qr_url + placeID));

            Gson gson = new Gson();

            System.out.println("---------> placeee : " + gson.toJson(place));

            if (assistant.getPlateType(place) == PlateType.simple) {

                printTemplateBinding.plateSimpleArea.setVisibility(View.VISIBLE);
                printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
                printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);

                printTemplateBinding.plateSimpleTag1.setText(place.tag1);
                printTemplateBinding.plateSimpleTag2.setText(place.tag2);
                printTemplateBinding.plateSimpleTag3.setText(place.tag3);
                printTemplateBinding.plateSimpleTag4.setText(place.tag4);

            } else if (assistant.getPlateType(place) == PlateType.old_aras) {

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

                samanPayment.printParkInfo(assistant.toJalali(startTime), place.tag1, place.tag2, place.tag3, place.tag4, place.id, binding.printArea, pricing, telephone, sms_number, qr_url, debt, balance);

            }, 500);


        }


    }

    @SuppressLint("SetTextI18n")
    private void printMiniFactor(String tag1, String tag2, String tag3, String tag4, int balance) {

            if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN) {

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


            }


    }

    //-------------------------------------------------------- API calls

    private void getPlaces() {

        System.out.println("-----------> getPlaces");

//        Log.e("getPlaces", "sending ... ");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        if (placesLoadedForFirstTime)
            loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

                System.out.println("-----------> getPlaces response : " + response.raw().toString());

                binding.refreshLayout.setRefreshing(false);

//                if (placesLoadedForFirstTime)
                loadingBar.dismiss();
                if (response.isSuccessful()) {

                    placesLoadedForFirstTime = false;

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

                        myPlaces.addAll(response.body().watchman.places);
                        for (Place place : response.body().watchman.places)
                            if (place.exit_request != null) {
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
//                if (placesLoadedForFirstTime)
                    loadingBar.dismiss();
                binding.refreshLayout.setRefreshing(false);
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

                assistant.hideSoftKeyboard(activity);

                loadingBar.dismiss();
                if (response.isSuccessful()) {

                    if (response.body().getSuccess() == 1) {

                        parkResponseDialog = new ParkResponseDialog(response.body().getInfo().number, response.body().getInfo().price, response.body().getInfo().car_balance, () -> {

                            parkResponseDialog.dismiss();

                        });

                        parkResponseDialog.show(getSupportFragmentManager(), ParkResponseDialog.TAG);

                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();
                        parkDialog.dismiss();

                        if (response.body().getInfo().car_balance < 0)
                            debt = -response.body().getInfo().car_balance;

                        if (printFactor) {

                            Place place = response.body().getInfo().place;
                            printFactor(place.id, place.start, response.body().getInfo().car_balance, place);

                        }


                        getPlaces();

                    } else
                        Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                } else
                    APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> parkCar(parkBody, printFactor));
            }

            @Override
            public void onFailure(Call<ParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> parkCar(parkBody, printFactor));
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
                t.printStackTrace();
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

    private void verifyTransaction(Transaction transaction) {

        System.out.println("----------> verifyTransaction");

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());

        loadingBar.show();

        transaction.devideAmountByTen();

        repository.verifyTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                transaction, new Callback<VerifyTransactionResponse>() {
                    @Override
                    public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {


                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            if (parkInfoDialog != null)
                                parkInfoDialog.dismiss();

                            sh_r.removeFromTransactions(transaction);

                            Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();

                            if (Assistant.SELECTED_PAYMENT == Assistant.SAMAN) {

                                String tag1 = sh_r.getString(SharedPreferencesRepository.TAG1, "0");
                                String tag2 = sh_r.getString(SharedPreferencesRepository.TAG2, "0");
                                String tag3 = sh_r.getString(SharedPreferencesRepository.TAG3, "0");
                                String tag4 = sh_r.getString(SharedPreferencesRepository.TAG4, "0");

                                getCarDebtHistory(assistant.getPlateType(tag1, tag2, tag3, tag4), tag1, tag2, tag3, tag4, 0, 1);

                            }

                            getPlaces();

                        } else
                            APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), activity, response, () -> verifyTransaction(transaction));
                    }

                    @Override
                    public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        t.printStackTrace();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> verifyTransaction(transaction));
                    }
                });

    }

    private void verifyUnverifiedTransaction(Transaction transaction) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());

        transaction.devideAmountByTen();

        repository.verifyTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                transaction, new Callback<VerifyTransactionResponse>() {
                    @Override
                    public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                        if (response.isSuccessful()) {
                            sh_r.removeFromTransactions(transaction);
                        }
                    }

                    @Override
                    public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                        t.printStackTrace();
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

                    assistant.eventByMobile(sh_r.getString(SharedPreferencesRepository.USERNAME,"not logged-in"),"logout");

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

    private void getCarDebtHistory(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        repository.getCarDebtHistory("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, limit, offset, new Callback<DebtHistoryResponse>() {
                    @Override
                    public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {


                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            if (response.body().getSuccess() == 1) {

                                if (assistant.getPlateType(tag1,tag2,tag3,tag4) == PlateType.simple)
                                    printMiniFactor(tag1,
                                            tag2,
                                            tag3,
                                            tag4, response.body().balance);
                                else if (assistant.getPlateType(tag1,tag2,tag3,tag4) == PlateType.old_aras)
                                    printMiniFactor(tag1, "0", "0", "0", response.body().balance);
                                else
                                    printMiniFactor(tag1, tag2, "0", "0", response.body().balance);

                            }


                        }
                    }

                    @Override
                    public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        t.printStackTrace();
                    }
                });

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
