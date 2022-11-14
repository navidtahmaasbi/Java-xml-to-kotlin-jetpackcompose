package com.azarpark.watchman.detection;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Auto_Analog_Meter_Recog {
    private String TAG = "Auto_Analog_Meter_Recog";
    Activity activity;
    private boolean all_model_isloaded;
    private Context context;

    private Detection_Analog_Ocr_TF detection_ocr_tf = null;

    private AOCR_Analog_TF numerator_ocr = null;
    ArrayList<String> numerator_str;
    private float prob;
    private String type_str;
    private List<String> valid_code;
    /* access modifiers changed from: private */
    public double wayLatitude = 0.0d;
    /* access modifiers changed from: private */
    public double wayLongitude = 0.0d;

    public Auto_Analog_Meter_Recog(Activity activity2, Context context2) {
        this.activity = activity2;
        this.all_model_isloaded = false;
        this.context = context2;


    }

    public void initilized(int i, int i2, int i3) {
        this.numerator_str = new ArrayList<>();
        this.all_model_isloaded = true;
        try {
            this.detection_ocr_tf = new Detection_Analog_Ocr_TF(this.activity, i2, i3, i, context);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(this.TAG, "Detection model doest not load");
            this.all_model_isloaded = false;
        }
        try {
//            this.numerator_ocr = new AOCR_Analog_TF(this.activity, Integer.parseInt(CplusConfig.getInstance().Get_Config("OcrtHeight")), Integer.parseInt(CplusConfig.getInstance().Get_Config("OcrWidth")), Integer.parseInt(CplusConfig.getInstance().Get_Config("decode_length")), i + 1, CplusConfig.getInstance().Get_Config("code"), false);
            int OcrtHeight = 48;//?
            int OcrWidth = 184;
            int decode_length = 8;//?
            //String code = "0123456789DSabcdefhijlmnoqstvwypuzx";
            String code = "00123456789DSabcdefhijlmnoqstvwypuzx";
            this.numerator_ocr = new AOCR_Analog_TF(this.activity, OcrtHeight, OcrWidth, decode_length, i + 1, code, false , context);
        } catch (IOException e2) {
            e2.printStackTrace();
            Log.e(this.TAG, "Numerator Ocr  doest not load");
            this.all_model_isloaded = false;
        }
    }

    public boolean reconized(Mat mat) {
        if (!this.all_model_isloaded) {
            return false;
        }
        this.type_str = "";
        Imgproc.cvtColor(mat, mat, 4);
        Detection_Analog_Ocr_TF detection_Analog_Ocr_TF = this.detection_ocr_tf;
        if (detection_Analog_Ocr_TF != null) {
            detection_Analog_Ocr_TF.recognize(mat);
            if (this.detection_ocr_tf.is_run_successful()) {
                AOCR_Analog_TF aOCR_Analog_TF = this.numerator_ocr;
                if (aOCR_Analog_TF != null) {
                    this.numerator_str = aOCR_Analog_TF.recongnized(this.detection_ocr_tf.get_numerator());
                }
                if (this.numerator_str.size() > 0) {
                    return true;
                }
                Log.e(this.TAG, "ocr faild");
                this.prob = 0.0f;
                return false;
            }
            Log.e(this.TAG, "model is not detected");
        }
        return false;
    }

    public void close_all() {
        Detection_Analog_Ocr_TF detection_Analog_Ocr_TF = this.detection_ocr_tf;
        if (detection_Analog_Ocr_TF != null) {
            detection_Analog_Ocr_TF.close();
        }
        AOCR_Analog_TF aOCR_Analog_TF = this.numerator_ocr;
        if (aOCR_Analog_TF != null) {
            aOCR_Analog_TF.close();
        }
        this.detection_ocr_tf = null;
        this.numerator_ocr = null;
    }

    public boolean are_models_initilized() {
        return this.all_model_isloaded;
    }

    public float getProb() {
        return 1.0f / (this.prob + 1.0f);
    }

    public String getType_str() {
        return this.type_str;
    }

    public double getLatitude() {
        return this.wayLatitude;
    }

    public double getLongitude() {
        return this.wayLongitude;
    }

    public ArrayList<String> getNumerator_str() {
        return this.numerator_str;
    }


}
