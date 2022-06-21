package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ActivityExitRequestBinding;
import com.azarpark.watchman.dialogs.LoadingBar;
import com.azarpark.watchman.dialogs.MessageDialog;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.web_service.responses.ExitRequestResponse;
import com.azarpark.watchman.utils.Assistant;
import com.azarpark.watchman.utils.SharedPreferencesRepository;
import com.azarpark.watchman.web_service.NewErrorHandler;
import com.azarpark.watchman.web_service.WebService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExitRequestActivity extends AppCompatActivity {

    ActivityExitRequestBinding binding;
    LoadingBar loadingBar;
    MessageDialog messageDialog;
    private PlateType selectedTab = PlateType.simple;
    Activity activity = this;
    WebService webService = new WebService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExitRequestBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadingBar = new LoadingBar(ExitRequestActivity.this);

        binding.plateSimpleTag1.requestFocus();

        binding.plateSimpleSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.simple);

        });

        binding.plateOldArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.old_aras);

        });

        binding.plateNewArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.new_aras);

        });

        binding.plateSimpleTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.plateSimpleTag1.getText().toString().length()==2)     //size is your limit
                {
                    binding.plateSimpleTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.plateSimpleTag2.getText().toString().length()==1)     //size is your limit
                {
                    binding.plateSimpleTag3.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateSimpleTag3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.plateSimpleTag3.getText().toString().length()==3)     //size is your limit
                {
                    binding.plateSimpleTag4.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.plateNewArasTag1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(binding.plateNewArasTag1.getText().toString().length()==5)     //size is your limit
                {
                    binding.plateNewArasTag2.requestFocus();
                }


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    public void myOnBackPressed(View view){

        onBackPressed();

        View v = this.getCurrentFocus();
        if (v != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }

    }

    //------------------------------------------------------------------------------------------------------------------------

    private void setSelectedTab(PlateType selectedTab) {

        this.selectedTab = selectedTab;

        if (selectedTab == PlateType.simple) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.selected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.white));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.black));

            binding.plateSimpleArea.setVisibility(View.VISIBLE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.old_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.white));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.black));

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);
        } else if (selectedTab == PlateType.new_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_tab);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_tab);

            binding.plateSimpleTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateOldArasTitle.setTextColor(getResources().getColor(R.color.black));
            binding.plateNewArasTitle.setTextColor(getResources().getColor(R.color.white));

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);
        }

    }

    public void onSubmitClicked(View view){

        Assistant assistant = new Assistant();

        if (selectedTab == PlateType.simple &&
                (binding.plateSimpleTag1.getText().toString().isEmpty() ||
                        binding.plateSimpleTag2.getText().toString().isEmpty() ||
                        binding.plateSimpleTag3.getText().toString().isEmpty() ||
                        binding.plateSimpleTag4.getText().toString().isEmpty()))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.simple &&
                !assistant.isPersianAlphabet(binding.plateSimpleTag2.getText().toString()))
            Toast.makeText(getApplicationContext(), "حرف وسط پلاک باید فارسی باشد", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.old_aras &&
                binding.plateOldAras.getText().toString().isEmpty())
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

        else if (selectedTab == PlateType.new_aras &&
                (binding.plateNewArasTag1.getText().toString().isEmpty() ||
                        binding.plateNewArasTag2.getText().toString().isEmpty()))
            Toast.makeText(getApplicationContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
        else if (selectedTab == PlateType.simple)
            exitRequest02(
                    selectedTab,
                    binding.plateSimpleTag1.getText().toString(),
                    binding.plateSimpleTag2.getText().toString(),
                    binding.plateSimpleTag3.getText().toString(),
                    binding.plateSimpleTag4.getText().toString()
            );
        else if (selectedTab == PlateType.old_aras)
            exitRequest02(
                    selectedTab,
                    binding.plateOldAras.getText().toString(),
                    "0", "0", "0"

            );
        else
            exitRequest02(
                    selectedTab,
                    binding.plateNewArasTag1.getText().toString(),
                    binding.plateNewArasTag2.getText().toString(),
                    "0", "0"
            );

    }

    //------------------------------------------------------------------------------------------------------------------------

    private void exitRequest02(PlateType plateType, String tag1, String tag2, String tag3, String tag4) {

        Runnable functionRunnable = () -> exitRequest02(plateType, tag1, tag2, tag3, tag4);
        LoadingBar loadingBar = new LoadingBar(ExitRequestActivity.this);
        loadingBar.show();


        webService.getClient(getApplicationContext()).exitRequest(SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), tag1, tag2, tag3, tag4).enqueue(new Callback<ExitRequestResponse>() {
            @Override
            public void onResponse(Call<ExitRequestResponse> call, Response<ExitRequestResponse> response) {

                Assistant.hideKeyboard(ExitRequestActivity.this, binding.getRoot());

                loadingBar.dismiss();
                if (NewErrorHandler.apiResponseHasError(response, getApplicationContext()))
                    return;

                messageDialog = new MessageDialog("درخواست خروج", response.body().getDescription(), "تایید", () -> {
                    messageDialog.dismiss();
                    onBackPressed();
                });

                messageDialog.show(getSupportFragmentManager(), MessageDialog.TAG);


            }

            @Override
            public void onFailure(Call<ExitRequestResponse> call, Throwable t) {
                loadingBar.dismiss();
                NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable);
            }
        });

    }



}