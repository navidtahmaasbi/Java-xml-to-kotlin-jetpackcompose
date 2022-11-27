package com.azarpark.watchman.presentation.page.places

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.azarpark.watchman.R
import com.azarpark.watchman.activities.IncomeStatisticsActivity02
import com.azarpark.watchman.activities.NotificationsActivity
import com.azarpark.watchman.adapters.LocalNotificationsListAdapter
import com.azarpark.watchman.adapters.ParkListAdapter
import com.azarpark.watchman.databinding.ActivityPlacesBinding
import com.azarpark.watchman.dialogs.ParkDialog
import com.azarpark.watchman.enums.PlaceStatus
import com.azarpark.watchman.interfaces.PaymentCallBack
import com.azarpark.watchman.models.Place
import com.azarpark.watchman.payment.PaymentService
import com.azarpark.watchman.payment.saman.NewSamanPayment
import com.azarpark.watchman.utils.Constants
import com.azarpark.watchman.utils.SharedPreferencesRepository
import com.azarpark.watchman.web_service.bodies.ParkBody
import java.util.*
import kotlin.concurrent.timerTask

class PlacesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlacesBinding
    private lateinit var viewModel: PlacesViewModel
    private lateinit var adapter: ParkListAdapter
    private var timer: Timer? = null
    private lateinit var localNotificationsListAdapter: LocalNotificationsListAdapter
    private lateinit var paymentService: PaymentService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlacesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupList()
        setObservers()
        setViewListeners()
        setupPayment()
    }

    private fun setupPayment() {
        // todo az inja mundi
        if (Constants.SELECTED_PAYMENT == Constants.SAMAN){
            paymentService = NewSamanPayment(this, object : PaymentCallBack{
                override fun onSynced() {
                    //todo
                }
            })
        }
    }

    private fun setViewListeners() {
        binding.swipeToRefresh.setOnClickListener { viewModel.fetchData() }
        binding.filterEditText.doOnTextChanged{text, _, _, _ -> adapter.filterItems(text.toString())}
        binding.notification.setOnClickListener { NotificationsActivity.open(this) }
        binding.income.setOnClickListener { IncomeStatisticsActivity02.open(this) }
    }

    override fun onResume() {
        super.onResume()
        //viewModel.fetchData() //uncomment this if needed
        startFetchDataLoop()
    }

    private fun setupList() {
        adapter = ParkListAdapter(this) {
            if (it.status.contains(PlaceStatus.free.toString())) {
                openParkDialog(it, false)
            }else{

            }
        }
        binding.placesList.adapter = adapter

        localNotificationsListAdapter = LocalNotificationsListAdapter { placeID: Int ->
            if (binding.filterEditText.text.toString() == placeID.toString())
                binding.filterEditText.setText("") else binding.filterEditText.setText(
                placeID.toString()
            )
        }
        binding.notificationsRecyclerview.adapter = localNotificationsListAdapter
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setObservers() {
        viewModel = ViewModelProvider(this)[PlacesViewModel::class.java]
        viewModel.placesLiveData.observe(this, {
            if (binding.swipeToRefresh.isRefreshing) binding.swipeToRefresh.isRefreshing = false
            adapter.setItems(it)
        })
        SharedPreferencesRepository.notificationsLiveData.observe(this,{
            binding.notificationsCount.text = "${it.size}"
            binding.notificationsCount.background = getDrawable(if (it.isNotEmpty()) R.drawable.red_circle else R.drawable.blue_circle)
        })
        SharedPreferencesRepository.localNotificationsLiveData.observe(this,{
            binding.notificationsRecyclerview.visibility = if (it.isEmpty()) View.GONE else View.VISIBLE
            localNotificationsListAdapter.updateItems(it)
        })
        viewModel.dialogLiveData.observe(this,{
            it?.show(supportFragmentManager, ParkDialog.TAG)
//            Assistant.hideKeyboard(this@PlacesActivity)
        })
    }

    private fun startFetchDataLoop() {
        if (timer == null){
            timer = Timer()
            timer!!.scheduleAtFixedRate(timerTask {
                viewModel.fetchData()
            }, 0, 5000)
        }
    }

    private fun stopFetchDataLoop() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    private fun openParkDialog(place: Place, isParkingNewPlateOnPreviousPlate: Boolean) {
        viewModel.lastOpenedPlace = place
        viewModel.openDialog(ParkDialog({ parkBody: ParkBody, printFactor: Boolean ->
            if (!isParkingNewPlateOnPreviousPlate) {
                viewModel.parkCar(parkBody, printFactor, this)
            } else {
//                exitPark(place.id, parkBody, printFactor)
            }
        }, place, isParkingNewPlateOnPreviousPlate))
    }

}