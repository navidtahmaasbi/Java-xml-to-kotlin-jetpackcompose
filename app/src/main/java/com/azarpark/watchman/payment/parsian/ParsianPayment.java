package com.azarpark.watchman.payment.parsian;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.azarpark.watchman.databinding.PrintTemplateBinding;
import com.azarpark.watchman.enums.PlateType;

import java.util.Set;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class ParsianPayment {

    public static int PAYMENT_REQUEST_CODE = 103;
    String PLATE_TYPE = "plate_type";
    String TAG1 = "tag1";
    String TAG2 = "tag2";
    String TAG3 = "tag3";
    String TAG4 = "tag4";
    String PLACE_ID = "place_id";

    Context context;

    public ParsianPayment(Context context) {
        this.context = context;
    }

    public void paymentRequest(int amount, String res_num, Activity activity, PlateType plateType, String tag1, String tag2, String tag3, String tag4, int placeID) {

        Intent intent = new Intent("ir.totan.pos.view.cart.TXN");
        intent.putExtra("type", 3);
        intent.putExtra("amount", "1000");
        intent.putExtra("res_num", res_num);
        intent.putExtra(PLATE_TYPE, plateType.toString());
        intent.putExtra(TAG1, tag1);
        intent.putExtra(TAG2, tag2);
        intent.putExtra(TAG3, tag3);
        intent.putExtra(TAG4, tag4);
        intent.putExtra(PLACE_ID, placeID);
        activity.startActivityForResult(intent, PAYMENT_REQUEST_CODE);

    }

    public void handleResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PAYMENT_REQUEST_CODE) {

            if (resultCode == Activity.RESULT_OK) {

                Bundle b = data.getBundleExtra("response");
                Log.d("bundle data", getBundleString(b));

            } else if (resultCode == Activity.RESULT_CANCELED)
                Log.d("Parsian Payment", "result canceled");
            else
                Log.d("Parsian Payment", "unknown result : " + resultCode);

        } else
            Log.d("Parsian Payment", "result is not for parsaian payment");

    }



    private String getBundleString(Bundle b) {

//        sample
//        amount : 000000001000
//        result : succeed
//        pan : 589210***2557
//        rrn : 801663199541
//        date : 23732049000
//        trace : 000015
//        message :          this will have value if there is an error
//        res_num : -1
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
            sb.append("\n");
        }

        return sb.toString();

    }



    public void printParkInfo (String startTime, String tag1, String tag2, String tag3, String tag4,
                               int placeID, ViewGroup viewGroupForBindFactor, String pricing, String telephone, String sms_number,
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

//        connection.print(getViewBitmap(printTemplateBinding.getRoot()));


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

}
