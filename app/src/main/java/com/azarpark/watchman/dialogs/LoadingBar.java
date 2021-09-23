package com.azarpark.watchman.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.azarpark.watchman.R;

public class LoadingBar {

    public Dialog dialog;
    private boolean isLoading;

    public boolean isLoading() {
        return isLoading;
    }

    public LoadingBar(Context context) {

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.loadingbar);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

    }

    public void show() {

        isLoading = true;
        dialog.show();

    }

    public void dismiss() {

        isLoading = false;
        dialog.dismiss();

    }

}
