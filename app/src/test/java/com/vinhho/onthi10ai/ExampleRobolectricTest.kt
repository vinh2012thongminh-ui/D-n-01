package com.vinhho.onthi10ai

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import androidx.test.core.app.ApplicationProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vinhho.onthi10ai.data.model.ChatMessage
import com.vinhho.onthi10ai.data.repository.HybridAiRepository
import com.vinhho.onthi10ai.data.repository.LocalAuthRepository
import com.vinhho.onthi10ai.data.solver.OfflineSolveResult
import com.vinhho.onthi10ai.data.solver.OfflineSolverEngine
import com.vinhho.onthi10ai.ui.chat.ChatFragment
import com.vinhho.onthi10ai.ui.login.LoginActivity
import com.vinhho.onthi10ai.ui.main.MainActivity
import com.vinhho.onthi10ai.util.ProManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read updated app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Gia Sư AI - Học Tập 12 Cấp", appName)
    }

    @Test
    fun `nlp parser handles vietnamese words without accents and solves Hai cong ba`() {
        val engine = OfflineSolverEngine()

        val haiCongBa = engine.solve("Hai cong ba", hasImage = false)
        assertTrue(haiCongBa is OfflineSolveResult.Solved)
        val solved1 = haiCongBa as OfflineSolveResult.Solved
        assertEquals("Toán học (Cấp 1)", solved1.detectedSubject)
        assertTrue(solved1.answer.contains("2 + 3 = 5"))
        assertTrue(solved1.answer.contains("5"))
        assertTrue(solved1.answer.contains("Năm"))

        val accented = engine.solve("hai cộng ba", hasImage = false)
        assertTrue(accented is OfflineSolveResult.Solved)
        assertTrue((accented as OfflineSolveResult.Solved).answer.contains("2 + 3 = 5"))

        val chained = engine.solve("15 nhan 4 chia 2", hasImage = false)
        assertTrue(chained is OfflineSolveResult.Solved)
        assertTrue((chained as OfflineSolveResult.Solved).answer.contains("30"))

        val radical48 = engine.solve("căn bậc hai của 48", hasImage = false)
        assertTrue(radical48 is OfflineSolveResult.Solved)
        assertTrue((radical48 as OfflineSolveResult.Solved).answer.contains("4√3"))
    }

    @Test
    fun `offline solver computes complex arithmetic equations and physics chemistry units`() {
        val engine = OfflineSolverEngine()

        val complexArith = engine.solve("Tính (12 + 8) * 3 / 5", hasImage = false)
        assertTrue(complexArith is OfflineSolveResult.Solved)
        assertTrue((complexArith as OfflineSolveResult.Solved).answer.contains("12"))

        val linear = engine.solve("tìm x biết 2x + 5 = 15", hasImage = false)
        assertTrue(linear is OfflineSolveResult.Solved)
        assertTrue((linear as OfflineSolveResult.Solved).answer.contains("x = 5"))

        val quadratic = engine.solve("giải phương trình x^2 - 5x + 6 = 0", hasImage = false)
        assertTrue(quadratic is OfflineSolveResult.Solved)
        val quadAnswer = (quadratic as OfflineSolveResult.Solved).answer
        assertTrue(quadAnswer.contains("3"))
        assertTrue(quadAnswer.contains("2"))

        val ohmLaw = engine.solve("Cho mạch điện có U = 210V, R = 70Ω. Tính I?", hasImage = false)
        assertTrue(ohmLaw is OfflineSolveResult.Solved)
        val ohmSolved = ohmLaw as OfflineSolveResult.Solved
        assertEquals("Vật lí", ohmSolved.detectedSubject)
        assertTrue(ohmSolved.answer.contains("3 A"))

        val kinetic = engine.solve("Vật có khối lượng m = 400g chuyển động với v = 2m/s", hasImage = false)
        assertTrue(kinetic is OfflineSolveResult.Solved)
        val kineticSolved = kinetic as OfflineSolveResult.Solved
        assertEquals("Vật lí", kineticSolved.detectedSubject)
        assertTrue(kineticSolved.answer.contains("0.4 kg"))
        assertTrue(kineticSolved.answer.contains("0.8 J"))

        val chemMol = engine.solve("Tính số mol của m = 5,6g Fe", hasImage = false)
        assertTrue(chemMol is OfflineSolveResult.Solved)
        val chemSolved = chemMol as OfflineSolveResult.Solved
        assertEquals("Hóa học", chemSolved.detectedSubject)
        assertTrue(chemSolved.answer.contains("0.1 mol"))
    }

    @Test
    fun `fuzzy knowledge search works without accents and supports minor typos`() {
        val engine = OfflineSolverEngine()

        val identities = engine.solve("hang dang thuc", hasImage = false)
        assertTrue(identities is OfflineSolveResult.Solved)
        assertTrue((identities as OfflineSolveResult.Solved).answer.contains("(A + B)²"))

        val passive = engine.solve("cau bi dong", hasImage = false)
        assertTrue(passive is OfflineSolveResult.Solved)
        assertTrue((passive as OfflineSolveResult.Solved).answer.contains("Passive Voice"))

        val ohmFormula = engine.solve("dinh luat om", hasImage = false)
        assertTrue(ohmFormula is OfflineSolveResult.Solved)
        assertTrue((ohmFormula as OfflineSolveResult.Solved).answer.contains("I = U / R"))
    }

    @Test
    fun `hybrid repository answers biology offline and returns friendly internet prompt for images or deep essays`() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            val repository = HybridAiRepository(context)

            val bio = repository.solveExamQuestion("Con người có mấy gen?", null).getOrThrow()
            assertTrue(bio.detectedSubject.startsWith("Sinh học"))
            assertTrue(bio.isOfflineSolved)
            assertFalse(bio.needsOnlineAnalysis)
            assertTrue(bio.answer.contains("20.000"))

            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            Shadows.shadowOf(cm).setDefaultNetworkActive(false)
            Shadows.shadowOf(cm).setActiveNetworkInfo(null)
            cm.activeNetwork?.let { Shadows.shadowOf(cm).setNetworkCapabilities(it, null) }

            val dummyBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
            val imagePrompt = repository.solveExamQuestion("", dummyBitmap).getOrThrow()
            assertFalse(imagePrompt.isOfflineSolved)
            assertTrue(imagePrompt.needsOnlineAnalysis)
            assertEquals(context.getString(R.string.ai_needs_internet_friendly), imagePrompt.answer)
        }

    private fun simulateOffline(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        Shadows.shadowOf(cm).setDefaultNetworkActive(false)
        Shadows.shadowOf(cm).setActiveNetworkInfo(null)
        cm.activeNetwork?.let { Shadows.shadowOf(cm).setNetworkCapabilities(it, null) }
    }

    @Test
    fun `quiz generator filters KHTN strictly to grade 9 physics chemistry biology without math or other grades`() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            simulateOffline(context)
            val repository = HybridAiRepository(context)

            val khtnResult = repository.generatePracticeQuiz("Lớp 9 (Ôn thi vào 10)", "Khtn", 35).getOrThrow()
            assertEquals("Lớp 9", khtnResult.gradeLevel)
            assertEquals("KHTN", khtnResult.detectedSubject)
            assertEquals(35, khtnResult.questions.size)
            assertEquals(35, khtnResult.questions.map { it.question }.toSet().size)

            val forbiddenKeywords = listOf("tam giác", "đường tròn", "hàm số", "vi-ét", "x²", "Lớp 12", "Lớp 10", "Lớp 6")
            khtnResult.questions.forEach { q ->
                assertTrue("Expected Grade 9 KHTN tag in question: ${q.question}", q.question.contains("Lớp 9"))
                val lowerQ = q.question.lowercase()
                forbiddenKeywords.forEach { kw ->
                    assertFalse("Grade 9 KHTN quiz must not contain forbidden keyword '$kw': ${q.question}", lowerQ.contains(kw.lowercase()))
                }
            }
        }

    @Test
    fun `hybrid repository generates 30 to 40 unique randomized quiz questions tailored across all 12 grades`() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            simulateOffline(context)
            val repository = HybridAiRepository(context)

            val cases = listOf(
                Triple("Lớp 1", "Toán", 30) to "Lớp 1",
                Triple("Lớp 5", "Tiếng Anh", 32) to "Lớp 5",
                Triple("Lớp 6", "KHTN", 34) to "Lớp 6",
                Triple("Lớp 9 (Ôn thi vào 10)", "Toán", 35) to "Lớp 9",
                Triple("Lớp 9 (Ôn thi vào 10)", "Ngữ văn", 30) to "Lớp 9",
                Triple("Lớp 10", "Vật lí", 36) to "Lớp 10",
                Triple("Lớp 11", "Hóa học", 38) to "Lớp 11",
                Triple("Lớp 12 (Ôn thi THPT)", "Toán", 40) to "Lớp 12"
            )

            cases.forEach { (input, expectedGrade) ->
                val (rawGrade, topic, count) = input
                val result = repository.generatePracticeQuiz(rawGrade, topic, count)
                assertTrue("Expected success for $rawGrade - $topic", result.isSuccess)
                val generated = result.getOrThrow()
                assertEquals(expectedGrade, generated.gradeLevel)
                assertEquals(count, generated.questions.size)
                assertEquals(count, generated.questions.map { it.question }.toSet().size)
                generated.questions.forEach { q ->
                    assertTrue("Question should match $expectedGrade: ${q.question}", q.question.contains(expectedGrade))
                    assertEquals(4, q.options.size)
                    assertTrue(q.correctIndex in 0..3)
                    assertTrue(q.options[0].startsWith("A. "))
                    assertTrue(q.options[1].startsWith("B. "))
                    assertTrue(q.options[2].startsWith("C. "))
                    assertTrue(q.options[3].startsWith("D. "))
                    assertTrue(q.explanation.isNotBlank())
                }
            }

            val prompt = repository.buildGeminiQuizPrompt("Lớp 9", "KHTN", "Điện học", 35)
            assertTrue(prompt.contains("Lớp 9"))
            assertTrue(prompt.contains("KHTN"))
            assertTrue(prompt.contains("35"))
        }

    @Test
    fun `pro manager activates and deactivates subscription state`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
        val proManager = app.proManager
        proManager.deactivatePro()
        assertFalse(proManager.isPro.value)

        assertEquals("24.500đ", app.getString(R.string.pro_plan_monthly_new))
        assertEquals("149.500đ", app.getString(R.string.pro_plan_yearly_new))
        assertEquals("299.500đ", app.getString(R.string.pro_plan_lifetime_new))

        val contestMessage = app.getString(R.string.pro_contest_dialog_message, ProManager.PLAN_YEARLY)
        assertTrue(contestMessage.contains("bài dự thi"))
        assertTrue(contestMessage.contains("toàn bộ tính năng PRO"))

        app.authRepository.signInWithGoogle("Nguyễn Minh Anh", "minhanh@gmail.com").getOrThrow()
        val mainController = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = mainController.get()
        val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_history
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val btnToolbarPro = activity.findViewById<android.view.View>(R.id.btnToolbarPro)
        val btnHistoryPro = activity.findViewById<android.view.View>(R.id.btnHistoryPro)
        val tvProCardTitle = activity.findViewById<android.widget.TextView>(R.id.tvProCardTitle)
        assertEquals(android.view.View.VISIBLE, btnToolbarPro.visibility)
        assertEquals(android.view.View.VISIBLE, btnHistoryPro.visibility)

        proManager.activatePro(ProManager.PLAN_YEARLY)
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertTrue(proManager.isPro.value)
        assertEquals(ProManager.PLAN_YEARLY, proManager.currentPlan.value)
        assertEquals(android.view.View.GONE, btnToolbarPro.visibility)
        assertEquals(android.view.View.GONE, btnHistoryPro.visibility)
        assertEquals("Chào bạn, thành viên PRO!", tvProCardTitle.text.toString())

        proManager.deactivatePro()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertFalse(proManager.isPro.value)
        assertEquals(ProManager.PLAN_FREE, proManager.currentPlan.value)
    }

    @Test
    fun `auth startup shows LoginActivity unless signed in with Google or Email and signOut returns cleanly`() =
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
            val authRepo = app.authRepository
            authRepo.signOut()

            assertNull(authRepo.getCurrentUser())
            assertFalse(authRepo.isRealUserLoggedIn())

            // Cold start without login keeps LoginActivity open and never auto-signs in anonymously
            val coldController = Robolectric.buildActivity(LoginActivity::class.java).setup()
            assertFalse(coldController.get().isFinishing)
            assertNull(authRepo.getCurrentUser())

            // Explicit guest login works for current session, but does NOT bypass LoginActivity on next cold launch
            val guestUser = authRepo.signInAnonymously().getOrThrow()
            assertTrue(guestUser.isGuest)
            assertFalse(authRepo.isRealUserLoggedIn())

            val guestRelaunchController = Robolectric.buildActivity(LoginActivity::class.java).setup()
            assertFalse(guestRelaunchController.get().isFinishing)
            assertNull(authRepo.getCurrentUser())

            // Google login creates a real account session and allows auto-redirect until signed out
            val googleUser = authRepo.signInWithGoogle("Nguyễn Minh Anh", "minhanh@gmail.com").getOrThrow()
            assertFalse(googleUser.isGuest)
            assertEquals(LocalAuthRepository.PROVIDER_GOOGLE, googleUser.authProvider)
            assertTrue(authRepo.isRealUserLoggedIn())

            val googleRelaunchController = Robolectric.buildActivity(LoginActivity::class.java).setup()
            assertTrue(googleRelaunchController.get().isFinishing)

            // Sign out wipes the session and shows LoginActivity again
            authRepo.signOut()
            assertNull(authRepo.getCurrentUser())
            assertFalse(authRepo.isRealUserLoggedIn())

            val afterLogoutController = Robolectric.buildActivity(LoginActivity::class.java).setup()
            assertFalse(afterLogoutController.get().isFinishing)
        }

    @Test
    fun `math parser handles implicit multiplication and exponentiation precedence correctly`() {
        val engine = OfflineSolverEngine()

        val implicit1 = engine.solve("2(3+4)", hasImage = false)
        assertTrue(implicit1 is OfflineSolveResult.Solved)
        assertTrue((implicit1 as OfflineSolveResult.Solved).answer.contains("14"))

        val implicit2 = engine.solve("(1+2)(3+4)", hasImage = false)
        assertTrue(implicit2 is OfflineSolveResult.Solved)
        assertTrue((implicit2 as OfflineSolveResult.Solved).answer.contains("21"))

        val implicitSqrt = engine.solve("2sqrt(9)", hasImage = false)
        assertTrue(implicitSqrt is OfflineSolveResult.Solved)
        assertTrue((implicitSqrt as OfflineSolveResult.Solved).answer.contains("6"))

        val negPow = engine.solve("-3^2", hasImage = false)
        assertTrue(negPow is OfflineSolveResult.Solved)
        assertTrue(
            "Expected -3^2 to evaluate to -9, got: ${(negPow as OfflineSolveResult.Solved).answer}",
            negPow.answer.contains("-9")
        )

        val parenNegPow = engine.solve("(-3)^2", hasImage = false)
        assertTrue(parenNegPow is OfflineSolveResult.Solved)
        val parenAnswer = (parenNegPow as OfflineSolveResult.Solved).answer
        assertTrue("Expected (-3)^2 to evaluate to 9, got: $parenAnswer", parenAnswer.contains("= **9**") || parenAnswer.contains("9"))
        assertFalse("Expected (-3)^2 not to evaluate to -9", parenAnswer.contains("-9"))
    }

    @Test
    fun `offline quiz bank produces 40 unique non-numbered questions for all subjects and prioritizes topic`() =
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            simulateOffline(context)
            val repository = HybridAiRepository(context)

            val subjects = listOf(
                "Lớp 3" to "Tiếng Anh",
                "Lớp 9" to "Tiếng Anh",
                "Lớp 12" to "Tiếng Anh",
                "Lớp 4" to "Ngữ văn",
                "Lớp 9" to "Ngữ văn",
                "Lớp 12" to "Ngữ văn",
                "Lớp 9" to "Sinh học",
                "Lớp 12" to "Sinh học",
                "Lớp 5" to "Lịch sử",
                "Lớp 9" to "Lịch sử",
                "Lớp 12" to "Lịch sử",
                "Lớp 5" to "Địa lí",
                "Lớp 9" to "Địa lí",
                "Lớp 12" to "Địa lí",
                "Lớp 9" to "Vật lí",
                "Lớp 12" to "Hóa học",
                "Lớp 1" to "Toán"
            )

            subjects.forEach { (grade, subjectTopic) ->
                val result = repository.generatePracticeQuiz(grade, subjectTopic, 40).getOrThrow()
                assertEquals("Expected 40 questions for $grade - $subjectTopic", 40, result.questions.size)
                assertEquals(
                    "Expected 40 distinct questions for $grade - $subjectTopic",
                    40,
                    result.questions.map { it.question }.toSet().size
                )
                result.questions.forEach { q ->
                    assertFalse("Must not contain numbered filler '(Câu <N>': ${q.question}", q.question.contains(Regex("\\(Câu \\d+")))
                    assertFalse("Must not contain synthetic '(lần thử': ${q.question}", q.question.contains("(lần thử"))
                    assertFalse("Must not contain synthetic '(với i': ${q.question}", q.question.contains("(với i"))
                }
            }

            val kieuQuiz = repository.generatePracticeQuiz("Lớp 9", "Ngữ văn - Truyện Kiều", 30).getOrThrow()
            assertEquals("Ngữ văn", kieuQuiz.detectedSubject)
            assertTrue(
                "Expected Truyện Kiều questions prioritized at top, first was: ${kieuQuiz.questions.first().question}",
                kieuQuiz.questions.take(3).any {
                    it.question.contains("Truyện Kiều") || it.explanation.contains("Truyện Kiều")
                }
            )

            val dbpQuiz = repository.generatePracticeQuiz("Lớp 12", "Lịch sử - Chiến thắng Điện Biên Phủ", 30).getOrThrow()
            assertEquals("Lịch sử", dbpQuiz.detectedSubject)
            assertTrue(
                "Expected Điện Biên Phủ questions prioritized at top, first was: ${dbpQuiz.questions.first().question}",
                dbpQuiz.questions.take(3).any {
                    it.question.contains("Điện Biên Phủ") || it.explanation.contains("Điện Biên Phủ")
                }
            )

            val english12 = repository.generatePracticeQuiz("Lớp 12", "Tiếng Anh", 40).getOrThrow()
            english12.questions.forEach { q ->
                assertFalse("Draft volunteer note must be removed", q.explanation.contains("volunteer"))
            }
        }

    @Test
    fun `email registration login and study history saving work reliably when offline`() =
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
            simulateOffline(app)
            val authRepo = app.authRepository
            val historyRepo = app.studyHistoryRepository
            authRepo.signOut()

            val email = "hocsinh.test@example.com"
            val registered = authRepo.registerWithEmail(email, "123456").getOrThrow()
            assertFalse(registered.isGuest)
            assertEquals(LocalAuthRepository.PROVIDER_EMAIL, registered.authProvider)
            assertTrue(authRepo.isRealUserLoggedIn())

            authRepo.signOut()
            assertFalse(authRepo.isRealUserLoggedIn())

            val loggedIn = authRepo.loginWithEmail(email, "123456").getOrThrow()
            assertEquals(registered.uid, loggedIn.uid)
            assertTrue(authRepo.isRealUserLoggedIn())

            // Save tutor history & exam result offline (Firestore sync should gracefully skip/catch without throwing)
            historyRepo.saveTutorHistory(loggedIn.uid, "Toán", "2 + 3 = ?", "Kết quả là 5")
            historyRepo.saveExamResult(loggedIn.uid, "Toán", "Lớp 9 - Đại số", 35, 40)

            assertEquals("Gia Sư AI - Học Tập 12 Cấp", app.getString(R.string.app_name))
            assertEquals("Gia sư AI", app.getString(R.string.tab_tutor))
            assertEquals("Trò chuyện", app.getString(R.string.tab_chat))
            assertEquals("Luyện đề", app.getString(R.string.tab_quiz))
            assertEquals("Hồ sơ", app.getString(R.string.tab_history))
        }

    @Test
    fun `multi-turn chat solves offline maintains 10-message memory and MainActivity switches to ChatFragment`() =
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
            simulateOffline(app)
            val repository = HybridAiRepository(app)

            val history = mutableListOf(
                ChatMessage(
                    text = app.getString(R.string.chat_welcome_message),
                    isFromUser = false,
                    isOfflineReply = true,
                    isWelcome = true
                )
            )

            // Turn 1: Solve quadratic equation offline inside chat
            val q1 = "x^2 - 5x + 6 = 0"
            val r1 = repository.sendChatMessage(history, q1).getOrThrow()
            assertTrue(r1.isOfflineReply)
            assertTrue(r1.text.contains("3") && r1.text.contains("2"))
            history.add(ChatMessage(text = q1, isFromUser = true))
            history.add(ChatMessage(text = r1.text, isFromUser = false, isOfflineReply = true))

            // Turn 2: Follow-up "Giải thích dễ hiểu hơn" leverages prior quadratic context
            val q2 = "Giải thích dễ hiểu hơn"
            val r2 = repository.sendChatMessage(history, q2).getOrThrow()
            assertTrue(r2.isOfflineReply)
            assertTrue(r2.text.contains("Δ"))
            history.add(ChatMessage(text = q2, isFromUser = true))
            history.add(ChatMessage(text = r2.text, isFromUser = false, isOfflineReply = true))

            // Turn 3: Follow-up "Cho ví dụ tương tự" produces a fresh quadratic example
            val q3 = "Cho ví dụ tương tự"
            val r3 = repository.sendChatMessage(history, q3).getOrThrow()
            assertTrue(r3.text.contains("x² - 7x + 10 = 0"))

            // Turn 4: Emotional stress support & Pomodoro tips
            val stressReply = repository.sendChatMessage(history, "Mình đang áp lực và lo thi quá").getOrThrow()
            assertTrue(stressReply.text.contains("Hít một hơi"))
            val planReply = repository.sendChatMessage(history, "Lập kế hoạch ôn thi").getOrThrow()
            assertTrue(planReply.text.contains("Pomodoro"))

            // Verify 10-message context truncation & valid alternating Gemini history
            val longHistory = (1..14).map { idx ->
                ChatMessage(text = "Msg $idx", isFromUser = idx % 2 == 1)
            }
            val geminiHistory = repository.buildGeminiChatHistory(longHistory)
            assertTrue(geminiHistory.size <= HybridAiRepository.MAX_CHAT_HISTORY)
            assertEquals("user", geminiHistory.first().role)
            assertEquals("model", geminiHistory.last().role)

            // Verify MainActivity renders 4 tabs and switches to ChatFragment
            app.authRepository.signInWithGoogle("Nguyễn Minh Anh", "minhanh@gmail.com").getOrThrow()
            val mainController = Robolectric.buildActivity(MainActivity::class.java).setup()
            val activity = mainController.get()
            val bottomNav = activity.findViewById<BottomNavigationView>(R.id.bottomNavigation)
            assertEquals(4, bottomNav.menu.size())
            assertEquals(R.id.nav_tutor, bottomNav.menu.getItem(0).itemId)
            assertEquals(R.id.nav_chat, bottomNav.menu.getItem(1).itemId)
            assertEquals(R.id.nav_quiz, bottomNav.menu.getItem(2).itemId)
            assertEquals(R.id.nav_history, bottomNav.menu.getItem(3).itemId)

            bottomNav.selectedItemId = R.id.nav_chat
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
            val chatFragment = activity.supportFragmentManager.findFragmentByTag("tab_chat")
            assertTrue(chatFragment is ChatFragment)
        }

    @Test
    fun `pro manager persists fixed expiry for monthly and yearly plans emits renewal notice and stores lifetime permanently`() {
        val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
        val proManager = app.proManager
        proManager.deactivatePro()

        val startTime = 1_700_000_000_000L

        // 1. Monthly plan: fixed 30 days on device
        proManager.activatePro(ProManager.PLAN_MONTHLY, nowMillis = startTime)
        assertTrue(proManager.isPro.value)
        assertFalse(proManager.isLifetime.value)
        val expectedMonthlyExpiry = startTime + ProManager.DAYS_MONTHLY * ProManager.MILLIS_PER_DAY
        assertEquals(expectedMonthlyExpiry, proManager.expiresAtMillis.value)
        assertNull(proManager.pendingRenewalNotice.value)

        // Still active at day 29
        proManager.refreshSubscriptionStatus(startTime + 29L * ProManager.MILLIS_PER_DAY)
        assertTrue(proManager.isPro.value)
        assertNull(proManager.pendingRenewalNotice.value)

        // Re-instantiate ProManager to verify persisted on device, then advance to day 30
        val reloadedAfterMonth = ProManager(app)
        reloadedAfterMonth.refreshSubscriptionStatus(expectedMonthlyExpiry)
        assertFalse(reloadedAfterMonth.isPro.value)
        assertEquals(ProManager.PLAN_FREE, reloadedAfterMonth.currentPlan.value)
        val monthlyNotice = reloadedAfterMonth.pendingRenewalNotice.value
        assertEquals(ProManager.PLAN_MONTHLY, monthlyNotice?.expiredPlanName)
        assertEquals(expectedMonthlyExpiry, monthlyNotice?.expiredAtMillis)

        reloadedAfterMonth.consumeRenewalNotice()
        assertNull(reloadedAfterMonth.pendingRenewalNotice.value)

        // 2. Yearly plan: fixed 365 days on device
        proManager.activatePro(ProManager.PLAN_YEARLY, nowMillis = startTime)
        val expectedYearlyExpiry = startTime + ProManager.DAYS_YEARLY * ProManager.MILLIS_PER_DAY
        assertEquals(expectedYearlyExpiry, proManager.expiresAtMillis.value)
        proManager.refreshSubscriptionStatus(expectedYearlyExpiry + 1000L)
        assertFalse(proManager.isPro.value)
        assertEquals(ProManager.PLAN_YEARLY, proManager.pendingRenewalNotice.value?.expiredPlanName)
        proManager.consumeRenewalNotice()

        // 3. Lifetime plan: saved permanently on device and never expires
        proManager.activatePro(ProManager.PLAN_LIFETIME, nowMillis = startTime)
        assertTrue(proManager.isPro.value)
        assertTrue(proManager.isLifetime.value)
        assertEquals(ProManager.EXPIRY_PERMANENT, proManager.expiresAtMillis.value)
        assertTrue(proManager.formatExpiryLabel(app).contains("Vĩnh viễn trọn đời"))

        val tenYearsLater = startTime + 3650L * ProManager.MILLIS_PER_DAY
        val reloadedLifetime = ProManager(app)
        reloadedLifetime.refreshSubscriptionStatus(tenYearsLater)
        assertTrue(reloadedLifetime.isPro.value)
        assertTrue(reloadedLifetime.isLifetime.value)
        assertEquals(ProManager.PLAN_LIFETIME, reloadedLifetime.currentPlan.value)
        assertNull(reloadedLifetime.pendingRenewalNotice.value)
    }

    @Test
    fun `theme manager and UI toggles switch cleanly between light and dark modes`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
        val themeManager = app.themeManager
        themeManager.setDarkMode(false)
        assertFalse(themeManager.isDarkMode())
        assertEquals(com.vinhho.onthi10ai.util.ThemeManager.MODE_LIGHT, themeManager.themeMode.value)

        app.authRepository.signInWithGoogle("Nguyễn Minh Anh", "minhanh@gmail.com").getOrThrow()
        val mainController = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = mainController.get()

        // Toolbar theme button toggles Dark Mode on (AppCompatDelegate recreates the activity)
        val btnToolbarTheme = activity.findViewById<android.view.View>(R.id.btnToolbarTheme)
        btnToolbarTheme.performClick()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertTrue(themeManager.isDarkMode())
        assertEquals(com.vinhho.onthi10ai.util.ThemeManager.MODE_DARK, themeManager.themeMode.value)

        // Verify persisted across new ThemeManager instance
        val reloadedTheme = com.vinhho.onthi10ai.util.ThemeManager(app)
        assertTrue(reloadedTheme.isDarkMode())

        // Recreate MainActivity in Dark Mode, navigate to History tab, and switch back to Light Mode
        val darkActivity = Robolectric.buildActivity(MainActivity::class.java).setup().get()
        val bottomNav = darkActivity.findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.selectedItemId = R.id.nav_history
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

        val btnThemeLight = darkActivity.findViewById<android.view.View>(R.id.btnThemeLight)
        btnThemeLight.performClick()
        Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
        assertFalse(themeManager.isDarkMode())
        assertEquals(com.vinhho.onthi10ai.util.ThemeManager.MODE_LIGHT, themeManager.themeMode.value)
    }

    @Test
    fun `offline solver handles conversational prompts without misclassifying ai as Biology and supports fallback`() =
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
            val engine = OfflineSolverEngine()

            // "ai" in "bạn là ai" must NEVER be classified as Sinh học
            assertFalse(engine.detectSubject("bạn là ai", hasImage = false).contains("Sinh"))
            assertEquals("Gia sư AI", engine.detectSubject("bạn là ai", hasImage = false))

            val identityPrompts = listOf("xin chào", "hi", "hello", "bạn là ai", "tên gì", "bạn làm được gì")
            identityPrompts.forEach { prompt ->
                val reply = engine.tryConversationalReply(prompt)
                assertTrue("Expected non-null intro reply for '$prompt'", !reply.isNullOrBlank())
                assertTrue("Expected Gia sư AI 12 Cấp intro for '$prompt'", reply!!.contains("Gia sư AI 12 Cấp"))
                assertTrue("Expected quiz 30–40 mention for '$prompt'", reply.contains("30–40"))
            }

            val stressPrompts = listOf("mệt quá", "chán học", "áp lực", "lo lắng thi cử", "cách học hiệu quả")
            stressPrompts.forEach { prompt ->
                val reply = engine.tryConversationalReply(prompt)
                assertTrue("Expected non-null guidance reply for '$prompt'", !reply.isNullOrBlank())
                assertTrue("Expected Pomodoro guidance for '$prompt'", reply!!.contains("Pomodoro"))
            }

            val quickChips = listOf(
                "Lập kế hoạch ôn thi",
                "Mẹo ghi nhớ nhanh",
                "Giải thích dễ hiểu hơn",
                "Cho ví dụ tương tự"
            )
            quickChips.forEach { chip ->
                val reply = engine.tryConversationalReply(chip)
                assertTrue("Expected non-null quick chip reply for '$chip'", !reply.isNullOrBlank())
            }

            val repo = HybridAiRepository(app)
            val whoAreYou = repo.solveExamQuestion("bạn là ai", null).getOrThrow()
            assertTrue(whoAreYou.isOfflineSolved)
            assertEquals("Gia sư AI", whoAreYou.detectedSubject)
            assertTrue(whoAreYou.answer.contains("Gia sư AI 12 Cấp"))
        }

    @Test
    fun `clicking Google Sign-In opens fallback dialog when Play Services or CredentialManager unavailable`() =
        runBlocking {
            val app = ApplicationProvider.getApplicationContext<OnThiApplication>()
            app.authRepository.signOut()

            val controller = Robolectric.buildActivity(LoginActivity::class.java).setup()
            val activity = controller.get()

            val btnGoogle = activity.findViewById<android.view.View>(R.id.btnGoogleSignIn)
            btnGoogle.performClick()
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

            val dialog = org.robolectric.shadows.ShadowAlertDialog.getLatestDialog() as? androidx.appcompat.app.AlertDialog
            assertTrue("Expected Google fallback AlertDialog to be displayed", dialog != null && dialog.isShowing)

            val etName = dialog!!.findViewById<android.widget.EditText>(R.id.etGoogleName)
            val etEmail = dialog.findViewById<android.widget.EditText>(R.id.etGoogleEmail)
            etName?.setText("Trần Bảo Ngọc")
            etEmail?.setText("baongoc@gmail.com")

            dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE).performClick()
            Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

            assertTrue(app.authRepository.isRealUserLoggedIn())
            assertEquals("baongoc@gmail.com", app.authRepository.getCurrentUser()?.email)
        }
}


