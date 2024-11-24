package com.azarpark.cunt.payment

import android.content.Intent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.azarpark.cunt.adapters.DebtObjectAdapter
import com.azarpark.cunt.core.AppConfig
import com.azarpark.cunt.enums.PlateType
import com.azarpark.cunt.models.Transaction
import com.azarpark.cunt.payment.behpardakht.BehPardakhtPayment
import com.azarpark.cunt.payment.parsian.ParsianPayment
import com.azarpark.cunt.payment.saman.SamanPayment
import com.azarpark.cunt.utils.Assistant
import com.azarpark.cunt.utils.Constants
import com.azarpark.cunt.utils.Logger
import com.azarpark.cunt.utils.SharedPreferencesRepository
import com.azarpark.cunt.web_service.NewErrorHandler
import com.azarpark.cunt.web_service.WebService
import com.azarpark.cunt.web_service.responses.CreateTransactionResponse
import com.azarpark.cunt.web_service.responses.DebtObject
import com.azarpark.cunt.web_service.responses.VerifyTransactionResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


abstract class PaymentService(
    val activity: AppCompatActivity,
    val webService: WebService,
    val paymentCallback: OnPaymentCallback
) : Printer {
    private var debtObjects: List<DebtObject> = emptyList()
    private lateinit var debtObjectAdapter: DebtObjectAdapter
    var isCreatingTransaction = false


    open fun initialize() {}

    open fun stop() {}

    abstract fun onActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent)
    abstract fun launchPayment(
        shabaType: ShabaType, paymentToken: Long, amount: Int, plateType: PlateType,
        tag1: String?, tag2: String?, tag3: String?, tag4: String?, placeID: Int
    )
    open fun launchPayment(
        shabaType: ShabaType, paymentToken: Long, amountPartList: List<TransactionAmount>, amount: Int, plateType: PlateType,
        tag1: String?, tag2: String?, tag3: String?, tag4: String?, placeID: Int
    ){
        throw Exception("Function not implemented")
    }

    abstract fun launchQrCodeScanner()
    abstract fun getPaymentType(): String

    fun createTransaction(
        shabaType: ShabaType, plateType: PlateType,
        tag1: String?, tag2: String?, tag3: String?, tag4: String?,
        amount: Int, placeID: Int, transactionType: Int, transactionListener: OnTransactionCreated?,
        discountId: Int, isWage: Boolean, payload: String? = null
    ) {
        createTransaction(
            shabaType, plateType, tag1, tag2, tag3, tag4, emptyList(), amount, placeID, transactionType,
            transactionListener, discountId, isWage, payload
        )
    }

    /**
     *  if no discount is set, it should be -1
     */
    fun createTransaction(
        shabaType: ShabaType, plateType: PlateType,
        tag1: String?, tag2: String?, tag3: String?, tag4: String?,
        amountPartList: List<TransactionAmount>, amount: Int, placeID: Int, transactionType: Int, transactionListener: OnTransactionCreated?,
        discountId: Int, isWage: Boolean, payload: String? = null
    ) {
        if (isCreatingTransaction) return
        isCreatingTransaction = true

        val functionRunnable = Runnable {
            createTransaction(
                shabaType, plateType, tag1, tag2, tag3, tag4, amountPartList, amount, placeID, transactionType,
                transactionListener, discountId, isWage, payload
            )
        }

        val t1 = tag1 ?: ""
        val t2 = tag2 ?: "-1"
        val t3 = tag3 ?: "-1"
        val t4 = tag4 ?: "-1"

        val call: Call<CreateTransactionResponse> =
            if (discountId == -1)
                webService.getClient(activity).createTransaction(
                    SharedPreferencesRepository.getTokenWithPrefix(),
                    plateType.toString(),
                    t1,
                    t2,
                    t3,
                    t4,
                    amount,
                    transactionType,
                    payload ?: "",
                    if(isWage) 1 else 0
                )
            else
                webService.getClient(activity).createTransactionForDiscount(
                    SharedPreferencesRepository.getTokenWithPrefix(),
                    plateType.toString(),
                    t1,
                    t2,
                    t3,
                    t4,
                    amount,
                    transactionType,
                    discountId,
                    "App\\Models\\Discount",
                    payload ?: "",
                    if(isWage) 1 else 0
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
                SharedPreferencesRepository.setValue(Constants.IS_WAGE_TRANSACTION, isWage.toString())
                SharedPreferencesRepository.setValue(
                    Constants.OUR_TOKEN,
                    java.lang.Long.toString(our_token)
                )


                if(amountPartList.isNotEmpty()){
                    launchPayment(
                        shabaType,
                        our_token,
                        amountPartList,
                        amount,
                        plateType,
                        tag1,
                        tag2,
                        tag3,
                        tag4,
                        placeID
                    )
                }
                else {
                    launchPayment(
                        shabaType,
                        our_token,
                        amount,
                        plateType,
                        tag1,
                        tag2,
                        tag3,
                        tag4,
                        placeID
                    )
                }
            }

            override fun onFailure(call: Call<CreateTransactionResponse>, t: Throwable) {
                isCreatingTransaction = false
                transactionListener?.onCreateTransactionFinished()
                NewErrorHandler.apiFailureErrorHandler(
                    call,
                    t,
                    activity.supportFragmentManager,
                    functionRunnable
                )
            }
        })
    }

    open fun verifyTransaction(transaction: Transaction) {
        if (Assistant.checkIfVerifyIsPermittedNow()) {
            Assistant.updateLastVerifyRequestTime()

            val functionRunnable = Runnable { verifyTransaction(transaction) }

            webService.getClient(activity).verifyTransaction(
                SharedPreferencesRepository.getTokenWithPrefix(),
                transaction.amount,
                transaction.our_token,
                transaction.bank_token,
                transaction.placeID,
                transaction.status,
                transaction.bank_type,
                transaction.state,
                transaction.card_number,
                transaction.bank_datetime,
                transaction.trace_number,
                transaction.result_message,
                if(transaction.isWage) 1 else 0
            ).enqueue(object : Callback<VerifyTransactionResponse> {
                override fun onResponse(
                    call: Call<VerifyTransactionResponse>,
                    response: Response<VerifyTransactionResponse>
                ) {
                    if (NewErrorHandler.apiResponseHasError(response, activity)) return
                    SharedPreferencesRepository.removeFromTransactions02(transaction)
                    Toast.makeText(activity, response.body()!!.description, Toast.LENGTH_SHORT)
                        .show()
                    transaction.transactionType = response.body()!!.flag
                    if (transaction.status == 1) paymentCallback.onTransactionVerified(transaction)
                }

                override fun onFailure(call: Call<VerifyTransactionResponse>, t: Throwable) {
                    NewErrorHandler.apiFailureErrorHandler(
                        call,
                        t,
                        activity.supportFragmentManager,
                        functionRunnable
                    )
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
        fun paymentCallback(paymentCallback: OnPaymentCallback) =
            apply { this.paymentCallback = paymentCallback }

        fun build(): PaymentService {
            if (activity == null || webService == null || paymentCallback == null) {
                throw Exception("PaymentService: requirements not met")
            }

            if (AppConfig.paymentIsSaman()) {
                Logger.d("Using SAMAN payment")
                return SamanPayment(activity!!, webService!!, paymentCallback!!)
            } else if (AppConfig.paymentIsParsian()) {
                Logger.d("Using PARSIAN payment")
                return ParsianPayment(activity!!, webService!!, paymentCallback!!)
            } else if (AppConfig.paymentIsBehPardakht()) {
                Logger.d("Using BEH_PARDAKHT payment")
                return BehPardakhtPayment(activity!!, webService!!, paymentCallback!!)
            }

            throw Exception("No valid implementation is found")
        }
    }

    interface OnPaymentCallback {
        fun onScanDataReceived(data: Int)
        fun onTransactionVerified(transaction: Transaction)
    }

    fun interface OnTransactionCreated {
        fun onCreateTransactionFinished()
    }
}
