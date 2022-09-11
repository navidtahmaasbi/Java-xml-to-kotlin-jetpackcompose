package com.azarpark.watchman.payment.parsian;

import android.annotation.SuppressLint;
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

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.activities.MainActivity;
import com.azarpark.watchman.databinding.ParsianAfterPaymentPrintTemplateBinding;
import com.azarpark.watchman.databinding.PrintTemplateBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.saman.SamanPayment;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.DebtHistoryResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.google.gson.Gson;

import java.util.Set;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ParsianPayment {

    public static int PAYMENT_REQUEST_CODE = 103;
    String PARSIAN = "parsian";
    PlateType plateType;
    String tag1;
    String tag2;
    String tag3;
    String tag4;
    int placeID;
    WebService webService = new WebService();

    Context context;
    Activity activity;
    ParsianPaymentCallBack parsianPaymentCallBack;
    FragmentManager fragmentManager;
    LoadingBar loadingBar;
    Assistant assistant;
    ViewGroup printArea;

    public ParsianPayment(ViewGroup printArea, Context context, Activity activity, ParsianPaymentCallBack parsianPaymentCallBack, FragmentManager fragmentManager) {
        this.context = context;
        this.parsianPaymentCallBack = parsianPaymentCallBack;
        this.fragmentManager = fragmentManager;
        this.activity = activity;
        this.printArea = printArea;
        loadingBar = new LoadingBar(activity);
//        sh_p = new SharedPreferencesRepository(context);
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

    public void handleResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAYMENT_REQUEST_CODE) {

            Transaction transaction;

            if (resultCode == Activity.RESULT_OK) {

                Bundle b = data.getBundleExtra("response");
                if (b != null) {

                    Log.d("bundle data", getBundleString(b));

                    int amount = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.AMOUNT, "0"));
                    String result = b.getString("result");
                    String pan = b.getString("pan");
                    String rrn = b.getString("rrn");
                    Long date = b.getLong("date");
                    String trace = b.getString("trace");
                    String errorMessage = b.getString("message", "");
                    Long res_num = b.getLong("res_num");
                    int status = result.equals("succeed") ? 1 : -1;

                    if (tag2 == null) tag2 = "null";
                    if (tag3 == null) tag3 = "null";
                    if (tag4 == null) tag4 = "null";

                    transaction = new Transaction(
                            Integer.toString(amount),
                            Long.toString(res_num),
                            (rrn == null || rrn.isEmpty()) ? "0" : rrn,
                            placeID,
                            status,
                            PARSIAN,
//                            result.equals("succeed") ? "1" : "-1",
                            result,
                            pan,
                            Long.toString(date),
                            trace,
                            result,
                            Assistant.getUnixTime());


                } else {

                    String amount = SharedPreferencesRepository.getValue(Constants.AMOUNT, "0");
                    String resNum = SharedPreferencesRepository.getValue(Constants.OUR_TOKEN, "0");
                    int placeID = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0"));
                    String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");

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

                String amount = SharedPreferencesRepository.getValue(Constants.AMOUNT, "0");
                String resNum = SharedPreferencesRepository.getValue(Constants.OUR_TOKEN, "0");
                int placeID = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0"));

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
                        "",
                        "not succeed",
                        Assistant.getUnixTime());


            }

            Gson gson = new Gson();
            SharedPreferencesRepository.updateTransactions02(transaction);
            verifyTransaction02(transaction);

