package com.azarpark.watchman.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.FreeParkItemBinding;
import com.azarpark.watchman.databinding.FullParkItemBinding;
import com.azarpark.watchman.enums.PlaceStatus;
import com.azarpark.watchman.enums.PlateType;
import com.azarpark.watchman.models.Place;
import com.azarpark.watchman.utils.Assistant;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ParkListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Place> items, filteredItems;
    OnItemClicked onItemClicked;
    int VIEW_TYPE_FREE = 0, VIEW_TYPE_FUll = 1;
    boolean showExitRequestItems = false;
    String filterText = "";
    Context context;
    Assistant assistant;

    public ParkListAdapter(Context context, OnItemClicked onItemClicked) {
        items = new ArrayList<>();
        filteredItems = new ArrayList<>();
        this.onItemClicked = onItemClicked;
        this.context = context;
        assistant = new Assistant();
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

        Place place = filteredItems.get(position);
        if (holder instanceof FullParkModelViewHolder) {

            FullParkModelViewHolder viewHolder = (FullParkModelViewHolder) holder;

            viewHolder.binding.placeNumber.setText(Integer.toString(place.number));

            viewHolder.binding.placeStatus.setText(place.status.equals(PlaceStatus.full_by_watchman.toString()) ? "پارکبان" : "شهروند");

            viewHolder.binding.placeStatus.setBackgroundColor(place.status.equals(PlaceStatus.full_by_user.toString()) ? context.getResources().getColor(R.color.orange) : context.getResources().getColor(R.color.dark_blue));

            if (place.estimate_price.price > 0)
                viewHolder.binding.paymentStatus.setText("پولی");
            else
                viewHolder.binding.paymentStatus.setText("رایگان");

            if (assistant.getPlateType(place) == PlateType.simple) {

                viewHolder.binding.plateIrArea.setVisibility(View.VISIBLE);
                viewHolder.binding.plateArasArea.setVisibility(View.GONE);
                viewHolder.binding.plateArasNewArea.setVisibility(View.GONE);

                viewHolder.binding.plateIrTag1.setText(place.tag1);
                viewHolder.binding.plateIrTag2.setText(place.tag2);
                viewHolder.binding.plateIrTag3.setText(place.tag3);
                viewHolder.binding.plateIrTag4.setText(place.tag4);

            } else if (assistant.getPlateType(place) == PlateType.old_aras) {

                viewHolder.binding.plateIrArea.setVisibility(View.GONE);
                viewHolder.binding.plateArasArea.setVisibility(View.VISIBLE);
                viewHolder.binding.plateArasNewArea.setVisibility(View.GONE);

                viewHolder.binding.plateArasTag1Fa.setText(place.tag1);
                viewHolder.binding.plateArasTag1En.setText(place.tag1);

            } else {

                viewHolder.binding.plateIrArea.setVisibility(View.GONE);
                viewHolder.binding.plateArasArea.setVisibility(View.GONE);
                viewHolder.binding.plateArasNewArea.setVisibility(View.VISIBLE);

                viewHolder.binding.plateArasNewTag1Fa.setText(place.tag1);
                viewHolder.binding.plateArasNewTag2Fa.setText(place.tag2);
                viewHolder.binding.plateArasNewTag1En.setText(place.tag1);
                viewHolder.binding.plateArasNewTag2En.setText(place.tag2);

            }

            viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.itemClicked(place));

            if (place.exit_request != null)
                viewHolder.binding.header.setBackgroundResource(R.drawable.red_top_5_bg);
            else
                viewHolder.binding.header.setBackgroundResource(R.drawable.blue_top_5_bg);


        } else {

            FreeParkModelViewHolder viewHolder = (FreeParkModelViewHolder) holder;

            viewHolder.binding.placeNumber.setText(Integer.toString(place.number));

            viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.itemClicked(filteredItems.get(position)));

        }


    }

    public void setItems(ArrayList<Place> items) {

        this.items = items;
        filteredItems = items;
        filterItems(filterText);

    }

    @Override
    public int getItemViewType(int position) {

        if (filteredItems.get(position).status.equals(PlaceStatus.full_by_user.toString()) || filteredItems.get(position).status.equals(PlaceStatus.full_by_watchman.toString()))
            return VIEW_TYPE_FUll;

        return VIEW_TYPE_FREE;
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    public void filterItems(String filterText) {

        this.filterText = filterText;

        filteredItems = new ArrayList<>();

        for (Place place : items)
            if (Integer.toString(place.number).contains(filterText) ||
                    (place.tag1 != null && place.tag1.contains(this.filterText)) ||
                    (place.tag2 != null && place.tag2.contains(this.filterText)) ||
                    (place.tag3 != null && place.tag3.contains(this.filterText)) ||
                    (place.tag4 != null && place.tag4.contains(this.filterText))
            )
                filteredItems.add(place);

        if (showExitRequestItems) {

            ArrayList<Place> arr = filteredItems;
            filteredItems = new ArrayList<>();

            for (Place place : arr)
                if (place.exit_request != null) {
                    filteredItems.add(place);
                }

        }

        notifyDataSetChanged();


    }

    public boolean isShowExitRequestItems() {

        return showExitRequestItems;

    }

    public void showExitRequestItems(boolean show) {

        showExitRequestItems = show;
        filteredItems = new ArrayList<>();

        if (show) {

            for (Place place : items)
                if (place.exit_request != null) {
                    filteredItems.add(place);
                }

        } else
            filteredItems = items;

        notifyDataSetChanged();

    }

    public Place getItemWithID(int placeId) {

        for (Place place : items)
            if (place.id == placeId)
                return place;

        return null;
    }

    public boolean listHaveNewExitRequest(ArrayList<Integer> exitRequestPlaceIDs) {

        ArrayList<Integer> arrayList = getExitRequestsIDs();
        if (arrayList.size() < exitRequestPlaceIDs.size())
            return true;
        else if (arrayList.size() == exitRequestPlaceIDs.size())
            return !(exitRequestPlaceIDs.containsAll(arrayList) && arrayList.containsAll(exitRequestPlaceIDs));
        else
            return !(exitRequestPlaceIDs.containsAll(exitRequestPlaceIDs));

    }

    private ArrayList<Integer> getExitRequestsIDs() {

        ArrayList<Integer> exitRequestPlaceIDs = new ArrayList<>();

        for (Place place : items)
            if (place.exit_request != null)
                exitRequestPlaceIDs.add(place.id);

        return exitRequestPlaceIDs;

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
