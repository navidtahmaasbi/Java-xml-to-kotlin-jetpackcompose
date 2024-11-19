package com.azarpark.cunt.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.azarpark.cunt.databinding.NotificationViewItemBinding
import com.azarpark.cunt.models.Notification
import com.azarpark.cunt.utils.Assistant
import java.util.ArrayList

class NotificationsAdapter(private val onItemClicked:(notification:Notification)-> Unit):
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var items = arrayListOf<Notification>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NotificationViewHolder(NotificationViewItemBinding.inflate(LayoutInflater.from(parent.context),parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as NotificationViewHolder).binding
        val notification = items[position]

        binding.title.text = notification.title
        binding.description.text = notification.description
        binding.date.text = Assistant.miladiToJalali(notification.created_at)
        binding.root.setOnClickListener { onItemClicked.invoke(notification) }

    }

    override fun getItemCount(): Int {
        return items.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addItems(notifications: ArrayList<Notification>) {
        items = notifications
        notifyDataSetChanged()
    }

    class NotificationViewHolder(val binding: NotificationViewItemBinding) : RecyclerView.ViewHolder(binding.root)
}