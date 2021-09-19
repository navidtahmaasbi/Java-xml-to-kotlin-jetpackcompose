package com.azarpark.watchman.adapters;


import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.databinding.FreeParkItemBinding;
import com.azarpark.watchman.databinding.FullParkItemBinding;
import com.azarpark.watchman.enums.PlaceStatus;
import com.azarpark.watchman.models.Place;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ParkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Place> items;
    OnItemClicked onItemClicked;
    int VIEW_TYPE_FREE = 0, VIEW_TYPE_FUll = 1;

    public ParkListAdapter(OnItemClicked onItemClicked) {
        items = new ArrayList<>();
        this.onItemClicked = onItemClicked;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FUll)
            return new FullParkModelViewHolder(FullParkItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        return new FreeParkModelViewHolder(FreeParkItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Place place = items.get(position);
        if (holder instanceof FullParkModelViewHolder) {

            FullParkModelViewHolder viewHolder = (FullParkModelViewHolder) holder;

            viewHolder.binding.placeNumber.setText(Integer.toString(place.number));

            viewHolder.binding.placeStatus.setText(place.status.equals(PlaceStatus.full_by_watchman.toString())?"پارکبان":"شهروند");

            if (place.tag4 != null && !place.tag4.isEmpty()){

                //this is plate ir

            }// else if


            viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.itemClicked(items.get(position)));
        } else {

            FreeParkModelViewHolder viewHolder = (FreeParkModelViewHolder) holder;

            viewHolder.binding.placeNumber.setText(Integer.toString(place.number));

            viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.itemClicked(items.get(position)));

        }


    }

    public void setItems(ArrayList<Place> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemViewType(int position) {

        if (items.get(position).status.equals(PlaceStatus.full_by_user.toString()) || items.get(position).status.equals(PlaceStatus.full_by_watchman.toString()))
            return VIEW_TYPE_FUll;

        return VIEW_TYPE_FREE;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class FullParkModelViewHolder extends RecyclerView.ViewHolder {

        FullParkItemBinding binding;

        public FullParkModelViewHolder(@NonNull FullParkItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class FreeParkModelViewHolder extends RecyclerView.ViewHolder {

        FreeParkItemBinding binding;

        public FreeParkModelViewHolder(@NonNull FreeParkItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnItemClicked {

        public void itemClicked(Place place);

    }

}
