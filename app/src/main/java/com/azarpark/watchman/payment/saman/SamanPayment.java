package com.azarpark.watchman.payment.saman;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.CreateTransactionResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.google.gson.Gson;

import java.util.Set;

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
    public static int PAYMENT_REQUEST_CODE = 1003;
    int QR_SCANNER_REQUEST_CODE = 1004;
    public IProxy service;
    public MyServiceConnection connection;
    SamanPaymentCallBack samanPaymentCallBack;
    MessageDialog messageDialog;
//    LoadingBar loadingBar;
    FragmentManager fragmentManager;

    private String STATE = "State",
            REF_NUM = "RefNum",
            SAMAN = "saman",
            SCANNER_RESULT = "ScannerResult",
            RES_NUM = "ResNum";

    int STATE_SUCCESSFUL = 0;

    public SamanPayment(Context context, Activity activity, SamanPaymentCallBack samanPaymentCallBack) {

        sh_r = new SharedPreferencesRepository(context);
        this.activity = activity;
        this.context = context;
        this.samanPaymentCallBack = samanPaymentCallBack;

//        loadingBar = new LoadingBar(activity);

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

    public void paymentRequest(String resNum, int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {


        sh_r.saveString(SharedPreferencesRepository.PLATE_TYPE, plateType.toString());
        sh_r.saveString(SharedPreferencesRepository.TAG1, tag1);
        sh_r.saveString(SharedPreferencesRepository.TAG2, tag2);
        sh_r.saveString(SharedPreferencesRepository.TAG3, tag3);
        sh_r.saveString(SharedPreferencesRepository.TAG4, tag4);
        sh_r.saveString(SharedPreferencesRepository.TAG4, tag4);
        sh_r.saveString(SharedPreferencesRepository.AMOUNT, String.valueOf(amount));
        sh_r.saveString(SharedPreferencesRepository.PLACE_ID, Integer.toString(placeID));
        sh_r.saveString(SharedPreferencesRepository.OUR_TOKEN, resNum);

        Intent intent = new Intent();
        intent.putExtra("TransType", 1);
        intent.putExtra("Amount", String.valueOf(amount));
        intent.putExtra("ResNum", resNum);
        intent.putExtra("AppId", "0");

//        for (String key:intent.getExtras().keySet()) {
        Log.d("-----> TransType", String.valueOf(intent.getExtras().getInt("TransType")));
        Log.d("-----> Amount", intent.getExtras().getString("Amount"));
        Log.d("-----> ResNum", intent.getExtras().getString("ResNum"));
        Log.d("-----> AppId", intent.getExtras().getString("AppId"));
//        }

        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));

        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void tashimPaymentRequest(String shaba, String resNum, int amount, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {


        sh_r.saveString(SharedPreferencesRepository.PLATE_TYPE, plateType.toString());
        sh_r.saveString(SharedPreferencesRepository.TAG1, tag1);
        sh_r.saveString(SharedPreferencesRepository.TAG2, tag2);
        sh_r.saveString(SharedPreferencesRepository.TAG3, tag3);
        sh_r.saveString(SharedPreferencesRepository.TAG4, tag4);
        sh_r.saveString(SharedPreferencesRepository.TAG4, tag4);
        sh_r.saveString(SharedPreferencesRepository.AMOUNT, String.valueOf(amount));
        sh_r.saveString(SharedPreferencesRepository.PLACE_ID, Integer.toString(placeID));
        sh_r.saveString(SharedPreferencesRepository.OUR_TOKEN, resNum);

        String[] param = new String[1];
        param[0] = shaba;


        Intent intent = new Intent();
        intent.putExtra("TransType", 3);
        intent.putExtra("Amount", String.valueOf(amount));
        intent.putExtra("ResNum", resNum);
        intent.putExtra("AppId", "0");
        intent.putExtra("Tashim", param);


//        for (String key:intent.getExtras().keySet()) {
        Log.d("-----> TransType", String.valueOf(intent.getExtras().getInt("TransType")));
        Log.d("-----> Amount", intent.getExtras().getString("Amount"));
        Log.d("-----> ResNum", intent.getExtras().getString("ResNum"));
        Log.d("-----> AppId", intent.getExtras().getString("AppId"));
//        }


        intent.setComponent(new ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity"));


        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void createTransaction(String shaba, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(context);
        RetrofitAPIRepository repository = new RetrofitAPIRepository(context);
//        loadingBar.show();

        repository.createTransaction("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType,
                tag1,
                tag2,
                tag3,
                tag4,
                amount,
                new Callback<CreateTransactionResponse>() {
                    @Override
                    public void onResponse(Call<CreateTransactionResponse> call, Response<CreateTransactionResponse> response) {


//                        loadingBar.dismiss();
                        if (response.isSuccessful()) {

                            long our_token = response.body().our_token;

                            Transaction transaction = new Transaction(
                                    Integer.toString(amount),
                                    Long.toString(our_token),
                                    "0",
                                    Integer.parseInt(sh_r.getString(SharedPreferencesRepository.PLACE_ID,"0")),
                                    0,
                                    SAMAN,
                                    "0",
                                    "",
                                    "",
                                    "",
                                    "",
                                    Assistant.getUnixTime());

                            sh_r.addToTransactions(transaction);

//                            paymentRequest(Long.toString(our_token), amount, plateType, tag1, tag2, tag3, tag4, placeID);
                            tashimPaymentRequest("0:" + (amount * 10) + ":" + shaba, Long.toString(our_token), (amount * 10), plateType, tag1, tag2, tag3, tag4, placeID);

                        } else
                            APIErrorHandler.orResponseErrorHandler(fragmentManager, activity, response, () -> createTransaction(shaba, plateType, tag1, tag2, tag3, tag4, amount, placeID));
                    }

                    @Override
                    public void onFailure(Call<CreateTransactionResponse> call, Throwable t) {
//                        loadingBar.dismiss();
                        t.printStackTrace();
                        APIErrorHandler.onFailureErrorHandler(fragmentManager, t, () -> createTransaction(shaba, plateType, tag1, tag2, tag3, tag4, amount, placeID));
                    }
                });

    }

    public void handleResult(int requestCode, int resultCode, Intent data) {


        if (resultCode == Activity.RESULT_OK && requestCode == PAYMENT_REQUEST_CODE) {

            int state = data.getIntExtra(STATE, -1);

//            AdditionalData :
//            ++State : 55
//            ++RefNum : 819972850538
//            ++ResNum : 8019291329
//            ++Amount : 1000.0
//            ++Pan : 622106-fdaffd-8750
//            ++DateTime : 211009182155
//            ++TraceNumber : 598994
//            ++result : رمز اشتباه واردشده است
//            --TerminalId : 00002280
//            --AmountAffective : 1000.0


//        ++result : (succeed / unsucceed)
//        ++rrn : 801663199541
//        ++res_num : -1 (our_token)
//        ++amount : 000000001000
//        ++pan : 589210***2557
//        ++date : 23732049000
//        ++trace : 000015
//        ++message :    (this will have value if there is an error)

            Transaction transaction;

            if (state == STATE_SUCCESSFUL) // successful
            {
                Log.e("saman payment", "Purchase did successful....");

                String tag1 = sh_r.getString(SharedPreferencesRepository.TAG1, "0");
                String tag2 = sh_r.getString(SharedPreferencesRepository.TAG2, "0");
                String tag3 = sh_r.getString(SharedPreferencesRepository.TAG3, "0");
                String tag4 = sh_r.getString(SharedPreferencesRepository.TAG4, "0");

                if (tag2 == null) tag2 = "null";
                if (tag3 == null) tag3 = "null";
                if (tag4 == null) tag4 = "null";

                int status = state == 0 ? 1 : -1;
                String refNum = data.getExtras().getString("RefNum", "");
                String resNum = data.getExtras().getString("ResNum", "");
                String amount = data.getExtras().getString("Amount", "0");
                String pan = data.getExtras().getString("Pan", "");
                String dateTime = data.getExtras().getString("DateTime", "0");
                String traceNumber = data.getExtras().getString("TraceNumber", "");
                String result = data.getExtras().getString("result", "");


                transaction = new Transaction(
                        amount,
                        resNum,
                        refNum,
                        Integer.parseInt(sh_r.getString(SharedPreferencesRepository.PLACE_ID)),
                        status,
                        SAMAN,
                        Integer.toString(state),
                        pan,
                        dateTime,
                        traceNumber,
                        result,
                        Assistant.getUnixTime());


            } else {


                String result = data.getExtras().getString("result", "");

                String amount = sh_r.getString(SharedPreferencesRepository.AMOUNT, "0");
                String resNum = sh_r.getString(SharedPreferencesRepository.OUR_TOKEN, "0");
                int placeID = Integer.parseInt(sh_r.getString(SharedPreferencesRepository.PLACE_ID, "0"));
                String tag4 = sh_r.getString(SharedPreferencesRepository.TAG4, "0");

                System.out.println("----------> hereeeee");

                transaction = new Transaction(
                        amount,
                        resNum,
                        "0",
                        placeID,
                        -1,
                        SAMAN,
                        Integer.toString(state),
                        "****-****-****-****",
                        "0",
                        null,
                        result,
                        Assistant.getUnixTime());

                Gson gson = new Gson();

                System.out.println("----------> transaction : " +gson.toJson(transaction));

                if (!result.isEmpty())
                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();

                Log.e("saman payment", "Purchase did failed....");
                messageDialog = new MessageDialog("خطا ی" + state, "خطایی رخ داده است", "خروج", () -> {
                    if (messageDialog != null)
                        messageDialog.dismiss();
                });

                messageDialog.show(fragmentManager, MessageDialog.TAG);
            }

            sh_r.updateTransactions(transaction);

            samanPaymentCallBack.verifyTransaction(
                    transaction
            );


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

    public void printParkInfo(String startTime, String tag1, String tag2, String tag3, String tag4,
                              int placeID, ViewGroup viewGroupForBindFactor, String pricing, String telephone, String sms_number,
                              String qr_url, int debt, int balance) {

//        SamanPrintTemplateBinding printTemplateBinding = SamanPrintTemplateBinding.inflate(LayoutInflater.from(context), viewGroupForBindFactor, true);
//
//        if (balance > 0) {
//
//            printTemplateBinding.description2.setText("شهروند گرامی؛از این که جز مشتریان خوش حساب ما هستید سپاسگزاریم");
//
//        } else if (balance < 0) {
//
//            printTemplateBinding.description2.setText("اخطار: شهروند گرامی؛بدهی پلاک شما بیش از حد مجاز میباشد در صورت عدم پرداخت بدهی مشمول جریمه پارک ممنوع خواهید شد");
//
//        } else {
//
//            printTemplateBinding.description2.setText("شهروند گرامی در صورت عدم پرداخت هزینه پارک مشمول جریمه پارک ممنوع خواهید شد");
//
//        }
//
//        printTemplateBinding.debtArea.setVisibility(balance <= 0 ? View.VISIBLE : View.GONE);
//
//        printTemplateBinding.placeId.setText(placeID + "");
//        printTemplateBinding.debt.setText(debt + " تومان");
//
//
//        printTemplateBinding.startTime.setText(startTime);
//
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

        connection.print(getViewBitmap(viewGroupForBindFactor));


    }

    public void printParkInfo(ViewGroup viewGroupForBindFactor) {


        connection.print(getViewBitmap(viewGroupForBindFactor));


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

    public interface SamanPaymentCallBack {

        public void verifyTransaction(Transaction transaction);

        public void getScannerData(int placeID);
    }

    public void releaseService() {
        activity.unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
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
            if (key.equals("State"))
                sb.append(b.getInt(key));
            else
                sb.append(b.getString(key));
            sb.append("\n");
        }

        return sb.toString();

    }


}
