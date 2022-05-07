package com.azarpark.watchman.adapters;

import android.annotation.SuppressLint;
import android.renderscript.ScriptGroup;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import com.azarpark.watchman.databinding.SingleSelectItemBinding;
import com.azarpark.watchman.databinding.VacationItemBinding;
import com.azarpark.watchman.dialogs.SingleSelectDialog;
import com.azarpark.watchman.models.Vacation;

import java.util.ArrayList;

public class VacationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Vacation> items;
    private  OnAction onAction;

    public VacationListAdapter(OnAction onAction) {
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(VacationItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder)holder;

        viewHolder.binding.content.setOnLongClickListener(view -> {
            onAction.onRemove(1);
            return true;
        });

        if (position < items.size()){
            viewHolder.binding.lastItemPadding.setVisibility(View.GONE);
        }else
            viewHolder.binding.lastItemPadding.setVisibility(View.VISIBLE);

    }

//    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Vacation> items){

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size()+1;
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        VacationItemBinding binding;

        public ItemViewHolder(@NonNull VacationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnAction{

        public void onRemove(int id);

    }
}
