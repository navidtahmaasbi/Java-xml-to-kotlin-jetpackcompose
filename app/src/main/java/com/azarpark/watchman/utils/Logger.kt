package com.azarpark.watchman.utils

import android.os.Build
import android.util.Log
import com.azarpark.watchman.BuildConfig
import java.util.regex.Pattern

class Logger {
    val tag: String?
        get() = Throwable().stackTrace[3]
            .let(::createStackElementTag)


    protected  fun createStackElementTag(element: StackTraceElement): String? {
        var tag = element.className.substringAfterLast('.')
        val m = ANONYMOUS_CLASS.matcher(tag)
        if (m.find()) {
            tag = m.replaceAll("")
        }
        // Tag length limit was removed in API 26.
        return if (tag.length <= MAX_TAG_LENGTH || Build.VERSION.SDK_INT >= 26) {
            tag
        } else {
            tag.substring(0, MAX_TAG_LENGTH)
        }
    }

    private fun prepareLog(message: String, vararg args: Any?): String?
    {
        if(BuildConfig.DEBUG)
        {
            return String.format(message, *args)
        }
        return null
    }

    companion object {
        private const val MAX_TAG_LENGTH = 23
        private val ANONYMOUS_CLASS = Pattern.compile("(\\$\\d+)+$")
        val logger = Logger()

        @JvmStatic
        fun d(message: String, vararg args: Any?)
        {
            logger.prepareLog(message, *args)?.let {
                Log.d(logger.tag, it)
            }
        }

        @JvmStatic
        fun i(message: String, vararg args: Any?)
        {
            logger.prepareLog(message, *args)?.let {
                Log.i(logger.tag, it)
            }
        }

        @JvmStatic
        fun w(message: String, vararg args: Any?)
        {
            logger.prepareLog(message, *args)?.let {
                Log.w(logger.tag, it)
            }
        }

        @JvmStatic
        fun e(message: String, vararg args: Any?)
        {
            logger.prepareLog(message, *args)?.let {
                Log.e(logger.tag, it)
            }
        }
    }
}