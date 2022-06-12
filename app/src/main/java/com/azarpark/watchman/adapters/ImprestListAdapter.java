package com.azarpark.watchman.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.databinding.ImpressedItemBinding;
import com.azarpark.watchman.models.Imprest;
import com.azarpark.watchman.utils.Assistant;

import java.util.ArrayList;

public class ImprestListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Imprest> items = new ArrayList<>();
    private final OnAction onAction;
    Assistant assistant = new Assistant();

    public ImprestListAdapter(OnAction onAction) {
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(ImpressedItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        viewHolder.binding.delete.setVisibility(items.get(position).status.equals("watchman_added") ? View.GONE : View.VISIBLE);
        viewHolder.binding.delete.setOnClickListener(view -> {
            if (!items.get(position).status.equals("watchman_added"))
                onAction.onRemove(items.get(position).id);
        });

        if (position < items.size()) {
            viewHolder.binding.lastItemPadding.setVisibility(View.GONE);
        } else
            viewHolder.binding.lastItemPadding.setVisibility(View.VISIBLE);

        viewHolder.binding.title.setText(items.get(position).amount + " تومان");
        viewHolder.binding.status.setText(getStatus(items.get(position).status));
        viewHolder.binding.description.setText(assistant.toJalali(items.get(position).created_at));

    }

    private String getStatus(String englishStatus) {
        switch (englishStatus) {
            case "watchman_added":
                return "ثبت شده توسط پارکبان";
            case "supervisor_accepted":
                return "تایید شده توسط بازرس";
            case "supervisor_rejected":
                return "رد شده توسط بازرس";
            default:
                return "معادل فارسی ثبت نشده";
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Imprest> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        ImpressedItemBinding binding;

        public ItemViewHolder(@NonNull ImpressedItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnAction {

        public void onRemove(int id);

    }
}
