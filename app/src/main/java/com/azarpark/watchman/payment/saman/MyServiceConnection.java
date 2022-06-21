package com.azarpark.watchman.payment.saman;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.azarpark.watchman.interfaces.PrintMessage;

import ir.sep.android.Service.IProxy;

public class MyServiceConnection implements ServiceConnection {

    IProxy service;

    public MyServiceConnection(IProxy service) {
        this.service = service;
    }

    public void onServiceConnected(ComponentName name, IBinder boundService) {
        service = IProxy.Stub.asInterface((IBinder) boundService);
        Log.i("--------->", "onServiceConnected(): Connected");
    }

    public void onServiceDisconnected(ComponentName name) {
        service = null;
        Log.i("---------->", "onServiceDisconnected(): Disconnected");
    }

    public void print(Bitmap bitmap) {

        int result = 0;
        try {
            result = service.PrintByBitmap(bitmap);

        } catch (RemoteException e) {
            e.printStackTrace();
        }


    }

    public IProxy getService() {
        return service;
    }
}
