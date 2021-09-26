package com.azarpark.watchman.payment;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

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
}
