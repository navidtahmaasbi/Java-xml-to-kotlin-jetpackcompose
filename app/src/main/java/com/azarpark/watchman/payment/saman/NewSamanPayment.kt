package com.azarpark.watchman.payment.saman

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.util.Log
import com.azarpark.watchman.interfaces.PaymentCallBack
import com.azarpark.watchman.models.Plate
import com.azarpark.watchman.models.Transaction
import com.azarpark.watchman.payment.PaymentService
import com.azarpark.watchman.payment.PaymentType
import com.azarpark.watchman.remote.Remote
import com.azarpark.watchman.utils.Assistant
import com.azarpark.watchman.utils.Constants
import com.azarpark.watchman.utils.SharedPreferencesRepository
import com.azarpark.watchman.web_service.NewErrorHandler
import com.azarpark.watchman.web_service.responses.CreateTransactionResponse
import com.azarpark.watchman.web_service.responses.VerifyTransactionResponse
import ir.sep.android.Service.IProxy
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewSamanPayment(val activity: Activity, val paymentCallBack: PaymentCallBack) : PaymentService() {

    private lateinit var service: IProxy
    private var connection: MyServiceConnection?
    private val paymentType = PaymentType.SAMAN
    private val stateSuccessful = 0
    private val state = "State"

    init {
        Log.i("TAG", "initService()")
        connection = MyServiceConnection(service)
        val i = Intent()
        i.setClassName("ir.sep.android.smartpos", "ir.sep.android.Service.Proxy")
        val ret: Boolean = activity.bindService(i, connection!!, Context.BIND_AUTO_CREATE)
        Log.i("TAG", "initService() bound value: $ret")
    }

    //should call in onDestroy()
    fun releaseService() {
        activity.unbindService(connection!!)
        connection = null
        Log.d(ContentValues.TAG, "releaseService(): unbound.")
    }

    override fun getPaymentType(): Int {
        return Constants.SAMAN;
    }

    override fun createTransaction(
        plate: Plate,
        transactionType: Int,
        amount: Int,
        placeIdForExitPark: Int?
    ) {
        Remote.getAPI().createTransaction(
            SharedPreferencesRepository.getTokenWithPrefix(),
            plate.plateType.toString(),
            plate.tag1,
            plate.tag2,
            plate.tag3,
            plate.tag4,
            amount,
            transactionType
        ).enqueue(object : Callback<CreateTransactionResponse> {
            override fun onResponse(
                call: Call<CreateTransactionResponse>,
                response: Response<CreateTransactionResponse>
            ) {
                if (NewErrorHandler.apiResponseHasError(
                        response,
                        activity.applicationContext
                    )
                ) return
                val ourToken = response.body()!!.our_token
                val transaction = Transaction(
                    amount.toString(),
                    ourToken.toString(),
                    "0",
                    SharedPreferencesRepository.getValue(Constants.PLACE_ID, "0").toInt(),
                    0,
                    paymentType.toString(),
                    "0",
                    "",
                    "",
                    "",
                    "",
                    Assistant.getUnixTime()
                )
                SharedPreferencesRepository.setCurrentTransaction(transaction)
                val tashim = "0:${amount * 10}:${getShaba(transactionType)}"
                createTashimPayment(
                    tashim,
                    ourToken.toString(),
                    amount * 10,
                    plate,
                    placeIdForExitPark
                )
            }

            override fun onFailure(call: Call<CreateTransactionResponse>, t: Throwable) {
                //todo implement new error handler
//                NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable)
            }
        })
    }

    private fun getShaba(transactionType: Int): String {
        if (transactionType == Constants.TRANSACTION_TYPE_CHAREG)
            return Constants.CHARGE_SHABA
        return Constants.NON_CHARGE_SHABA
    }

    override fun createPayment() {
        TODO("Not yet implemented")
    }

    override fun createTashimPayment(
        tashim: String,
        ourToken: String,
        amount: Int,
        plate: Plate,
        placeIdForExitPark: Int?
    ) {
        val intent = Intent()
        intent.putExtra("TransType", 3)
        intent.putExtra("Amount", amount.toString())
        intent.putExtra("ResNum", ourToken)
        intent.putExtra("AppId", "0")
        intent.putExtra("Tashim", arrayListOf<String>(tashim))
        intent.component =
            ComponentName("ir.sep.android.smartpos", "ir.sep.android.smartpos.ThirdPartyActivity")
        activity.startActivityForResult(intent, SamanPayment.PAYMENT_REQUEST_CODE)
    }

    override fun onActivityResultHandler(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == SamanPayment.PAYMENT_REQUEST_CODE) {
            val state: Int = data.getIntExtra(state, -1)

            //todo implement error handler
//            if (state != 0) Toast.makeText(context, state.toString() + "", Toast.LENGTH_LONG).show()



//      AdditionalData :
//      ++State : 55 ++RefNum : 819972850538 ++ResNum : 8019291329 ++Amount : 1000.0
//      ++Pan : 622106-fdaffd-8750 ++DateTime : 211009182155 ++TraceNumber : 598994 ++result : رمز اشتباه واردشده است
//      --TerminalId : 00002280 --AmountAffective : 1000.0

//      ++result : (succeed / unsucceed) ++rrn : 801663199541 ++res_num : -1 (our_token) ++amount : 000000001000
//      ++pan : 589210***2557 ++date : 23732049000 ++trace : 000015
//      ++message :    (this will have value if there is an error)

            val transaction = SharedPreferencesRepository.getCurrentTransaction()!!
            val refNum = if (state == stateSuccessful)data.extras!!.getString("RefNum", "") else "0"
            val pan = if (state == stateSuccessful) data.extras!!.getString("Pan", "") else "****-****-****-****"
            val dateTime =  if (state == stateSuccessful)data.extras!!.getString("DateTime", "0") else "0"
            val traceNumber =  if (state == stateSuccessful)data.extras!!.getString("TraceNumber", "") else null
            val result =  data.extras!!.getString("result", "")
            transaction.updateTransaction(
                refNum,
                if (state == 0) 1 else -1,
                state.toString(),
                pan,
                dateTime,
                traceNumber,
                result
            )
            if (state != stateSuccessful){
                //todo show 'result' as error message
            }
            SharedPreferencesRepository.updateTransactions02(transaction)
            syncTransaction(transaction)
        }
    }

    override fun syncTransaction(transaction: Transaction) {
        if (Assistant.checkIfVerifyIsPermittedNow()) {
            Assistant.updateLastVerifyRequestTime()
            Remote.getAPI().verifyTransaction(
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
                transaction.result_message
            ).enqueue(object : Callback<VerifyTransactionResponse> {
                override fun onResponse(
                    call: Call<VerifyTransactionResponse>,
                    response: Response<VerifyTransactionResponse>
                ) {
                    //todo implement ne error handler
//                    if (NewErrorHandler.apiResponseHasError(response, context)) return
                    SharedPreferencesRepository.removeFromTransactions02(transaction)
                    if (transaction.status == 1) paymentCallBack.onSynced()
                }

                override fun onFailure(call: Call<VerifyTransactionResponse>, t: Throwable) {
                    //todo implement ne error handler
//                    NewErrorHandler.apiFailureErrorHandler(call, t, fragmentManager, functionRunnable)
                }
            })
        }
    }

    override fun print() {
        TODO("Not yet implemented")
    }
}