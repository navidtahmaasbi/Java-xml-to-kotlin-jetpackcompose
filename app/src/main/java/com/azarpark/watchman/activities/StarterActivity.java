package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.core.AppConfig;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.web_service.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.WebService;
import com.azarpark.watchman.web_service.responses.SendExitCodeResponse;

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StarterActivity extends AppCompatActivity {


    private int code = 7869;
    LoadingBar loadingBar;
    private final int masterCode = 6677;//todo release
    Assistant assistant;
    int showExitCodeInputTapCount = 0;
    int sendSMSTapCount = 0;
    int versionCode = 0;
    String versionName = "";
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starter);

        if(AppConfig.Companion.isPaymentLessParkLess()){
            startActivity(new Intent(this, SplashActivity.class));
            finish();
        }

        assistant = new Assistant();
        loadingBar = new LoadingBar(StarterActivity.this);

        findViewById(R.id.app).setOnClickListener(view -> {

            startActivity(new Intent(StarterActivity.this, SplashActivity.class));
//            StarterActivity.this.finish();

        });

        findViewById(R.id.exit_area).setOnClickListener(view -> {
            showExitCodeInputTapCount += 1;
        });

        findViewById(R.id.send_sms).setOnClickListener(view -> {
            sendSMSTapCount += 1;
        });

        findViewById(R.id.send_sms).setOnLongClickListener(view -> {

            if (sendSMSTapCount >= 2){

                Random random = new Random();
                code = 1000 + random.nextInt(9000);
                sendExitCode(code);

            }


            return true;
        });

        findViewById(R.id.exit).setOnLongClickListener(view -> {

            if (showExitCodeInputTapCount >= 4) {

                findViewById(R.id.password_area).setVisibility(findViewById(R.id.password_area).getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                if (findViewById(R.id.password_area).getVisibility() == View.VISIBLE)
                    Assistant.eventByMobile(SharedPreferencesRepository.getValue(Constants.USERNAME, "not logged-in"), "click exit app");

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

                Assistant.eventByMobile(SharedPreferencesRepository.getValue(Constants.USERNAME, "not logged-in"), "show launcher chooser");

                startActivity(chooser);


            }

        });

        PackageInfo pInfo = null;
        try {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            versionCode = pInfo.versionCode;
            versionName = pInfo.versionName;

            String version = "ادامه کار با برنامه\n\nنسخه " + versionName;

            ((TextView)findViewById(R.id.app)).setText(version);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    //------------------------------------------------------------------------------------------------------------------------

    private void sendExitCode(int code) {

        webService.getClient(getApplicationContext()).sendExitCode(SharedPreferencesRepository.getTokenWithPrefix(), code).enqueue(new Callback<SendExitCodeResponse>() {
            @Override
            public void onResponse(Call<SendExitCodeResponse> call, Response<SendExitCodeResponse> response) {


            }

            @Override
            public void onFailure(Call<SendExitCodeResponse> call, Throwable t) {

            }
        });

    }
}