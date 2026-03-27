package com.timeforyou.app

import android.app.Application
import com.timeforyou.app.data.local.AppDatabase
import com.timeforyou.app.data.repository.TimeRepository
import com.timeforyou.app.data.repository.TimeRepositoryImpl

class TimeForYouApplication : Application() {

    lateinit var repository: TimeRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = TimeRepositoryImpl(db.behaviorLogDao())
    }
}