//            parsianPaymentCallBack.verifyTransaction(transaction);


        } else if (requestCode == Constants.QR_SCANER_REQUEST_CODE) {

            if (data != null) {

                try {

                    String scannedData = data.getExtras().getString(Constants.QR_DATA);
                    int placeId = Integer.parseInt(scannedData.split("=")[scannedData.split("=").length - 1]);
                    parsianPaymentCallBack.getScannerData(placeId);

                } catch (Exception e) {
                    Toast.makeText(context, "معتبر نمیباشد", Toast.LENGTH_LONG).show();
                }

            }

        }

    }
    public static interface LoadingListener{
        public void onCreateTransactionFinished();
    }
    public void createTransaction(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID, int transactionType, LoadingListener loadingListener) {

        Runnable functionRunnable = () -> createTransaction(plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType, loadingListener);

        String t1 = tag1 == null ? "" : tag1;
        String t2 = tag2 == null ? "-1" : tag2;
        String t3 = tag3 == null ? "-1" : tag3;
        String t4 = tag4 == null ? "-1" : tag4;

        webService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4, amount, transactionType).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {

                if (loadingListener != null)
                    loadingListener.onCreateTransactionFinished();
                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = response.body().our_token;

                SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString());
                SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
                SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
                SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
                SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
                SharedPreferencesRepository.setValue(Constants.AMOUNT, String.valueOf(amount));
                SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID));
                SharedPreferencesRepository.setValue(Constants.OUR_TOKEN, Long.toString(our_token));

                Transaction transaction = new Transaction(
                        Integer.toString(amount),
                        Long.toString(our_token),
                        "0",
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0")),
                        0,
                        PARSIAN,
                        "0",
                        "",
                        "",
                        "",
                        "",
                        Assistant.getUnixTime());

                SharedPreferencesRepository.addToTransactions02(transaction);

                paymentRequest(amount, our_token, activity, plateType, tag1, tag2, tag3, tag4, placeID);


            }

            @Override
            public void onFailure(@NonNull Call<CreateTransactionResponse> call, @NonNull Throwable t) {
                if (loadingListener != null)
                    loadingListener.onCreateTransactionFinished();
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void createTransaction(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID, int transactionType) {

        Runnable functionRunnable = () -> createTransaction(plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType);

        String t1 = tag1 == null ? "" : tag1;
        String t2 = tag2 == null ? "-1" : tag2;
        String t3 = tag3 == null ? "-1" : tag3;
        String t4 = tag4 == null ? "-1" : tag4;

        webService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4, amount, transactionType).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = response.body().our_token;

                SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString());
                SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
                SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
                SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
                SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
                SharedPreferencesRepository.setValue(Constants.AMOUNT, String.valueOf(amount));
                SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID));
                SharedPreferencesRepository.setValue(Constants.OUR_TOKEN, Long.toString(our_token));

                Transaction transaction = new Transaction(
                        Integer.toString(amount),
                        Long.toString(our_token),
                        "0",
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0")),
                        0,
                        PARSIAN,
                        "0",
                        "",
                        "",
                        "",
                        "",
                        Assistant.getUnixTime());

                SharedPreferencesRepository.addToTransactions02(transaction);

                paymentRequest(amount, our_token, activity, plateType, tag1, tag2, tag3, tag4, placeID);


            }

            @Override
            public void onFailure(@NonNull Call<CreateTransactionResponse> call, @NonNull Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void verifyTransaction02(Transaction transaction) {

        Runnable functionRunnable = () -> verifyTransaction02(transaction);

        webService.getClient(context).verifyTransaction(SharedPreferencesRepository.getTokenWithPrefix(), transaction.getAmount(), transaction.getOur_token(),
                transaction.getBank_token(), transaction.getPlaceID(), transaction.getStatus(), transaction.getBank_type(), transaction.getState(),
                transaction.getCard_number(), transaction.getBank_datetime(), transaction.getTrace_number(), transaction.getResult_message()).enqueue(new Callback<VerifyTransactionResponse>() {
            @Override
            public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                SharedPreferencesRepository.removeFromTransactions02(transaction);
                Toast.makeText(context, response.body().getDescription(), Toast.LENGTH_SHORT).show();
                String tag1 = SharedPreferencesRepository.getValue(Constants.TAG1, "0");
                String tag2 = SharedPreferencesRepository.getValue(Constants.TAG2, "0");
                String tag3 = SharedPreferencesRepository.getValue(Constants.TAG3, "0");
                String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");

                getCarDebtHistory02(assistant.getPlateType(tag1, tag2, tag3, tag4), tag1, tag2, tag3, tag4, 0, 1);
                if (transaction.getStatus() == 1)
                    parsianPaymentCallBack.onVerifyFinished();

            }

            @Override
            public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    private void getCarDebtHistory02(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        Runnable functionRunnable = () -> getCarDebtHistory02(plateType, tag1, tag2, tag3, tag4, limit, offset);
        LoadingBar loadingBar = new LoadingBar(activity);
        loadingBar.show();

        webService.getClient(context).getCarDebtHistory(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4, limit, offset).enqueue(new Callback<DebtHistoryResponse>() {
            @Override
            public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                if (assistant.getPlateType(tag1, tag2, tag3, tag4) == PlateType.simple)
                    printFactor(tag1,
                            tag2,
                            tag3,
                            tag4, response.body().balance);
                else if (assistant.getPlateType(tag1, tag2, tag3, tag4) == PlateType.old_aras)
                    printFactor(tag1, "0", "0", "0", response.body().balance);
                else
                    printFactor(tag1, tag2, "0", "0", response.body().balance);


            }

            @Override
            public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------

    @SuppressLint("SetTextI18n")
    private void printFactor(String tag1, String tag2, String tag3, String tag4, int balance) {


        if (Constants.SELECTED_PAYMENT == Constants.SAMAN)

            printArea.removeAllViews();

        ParsianAfterPaymentPrintTemplateBinding printTemplateBinding = ParsianAfterPaymentPrintTemplateBinding.inflate(LayoutInflater.from(context), printArea, true);

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

        printTemplateBinding.text.setText("\n.");

        try {

            printArea.post(() -> {

                PrinterManager printer = new PrinterManager();
                int setupResult = printer.setupPage(-1, -1);
                printer.drawBitmap(getViewBitmap(printTemplateBinding.getRoot()), 0, 0);
                int printResult = printer.printPage(0);
            });


        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("----------> Exception  ");
        }

    }

    @SuppressLint("SetTextI18n")
    public void printParkInfo(Place place, int placeID, ViewGroup viewGroupForBindFactor, String pricing, String telephone, String sms_number,
                              String qr_url, int balance, String printDescription) {

        String cityID = SharedPreferencesRepository.getValue(Constants.CITY_ID);

        PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(context), viewGroupForBindFactor, true);

        if (balance > 0) {

            printTemplateBinding.description.setText("شهروند گرامی؛از این که جز مشتریان خوش حساب ما هستید سپاسگزاریم. " + printDescription);
            printTemplateBinding.balanceTitle.setText("اعتبار پلاک");

        } else if (balance < 0) {

            printTemplateBinding.description.setText("اخطار: شهروند گرامی؛بدهی پلاک شما بیش از حد مجاز میباشد در صورت عدم پرداخت بدهی مشمول جریمه پارک ممنوع خواهید شد. " + printDescription);
            printTemplateBinding.balanceTitle.setText("بدهی پلاک");

        } else {

            printTemplateBinding.description.setText("شهروند گرامی در صورت عدم پرداخت هزینه پارک مشمول جریمه پارک ممنوع خواهید شد. " + printDescription);
            printTemplateBinding.balanceTitle.setText("اعتبار پلاک");
        }

//        printTemplateBinding.debtArea.setVisibility(balance <= 0 ? View.VISIBLE : View.GONE);

        printTemplateBinding.placeId.setText(place.number + "");
        printTemplateBinding.debt.setText(balance + " تومان");

        printTemplateBinding.startTime.setText(assistant.toJalali(place.start));

        printTemplateBinding.prices.setText(pricing);
        printTemplateBinding.supportPhone.setText(telephone);

        printTemplateBinding.description2.setText(SharedPreferencesRepository.getValue(Constants.print_description_2,""));

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

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------

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

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------

    public interface ParsianPaymentCallBack {

        public void verifyTransaction(Transaction transaction);

        public void getScannerData(int placeID);

        public void onVerifyFinished();

    }

}
