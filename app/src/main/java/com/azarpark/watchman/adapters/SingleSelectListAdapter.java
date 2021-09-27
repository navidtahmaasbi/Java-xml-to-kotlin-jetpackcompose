package com.azarpark.watchman.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.databinding.AmountItemBinding;
import com.azarpark.watchman.databinding.SingleSelectItemBinding;
import com.azarpark.watchman.dialogs.SingleSelectDialog;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class SingleSelectListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<String> items;
    SingleSelectDialog.OnItemSelected onItemSelected;

    public SingleSelectListAdapter(SingleSelectDialog.OnItemSelected onItemSelected) {
        this.onItemSelected = onItemSelected;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(SingleSelectItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder)holder;

        viewHolder.binding.getRoot().setOnClickListener(view -> onItemSelected.select(position));

        viewHolder.binding.title.setText(items.get(position));


    }

    public void setItems(ArrayList<String> items){

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        SingleSelectItemBinding binding;

        public ItemViewHolder(@NonNull SingleSelectItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
