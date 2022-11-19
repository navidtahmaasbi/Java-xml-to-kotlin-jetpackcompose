package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.LinearLayoutCompat;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.detection.Auto_Analog_Meter_Recog;
import com.azarpark.watchman.detection.CaptureActivity;
import com.azarpark.watchman.detection.Dictionary_char;
import com.azarpark.watchman.detection.MyCameraView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class DetectActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, Runnable {

    static {
        if (!OpenCVLoader.initDebug()) {
            Log.d("ERROR", "Unable to load OpenCV");
        } else {
            Log.d("SUCCESS", "OpenCV loaded");
        }
    }

    private final BaseLoaderCallback baseLoaderCallback = new BaseLoaderCallback(this) {
        public void onManagerConnected(int i) {
            if (i == 0) {
                mOpenCvCameraView.enableView();
                return;
            }
            super.onManagerConnected(i);
        }
    };
    public Auto_Analog_Meter_Recog auto_analog_meter_recog;
    private MyCameraView mOpenCvCameraView;

    private int boxHeight;
    private int boxWidth;
    private Rect crop_rect;
    private Mat img;
    private Mat img_mask;
    private Mat mask;
    private AtomicBoolean is_run;
    private Mat crop_img_test;
    private Dictionary_char dictionary_char;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detect);

        mOpenCvCameraView = findViewById(R.id.my_camera_view);
        mOpenCvCameraView.setLayoutParams(new LinearLayoutCompat.LayoutParams(getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels));

        mOpenCvCameraView.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        this.dictionary_char = new Dictionary_char();
        this.is_run = new AtomicBoolean(false);

        this.boxWidth = 448;
        this.boxHeight = 288;
        this.auto_analog_meter_recog = new Auto_Analog_Meter_Recog(this, getApplicationContext());
        int DetectWidth = 448;
        int DetectHeight = 288;
        this.auto_analog_meter_recog.initilized(1, DetectWidth, DetectHeight);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, baseLoaderCallback);
        } else {
            Log.d("TAG", "OpenCV library found inside package. Using it!");
            baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onCameraViewStarted(int i, int i2) {
        int i3 = this.boxWidth;
        int i4 = this.boxHeight;
        if (getResources().getConfiguration().orientation == 1) {
            int min = Math.min(i2, i4);
            int min2 = Math.min(i, i3);
            this.crop_rect = new Rect(Math.max((i - min) / 2, 50), (i2 - min2) / 2, min, min2);
        } else {
            this.crop_rect = new Rect(Math.max((i - i3) / 2, 50), (i2 - i4) / 2, i3, i4);
        }
        this.img = new Mat(i2, i, CvType.CV_8UC4);
        this.img_mask = new Mat(i2, i, CvType.CV_8UC3);
        Mat mat = new Mat(this.img.size(), CvType.CV_8UC1, new Scalar(255.0d));
        this.mask = mat;
        Imgproc.rectangle(mat, new Point(this.crop_rect.x, this.crop_rect.y), new Point((this.crop_rect.x + this.crop_rect.width), (this.crop_rect.y + this.crop_rect.height)), new Scalar(0.0d), -1);
    }

    public void onCameraViewStopped() {
        Mat mat = this.img;
        if (mat != null) {
            mat.release();
        }
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame cvCameraViewFrame) {
        Mat mat = this.img;
        if (mat != null) {
            mat.release();
        }
        Mat mat2 = this.img_mask;
        if (mat2 != null) {
            mat2.release();
        }
        Mat rgba = cvCameraViewFrame.rgba();
        this.img = rgba;
        Imgproc.cvtColor(rgba, this.img_mask, 1);
        if (!this.is_run.get()) {
            this.crop_img_test = this.img_mask.clone();
            new Thread(this).start();
            Imgproc.rectangle(this.img_mask, new Point(this.crop_rect.x, this.crop_rect.y), new Point(this.crop_rect.x + this.crop_rect.width, this.crop_rect.y + this.crop_rect.height), new Scalar(0.0d, 0.0d, 255.0d), 2);
        }
        Core.add(this.img_mask, new Scalar(-150.0d, -150.0d, -150.0d), this.img_mask, this.mask);
        if (getResources().getConfiguration().orientation == 1) {
            return this.img_mask;
        }
        Mat subMat;
        try {
            subMat = this.img_mask.submat(this.crop_rect.y - 100, this.crop_rect.y + this.crop_rect.height + 100, this.crop_rect.x - 100, this.crop_rect.x + this.crop_rect.width + 100);
        } catch (Exception e) {
            subMat = this.img_mask.submat(this.crop_rect.y, this.crop_rect.y + this.crop_rect.height, this.crop_rect.x, this.crop_rect.x + this.crop_rect.width);
        }
        Imgproc.resize(subMat, subMat, this.img_mask.size());
        return subMat;
    }

    public void run() {
        this.is_run.set(true);
        Mat mat = new Mat(this.crop_img_test, this.crop_rect);
        if (getResources().getConfiguration().orientation == 1) {
            Mat t = mat.t();
            Core.flip(t, mat, 1);
            t.release();
        }
        ArrayList<String> arrayList = new ArrayList<>();
        if (this.auto_analog_meter_recog.reconized(mat)) {
            arrayList = this.auto_analog_meter_recog.getNumerator_str();
        }
        if (arrayList.size() <= 0 || arrayList.get(0).length() != 8) {
            mat.release();
        } else {
            runOnUiThread(new CaptureActivity(this, arrayList.get(0), mat));
        }
        this.is_run.set(false);
    }

    private void playSound() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.check);
        mediaPlayer.start();
    }

    public void onDetect(String str, Mat mat) {

        String tag1 = str.substring(0, 2);
        String tag2 = this.dictionary_char.get_persian_string(str.charAt(2));
        String tag3 = str.substring(3, 6);
        String tag4 = str.substring(6, 8);

        playSound();
        MainActivity.detectTag1 = tag1;
        MainActivity.detectTag2 = tag2;
        MainActivity.detectTag3 = tag3;
        MainActivity.detectTag4 = tag4;
        finish();
        mat.release();
    }

}