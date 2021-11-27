package com.azarpark.watchman.payment.parsian;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.device.PrinterManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.databinding.PrintTemplateBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.CreateTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.util.Set;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParsianPayment {

    public static int PAYMENT_REQUEST_CODE = 103;
    String PLATE_TYPE = "plate_type";
    public static String QR_DATA = "qr_data";
    String TAG1 = "tag1";
    String TAG2 = "tag2";
    String TAG3 = "tag3";
    String TAG4 = "tag4";
    String PLACE_ID = "place_id";
    String PARSIAN = "parsian";
    public static int QR_SCANER_REQUEST_CODE = 350;
    MessageDialog messageDialog;
    PlateType plateType;
    String tag1;
    String tag2;
    String tag3;
    String tag4;
    int placeID;

    Context context;
    Activity activity;
    ParsianPaymentCallBack parsianPaymentCallBack;
    FragmentManager fragmentManager;
    LoadingBar loadingBar;
    SharedPreferencesRepository sh_p;
    Assistant assistant;

    public ParsianPayment(Context context, Activity activity, ParsianPaymentCallBack parsianPaymentCallBack, FragmentManager fragmentManager) {
        this.context = context;
        this.parsianPaymentCallBack = parsianPaymentCallBack;
        this.fragmentManager = fragmentManager;
        this.activity = activity;
        loadingBar = new LoadingBar(activity);
        sh_p = new SharedPreferencesRepository(context);
        assistant = new Assistant();
    }

    public void paymentRequest(int amount, Long res_num, Activity activity, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {

        amount *= 10;


        Intent intent = new Intent("ir.totan.pos.view.cart.TXN");
        intent.putExtra("type", 3);
        intent.putExtra("amount", Integer.toString(amount));
        intent.putExtra("res_num", res_num);
        this.plateType = plateType;
        this.tag1 = tag1;
        this.tag2 = tag2;
        this.tag3 = tag3;
        this.tag4 = tag4;
        this.placeID = placeID;
        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void createTransaction(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID, int transactionType) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(context);
        RetrofitAPIRepository repository = new RetrofitAPIRepository(context);
        loadingBar.show();

        if (tag2 == null) tag2 = "null";
        if (tag3 == null) tag3 = "null";
        if (tag4 == null) tag4 = "null";
//
        String finalTag2 = tag2;
        String finalTag3 = tag3;
        String finalTag4 = tag4;
        repository.createTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType,
                tag1,
                tag2,
                tag3,
                tag4,
                amount,
                transactionType,
                new Callback<CreateTransactionResponse>() {
                    @Override
                    public void onResponse(Call<CreateTransactionResponse> call, Response<CreateTransactionResponse> response) {


                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            long our_token = response.body().our_token;

                            sh_p.saveString(SharedPreferencesRepository.PLATE_TYPE, plateType.toString());
                            sh_p.saveString(SharedPreferencesRepository.TAG1, tag1);
                            sh_p.saveString(SharedPreferencesRepository.TAG2, finalTag2);
                            sh_p.saveString(SharedPreferencesRepository.TAG3, finalTag3);
                            sh_p.saveString(SharedPreferencesRepository.TAG4, finalTag4);
                            sh_p.saveString(SharedPreferencesRepository.AMOUNT, String.valueOf(amount));
                            sh_p.saveString(SharedPreferencesRepository.PLACE_ID, Integer.toString(placeID));
                            sh_p.saveString(SharedPreferencesRepository.OUR_TOKEN, Long.toString(our_token));

                            Transaction transaction = new Transaction(
                                    Integer.toString(amount),
                                    Long.toString(our_token),
                                    "0",
                                    Integer.parseInt(sh_r.getString(SharedPreferencesRepository.PLACE_ID,"0")),
                                    0,
                                    PARSIAN,
                                    "0",
                                    "",
                                    "",
                                    "",
                                    "",
                                    Assistant.getUnixTime());

                            sh_r.addToTransactions(transaction);

                            paymentRequest(amount, our_token, activity, plateType, tag1, finalTag2, finalTag3, finalTag4, placeID);

                        } else
                            try {

                                APIErrorHandler.onResponseErrorHandler(fragmentManager, activity, response, () -> createTransaction(plateType, tag1, finalTag2, finalTag3, finalTag4, amount, placeID,transactionType));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }

                    @Override
                    public void onFailure(Call<CreateTransactionResponse> call, Throwable t) {

                        try {

                            loadingBar.dismiss();
                            t.printStackTrace();
                            APIErrorHandler.onFailureErrorHandler(fragmentManager, t, () -> createTransaction(plateType, tag1, finalTag2, finalTag3, finalTag4, amount, placeID,transactionType));


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                       }
                });

    }

    public void handleResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAYMENT_REQUEST_CODE) {

            Transaction transaction;

            if (resultCode == Activity.RESULT_OK) {

                Bundle b = data.getBundleExtra("response");
                if (b != null) {

                    Log.d("bundle data", getBundleString(b));

                    int amount = Integer.parseInt(sh_p.getString(SharedPreferencesRepository.AMOUNT,"0"));
                    String result = b.getString("result");
                    String pan = b.getString("pan");
                    String rrn = b.getString("rrn");
                    Long date = b.getLong("date");
                    String trace = b.getString("trace");
                    String errorMessage = b.getString("message", "");
//                    Long res_num = Long.parseLong(sh_p.getString(SharedPreferencesRepository.OUR_TOKEN));
                    Long res_num = b.getLong("res_num");
                    int status = errorMessage.isEmpty() ? 1 : -1;


                    if (tag2 == null) tag2 = "null";
                    if (tag3 == null) tag3 = "null";
                    if (tag4 == null) tag4 = "null";

                    transaction = new Transaction(
                            Integer.toString(amount),
                            Long.toString(res_num),
                            rrn.isEmpty()?"0":rrn,
                            placeID,
                            status,
                            PARSIAN,
                            result.equals("succeed") ? "1" : "-1",
                            pan,
                            Long.toString(date),
                            trace,
                            result,
                            Assistant.getUnixTime());


                } else {

                    String amount = sh_p.getString(SharedPreferencesRepository.AMOUNT, "0");
                    String resNum = sh_p.getString(SharedPreferencesRepository.OUR_TOKEN, "0");
                    int placeID = Integer.parseInt(sh_p.getString(SharedPreferencesRepository.PLACE_ID, "0"));
                    String tag4 = sh_p.getString(SharedPreferencesRepository.TAG4, "0");

                    transaction = new Transaction(
                            amount,
                            resNum,
                            "0",
                            placeID,
                            -1,
                            PARSIAN,
                            "-1",
                            "****-****-****-****",
                            "0",
                            null,
                            "not succeed",
                            Assistant.getUnixTime());

                }




            } else {

                String amount = sh_p.getString(SharedPreferencesRepository.AMOUNT, "0");
                String resNum = sh_p.getString(SharedPreferencesRepository.OUR_TOKEN, "0");
                int placeID = Integer.parseInt(sh_p.getString(SharedPreferencesRepository.PLACE_ID, "0"));

                transaction = new Transaction(
                        amount,
                        resNum,
                        "0",
                        placeID,
                        -1,
                        PARSIAN,
                        "-1",
                        "****-****-****-****",
                        "0",
                        null,
                        "not succeed",
                        Assistant.getUnixTime());


            }


            sh_p.updateTransactions(transaction);

            parsianPaymentCallBack.verifyTransaction(transaction);


        }
        else if (requestCode == QR_SCANER_REQUEST_CODE) {

            if (data != null) {

                try{

                    String scannedData = data.getExtras().getString(QR_DATA);
                    int placeId = Integer.parseInt(scannedData.split("=")[scannedData.split("=").length - 1]);
                    parsianPaymentCallBack.getScannerData(placeId);

                } catch (Exception e) {
                    Toast.makeText(context, "معتبر نمیباشد", Toast.LENGTH_LONG).show();
                }

            }

        }

    }

    private String getBundleString(Bundle b) {

//        sample
//        amount : 000000001000
//        result : (succeed / unsucceed)
//        pan : 589210***2557
//        rrn : 801663199541
//        date : 23732049000
//        trace : 000015
//        message :    (this will have value if there is an error)
//        res_num : -1 (our_token)
//        charge_pin : null

        Set<String> keys = b.keySet();

        StringBuffer sb = new StringBuffer();

        for (String key : keys) {
            sb.append(key);
            sb.append(" : ");
            if (key.equals("date") || key.equals("res_num"))
                sb.append(b.getLong(key));
            else
                sb.append(b.getString(key));
            sb.append(" - ");
        }

        return sb.toString();

    }

