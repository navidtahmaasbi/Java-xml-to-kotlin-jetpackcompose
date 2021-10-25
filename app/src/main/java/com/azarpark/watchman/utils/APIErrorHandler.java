package com.azarpark.watchman.utils;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import androidx.fragment.app.FragmentManager;

import com.azarpark.watchman.activities.LoginActivity;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.interfaces.OnResponseErrorAction;

import retrofit2.Response;

public class APIErrorHandler {

    static ConfirmDialog confirmDialog;
    public static boolean onfailureDialogIsShowing = false;
    public static boolean onResponseErrorDialogIsShowing = false;

    public static void orResponseErrorHandler(FragmentManager fragmentManager, Activity activity, Response response, OnResponseErrorAction onResponseErrorAction) {

        if (response.code() > 400 && response.code() < 500) {

            activity.startActivity(new Intent(activity, LoginActivity.class));
            activity.finish();

        }

        try {


            if (!onResponseErrorDialogIsShowing) {
                onResponseErrorDialogIsShowing = true;


                String title = " خطای " + response.code();
                String description = " خطایی رخ داده ";
                String confirmTitle = " تلاش دوباره";
                String cancelTitle = "انصراف";
                confirmDialog = new ConfirmDialog(title, description, confirmTitle, cancelTitle, new ConfirmDialog.ConfirmButtonClicks() {
                    @Override
                    public void onConfirmClicked() {

                        onResponseErrorDialogIsShowing = false;
                        onResponseErrorAction.refresh();

                    }

                    @Override
                    public void onCancelClicked() {

                        onResponseErrorDialogIsShowing = false;
                        confirmDialog.dismiss();

                    }
                });


                confirmDialog.show(fragmentManager, ConfirmDialog.TAG);
            }

        } catch (Exception e) {

        }


    }

    public static void onFailureErrorHandler(FragmentManager fragmentManager, Throwable t, OnResponseErrorAction onResponseErrorAction) {

        try {

            if (!onfailureDialogIsShowing) {
                onfailureDialogIsShowing = true;

                String title = " خطای اینترنت";
                String description = "اتصال اینترنت خود را بررسی کنید";
                String confirmTitle = " تلاش دوباره";
                String cancelTitle = "انصراف";
                confirmDialog = new ConfirmDialog(title, description, confirmTitle, cancelTitle, new ConfirmDialog.ConfirmButtonClicks() {
                    @Override
                    public void onConfirmClicked() {

                        if (confirmDialog != null)
                            confirmDialog.dismiss();
                        onfailureDialogIsShowing = false;
                        onResponseErrorAction.refresh();

                    }

                    @Override
                    public void onCancelClicked() {
                        Log.d("---------> ", "dismiss");
                        if (confirmDialog != null)
                            confirmDialog.dismiss();
                        onfailureDialogIsShowing = false;

                    }
                });

                confirmDialog.show(fragmentManager, ConfirmDialog.TAG);
            }


        } catch (Exception e) {

        }


    }

}
