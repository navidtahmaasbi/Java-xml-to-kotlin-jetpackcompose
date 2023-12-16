package com.azarpark.watchman.payment;

import android.view.View;

public interface Printer {
    /**
     * if the print area has a webview in it, wait for 1500 ms
     * else wait for 500 ms
     *
     * @param view
     * @param waitTime
     * @param callback
     * */
    void print(View view, int waitTime, OnPrintDone callback);

    interface OnPrintDone{
        void onDone();
    }
}
