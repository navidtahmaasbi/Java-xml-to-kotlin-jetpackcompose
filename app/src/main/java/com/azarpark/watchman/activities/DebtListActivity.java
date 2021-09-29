package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.adapters.DebtListAdapter;
import com.azarpark.watchman.databinding.ActivityDebtListBinding;
import com.azarpark.watchman.dialogs.ConfirmDialog;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.DebtModel;
import com.azarpark.watchman.models.Park;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.DebtHistoryResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DebtListActivity extends AppCompatActivity {

    ActivityDebtListBinding binding;
    LoadingBar loadingBar;
    DebtListAdapter adapter = new DebtListAdapter();
    ConfirmDialog confirmDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebtListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loadingBar = new LoadingBar(DebtListActivity.this);

        adapter = new DebtListAdapter();
        binding.recyclerView.setAdapter(adapter);

        PlateType plateType = PlateType.valueOf(getIntent().getExtras().getString("plateType"));
        String tag1 = getIntent().getExtras().getString("tag1");
        String tag2 = getIntent().getExtras().getString("tag2","0");
        String tag3 = getIntent().getExtras().getString("tag3","0");
        String tag4 = getIntent().getExtras().getString("tag4","0");

        getCarDebtHistory(plateType, tag1, tag2, tag3, tag4, 5, adapter.getItemCount());

//        ArrayList<Park> items = new ArrayList<>();
//        for (int i = 0; i < 40; i++) {
//            items.add(new Park());
//        }
//        adapter.setItems(items);

    }

    private void getCarDebtHistory(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        loadingBar.show();

        repository.getCarDebtHistory("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, limit, offset, new Callback<DebtHistoryResponse>() {
                    @Override
                    public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.code() == HttpURLConnection.HTTP_OK) {

                            if (response.body().getSuccess() == 1) {

                                adapter.addItems(response.body().items);


                            } else if (response.body().getSuccess() == 0) {

                                Toast.makeText(getApplicationContext(), response.body().getMsg(), Toast.LENGTH_SHORT).show();

                            }

                        } else {

                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        confirmDialog = new ConfirmDialog(
                                getResources().getString(R.string.retry_title),
                                getResources().getString(R.string.retry_text),
                                getResources().getString(R.string.retry_confirm_button),
                                getResources().getString(R.string.retry_cancel_button),
                                new ConfirmDialog.ConfirmButtonClicks() {
                                    @Override
                                    public void onConfirmClicked() {
                                        confirmDialog.dismiss();
                                        getCarDebtHistory(plateType,tag1,tag2,tag3,tag4, limit,offset);
                                    }

                                    @Override
                                    public void onCancelClicked() {
                                        confirmDialog.dismiss();
                                    }
                                }
                        );
                    }
                });

    }

    public void myOnBackPressed(View view) {

        onBackPressed();

    }
}