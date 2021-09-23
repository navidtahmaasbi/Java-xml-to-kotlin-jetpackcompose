package com.azarpark.watchman.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.ParkDialogBinding;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.interfaces.OnParkClicked;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.retrofit_remote.bodies.ParkBody;

public class ParkDialog extends DialogFragment {

    public static final String TAG = "ParkDialogTag";
    ParkDialogBinding binding;
    private OnParkClicked onParkClicked;
    private Place place;
    private PlateType selectedTab = PlateType.simple;

    public ParkDialog(OnParkClicked onParkClicked, Place place) {
        this.onParkClicked = onParkClicked;
        this.place = place;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        binding = ParkDialogBinding.inflate(LayoutInflater.from(getContext()));
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(binding.getRoot());

        binding.placeNumber.setText(place.number+"");

        setSelectedTab(selectedTab);

        binding.plateSimpleSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.simple);

        });

        binding.plateOldArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.old_aras);

        });

        binding.plateNewArasSelector.setOnClickListener(view -> {

            setSelectedTab(PlateType.new_aras);

        });

        binding.submit.setOnClickListener(view -> {

            if (selectedTab == PlateType.simple &&
                    (binding.plateSimpleTag1.getText().toString().isEmpty() ||
                            binding.plateSimpleTag2.getText().toString().isEmpty() ||
                            binding.plateSimpleTag3.getText().toString().isEmpty() ||
                            binding.plateSimpleTag4.getText().toString().isEmpty()))
                Toast.makeText(getContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

            else if (selectedTab == PlateType.old_aras &&
                    binding.plateOldAras.getText().toString().isEmpty())
                Toast.makeText(getContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();

            else if (selectedTab == PlateType.new_aras &&
                    (binding.plateNewArasTag1.getText().toString().isEmpty() ||
                            binding.plateNewArasTag2.getText().toString().isEmpty()))
                Toast.makeText(getContext(), "پلاک را درست وارد کنید", Toast.LENGTH_SHORT).show();
            else if (selectedTab == PlateType.simple)
                onParkClicked.clicked(new ParkBody(
                        binding.plateSimpleTag1.getText().toString(),
                        binding.plateSimpleTag2.getText().toString(),
                        binding.plateSimpleTag3.getText().toString(),
                        binding.plateSimpleTag4.getText().toString(),
                        "simple",
                        place.id,
                        place.street_id
                        ));
            else if (selectedTab == PlateType.old_aras)
                onParkClicked.clicked(new ParkBody(
                        binding.plateOldAras.getText().toString(),
                        "old_aras",
                        place.id,
                        place.street_id
                ));
            else
                onParkClicked.clicked(new ParkBody(
                        binding.plateNewArasTag1.getText().toString(),
                        binding.plateNewArasTag2.getText().toString(),
                        "new_aras",
                        place.id,
                        place.street_id
                ));
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

        return builder.create();
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

            binding.plateSimpleTag1.requestFocus();

        } else if (selectedTab == PlateType.old_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.selected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.unselected_background);

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.VISIBLE);
            binding.plateNewArasArea.setVisibility(View.GONE);

            binding.plateOldAras.requestFocus();

        } else if (selectedTab == PlateType.new_aras) {

            binding.plateSimpleSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateOldArasSelector.setBackgroundResource(R.drawable.unselected_background);
            binding.plateNewArasSelector.setBackgroundResource(R.drawable.selected_background);

            binding.plateSimpleArea.setVisibility(View.GONE);
            binding.plateOldAras.setVisibility(View.GONE);
            binding.plateNewArasArea.setVisibility(View.VISIBLE);

            binding.plateNewArasTag1.requestFocus();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
