package com.azarpark.cunt.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.azarpark.cunt.databinding.NotificationItemBinding;
import com.azarpark.cunt.models.LocalNotification;
import com.azarpark.cunt.utils.SharedPreferencesRepository;

import java.util.ArrayList;

public class LocalNotificationsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    ArrayList<LocalNotification> items;
    OnSubmitClicked onSubmitClicked;

    public LocalNotificationsListAdapter(OnSubmitClicked onSubmitClicked) {
        items = new ArrayList<>();
        this.onSubmitClicked = onSubmitClicked;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ItemViewHolder(NotificationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        ItemViewHolder viewHolder = (ItemViewHolder) holder;

        viewHolder.binding.submit.setOnClickListener(view -> removeItem(items.get(position)));
        viewHolder.binding.title.setText(
                items.get(position).type == LocalNotification.Type.exitRequest ? "درخواست خروج" :
                        items.get(position).type == LocalNotification.Type.freeByUser ? "خروج توسط کاربر" : "اعلان");
        viewHolder.binding.description.setText("جایگاه " + items.get(position).placeNumber);
        viewHolder.binding.getRoot().setOnClickListener(view -> onSubmitClicked.clicked(items.get(position).placeNumber));


    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItems() {

        items = SharedPreferencesRepository.getLocalNotifications();
        notifyDataSetChanged();

    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateItems(ArrayList<LocalNotification> items) {

        this.items = items;
        notifyDataSetChanged();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void removeItem(LocalNotification notification) {

        SharedPreferencesRepository.removeFromLocalNotifications(notification);
        items = SharedPreferencesRepository.getLocalNotifications();
        notifyDataSetChanged();

    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        NotificationItemBinding binding;

        public ItemViewHolder(@NonNull NotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    public static interface OnSubmitClicked {
        public void clicked(int placeID);

    }
}
