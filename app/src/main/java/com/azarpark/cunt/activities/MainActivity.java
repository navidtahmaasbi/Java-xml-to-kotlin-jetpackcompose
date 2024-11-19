package com.azarpark.cunt.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.azarpark.cunt.R;
import com.azarpark.cunt.adapters.LocalNotificationsListAdapter;
import com.azarpark.cunt.adapters.ParkListAdapter;
import com.azarpark.cunt.core.AppConfig;
import com.azarpark.cunt.databinding.ActivityMainBinding;
import com.azarpark.cunt.databinding.SamanAfterPaymentPrintTemplateBinding;
import com.azarpark.cunt.databinding.SamanPrintTemplateBinding;
import com.azarpark.cunt.dialogs.ConfirmDialog;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.dialogs.MessageDialog;
import com.azarpark.cunt.dialogs.ParkDialog;
import com.azarpark.cunt.dialogs.ParkInfoDialog;
import com.azarpark.cunt.dialogs.ParkResponseDialog;
import com.azarpark.cunt.dialogs.PlateChargeDialog;
import com.azarpark.cunt.dialogs.PlateDiscountDialog;
import com.azarpark.cunt.enums.PlaceStatus;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.interfaces.OnGetInfoClicked;
import com.azarpark.cunt.location.SingleShotLocationProvider;
import com.azarpark.cunt.models.Notification;
import com.azarpark.cunt.models.Place;
import com.azarpark.cunt.models.TicketMessage;
import com.azarpark.cunt.models.TicketMessagePart;
import com.azarpark.cunt.models.Transaction;
import com.azarpark.cunt.payment.PaymentService;
import com.azarpark.cunt.payment.ShabaType;
import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.ImageUploadCallback;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.ProgressRequestBody;
import com.azarpark.cunt.web_service.WebService;
import com.azarpark.cunt.web_service.bodies.ParkBody;
import com.azarpark.cunt.web_service.responses.DebtHistoryResponse;
import com.azarpark.cunt.web_service.responses.DeleteExitRequestResponse;
import com.azarpark.cunt.web_service.responses.ExitParkResponse;
import com.azarpark.cunt.web_service.responses.LogoutResponse;
import com.azarpark.cunt.web_service.responses.ParkResponse;
import com.azarpark.cunt.web_service.responses.PlacesResponse;
import com.azarpark.cunt.web_service.responses.VerifyTransactionResponse;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import dagger.hilt.android.AndroidEntryPoint;
import id.zelory.compressor.Compressor;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    Compressor compressor;
    ActivityMainBinding binding;
    boolean menuIsOpen = false;
    PopupWindow popupWindow;
    View popupView;
    ConfirmDialog confirmDialog;
    ParkListAdapter adapter;
    ParkDialog parkDialog;
    ParkInfoDialog parkInfoDialog;
    MessageDialog messageDialog;
    int exitRequestCount = 0;
    TextView watchManName;
    boolean updatePopUpIsShowed = false;
    int version = 0;
    PlateChargeDialog plateChargeDialog;

    PlateDiscountDialog plateDiscountDialog;
    Activity activity = this;
    int refresh_time = 10;
    String qr_url, telephone, pricing, sms_number, rules_url, about_us_url, guide_url;
    Timer timer;
    private PaymentService paymentService;
    Assistant assistant;
    int debt = 0;
    ParkResponseDialog parkResponseDialog;
    LocalNotificationsListAdapter localNotificationsListAdapter;
    int versionCode = 0;
    String versionName = "";
    WebService webService = new WebService();
    private int locationPermissionCode = 2563;
    boolean printing = false;

    //------------------------------------------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        assistant = new Assistant();

        compressor = new Compressor(this)
                .setQuality(70)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .setDestinationDirectoryPath(getFilesDir().toString() + Constants.IMAGES_DIRECTORY);


