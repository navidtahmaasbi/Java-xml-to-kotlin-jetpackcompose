package com.azarpark.cunt.adapters;


import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.azarpark.cunt.databinding.DebtListItemBinding;
import com.azarpark.cunt.models.Park;

import java.util.ArrayList;

public class DebtListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Park> items;

    public DebtListAdapter() {

        items = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DebtModelViewHolder(DebtListItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        DebtModelViewHolder viewHolder = (DebtModelViewHolder)holder;
        Park park = items.get(position);
        viewHolder.binding.price.setText(park.price + " تومان");
        viewHolder.binding.street.setText(park.street_name);
        viewHolder.binding.start.setText(park.start_j);
        viewHolder.binding.end.setText(park.end_j);


    }

    public void setItems(ArrayList<Park> items){

        this.items = items;
        notifyDataSetChanged();

    }

    public void addItems(ArrayList<Park> items){

        this.items.addAll(items);
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
