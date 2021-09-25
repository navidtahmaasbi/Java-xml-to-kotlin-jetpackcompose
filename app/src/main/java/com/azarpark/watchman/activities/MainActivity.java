package com.azarpark.watchman.activities;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.ParkListAdapter;
import com.azarpark.watchman.databinding.ActivityMainBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.dialogs.ParkDialog;
import com.azarpark.watchman.dialogs.ParkInfoDialog;
import com.azarpark.watchman.enums.PlaceStatus;
import com.azarpark.watchman.interfaces.OnGetInfoClicked;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.models.Street;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;
import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.EstimateParkPriceResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.LogoutResponse;
import com.azarpark.watchman.retrofit_remote.responses.ParkResponse;
import com.azarpark.watchman.retrofit_remote.responses.PlacesResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.UUID;

import ir.sep.android.Service.IProxy;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;
    boolean menuIsOpen = false;
    PopupWindow popupWindow;
    View popupView;
    ConfirmDialog confirmDialog;
    ParkListAdapter adapter;
    ParkDialog parkDialog;
    ParkInfoDialog parkInfoDialog;
    LoadingBar loadingBar;
    MessageDialog messageDialog;
    int exitRequestCount = 0;
    TextView watchManName;
    boolean updatePopUpIsShowed = false;
    int version = 0;
    MyServiceConnection connection;
    IProxy service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupUI(binding.getRoot());

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = pInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        loadingBar = new LoadingBar(MainActivity.this);

        initMenuPopup();

        listeners();

        adapter = new ParkListAdapter(place -> {

            if (place.status.equals(PlaceStatus.free.toString()) ||
                    place.status.equals(PlaceStatus.free_by_user.toString()) ||
                    place.status.equals(PlaceStatus.free_by_watchman.toString())) {

                openParkDialog(place);

            } else
                openParkInfoDialog(place);


        });
        binding.recyclerView.setAdapter(adapter);

        getPlaces();

        initService();

    }

    private void getPlaces() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.getPlaces("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<PlacesResponse>() {
            @Override
            public void onResponse(Call<PlacesResponse> call, Response<PlacesResponse> response) {

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (!updatePopUpIsShowed && version != 0 && response.body().update.last_version > version) {

                        updatePopUpIsShowed = true;

                        if (response.body().update.is_forced == 1) {

                            messageDialog = new MessageDialog("به روز رسانی", "به روز رسانی اجباری برای آذرپارک موجود است.", "به روز رسانی", () -> {

                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(response.body().update.update_link));
                                startActivity(browserIntent);

                            });

                        }


                    }

                    if (watchManName != null)
                        watchManName.setText(response.body().watchman.name);

                    exitRequestCount = 0;
                    //todo set placeholder to empty street or places list
                    for (Street street : response.body().watchman.streets) {

                        adapter.setItems(street.places);
                        for (Place place : street.places)
                            if (place.exit_request != null)
                                exitRequestCount++;

                    }

                    if (exitRequestCount > 0) {

                        binding.exitRequestCount.setText(Integer.toString(exitRequestCount));
                        binding.exitRequests.setVisibility(View.VISIBLE);
                    } else
                        binding.exitRequests.setVisibility(View.GONE);


                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<PlacesResponse> call, Throwable t) {
                loadingBar.dismiss();
                System.out.println("---------> onFailure");
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openParkDialog(Place place) {

        parkDialog = new ParkDialog(this::parkCar, place);
        parkDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void parkCar(ParkBody parkBody) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.park("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), parkBody, new Callback<ParkResponse>() {
            @Override
            public void onResponse(Call<ParkResponse> call, Response<ParkResponse> response) {

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1) {

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        parkDialog.dismiss();
                        getPlaces();
                    } else if (response.body().getSuccess() == 0) {

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void openParkInfoDialog(Place place) {

        parkInfoDialog = new ParkInfoDialog(new OnGetInfoClicked() {
            @Override
            public void pay(int price, int placeID) {

                Toast.makeText(getApplicationContext(), "go to payment app and then refresh list then open this dialog again", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void payAsDebt(Place place) {

                exitPark(place);

            }

            @Override
            public void removeExitRequest(Place place1) {

                deleteExitRequest(place1.id);

            }
        }, place);
        parkInfoDialog.show(getSupportFragmentManager(), ParkDialog.TAG);


    }

    private void exitPark(Place place) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        LoadingBar loadingBar = new LoadingBar(MainActivity.this);
        loadingBar.show();

        repository.exitPark("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), place.id, new Callback<ExitParkResponse>() {
            @Override
            public void onResponse(Call<ExitParkResponse> call, Response<ExitParkResponse> response) {


                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    if (response.body().getSuccess() == 1) {

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();
                        parkInfoDialog.dismiss();
                        getPlaces();
                    } else if (response.body().getSuccess() == 0) {

                        Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                    }

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<ExitParkResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        if(inputMethodManager.isAcceptingText()){
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(),
                    0
            );
        }
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(MainActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    private void listeners() {

        binding.refresh.setOnClickListener(view -> getPlaces());

        binding.filterEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                adapter.filterItems(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void initMenuPopup() {

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(R.layout.menu_popup_window02, null);
        int width = LinearLayout.LayoutParams.MATCH_PARENT;
        int height = LinearLayout.LayoutParams.MATCH_PARENT;
        boolean focusable = false; // lets taps outside the popup also dismiss it
        popupWindow = new PopupWindow(popupView, width, height, focusable);

        popupView.findViewById(R.id.exit_request).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ExitRequestActivity.class)));
        popupView.findViewById(R.id.debt_inquiry).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, DebtCheckActivity.class)));
        popupView.findViewById(R.id.car_number_charge).setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CarNumberChargeActivity.class)));
        popupView.findViewById(R.id.help).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//            intent.putExtra("url","");
            startActivity(intent);

        });
        watchManName = popupView.findViewById(R.id.name);
        popupView.findViewById(R.id.about_us).setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//            intent.putExtra("url","");
            startActivity(intent);
        });
        popupView.findViewById(R.id.rules).setOnClickListener(view -> {

            Intent intent = new Intent(MainActivity.this, WebViewActivity.class);
//            intent.putExtra("url","");
            startActivity(intent);

        });
        popupView.findViewById(R.id.logout).setOnClickListener(view -> {

            confirmDialog = new ConfirmDialog("خروج", "ایا اطمینان دارید؟", "خروج", "لغو", new ConfirmDialog.ConfirmButtonClicks() {
                @Override
                public void onConfirmClicked() {

                    logout();

                }

                @Override
                public void onCancelClicked() {

                    confirmDialog.dismiss();
                }
            });

            confirmDialog.show(getSupportFragmentManager(), ConfirmDialog.TAG);

        });

        popupView.findViewById(R.id.logo).setOnClickListener(view -> {

            LinearLayout root = popupView.findViewById(R.id.root);

            



            String text="<div> mehdi08 </div>";
            try {
//                int result= service.PrintByString(text);
                int result= service.PrintByBitmap(getBitmapFromView(root));
            } catch (RemoteException e) {
                e.printStackTrace();
                Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_LONG).show();
            }



        });

