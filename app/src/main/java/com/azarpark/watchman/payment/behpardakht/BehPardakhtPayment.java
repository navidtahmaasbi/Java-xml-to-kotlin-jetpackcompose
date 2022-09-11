package com.azarpark.watchman.payment.behpardakht;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.PaymentData;
import com.azarpark.watchman.models.PaymentResult;
import com.azarpark.watchman.models.Transaction;
import com.azarpark.watchman.payment.behpardakht.device.Device;
import com.azarpark.watchman.payment.behpardakht.device.IPosPrinterEvent;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse;
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse;
import com.google.gson.Gson;
import com.pax.gl.page.IPage;
import com.pax.gl.page.PaxGLPage;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class BehPardakhtPayment {

    private final String versionName = "1.0.0";
    int applicationId = 0; //given from beh pardakht
    private final Gson gson;
    private final Activity activity;
    private final Context context;
    WebService webService = new WebService();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static Device device;

    private final String BEH_PARDAKHT = "beh_pardakht";
    FragmentManager fragmentManager;
    public static int PAYMENT_REQUEST_CODE = 105;
    BehPardakhtPaymentCallBack behPardakhtPaymentCallBack;

    public BehPardakhtPayment(Activity activity,
                              Context context,
                              FragmentManager fragmentManager,
                              BehPardakhtPaymentCallBack behPardakhtPaymentCallBack) {
        this.activity = activity;
        this.context = context;
        this.behPardakhtPaymentCallBack = behPardakhtPaymentCallBack;
        this.fragmentManager = fragmentManager;
        gson = new Gson();
        device = Device.getInstance(context);
    }

    public void verifyTransaction(Transaction transaction) {

        if (Assistant.checkIfVerifyIsPermittedNow()) {
            Assistant.updateLastVerifyRequestTime();
            Runnable functionRunnable = () -> verifyTransaction(transaction);

            webService.getClient(context).verifyTransaction(SharedPreferencesRepository.getTokenWithPrefix(), transaction.getAmount(), transaction.getOur_token(),
                    transaction.getBank_token(), transaction.getPlaceID(), transaction.getStatus(), transaction.getBank_type(), transaction.getState(),
                    transaction.getCard_number(), transaction.getBank_datetime(), transaction.getTrace_number(), transaction.getResult_message()).enqueue(new Callback<VerifyTransactionResponse>() {
                @Override
                public void onResponse(@NonNull Call<VerifyTransactionResponse> call, @NonNull Response<VerifyTransactionResponse> response) {

                    System.out.println("---------> rerereer : "  +gson.toJson(response.body()));

                    if (NewErrorHandler.apiResponseHasError(response, context))
                        return;

                    SharedPreferencesRepository.removeFromTransactions02(transaction);
                    if (response.body() != null)
                        Toast.makeText(context, response.body().getDescription(), Toast.LENGTH_SHORT).show();
                    if (transaction.getStatus() == 1)
                        behPardakhtPaymentCallBack.onVerifyFinished();

                }

                @Override
                public void onFailure(@NonNull Call<VerifyTransactionResponse> call, @NonNull Throwable t) {
                    NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
                }
            });
        }
    }


    public void createTransaction(String shaba, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int amount, int placeID
            , int transactionType, LoadingListener loadingListener) {

        Runnable functionRunnable = () -> createTransaction(shaba, plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType, loadingListener);

        String t1 = tag1 == null ? "" : tag1;
        String t2 = tag2 == null ? "-1" : tag2;
        String t3 = tag3 == null ? "-1" : tag3;
        String t4 = tag4 == null ? "-1" : tag4;

        webService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(),
                t1, t2, t3, t4, amount, transactionType).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {

                if (loadingListener != null)
                    loadingListener.onCreateTransactionFinished();
                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = 0;
                if (response.body() != null)
                    our_token = response.body().our_token;

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

                String[] extras = new String[1];
                extras[0] = Integer.toString(placeID);

                paymentRequest(new PaymentData(versionName, Long.toString(our_token), applicationId, Integer.toString(amount * 10), shaba, extras));

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

        webService.getClient(context).createTransaction(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4,
                amount, transactionType).enqueue(new Callback<CreateTransactionResponse>() {
            @Override
            public void onResponse(@NonNull Call<CreateTransactionResponse> call, @NonNull Response<CreateTransactionResponse> response) {

                if (NewErrorHandler.apiResponseHasError(response, context))
                    return;

                long our_token = 0;
                if (response.body() != null)
                    our_token = response.body().our_token;

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
                String[] extras = new String[1];
                extras[0] = Integer.toString(placeID);

                paymentRequest(new PaymentData(versionName, Long.toString(our_token), applicationId, Integer.toString(amount * 10), shaba, extras));


            }

            @Override
            public void onFailure(@NonNull Call<CreateTransactionResponse> call, @NonNull Throwable t) {
                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable);
            }
        });

    }

    public void paymentRequest(PaymentData paymentData) {
        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", gson.toJson(paymentData));
        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    public void paymentRequest(PaymentData paymentData, View printTrailing) {
        Intent intent = new Intent("com.bpmellat.merchant");
        intent.putExtra("PaymentData", gson.toJson(paymentData));
        if (printTrailing != null)
            intent.putExtra("bmp", buildBitmap(printTrailing));
        else
            System.out.println("----------> printTrailing is null");
        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);
    }

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Constants.QR_SCANER_REQUEST_CODE && resultCode ==  PAYMENT_REQUEST_CODE && data != null){
                try {

                    String scannedData = data.getExtras().getString(Constants.QR_DATA);
                    int placeId = Integer.parseInt(scannedData.split("=")[scannedData.split("=").length - 1]);
                    behPardakhtPaymentCallBack.getScannerData(placeId);

                } catch (Exception e) {
                    Toast.makeText(context, "معتبر نمیباشد", Toast.LENGTH_LONG).show();
                }

        }

        if (requestCode != PAYMENT_REQUEST_CODE) {
            System.out.println("----------> This result is not for Behpardakht payment");
        } else if (resultCode != Activity.RESULT_OK) {
            System.out.println("----------> Behpardakht resultCode is " + resultCode);
        } else {
            PaymentResult paymentResult = gson.fromJson(data.getStringExtra("PaymentResult"), PaymentResult.class);

            System.out.println("---------> resss : " + data.getStringExtra("PaymentResult"));
            Transaction transaction = new Transaction(
                    Integer.toString(Integer.parseInt(paymentResult.transactionAmount) / 10),
                    paymentResult.sessionId,
                    paymentResult.referenceID,
                    Integer.parseInt(SharedPreferencesRepository.getValue(Constants.PLACE_ID, "-1")),
                    paymentResult.resultCode.equals("000") ? 1 : 0,
                    BEH_PARDAKHT,
                    paymentResult.resultDescription == null?"":paymentResult.resultDescription,
                    paymentResult.maskedCardNumber,
                    paymentResult.dateOfTransaction,
                    paymentResult.referenceID,
                    paymentResult.resultDescription,
                    paymentResult.dateOfTransaction
            );
            verifyTransaction(transaction);
        }
    }


    public static Bitmap getViewBitmap02(View view) {


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

    private Bitmap buildBitmap(View v) {

        v.setDrawingCacheEnabled(true);
        v.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        v.layout(0, 0, v.getMeasuredWidth(), v.getMeasuredHeight());
        v.buildDrawingCache(true);
        Bitmap originalBitmap = Bitmap.createBitmap(v.getDrawingCache());
        v.setDrawingCacheEnabled(false); // clear drawing cache return b;
        if (originalBitmap == null || originalBitmap.getWidth() == 0 || originalBitmap.getHeight() == 0) {
            return null;
        }
        return originalBitmap;
    }

    public static Bitmap getViewBitmap(View v) {

        Bitmap b = Bitmap.createBitmap(300, 500, Bitmap.Config.ARGB_8888);

        Canvas c = new Canvas(b);
        v.layout(0, 0, v.getLayoutParams().width, v.getLayoutParams().height);
        v.draw(c);
        return b;

    }

    public interface BehPardakhtPaymentCallBack {

        void verifyTransaction(Transaction transaction);

        void getScannerData(int placeID);

        void onVerifyFinished();
    }

    public interface LoadingListener {
        void onCreateTransactionFinished();
    }

    public Bitmap generateBitmap(View view)  {
        PaxGLPage iPaxGLPage = PaxGLPage.getInstance(context);
        IPage page = iPaxGLPage.createPage();

        page.addLine().addUnit(buildBitmap(view), IPage.EAlign.CENTER);

        int width = 384;
        Bitmap bitmap = iPaxGLPage.pageToBitmap(page, width);
        return bitmap;
    }

    public void print(View view){

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                view.getHeight(); //height is ready

                Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(returnedBitmap);
                Drawable bgDrawable =view.getBackground();
                if (bgDrawable!=null) {
                    bgDrawable.draw(canvas);
                }   else{
                    canvas.drawColor(Color.WHITE);
                }
                view.draw(canvas);

                new Printer(returnedBitmap).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });


    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            bgDrawable.draw(canvas);
        }   else{
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }

    private class Printer extends AsyncTask {

        Bitmap bitmap;

        Printer(Bitmap bitmap){
            this.bitmap = bitmap;
        }

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                device.print(bitmap, new IPosPrinterEvent() {
                    @Override
                    public void onPrintStarted() {

                    }

                    @Override
                    public void onPrinterError(String error, boolean isPaperError) {
                        System.out.println("---------> error : "+error);
                    }

                    @Override
                    public void onPrintEnd() {

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }




    public Bitmap decodeResource(Resources res, int resId, int dstWidth, int dstHeight)
    {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inJustDecodeBounds = false;

        options.inSampleSize = calculateSampleSize(options.outWidth, options.outHeight, dstWidth,
                dstHeight);

        options = new BitmapFactory.Options();
        //May use null here as well. The funciton may interpret the pre-used options variable in ways hard to tell.
        Bitmap unscaledBitmap = BitmapFactory.decodeResource(res, resId, options);

        if(unscaledBitmap == null)
        {
            Log.e("ERR","Failed to decode resource - " + resId + " " + res.toString());
            return null;
        }

        return unscaledBitmap;
    }

    public static int calculateSampleSize(int srcWidth, int srcHeight, int dstWidth, int dstHeight)
    {
        final float srcAspect = (float)srcWidth / (float)srcHeight;
        final float dstAspect = (float)dstWidth / (float)dstHeight;

        if (srcAspect > dstAspect)
        {
            return srcWidth / dstWidth;
        }
        else
        {
            return srcHeight / dstHeight;
        }
    }

}

