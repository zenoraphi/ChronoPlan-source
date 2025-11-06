package com.chronoplan.work

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    /**
     * Menjadwalkan notifikasi pengingat agenda
     * @param context Context aplikasi
     * @param title Judul agenda
     * @param desc Deskripsi agenda
     * @param delayInMinutes Jeda waktu sebelum reminder dikirim
     */
    fun scheduleReminder(
        context: Context,
        title: String,
        desc: String,
        delayInMinutes: Long
    ) {
        val inputData = Data.Builder()
            .putString("title", title)
            .putString("desc", desc)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}
