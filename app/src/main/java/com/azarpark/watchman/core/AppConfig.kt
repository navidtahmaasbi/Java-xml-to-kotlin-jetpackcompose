package com.azarpark.watchman.core

import com.azarpark.watchman.models.TicketMessage

class AppConfig {

    companion object {
        const val domain = "backend1.azarpark.irana.app"
        const val http = "https://"
        const val tabriz = "tabriz"
        const val sarab = "sarab"
        const val ardabil = "ardabil"
        val cityPayment = mapOf(
            tabriz to PaymentType.SAMAN,
            sarab to PaymentType.PARSIAN,
            ardabil to PaymentType.BEH_PARDAKHT,
//            ardabil to PaymentType.SAMAN,
        )
        var selectedConfig = Config(
            baseUrl = "$http$tabriz.$domain",
            paymentType = PaymentType.SAMAN,
            tabriz
        ) // default to tabriz

        data class Config(val baseUrl: String, val paymentType: PaymentType, val city: String)

        fun paymentIsSaman() = selectedConfig.paymentType == PaymentType.SAMAN
        fun paymentIsParsian() = selectedConfig.paymentType == PaymentType.PARSIAN
        fun paymentIsBehPardakht() = selectedConfig.paymentType == PaymentType.BEH_PARDAKHT
        fun isPaymentLess() = selectedConfig.paymentType == PaymentType.PAYMENT_LESS
        fun isPaymentLessParkLess() =
            selectedConfig.paymentType == PaymentType.PAYMENT_LESS_PARK_LESS

        fun isTabriz() = selectedConfig.city == tabriz
        fun isArdabil() = selectedConfig.city == ardabil

        var ticketMessage: Map<String, TicketMessage>? = null

        fun buildConfig(subdomain: String): Config {
            if (subdomain == null || subdomain.isEmpty()) {
                return Config(
                    baseUrl = "$http$tabriz.$domain",
                    paymentType = PaymentType.SAMAN,
                    tabriz
                );
            }

            return Config(
                "$http$subdomain.$domain",
                cityPayment[subdomain] ?: PaymentType.PAYMENT_LESS,
                subdomain
            )
        }
    }

    enum class PaymentType {
        PAYMENT_LESS,
        PAYMENT_LESS_PARK_LESS,
        SAMAN,
        PARSIAN,
        BEH_PARDAKHT,
    }
}