package com.vinhho.onthi10ai.data.solver

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.Normalizer
import java.util.Locale
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

sealed class OfflineSolveResult {
    data class Solved(
        val detectedSubject: String,
        val answer: String
    ) : OfflineSolveResult()

    data class NeedsOnline(
        val detectedSubject: String
    ) : OfflineSolveResult()
}

class OfflineSolverEngine {

    fun stripVietnameseAccents(input: String): String {
        val decomposed = Normalizer.normalize(input, Normalizer.Form.NFD)
        return decomposed
            .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
            .replace('đ', 'd')
            .replace('Đ', 'D')
    }

    fun normalizeForMatch(input: String): String {
        return stripVietnameseAccents(input)
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9+\\-*/^=().\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun detectSubject(input: String, hasImage: Boolean = false): String {
        val rawLower = input.lowercase(Locale.ROOT).trim()
        val noAccent = stripVietnameseAccents(rawLower)
        if (rawLower.isBlank() && hasImage) return "Nhận diện từ ảnh đề bài"

        if (!hasImage && !containsEquationOrDigits(rawLower) && tryConversationalReply(input) != null) {
            return "Gia sư AI"
        }

        val tokens = noAccent.split(Regex("[^a-z0-9]+")).filter { it.isNotBlank() }.toSet()

        return when {
            listOf(
                "khtn", "khoa hoc tu nhien", "tu nhien", "ly hoa sinh", "li hoa sinh"
            ).any { noAccent.contains(it) } -> "KHTN"

            listOf(
                "sinh hoc", "sinh 9", "sinh 12", "nhiem sac the", "di truyen", "menden",
                "nguyen phan", "giam phan", "te bao", "quang hop", "ho hap", "he sinh thai",
                "dot bien", "protein", "riboxom", "axit amin", "bo ba ma hoa", "phien ma", "dich ma"
            ).any { noAccent.contains(it) } ||
                listOf("gen", "adn", "arn", "nst").any { it in tokens } -> "Sinh học"

            listOf(
                "vat li", "vat ly", "dien tro", "hieu dien the", "cuong do dong dien",
                "dinh luat om", "cong suat", "thau kinh", "quang hoc", "khuc xa",
                "tu truong", "cam ung dien tu", "van toc", "gia toc", "luc hap dan",
                "ampe", "von", "dao dong", "song co", "newton", "nhiet luong", "jun",
                "dong nang", "the nang", "co nang", "con lac", "buoc song", "ω"
            ).any { noAccent.contains(it) || rawLower.contains(it) } ||
                listOf("ohm").any { it in tokens } -> "Vật lí"

            listOf(
                "hoa hoc", "hoa 8", "hoa 9", "hoa 10", "hoa 11", "hoa 12", "so mol", "tinh mol",
                "phuong trinh hoa hoc", "axit", "bazo", "bazơ", "muoi", "kim loai", "phi kim",
                "nong do", "dung dich", "khoi luong mol", "nguyen tu khoi", "hoa tri",
                "h2so4", "hcl", "naoh", "co2", "caco3", "quy tim", "phenolphtalein",
                "este", "ancol", "polime", "amin", "cacbohidrat", "g/mol"
            ).any { noAccent.contains(it) || rawLower.contains(it) } ||
                listOf("mol").any { it in tokens } -> "Hóa học"

            listOf(
                "tieng anh", "english", "grammar", "passive", "conditional", "reported speech",
                "relative clause", "thi dong tu", "12 thi", "cau bi dong", "cau dieu kien",
                "menh de quan he", "cau tuong thuat", "phat am", "pronunciation", "choose the", "rewrite"
            ).any { noAccent.contains(it) } ||
                listOf("tense").any { it in tokens } -> "Tiếng Anh"

            listOf(
                "ngu van", "van hoc", "phan tich", "bai tho", "kho tho", "doan tho",
                "tac gia", "tac pham", "nghi luan", "bien phap tu tu", "an du",
                "hoan du", "nhan hoa", "diep ngu", "nhan vat", "truyen ngan", "viet doan van",
                "truyen kieu", "sang thu", "vieng lang bac", "vo nhat", "song da", "tay tien"
            ).any { noAccent.contains(it) } -> "Ngữ văn"

            listOf(
                "lich su", "chien tranh", "cach mang", "khoi nghia", "hiep dinh",
                "trieu dai", "the chien", "ho chi minh", "dien bien phu", "nam 1945"
            ).any { noAccent.contains(it) } -> "Lịch sử"

            listOf(
                "dia li", "dia ly", "khi hau", "dong bang", "vung kinh te",
                "dan so", "khoang san", "song ngoi", "dia hinh", "bien dong"
            ).any { noAccent.contains(it) } -> "Địa lí"

            looksLikeMath(noAccent, rawLower, tokens) -> "Toán"

            else -> "Kiến thức tổng hợp"
        }
    }

    fun solve(questionText: String, hasImage: Boolean): OfflineSolveResult {
        val clean = questionText.trim()
        val subject = detectSubject(clean, hasImage)

        if (hasImage || clean.isBlank()) {
            return OfflineSolveResult.NeedsOnline(subject)
        }

        // 1. Quantitative Physics / Chemistry solver from units & numerical parameters
        trySolveScienceProblem(clean)?.let { (sciSubject, answer) ->
            return OfflineSolveResult.Solved(sciSubject, answer)
        }

        // 2. Translate Vietnamese natural language math phrases -> standard math expression/equation
        val translatedMath = translateVietnameseMathNlp(clean)

        // 3. Try solving linear or quadratic equations (including Vietnamese NLP equations)
        trySolveEquation(translatedMath)?.let { (levelSubject, solution) ->
            return OfflineSolveResult.Solved(levelSubject, solution)
        }
        if (translatedMath != clean) {
            trySolveEquation(clean)?.let { (levelSubject, solution) ->
                return OfflineSolveResult.Solved(levelSubject, solution)
            }
        }

        // 4. Try evaluating arithmetic expressions & simplifying radicals (e.g. "Hai cong ba", "√48")
        tryEvaluateArithmetic(translatedMath, clean)?.let { (levelSubject, solution) ->
            return OfflineSolveResult.Solved(levelSubject, solution)
        }

        // 5. Smart conversational school assistant reply (greetings, identity, stress/Pomodoro, quick study tips)
        if (!clean.any { it.isDigit() }) {
            tryConversationalReply(clean, subject)?.let { reply ->
                return OfflineSolveResult.Solved("Gia sư AI", reply)
            }
        }

        // 6. Fuzzy search across K-12 Knowledge Base (Toán, Lí, Hóa, Sinh, Anh, Văn)
        tryFuzzySearchKnowledge(clean, subject)?.let { (matchedSubject, answer) ->
            return OfflineSolveResult.Solved(matchedSubject, answer)
        }

        // 7. Catch any remaining conversational phrases (even if containing numbers like "12 cap")
        tryConversationalReply(clean, subject)?.let { reply ->
            return OfflineSolveResult.Solved("Gia sư AI", reply)
        }

        // 8. Escalate complex/deep open-ended questions to Online AI
        return OfflineSolveResult.NeedsOnline(subject)
    }

    fun replyInChat(
        history: List<com.vinhho.onthi10ai.data.model.ChatMessage>,
        userMessage: String
    ): String {
        return OfflineChatResponder.reply(this, history, userMessage)
    }

    fun tryConversationalReply(
        input: String,
        contextSubject: String = "Kiến thức tổng hợp"
    ): String? {
        val clean = input.trim()
        if (clean.isEmpty()) return null
        val noAccent = stripVietnameseAccents(clean.lowercase(Locale.ROOT))
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
        if (noAccent.isEmpty()) return null
        val tokens = noAccent.split(" ").filter { it.isNotBlank() }

        // 1. Greetings & Identity ("xin chào", "hi", "hello", "bạn là ai", "tên gì", "bạn làm được gì")
        val isIdentityOrGreeting =
            listOf("xin chao", "chao ban", "chao anh", "chao chi", "chao gia su", "chao ad").any {
                noAccent == it || noAccent.startsWith("$it ")
            } ||
                (tokens.firstOrNull() in setOf("hi", "hello", "alo", "chao") && tokens.size <= 5) ||
                listOf(
                    "ban la ai", "may la ai", "cau la ai", "ai day", "ai vay",
                    "ten gi", "ten la gi", "ban ten gi", "ten cua ban",
                    "ban lam duoc gi", "lam duoc gi", "giup duoc gi", "co chuc nang gi"
                ).any { phrase -> Regex("\\b${Regex.escape(phrase)}\\b").containsMatchIn(noAccent) }

        if (isIdentityOrGreeting) {
            return "Chào bạn! Mình là **Gia sư AI 12 Cấp** — người bạn đồng hành học tập từ Lớp 1 đến Lớp 12.\n\n" +
                "Ngay cả khi **ngoại tuyến (không có mạng)**, mình vẫn hỗ trợ bạn rất nhiều việc:\n" +
                "• **Giải bài tập từng bước:** Toán (số học, căn thức, phương trình bậc 1/bậc 2), Vật lí, Hóa học có tự động đổi đơn vị SI.\n" +
                "• **Tra cứu nhanh:** Hằng đẳng thức, Vi-ét, công thức Điện học/Cơ học/Mol, Ngữ văn (biện pháp tu từ, dàn ý), Tiếng Anh (12 thì, câu bị động, điều kiện).\n" +
                "• **Luyện đề thông minh:** Tạo bộ đề trắc nghiệm **30–40 câu** sát từng khối **Lớp 1 → Lớp 12**.\n" +
                "• **Lập kế hoạch ôn thi & phương pháp Pomodoro:** Giúp bạn học tập trung, giảm áp lực.\n\n" +
                "Bạn đang muốn ôn môn nào hay có bài tập nào cần mình giải giúp không?"
        }

        // 2. Emotional stress & study motivation / Pomodoro ("mệt quá", "chán học", "áp lực", "lo lắng thi cử", "cách học hiệu quả")
        val emotionalTriggers = listOf(
            "met qua", "qua met", "met moi", "chan hoc", "chan nan", "nan qua", "buon qua",
            "ap luc", "stress", "lo lang", "thi cu", "so thi", "so truot", "diem kem", "diem thap",
            "tam su", "cang thang", "khoc", "co don", "bo cuoc", "mat dong luc"
        )
        val effectiveMethodTriggers = listOf(
            "cach hoc hieu qua", "hoc hieu qua", "phuong phap hoc", "bi quyet hoc",
            "mat tap trung", "pomodoro", "meo ghi nho", "ghi nho nhanh", "cach hoc thuoc"
        )
        if (emotionalTriggers.any { noAccent.contains(it) } || noAccent.contains("pomodoro")) {
            return "Hít một hơi thật sâu nhé, anh/chị **Gia sư AI 12 Cấp** luôn ở đây lắng nghe và đồng hành cùng bạn!\n\n" +
                "Cảm giác mệt mỏi hay áp lực trước kỳ thi ai cũng từng gặp — điều đó chứng tỏ bạn rất có trách nhiệm với việc học của mình. Hãy thử ngay **phương pháp Pomodoro** để lấy lại nhịp nhẹ nhàng:\n\n" +
                "• **25 phút tập trung:** Chọn **duy nhất 1 mục tiêu nhỏ** (1 dạng toán hoặc 5 công thức), tắt thông báo để tập trung trọn vẹn.\n" +
                "• **5 phút nghỉ ngơi:** Đứng dậy vươn vai, uống ngụm nước mát, hít thở sâu (hít 4 giây – giữ 4 giây – thở 6 giây).\n" +
                "• **Nghỉ dài 15–20 phút** sau mỗi 4 phiên.\n\n" +
                "Nếu đang bí bài nào, cứ nhắn trực tiếp vào đây — mình cùng gỡ từng bước nhỏ với bạn nhé!"
        }

        if (effectiveMethodTriggers.any { noAccent.contains(it) }) {
            val subjectTip = when {
                contextSubject.startsWith("Tiếng Anh") ->
                    "\n• **Mẹo Tiếng Anh:** Học từ vựng theo cụm từ và đặt 1 câu gắn với bản thân."
                contextSubject.startsWith("Toán") || contextSubject.startsWith("Vật lí") || contextSubject.startsWith("Hóa học") ->
                    "\n• **Mẹo Tự nhiên:** Che lời giải, tự viết lại công thức kèm đơn vị rồi làm ngay 2 ví dụ ngắn."
                contextSubject.startsWith("Ngữ văn") || contextSubject.startsWith("Lịch sử") || contextSubject.startsWith("Địa lí") ->
                    "\n• **Mẹo Xã hội:** Tóm tắt theo sơ đồ Từ khóa → Mốc/Sự kiện → Ý nghĩa."
                else -> ""
            }
            return "**4 Bí quyết học hiệu quả & ghi nhớ nhanh (Áp dụng cho cả 12 cấp):**\n\n" +
                "1. **Pomodoro (25 phút tập trung – 5 phút nghỉ):** Giữ não tỉnh táo, tránh học dồn gây quá tải.\n" +
                "2. **Active Recall (Chủ động gợi nhớ):** Đọc xong 1 ý, gấp sách lại và tự nhẩm hoặc viết ra giấy những gì còn nhớ.\n" +
                "3. **Kỹ thuật Feynman:** Giải thích lại kiến thức bằng lời văn đơn giản nhất như đang giảng cho bạn cùng lớp.\n" +
                "4. **Lặp lại ngắt quãng (1–3–7):** Ôn lại sau 1 ngày, 3 ngày và 7 ngày để nhớ bền đến ngày thi.$subjectTip"
        }

        // 3. Quick prompt chips standalone support ("Lập kế hoạch ôn thi", "Giải thích dễ hiểu hơn", "Cho ví dụ tương tự")
        if (listOf("lap ke hoach on thi", "ke hoach on thi", "lich on thi", "thoi gian bieu").any { noAccent.contains(it) }) {
            val focus = if (contextSubject != "Kiến thức tổng hợp" && contextSubject != "Gia sư AI") {
                " (ưu tiên môn **$contextSubject**)"
            } else {
                ""
            }
            return "**Kế hoạch ôn thi khoa học trong 7 ngày**$focus:\n\n" +
                "• **Ngày 1–2 (Nền tảng):** Hệ thống công thức & khái niệm cốt lõi ra 1 trang tóm tắt.\n" +
                "• **Ngày 3–4 (Chuyên đề):** Làm 15–20 câu mỗi chuyên đề từ cơ bản đến vận dụng.\n" +
                "• **Ngày 5 (Tổng ôn 30–40 câu):** Vào tab **Luyện đề** chọn đúng khối lớp để làm đề bấm giờ như thi thật.\n" +
                "• **Ngày 6 (Sửa lỗi sai):** Xem lại câu hay sai dấu, sai đơn vị hoặc đọc nhầm đề.\n" +
                "• **Ngày 7 (Giữ sức khỏe):** Ôn lướt mindmap, đi ngủ sớm trước 22h30.\n\n" +
                "Mỗi tối học **2 phiên Pomodoro (25 phút tập trung – 5 phút nghỉ)** là đủ hiệu quả mà không bị kiệt sức!"
        }

        if (listOf("giai thich de hieu hon", "de hieu hon", "giai thich lai").any { noAccent.contains(it) }) {
            return "Mình rất sẵn lòng giảng chậm và dễ hiểu hơn! Bạn có thể nhắn trực tiếp đề bài (ví dụ: `2x + 5 = 15`, `x^2 - 5x + 6 = 0`, `U = 210V, R = 70Ω`, hoặc tên công thức như `hằng đẳng thức`, `định luật Ôm`) — mình sẽ tách từng bước 1 → 2 → 3 thật rõ ràng cho bạn."
        }

        if (listOf("cho vi du tuong tu", "vi du tuong tu").any { noAccent.contains(it) }) {
            return "**Ví dụ mẫu giải từng bước để bạn tham khảo:**\n\n" +
                "• **Toán:** Giải `x² - 7x + 10 = 0`\n" +
                "  Δ = (-7)² - 4·1·10 = 9 > 0 → `x₁ = (7 + 3)/2 = 5`, `x₂ = (7 - 3)/2 = 2`.\n" +
                "• **Vật lí:** Mạch có `U = 12V`, `R = 4Ω` → `I = U / R = 12 / 4 = 3 A`.\n\n" +
                "Bạn hãy gửi bài toán hoặc môn học bạn đang cần luyện, mình sẽ đưa ví dụ sát đúng dạng đó nhé!"
        }

        // 4. Gratitude / Farewell
        if (listOf("cam on", "thank", "tam biet", "bye").any { noAccent.contains(it) }) {
            return "Không có gì đâu nè! Chúc bạn giữ tinh thần thoải mái và học thật tốt. Khi nào cần giải bài hay tâm sự, cứ mở Gia sư AI nhé!"
        }

        return null
    }

    fun buildOfflineFallbackSolution(
        questionText: String,
        hasImage: Boolean,
        detectedSubject: String
    ): String {
        val clean = questionText.trim()
        if (clean.isNotEmpty()) {
            tryConversationalReply(clean, detectedSubject)?.let { return it }
            when (val localSolved = solve(clean, hasImage = false)) {
                is OfflineSolveResult.Solved -> return localSolved.answer
                is OfflineSolveResult.NeedsOnline -> Unit
            }
        }
        return if (hasImage) {
            "Hiện kết nối AI phân tích ảnh trực tuyến đang tạm gián đoạn.\n\n" +
                "Bạn có thể **gõ lại nội dung đề bài hoặc phép tính trong ảnh** vào ô phía trên (ví dụ: `2x + 5 = 15`, `x^2 - 5x + 6 = 0`, `U = 210V, R = 70Ω`, `tính số mol m = 5,6g Fe`, `7 hằng đẳng thức`...) để **bộ giải Offline thông minh** giải chi tiết từng bước cho bạn ngay lập tức!"
        } else {
            "Hiện kết nối AI trực tuyến đang tạm gián đoạn, nên hệ thống đã chuyển sang **chế độ Gia sư Offline** ($detectedSubject):\n\n" +
                "• Nếu là bài tính toán / phương trình / Vật lí / Hóa học: hãy ghi rõ biểu thức hoặc số liệu kèm đơn vị (VD: `2x + 5 = 15`, `căn bậc hai của 48`, `m = 400g, v = 2m/s`).\n" +
                "• Nếu cần ôn lý thuyết nhanh: thử nhập từ khóa như `hằng đẳng thức`, `định lí Vi-ét`, `định luật Ôm`, `12 thì tiếng Anh`, `biện pháp tu từ`.\n" +
                "• Hoặc mở tab **Luyện đề** để làm bộ đề trắc nghiệm 30–40 câu theo từng khối Lớp 1–12 hoàn toàn ngoại tuyến!"
        }
    }

    private fun containsEquationOrDigits(rawLower: String): Boolean {
        return rawLower.any { it.isDigit() } || rawLower.any { it in charArrayOf('+', '=', '^', '√', '*', '/') }
    }

    private fun looksLikeMath(noAccent: String, rawLower: String, tokens: Set<String>): Boolean {
        val mathPhrases = listOf(
            "toan", "phuong trinh", "he phuong trinh", "ham so", "parabol", "vi-et",
            "can bac hai", "rut gon", "tam giac", "duong tron", "tu giac noi tiep",
            "tiep tuyen", "bat dang thuc", "tim x", "chung minh",
            "dao ham", "tich phan", "nguyen ham", "logarit", "xac suat", "to hop",
            "dien tich", "chu vi", "the tich", "hang dang thuc", "pytago", "py-ta-go",
            "binh phuong", "lap phuong", "x²", "x^2", "√", "sqrt"
        )
        val mathWordTokens = setOf("sin", "cos", "tan", "viet", "cong", "tru", "nhan", "chia")
        if (mathPhrases.any { noAccent.contains(it) || rawLower.contains(it) }) return true
        if (mathWordTokens.any { it in tokens }) return true
        val hasDigits = rawLower.any { it.isDigit() }
        val hasOperators = rawLower.any { it in charArrayOf('+', '-', '*', '×', '/', '÷', '^', '=') }
        return hasDigits && hasOperators
    }

    // -------------------------------------------------------------------------
    // 1. Vietnamese Natural Language -> Math Expression Translator (NLP)
    // -------------------------------------------------------------------------

    fun translateVietnameseMathNlp(rawInput: String): String {
        var text = stripVietnameseAccents(rawInput.trim().lowercase(Locale.ROOT))

        // Remove polite/question framing prefixes & suffixes
        val leadingPhrases = listOf(
            "hay tinh giup minh", "hay tinh", "tinh gia tri bieu thuc", "tinh gia tri",
            "tinh nhanh", "tinh nham", "tinh", "ket qua cua", "cho biet", "giai giup",
            "giai phuong trinh", "giai pt", "tim x biet", "tim x,", "tim x:", "tim x"
        )
        for (phrase in leadingPhrases) {
            if (text.startsWith("$phrase ") || text == phrase) {
                text = text.removePrefix(phrase).trim(' ', ':', ',', '.')
                break
            }
        }
        text = text
            .replace("bang bao nhieu", "")
            .replace("la bao nhieu", "")
            .replace("duoc bao nhieu", "")
            .replace("bang may", "")
            .replace("?", "")
            .trim()

        // Normalize decimal commas between digits (e.g., 2,5 -> 2.5)
        text = text.replace(Regex("(?<=\\d),(?=\\d)"), ".")

        // Replace multi-word math operators before tokenizing numbers
        val phraseReplacements = listOf(
            "can bac hai cua" to " sqrt ",
            "can bac 2 cua" to " sqrt ",
            "can bac hai" to " sqrt ",
            "can bac 2" to " sqrt ",
            "can cua" to " sqrt ",
            "mo ngoac" to " ( ",
            "dong ngoac" to " ) ",
            "binh phuong" to " ^ 2 ",
            "lap phuong" to " ^ 3 ",
            "luy thua" to " ^ ",
            "cong voi" to " + ",
            "tru di" to " - ",
            "nhan voi" to " * ",
            "chia cho" to " / ",
            "phan tram" to " % ",
            "cong" to " + ",
            "them" to " + ",
            "tru" to " - ",
            "bot" to " - ",
            "nhan" to " * ",
            "chia" to " / ",
            "mu" to " ^ ",
            "bang" to " = ",
            "can" to " sqrt "
        )

        // Pad punctuation/math symbols so tokenization stays clean
        text = text
            .replace("√", " sqrt ")
            .replace("×", " * ")
            .replace("·", " * ")
            .replace("÷", " / ")
            .replace(":", " / ")
            .replace("²", " ^ 2 ")
            .replace("³", " ^ 3 ")
            .replace("(", " ( ")
            .replace(")", " ) ")
            .replace("+", " + ")
            .replace("-", " - ")
            .replace("*", " * ")
            .replace("/", " / ")
            .replace("^", " ^ ")
            .replace("=", " = ")
            .replace("%", " % ")
            .replace(Regex("\\s+"), " ")
            .trim()

        for ((phrase, replacement) in phraseReplacements) {
            text = text.replace(Regex("\\b${Regex.escape(phrase)}\\b"), replacement)
        }
        text = text.replace(Regex("\\s+"), " ").trim()

        // Convert Vietnamese number words & fractions ("ba phan tu" -> "3 / 4")
        val tokens = text.split(" ").filter { it.isNotBlank() }
        val convertedTokens = convertVietnameseNumberTokens(tokens)
        val joined = convertedTokens.joinToString(" ")
            .replace(Regex("\\bsqrt\\s+([0-9]+(?:\\.[0-9]+)?)")) { m -> "sqrt(${m.groupValues[1]})" }
            .replace(Regex("\\b(\\d+(?:\\.\\d+)?)\\s+phan\\s+(\\d+(?:\\.\\d+)?)\\b")) { m ->
                "(${m.groupValues[1]} / ${m.groupValues[2]})"
            }
            .replace(" phan ", " / ")
            .replace(Regex("\\s+"), " ")
            .trim()

        return joined
    }

    private fun convertVietnameseNumberTokens(tokens: List<String>): List<String> {
        val numberWords = setOf(
            "khong", "mot", "hai", "ba", "bon", "tu", "nam", "lam",
            "sau", "bay", "tam", "chin", "muoi", "tram", "nghin", "ngan", "trieu",
            "linh", "le", "phay", "cham"
        )
        val out = mutableListOf<String>()
        var i = 0
        while (i < tokens.size) {
            val token = tokens[i]
            if (token in numberWords) {
                val group = mutableListOf<String>()
                while (i < tokens.size && tokens[i] in numberWords) {
                    group.add(tokens[i])
                    i++
                }
                val parsed = parseVietnameseNumberPhrase(group)
                if (parsed != null) {
                    out.add(parsed)
                } else {
                    out.addAll(group)
                }
            } else {
                out.add(token)
                i++
            }
        }
        return out
    }

    private fun parseVietnameseNumberPhrase(tokens: List<String>): String? {
        if (tokens.isEmpty()) return null
        // Check decimal separator "phay" / "cham"
        val sepIndex = tokens.indexOfFirst { it == "phay" || it == "cham" }
        if (sepIndex > 0 && sepIndex < tokens.lastIndex) {
            val intPart = parseVietnameseIntWords(tokens.subList(0, sepIndex)) ?: return null
            val fracTokens = tokens.subList(sepIndex + 1, tokens.size)
            val fracDigits = fracTokens.mapNotNull { digitWordValue(it) }
            if (fracDigits.size == fracTokens.size) {
                return "$intPart.${fracDigits.joinToString("")}"
            }
            val fracVal = parseVietnameseIntWords(fracTokens) ?: return null
            return "$intPart.$fracVal"
        }
        val intVal = parseVietnameseIntWords(tokens) ?: return null
        return intVal.toString()
    }

    private fun parseVietnameseIntWords(tokens: List<String>): Long? {
        if (tokens.isEmpty()) return null
        if (tokens.size == 1) {
            return digitWordValue(tokens[0])?.toLong() ?: when (tokens[0]) {
                "muoi" -> 10L
                "tram" -> 100L
                "nghin", "ngan" -> 1000L
                "trieu" -> 1_000_000L
                else -> null
            }
        }
        var total = 0L
        var section = 0L
        var currentDigit: Long? = null

        for (token in tokens) {
            val d = digitWordValue(token)?.toLong()
            when {
                d != null -> {
                    currentDigit = d
                }
                token == "muoi" -> {
                    val tens = currentDigit ?: 1L
                    section += tens * 10L
                    currentDigit = null
                }
                token == "tram" -> {
                    val hundreds = currentDigit ?: 1L
                    section += hundreds * 100L
                    currentDigit = null
                }
                token == "linh" || token == "le" -> {
                    currentDigit = null
                }
                token == "nghin" || token == "ngan" -> {
                    section += currentDigit ?: 0L
                    if (section == 0L) section = 1L
                    total += section * 1000L
                    section = 0L
                    currentDigit = null
                }
                token == "trieu" -> {
                    section += currentDigit ?: 0L
                    if (section == 0L) section = 1L
                    total += section * 1_000_000L
                    section = 0L
                    currentDigit = null
                }
                else -> return null
            }
        }
        return total + section + (currentDigit ?: 0L)
    }

    private fun digitWordValue(word: String): Int? = when (word) {
        "khong" -> 0
        "mot" -> 1
        "hai" -> 2
        "ba" -> 3
        "bon", "tu" -> 4
        "nam", "lam" -> 5
        "sau" -> 6
        "bay" -> 7
        "tam" -> 8
        "chin" -> 9
        else -> null
    }

    private fun numberToVietnameseWords(n: Long): String? {
        if (n < 0 || n > 9999) return null
        val units = arrayOf("Không", "Một", "Hai", "Ba", "Bốn", "Năm", "Sáu", "Bảy", "Tám", "Chín")
        if (n < 10) return units[n.toInt()]
        if (n < 20) {
            val u = (n % 10).toInt()
            return when (u) {
                0 -> "Mười"
                5 -> "Mười lăm"
                else -> "Mười ${units[u].lowercase(Locale.ROOT)}"
            }
        }
        if (n < 100) {
            val t = (n / 10).toInt()
            val u = (n % 10).toInt()
            val tail = when (u) {
                0 -> ""
                1 -> " mốt"
                5 -> " lăm"
                else -> " ${units[u].lowercase(Locale.ROOT)}"
            }
            return "${units[t]} mươi$tail"
        }
        return null
    }

    // -------------------------------------------------------------------------
    // 2. Arithmetic Evaluator + Radical Simplification
    // -------------------------------------------------------------------------

    private fun tryEvaluateArithmetic(
        translatedExpr: String,
        originalInput: String
    ): Pair<String, String>? {
        val stripped = translatedExpr
            .replace(Regex("=\\s*$"), "")
            .trim()
        if (stripped.isEmpty()) return null

        val compact = stripped
            .replace(" ", "")
            .replace(Regex("(?<=[0-9)])[xX](?=[0-9(]|sqrt)"), "*")
            .replace(Regex("(?<=[0-9%])(?=\\(|sqrt)"), "*")
            .replace(Regex("(?<=\\))(?=[0-9(]|sqrt)"), "*")

        // Reject if letters remain other than 'sqrt'
        val withoutSqrt = compact.replace("sqrt", "")
        if (withoutSqrt.any { it.isLetter() }) return null
        if (!compact.any { it.isDigit() }) return null

        val hasOperatorOrFunc = compact.any { it in charArrayOf('+', '-', '*', '/', '^', '%') } ||
            compact.contains("sqrt")
        if (!hasOperatorOrFunc) return null

        // Check for pure radical simplification (e.g., sqrt(48))
        val pureSqrtMatch = Regex("^sqrt\\((\\d+)\\)$").matchEntire(compact)
        if (pureSqrtMatch != null) {
            val n = pureSqrtMatch.groupValues[1].toLongOrNull()
            if (n != null && n >= 0) {
                val radicalInfo = simplifySquareRoot(n)
                val approx = formatNumber(sqrt(n.toDouble()))
                val answer = buildString {
                    appendLine("Phép tính: √$n")
                    appendLine()
                    if (radicalInfo.inside == 1L) {
                        appendLine("• Rút gọn: √$n = ${radicalInfo.outside}")
                        appendLine("• Kết quả: ${radicalInfo.outside}")
                    } else if (radicalInfo.outside > 1L) {
                        appendLine("• Phân tích thừa số chính phương: $n = ${radicalInfo.outside * radicalInfo.outside} × ${radicalInfo.inside} = ${radicalInfo.outside}² × ${radicalInfo.inside}")
                        appendLine("• Rút gọn căn thức: √$n = ${radicalInfo.outside}√${radicalInfo.inside}")
                        appendLine("• Giá trị gần đúng: ≈ $approx")
                    } else {
                        appendLine("• $n không chia hết cho số chính phương nào lớn hơn 1 nên căn thức giữ nguyên là √$n")
                        appendLine("• Giá trị gần đúng: ≈ $approx")
                    }
                }.trim()
                return "Toán học (Cấp 2)" to answer
            }
        }

        val parser = MathExpressionParser(compact)
        val result = parser.parse() ?: return null
        val formattedResult = formatNumber(result)
        val displayExpr = stripped
            .replace("*", "×")
            .replace("/", ":")
            .replace("sqrt(", "√(")
            .replace(Regex("\\s+"), " ")
            .trim()

        val longResult = formattedResult.toLongOrNull()
        val wordsSuffix = longResult?.let { numberToVietnameseWords(it) }?.let { " ($it)" } ?: ""
        val isPrimarySimple = !compact.contains("sqrt") &&
            !compact.contains("^") &&
            !compact.contains("%") &&
            result >= 0 &&
            compact.length <= 24
        val detectedLevelSubject = if (isPrimarySimple) "Toán học (Cấp 1)" else "Toán học"

        val answer = buildString {
            appendLine("Phép tính: $displayExpr = $formattedResult")
            appendLine()
            appendLine("• Kết quả: **$formattedResult**$wordsSuffix")
            when {
                compact.contains("sqrt") || compact.contains("^") || compact.contains("(") -> {
                    appendLine("• Thứ tự thực hiện: Trong ngoặc ( ) → Lũy thừa / Căn bậc hai → Nhân / Chia → Cộng / Trừ.")
                }
                (compact.contains("*") || compact.contains("/")) &&
                    (compact.contains("+") || compact.contains("-")) -> {
                    appendLine("• Quy tắc: Thực hiện phép nhân/chia trước, phép cộng/trừ sau.")
                }
            }
            if (translatedExpr != stripVietnameseAccents(originalInput.lowercase(Locale.ROOT)).trim()) {
                appendLine("• Đã tự động chuyển đổi từ câu hỏi: \"$originalInput\"")
            }
        }.trim()

        return detectedLevelSubject to answer
    }

    private data class SimplifiedRadical(val outside: Long, val inside: Long)

    private fun simplifySquareRoot(n: Long): SimplifiedRadical {
        if (n == 0L) return SimplifiedRadical(0L, 1L)
        var outside = 1L
        var inside = n
        var factor = 2L
        while (factor * factor <= inside) {
            val sq = factor * factor
            while (inside % sq == 0L) {
                outside *= factor
                inside /= sq
            }
            factor++
        }
        return SimplifiedRadical(outside, inside)
    }

    private class MathExpressionParser(private val s: String) {
        private var pos = 0

        fun parse(): Double? {
            if (s.isEmpty()) return null
            val value = parseExpression() ?: return null
            if (pos != s.length) return null
            if (value.isNaN() || value.isInfinite()) return null
            return value
        }

        private fun parseExpression(): Double? {
            var x = parseTerm() ?: return null
            while (pos < s.length) {
                when (s[pos]) {
                    '+' -> {
                        pos++
                        val rhs = parseTerm() ?: return null
                        x += rhs
                    }
                    '-' -> {
                        pos++
                        val rhs = parseTerm() ?: return null
                        x -= rhs
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parseTerm(): Double? {
            var x = parseUnary() ?: return null
            while (pos < s.length) {
                when (s[pos]) {
                    '*' -> {
                        pos++
                        val rhs = parseUnary() ?: return null
                        x *= rhs
                    }
                    '/' -> {
                        pos++
                        val rhs = parseUnary() ?: return null
                        if (abs(rhs) < 1e-12) return null
                        x /= rhs
                    }
                    else -> return x
                }
            }
            return x
        }

        private fun parseUnary(): Double? {
            if (pos < s.length) {
                if (s[pos] == '+') {
                    pos++
                    return parseUnary()
                }
                if (s[pos] == '-') {
                    pos++
                    val value = parseUnary() ?: return null
                    return -value
                }
            }
            return parsePower()
        }

        private fun parsePower(): Double? {
            val base = parsePostfix() ?: return null
            if (pos < s.length && s[pos] == '^') {
                pos++
                val exp = parseUnary() ?: return null
                return base.pow(exp)
            }
            return base
        }

        private fun parsePostfix(): Double? {
            var value = parsePrimary() ?: return null
            while (pos < s.length && s[pos] == '%') {
                pos++
                value /= 100.0
            }
            return value
        }

        private fun parsePrimary(): Double? {
            if (pos >= s.length) return null
            if (s[pos] == '(') {
                pos++
                val inner = parseExpression() ?: return null
                if (pos >= s.length || s[pos] != ')') return null
                pos++
                return inner
            }
            if (s.startsWith("sqrt", pos)) {
                pos += 4
                val arg = if (pos < s.length && s[pos] == '(') {
                    parsePrimary()
                } else {
                    parseNumber()
                } ?: return null
                if (arg < 0) return null
                return sqrt(arg)
            }
            return parseNumber()
        }

        private fun parseNumber(): Double? {
            val start = pos
            var dotCount = 0
            while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) {
                if (s[pos] == '.') dotCount++
                pos++
            }
            if (pos == start || dotCount > 1) return null
            return s.substring(start, pos).toDoubleOrNull()
        }
    }

    // -------------------------------------------------------------------------
    // 3. Linear & Quadratic Equation Solver (ax² + bx + c = dx² + ex + f)
    // -------------------------------------------------------------------------

    private fun trySolveEquation(rawInput: String): Pair<String, String>? {
        var cleaned = rawInput.trim()
        val prefixes = listOf(
            "giai phuong trinh", "giải phương trình", "tim x biet", "tìm x biết",
            "tim x,", "tìm x,", "tim x:", "tìm x:", "tim x", "tìm x", "giai pt", "giải pt", "giai", "giải"
        )
        for (prefix in prefixes) {
            if (cleaned.lowercase(Locale.ROOT).startsWith(prefix)) {
                cleaned = cleaned.substring(prefix.length).trim(' ', ':', ',', '.')
            }
        }
        if (!cleaned.contains("=")) return null
        val sides = cleaned.split("=")
        if (sides.size != 2) return null

        val leftStr = normalizePolynomialSide(sides[0]) ?: return null
        val rightStr = normalizePolynomialSide(sides[1]) ?: return null

        val lhs = parseQuadraticPolynomial(leftStr) ?: return null
        val rhs = parseQuadraticPolynomial(rightStr) ?: return null

        val a = lhs.a - rhs.a
        val b = lhs.b - rhs.b
        val c = lhs.c - rhs.c

        if (!leftStr.contains("x") && !rightStr.contains("x")) return null

        val displayEq = cleaned.replace("x^2", "x²").replace(Regex("\\s+"), " ").trim()
        return if (abs(a) < 1e-9) {
            "Toán học (Cấp 2)" to solveLinear(b, c, displayEq)
        } else {
            "Toán học (Cấp 2)" to solveQuadratic(a, b, c, displayEq)
        }
    }

    private fun normalizePolynomialSide(side: String): String? {
        val s = side.trim()
            .replace(" ", "")
            .replace(",", ".")
            .replace("−", "-")
            .replace("–", "-")
            .replace("X", "x")
            .replace("x²", "x^2")
            .replace("·", "")
            .replace("*", "")
        if (s.isEmpty()) return null
        if (s.any { it !in charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '+', '-', 'x', '^') }) {
            return null
        }
        return s
    }

    private data class QuadPoly(val a: Double, val b: Double, val c: Double)

    private fun parseQuadraticPolynomial(expr: String): QuadPoly? {
        var a = 0.0
        var b = 0.0
        var c = 0.0
        val normalized = if (expr.startsWith("+") || expr.startsWith("-")) expr else "+$expr"
        val terms = Regex("[+-][^+-]+").findAll(normalized).map { it.value }.toList()
        if (terms.isEmpty() || terms.sumOf { it.length } != normalized.length) return null

        for (term in terms) {
            when {
                term.endsWith("x^2") -> {
                    val coeffStr = term.removeSuffix("x^2")
                    val coeff = when (coeffStr) {
                        "+" -> 1.0
                        "-" -> -1.0
                        else -> coeffStr.toDoubleOrNull() ?: return null
                    }
                    a += coeff
                }
                term.endsWith("x") -> {
                    val coeffStr = term.removeSuffix("x")
                    val coeff = when (coeffStr) {
                        "+" -> 1.0
                        "-" -> -1.0
                        else -> coeffStr.toDoubleOrNull() ?: return null
                    }
                    b += coeff
                }
                else -> {
                    val value = term.toDoubleOrNull() ?: return null
                    c += value
                }
            }
        }
        return QuadPoly(a, b, c)
    }

    private fun solveLinear(b: Double, c: Double, originalEq: String): String {
        return buildString {
            appendLine("Giải phương trình bậc nhất: $originalEq")
            appendLine()
            if (abs(b) < 1e-9) {
                if (abs(c) < 1e-9) {
                    appendLine("• Biến đổi: 0x = 0")
                    appendLine("• Kết luận: Phương trình có vô số nghiệm (x ∈ ℝ).")
                } else {
                    appendLine("• Biến đổi: 0x = ${formatNumber(-c)} (vô lý)")
                    appendLine("• Kết luận: Phương trình vô nghiệm.")
                }
            } else {
                val rhs = -c
                val root = rhs / b
                appendLine("• Bước 1: Chuyển vế đưa về dạng ax = b → ${formatNumber(b)}x = ${formatNumber(rhs)}")
                appendLine("• Bước 2: Chia hai vế cho ${formatNumber(b)} → x = ${formatNumber(rhs)} / ${formatNumber(b)}")
                appendLine("• Đáp số: x = ${formatNumber(root)}")
            }
        }.trim()
    }

    private fun solveQuadratic(a: Double, b: Double, c: Double, originalEq: String): String {
        val delta = b * b - 4.0 * a * c
        return buildString {
            appendLine("Giải phương trình bậc hai: $originalEq")
            appendLine()
            appendLine("• Đưa về dạng chuẩn: ${formatNumber(a)}x² + (${formatNumber(b)})x + (${formatNumber(c)}) = 0")
            appendLine("  Hệ số: a = ${formatNumber(a)}, b = ${formatNumber(b)}, c = ${formatNumber(c)}")
            appendLine("• Tính biệt thức Δ = b² - 4ac = (${formatNumber(b)})² - 4·(${formatNumber(a)})·(${formatNumber(c)}) = ${formatNumber(delta)}")
            appendLine()
            when {
                delta > 1e-9 -> {
                    val sqrtDelta = sqrt(delta)
                    val x1 = (-b + sqrtDelta) / (2.0 * a)
                    val x2 = (-b - sqrtDelta) / (2.0 * a)
                    appendLine("• Vì Δ > 0 nên phương trình có 2 nghiệm phân biệt:")
                    appendLine("  x₁ = (-b + √Δ) / (2a) = ${formatNumber(x1)}")
                    appendLine("  x₂ = (-b - √Δ) / (2a) = ${formatNumber(x2)}")
                    appendLine("• Kiểm tra Vi-ét: x₁ + x₂ = ${formatNumber(x1 + x2)}; x₁·x₂ = ${formatNumber(x1 * x2)}")
                }
                abs(delta) <= 1e-9 -> {
                    val x0 = -b / (2.0 * a)
                    appendLine("• Vì Δ = 0 nên phương trình có nghiệm kép:")
                    appendLine("  x₁ = x₂ = -b / (2a) = ${formatNumber(x0)}")
                }
                else -> {
                    appendLine("• Vì Δ < 0 nên phương trình vô nghiệm trên tập số thực ℝ.")
                }
            }
        }.trim()
    }

    // -------------------------------------------------------------------------
    // 4. Quantitative Physics & Chemistry Solver (Units & SI Conversion)
    // -------------------------------------------------------------------------

    private data class QuantityMatch(val value: Double, val unit: String)

    private fun trySolveScienceProblem(rawInput: String): Pair<String, String>? {
        val lower = rawInput.lowercase(Locale.ROOT)
        val noAccent = stripVietnameseAccents(lower)

        // Extract common named quantities with units
        val uVal = extractUnitValue(rawInput, listOf("u", "hieu dien the"), listOf("kv", "v", "von"))
        val rVal = extractUnitValue(rawInput, listOf("r", "dien tro"), listOf("kω", "kohm", "ω", "ohm", "om"))
        val iVal = extractUnitValue(rawInput, listOf("i", "cuong do dong dien", "dong dien"), listOf("ma", "a", "ampe"))

        // 4A. Ohm's Law & Electrical Power (U, R, I)
        if (uVal != null && rVal != null && iVal == null) {
            val uVolt = convertVoltageToVolt(uVal)
            val rOhm = convertResistanceToOhm(rVal)
            if (rOhm > 1e-9) {
                val iAmp = uVolt / rOhm
                val power = uVolt * iAmp
                val answer = buildString {
                    appendLine("Bài toán Vật lí (Điện học - Định luật Ôm):")
                    appendLine()
                    appendLine("1. Tóm tắt & Đổi đơn vị SI:")
                    appendLine("   • Hiệu điện thế: U = ${formatNumber(uVal.value)} ${uVal.unit} = ${formatNumber(uVolt)} V")
                    appendLine("   • Điện trở: R = ${formatNumber(rVal.value)} ${rVal.unit} = ${formatNumber(rOhm)} Ω")
                    appendLine("2. Áp dụng định luật Ôm:")
                    appendLine("   • Công thức: I = U / R")
                    appendLine("   • Thay số: I = ${formatNumber(uVolt)} / ${formatNumber(rOhm)} = ${formatNumber(iAmp)} A")
                    appendLine("   • Công suất tiêu thụ (nếu cần): P = U · I = ${formatNumber(uVolt)} · ${formatNumber(iAmp)} = ${formatNumber(power)} W")
                    appendLine("3. Đáp số: Cường độ dòng điện I = **${formatNumber(iAmp)} A**")
                }.trim()
                return "Vật lí" to answer
            }
        }
        if (iVal != null && rVal != null && uVal == null) {
            val iAmp = convertCurrentToAmpere(iVal)
            val rOhm = convertResistanceToOhm(rVal)
            val uVolt = iAmp * rOhm
            val answer = buildString {
                appendLine("Bài toán Vật lí (Điện học - Định luật Ôm):")
                appendLine()
                appendLine("1. Tóm tắt & Đổi đơn vị SI:")
                appendLine("   • Cường độ dòng điện: I = ${formatNumber(iAmp)} A")
                appendLine("   • Điện trở: R = ${formatNumber(rOhm)} Ω")
                appendLine("2. Áp dụng công thức U = I · R = ${formatNumber(iAmp)} · ${formatNumber(rOhm)} = ${formatNumber(uVolt)} V")
                appendLine("3. Đáp số: Hiệu điện thế U = **${formatNumber(uVolt)} V**")
            }.trim()
            return "Vật lí" to answer
        }
        if (uVal != null && iVal != null && rVal == null) {
            val uVolt = convertVoltageToVolt(uVal)
            val iAmp = convertCurrentToAmpere(iVal)
            if (iAmp > 1e-9) {
                val rOhm = uVolt / iAmp
                val power = uVolt * iAmp
                val answer = buildString {
                    appendLine("Bài toán Vật lí (Điện học):")
                    appendLine()
                    appendLine("1. Tóm tắt: U = ${formatNumber(uVolt)} V; I = ${formatNumber(iAmp)} A")
                    appendLine("2. Điện trở dây dẫn: R = U / I = ${formatNumber(uVolt)} / ${formatNumber(iAmp)} = ${formatNumber(rOhm)} Ω")
                    appendLine("   Công suất điện: P = U · I = ${formatNumber(uVolt)} · ${formatNumber(iAmp)} = ${formatNumber(power)} W")
                    appendLine("3. Đáp số: R = **${formatNumber(rOhm)} Ω**; P = **${formatNumber(power)} W**")
                }.trim()
                return "Vật lí" to answer
            }
        }

        // 4B. Physics Mechanics: Kinetic Energy W_đ = 0.5 * m * v^2 & Newton's 2nd Law F = m * a
        val mPhys = extractUnitValue(rawInput, listOf("m", "khoi luong"), listOf("tan", "tấn", "kg", "g", "gam"))
        val vPhys = extractUnitValue(rawInput, listOf("v", "van toc", "toc do"), listOf("km/h", "km/gio", "m/s"))
        if (mPhys != null && vPhys != null) {
            val mKg = convertMassToKg(mPhys)
            val vMs = convertSpeedToMs(vPhys)
            val kineticEnergy = 0.5 * mKg * vMs * vMs
            val momentum = mKg * vMs
            val answer = buildString {
                appendLine("Bài toán Vật lí (Cơ học - Động năng & Động lượng):")
                appendLine()
                appendLine("1. Tóm tắt & Đổi đơn vị chuẩn SI:")
                appendLine("   • Khối lượng: m = ${formatNumber(mPhys.value)} ${mPhys.unit} = ${formatNumber(mKg)} kg")
                appendLine("   • Vận tốc: v = ${formatNumber(vPhys.value)} ${vPhys.unit} = ${formatNumber(vMs)} m/s")
                appendLine("2. Áp dụng công thức:")
                appendLine("   • Động năng: W_đ = ½ · m · v² = 0,5 · ${formatNumber(mKg)} · (${formatNumber(vMs)})² = ${formatNumber(kineticEnergy)} J")
                appendLine("   • Động lượng: p = m · v = ${formatNumber(mKg)} · ${formatNumber(vMs)} = ${formatNumber(momentum)} kg·m/s")
                appendLine("3. Đáp số: W_đ = **${formatNumber(kineticEnergy)} J**; p = **${formatNumber(momentum)} kg·m/s**")
            }.trim()
            return "Vật lí" to answer
        }

        val aPhys = extractUnitValue(rawInput, listOf("a", "gia toc"), listOf("m/s2", "m/s²"))
        if (mPhys != null && aPhys != null) {
            val mKg = convertMassToKg(mPhys)
            val force = mKg * aPhys.value
            val answer = buildString {
                appendLine("Bài toán Vật lí (Định luật II Newton):")
                appendLine()
                appendLine("1. Tóm tắt & Đổi đơn vị SI:")
                appendLine("   • m = ${formatNumber(mPhys.value)} ${mPhys.unit} = ${formatNumber(mKg)} kg; a = ${formatNumber(aPhys.value)} m/s²")
                appendLine("2. Công thức: F = m · a = ${formatNumber(mKg)} · ${formatNumber(aPhys.value)} = ${formatNumber(force)} N")
                appendLine("3. Đáp số: Lực tác dụng F = **${formatNumber(force)} N**")
            }.trim()
            return "Vật lí" to answer
        }

        // 4C. Chemistry: Mole calculation n = m / M (or known substances)
        val mChem = extractUnitValue(rawInput, listOf("m", "khoi luong"), listOf("kg", "g", "gam"))
        val molarMass = extractUnitValue(rawInput, listOf("m_lon", "khoi luong mol", "ptk", "ntk"), listOf("g/mol", "dvC", "đvc"))
        val explicitM = Regex("(?:^|[^a-zA-Z])M\\s*=\\s*(\\d+(?:[.,]\\d+)?)").find(rawInput)
            ?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
            ?: molarMass?.value
        val nVal = extractUnitValue(rawInput, listOf("n", "so mol"), listOf("mol"))

        val inferredSubstance = detectChemicalMolarMass(rawInput)
        val effectiveM = explicitM ?: inferredSubstance?.second

        val isChemistryContext = noAccent.contains("mol") ||
            explicitM != null ||
            inferredSubstance != null ||
            noAccent.contains("hoa hoc") ||
            noAccent.contains("khoi luong mol")

        if (isChemistryContext && mChem != null && effectiveM != null && effectiveM > 0 && nVal == null) {
            val mGram = if (mChem.unit == "kg") mChem.value * 1000.0 else mChem.value
            val mol = mGram / effectiveM
            val substanceLabel = inferredSubstance?.first?.let { " của $it" } ?: ""
            val answer = buildString {
                appendLine("Bài toán Hóa học (Tính số mol$substanceLabel):")
                appendLine()
                appendLine("1. Tóm tắt dữ kiện:")
                appendLine("   • Khối lượng chất: m = ${formatNumber(mChem.value)} ${mChem.unit} = ${formatNumber(mGram)} g")
                appendLine("   • Khối lượng mol: M = ${formatNumber(effectiveM)} g/mol")
                appendLine("2. Áp dụng công thức tính số mol:")
                appendLine("   • n = m / M = ${formatNumber(mGram)} / ${formatNumber(effectiveM)} = ${formatNumber(mol)} mol")
                appendLine("3. Đáp số: n = **${formatNumber(mol)} mol**")
            }.trim()
            return "Hóa học" to answer
        }
        if (isChemistryContext && nVal != null && effectiveM != null && mChem == null) {
            val mass = nVal.value * effectiveM
            val answer = buildString {
                appendLine("Bài toán Hóa học (Tính khối lượng):")
                appendLine()
                appendLine("1. Tóm tắt: n = ${formatNumber(nVal.value)} mol; M = ${formatNumber(effectiveM)} g/mol")
                appendLine("2. Công thức: m = n · M = ${formatNumber(nVal.value)} · ${formatNumber(effectiveM)} = ${formatNumber(mass)} g")
                appendLine("3. Đáp số: m = **${formatNumber(mass)} g**")
            }.trim()
            return "Hóa học" to answer
        }

        return null
    }

    private fun extractUnitValue(
        rawInput: String,
        varNames: List<String>,
        units: List<String>
    ): QuantityMatch? {
        val noAccent = stripVietnameseAccents(rawInput.lowercase(Locale.ROOT))
        val cleanUnits = units.filter { it.isNotEmpty() }.joinToString("|") { Regex.escape(stripVietnameseAccents(it.lowercase(Locale.ROOT))) }
        val varGroup = varNames.joinToString("|") { Regex.escape(it) }

        if (cleanUnits.isNotEmpty()) {
            // Pattern 1: "U = 210V" or "khoi luong 400g"
            val p1 = Regex("\\b(?:$varGroup)\\s*(?:=|la|:)?\\s*(\\d+(?:[.,]\\d+)?)\\s*($cleanUnits)(?![a-zA-Z])")
            p1.find(noAccent)?.let { m ->
                val v = m.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
                return QuantityMatch(v, m.groupValues[2])
            }
            // Fallback for symbols with special chars like Ω directly after digits
            val rawLower = rawInput.lowercase(Locale.ROOT)
            val rawUnits = units.filter { it.isNotEmpty() }.joinToString("|") { Regex.escape(it.lowercase(Locale.ROOT)) }
            val pRaw = Regex("\\b(?:$varGroup)\\s*(?:=|la|:)?\\s*(\\d+(?:[.,]\\d+)?)\\s*($rawUnits)")
            pRaw.find(rawLower)?.let { m ->
                val v = m.groupValues[1].replace(",", ".").toDoubleOrNull() ?: return null
                return QuantityMatch(v, m.groupValues[2])
            }
        }
        return null
    }

    private fun detectChemicalMolarMass(rawInput: String): Pair<String, Double>? {
        val rawLower = rawInput.lowercase(Locale.ROOT)
        val symbols = listOf(
            Triple("Sắt (Fe)", listOf("Fe"), 56.0),
            Triple("Đồng (Cu)", listOf("Cu"), 64.0),
            Triple("Kẽm (Zn)", listOf("Zn"), 65.0),
            Triple("Nhôm (Al)", listOf("Al"), 27.0),
            Triple("Magie (Mg)", listOf("Mg"), 24.0),
            Triple("Canxi (Ca)", listOf("Ca"), 40.0),
            Triple("Natri (Na)", listOf("Na"), 23.0),
            Triple("Bạc (Ag)", listOf("Ag"), 108.0),
            Triple("NaOH", listOf("NaOH", "naoh"), 40.0),
            Triple("HCl", listOf("HCl", "hcl"), 36.5),
            Triple("H₂SO₄", listOf("H2SO4", "h2so4", "H₂SO₄"), 98.0),
            Triple("CO₂", listOf("CO2", "co2", "CO₂"), 44.0),
            Triple("CaCO₃", listOf("CaCO3", "caco3", "CaCO₃"), 100.0)
        )
        for ((label, keys, mass) in symbols) {
            if (keys.any { k -> Regex("\\b${Regex.escape(k)}\\b").containsMatchIn(rawInput) }) {
                return label to mass
            }
        }
        val accentedNames = listOf(
            Triple("Sắt (Fe)", listOf("sắt"), 56.0),
            Triple("Đồng (Cu)", listOf("đồng"), 64.0),
            Triple("Kẽm (Zn)", listOf("kẽm"), 65.0),
            Triple("Nhôm (Al)", listOf("nhôm"), 27.0),
            Triple("Magie (Mg)", listOf("magie"), 24.0),
            Triple("Canxi (Ca)", listOf("canxi"), 40.0),
            Triple("Natri (Na)", listOf("natri"), 23.0)
        )
        for ((label, keys, mass) in accentedNames) {
            if (keys.any { k -> Regex("\\b${Regex.escape(k)}\\b").containsMatchIn(rawLower) }) {
                return label to mass
            }
        }
        return null
    }

    private fun convertVoltageToVolt(q: QuantityMatch): Double =
        if (q.unit == "kv") q.value * 1000.0 else q.value

    private fun convertResistanceToOhm(q: QuantityMatch): Double =
        if (q.unit == "kω" || q.unit == "kohm") q.value * 1000.0 else q.value

    private fun convertCurrentToAmpere(q: QuantityMatch): Double =
        if (q.unit == "ma") q.value / 1000.0 else q.value

    private fun convertMassToKg(q: QuantityMatch): Double = when (q.unit) {
        "g", "gam" -> q.value / 1000.0
        "tan", "tấn" -> q.value * 1000.0
        else -> q.value
    }

    private fun convertSpeedToMs(q: QuantityMatch): Double =
        if (q.unit == "km/h" || q.unit == "km/gio") q.value / 3.6 else q.value

    // -------------------------------------------------------------------------
    // 5. K-12 Knowledge Base + Accent-Insensitive Fuzzy Search
    // -------------------------------------------------------------------------

    private data class KnowledgeEntry(
        val subject: String,
        val levelHint: String,
        val keywords: List<String>,
        val answer: String
    )

    private val knowledgeBase: List<KnowledgeEntry> by lazy {
        listOf(
            // --- SINH HỌC ---
            KnowledgeEntry(
                subject = "Sinh học",
                levelHint = "Cấp 2–3",
                keywords = listOf("con nguoi co may gen", "nguoi co bao nhieu gen", "so luong gen", "bo gen nguoi", "may gen"),
                answer = "Ở người, bộ gen có khoảng **20.000 – 25.000 gen mã hóa protein**, phân bố trên 23 cặp nhiễm sắc thể (tổng cộng 46 NST trong tế bào sinh dưỡng).\n\n" +
                    "Giải thích ngắn gọn:\n" +
                    "• Gen là một đoạn của phân tử ADN mang thông tin quy định một chuỗi pôlipeptit hoặc phân tử ARN.\n" +
                    "• Bộ NST 2n = 46 gồm 22 cặp NST thường và 1 cặp NST giới tính (XX ở nữ, XY ở nam)."
            ),
            KnowledgeEntry(
                subject = "Sinh học",
                levelHint = "Cấp 2–3",
                keywords = listOf("nguyen phan", "giam phan", "phan bao"),
                answer = "So sánh Nguyên phân và Giảm phân:\n\n" +
                    "• Nguyên phân (tế bào sinh dưỡng): 1 lần phân bào tạo 2 tế bào con có bộ NST giữ nguyên giống tế bào mẹ (2n).\n" +
                    "• Giảm phân (tế bào sinh dục chín): 2 lần phân bào liên tiếp nhưng ADN chỉ nhân đôi 1 lần → tạo 4 giao tử có bộ NST giảm đi một nửa (n)."
            ),
            KnowledgeEntry(
                subject = "Sinh học",
                levelHint = "Cấp 2–3",
                keywords = listOf("nguyen tac bo sung", "cau truc adn", "cau tao adn", "adn", "nucleotit", "chieu dai gen"),
                answer = "Cấu trúc phân tử ADN & Nguyên tắc bổ sung:\n\n" +
                    "• Đơn phân của ADN là 4 loại nuclêôtit: A, T, G, X.\n" +
                    "• Nguyên tắc bổ sung: A liên kết với T (2 liên kết hiđrô), G liên kết với X (3 liên kết hiđrô).\n" +
                    "• Công thức tính nhanh: N = 2A + 2G; Chiều dài L = (N / 2) · 3,4 Å; Khối lượng M = N · 300 đvC."
            ),
            KnowledgeEntry(
                subject = "Sinh học",
                levelHint = "Cấp 2–3",
                keywords = listOf("quy luat menden", "phan li doc lap", "lai mot cap tinh trang", "menden"),
                answer = "Quy luật di truyền Menđen:\n\n" +
                    "• Quy luật phân li: Aa × Aa → tỉ lệ kiểu gen 1 AA : 2 Aa : 1 aa; tỉ lệ kiểu hình (trội hoàn toàn) 3 trội : 1 lặn.\n" +
                    "• Quy luật phân li độc lập: Các cặp nhân tố di truyền (cặp alen) phân li độc lập trong quá trình phát sinh giao tử (AaBb × AaBb cho tỉ lệ kiểu hình 9 : 3 : 3 : 1)."
            ),
            KnowledgeEntry(
                subject = "Sinh học",
                levelHint = "Cấp 1–2–3",
                keywords = listOf("quang hop", "ho hap te bao", "phuong trinh quang hop"),
                answer = "Phương trình Quang hợp & Hô hấp tế bào:\n\n" +
                    "• Quang hợp (ở lục lạp): 6CO₂ + 6H₂O —(ánh sáng, diệp lục)→ C₆H₁₂O₆ + 6O₂\n" +
                    "• Hô hấp tế bào (ở ti thể): C₆H₁₂O₆ + 6O₂ → 6CO₂ + 6H₂O + Năng lượng (ATP + nhiệt)"
            ),

            // --- TOÁN HỌC ---
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 2–3",
                keywords = listOf("hang dang thuc", "7 hang dang thuc", "bay hang dang thuc dang nho"),
                answer = "7 Hằng đẳng thức đáng nhớ:\n\n" +
                    "1. (A + B)² = A² + 2AB + B²\n" +
                    "2. (A - B)² = A² - 2AB + B²\n" +
                    "3. A² - B² = (A - B)(A + B)\n" +
                    "4. (A + B)³ = A³ + 3A²B + 3AB² + B³\n" +
                    "5. (A - B)³ = A³ - 3A²B + 3AB² - B³\n" +
                    "6. A³ + B³ = (A + B)(A² - AB + B²)\n" +
                    "7. A³ - B³ = (A - B)(A² + AB + B²)"
            ),
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 2–3",
                keywords = listOf("he thuc viet", "dinh li viet", "vi et", "phuong trinh bac hai"),
                answer = "Định lí Vi-ét cho phương trình bậc hai ax² + bx + c = 0 (a ≠ 0):\n\n" +
                    "• Điều kiện có nghiệm: Δ = b² - 4ac ≥ 0\n" +
                    "• Tổng hai nghiệm: S = x₁ + x₂ = -b/a\n" +
                    "• Tích hai nghiệm: P = x₁ · x₂ = c/a\n" +
                    "• Biến đổi thường gặp: x₁² + x₂² = S² - 2P; (x₁ - x₂)² = S² - 4P"
            ),
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 1–2",
                keywords = listOf("dien tich hinh tron", "chu vi hinh tron", "dien tich tam giac", "dien tich hinh thang", "chu vi hinh chu nhat", "dien tich hinh vuong"),
                answer = "Công thức Chu vi & Diện tích hình phẳng (Cấp 1 – Cấp 2):\n\n" +
                    "• Hình chữ nhật: P = (a + b) × 2; S = a × b\n" +
                    "• Hình vuông: P = a × 4; S = a × a\n" +
                    "• Hình tam giác: S = (đáy × chiều cao) / 2\n" +
                    "• Hình thang: S = ((đáy lớn + đáy nhỏ) × chiều cao) / 2\n" +
                    "• Hình tròn (bán kính r, đường kính d): C = 2πr = πd; S = π · r² (π ≈ 3,14)"
            ),
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 2–3",
                keywords = listOf("the tich hinh cau", "the tich hinh tru", "the tich hinh non", "the tich khoi hop"),
                answer = "Công thức Thể tích hình học không gian:\n\n" +
                    "• Hình hộp chữ nhật: V = a · b · c; Hình lập phương: V = a³\n" +
                    "• Hình trụ: V = π · r² · h; Diện tích xung quanh S_xq = 2πrh\n" +
                    "• Hình nón: V = (1/3) · π · r² · h; S_xq = πrl\n" +
                    "• Hình cầu: V = (4/3) · π · r³; Diện tích mặt cầu S = 4πr²"
            ),
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 2",
                keywords = listOf("pytago", "py ta go", "he thuc luong tam giac vuong", "ti so luong giac", "sin cos tan"),
                answer = "Định lí Py-ta-go & Hệ thức lượng trong tam giác vuông ABC vuông tại A (đường cao AH):\n\n" +
                    "• Py-ta-go: BC² = AB² + AC²\n" +
                    "• Hệ thức lượng: AB² = BH·BC; AC² = CH·BC; AH² = BH·CH; AB·AC = AH·BC; 1/AH² = 1/AB² + 1/AC²\n" +
                    "• Lượng giác góc nhọn: sin α = Đối/Huyền; cos α = Kề/Huyền; tan α = Đối/Kề; sin²α + cos²α = 1"
            ),
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 3",
                keywords = listOf("dao ham", "nguyen ham", "tich phan", "bang dao ham"),
                answer = "Bảng Đạo hàm & Nguyên hàm cơ bản (Toán THPT):\n\n" +
                    "• (c)' = 0; (xⁿ)' = n·xⁿ⁻¹; (√x)' = 1/(2√x)\n" +
                    "• (sin x)' = cos x; (cos x)' = -sin x; (eˣ)' = eˣ; (ln x)' = 1/x\n" +
                    "• ∫ xⁿ dx = xⁿ⁺¹ / (n + 1) + C (n ≠ -1); ∫ (1/x) dx = ln|x| + C; ∫ eˣ dx = eˣ + C"
            ),
            KnowledgeEntry(
                subject = "Toán học",
                levelHint = "Cấp 3",
                keywords = listOf("cap so cong", "cap so nhan", "logarit", "to hop chinh hop"),
                answer = "Công thức Cấp số, Logarit & Tổ hợp (Toán THPT):\n\n" +
                    "• Cấp số cộng: uₙ = u₁ + (n - 1)d; Sₙ = n(u₁ + uₙ)/2\n" +
                    "• Cấp số nhân: uₙ = u₁ · qⁿ⁻¹; Sₙ = u₁(1 - qⁿ)/(1 - q) (q ≠ 1)\n" +
                    "• Logarit: log_a(xy) = log_a(x) + log_a(y); log_a(x^k) = k·log_a(x)\n" +
                    "• Chỉnh hợp A(n, k) = n!/(n - k)!; Tổ hợp C(n, k) = n! / (k!·(n - k)!)"
            ),

            // --- VẬT LÍ ---
            KnowledgeEntry(
                subject = "Vật lí",
                levelHint = "Cấp 2–3",
                keywords = listOf("dinh luat om", "mach noi tiep", "mach song song", "cong suat dien", "jun len xo"),
                answer = "Công thức Điện học (Định luật Ôm & Jun – Len-xơ):\n\n" +
                    "• Định luật Ôm: I = U / R (I: Ampe, U: Vôn, R: Ôm)\n" +
                    "• Đoạn mạch nối tiếp: I = I₁ = I₂; U = U₁ + U₂; R_tđ = R₁ + R₂\n" +
                    "• Đoạn mạch song song: U = U₁ = U₂; I = I₁ + I₂; 1/R_tđ = 1/R₁ + 1/R₂\n" +
                    "• Công suất & Nhiệt lượng: P = U·I = I²·R = U²/R; Q = I²·R·t"
            ),
            KnowledgeEntry(
                subject = "Vật lí",
                levelHint = "Cấp 2–3",
                keywords = listOf("thau kinh", "tieu cu", "do tu", "quang hoc"),
                answer = "Công thức Thấu kính (Quang học):\n\n" +
                    "• Công thức thấu kính: 1/f = 1/d + 1/d' (ảnh thật d' > 0, ảnh ảo d' < 0; hội tụ f > 0, phân kỳ f < 0)\n" +
                    "• Số phóng đại: k = -d'/d (|k| = A'B'/AB)\n" +
                    "• Độ tụ: D = 1/f (f tính bằng mét, D tính bằng điốp dp)"
            ),
            KnowledgeEntry(
                subject = "Vật lí",
                levelHint = "Cấp 2–3",
                keywords = listOf("dong nang", "the nang", "co nang", "dinh luat newton", "chuyen dong bien doi deu"),
                answer = "Công thức Cơ học (Cấp 2 – Cấp 3):\n\n" +
                    "• Chuyển động thẳng biến đổi đều: v = v₀ + a·t; s = v₀·t + ½·a·t²; v² - v₀² = 2a·s\n" +
                    "• Định luật II Newton: F = m · a\n" +
                    "• Động năng: W_đ = ½ · m · v²; Thế năng trọng trường: W_t = m · g · h\n" +
                    "• Cơ năng (bảo toàn khi chỉ có trọng lực/lực đàn hồi): W = W_đ + W_t = hằng số"
            ),
            KnowledgeEntry(
                subject = "Vật lí",
                levelHint = "Cấp 3",
                keywords = listOf("dao dong dieu hoa", "con lac lo xo", "con lac don", "song co", "buoc song"),
                answer = "Công thức Dao động & Sóng cơ (Vật lí THPT):\n\n" +
                    "• Dao động điều hòa: x = A·cos(ωt + φ); v_max = ωA; a_max = ω²A; A² = x² + v²/ω²\n" +
                    "• Chu kì con lắc lò xo: T = 2π·√(m/k); Con lắc đơn: T = 2π·√(l/g)\n" +
                    "• Bước sóng: λ = v · T = v / f"
            ),

            // --- HÓA HỌC ---
            KnowledgeEntry(
                subject = "Hóa học",
                levelHint = "Cấp 2–3",
                keywords = listOf("cong thuc tinh mol", "so mol", "nong do mol", "nong do phan tram"),
                answer = "Các công thức tính Mol & Nồng độ Hóa học:\n\n" +
                    "• Số mol theo khối lượng: n = m / M\n" +
                    "• Số mol chất khí ở đkc (25°C, 1 bar): n = V / 24,79\n" +
                    "• Nồng độ mol: C_M = n / V_dd (lít)\n" +
                    "• Nồng độ phần trăm: C% = (m_ct / m_dd) × 100%"
            ),
            KnowledgeEntry(
                subject = "Hóa học",
                levelHint = "Cấp 2–3",
                keywords = listOf("bang hoa tri", "nguyen tu khoi", "khoi luong mol"),
                answer = "Bảng Nguyên tử khối & Hóa trị thường gặp:\n\n" +
                    "• Hóa trị I: H (1), Na (23), K (39), Ag (108), Cl (35,5)\n" +
                    "• Hóa trị II: Mg (24), Ca (40), Zn (65), Ba (137), Cu (64 - I/II), O (16)\n" +
                    "• Hóa trị III: Al (27), Fe (56 - II/III)\n" +
                    "• Phi kim khác: C (12 - II/IV), N (14 - II/III/IV/V), S (32 - II/IV/VI), P (31 - III/V)"
            ),
            KnowledgeEntry(
                subject = "Hóa học",
                levelHint = "Cấp 2–3",
                keywords = listOf("day hoat dong hoa hoc", "day dien hoa", "kim loai tac dung axit"),
                answer = "Dãy hoạt động hóa học của kim loại:\n\n" +
                    "K, Na, Ba, Ca, Mg, Al, Zn, Fe, Ni, Sn, Pb, (H), Cu, Hg, Ag, Pt, Au\n" +
                    "• Kim loại đứng trước (H) tác dụng với dung dịch HCl / H₂SO₄ loãng giải phóng khí H₂.\n" +
                    "• Kim loại đứng trước (trừ K, Na, Ba, Ca) đẩy được kim loại đứng sau ra khỏi dung dịch muối."
            ),

            // --- TIẾNG ANH ---
            KnowledgeEntry(
                subject = "Tiếng Anh",
                levelHint = "Cấp 1–2–3",
                keywords = listOf("12 thi tieng anh", "cac thi trong tieng anh", "thi dong tu", "tense"),
                answer = "Tóm tắt các thì Tiếng Anh trọng tâm:\n\n" +
                    "1. Hiện tại đơn: S + V(s/es) (always, usually, every day)\n" +
                    "2. Hiện tại tiếp diễn: S + am/is/are + V-ing (now, at the moment, Look!)\n" +
                    "3. Quá khứ đơn: S + V₂/ed (yesterday, ago, last..., in + năm quá khứ)\n" +
                    "4. Hiện tại hoàn thành: S + have/has + V₃/ed (since, for, already, yet, just)\n" +
                    "5. Tương lai đơn: S + will + V(nguyên mẫu) (tomorrow, next...)"
            ),
            KnowledgeEntry(
                subject = "Tiếng Anh",
                levelHint = "Cấp 2–3",
                keywords = listOf("cau bi dong", "passive voice", "cong thuc bi dong"),
                answer = "Công thức Câu bị động (Passive Voice):\n\n" +
                    "• Cấu trúc chung: S + be (chia theo thì) + V₃/ed + (by O)\n" +
                    "• Hiện tại đơn: S + am/is/are + V₃/ed\n" +
                    "• Quá khứ đơn: S + was/were + V₃/ed\n" +
                    "• Hiện tại hoàn thành: S + have/has + been + V₃/ed\n" +
                    "• Động từ khuyết thiếu (can/must/should/will): S + modal + be + V₃/ed"
            ),
            KnowledgeEntry(
                subject = "Tiếng Anh",
                levelHint = "Cấp 2–3",
                keywords = listOf("cau dieu kien", "conditional", "dieu kien loai 1", "dieu kien loai 2"),
                answer = "Tổng hợp Câu điều kiện (Conditional Sentences):\n\n" +
                    "• Loại 1 (Có thật ở hiện tại/tương lai): If + S + V(s/es), S + will/can + V\n" +
                    "• Loại 2 (Không có thật ở hiện tại): If + S + V₂/ed (were), S + would/could + V\n" +
                    "• Loại 3 (Không có thật ở quá khứ): If + S + had + V₃/ed, S + would/could + have + V₃/ed"
            ),
            KnowledgeEntry(
                subject = "Tiếng Anh",
                levelHint = "Cấp 2–3",
                keywords = listOf("menh de quan he", "relative clause", "who whom which whose"),
                answer = "Mệnh đề quan hệ (Relative Clauses):\n\n" +
                    "• who: thay thế danh từ chỉ người (làm chủ ngữ/tân ngữ)\n" +
                    "• whom: thay thế danh từ chỉ người (làm tân ngữ)\n" +
                    "• which: thay thế danh từ chỉ vật\n" +
                    "• whose + N: chỉ sự sở hữu\n" +
                    "• that: thay cho who/whom/which trong mệnh đề quan hệ xác định (không dùng sau dấu phẩy)"
            ),

            // --- NGỮ VĂN ---
            KnowledgeEntry(
                subject = "Ngữ văn",
                levelHint = "Cấp 1–2–3",
                keywords = listOf("bien phap tu tu", "an du", "hoan du", "nhan hoa", "so sanh", "diep ngu"),
                answer = "Các biện pháp tu từ thường gặp & tác dụng (Ngữ văn 12 Cấp):\n\n" +
                    "• So sánh: Đối chiếu sự vật có nét tương đồng → tăng sức gợi hình, gợi cảm.\n" +
                    "• Ẩn dụ: Gọi tên sự vật này bằng tên sự vật khác có nét tương đồng → tăng tính hàm súc.\n" +
                    "• Hoán dụ: Gọi tên sự vật dựa trên quan hệ gần gũi (bộ phận – toàn thể, dấu hiệu – sự vật).\n" +
                    "• Nhân hóa: Gán hành động, tình cảm con người cho vật vô tri → câu văn sinh động, gần gũi.\n" +
                    "• Điệp ngữ: Lặp lại từ/cụm từ để nhấn mạnh ý và tạo nhịp điệu."
            ),
            KnowledgeEntry(
                subject = "Ngữ văn",
                levelHint = "Cấp 2–3",
                keywords = listOf("dan y nghi luan xa hoi", "nghi luan xa hoi", "cach viet doan van nghi luan"),
                answer = "Dàn ý chuẩn bài/đoạn văn Nghị luận xã hội:\n\n" +
                    "1. Mở đoạn / Mở bài: Giới thiệu trực tiếp vấn đề nghị luận.\n" +
                    "2. Thân đoạn / Thân bài:\n" +
                    "   • Giải thích ngắn gọn khái niệm/từ khóa chính.\n" +
                    "   • Phân tích biểu hiện, vai trò và ý nghĩa (kèm 1–2 dẫn chứng thực tế tiêu biểu).\n" +
                    "   • Bàn luận mở rộng / Phê phán biểu hiện trái ngược.\n" +
                    "3. Kết đoạn / Kết bài: Rút ra bài học nhận thức và hành động cho bản thân."
            )
        )
    }

    private fun tryFuzzySearchKnowledge(
        question: String,
        detectedSubject: String
    ): Pair<String, String>? {
        val normQuery = normalizeForMatch(question)
        if (normQuery.length < 4) return null

        val queryTokens = normQuery.split(" ").filter { it.length >= 2 }
        // Very long open-ended essays / passages shouldn't falsely match short dictionary tips
        if (queryTokens.size > 22) return null

        var bestEntry: KnowledgeEntry? = null
        var bestScore = 0.0

        for (entry in knowledgeBase) {
            var entryScore = 0.0
            for (rawKw in entry.keywords) {
                val kw = normalizeForMatch(rawKw)
                if (normQuery == kw) {
                    entryScore = maxOf(entryScore, 100.0)
                } else if (normQuery.contains(kw)) {
                    entryScore = maxOf(entryScore, 40.0 + kw.length)
                } else {
                    val kwTokens = kw.split(" ").filter { it.length >= 2 }
                    if (kwTokens.isNotEmpty()) {
                        var matched = 0.0
                        for (kt in kwTokens) {
                            val tokenMatched = queryTokens.any { qt ->
                                qt == kt || (kt.length >= 4 && levenshteinDistance(qt, kt) <= 1)
                            }
                            if (tokenMatched) matched += 1.0
                        }
                        val coverage = matched / kwTokens.size
                        if (coverage >= 0.74 && matched >= 2.0) {
                            entryScore = maxOf(entryScore, coverage * 35.0 + matched * 4.0)
                        } else if (kwTokens.size == 1 && matched == 1.0 && kw.length >= 5) {
                            entryScore = maxOf(entryScore, 28.0)
                        }
                    }
                }
            }

            if (entryScore > 0 && detectedSubject.startsWith(entry.subject.take(4))) {
                entryScore += 6.0
            }

            if (entryScore > bestScore) {
                bestScore = entryScore
                bestEntry = entry
            }
        }

        if (bestEntry != null && bestScore >= 26.0) {
            return "${bestEntry.subject} (${bestEntry.levelHint})" to bestEntry.answer
        }
        return null
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        if (a == b) return 0
        if (a.isEmpty()) return b.length
        if (b.isEmpty()) return a.length
        val prev = IntArray(b.length + 1) { it }
        val curr = IntArray(b.length + 1)
        for (i in 1..a.length) {
            curr[0] = i
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost
                )
            }
            System.arraycopy(curr, 0, prev, 0, b.length + 1)
        }
        return prev[b.length]
    }

    private fun formatNumber(value: Double): String {
        val cleaned = if (abs(value) < 1e-9) 0.0 else value
        val bd = BigDecimal.valueOf(cleaned).setScale(6, RoundingMode.HALF_UP).stripTrailingZeros()
        val plain = bd.toPlainString()
        return if (plain == "-0") "0" else plain
    }
}
