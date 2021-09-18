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

    public LoadingBar(Context context, Activity activity) {


        dialog = new Dialog(context);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.loadingbar);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        window.setBackgroundDrawableResource(R.color.loadingbar_background);
        dialog.getWindow().setGravity(Gravity.CENTER);

    }

    public void show() {

        dialog.show();

    }

    public void dismiss() {

        dialog.dismiss();

    }

}
