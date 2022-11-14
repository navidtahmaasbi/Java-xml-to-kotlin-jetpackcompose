package com.azarpark.watchman.detection;

import org.opencv.core.Point;
import org.opencv.core.RotatedRect;

import java.util.ArrayList;

/* compiled from: compute_plates */
class plate_prob {
    public boolean is_check = false;
    public float prob;
    public ArrayList<Point> pts;
    public RotatedRect rect;

    public plate_prob(RotatedRect rotatedRect, float f, ArrayList<Point> arrayList) {
        this.rect = rotatedRect;
        this.prob = f;
        this.pts = arrayList;
    }
}
