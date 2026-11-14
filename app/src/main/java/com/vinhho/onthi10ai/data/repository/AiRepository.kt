package com.vinhho.onthi10ai.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Content
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.vinhho.onthi10ai.R
import com.vinhho.onthi10ai.data.model.ChatMessage
import com.vinhho.onthi10ai.data.model.ChatReply
import com.vinhho.onthi10ai.data.model.QuizGenerationResult
import com.vinhho.onthi10ai.data.model.QuizQuestion
import com.vinhho.onthi10ai.data.model.TutorSolution
import com.vinhho.onthi10ai.data.solver.OfflineQuizBank
import com.vinhho.onthi10ai.data.solver.OfflineSolveResult
import com.vinhho.onthi10ai.data.solver.OfflineSolverEngine
import com.vinhho.onthi10ai.util.NetworkUtils
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.roundToInt

interface AiRepository {
    suspend fun solveExamQuestion(
        questionText: String,
        imageBitmap: Bitmap?
    ): Result<TutorSolution>

    suspend fun generatePracticeQuiz(
        selectedGrade: String,
        topicPrompt: String,
        questionCount: Int
    ): Result<QuizGenerationResult>

    suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String
    ): Result<ChatReply>
}

class HybridAiRepository(
    private val context: Context,
    private val solverEngine: OfflineSolverEngine = OfflineSolverEngine(),
    private val quizBank: OfflineQuizBank = OfflineQuizBank()
) : AiRepository {

    private val tutorModel by lazy {
        Firebase.ai.generativeModel(modelName = MODEL_GEMINI_FLASH)
    }

    private val quizResponseSchema by lazy {
        Schema.obj(
            properties = mapOf(
                "questions" to Schema.array(
                    items = Schema.obj(
                        properties = mapOf(
                            "question" to Schema.string(description = "Nội dung câu hỏi trắc nghiệm bằng tiếng Việt"),
                            "options" to Schema.array(
                                items = Schema.string(),
                                description = "Đúng 4 phương án trả lời A, B, C, D"
                            ),
                            "correctIndex" to Schema.integer(description = "Chỉ số đáp án đúng từ 0 đến 3"),
                            "explanation" to Schema.string(description = "Giải thích ngắn gọn, dễ hiểu tại sao đáp án đó đúng")
                        )
                    ),
                    description = "Danh sách câu hỏi trắc nghiệm sát khối lớp và môn học"
                )
            )
        )
    }

    private val quizModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_GEMINI_FLASH,
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = quizResponseSchema
            }
        )
    }

    private val chatModel by lazy {
        Firebase.ai.generativeModel(
            modelName = MODEL_GEMINI_FLASH,
            systemInstruction = content {
                text(
                    "Bạn là anh/chị Gia sư AI thân thiện, kiên nhẫn và tâm lý dành cho học sinh Việt Nam từ Lớp 1 đến Lớp 12. " +
                        "Hãy trò chuyện tự nhiên, gần gũi như một người anh/chị gia sư giỏi: " +
                        "1) Ghi nhớ mạch hội thoại để trả lời đúng trọng tâm câu hỏi nối tiếp (ví dụ giải thích kỹ bước trước, cho thêm ví dụ mới). " +
                        "2) Giảng bài rõ ràng, dễ hiểu, từng bước mạch lạc theo đúng cấp học của học sinh. " +
                        "3) Khi học sinh chia sẻ áp lực, mệt mỏi hoặc lo lắng thi cử, hãy lắng nghe, động viên tích cực và gợi ý cách học hiệu quả."
                )
            }
        )
    }

    override suspend fun solveExamQuestion(
        questionText: String,
        imageBitmap: Bitmap?
    ): Result<TutorSolution> = withContext(Dispatchers.Default) {
        val cleanPrompt = questionText.trim()
        val hasImage = imageBitmap != null

        when (val solved = solverEngine.solve(cleanPrompt, hasImage)) {
            is OfflineSolveResult.Solved -> {
                Result.success(
                    TutorSolution(
                        detectedSubject = solved.detectedSubject,
                        answer = solved.answer,
                        isOfflineSolved = true,
                        needsOnlineAnalysis = false
                    )
                )
            }
            is OfflineSolveResult.NeedsOnline -> {
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    return@withContext Result.success(
                        TutorSolution(
                            detectedSubject = solved.detectedSubject,
                            answer = context.getString(R.string.ai_needs_internet_friendly),
                            isOfflineSolved = false,
                            needsOnlineAnalysis = true
                        )
                    )
                }

                solveWithFirebaseAi(
                    questionText = cleanPrompt,
                    imageBitmap = imageBitmap,
                    fallbackSubject = solved.detectedSubject
                )
            }
        }
    }

    private suspend fun solveWithFirebaseAi(
        questionText: String,
        imageBitmap: Bitmap?,
        fallbackSubject: String
    ): Result<TutorSolution> {
        return try {
            val scaledBitmap = imageBitmap?.let { ensureInlineSizeLimit(it) }
            val prompt = buildString {
                appendLine("Bạn là Gia sư AI thông minh cho học sinh phổ thông Việt Nam (từ Lớp 1 đến Lớp 12).")
                appendLine("Dòng đầu tiên hãy ghi đúng định dạng: Môn: <Tên môn học và phân môn phù hợp, ví dụ Toán, Vật lí, Hóa học, Sinh học, Ngữ văn, Tiếng Anh, Lịch sử, Địa lí>.")
                appendLine("Sau đó xuống dòng và trả lời thẳng, chính xác, logic tự nhiên, đi sâu đúng trọng tâm câu hỏi hoặc ảnh đề bài:")
                appendLine("- Nếu là câu hỏi lý thuyết: trả lời trực tiếp đáp án chính xác và giải thích ngắn gọn, dễ hiểu.")
                appendLine("- Nếu là bài toán/tự luận: tóm tắt ngắn, nêu công thức đúng của riêng bài đó và giải chi tiết từng bước tới đáp số cuối cùng.")
                appendLine("- Nếu là Ngữ văn hoặc Tiếng Anh: phân tích đúng ngữ pháp, cấu trúc câu hoặc lập dàn ý trọng tâm đề bài.")
                appendLine("Tuyệt đối không dùng khung văn mẫu rập khuôn không liên quan.")
                if (questionText.isNotEmpty()) {
                    appendLine()
                    appendLine("Đề bài / Câu hỏi của học sinh:")
                    appendLine(questionText)
                } else {
                    appendLine()
                    appendLine("Hãy đọc kỹ đề bài trong hình ảnh đính kèm, nhận diện môn học và giải chi tiết.")
                }
            }.trim()

            val response = withTimeout(AI_TIMEOUT_MILLIS) {
                tutorModel.generateContent(
                    content {
                        if (scaledBitmap != null) {
                            image(scaledBitmap)
                        }
                        text(prompt)
                    }
                )
            }

            val rawText = response.text?.trim().orEmpty()
            if (rawText.isEmpty()) {
                return Result.failure(
                    IllegalStateException("AI chưa trả về nội dung lời giải. Vui lòng thử lại.")
                )
            }

            val lines = rawText.lines()
            val firstLine = lines.firstOrNull()?.trim().orEmpty()
            val detectedSubject = if (firstLine.startsWith("Môn:", ignoreCase = true)) {
                firstLine.substringAfter(":").trim().ifEmpty { fallbackSubject }
            } else {
                fallbackSubject
            }
            val cleanedAnswer = if (firstLine.startsWith("Môn:", ignoreCase = true)) {
                lines.drop(1).joinToString("\n").trim()
            } else {
                rawText
            }.ifEmpty { rawText }

            Result.success(
                TutorSolution(
                    detectedSubject = detectedSubject,
                    answer = cleanedAnswer,
                    isOfflineSolved = false,
                    needsOnlineAnalysis = false
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Firebase AI tutor generation timed out", e)
            Result.failure(
                IllegalStateException("Yêu cầu giải bài quá thời gian chờ (60 giây). Vui lòng thử lại.")
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI tutor generation failed", e)
            Result.failure(
                IllegalStateException(
                    e.localizedMessage ?: "Không thể kết nối AI trực tuyến. Vui lòng thử lại sau."
                )
            )
        }
    }

    override suspend fun generatePracticeQuiz(
        selectedGrade: String,
        topicPrompt: String,
        questionCount: Int
    ): Result<QuizGenerationResult> = withContext(Dispatchers.Default) {
        val rawGrade = selectedGrade.trim().ifEmpty { context.getString(R.string.grade_9) }
        val normalizedGrade = quizBank.shortGradeLabel(rawGrade)
        val cleanTopic = topicPrompt.trim().ifEmpty { "Kiến thức tổng hợp" }
        val detectedSubject = solverEngine.detectSubject(cleanTopic, hasImage = false)
        val clampedCount = questionCount.coerceIn(30, 40)

        val prompt = buildGeminiQuizPrompt(
            selectedGrade = normalizedGrade,
            subject = detectedSubject,
            topic = cleanTopic,
            questionCount = clampedCount
        )

        val onlineQuestions = if (NetworkUtils.isNetworkAvailable(context)) {
            tryGenerateQuizOnline(prompt, clampedCount)
        } else {
            emptyList()
        }

        val finalQuestions = if (onlineQuestions.size >= clampedCount) {
            onlineQuestions.take(clampedCount)
        } else {
            val offlineQuestions = quizBank.generateQuiz(
                gradeLevel = normalizedGrade,
                subject = detectedSubject,
                topic = cleanTopic,
                questionCount = clampedCount
            )
            if (onlineQuestions.isEmpty()) {
                offlineQuestions
            } else {
                val merged = LinkedHashMap<String, QuizQuestion>()
                onlineQuestions.forEach { merged.putIfAbsent(it.question, it) }
                offlineQuestions.forEach {
                    if (merged.size < clampedCount) {
                        merged.putIfAbsent(it.question, it)
                    }
                }
                merged.values.toList().take(clampedCount)
            }
        }

        Result.success(
            QuizGenerationResult(
                gradeLevel = normalizedGrade,
                detectedSubject = detectedSubject,
                questions = finalQuestions
            )
        )
    }

    private suspend fun tryGenerateQuizOnline(
        prompt: String,
        expectedCount: Int
    ): List<QuizQuestion> {
        return try {
            val response = withTimeout(AI_TIMEOUT_MILLIS) {
                quizModel.generateContent(prompt)
            }
            val jsonText = response.text?.trim().orEmpty()
            if (jsonText.isEmpty()) return emptyList()

            val root = JSONObject(jsonText)
            val arr = root.optJSONArray("questions") ?: return emptyList()
            val letters = listOf("A", "B", "C", "D")
            val parsed = LinkedHashMap<String, QuizQuestion>()

            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i) ?: continue
                val questionText = obj.optString("question").trim()
                val optionsArr = obj.optJSONArray("options") ?: continue
                if (questionText.isEmpty() || optionsArr.length() != 4) continue

                val formattedOptions = (0 until 4).map { idx ->
                    val rawOpt = optionsArr.optString(idx).trim()
                        .replace(Regex("^[A-Da-d][.)]\\s*"), "")
                        .trim()
                    "${letters[idx]}. $rawOpt"
                }
                val correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, 3)
                val explanation = obj.optString("explanation").trim()
                    .ifEmpty { "Đáp án đúng là ${letters[correctIndex]}." }

                parsed.putIfAbsent(
                    questionText,
                    QuizQuestion(
                        question = questionText,
                        options = formattedOptions,
                        correctIndex = correctIndex,
                        explanation = explanation
                    )
                )
                if (parsed.size >= expectedCount) break
            }
            parsed.values.toList()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Online Gemini quiz generation failed, falling back to OfflineQuizBank", e)
            emptyList()
        }
    }

    fun buildGeminiQuizPrompt(
        selectedGrade: String,
        subject: String,
        topic: String,
        questionCount: Int
    ): String {
        return """
            Hãy đóng vai giáo viên giỏi chương trình Giáo dục Phổ thông Việt Nam.
            Tạo chính xác $questionCount câu hỏi trắc nghiệm khách quan (4 đáp án A, B, C, D) bám sát 100% chương trình $selectedGrade, môn $subject, chủ đề "$topic".
            Không lấy kiến thức của khối lớp khác hoặc môn học khác.
            Trả về JSON theo schema có mảng "questions", mỗi phần tử gồm: question, options (4 chuỗi A/B/C/D), correctIndex (0..3), explanation.
        """.trimIndent()
    }

    override suspend fun sendChatMessage(
        history: List<ChatMessage>,
        userMessage: String
    ): Result<ChatReply> = withContext(Dispatchers.Default) {
        val cleanMessage = userMessage.trim()
        if (cleanMessage.isEmpty()) {
            return@withContext Result.failure(
                IllegalArgumentException("Bạn hãy nhập nội dung tin nhắn trước khi gửi nhé!")
            )
        }

        val recentContext = history.filter { !it.isWelcome }.takeLast(MAX_CHAT_HISTORY)

        if (!NetworkUtils.isNetworkAvailable(context)) {
            val offlineText = solverEngine.replyInChat(recentContext, cleanMessage)
            return@withContext Result.success(ChatReply(text = offlineText, isOfflineReply = true))
        }

        return@withContext try {
            val geminiHistory = buildGeminiChatHistory(recentContext)
            val chat = chatModel.startChat(history = geminiHistory)
            val response = withTimeout(AI_TIMEOUT_MILLIS) {
                chat.sendMessage(cleanMessage)
            }
            val replyText = response.text?.trim().orEmpty()
            if (replyText.isNotEmpty()) {
                Result.success(ChatReply(text = replyText, isOfflineReply = false))
            } else {
                val fallback = solverEngine.replyInChat(recentContext, cleanMessage)
                Result.success(ChatReply(text = fallback, isOfflineReply = true))
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Online chat failed, falling back to OfflineChatResponder", e)
            val fallback = solverEngine.replyInChat(recentContext, cleanMessage)
            Result.success(ChatReply(text = fallback, isOfflineReply = true))
        }
    }

    fun buildGeminiChatHistory(recentMessages: List<ChatMessage>): List<Content> {
        val trimmed = recentMessages
            .filter { !it.isWelcome && it.text.isNotBlank() }
            .takeLast(MAX_CHAT_HISTORY)
            .dropWhile { !it.isFromUser }

        val sanitized = mutableListOf<Content>()
        var expectedUserTurn = true
        for (msg in trimmed) {
            if (msg.isFromUser == expectedUserTurn) {
                sanitized.add(
                    content(role = if (msg.isFromUser) "user" else "model") {
                        text(msg.text)
                    }
                )
                expectedUserTurn = !expectedUserTurn
            }
        }
        // Ensure history ends on a 'model' turn before calling chat.sendMessage(...)
        if (sanitized.isNotEmpty() && expectedUserTurn.not()) {
            sanitized.removeAt(sanitized.lastIndex)
        }
        return sanitized
    }

    private fun ensureInlineSizeLimit(bitmap: Bitmap, maxDim: Int = 1280): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        val largest = max(w, h)
        if (largest <= maxDim) return bitmap
        val scale = maxDim.toFloat() / largest.toFloat()
        val newW = (w * scale).roundToInt().coerceAtLeast(1)
        val newH = (h * scale).roundToInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }

    companion object {
        private const val TAG = "HybridAiRepository"
        private const val MODEL_GEMINI_FLASH = "gemini-2.5-flash"
        private const val AI_TIMEOUT_MILLIS = 60_000L
        const val MAX_CHAT_HISTORY = 10
    }
}
