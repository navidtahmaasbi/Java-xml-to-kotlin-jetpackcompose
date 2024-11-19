package com.azarpark.cunt;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.azarpark.cunt.activities.SplashActivity;

public class AutoStartService extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Intent intent1 = new Intent(context, SplashActivity.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent1);

//        Intent startServiceIntent = new Intent(context, YourService.class);
//        context.startService(startServiceIntent);
    }

}