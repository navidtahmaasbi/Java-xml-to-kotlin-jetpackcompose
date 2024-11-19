package com.azarpark.cunt.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.azarpark.cunt.adapters.NotificationsAdapter
import com.azarpark.cunt.databinding.ActivityNotificationsBinding
import com.azarpark.cunt.enums.NotificationType
import com.azarpark.cunt.utils.SharedPreferencesRepository
import androidx.activity.OnBackPressedDispatcher


class NotificationsActivity : AppCompatActivity() {

    companion object{
        fun open(activity:Activity){
            activity.startActivity(Intent(activity, NotificationsActivity::class.java))
        }
    }

    lateinit var binding:ActivityNotificationsBinding
    lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clearAll.setOnClickListener {
            SharedPreferencesRepository.removeAllNotifications()
            OnBackPressedDispatcher()
        }
        binding.back.setOnClickListener { OnBackPressedDispatcher() }

        adapter = NotificationsAdapter {

            SharedPreferencesRepository.removeNotification(it.id)

            val destination = when(it.type){
                NotificationType.statistics.toString() -> IncomeStatisticsActivity02::class.java
                else -> TicketsActivity::class.java
            }

            startActivity(Intent(this, destination))

        }
        binding.recyclerView.adapter = adapter

    }

    override fun onResume() {
        super.onResume()
        println("----------> onResume ${SharedPreferencesRepository.getNotifications().size}")
        adapter.addItems(SharedPreferencesRepository.getNotifications())


    }
}