//        ((TextView) popupView.findViewById(R.id.text)).setText(Html.fromHtml(getResources().getString(R.string.lorem)));


    }

    //create bitmap from view and returns it
    private Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }
    // used for scanning gallery

    private void deleteExitRequest(int place_id) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        loadingBar.show();

        repository.deleteExitRequest("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                place_id, new Callback<DeleteExitRequestResponse>() {
                    @Override
                    public void onResponse(Call<DeleteExitRequestResponse> call, Response<DeleteExitRequestResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.code() == HttpURLConnection.HTTP_OK) {

                            getPlaces();
                            if (parkInfoDialog != null)
                                parkInfoDialog.dismiss();

                        } else {

                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<DeleteExitRequestResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void logout() {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        loadingBar.show();

        repository.logout("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN), new Callback<LogoutResponse>() {
            @Override
            public void onResponse(Call<LogoutResponse> call, Response<LogoutResponse> response) {

                System.out.println("--------> url : " + response.raw().request().url());

                loadingBar.dismiss();
                if (response.code() == HttpURLConnection.HTTP_OK) {

                    SharedPreferencesRepository sh_p = new SharedPreferencesRepository(getApplicationContext());
                    sh_p.saveString(SharedPreferencesRepository.ACCESS_TOKEN, "");
                    sh_p.saveString(SharedPreferencesRepository.REFRESH_TOKEN, "");
                    MainActivity.this.finish();
                    confirmDialog.dismiss();

                } else {

                    Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onFailure(Call<LogoutResponse> call, Throwable t) {
                loadingBar.dismiss();
                Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void onMenuToggleClicked(View view) {

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        menuIsOpen = true;

//        popupView.setOnTouchListener((v, event) -> {
//            popupWindow.dismiss();
//            return true;
//        });
    }

    public void onExitRequestIconClicked(View view) {

        adapter.showExitRequestItems(!adapter.isShowExitRequestItems());
    }

    public void onBarcodeIconClicked(View view) {

        Intent intent = new Intent();
        intent.setComponent(new ComponentName("ir.sep.android.smartpos",
                "ir.sep.android.smartpos.ScannerActivity"));
        startActivityForResult(intent, 2);
    }

    @Override
    public void onBackPressed() {
        if (menuIsOpen) {
            menuIsOpen = false;
            popupWindow.dismiss();
        } else
            super.onBackPressed();
    }


    class MyServiceConnection implements ServiceConnection {
        public void onServiceConnected(ComponentName name, IBinder boundService) {
            service = IProxy.Stub.asInterface((IBinder) boundService);
            Log.i("--------->", "onServiceConnected(): Connected");
            Toast.makeText(MainActivity.this, "AIDLExample Service connected",
                    Toast.LENGTH_LONG).show();
        }

        public void onServiceDisconnected(ComponentName name) {
            service = null;
            Log.i("---------->", "onServiceDisconnected(): Disconnected");
            Toast.makeText(MainActivity.this, "AIDLExample Service Connected",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void initService() {
        Log.i("TAG", "initService()");
        connection = new MainActivity.MyServiceConnection();
        Intent i = new Intent();
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy");
        boolean ret = bindService(i, connection, Context.BIND_AUTO_CREATE);
        Log.i("TAG", "initService() bound value: " + ret);
    }

    private void releaseService() {
        unbindService(connection);
        connection = null;
        Log.d(TAG, "releaseService(): unbound.");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseService();
    }

    public void pay(int amount) {

        UUID uuid = UUID.randomUUID();
        String randomUUIDString = uuid.toString();
        Intent intent = new Intent();
        intent.putExtra("TransType", 3);
        intent.putExtra("Amount", amount);
        intent.putExtra("ResNum", randomUUIDString);
//        intent.putExtra("AppId", "1");
        intent.setComponent(new ComponentName("ir.sep.android.smartpos",
                "ir.sep.android.smartpos.ThirdPartyActivity"));
        startActivityForResult(intent, 1);

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode==1) {

            int state = data.getIntExtra("State", -1); // Response Code Switch

            String refNum = data.getStringExtra("RefNum"); // Reference number
            String resNum = data.getStringExtra("ResNum");
            // you should store the resNum variable and then call verify method
            System.out.println("--------> state : " + state);
            if (state == 0) // successful
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();
                verify(refNum,resNum);
            } else
                Toast.makeText(getBaseContext(), "Purchase did faild....", Toast.LENGTH_LONG).show();

        }

        else if (resultCode == RESULT_OK && requestCode==2) {
            Toast.makeText(MainActivity.this,data.getStringExtra("ScannerResult"),Toast.LENGTH_LONG).show();
            System.out.println("---------> ScannerResult :" + data.getStringExtra("ScannerResult"));//https://irana.app/how?qr=090YK6
        }
    }

    public void verify(String refNum,String resNum){

        try {
            int verifyResult = service.VerifyTransaction(1, refNum,resNum);
            if (verifyResult == 0) // sucsess
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();
            } else if (verifyResult == 1)//sucsess but print is faild
            {
                Toast.makeText(getBaseContext(), "Purchase did sucssessful....", Toast.LENGTH_LONG).show();
                int r = service.PrintByRefNum(refNum);
            } else // faild
            {
                Toast.makeText(getBaseContext(), "Purchase did faild....", Toast.LENGTH_LONG).show();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

}
