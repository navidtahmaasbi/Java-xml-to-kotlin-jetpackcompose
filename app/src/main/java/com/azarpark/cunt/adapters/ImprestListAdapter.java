package com.azarpark.cunt.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.cunt.databinding.ImprestItemBinding;
import com.azarpark.cunt.models.Imprest;
import com.azarpark.cunt.utils.Assistant;

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
        return new ItemViewHolder(ImprestItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        viewHolder.binding.delete.setVisibility(!items.get(position).status.equals("watchman_added") ? View.GONE : View.VISIBLE);
        viewHolder.binding.delete.setOnClickListener(view -> {
            if (items.get(position).status.equals("watchman_added"))
                onAction.onRemove(items.get(position).id);
        });

        if (position < items.size()-1) {
            viewHolder.binding.lastItemPadding.setVisibility(View.GONE);
        } else
            viewHolder.binding.lastItemPadding.setVisibility(View.VISIBLE);

        viewHolder.binding.title.setText(assistant.numberFormat(items.get(position).amount) + " تومان");
        viewHolder.binding.status.setText(Assistant.getImprestStatus(items.get(position).status));
        viewHolder.binding.description.setText(assistant.toJalali(items.get(position).created_at));
        String amount = items.get(position).accepted_amount == null?"درانتظار":assistant.numberFormat(items.get(position).accepted_amount) + " تومان";
        viewHolder.binding.acceptedAmount.setText("مبلغ تایید شده : " + amount);

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

        ImprestItemBinding binding;

        public ItemViewHolder(@NonNull ImprestItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnAction {
        void onRemove(int id);
    }
}
