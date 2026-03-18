package io.github.hazemafaneh.networkinspectionpro.internal.utils

import android.content.Context

internal object ApplicationContextHolder {
    lateinit var applicationContext: Context
        private set

    fun init(context: Context) {
        applicationContext = context.applicationContext
    }
}
