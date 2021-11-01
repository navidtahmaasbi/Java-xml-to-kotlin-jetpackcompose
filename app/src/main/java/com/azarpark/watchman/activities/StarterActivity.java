package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.dialogs.GetValueDialog;
import com.azarpark.watchman.dialogs.MessageDialog;

public class StarterActivity extends AppCompatActivity {

    GetValueDialog messageDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        findViewById(R.id.app).setOnClickListener(view -> {

            startActivity(new Intent(StarterActivity.this,SplashActivity.class));
            StarterActivity.this.finish();

        });

        findViewById(R.id.exit).setOnClickListener(view -> {

            messageDialog = new GetValueDialog("", "", "", (s) -> {

                if (s.equals("12345")){

                    messageDialog.dismiss();

                    getPackageManager().clearPackagePreferredActivities(getPackageName());
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    Intent chooser = Intent.createChooser(home, "Launcher");
                    startActivity(chooser);

                }else
                    Toast.makeText(getApplicationContext(), "رمز اشتباه است", Toast.LENGTH_SHORT).show();



            });

            messageDialog.show(getSupportFragmentManager(),GetValueDialog.TAG);

        });

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}