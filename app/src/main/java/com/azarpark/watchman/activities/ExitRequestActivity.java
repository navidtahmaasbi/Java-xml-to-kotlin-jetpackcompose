package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ActivityExitRequestBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.retrofit_remote.RetrofitAPIRepository;
import com.azarpark.watchman.retrofit_remote.responses.DeleteExitRequestResponse;
import com.azarpark.watchman.retrofit_remote.responses.ExitRequestResponse;
import com.azarpark.watchman.utils.SharedPreferencesRepository;

import java.net.HttpURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExitRequestActivity extends AppCompatActivity {

    ActivityExitRequestBinding binding;
    LoadingBar loadingBar;
    MessageDialog messageDialog;
    private PlateType selectedTab = PlateType.simple;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExitRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingBar = new LoadingBar(ExitRequestActivity.this);

        binding.plateSimpleSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.simple);

        });

        binding.plateOldArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.old_aras);

        });

        binding.plateNewArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.new_aras);

        });

    }

    private void setSelectedTab(PlateType selectedTab) {

        this.selectedTab = selectedTab;

        if (selectedTab == PlateType.simple) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.selected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_background);

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.old_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_background);

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.new_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_background);

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);
        }

    }

    public void onSubmitClicked(View view){

        if (selectedTab == PlateType.simple &&
                (binding.plateSimpleTag1.getText().toString().isEmpty() ||
                        binding.plateSimpleTag2.getText().toString().isEmpty() ||
                        binding.plateSimpleTag3.getText().toString().isEmpty() ||
                        binding.plateSimpleTag4.getText().toString().isEmpty()))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

        else if (selectedTab == PlateType.old_aras &&
                binding.plateOldAras.getText().toString().isEmpty())
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

        else if (selectedTab == PlateType.new_aras &&
                (binding.plateNewArasTag1.getText().toString().isEmpty() ||
                        binding.plateNewArasTag2.getText().toString().isEmpty()))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.simple)
            exitRequest(
                    selectedTab,
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString()
            );
        else if (selectedTab == PlateType.old_aras)
            exitRequest(
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0", "0", "0"

            );
        else
            exitRequest(
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0", "0"
            );

    }

    private void exitRequest(PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        SharedPreferencesRepository sh_r = new SharedPreferencesRepository(getApplicationContext());
        RetrofitAPIRepository repository = new RetrofitAPIRepository();
        loadingBar.show();

        repository.exitRequest("Bearer " + sh_r.getString(SharedPreferencesRepository.ACCESS_TOKEN),
                plateType, tag1, tag2, tag3, tag4, new Callback<ExitRequestResponse>() {
                    @Override
                    public void onResponse(Call<ExitRequestResponse> call, Response<ExitRequestResponse> response) {

                        System.out.println("--------> url : " + response.raw().request().url());

                        loadingBar.dismiss();
                        if (response.code() == HttpURLConnection.HTTP_OK) {


                            messageDialog = new MessageDialog("درخواست خروج", response.body().getDescription(), "تایید", () -> messageDialog.dismiss());

                            messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);


                        } else {

                            Toast.makeText(getApplicationContext(), "error", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onFailure(Call<ExitRequestResponse> call, Throwable t) {
                        loadingBar.dismiss();
                        Toast.makeText(getApplicationContext(), "onFailure", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void myOnBackPressed(View view){

        onBackPressed();

    }

}