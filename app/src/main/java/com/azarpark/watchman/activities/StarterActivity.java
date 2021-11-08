package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.dialogs.GetValueDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.SendExitCodeResponse;
import com.azarpark.watchman.utils.APIErrorHandler;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StarterActivity extends AppCompatActivity {


    GetValueDialog messageDialog;
    private int code = 5555;
    LoadingBar loadingBar;
    private final int masterCode = 2580;
    Assistant assistant;
    SharedPreferencesRepository sh_r ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        assistant = new Assistant();
        loadingBar = new LoadingBar(StarterActivity.this);
        sh_r = new SharedPreferencesRepository(getApplicationContext());

        findViewById(R.id.app).setOnClickListener(view -> {

            startActivity(new Intent(StarterActivity.this, SplashActivity.class));
//            StarterActivity.this.finish();

        });

        findViewById(R.id.exit).setOnLongClickListener(view -> {

            Random random = new Random();
            code = 1000 + random.nextInt(9000);
            sendExitCode(code);

//            messageDialog = new GetValueDialog("خروج از برنامه", "برای خروج از برنامه کد ارسال شده را وارد نمایید", "خروج", (s) -> {
//
//                if (s.equals(Integer.toString(code)) || s.equals(Integer.toString(masterCode))) {
//
//                    messageDialog.dismiss();
//
//
//
//                } else
//                    Toast.makeText(getApplicationContext(), "رمز اشتباه است", Toast.LENGTH_SHORT).show();
//
//            });

//            messageDialog.show(getSupportFragmentManager(), GetValueDialog.TAG);




            findViewById(R.id.password_area).setVisibility(findViewById(R.id.password_area).getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);

            if (findViewById(R.id.password_area).getVisibility() == View.VISIBLE){
                assistant.eventByMobile(sh_r.getString(SharedPreferencesRepository.USERNAME,"not logged-in"),"click exit app");
            }

            return true;

        });

        findViewById(R.id.confirm).setOnClickListener(view -> {

            EditText edt = findViewById(R.id.value);

            if (edt.getText().toString().isEmpty()) {
                Toast.makeText(getApplicationContext(), "رمز را وارد کنید", Toast.LENGTH_SHORT).show();

            } else if (!edt.getText().toString().equals(Integer.toString(code)) && !edt.getText().toString().equals(Integer.toString(masterCode)))
                Toast.makeText(getApplicationContext(), "رمز را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else {

                getPackageManager().clearPackagePreferredActivities(getPackageName());
                Intent home = new Intent(Intent.ACTION_MAIN);
                home.addCategory(Intent.CATEGORY_HOME);
                home.addCategory(Intent.CATEGORY_DEFAULT);
                home.addCategory(Intent.CATEGORY_MONKEY);
                Intent chooser = Intent.createChooser(home, "Launcher");


                edt.setText("");
                findViewById(R.id.password_area).setVisibility(View.GONE);

                assistant.eventByMobile(sh_r.getString(SharedPreferencesRepository.USERNAME,"not logged-in"),"show launcher chooser");

                startActivity(chooser);


            }

        });

    }

    private void sendExitCode(int code) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository(getApplicationContext());
//        loadingBar.show();

        repository.sendExitCode("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                code, new Callback<SendExitCodeResponse>() {
                    @Override
                    public void onResponse(Call<SendExitCodeResponse> call, Response<SendExitCodeResponse> response) {

//                        loadingBar.dismiss();
//                        if (!response.isSuccessful())
//                            APIErrorHandler.orResponseErrorHandler(getSupportFragmentManager(), StarterActivity.this, response, () -> sendExitCode(code));
                    }

                    @Override
                    public void onFailure(Call<SendExitCodeResponse> call, Throwable t) {
//                        loadingBar.dismiss();
//                        APIErrorHandler.onFailureErrorHandler(getSupportFragmentManager(), t, () -> sendExitCode(code));
                    }
                });

    }


    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }
}