package com.azarpark.watchman.presentation.page.places

import android.app.Activity
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.azarpark.watchman.dialogs.LoadingBar
import com.azarpark.watchman.dialogs.ParkDialog
import com.azarpark.watchman.dialogs.ParkResponseDialog
import com.azarpark.watchman.enums.PlaceStatus
import com.azarpark.watchman.models.LocalNotification
import com.azarpark.watchman.models.Notification
import com.azarpark.watchman.models.Place
import com.azarpark.watchman.remote.Remote
import com.azarpark.watchman.utils.Assistant
import com.azarpark.watchman.utils.SharedPreferencesRepository
import com.azarpark.watchman.web_service.NewErrorHandler
import com.azarpark.watchman.web_service.bodies.ParkBody
import com.azarpark.watchman.web_service.responses.ParkResponse
import com.azarpark.watchman.web_service.responses.PlacesResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.callbackFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class PlacesViewModel : ViewModel() {

    //    var parkDialog: ParkDialog? = null
    var dialog: DialogFragment? = null
    var lastOpenedPlace: Place? = null
    private var currentItems: ArrayList<Place> = arrayListOf()
    val placesLiveData: MutableLiveData<ArrayList<Place>> = MutableLiveData(currentItems)
    val dialogLiveData: MutableLiveData<DialogFragment> = MutableLiveData(null)
    val toastLiveData: MutableLiveData<String> = MutableLiveData(null)

    fun openDialog(dialog: DialogFragment) {
        this.dialog = dialog
        dialogLiveData.value = dialog
    }

    fun fetchData() {
        Remote.getAPI().getPlaces(SharedPreferencesRepository.getTokenWithPrefix())
            .enqueue(object : Callback<PlacesResponse> {
                override fun onResponse(
                    call: Call<PlacesResponse>,
                    response: Response<PlacesResponse>
                ) {
                    checkForNotification(response.body()!!.watchman.places)
                    currentItems = response.body()!!.watchman.places
                    placesLiveData.value = currentItems
                    val notifications = response.body()!!.notifications
                    if (notifications.isNotEmpty()) {
                        SharedPreferencesRepository.addNotifications(notifications)
                    }
                }

                override fun onFailure(call: Call<PlacesResponse>, t: Throwable) {
                    println("---------> onFailure")
                }

            })

    }

    fun parkCar(parkBody: ParkBody, printFactor: Boolean, activity: Activity) {
        Assistant.hideKeyboard(activity)
        //todo loading on
        Remote.getAPI()
            .parkCar(SharedPreferencesRepository.getTokenWithPrefix(), parkBody)
            .enqueue(object : Callback<ParkResponse> {
                override fun onResponse(
                    call: Call<ParkResponse>,
                    response: Response<ParkResponse>
                ) {
                    //todo loading off
                    //todo implement new error handler
//                    if (NewErrorHandler.apiResponseHasError(response, getApplicationContext())) return

                    val printCommand = response.body()!!.info.print_command
                    if (dialog != null) dialog!!.dismiss()
                    dialogLiveData.value = ParkResponseDialog(
                        response.body()!!.info.number,
                        response.body()!!.info.price,
                        response.body()!!
                            .info.car_balance,
                        printCommand,
                        true
                    ) {}
                    toastLiveData.value = response.body()!!.description
                    if (printFactor && printCommand == 1) {
                        val place = response.body()!!.info.place

                        //todo implement print
//                        printFactor(
//                            place.id,
//                            place.start,
//                            response.body()!!.info.car_balance,
//                            place,
//                            response.body()!!
//                                .info.print_description
//                        )
                    }
                    fetchData()
                }

                override fun onFailure(call: Call<ParkResponse>, t: Throwable) {
                    //todo loading off
                    //todo implement new error handler
//                    NewErrorHandler.apiFailureErrorHandler(call, t, getSupportFragmentManager(), functionRunnable)
                }
            })
    }

    private fun checkForNotification(newItems: java.util.ArrayList<Place>) {

        for (newPlace in newItems) {
            val oldPlace: Place? = findInCurrentPlaces(newPlace)
            if (oldPlace != null && (oldPlace.exit_request == null && newPlace.exit_request != null ||
                        oldPlace.exit_request != null && newPlace.exit_request != null && oldPlace.exit_request.id != newPlace.exit_request.id)
            ) {
                SharedPreferencesRepository.addToLocalNotifications(
                    LocalNotification(
                        Date().time.toString(),
                        newPlace.id,
                        newPlace.number,
                        LocalNotification.Type.exitRequest
                    )
                )
            }
            if (oldPlace != null &&
                (oldPlace.status != PlaceStatus.free_by_user.toString() && newPlace.status == PlaceStatus.free_by_user.toString() ||
                        oldPlace.status != PlaceStatus.free_by_sms.toString() && newPlace.status == PlaceStatus.free_by_sms.toString())
            ) {
                SharedPreferencesRepository.addToLocalNotifications(
                    LocalNotification(
                        Date().time.toString(), newPlace.id, newPlace.number, LocalNotification.Type.freeByUser
                    ),
                )
            }
        }
    }

    private fun findInCurrentPlaces(place: Place): Place? {
        for (p in currentItems) if (p.id == place.id) return p
        return null
    }
}