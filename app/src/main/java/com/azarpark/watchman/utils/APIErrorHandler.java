package com.azarpark.watchman.utils;

import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.interfaces.OnResponseErrorAction;

import retrofit2.Call;
import retrofit2.Response;

public class APIErrorHandler {

    static ConfirmDialog confirmDialog;

    public static void orResponseErrorHandler(Response response, OnResponseErrorAction onResponseErrorAction) {

        String title = " خطای " + response.code();
        String description = " خطایی رخ داده " ;
        String confirmTitle = " تلاش دوباره" ;
        String cancelTitle = "انصراف" ;
        confirmDialog = new ConfirmDialog(title, description, confirmTitle, cancelTitle, new ConfirmDialog.ConfirmButtonClicks() {
            @Override
            public void onConfirmClicked() {

                onResponseErrorAction.refresh();

            }

            @Override
            public void onCancelClicked() {

                confirmDialog.dismiss();

            }
        });


    }

    public static void onFailureErrorHandler(Throwable t, OnResponseErrorAction onResponseErrorAction) {

        String title = " خطای اینترنت";
        String description = "اتصال اینترنت خود را بررسی کنید" ;
        String confirmTitle = " تلاش دوباره" ;
        String cancelTitle = "انصراف" ;
        confirmDialog = new ConfirmDialog(title, description, confirmTitle, cancelTitle, new ConfirmDialog.ConfirmButtonClicks() {
            @Override
            public void onConfirmClicked() {

                onResponseErrorAction.refresh();

            }

            @Override
            public void onCancelClicked() {

                confirmDialog.dismiss();

            }
        });


    }

}
