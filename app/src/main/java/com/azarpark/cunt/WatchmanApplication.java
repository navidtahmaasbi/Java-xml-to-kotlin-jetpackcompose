package com.azarpark.cunt;

import android.app.Application;

import com.azarpark.cunt.utils.Assistant;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.yandex.metrica.YandexMetrica;
import com.yandex.metrica.YandexMetricaConfig;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class WatchmanApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Creating an extended library configuration.
        YandexMetricaConfig config = YandexMetricaConfig.newConfigBuilder("4464c04b-3a95-422f-a543-44b93d96f2d2").build();
        // Initializing the AppMetrica SDK.
        YandexMetrica.activate(getApplicationContext(), config);
        // Automatic tracking of user activity.
        YandexMetrica.enableActivityAutoTracking(this);

        SharedPreferencesRepository.create(getApplicationContext());

        Thread.setDefaultUncaughtExceptionHandler(new LocalCrashReport(getLogFilePath()));
    }

    private String getLogFilePath() {
        String date = Assistant.getDate().toString();
        return String.format("%s/watchman_log_%s.log", getExternalFilesDir(null).getAbsolutePath(), date);
    }
}
