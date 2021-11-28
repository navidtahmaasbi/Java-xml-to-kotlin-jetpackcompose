package com.azarpark.watchman.web_service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.azarpark.watchman.activities.SplashActivity;
import com.azarpark.watchman.utils.Constants;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;

import retrofit2.Response;

public class NewErrorHandler {

    public static boolean responseHasError(Response response, Context context) {


        Gson gson = new Gson();

        if (response.isSuccessful()){

            try {
                JSONObject responseObject = new JSONObject(gson.toJson(response.body()));

                if (responseObject.has(Constants.SUCCESS) && !responseObject.getString(Constants.SUCCESS).equals("1")){

                    Toast.makeText(context, responseObject.has(Constants.DESCRIPTION)? responseObject.getString(Constants.DESCRIPTION) : responseObject.getString(Constants.SUCCESS), Toast.LENGTH_SHORT).show();
                    return true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "response parse error", Toast.LENGTH_SHORT).show();
            }

            return false;
        }

        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED){

            SharedPreferencesRepository.removeToken();
            context.startActivity(new Intent(context, SplashActivity.class));
            return true;
        }

//        response.body().toString()

        return true;

    }

}
