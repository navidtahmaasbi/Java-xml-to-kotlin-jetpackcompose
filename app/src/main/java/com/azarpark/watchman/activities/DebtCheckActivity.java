package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.azarpark.watchman.adapters.DebtListAdapter;
import com.azarpark.watchman.databinding.ActivityDebtCheckBinding;
import com.azarpark.watchman.databinding.ActivityDebtListBinding;
import com.azarpark.watchman.models.DebtModel;

import java.util.ArrayList;

public class DebtCheckActivity extends AppCompatActivity {

    ActivityDebtCheckBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebtCheckBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DebtListAdapter adapter = new DebtListAdapter();
        binding.recyclerView.setAdapter(adapter);

        ArrayList<DebtModel> items = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            items.add(new DebtModel());
        }
        adapter.setItems(items);

    }
}