package com.vinhho.onthi10ai.data.solver

import com.vinhho.onthi10ai.data.model.QuizQuestion
import java.text.Normalizer
import java.util.Locale
import kotlin.random.Random

class OfflineQuizBank {

    data class RawQuestion(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val explanation: String
    )

    fun extractGradeNumber(gradeLevel: String): Int {
        return Regex("\\d+").find(gradeLevel)?.value?.toIntOrNull()?.coerceIn(1, 12) ?: 9
    }

    fun shortGradeLabel(gradeLevel: String): String = "Lớp ${extractGradeNumber(gradeLevel)}"

    fun generateQuiz(
        gradeLevel: String,
        subject: String,
        topic: String,
        questionCount: Int,
        random: Random = Random.Default
    ): List<QuizQuestion> {
        val grade = extractGradeNumber(gradeLevel)
        val targetCount = questionCount.coerceIn(30, 40)
        val pool = LinkedHashMap<String, RawQuestion>()

        fun addAll(items: List<RawQuestion>) {
            items.forEach { item ->
                pool.putIfAbsent(item.question, item)
            }
        }

        when (subject) {
            "Toán" -> addAll(OfflineQuizBankMath.buildForGrade(grade))
            "Vật lí" -> addAll(OfflineQuizBankPhysics.buildForGrade(grade))
            "Hóa học" -> addAll(OfflineQuizBankChemistry.buildForGrade(grade))
            "Sinh học" -> addAll(OfflineQuizBankBiology.buildForGrade(grade))
            "KHTN" -> {
                addAll(OfflineQuizBankPhysics.buildForGrade(grade))
                addAll(OfflineQuizBankChemistry.buildForGrade(grade))
                addAll(OfflineQuizBankBiology.buildForGrade(grade))
            }
            "Tiếng Anh" -> addAll(OfflineQuizBankEnglish.buildForGrade(grade))
            "Ngữ văn" -> addAll(OfflineQuizBankLiterature.buildForGrade(grade))
            "Lịch sử" -> addAll(OfflineQuizBankHistory.buildForGrade(grade))
            "Địa lí" -> addAll(OfflineQuizBankGeography.buildForGrade(grade))
            else -> {
                // Kiến thức tổng hợp của đúng khối lớp đó
                addAll(OfflineQuizBankMath.buildForGrade(grade).shuffled(random).take(8))
                addAll(OfflineQuizBankPhysics.buildForGrade(grade).shuffled(random).take(6))
                addAll(OfflineQuizBankChemistry.buildForGrade(grade).shuffled(random).take(6))
                addAll(OfflineQuizBankBiology.buildForGrade(grade).shuffled(random).take(6))
                addAll(OfflineQuizBankEnglish.buildForGrade(grade).shuffled(random).take(6))
                addAll(OfflineQuizBankLiterature.buildForGrade(grade).shuffled(random).take(6))
                addAll(OfflineQuizBankHistory.buildForGrade(grade).shuffled(random).take(4))
                addAll(OfflineQuizBankGeography.buildForGrade(grade).shuffled(random).take(4))
            }
        }

        val orderedPool = prioritizeByTopic(
            questions = pool.values.toList(),
            subject = subject,
            topic = topic,
            random = random
        )

        return orderedPool
            .take(targetCount)
            .map { raw -> shuffleQuestionOptions(raw, random) }
    }

    private fun prioritizeByTopic(
        questions: List<RawQuestion>,
        subject: String,
        topic: String,
        random: Random
    ): List<RawQuestion> {
        val keywords = extractTopicKeywords(topic, subject)
        if (keywords.isEmpty()) {
            return questions.shuffled(random)
        }

        val scored = questions.map { raw ->
            val haystack = normalizeText("${raw.question} ${raw.explanation}")
            val matchScore = keywords.count { kw -> haystack.contains(kw) }
            raw to matchScore
        }

        val matchedGroups = scored
            .filter { it.second > 0 }
            .groupBy { it.second }
            .toSortedMap(compareByDescending { it })
            .values
            .flatMap { group -> group.map { it.first }.shuffled(random) }

        val unmatched = scored
            .filter { it.second == 0 }
            .map { it.first }
            .shuffled(random)

        return matchedGroups + unmatched
    }

    private fun extractTopicKeywords(topic: String, subject: String): List<String> {
        val normTopic = normalizeText(topic)
        if (normTopic.isBlank() || normTopic == "kien thuc tong hop") return emptyList()

        val stopWords = setOf(
            "mon", "lop", "on", "thi", "vao", "10", "thpt", "chuyen", "de", "bai", "tap",
            "tong", "hop", "kien", "thuc", "cac", "cua", "ve", "va", "hay", "trong", "cho",
            "phan", "chuong", "hoc", "sinh", "trac", "nghiem", "cap"
        )
        val subjectWords = normalizeText(subject).split(" ").filter { it.length >= 2 }.toSet()

        val rawWords = normTopic.split(" ")
            .map { it.trim() }
            .filter { it.length >= 2 && it !in stopWords }

        val subtopicWords = rawWords.filter { it !in subjectWords }
        if (subtopicWords.isEmpty()) return emptyList()

        // Also match 2-word and 3-word consecutive subtopic phrases for stronger relevance
        val phrases = mutableListOf<String>()
        if (subtopicWords.size >= 2) {
            for (i in 0 until subtopicWords.size - 1) {
                phrases.add("${subtopicWords[i]} ${subtopicWords[i + 1]}")
            }
        }
        return (phrases + subtopicWords).distinct()
    }

    private fun normalizeText(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
        return decomposed
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun shuffleQuestionOptions(
        raw: RawQuestion,
        random: Random
    ): QuizQuestion {
        val cleanOptions = raw.options.map { opt ->
            opt.replace(Regex("^[A-Da-d][.)]\\s*"), "").trim()
        }
        val correctOptionText = cleanOptions.getOrElse(raw.correctIndex.coerceIn(0, 3)) { cleanOptions.first() }
        val shuffled = cleanOptions.shuffled(random)
        val newCorrectIndex = shuffled.indexOf(correctOptionText).coerceAtLeast(0)
        val letters = listOf("A", "B", "C", "D")
        val labeledOptions = shuffled.mapIndexed { idx, text ->
            "${letters.getOrElse(idx) { "A" }}. $text"
        }
        return QuizQuestion(
            question = raw.question,
            options = labeledOptions,
            correctIndex = newCorrectIndex,
            explanation = raw.explanation
        )
    }

    companion object {
        fun distinctNumbers(correct: Int, candidates: List<Int>): List<String> =
            com.vinhho.onthi10ai.data.solver.distinctNumbers(correct, candidates)
    }
}

internal fun distinctNumbers(correct: Int, candidates: List<Int>): List<String> {
    val set = linkedSetOf(correct)
    candidates.forEach { set.add(it) }
    var offset = 1
    while (set.size < 4) {
        set.add(correct + offset)
        offset++
    }
    return set.take(4).map { it.toString() }
}
