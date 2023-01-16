package com.azarpark.watchman.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.azarpark.watchman.databinding.ActivityEmployeeBinding
import com.azarpark.watchman.dialogs.ConfirmDialog
import com.azarpark.watchman.dialogs.LoadingBar
import com.azarpark.watchman.utils.Constants
import com.azarpark.watchman.utils.SharedPreferencesRepository
import com.azarpark.watchman.web_service.NewErrorHandler
import com.azarpark.watchman.web_service.WebService
import com.azarpark.watchman.web_service.responses.LogoutResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EmployeeActivity : AppCompatActivity() {
    lateinit var binding:ActivityEmployeeBinding
    var webService = WebService()
    var confirmDialog: ConfirmDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityEmployeeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.name.text = SharedPreferencesRepository.getValue(Constants.WATCHMAN_NAME)
        binding.mobile.text = SharedPreferencesRepository.getValue(Constants.WATCHMAN_MOBILE)

        binding.logOut.setOnClickListener{

            confirmDialog = ConfirmDialog(
                "خروج",
                "ایا اطمینان دارید؟",
                "خروج",
                "لغو",
                object : ConfirmDialog.ConfirmButtonClicks {
                    override fun onConfirmClicked() {
                        logout02()
                    }

                    override fun onCancelClicked() {
                        confirmDialog?.dismiss()
                    }
                })

            confirmDialog?.show(supportFragmentManager, ConfirmDialog.TAG)

        }

        binding.income.setOnClickListener{
            startActivity(Intent(this@EmployeeActivity, IncomeStatisticsActivity02::class.java))
        }

        binding.vacation.setOnClickListener{
            startActivity(Intent(this@EmployeeActivity, VacationsActivity::class.java))
        }

        binding.imprest.setOnClickListener{
            startActivity(Intent(this@EmployeeActivity, ImprestActivity::class.java))
        }

    }

    private fun logout02() {
        val functionRunnable = Runnable { logout02() }
        val loadingBar = LoadingBar(this@EmployeeActivity)
        loadingBar.show()
        webService.getClient(applicationContext)
            .logout(SharedPreferencesRepository.getTokenWithPrefix())
            .enqueue(object : Callback<LogoutResponse?> {
                override fun onResponse(
                    call: Call<LogoutResponse?>,
                    response: Response<LogoutResponse?>
                ) {
                    loadingBar.dismiss()
                    if (NewErrorHandler.apiResponseHasError(response, applicationContext)) return
                    SharedPreferencesRepository.setValue(Constants.ACCESS_TOKEN, "")
                    SharedPreferencesRepository.setValue(Constants.REFRESH_TOKEN, "")
                    SharedPreferencesRepository.setValue(Constants.SUB_DOMAIN, "")
                    startActivity(Intent(this@EmployeeActivity, SplashActivity::class.java))
                    this@EmployeeActivity.finish()
                }

                override fun onFailure(call: Call<LogoutResponse?>, t: Throwable) {
                    loadingBar.dismiss()
                    NewErrorHandler.apiFailureErrorHandler(
                        call,
                        t,
                        supportFragmentManager,
                        functionRunnable
                    )
                }
            })
    }
}