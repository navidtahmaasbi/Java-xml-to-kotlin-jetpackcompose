package com.azarpark.cunt.web_service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.azarpark.cunt.activities.SplashActivity;
import com.azarpark.cunt.dialogs.ConfirmDialog;
import com.azarpark.cunt.dialogs.MessageDialog;
import com.azarpark.cunt.utils.Constants;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.responses.LoginResponse;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import kotlin.Unit;
import retrofit2.Call;
import retrofit2.Response;

public class NewErrorHandler {

    static ConfirmDialog confirmDialog;
    static MessageDialog messageDialog;
    public static boolean onfailureDialogIsShowing = false;
    public static boolean onResponseErrorDialogIsShowing = false;

    public static boolean apiResponseHasError(Response response, Context context) {
        Log.d("kabiri", "Response: " + response);


        Gson gson = new Gson();

        if (response.isSuccessful()) {

            try {
                JSONObject responseObject = new JSONObject(gson.toJson(response.body()));

                if (responseObject.has("msg") && responseObject.getString("msg").equals("logout")){
                    SharedPreferencesRepository.removeToken();
                }

                if (responseObject.has(Constants.SUCCESS) && !responseObject.getString(Constants.SUCCESS).equals("1")) {

                    Toast.makeText(context, responseObject.has(Constants.DESCRIPTION) ? responseObject.getString(Constants.DESCRIPTION) : responseObject.getString(Constants.SUCCESS), Toast.LENGTH_LONG).show();
                    return true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "response parse error", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        if (response.code() == HttpURLConnection.HTTP_BAD_REQUEST) {

            Toast.makeText(context, "درخواست اشتباه. احتمالا رمز عبور را اشتباه وارد کرده اید", Toast.LENGTH_LONG).show();
        }

        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {

            SharedPreferencesRepository.removeToken();
            context.startActivity(new Intent(context, SplashActivity.class));
        }

        if (response.code() == HttpURLConnection.HTTP_FORBIDDEN){

            Toast.makeText(context, "شما از طرف پشتیبانی غیر فعال هستید", Toast.LENGTH_SHORT).show();
            SharedPreferencesRepository.removeToken();

        }

        return true;

    }

    public static void apiFailureErrorHandler(Call call, Throwable t, FragmentManager fragmentManager, Runnable runnable) {

        t.printStackTrace();

        if (!onfailureDialogIsShowing) {
            onfailureDialogIsShowing = true;

            String title = " خطای اینترنت";
            String description = "اتصال اینترنت خود را بررسی کنید";
            String confirmTitle = " تلاش دوباره";

//            messageDialog = new MessageDialog(title, description, confirmTitle, () -> {
//
//                if (messageDialog != null)
//                    messageDialog.dismiss();
//                onfailureDialogIsShowing = false;
//                runnable.run();
//
//            });
            messageDialog = MessageDialog.newInstance(
                    title,
                    description,
                    confirmTitle,
                    () -> {
                        if (messageDialog != null)
                            messageDialog.dismiss();
                        onfailureDialogIsShowing = false;
                        runnable.run();
                    }
            );


            messageDialog.setCancelable(true);
            messageDialog.show(fragmentManager, ConfirmDialog.TAG);
        }


    }

    public static void apiFailureErrorHandler(@NotNull Call<LoginResponse> call, @NotNull Throwable t, @NotNull Unit show, @NotNull Runnable functionRunnable) {
        
    }
}
