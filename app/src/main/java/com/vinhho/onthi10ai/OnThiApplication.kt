package com.vinhho.onthi10ai

import android.app.Application
import com.vinhho.onthi10ai.data.local.OnThiDatabase
import com.vinhho.onthi10ai.data.repository.AiRepository
import com.vinhho.onthi10ai.data.repository.AuthRepository
import com.vinhho.onthi10ai.data.repository.HybridAiRepository
import com.vinhho.onthi10ai.data.repository.LocalAuthRepository
import com.vinhho.onthi10ai.data.repository.LocalStudyHistoryRepository
import com.vinhho.onthi10ai.data.repository.StudyHistoryRepository
import com.vinhho.onthi10ai.util.ProManager
import com.vinhho.onthi10ai.util.ThemeManager

class OnThiApplication : Application() {

    lateinit var authRepository: AuthRepository
        private set

    lateinit var aiRepository: AiRepository
        private set

    lateinit var studyHistoryRepository: StudyHistoryRepository
        private set

    lateinit var proManager: ProManager
        private set

    lateinit var themeManager: ThemeManager
        private set

    override fun onCreate() {
        super.onCreate()
        themeManager = ThemeManager(this)
        themeManager.applySavedTheme()
        val database = OnThiDatabase.getInstance(this)
        val dao = database.onThiDao()
        authRepository = LocalAuthRepository(this, dao)
        aiRepository = HybridAiRepository(this)
        studyHistoryRepository = LocalStudyHistoryRepository(dao, this)
        proManager = ProManager(this)
    }
}
