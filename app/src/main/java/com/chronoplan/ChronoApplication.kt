package com.chronoplan

import android.app.Application
import com.chronoplan.di.AppContainer

/**
 * Application utama ChronoPlan.
 * Digunakan untuk inisialisasi dependency global (Firebase, repository, dsb).
 */
class ChronoApplication : Application() {

    // Singleton container untuk dependency injection manual (tanpa Hilt)
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        // Inisialisasi container global
        container = AppContainer
    }
}