//        String subdomain = "ardabil"; // Change this based on your city logic if it's dynamic
////        AppConfig.PaymentType paymentType = AppConfig.getCurrentConfig().getPaymentType(subdomain);
//        AppConfig.PaymentType paymentType = AppConfig.Companion.getCityPayment().containsKey(subdomain)
//                ? AppConfig.Companion.getCityPayment().get(subdomain)
//                : AppConfig.PaymentType.PAYMENT_LESS;
//
//        // Update selectedConfig with the payment type for the selected city
//        assert paymentType != null;
//        AppConfig.updateSelectedConfig(new AppConfig.Companion.Config(
//                AppConfig.http + subdomain + "." + AppConfig.domain,
//                paymentType,
//                subdomain
//        ));
//
//        // Log the selected payment type to verify it loaded correctly
//        Log.d("MainActivity", "Selected payment type: " + AppConfig.getCurrentConfig().getPaymentType());
//
//        try {
//            // Initialize payment service after confirming selectedConfig is set up
//            paymentService = new PaymentService.Builder()
//                    .activity(this)
//                    .webService(webService)
//                    .paymentCallback(new PaymentService.OnPaymentCallback() {
//                        @Override
//                        public void onScanDataReceived(int data) {
//                            handleScannedPlaceID(data);
//                        }
//
//                        @Override
//                        public void onTransactionVerified(@NonNull Transaction transaction) {
//                            String tag1 = SharedPreferencesRepository.getValue(Constants.TAG1, "0");
//                            String tag2 = SharedPreferencesRepository.getValue(Constants.TAG2, "0");
//                            String tag3 = SharedPreferencesRepository.getValue(Constants.TAG3, "0");
//                            String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");
//
//                            if (transaction.getTransactionType() == Constants.TRANSACTION_TYPE_DISCOUNT) {
//                                printMiniFactor(tag1, tag2, tag3, tag4, Integer.valueOf(transaction.getAmount()), true);
//                            } else {
//                                getCarDebtHistory02(assistant.getPlateType(tag1, tag2, tag3, tag4), tag1, tag2, tag3, tag4, 0, 1);
//                            }
//
//                            getPlaces02();
//                        }
//                    })
//                    .build();
//
//            paymentService.initialize();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("Failed to initialize payment service.", e);
//        }





        paymentService = new PaymentService.Builder()
                .activity(this)
                .webService(webService)
                .paymentCallback(new PaymentService.OnPaymentCallback() {
                    @Override
                    public void onScanDataReceived(int data) {
                        handleScannedPlaceID(data);
                    }

                    @Override
                    public void onTransactionVerified(@NonNull Transaction transaction) {
                        String tag1 = SharedPreferencesRepository.getValue(Constants.TAG1, "0");
                        String tag2 = SharedPreferencesRepository.getValue(Constants.TAG2, "0");
                        String tag3 = SharedPreferencesRepository.getValue(Constants.TAG3, "0");
                        String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");

                        if (transaction.getTransactionType() == Constants.TRANSACTION_TYPE_DISCOUNT) {
                            printMiniFactor(tag1, tag2, tag3, tag4, Integer.valueOf(transaction.getAmount()), true);
                        } else {
                            getCarDebtHistory02(assistant.getPlateType(tag1, tag2, tag3, tag4), tag1, tag2, tag3, tag4, 0, 1);
                        }

                        getPlaces02();
                    }
                })
                .build();
        Log.d("MainActivity", "Selected payment type: " + AppConfig.getCurrentConfig().getPaymentType());

        paymentService.initialize();


        qr_url = SharedPreferencesRepository.getValue(Constants.qr_url, "");
        refresh_time = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.refresh_time, "10"));
        telephone = SharedPreferencesRepository.getValue(Constants.telephone, "");
        pricing = SharedPreferencesRepository.getValue(Constants.pricing, "");
        sms_number = SharedPreferencesRepository.getValue(Constants.sms_number, "");
        rules_url = SharedPreferencesRepository.getValue(Constants.rules_url, "");
        about_us_url = SharedPreferencesRepository.getValue(Constants.about_us_url, "");
        guide_url = SharedPreferencesRepository.getValue(Constants.guide_url, "");

        localNotificationsListAdapter = new LocalNotificationsListAdapter((int placeID) -> {

            if (binding.filterEdittext.getText().toString().equals(Integer.toString(placeID)))
                binding.filterEdittext.setText("");
            else
                binding.filterEdittext.setText(Integer.toString(placeID));

        });
        binding.notificationsRecyclerview.setAdapter(localNotificationsListAdapter);

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        listeners();

        binding.filterEdittext.clearFocus();

        adapter = new ParkListAdapter(getApplicationContext(), place -> {

            if (place.status.contains(PlaceStatus.free.toString())) {
                if (SharedPreferencesRepository.needLocation()) {
                    System.out.println("---------> permission : " + locationPermissionGranted());
                    if (!locationPermissionGranted()) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                                locationPermissionCode);
                    } else if (!isLocationEnabled(this)) {
                        Toast.makeText(this, "جی پی اس خود را روشن کنید", Toast.LENGTH_SHORT).show();
                    } else {
                        openParkDialog(place, false);
                        SingleShotLocationProvider.requestSingleUpdate(this,
                                location -> {
                                    if (parkDialog != null)
                                        parkDialog.setLocation(location);
                                });
                    }
                } else
                    openParkDialog(place, false);

            } else {

                openParkInfoDialog(place);
            }

