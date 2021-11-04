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
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.SendExitCodeResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StarterActivity extends AppCompatActivity {

    GetValueDialog messageDialog;
    private int code = 5555;
    LoadingBar loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        loadingBar = new LoadingBar(StarterActivity.this);

        findViewById(R.id.app).setOnClickListener(view -> {

            startActivity(new Intent(StarterActivity.this, SplashActivity.class));
            StarterActivity.this.finish();

        });

        findViewById(R.id.exit).setOnClickListener(view -> {

            Random random = new Random();
            code = 1000 + random.nextInt(9000);
            sendExitCode(code);

            messageDialog = new GetValueDialog("خروج از برنامه", "برای خروج از برنامه کد ارسال شده را وارد نمایید", "خروج", (s) -> {

                if (s.equals(Integer.toString(code))) {

                    messageDialog.dismiss();

                    getPackageManager().clearPackagePreferredActivities(getPackageName());
                    Intent home = new Intent(Intent.ACTION_MAIN);
                    home.addCategory(Intent.CATEGORY_HOME);
                    home.addCategory(Intent.CATEGORY_DEFAULT);
                    home.addCategory(Intent.CATEGORY_MONKEY);
                    Intent chooser = Intent.createChooser(home, "Launcher");
                    startActivity(chooser);

                } else
                    Toast.makeText(getApplicationContext(), "رمز اشتباه است", Toast.LENGTH_SHORT).show();

            });

            messageDialog.show(getSupportFragmentManager(), GetValueDialog.TAG);

        });

    }

    private void sendExitCode(int code) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
        loadingBar.show();

        repository.sendExitCode("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                code, new Callback<SendExitCodeResponse>() {
                    @Override
                    public void onResponse(Call<SendExitCodeResponse> call, Response<SendExitCodeResponse> response) {
                        
                        loadingBar.dismiss();
                        if (!response.isSuccessful())
                            APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), StarterActivity.this, response, () -> sendExitCode(code));
                    }

                    @Override
                    public void onFailure(Call<SendExitCodeResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> sendExitCode(code));
                    }
                });

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}