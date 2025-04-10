package com.azarpark.cunt.adapters;

import android.annotation.SuppressLint;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.cunt.R;
import com.azarpark.cunt.databinding.LeftMessageItemBinding;
import com.azarpark.cunt.databinding.RightMessageItemBinding;
import com.azarpark.cunt.models.Message;
import com.azarpark.cunt.utils.Assistant;

import java.util.ArrayList;

public class TicketMessagesListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Message> items = new ArrayList<>();
    Assistant assistant = new Assistant();
    private int itemTypeSupport = 500, itemTypeUser = 501;


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == itemTypeSupport)
         return new LeftMessageItemViewHolder(LeftMessageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    return new RightMessageItemViewHolder(RightMessageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        Message message = items.get(position);

        if (holder instanceof RightMessageItemViewHolder){

            RightMessageItemViewHolder viewHolder = (RightMessageItemViewHolder) holder;
            viewHolder.binding.date.setText(message.created_at_j);
            viewHolder.binding.message.setLinkTextColor(viewHolder.binding.getRoot().getContext().getResources().getColor(R.color.blue));
            viewHolder.binding.message.setText(message.description);
            viewHolder.binding.bottomPadding.setVisibility(position == items.size()-1? View.VISIBLE:View.GONE);

            Linkify.addLinks(viewHolder.binding.message, Linkify.ALL);
        }else {

            LeftMessageItemViewHolder viewHolder = (LeftMessageItemViewHolder) holder;
            viewHolder.binding.date.setText(message.created_at_j);
            viewHolder.binding.message.setLinkTextColor(viewHolder.binding.getRoot().getContext().getResources().getColor(R.color.blue));
            viewHolder.binding.message.setText(message.description);
            viewHolder.binding.bottomPadding.setVisibility(position == items.size()-1? View.VISIBLE:View.GONE);

            Linkify.addLinks(viewHolder.binding.message, Linkify.ALL);
        }

    }


    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Message> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).is_watchman == 1)
            return itemTypeUser;
        return itemTypeSupport;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class RightMessageItemViewHolder extends RecyclerView.ViewHolder {

        RightMessageItemBinding binding;

        public RightMessageItemViewHolder(@NonNull RightMessageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static class LeftMessageItemViewHolder extends RecyclerView.ViewHolder {

        LeftMessageItemBinding binding;

        public LeftMessageItemViewHolder(@NonNull LeftMessageItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }


}