//            binding.filterEdittext.setText("");


        });
        binding.recyclerView.setAdapter(adapter);

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
            versionName = pInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        initMenuPopup();

        binding.incomeStatistics.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, IncomeStatisticsActivity02.class)));

    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        setTimer();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        paymentService.onActivityResultHandler(requestCode, resultCode, data);
        if(parkDialog != null) parkDialog.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (menuIsOpen) {
            menuIsOpen = false;
            popupWindow.dismiss();
        } else if (adapter.isShowExitRequestItems()) {

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
        paymentService.stop();

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

        popupView.findViewById(R.id.discount).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, DiscountActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.debt_inquiry).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, DebtCheckActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.impresst).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, ImprestActivity.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.vacation).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, VacationsActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.car_number_charge).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, CarNumberChargeActivity.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.pay_and_exit_parked_plate).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, PayAndExitParkedPlateActivity.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.watchman_times).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, WatchmanTimesActivity.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.tickets).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, TicketsActivity.class));
            popupWindow.dismiss();
        });

        popupView.findViewById(R.id.income_statistics).setOnClickListener(view -> {
            startActivity(new Intent(MainActivity.this, IncomeStatisticsActivity02.class));
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.help).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra("url", SharedPreferencesRepository.getValue(Constants.guide_url));
            startActivity(intent);
            popupWindow.dismiss();

        });
        watchManName = popupView.findViewById(R.id.name);
        popupView.findViewById(R.id.about_us).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra("url", SharedPreferencesRepository.getValue(Constants.about_us_url));
            startActivity(intent);
            popupWindow.dismiss();
        });

        CardView check_current_park_card = popupView.findViewById(R.id.check_current_park_card);
        CardView buy_discount_card = popupView.findViewById(R.id.buy_discount_card);
        CardView check_debt_card = popupView.findViewById(R.id.check_debt_card);
        CardView car_balance_card = popupView.findViewById(R.id.car_balance_card);
        CardView changePlateBtn = popupView.findViewById(R.id.change_plate_btn);
        changePlateBtn.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, ChangePlateActivity.class);
            startActivity(intent);
            popupWindow.dismiss();
        });

        if (SharedPreferencesRepository.getIsWage()) {
            check_current_park_card.setVisibility(View.GONE);
            buy_discount_card.setVisibility(View.GONE);
            check_debt_card.setVisibility(View.GONE);
            car_balance_card.setVisibility(View.GONE);
            changePlateBtn.setVisibility(View.VISIBLE);
        } else {
            check_current_park_card.setVisibility(View.VISIBLE);
            buy_discount_card.setVisibility(View.VISIBLE);
            check_debt_card.setVisibility(View.VISIBLE);
            car_balance_card.setVisibility(View.VISIBLE);
            changePlateBtn.setVisibility(View.GONE);
        }

