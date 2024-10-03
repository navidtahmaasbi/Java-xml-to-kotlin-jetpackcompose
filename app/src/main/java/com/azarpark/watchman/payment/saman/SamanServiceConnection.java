package com.azarpark.watchman.payment.saman;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import ir.sep.android.Service.IProxy;

public class SamanServiceConnection implements ServiceConnection {

    IProxy service;

    public void onServiceConnected(ComponentName name, IBinder boundService) {
        service = IProxy.Stub.asInterface((IBinder) boundService);
        Log.i("--------->", "onServiceConnected(): Connected");
    }

    public void onServiceDisconnected(ComponentName name) {
        service = null;
        Log.i("---------->", "onServiceDisconnected(): Disconnected");
    }

    public void print(Bitmap bitmap) {
        try {
            service.PrintByBitmap(bitmap);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
