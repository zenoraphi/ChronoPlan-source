package com.chronoplan.work

import android.content.Context
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        agendaId: String,
        title: String,
        desc: String,
        delayInMinutes: Long
    ) {
        if (delayInMinutes <= 0) return

        val inputData = Data.Builder()
            .putString("agendaId", agendaId)
            .putString("title", title)
            .putString("desc", desc)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delayInMinutes, TimeUnit.MINUTES)
            .addTag("agenda_reminder_$agendaId")
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelReminder(context: Context, agendaId: String) {
        WorkManager.getInstance(context).cancelAllWorkByTag("agenda_reminder_$agendaId")
    }
}