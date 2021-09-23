package com.azarpark.watchman.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.azarpark.watchman.adapters.DebtListAdapter;
import com.azarpark.watchman.databinding.ActivityDebtListBinding;
import com.azarpark.watchman.models.DebtModel;
import com.azarpark.watchman.models.Park;

import java.util.ArrayList;

public class DebtListActivity extends AppCompatActivity {

    ActivityDebtListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDebtListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        DebtListAdapter adapter = new DebtListAdapter();
        binding.recyclerView.setAdapter(adapter);

        ArrayList<Park> items = new ArrayList<>();
        for (int i = 0; i < 40; i++) {
            items.add(new Park());
        }
        adapter.setItems(items);

    }

    public void myOnBackPressed(View view){

        onBackPressed();

    }
}