//    public void printParkInfo(String startTime, String tag1, String tag2, String tag3, String tag4,
//                              int placeID, ViewGroup viewGroupForBindFactor, String pricing, String telephone, String sms_number,
//                              String qr_url) {
//
//
//        PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(context), viewGroupForBindFactor, true);
//
//        printTemplateBinding.placeId.setText(placeID + "");
//
////                String time = startTime.split(" ")[]
//
//        printTemplateBinding.startTime.setText(startTime);
//        printTemplateBinding.prices.setText(pricing);
//        printTemplateBinding.supportPhone.setText(telephone);
//        printTemplateBinding.description.setText("در صورت عدم حضور پارکیار عدد " + placeID + " را به شماره " + sms_number + " ارسال کنید");
//
//        printTemplateBinding.qrcode.setImageBitmap(QRGenerator(qr_url + placeID));
//
//        if (tag4 != null && !tag4.isEmpty()) {
//
//            printTemplateBinding.plateSimpleArea.setVisibility(View.VISIBLE);
//            printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
//            printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);
//
//            printTemplateBinding.plateSimpleTag1.setText(tag1);
//            printTemplateBinding.plateSimpleTag2.setText(tag2);
//            printTemplateBinding.plateSimpleTag3.setText(tag3);
//            printTemplateBinding.plateSimpleTag4.setText(tag4);
//
//        } else if (tag2 == null || tag2.isEmpty()) {
//
//            printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
//            printTemplateBinding.plateOldArasArea.setVisibility(View.VISIBLE);
//            printTemplateBinding.plateNewArasArea.setVisibility(View.GONE);
//
//            printTemplateBinding.plateOldArasTag1En.setText(tag1);
//            printTemplateBinding.plateOldArasTag1Fa.setText(tag1);
//
//        } else {
//
//            printTemplateBinding.plateSimpleArea.setVisibility(View.GONE);
//            printTemplateBinding.plateOldArasArea.setVisibility(View.GONE);
//            printTemplateBinding.plateNewArasArea.setVisibility(View.VISIBLE);
//
//            printTemplateBinding.plateNewArasTag1En.setText(tag1);
//            printTemplateBinding.plateNewArasTag1Fa.setText(tag1);
//            printTemplateBinding.plateNewArasTag2En.setText(tag2);
//            printTemplateBinding.plateNewArasTag2Fa.setText(tag2);
//
//        }
//
//        PrinterManager printer = new PrinterManager();
//        int setupResult = printer.setupPage(-1,-1);
//        printer.drawBitmap(getViewBitmap(printTemplateBinding.getRoot()),0,0);
//        printer.printPage(0);
//
//
//    }
//
//    public static Bitmap getViewBitmap(View v) {
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
//    }

    public void printParkInfo(Place place, int placeID, ViewGroup viewGroupForBindFactor, String pricing, String telephone, String sms_number,
                              String qr_url, int balance) {

        PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(context), viewGroupForBindFactor, true);

        if (balance > 0) {

            printTemplateBinding.description2.setText("شهروند گرامی؛از این که جز مشتریان خوش حساب ما هستید سپاسگزاریم");
            printTemplateBinding.balanceTitle.setText("اعتبار پلاک");

        } else if (balance < 0) {

            printTemplateBinding.description2.setText("اخطار: شهروند گرامی؛بدهی پلاک شما بیش از حد مجاز میباشد در صورت عدم پرداخت بدهی مشمول جریمه پارک ممنوع خواهید شد");
            printTemplateBinding.balanceTitle.setText("بدهی پلاک");

        } else {

            printTemplateBinding.description2.setText("شهروند گرامی در صورت عدم پرداخت هزینه پارک مشمول جریمه پارک ممنوع خواهید شد");
            printTemplateBinding.balanceTitle.setText("اعتبار پلاک");
        }

