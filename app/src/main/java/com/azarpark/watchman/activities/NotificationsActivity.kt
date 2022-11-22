package com.azarpark.watchman.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.azarpark.watchman.adapters.NotificationsAdapter
import com.azarpark.watchman.databinding.ActivityNotificationsBinding
import com.azarpark.watchman.enums.NotificationType
import com.azarpark.watchman.utils.SharedPreferencesRepository

class NotificationsActivity : AppCompatActivity() {
    lateinit var binding:ActivityNotificationsBinding
    lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.clearAll.setOnClickListener {
            SharedPreferencesRepository.removeAllNotifications()
            onBackPressed()
        }
        binding.back.setOnClickListener { onBackPressed() }

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