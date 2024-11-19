package com.azarpark.cunt.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.cunt.databinding.VacationItemBinding;
import com.azarpark.cunt.models.Vacation;
import com.azarpark.cunt.utils.Assistant;

import java.util.ArrayList;

public class VacationListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<Vacation> items = new ArrayList<>();
    private final OnAction onAction;
    Assistant assistant = new Assistant();

    public VacationListAdapter(OnAction onAction) {
        this.onAction = onAction;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(VacationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        Vacation vacation = items.get(position);
        viewHolder.binding.delete.setVisibility(items.get(position).status.equals("watchman_added") ? View.VISIBLE : View.GONE);
        viewHolder.binding.delete.setOnClickListener(view -> {
            if (items.get(position).status.equals("watchman_added"))
            onAction.onRemove(vacation.id);
        });

        if (position < items.size()-1) {
            viewHolder.binding.lastItemPadding.setVisibility(View.GONE);
        } else
            viewHolder.binding.lastItemPadding.setVisibility(View.VISIBLE);

        viewHolder.binding.title.setText((vacation.type.equals("daily") ? "روزانه" : "ساعتی" ) + "(" + vacation.vacation_type + ")");
        viewHolder.binding.status.setText(Assistant.getVacationStatus(vacation.status));
        String startTime = vacation.type.equals("daily") ? "" : " از " + vacation.from_time;
        String endTime = vacation.type.equals("daily") ? "" : " تا " + vacation.to_time;
        viewHolder.binding.description.setText(assistant.toJalaliWithoutTime(vacation.vacation_date) + startTime + endTime);
        String description = vacation.description == null || vacation.description.isEmpty() ? "" : " به دلیل " + vacation.description;
        viewHolder.binding.createdAt.setText("ایجاد شده در " + assistant.toJalali(vacation.created_at) + description);

    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(ArrayList<Vacation> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        VacationItemBinding binding;

        public ItemViewHolder(@NonNull VacationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnAction {

        public void onRemove(int id);

    }
}