//        popupView.findViewById(R.id.about_us).setOnLongClickListener(new View.OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View view) {
//
//                startActivity(new Intent(MainActivity.this, PaymentTestActivity.class));
//
//                return false;
//            }
//        });
        popupView.findViewById(R.id.rules).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
            intent.putExtra("url", SharedPreferencesRepository.getValue(Constants.rules_url));
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

                    logout02();

                }

                @Override
                public void onCancelClicked() {

                    confirmDialog.dismiss();
                }
            });

            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

            popupWindow.dismiss();

        });

        ((TextView) popupView.findViewById(R.id.version)).setText("نسخه " + versionName);


    }

    private void setTimer() {
        if (timer == null) {
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    MainActivity.this.runOnUiThread(() -> {
                        getPlaces02();
                        SharedPreferencesRepository.checkTransactions();
                        verifyUnverifiedTransactions();
                    });
                }
            }, 0, refresh_time * 1000);
        } else {
            timer.cancel();
            timer = null;
            setTimer();
        }
    }

    private void verifyUnverifiedTransactions() {

        for (Transaction transaction : SharedPreferencesRepository.getTransactions())
            if (transaction.getStatus() != 0)
                verifyUnverifiedTransaction02(transaction);

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

            getPlaces02();

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 5023);
            return;
        }

        paymentService.launchQrCodeScanner();
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

    private void openParkDialog(Place place, boolean isParkingNewPlateOnPreviousPlate) {
        System.out.println("--------> BBB");
        parkDialog = new ParkDialog(
                (parkBody, printFactor, sourceImageUri, plateImageUri) -> {

                    if (!isParkingNewPlateOnPreviousPlate) {
                        parkCar(parkBody, printFactor, sourceImageUri, plateImageUri);
                    } else {
                        exitPark(place.id, parkBody, printFactor, sourceImageUri, plateImageUri);
                    }

                },
                place,
                isParkingNewPlateOnPreviousPlate
        );
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);

        assistant.hideSoftKeyboard(MainActivity.this);

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

                assistant.saveTags(place.tag1, place.tag2, place.tag3, place.tag4, getApplicationContext());

                if (parkInfoDialog != null)
                    parkInfoDialog.dismiss();

                paymentService.createTransaction(ShabaType.NON_CHARGE, selectedPlateType,
                        place.tag1, place.tag2, place.tag3, place.tag4,
                        price, place.id, Constants.TRANSACTION_TYPE_PARK_PRICE,
                        null, -1, false, null
                );
            }

            @Override
            public void payAsDebt(Place place) {

                exitPark(place.id);

            }

            @Override
            public void removeExitRequest(Place place1) {

                deleteExitRequest02(place1.id);

            }

            @Override
            public void charge(PlateType plateType, String tag1, String tag2, String tag3, String tag4, boolean hasMobile) {

                parkInfoDialog.dismiss();

                plateChargeDialog = new PlateChargeDialog((amount) -> {
                    paymentService.createTransaction(
                            ShabaType.CHARGE, plateType,
                            tag1, tag2, tag3, tag4,
                            amount, -1, Constants.TRANSACTION_TYPE_CHAREG,
                            () -> plateChargeDialog.dismiss(), -1, false, null
                    );
                }, place, hasMobile);

                plateChargeDialog.show(getSupportFragmentManager(), PlateChargeDialog.TAG);

            }

            @Override
            public void buyDiscount(PlateType plateType, String tag1, String tag2, String tag3, String tag4, boolean hasMobile) {

                parkInfoDialog.dismiss();

                plateDiscountDialog = new PlateDiscountDialog((discount) -> {
                    paymentService.createTransaction(
                            ShabaType.CHARGE, plateType,
                            tag1, tag2, tag3, tag4,
                            discount.price, -1,
                            Constants.TRANSACTION_TYPE_DISCOUNT, () -> {
                            }, discount.id, false, null
                    );
                }, place, hasMobile);

                plateDiscountDialog.show(getSupportFragmentManager(), PlateChargeDialog.TAG);

            }

            @Override
            public void print(int parkCount, String startTime, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID, int debt, int balance, String printDescription, int printCommand, String carToken) {

                if (printCommand == 1)
                    printFactor(parkCount, startTime, balance, place, carToken);

            }

            @Override
            public void newPark(Place place) {
                parkInfoDialog.dismiss();
                openParkDialog(place, true);
            }

        }, place);
        parkInfoDialog.show(getSupportFragmentManager(), ParkDialog.TAG);

    }

    @SuppressLint("SetTextI18n")
    private void printFactor(int userPrintNumber, String startTime, int balance, Place place, String carToken) {
        if (printing) return;

        binding.printArea.removeAllViews();
        SamanPrintTemplateBinding printTemplateBinding = SamanPrintTemplateBinding.inflate(LayoutInflater.from(this), binding.printArea, true);

        printTemplateBinding.placeTv.setText("جایگاه: " + place.number);
        printTemplateBinding.startTimeTv.setText(assistant.toJalali(startTime));
        printTemplateBinding.priceTv.setText("تعرفه: " + pricing);
        printTemplateBinding.debtTv.setText(
                balance > 0
                        ? "اعتبار پلاک: " + balance + " تومان"
                        : "بدهی پلاک: " + (-1 * balance) + " تومان"
        );
        printTemplateBinding.supportPhone.setText(telephone);


        if (assistant.getPlateType(place) == PlateType.simple) {
            printTemplateBinding.plateArea.setText(
                    String.format("%s %s %s / ایران %s", place.tag1, place.tag2, place.tag3, place.tag4)
            );
        } else if (assistant.getPlateType(place) == PlateType.old_aras) {
            printTemplateBinding.plateArea.setText(place.tag1);
        } else if (assistant.getPlateType(place) == PlateType.new_aras) {
            printTemplateBinding.plateArea.setText(String.format("%s %s", place.tag1, place.tag2));
        }

        Map<String, TicketMessage> ticketMessages = AppConfig.Companion.getTicketMessage();
        boolean hasWebView = false;
        if (ticketMessages != null && !ticketMessages.isEmpty()) {
            TicketMessage tMsg =
                    ticketMessages.containsKey(String.valueOf(userPrintNumber))
                            ? ticketMessages.get(String.valueOf(userPrintNumber))
                            : (ticketMessages.containsKey("default") ? ticketMessages.get("default") : null);

            if (tMsg != null) {
                if (tMsg.getNote() != null && !tMsg.getNote().isEmpty()) {
                    printTemplateBinding.noteSection.setVisibility(View.VISIBLE);
                    tMsg.inflateNote(this, printTemplateBinding.noteSection, true);
                    hasWebView = true;
                }

                Map<String, String> replacements = new HashMap<>();
                replacements.put("%car_token%", carToken);

                for (TicketMessagePart tMsgPrefix : tMsg.getPrefix()) {
                    if (tMsgPrefix.render(this, printTemplateBinding.prefixHolder, true, replacements))
                        hasWebView = true;
                }

                for (TicketMessagePart tMsgPostfix : tMsg.getPostfix()) {
                    if (tMsgPostfix.render(this, printTemplateBinding.postfixHolder, true, replacements))
                        hasWebView = true;
                }
            }
        }


        binding.loadingBar.setVisibility(View.VISIBLE);
        paymentService.print(
                binding.printArea,
                hasWebView ? 1500 : 500,
                () -> {
                    binding.loadingBar.setVisibility(View.GONE);
                    printing = false;
                }
        );
    }

    @SuppressLint("SetTextI18n")
    private void printMiniFactor(String tag1, String tag2, String tag3, String tag4, int balance, boolean discount) {
        binding.printArea.removeAllViews();

        SamanAfterPaymentPrintTemplateBinding printTemplateBinding = SamanAfterPaymentPrintTemplateBinding.inflate(LayoutInflater.from(getApplicationContext()), binding.printArea, true);

        if (discount) {
            printTemplateBinding.balanceTitle.setText("اشتراک پلاک");
        } else {
            printTemplateBinding.balanceTitle.setText(balance < 0 ? "بدهی پلاک" : "اعتبار پلاک");
        }

        printTemplateBinding.balance.setText(balance + " تومان" + (balance > 0 ? " (اعتبار 3 ماهه)" : ""));
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

        paymentService.print(binding.printArea, 500, null);
    }

    private void getPlaces02() {

        System.out.println("--------> getPlaces02");

        webService.getClient(getApplicationContext()).getPlaces(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<PlacesResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<PlacesResponse> call, @NonNull Response<PlacesResponse> response) {

                binding.refreshLayout.setRefreshing(false);
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (!updatePopUpIsShowed && version != 0 && response.body() != null && response.body().update.last_version > version) {

                    updatePopUpIsShowed = true;

                    if (response.body().update.is_forced == 1) {

                        messageDialog = MessageDialog.newInstance(
                                "به روز رسانی",
                                "به روز رسانی اجباری برای آذرپارک موجود است.",
                                "به روز رسانی",
                                () -> {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().update.update_link));
                                    startActivity(browserIntent);
                                }
                        );


                    }

                } else {

                    SharedPreferencesRepository.addNotifications(response.body().notifications);
                    if (!response.body().notifications.isEmpty()) {
                        playNotificationSound();
                    }
                    buildNotifications();

                    SharedPreferencesRepository.setValue(Constants.can_detect, response.body().can_detect);

                    if (watchManName != null && response.body() != null)
                        watchManName.setText(response.body().watchman.name);

                    exitRequestCount = 0;
                    ArrayList<Place> myPlaces = new ArrayList<>();
                    ArrayList<Integer> exitRequestPlaceIDs = new ArrayList<>();

                    if (response.body() != null)
                        myPlaces.addAll(response.body().watchman.places);
                    for (Place place : response.body().watchman.places)
                        if (place.exit_request != null) {
                            exitRequestCount++;
                            exitRequestPlaceIDs.add(place.id);
                        }

                    if (adapter.listHaveNewExitRequest(exitRequestPlaceIDs)) {

                        playNotificationSound();

                    }

                    adapter.setItems(myPlaces);
                    localNotificationsListAdapter.updateItems();
                    binding.notificationsRecyclerview.setVisibility(localNotificationsListAdapter.getItemCount() > 0 ? View.VISIBLE : View.GONE);

                    binding.placeholder.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);

                    binding.exitRequestCount.setText(Integer.toString(exitRequestCount));

                }

            }

            @Override
            public void onFailure(@NonNull Call<PlacesResponse> call, @NonNull Throwable t) {
                binding.refreshLayout.setRefreshing(false);
//                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            System.out.println("----------> song error");
            e.printStackTrace();
        }
    }

    private void buildNotifications() {
        ArrayList<Notification> notifications = SharedPreferencesRepository.getNotifications();
        if (!notifications.isEmpty()) {
            binding.notificationsArea.setVisibility(View.VISIBLE);
            binding.notificationCount.setText(String.valueOf(notifications.size()));
            binding.notificationsArea.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, NotificationsActivity.class)));
        } else {
            binding.notificationsArea.setVisibility(View.GONE);
        }
    }

    private MultipartBody.Part createFilePart(String partName, Bitmap bitmap) throws IOException {
        File file = Assistant.writeBitmapToFile(bitmap, Assistant.createCacheFile(this, "jpg"));
        ProgressRequestBody requestBody =
                new ProgressRequestBody(compressor.compressToFile(file), "image", mockListener);
        return MultipartBody.Part.createFormData(
                partName,
                file.getName(),
                requestBody
        );
    }

    private void parkCar(ParkBody parkBody, boolean printFactor, Bitmap sourceImageBmp, Bitmap plateImageBmp) {
        assistant.hideSoftKeyboard(MainActivity.this);
        Runnable functionRunnable = () -> parkCar(parkBody, printFactor, sourceImageBmp, plateImageBmp);

        Call<ParkResponse> call;
        if (sourceImageBmp != null && plateImageBmp != null) {
            try {
                List<MultipartBody.Part> parts = new ArrayList<>();
                parts.add(createFilePart("source_image", sourceImageBmp));
                parts.add(createFilePart("plate_image", plateImageBmp));

                call = webService.getClient(getApplicationContext()).parkCar(SharedPreferencesRepository.getTokenWithPrefix(), parkBody.toPartMap(), parts.toArray(new MultipartBody.Part[]{}));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
        call = webService.getClient(getApplicationContext()).parkCar(SharedPreferencesRepository.getTokenWithPrefix(), parkBody);
        }


        if(call == null){
            Toast.makeText(activity, "خطا در ایجاد درخواست پارک", Toast.LENGTH_SHORT).show();
            return;
        }

        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();
        call.enqueue(new Callback<ParkResponse>() {
            @Override
            public void onResponse(@NonNull Call<ParkResponse> call, @NonNull Response<ParkResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                SharedPreferencesRepository.setValue(Constants.lastLocationSentDate, Assistant.getDate().toString());

                int printCommand = response.body().getInfo().print_command;

                parkResponseDialog = new ParkResponseDialog(response.body().getInfo().number, response.body().getInfo().price, response.body().getInfo().car_balance, printCommand, () -> {

                    parkResponseDialog.dismiss();

                });

                parkResponseDialog.show(getSupportFragmentManager(), ParkResponseDialog.TAG);

                Toast.makeText(getApplicationContext(), response.body().getDescription(), Toast.LENGTH_SHORT).show();
                if (parkDialog != null)
                    parkDialog.dismiss();

                if (response.body().getInfo().car_balance < 0)
                    debt = -response.body().getInfo().car_balance;

                if (printFactor && printCommand == 1) {
                    Place place = response.body().getInfo().place;
                    printFactor(response.body().getInfo().park_count, place.start, response.body().getInfo().car_balance, place, response.body().getInfo().car_token);
                }


                getPlaces02();
            }

            @Override
            public void onFailure(@NonNull Call<ParkResponse> call, @NonNull Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void exitPark(int placeID) {

        Runnable functionRunnable = () -> exitPark(placeID);
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).exitPark(SharedPreferencesRepository.getTokenWithPrefix(), placeID).enqueue(new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (parkInfoDialog != null)
                    parkInfoDialog.dismiss();
                getPlaces02();

            }

            @Override
            public void onFailure(Call<ExitParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void exitPark(int placeID, ParkBody parkBody, boolean printFactor, Bitmap sourceImageUri, Bitmap plateImageUri) {

        Runnable functionRunnable = () -> exitPark(placeID);
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).exitPark(SharedPreferencesRepository.getTokenWithPrefix(), placeID).enqueue(new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (parkInfoDialog != null)
                    parkInfoDialog.dismiss();
                parkCar(parkBody, printFactor, sourceImageUri, plateImageUri);

            }

            @Override
            public void onFailure(Call<ExitParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void deleteExitRequest02(int placeID) {

        Runnable functionRunnable = () -> deleteExitRequest02(placeID);
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).deleteExitRequest(SharedPreferencesRepository.getTokenWithPrefix(), placeID).enqueue(new Callback<DeleteExitRequestResponse>() {
            @Override
            public void onResponse(Call<DeleteExitRequestResponse> call, Response<DeleteExitRequestResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (parkInfoDialog != null) {
                    parkInfoDialog.dismiss();
                }
                getPlaces02();

            }

            @Override
            public void onFailure(Call<DeleteExitRequestResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void verifyUnverifiedTransaction02(Transaction transaction) {

        Runnable functionRunnable = () -> verifyUnverifiedTransaction02(transaction);

        webService.getClient(getApplicationContext()).verifyTransaction(SharedPreferencesRepository.getTokenWithPrefix(), transaction.getAmount(),
                transaction.getOur_token(), transaction.getBank_token(), transaction.getPlaceID(), transaction.getStatus(), transaction.getBank_type(),
                transaction.getState(), transaction.getCard_number(), transaction.getBank_datetime(), transaction.getTrace_number(),
                transaction.getResult_message(), 0).enqueue(new Callback<VerifyTransactionResponse>() {
            @Override
            public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (response.isSuccessful() && response.body().getSuccess() == 1)
                    SharedPreferencesRepository.removeFromTransactions02(transaction);

            }

            @Override
            public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }//todo test

    private void logout02() {

        Runnable functionRunnable = () -> logout02();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).logout(SharedPreferencesRepository.getTokenWithPrefix()).enqueue(new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                Assistant.eventByMobile(SharedPreferencesRepository.getValue(Constants.USERNAME, "not logged-in"), "logout");

                SharedPreferencesRepository.setValue(Constants.ACCESS_TOKEN, "");
                SharedPreferencesRepository.setValue(Constants.REFRESH_TOKEN, "");
                SharedPreferencesRepository.setValue(Constants.SUB_DOMAIN, "");
                startActivity(new Intent(MainActivity.this, SplashActivity.class));
                MainActivity.this.finish();

            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }

    private void getCarDebtHistory02(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        Runnable functionRunnable = () -> getCarDebtHistory02(plateType, tag1, tag2, tag3, tag4, limit, offset);

        webService.getClient(getApplicationContext()).getCarDebtHistory(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4, limit, offset, null,null,0).enqueue(new Callback<DebtHistoryResponse>() {
            @Override
            public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                if (assistant.getPlateType(tag1, tag2, tag3, tag4) == PlateType.simple)
                    printMiniFactor(tag1,
                            tag2,
                            tag3,
                            tag4, response.body().balance, false);
                else if (assistant.getPlateType(tag1, tag2, tag3, tag4) == PlateType.old_aras)
                    printMiniFactor(tag1, "0", "0", "0", response.body().balance, false);
                else
                    printMiniFactor(tag1, tag2, "0", "0", response.body().balance, false);

            }

            @Override
            public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }//todo test

    //------------------------------------------------------------------------------------------

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

    private boolean locationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    private void requestLocation() {


//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER, 5000, 10, location -> {
//
//                });
    }

    ImageUploadCallback mockListener = new ImageUploadCallback() {
        @Override
        public void onProgressUpdate(int percentage) {

        }

        @Override
        public void onError(String message) {

        }

        @Override
        public void onSuccess(String message) {

        }
    };
}
