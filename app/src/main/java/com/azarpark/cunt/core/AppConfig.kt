package com.azarpark.cunt.core

import com.azarpark.cunt.models.TicketMessage
import com.azarpark.cunt.utils.Constants
import com.azarpark.cunt.utils.SharedPreferencesRepository

class AppConfig {

    companion object {
        const val domain = "backend1.azarpark.irana.app"
        const val http = "https://"
        const val tabriz = "tabriz"
        const val sarab = "sarab"
        const val ardabil = "ardabil"
        val cityPayment = mapOf(
//            tabriz to PaymentType.SAMAN,
//            tabriz to PaymentType.SAMAN,
//            sarab to PaymentType.PARSIAN,
//            ardabil to PaymentType.BEH_PARDAKHT,
            tabriz to getPaymentTypeFromPreferences(Constants.PAYMENT_TYPE_TABRIZ),
            sarab to getPaymentTypeFromPreferences(Constants.PAYMENT_TYPE_SARAB),
            ardabil to getPaymentTypeFromPreferences(Constants.PAYMENT_TYPE_ARDABIL),
//            ardabil to PaymentType.SAMAN,
        )

        private fun getPaymentTypeFromPreferences(key: String): PaymentType {
            val paymentTypeString = SharedPreferencesRepository.getValue(key)
            return try {
                PaymentType.valueOf(paymentTypeString)
            } catch (e: IllegalArgumentException) {
                PaymentType.PAYMENT_LESS // default to PAYMENT_LESS if conversion fails
            }
        }

        var selectedConfig = Config(
            baseUrl = "$http$tabriz.$domain",
            paymentType = PaymentType.SAMAN,
            tabriz
        ) // default to tabriz

        @JvmStatic
        fun getCurrentConfig(): Config {
            return selectedConfig
        }

        @JvmStatic
        fun updateSelectedConfig(newConfig: Config) {
            selectedConfig = newConfig
        }

        @JvmStatic
        fun setPaymentType(paymentType: PaymentType) {
            selectedConfig = selectedConfig.copy(paymentType = paymentType)
        }


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
            if (subdomain.isEmpty()) {
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