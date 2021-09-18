package com.azarpark.watchman.adapters;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.databinding.ParkItemBinding;
import com.azarpark.watchman.models.ParkModel;
import com.azarpark.watchman.models.Place;

import java.util.ArrayList;

public class ParkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Place> items;
    OnItemClicked onItemClicked;

    public ParkListAdapter(OnItemClicked onItemClicked) {
        items = new ArrayList<>();
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ParkModelViewHolder(ParkItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ParkModelViewHolder viewHolder = (ParkModelViewHolder)holder;

        viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.itemClicked(items.get(position)));


    }

    public void setItems(ArrayList<Place> items){

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ParkModelViewHolder extends RecyclerView.ViewHolder{

        ParkItemBinding binding;

        public ParkModelViewHolder(@NonNull ParkItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnItemClicked{

        public void itemClicked(Place place);

    }
}
