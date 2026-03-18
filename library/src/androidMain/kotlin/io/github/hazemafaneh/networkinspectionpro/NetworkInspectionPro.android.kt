package io.github.hazemafaneh.networkinspectionpro

import android.content.Context
import io.github.hazemafaneh.networkinspectionpro.internal.utils.ApplicationContextHolder

fun NetworkInspectionPro.init(context: Context) {
    ApplicationContextHolder.init(context)
    enable()
}
