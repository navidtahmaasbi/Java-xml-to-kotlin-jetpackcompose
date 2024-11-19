package com.azarpark.cunt.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.azarpark.cunt.adapters.DebtListAdapter;
import com.azarpark.cunt.databinding.ActivityDebtListBinding;
import com.azarpark.cunt.dialogs.LoadingBar;
import com.azarpark.cunt.enums.PlateType;
import com.azarpark.cunt.web_service.responses.DebtHistoryResponse;
import com.azarpark.cunt.utils.SharedPreferencesRepository;
import com.azarpark.cunt.web_service.NewErrorHandler;
import com.azarpark.cunt.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DebtListActivity extends AppCompatActivity {

    ActivityDebtListBinding binding;
    LoadingBar loadingBar;
    DebtListAdapter adapter = new DebtListAdapter();
    WebService webService = new WebService();

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

    public void myOnBackPressed(View view) {

        onBackPressed();

    }

    //------------------------------------------------------------------------------------------------------------------------

    private void getCarDebtHistory(PlateType plateType, String tag1, String tag2, String tag3, String tag4, int limit, int offset) {

        Runnable functionRunnable = () -> getCarDebtHistory(plateType, tag1, tag2, tag3, tag4, limit, offset);
        LoadingBar loadingBar = new LoadingBar(DebtListActivity.this);
        loadingBar.show();

        webService.getClient(getApplicationContext()).getCarDebtHistory(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4, limit, offset, null,null,0).enqueue(new Callback<DebtHistoryResponse>() {
            @Override
            public void onResponse(Call<DebtHistoryResponse> call, Response<DebtHistoryResponse> response) {

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                adapter.addItems(response.body().items);


            }

            @Override
            public void onFailure(Call<DebtHistoryResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }


}