package com.azarpark.cunt.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.cunt.R;
import com.azarpark.cunt.databinding.DiscountItemBinding;
import com.azarpark.cunt.web_service.responses.Discount;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class DiscountListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<Discount> items = new ArrayList<>();
    OnItemClicked onItemClicked;
    int selectedPosition = -1;
    Context context;


    public DiscountListAdapter(OnItemClicked onItemClicked, Context context) {
        this.onItemClicked = onItemClicked;
        this.context = context;
    }

    public void clearSelectedItem(){

        selectedPosition = -1;
        notifyDataSetChanged();

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(DiscountItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
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

        viewHolder.binding.amount.setText(items.get(position).name);
        int fakePrice = items.get(position).price * 6 / 5;
        viewHolder.binding.fakePrice.setText(NumberFormat.getNumberInstance(Locale.US).format(fakePrice) + " تومان");
        viewHolder.binding.price.setText(NumberFormat.getNumberInstance(Locale.US).format(items.get(position).price) + " تومان");


    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Discount> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        DiscountItemBinding binding;

        public ItemViewHolder(@NonNull DiscountItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnItemClicked {

        public void itemClicked(Discount amount);

    }
}
