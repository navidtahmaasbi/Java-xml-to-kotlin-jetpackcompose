package com.azarpark.cunt;

import android.util.Log;

import com.azarpark.cunt.utils.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class LocalCrashReport implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultUEH;
    private String localPath;

    public LocalCrashReport(String localPath) {
        this.localPath = localPath;
        this.defaultUEH = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Logger.d("[ExceptionHandler] caught an exception -> saving to log file: %s", localPath);

        // Save the exception to a file
        saveToFile(e);

        // Call the default handler
        defaultUEH.uncaughtException(t, e);
    }

    private void saveToFile(Throwable e) {
        try {
            FileWriter fw = new FileWriter(localPath, true);
            PrintWriter pw = new PrintWriter(fw);
            pw.println("Exception: " + e.toString());
            pw.println(Log.getStackTraceString(e));
            pw.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
