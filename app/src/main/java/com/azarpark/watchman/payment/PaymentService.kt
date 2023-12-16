package com.azarpark.watchman.payment

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azarpark.watchman.core.AppConfig
import com.azarpark.watchman.enums.PlateType
import com.azarpark.watchman.models.Transaction
import com.azarpark.watchman.payment.behpardakht.BehPardakhtPayment
import com.azarpark.watchman.payment.parsian.ParsianPayment
import com.azarpark.watchman.payment.saman.SamanPayment
import com.azarpark.watchman.utils.Assistant
import com.azarpark.watchman.utils.Constants
import com.azarpark.watchman.utils.SharedPreferencesRepository
import com.azarpark.watchman.web_service.NewErrorHandler
import com.azarpark.watchman.web_service.WebService
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

abstract class PaymentService(val activity: AppCompatActivity, val webService: WebService, val paymentCallback: OnPaymentCallback) : Printer {
    var isCreatingTransaction = false

    open fun initialize() {}
    open fun stop() {}

    abstract fun onActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent)
    abstract fun launchPayment(
            shabaType: ShabaType, paymentToken: Long, amount: Int, plateType: PlateType,
            tag1: String, tag2: String, tag3: String, tag4: String, placeID: Int
    )

    abstract fun launchQrCodeScanner()
    abstract fun getPaymentType(): String

    /**
     *  if no discount is set, it should be -1
     */
    open fun createTransaction(
            shabaType: ShabaType, plateType: PlateType,
            tag1: String, tag2: String, tag3: String, tag4: String,
            amount: Int, placeID: Int, transactionType: Int, transactionListener: OnTransactionCreated?,
            discountId: Int
    ) {
        if (isCreatingTransaction) return
        isCreatingTransaction = true

        val functionRunnable = Runnable {
            createTransaction(
                    shabaType, plateType, tag1, tag2, tag3, tag4, amount, placeID, transactionType,
                    transactionListener, discountId
            )
        }

        val t1 = tag1 ?: ""
        val t2 = tag2 ?: "-1"
        val t3 = tag3 ?: "-1"
        val t4 = tag4 ?: "-1"

        val call: Call<CreateTransactionResponse> =
                if (discountId == -1)
                    webService.getClient(activity).createTransaction(
                            SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4,
                            amount, transactionType
                    )
                else
                    webService.getClient(activity).createTransactionForDiscount(
                            SharedPreferencesRepository.getTokenWithPrefix(), plateType.toString(), t1, t2, t3, t4,
                            amount, transactionType, discountId, "App\\Models\\Discount"
                    )

        call.enqueue(object : Callback<CreateTransactionResponse> {
            override fun onResponse(
                    call: Call<CreateTransactionResponse>,
                    response: Response<CreateTransactionResponse>
            ) {
                isCreatingTransaction = false
                transactionListener?.onCreateTransactionFinished()
                if (NewErrorHandler.apiResponseHasError(response, activity)) return


                val our_token = response.body()!!.our_token
                val transaction = Transaction(
                        discountId,
                        Integer.toString(amount),
                        java.lang.Long.toString(our_token),
                        "0", SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0").toInt(),
                        0,
                        getPaymentType(),
                        "0",
                        "",
                        "",
                        "",
                        "",
                        Assistant.getUnixTime()
                )
                SharedPreferencesRepository.addToTransactions02(transaction)
                SharedPreferencesRepository.setValue(Constants.PLATE_TYPE, plateType.toString())
                SharedPreferencesRepository.setValue(Constants.TAG1, tag1)
                SharedPreferencesRepository.setValue(Constants.TAG2, tag2)
                SharedPreferencesRepository.setValue(Constants.TAG3, tag3)
                SharedPreferencesRepository.setValue(Constants.TAG4, tag4)
                SharedPreferencesRepository.setValue(Constants.AMOUNT, amount.toString())
                SharedPreferencesRepository.setValue(Constants.PLACE_ID, Integer.toString(placeID))
                SharedPreferencesRepository.setValue(
                        Constants.OUR_TOKEN,
                        java.lang.Long.toString(our_token)
                )

                launchPayment(shabaType, our_token, amount, plateType, tag1, tag2, tag3, tag4, placeID)
            }

            override fun onFailure(call: Call<CreateTransactionResponse>, t: Throwable) {
                isCreatingTransaction = false
                transactionListener?.onCreateTransactionFinished()
                NewErrorHandler.apiFailureErrorHandler(call, t, activity.supportFragmentManager, functionRunnable)
            }
        })
    }

    open fun verifyTransaction(transaction: Transaction) {
        if (Assistant.checkIfVerifyIsPermittedNow()) {
            Assistant.updateLastVerifyRequestTime()

            val functionRunnable = Runnable { verifyTransaction(transaction) }
            webService.getClient(activity).verifyTransaction(SharedPreferencesRepository.getTokenWithPrefix(), transaction.amount, transaction.our_token,
                    transaction.bank_token, transaction.placeID, transaction.status, transaction.bank_type, transaction.state,
                    transaction.card_number, transaction.bank_datetime, transaction.trace_number, transaction.result_message).enqueue(object : Callback<VerifyTransactionResponse> {
                override fun onResponse(call: Call<VerifyTransactionResponse>, response: Response<VerifyTransactionResponse>) {
                    if (NewErrorHandler.apiResponseHasError(response, activity)) return
                    SharedPreferencesRepository.removeFromTransactions02(transaction)
                    Toast.makeText(activity, response.body()!!.description, Toast.LENGTH_SHORT).show()
                    if (transaction.status == 1) paymentCallback.onTransactionVerified(transaction)
                }

                override fun onFailure(call: Call<VerifyTransactionResponse>, t: Throwable) {
                    NewErrorHandler.apiFailureErrorHandler(call, t, activity.supportFragmentManager, functionRunnable)
                }
            })
        }
    }


    class Builder {
        var activity: AppCompatActivity? = null
            private set
        var webService: WebService? = null
            private set
        var paymentCallback: OnPaymentCallback? = null
            private set

        fun activity(activity: AppCompatActivity) = apply { this.activity = activity }
        fun webService(webService: WebService) = apply { this.webService = webService }
        fun paymentCallback(paymentCallback: OnPaymentCallback) = apply { this.paymentCallback = paymentCallback }

        fun build(): PaymentService {
            if (activity == null || webService == null || paymentCallback == null) {
                throw Exception("PaymentService: requirements not met")
            }

            if (AppConfig.paymentIsSaman) {
                return SamanPayment(activity!!, webService!!, paymentCallback!!)
            } else if (AppConfig.paymentIsParsian) {
                return ParsianPayment(activity!!, webService!!, paymentCallback!!)
            }
            else if (AppConfig.paymentIsBehPardakht)
            {
                return BehPardakhtPayment(activity!!, webService!!, paymentCallback!!)
            }

            throw Exception("No valid implementation is found")
        }
    }

    interface OnPaymentCallback {
        fun onScanDataReceived(data: Int)
        fun onTransactionVerified(transaction: Transaction)
    }

    interface OnTransactionCreated {
        fun onCreateTransactionFinished()
    }
}
