package com.azarpark.watchman.detection;

import android.util.Log;

import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class compute_plates {
    private ArrayList<plate_prob> rects;

    public compute_plates(ArrayList<plate_prob> arrayList) {
        this.rects = arrayList;
    }

    public ArrayList<RotatedRect> compute() {
        ArrayList<RotatedRect> arrayList = new ArrayList<>();
        while (true) {
            int max_prob_rect = max_prob_rect();
            if (max_prob_rect == -1) {
                return arrayList;
            }
            this.rects.get(max_prob_rect).is_check = true;
            arrayList.add(plate_cheking(this.rects.get(max_prob_rect)));
        }
    }

    private RotatedRect plate_cheking(plate_prob plate_prob) {
        MatOfPoint matOfPoint2f = new MatOfPoint();
        matOfPoint2f.fromList(plate_prob.pts);
        Rect boundingRect = Imgproc.boundingRect(matOfPoint2f);
        int i = boundingRect.height * boundingRect.width;
        ListIterator<plate_prob> listIterator = this.rects.listIterator();
        ArrayList<Point> arrayList = plate_prob.pts;
        while (listIterator.hasNext()) {
            int nextIndex = listIterator.nextIndex();
            plate_prob next = listIterator.next();
            if (!next.is_check) {
                MatOfPoint matOfPoint2f2 = new MatOfPoint();
                matOfPoint2f2.fromList(next.pts);
                Rect boundingRect2 = Imgproc.boundingRect(matOfPoint2f2);
                int Intersect = Intersect(boundingRect, boundingRect2);
                double d = (double) (((float) Intersect) / ((float) (((boundingRect2.height * boundingRect2.width) + i) - Intersect)));
                if (d > 0.2d) {
                    this.rects.get(nextIndex).is_check = true;
                }
                if (d >= 0.85d && d <= 0.95d) {
                    arrayList.addAll(next.pts);
                }
            }
        }
        RotatedRect rotatedRect = get_rot_rect(arrayList);
        if (rotatedRect.size.width == rotatedRect.size.height) {
            Log.e("erro", "Detection model doest not load");
        }
        return rotatedRect;
    }

    private int Intersect(Rect rect, Rect rect2) {
        int max = Math.max(rect.x, rect2.x);
        int max2 = Math.max(rect.y, rect2.y);
        int min = Math.min(rect.x + rect.width, rect2.x + rect2.width);
        int min2 = Math.min(rect.y + rect.height, rect2.y + rect2.height);
        if (max > min || max2 > min2) {
            return 0;
        }
        return (min - max) * (min2 - max2);
    }

    private int Merge(Rect rect, Rect rect2) {
        int min = Math.min(rect.x, rect2.x);
        int min2 = Math.min(rect.y, rect2.y);
        return (Math.max(rect.x + rect.width, rect2.x + rect2.width) - min) * (Math.max(rect.y + rect.height, rect2.y + rect2.height) - min2);
    }

    private RotatedRect get_rot_rect(List<Point> list) {
        MatOfPoint2f matOfPoint2f = new MatOfPoint2f();
        matOfPoint2f.fromList(list);
        RotatedRect minAreaRect = Imgproc.minAreaRect(matOfPoint2f);
        Size size = new Size(minAreaRect.size.width, minAreaRect.size.height);
        if (minAreaRect.angle < 0.0d && size.height > size.width) {
            minAreaRect.angle += 90.0d;
            minAreaRect.size.height = size.width;
            minAreaRect.size.width = size.height;
        } else if (size.height > size.width) {
            minAreaRect.angle = 90.0d - minAreaRect.angle;
            minAreaRect.size.height = size.width;
            minAreaRect.size.width = size.height;
        }
        return minAreaRect;
    }

    private int max_prob_rect() {
        ListIterator<plate_prob> listIterator = this.rects.listIterator();
        float f = 0.0f;
        int i = -1;
        while (listIterator.hasNext()) {
            int nextIndex = listIterator.nextIndex();
            plate_prob next = listIterator.next();
            if (!next.is_check && f < next.prob) {
                f = next.prob;
                i = nextIndex;
            }
        }
        return i;
    }
}
