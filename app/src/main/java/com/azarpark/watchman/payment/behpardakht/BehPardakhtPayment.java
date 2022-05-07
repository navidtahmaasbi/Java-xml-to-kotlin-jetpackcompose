package com.azarpark.watchman.payment.behpardakht;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.PaymentData;
import com.azarpark.watchman.models.PaymentResult;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.saman.SamanPayment;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;
import com.google.gson.Gson;
import com.yandex.metrica.impl.ob.Pa;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BehPardakhtPayment {

    private final int paymentRequestCode = 1001;
    private final Gson gson;
    private final Activity activity;
    private final Context context;

    private String BEH_PARDAKHT = "beh_pardakht";
    FragmentManager fragmentManager;
    public static int PAYMENT_REQUEST_CODE = 1003;
    BehPardakhtPaymentCallBack behPardakhtPaymentCallBack;

    public BehPardakhtPayment(Activity activity, Context context, FragmentManager fragmentManager, BehPardakhtPaymentCallBack behPardakhtPaymentCallBack) {
        this.activity = activity;
        this.context = context;
        this.behPardakhtPaymentCallBack = behPardakhtPaymentCallBack;
        this.fragmentManager = fragmentManager;
        gson = new Gson();
    }

    public void paymentRequest(PaymentData paymentData) {
        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", gson.toJson(paymentData));
        activity.startActivityForResult(intent, paymentRequestCode);
    }

    public void paymentRequest(PaymentData paymentData, View printTrailing) {
        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", gson.toJson(paymentData));
        if (printTrailing != null)
            intent.putExtra("bmp", buildBitmap(printTrailing));
        else
            System.out.println("----------> printTrailing is null");
        activity.startActivityForResult(intent, paymentRequestCode);
    }

    private static Bitmap convertLayout(View v) {
        v.setDrawingCacheEnabled(true);
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false); // clear drawing cache return b;
        return b;
    }

    private Bitmap buildBitmap(View v) {
        Bitmap originalBitmap = convertLayout(v);
        if (originalBitmap == null || originalBitmap.getWidth() == 0 || originalBitmap.getHeight() == 0) {
            return null;
        }
        return originalBitmap;
    }

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data, OnResultListener onResultListener) {

        if (requestCode != paymentRequestCode) {
            System.out.println("----------> This result is not for Behpardakht payment");
        } else if (resultCode != Activity.RESULT_OK) {
            System.out.println("----------> Behpardakht resultCode is " + resultCode);
        } else {
            PaymentResult paymentResult = gson.fromJson(data.getStringExtra("PaymentResult"), PaymentResult.class);
            onResultListener.onResult(paymentResult);
        }
    }

    public static interface OnResultListener {
        public void onResult(PaymentResult paymentResult);
    }

    //--------------------------------------------------------------------------------------------------------------------------

    public void createTransaction(String shaba, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID
            , int transactionType, LoadingListener loadingListener) {

        Runnable functionRunnable = () -> createTransaction(shaba, plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType, loadingListener);

        String t1 = tag1 == null ? "" : tag1;
        String t2 = tag2 == null ? "-1" : tag2;
        String t3 = tag3 == null ? "-1" : tag3;
        String t4 = tag4 == null ? "-1" : tag4;

        WebService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(),
                t1, t2, t3, t4, amount, transactionType).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {

                if (loadingListener != null)
                    loadingListener.onCreateTransactionFinished();
                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = response.body().our_token;

                Transaction transaction = new Transaction(
                        Integer.toString(amount),
                        Long.toString(our_token),
                        "0",
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0")),
                        0,
                        BEH_PARDAKHT,
                        "0",
                        "",
                        "",
                        "",
                        "",
                        Assistant.getUnixTime());

                SharedPreferencesRepository.addToTransactions02(transaction);

                SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString());
                SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
                SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
                SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
                SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
                SharedPreferencesRepository.setValue(Constants.AMOUNT, String.valueOf(amount));
                SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID));
                SharedPreferencesRepository.setValue(Constants.OUR_TOKEN, Long.toString(our_token));

                tashimPaymentRequest(shaba, Long.toString(our_token), (amount * 10));


            }

            @Override
            public void onFailure(@NonNull Call<CreateTransactionResponse> call, @NonNull Throwable t) {
                if (loadingListener != null)
                    loadingListener.onCreateTransactionFinished();
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void createTransaction(String shaba, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID, int transactionType) {

        Runnable functionRunnable = () -> createTransaction(shaba, plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType);

        String t1 = tag1 == null ? "" : tag1;
        String t2 = tag2 == null ? "-1" : tag2;
        String t3 = tag3 == null ? "-1" : tag3;
        String t4 = tag4 == null ? "-1" : tag4;

        WebService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4,
                amount, transactionType).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = response.body().our_token;

                Transaction transaction = new Transaction(
                        Integer.toString(amount),
                        Long.toString(our_token),
                        "0",
                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0")),
                        0,
                        BEH_PARDAKHT,
                        "0",
                        "",
                        "",
                        "",
                        "",
                        Assistant.getUnixTime());

                SharedPreferencesRepository.addToTransactions02(transaction);

                SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString());
                SharedPreferencesRepository.setValue(Constants.TAG1, tag1);
                SharedPreferencesRepository.setValue(Constants.TAG2, tag2);
                SharedPreferencesRepository.setValue(Constants.TAG3, tag3);
                SharedPreferencesRepository.setValue(Constants.TAG4, tag4);
                SharedPreferencesRepository.setValue(Constants.AMOUNT, String.valueOf(amount));
                SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID));
                SharedPreferencesRepository.setValue(Constants.OUR_TOKEN, Long.toString(our_token));

                tashimPaymentRequest(shaba, Long.toString(our_token), (amount * 10));


            }

            @Override
            public void onFailure(@NonNull Call<CreateTransactionResponse> call, @NonNull Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void tashimPaymentRequest(String shaba, String resNum, int amount) {

        System.out.println("----------> behpardakht tashimPaymentRequest ");

        String tashim = shaba + "," + amount + ",;";

        PaymentData paymentData = new PaymentData("1.0.0", resNum, 1000, Integer.toString(amount), tashim);

        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", gson.toJson(paymentData));
        activity.startActivityForResult(intent, paymentRequestCode);

    }

    public void verifyTransaction(Transaction transaction) {

        if (Assistant.checkIfVerifyIsPermittedNow()) {
            Assistant.updateLastVerifyRequestTime();
            Runnable functionRunnable = () -> verifyTransaction(transaction);

            WebService.getClient(context).verifyTransaction(SharedPreferencesRepository.getTokenWithPrefix(), transaction.getAmount(), transaction.getOur_token(),
                    transaction.getBank_token(), transaction.getPlaceID(), transaction.getStatus(), transaction.getBank_type(), transaction.getState(),
                    transaction.getCard_number(), transaction.getBank_datetime(), transaction.getTrace_number(), transaction.getResult_message()).enqueue(new Callback<VerifyTransactionResponse>() {
                @Override
                public void onResponse(Call<VerifyTransactionResponse> call, Response<VerifyTransactionResponse> response) {

                    if (NewErrorHandler.apiResponseHasError(response, context))
                        return;

                    SharedPreferencesRepository.removeFromTransactions02(transaction);
                    Toast.makeText(context, response.body().getDescription(), Toast.LENGTH_SHORT).show();
                    if (transaction.getStatus() == 1)
                        behPardakhtPaymentCallBack.onVerifyFinished();

                }

                @Override
                public void onFailure(Call<VerifyTransactionResponse> call, Throwable t) {
                    NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
                }
            });
        }
    }

    public void printParkInfo(ViewGroup viewGroupForBindFactor) {

//        connection.print(getViewBitmap(viewGroupForBindFactor));

    }

    //todo
//    public void handleResult(int requestCode, int resultCode, Intent data) {
//
//        System.out.println("----------> handleResult " + resultCode);
//
//        if (/*resultCode == Activity.RESULT_OK &&*/ requestCode == PAYMENT_REQUEST_CODE) {
//
//
//            int state = data.getIntExtra(STATE, -1);
//
//            System.out.println("----------> state : " + state);
//
//            if (state != 0)
//                Toast.makeText(context, state + "", Toast.LENGTH_LONG).show();
//
////            AdditionalData :
////            ++State : 55
////            ++RefNum : 819972850538
////            ++ResNum : 8019291329
////            ++Amount : 1000.0
////            ++Pan : 622106-fdaffd-8750
////            ++DateTime : 211009182155
////            ++TraceNumber : 598994
////            ++result : رمز اشتباه واردشده است
////            --TerminalId : 00002280
////            --AmountAffective : 1000.0
//
//
////        ++result : (succeed / unsucceed)
////        ++rrn : 801663199541
////        ++res_num : -1 (our_token)
////        ++amount : 000000001000
////        ++pan : 589210***2557
////        ++date : 23732049000
////        ++trace : 000015
////        ++message :    (this will have value if there is an error)
//
//            Transaction transaction;
//
//            if (state == STATE_SUCCESSFUL) // successful
//            {
////                Log.e("saman payment", "Purchase did successful....");
//
//
//                String tag1 = SharedPreferencesRepository.getValue(Constants.TAG1, "0");
//                String tag2 = SharedPreferencesRepository.getValue(Constants.TAG2, "0");
//                String tag3 = SharedPreferencesRepository.getValue(Constants.TAG3, "0");
//                String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");
//
//                if (tag2 == null) tag2 = "null";
//                if (tag3 == null) tag3 = "null";
//                if (tag4 == null) tag4 = "null";
//
//                int status = state == 0 ? 1 : -1;
//                String refNum = data.getExtras().getString("RefNum", "");
//                String resNum = data.getExtras().getString("ResNum", "");
//                String amount = data.getExtras().getString("Amount", "0");
//                String pan = data.getExtras().getString("Pan", "");
//                String dateTime = data.getExtras().getString("DateTime", "0");
//                String traceNumber = data.getExtras().getString("TraceNumber", "");
//                String result = data.getExtras().getString("result", "");
//
//
//                transaction = new Transaction(
//                        SharedPreferencesRepository.getValue(Constants.AMOUNT),
//                        resNum,
//                        refNum,
//                        Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID)),
//                        status,
//                        SAMAN,
//                        Integer.toString(state),
//                        pan,
//                        dateTime,
//                        traceNumber,
//                        result,
//                        Assistant.getUnixTime());
//
//
//            } else {
//
//
//                String result = data.getExtras().getString("result", "");
//
//                String amount = SharedPreferencesRepository.getValue(Constants.AMOUNT, "0");
//                String resNum = SharedPreferencesRepository.getValue(Constants.OUR_TOKEN, "0");
//                int placeID = Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0"));
//                String tag4 = SharedPreferencesRepository.getValue(Constants.TAG4, "0");
//
//                transaction = new Transaction(
//                        amount,
//                        resNum,
//                        "0",
//                        placeID,
//                        -1,
//                        SAMAN,
//                        Integer.toString(state),
//                        "****-****-****-****",
//                        "0",
//                        null,
//                        result,
//                        Assistant.getUnixTime());
//
//
////                if (!result.isEmpty())
////                    Toast.makeText(context, result, Toast.LENGTH_LONG).show();
//
//                Log.e("saman payment", "Purchase did failed....");
//                messageDialog = new MessageDialog("خطا ی " + state, result, "خروج", () -> {
//                    if (messageDialog != null)
//                        messageDialog.dismiss();
//                });
//
//                messageDialog.show(fragmentManager, MessageDialog.TAG);
//            }
//
//            SharedPreferencesRepository.updateTransactions02(transaction);
//
//            Gson gson = new Gson();
//
//            verifyTransaction(transaction);
//
//
//        } else if (resultCode == Activity.RESULT_OK && requestCode == QR_SCANNER_REQUEST_CODE) {
//
//            try {
//
//                String url = data.getStringExtra(SCANNER_RESULT);
//                System.out.println("----------> url : " + url);
//                int placeId = Integer.parseInt(url.split("=")[url.split("=").length - 1]);
//                samanPaymentCallBack.getScannerData(placeId);
//
//            } catch (Exception e) {
//                Toast.makeText(context, "معتبر نمیباشد", Toast.LENGTH_LONG).show();
//            }
//
//
//        }
//
//    }

    //--------------------------------------------------------------------------------------------------------------------------

    public interface BehPardakhtPaymentCallBack {

        public void verifyTransaction(Transaction transaction);

        public void getScannerData(int placeID);

        public void onVerifyFinished();
    }

    public static interface LoadingListener{
        public void onCreateTransactionFinished();
    }
}