//        printTemplateBinding.debtArea.setVisibility(balance <= 0 ? View.VISIBLE : View.GONE);

        printTemplateBinding.placeId.setText(place.number + "");
        printTemplateBinding.debt.setText(balance + " تومان");

        printTemplateBinding.startTime.setText(place.start);

        printTemplateBinding.prices.setText(pricing);
        printTemplateBinding.supportPhone.setText(telephone);
        printTemplateBinding.description.setText("در صورت عدم حضور پارکیار عدد " + placeID + " را به شماره " + sms_number + " ارسال کنید");

        printTemplateBinding.qrcode.setImageBitmap(QRGenerator(qr_url + placeID));

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

        try {

            viewGroupForBindFactor.post(() -> {

                PrinterManager printer = new PrinterManager();
                int setupResult = printer.setupPage(-1, -1);
                printer.drawBitmap(getViewBitmap(printTemplateBinding.getRoot()), 0, 0);
                int printResult = printer.printPage(0);


            });


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static Bitmap getViewBitmap(View view) {


        view.setDrawingCacheEnabled(true);

        view.buildDrawingCache();

        return view.getDrawingCache();

//        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(returnedBitmap);
//        Drawable bgDrawable = view.getBackground();
//        if (bgDrawable != null)
//            bgDrawable.draw(canvas);
//        else
//            canvas.drawColor(Color.WHITE);
//        view.draw(canvas);
//        return returnedBitmap;





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

    public Bitmap QRGenerator(String value) {

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(value, null, QRGContents.Type.TEXT, 512);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        Bitmap bitmap = qrgEncoder.getBitmap();

        return bitmap;


    }

    public interface ParsianPaymentCallBack {

        public void verifyTransaction(Transaction transaction);

        public void getScannerData(int placeID);

        public void onVersifyFinished();

    }

}
