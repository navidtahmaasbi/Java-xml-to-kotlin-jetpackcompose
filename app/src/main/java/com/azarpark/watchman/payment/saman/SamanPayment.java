package com.azarpark.watchman.payment.saman;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.azarpark.watchman.databinding.PrintTemplateBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.VerifyTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.util.UUID;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;
import ir.sep.android.Service.IProxy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SamanPayment {

    SharedPreferencesRepository sh_r;
    Activity activity;
    Context context;
    int PAYMENT_REQUEST_CODE = 1003;
    int QR_SCANNER_REQUEST_CODE = 1004;
    IProxy service;
    MyServiceConnection connection;
    SamanPaymentCallBack samanPaymentCallBack;

    private String STATE = "State",
            REF_NUM = "RefNum",
            SCANNER_RESULT = "ScannerResult",
            RES_NUM = "ResNum";

    int STATE_SUCCESSFUL = 0;

    public SamanPayment(Context context, Activity activity, SamanPaymentCallBack samanPaymentCallBack) {

        sh_r = new SharedPreferencesRepository(context);
        this.activity = activity;
        this.context = context;
        this.samanPaymentCallBack = samanPaymentCallBack;

        initService();

    }

    private void initService() {

        Log.i("TAG", "initService()");
        connection = new MyServiceConnection(service);
        Intent i = new Intent();
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy");
        boolean ret = activity.bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i("TAG", "initService() bound value: " + ret);
    }

    public void paymentRequest(String resNum,int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {

        amount *= 10;

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
        intent.putExtra("ResNum", resNum);
        intent.putExtra("AppId", "0");

//        for (String key:intent.getExtras().keySet()) {
            Log.d("-----> TransType" , String.valueOf(intent.getExtras().getInt("TransType")));
            Log.d("-----> Amount" , intent.getExtras().getString("Amount"));
            Log.d("-----> ResNum" , intent.getExtras().getString("ResNum"));
            Log.d("-----> AppId" , intent.getExtras().getString("AppId"));
//        }

        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));

        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void handleResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK && requestCode == PAYMENT_REQUEST_CODE) {

            int state = data.getIntExtra(STATE, -1);

            String refNum = data.getStringExtra(REF_NUM);
            String resNum = data.getStringExtra(RES_NUM);

            sh_r.saveString(SharedPreferencesRepository.REF_NUM, refNum);

            if (state == STATE_SUCCESSFUL) // successful
            {
                Log.e("saman payment", "Purchase did successful....");

                samanPaymentCallBack.verifyTransaction(
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
                Log.e("saman payment", "Purchase did failed....");

        } else if (resultCode == Activity.RESULT_OK && requestCode == QR_SCANNER_REQUEST_CODE) {

            String url = data.getStringExtra(SCANNER_RESULT);
            int placeId = Integer.parseInt(url.split("=")[url.split("=").length - 1]);

            //add listener and call here
//            Place place = adapter.getItemWithID(placeId);
//            if (place != null)
//                openParkInfoDialog(place);
//            else {
//
//                confirmDialog = new ConfirmDialog("درخواست خروج", " آیا برای درخواست خروج اطمینان دارید؟", "بله", "خیر", new ConfirmDialog.ConfirmButtonClicks() {
//                    @Override
//                    public void onConfirmClicked() {
//
//                        exitPark(placeId);
//
//                    }
//
//                    @Override
//                    public void onCancelClicked() {
//
//                        confirmDialog.dismiss();
//
//                    }
//                });
//
//                confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);
//
//            }

        }

    }

    public void printParkInfo (String startTime, String tag1, String tag2, String tag3, String tag4,
                               int placeID, ViewGroup viewGroupForBindFactor,String pricing,String telephone,String sms_number,
                               String qr_url){


        PrintTemplateBinding printTemplateBinding = PrintTemplateBinding.inflate(LayoutInflater.from(context), viewGroupForBindFactor, true);

        printTemplateBinding.placeId.setText(placeID + "");

//                String time = startTime.split(" ")[]

        printTemplateBinding.startTime.setText(startTime);
        printTemplateBinding.prices.setText(pricing);
        printTemplateBinding.supportPhone.setText(telephone);
        printTemplateBinding.description.setText("در صورت عدم حضور پارکیار عدد " + placeID + " را به شماره " + sms_number + " ارسال کنید");

        printTemplateBinding.qrcode.setImageBitmap(QRGenerator(qr_url + placeID));

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

        connection.print(getViewBitmap(printTemplateBinding.getRoot()));


    }

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

    public Bitmap QRGenerator(String value) {

        // Initializing the QR Encoder with your value to be encoded, type you required and Dimension
        QRGEncoder qrgEncoder = new QRGEncoder(value, null, QRGContents.Type.TEXT, 512);
        qrgEncoder.setColorBlack(Color.BLACK);
        qrgEncoder.setColorWhite(Color.WHITE);
        Bitmap bitmap = qrgEncoder.getBitmap();

        return bitmap;


    }

    public interface SamanPaymentCallBack{

        public void verifyTransaction(PlateType plateType, String tag1, String tag2, String tag3, String tag4, String amount, String transaction_id, int placeID);


    }

    public void releaseService() {
        activity.unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
    }



}
