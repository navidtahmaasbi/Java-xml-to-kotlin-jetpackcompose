package com.azarpark.watchman.detection;

import com.azarpark.watchman.activities.DetectActivity;

import org.opencv.core.Mat;

public final class CaptureActivity implements Runnable {
    public final DetectActivity activity;
    public final  String f$1;
    public final  Mat f$2;

    public CaptureActivity(DetectActivity captureActivity, String str, Mat mat) {
        this.activity = captureActivity;
        this.f$1 = str;
        this.f$2 = mat;
    }

    public final void run() {
        this.activity.onDetect(this.f$1, this.f$2);
    }
}
