package com.azarpark.watchman.adapters;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.azarpark.watchman.databinding.DebtListItemBinding;
import com.azarpark.watchman.databinding.ParkItemBinding;
import com.azarpark.watchman.models.DebtModel;

import java.util.ArrayList;

public class DebtListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<DebtModel> items;


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DebtModelViewHolder(DebtListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DebtModelViewHolder viewHolder = (DebtModelViewHolder)holder;


    }

    public void setItems(ArrayList<DebtModel> items){

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class DebtModelViewHolder extends RecyclerView.ViewHolder{

        DebtListItemBinding binding;

        public DebtModelViewHolder(@NonNull DebtListItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

}
