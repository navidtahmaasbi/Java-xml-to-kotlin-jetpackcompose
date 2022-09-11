package com.azarpark.watchman.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.watchman.databinding.TicketItemBinding;
import com.azarpark.watchman.models.Ticket;
import com.azarpark.watchman.utils.Assistant;

import java.util.ArrayList;

public class TicketListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Ticket> items = new ArrayList<>();
    Assistant assistant = new Assistant();
    OnItemClicked onItemClicked;

    public TicketListAdapter(OnItemClicked onItemClicked){
        this.onItemClicked = onItemClicked;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(TicketItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        Ticket ticket = items.get(position);

        viewHolder.binding.getRoot().setOnClickListener(view -> onItemClicked.onClick(ticket));

        viewHolder.binding.title.setText(ticket.subject);
        viewHolder.binding.date.setText(ticket.created_at_j);
//        viewHolder.binding.description.setText(ticket.watchman_ticket_details.get(0).description);

        viewHolder.binding.bottomPadding.setVisibility(position == items.size()-1? View.VISIBLE:View.GONE);

    }


    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Ticket> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
     return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        TicketItemBinding binding;

        public ItemViewHolder(@NonNull TicketItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public interface OnItemClicked{
        public void onClick(Ticket ticket);
    }

}
