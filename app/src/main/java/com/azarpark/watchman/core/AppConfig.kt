package com.azarpark.watchman.core

import com.azarpark.watchman.BuildConfig
import com.azarpark.watchman.models.TicketMessage

class AppConfig {

    companion object {
        private const val domain = "backend1.azarpark.irana.app"
        private const val http = "https://"
        private const val tabriz = "tabriz"
        private const val sarab = "sarab"
        private val tabrizConfig = Config("$http$tabriz.$domain", PaymentType.SAMAN)
        private val sarabConfig = Config("$http$sarab.$domain", PaymentType.PARSIAN)
        private val testConfig = Config("$http$tabriz.$domain", PaymentType.BEH_PARDAKHT)
        private val paymentLessTabrizConfig = Config("$http$tabriz.$domain", PaymentType.PAYMENT_LESS)
        private val paymentLessSarabConfig = Config("$http$sarab.$domain", PaymentType.PAYMENT_LESS)
        private val paymentLessParkLessTabrizConfig = Config("$http$tabriz.$domain", PaymentType.PAYMENT_LESS_PARK_LESS)
        private val paymentLessParkLessSarabConfig = Config("$http$sarab.$domain", PaymentType.PAYMENT_LESS_PARK_LESS)
        val selectedConfig = testConfig // todo release

        data class Config(val baseUrl: String, val paymentType : PaymentType)

        val paymentIsSaman = selectedConfig.paymentType == PaymentType.SAMAN
        val paymentIsParsian = selectedConfig.paymentType == PaymentType.PARSIAN
        val paymentIsBehPardakht = selectedConfig.paymentType == PaymentType.BEH_PARDAKHT
        val isPaymentLess = selectedConfig.paymentType == PaymentType.PAYMENT_LESS
        val isPaymentLessParkLess = selectedConfig.paymentType == PaymentType.PAYMENT_LESS_PARK_LESS

        var ticketMessage: Map<String, TicketMessage>? = null
    }

    enum class PaymentType {
        PAYMENT_LESS,
        PAYMENT_LESS_PARK_LESS,
        SAMAN,
        PARSIAN,
        BEH_PARDAKHT,
    }
}