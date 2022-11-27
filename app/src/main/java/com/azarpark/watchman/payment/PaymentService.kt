package com.azarpark.watchman.payment

import android.content.Intent
import com.azarpark.watchman.enums.PlateType
import com.azarpark.watchman.models.Plate
import com.azarpark.watchman.models.Transaction
import com.azarpark.watchman.payment.saman.SamanPayment

abstract class PaymentService {

    abstract fun getPaymentType():Int

    abstract fun createTransaction(
        plate: Plate,
        transactionType: Int,
        amount: Int,
        placeIdForExitPark : Int?
    )

    abstract fun createPayment()

    abstract fun createTashimPayment(tashim:String, ourToken:String, amount:Int, plate:Plate, placeIdForExitPark:Int?)

    abstract fun onActivityResultHandler(requestCode:Int, resultCode:Int, data: Intent)

    abstract fun syncTransaction(transaction: Transaction)

    abstract fun print()

}
