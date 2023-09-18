package com.azarpark.watchman.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.R;
import com.azarpark.watchman.databinding.AmountItemBinding;
import com.azarpark.watchman.models.ChargeOrDiscount;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ChargeItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    ArrayList<ChargeOrDiscount> items;
    OnItemClicked onItemClicked;
    int selectedPosition = -1;
    Context context;


    public ChargeItemListAdapter(OnItemClicked onItemClicked,Context context) {
        this.onItemClicked = onItemClicked;
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clearSelectedItem(){

        selectedPosition = -1;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(AmountItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {

        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        viewHolder.binding.getRoot().setOnClickListener(view -> {
            onItemClicked.itemClicked(items.get(position));
            selectedPosition = position;
            notifyDataSetChanged();

        });

        if (position == selectedPosition)
            viewHolder.binding.check.setColorFilter(ContextCompat.getColor(context, R.color.blue), android.graphics.PorterDuff.Mode.SRC_IN);
        else
            viewHolder.binding.check.setColorFilter(ContextCompat.getColor(context, R.color.gray), android.graphics.PorterDuff.Mode.SRC_IN);

        ChargeOrDiscount item = items.get(position);

        int amount = item.isCharge ? item.chargeAmount : item.discount.price;
        String unit = item.isCharge ? "تومان" : item.discount.name;

        viewHolder.binding.amount.setText(NumberFormat.getNumberInstance(Locale.US).format(amount) + "");
        viewHolder.binding.unit.setText(unit);


    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<ChargeOrDiscount> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @SuppressLint("NotifyDataSetChanged")
    public void insert(ChargeOrDiscount item) {

        items.add(0, item);
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        AmountItemBinding binding;

        public ItemViewHolder(@NonNull AmountItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnItemClicked {

        public void itemClicked(ChargeOrDiscount amount);

    }
}
