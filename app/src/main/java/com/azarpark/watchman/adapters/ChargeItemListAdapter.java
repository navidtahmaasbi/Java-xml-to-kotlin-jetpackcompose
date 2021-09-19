package com.azarpark.watchman.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.databinding.AmountItemBinding;

import java.util.ArrayList;

public class ChargeItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Double> items;
    OnItemClicked onItemClicked;

    public ChargeItemListAdapter(OnItemClicked onItemClicked) {
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(AmountItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder)holder;

        viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.itemClicked(items.get(position)));

        viewHolder.binding.amount.setText(Double.toString(items.get(position)));


    }

    public void setItems(ArrayList<Double> items){

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        AmountItemBinding binding;

        public ItemViewHolder(@NonNull AmountItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnItemClicked{

        public void itemClicked(Double amount);

    }
}
