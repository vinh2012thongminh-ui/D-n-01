package com.vinhho.onthi10ai.data.solver

import com.vinhho.onthi10ai.data.model.ChatMessage
import java.util.Locale

internal object OfflineChatResponder {

    fun reply(
        solverEngine: OfflineSolverEngine,
        history: List<ChatMessage>,
        userMessage: String
    ): String {
        val clean = userMessage.trim()
        val lower = clean.lowercase(Locale.ROOT)
        val noAccent = solverEngine.stripVietnameseAccents(lower)
        val recentContext = history.filter { !it.isWelcome }.takeLast(10)

        val lastUserMsg = recentContext.lastOrNull { it.isFromUser }?.text.orEmpty()
        val lastAiMsg = recentContext.lastOrNull { !it.isFromUser }?.text.orEmpty()
        val contextSubject = detectContextSubject(solverEngine, recentContext, clean)

        // 1. Follow-up: Simplify / Explain step-by-step ("Giải thích dễ hiểu hơn", "Tại sao bước 2...")
        if (isExplainSimplerRequest(noAccent)) {
            return buildSimplerExplanation(solverEngine, lastUserMsg, lastAiMsg, contextSubject, clean)
        }

        // 2. Follow-up: Similar example ("Cho ví dụ tương tự", "Thêm 1 ví dụ nữa")
        if (isSimilarExampleRequest(noAccent)) {
            return buildSimilarExample(solverEngine, lastUserMsg, lastAiMsg, contextSubject)
        }

        // 3. Study plan / exam schedule ("Lập kế hoạch ôn thi", "Thời gian biểu")
        if (isStudyPlanRequest(noAccent)) {
            return buildStudyPlan(contextSubject)
        }

        // 4. Memory tricks / Pomodoro / study methods ("Mẹo ghi nhớ nhanh", "Pomodoro")
        if (isMemoryOrMethodRequest(noAccent)) {
            return buildMemoryAndPomodoroTips(contextSubject, noAccent)
        }

        // 5. Emotional support, greetings & gratitude (check before fuzzy knowledge search when conversational)
        if (!clean.any { it.isDigit() }) {
            if (isEmotionalSupportRequest(noAccent)) {
                return buildEmotionalSupport(noAccent)
            }
            if (isGreeting(noAccent)) {
                return "Chào bạn nhé! Mình là anh/chị Gia sư AI luôn sẵn sàng đồng hành cùng bạn từ Lớp 1 đến Lớp 12.\n\n" +
                    "Ngay cả khi chưa có mạng, bạn vẫn có thể nhắn mình:\n" +
                    "• Tính biểu thức hoặc giải phương trình (VD: `2x + 5 = 15`, `x^2 - 5x + 6 = 0`)\n" +
                    "• Tra nhanh công thức Toán, Vật lí, Hóa học, Sinh học, Ngữ văn, Tiếng Anh\n" +
                    "• Nhờ lập kế hoạch ôn thi, hướng dẫn học Pomodoro hoặc tâm sự giảm áp lực.\n\n" +
                    "Bạn đang cần mình hỗ trợ phần nào trước nào?"
            }
            if (isGratitude(noAccent)) {
                return "Không có gì đâu nè, học cùng bạn rất vui! Nếu còn chỗ nào lấn cấn hoặc muốn thử thêm bài tương tự, cứ nhắn mình nhé. Chúc bạn học thật tốt!"
            }
        }

        // 6. Try solving math / physics / chemistry / K-12 knowledge directly offline
        when (val solved = solverEngine.solve(clean, hasImage = false)) {
            is OfflineSolveResult.Solved -> {
                return "Mình đã kiểm tra nhanh câu hỏi thuộc môn **${solved.detectedSubject}** cho bạn nè:\n\n" +
                    "${solved.answer}\n\n" +
                    "Bạn có muốn mình giải thích kỹ hơn bước nào hoặc lấy thêm 1 ví dụ tương tự để luyện tay không?"
            }
            is OfflineSolveResult.NeedsOnline -> Unit
        }

        // 7. Catch any remaining emotional/greeting cases
        if (isEmotionalSupportRequest(noAccent)) {
            return buildEmotionalSupport(noAccent)
        }

        // 8. Friendly offline guidance when open-ended discussion requires online Gemini
        val subjectHint = if (contextSubject != "Kiến thức tổng hợp") " (môn $contextSubject)" else ""
        return "Câu hỏi này khá mở$subjectHint nên để phân tích sâu và tự nhiên nhất, bạn hãy bật kết nối Internet để mình trao đổi bằng AI trực tuyến nhé!\n\n" +
            "Trong lúc ngoại tuyến, bạn vẫn có thể:\n" +
            "• Nhập trực tiếp phép tính, phương trình hoặc công thức để mình giải ngay\n" +
            "• Chọn các gợi ý nhanh phía trên như **Lập kế hoạch ôn thi** hoặc **Mẹo ghi nhớ nhanh**."
    }

    private fun detectContextSubject(
        solverEngine: OfflineSolverEngine,
        recentContext: List<ChatMessage>,
        currentMessage: String
    ): String {
        val currentDetected = solverEngine.detectSubject(currentMessage, hasImage = false)
        if (currentDetected != "Kiến thức tổng hợp") return currentDetected
        for (msg in recentContext.asReversed()) {
            val detected = solverEngine.detectSubject(msg.text, hasImage = false)
            if (detected != "Kiến thức tổng hợp") return detected
        }
        return "Kiến thức tổng hợp"
    }

    private fun isExplainSimplerRequest(noAccent: String): Boolean {
        val triggers = listOf(
            "giai thich de hieu hon", "de hieu hon", "tai sao", "vi sao", "buoc 1", "buoc 2",
            "buoc 3", "chua hieu", "khong hieu", "noi ro hon", "chi tiet hon", "giai thich lai",
            "lam sao ra", "sao lai ra"
        )
        return triggers.any { noAccent.contains(it) }
    }

    private fun isSimilarExampleRequest(noAccent: String): Boolean {
        val triggers = listOf(
            "cho vi du tuong tu", "vi du tuong tu", "them 1 vi du", "them mot vi du",
            "vi du khac", "bai tuong tu", "cho them vi du", "lay vi du"
        )
        return triggers.any { noAccent.contains(it) }
    }

    private fun isStudyPlanRequest(noAccent: String): Boolean {
        val triggers = listOf(
            "lap ke hoach on thi", "ke hoach on thi", "ke hoach hoc", "lich on thi",
            "thoi gian bieu", "lo trinh on", "on thi hieu qua", "chia thoi gian"
        )
        return triggers.any { noAccent.contains(it) }
    }

    private fun isMemoryOrMethodRequest(noAccent: String): Boolean {
        val triggers = listOf(
            "meo ghi nho", "ghi nho nhanh", "pomodoro", "cach hoc thuoc", "hoc thuoc",
            "bi quyet hoc", "mat tap trung", "cach hoc hieu qua", "active recall", "meo lam bai"
        )
        return triggers.any { noAccent.contains(it) }
    }

    private fun isEmotionalSupportRequest(noAccent: String): Boolean {
        val triggers = listOf(
            "ap luc", "stress", "met moi", "qua met", "chan nan", "nan qua", "buon qua",
            "lo lang", "so thi", "so truot", "diem kem", "diem thap", "tam su", "cang thang",
            "khoc", "co don", "khong lam duoc", "bo cuoc"
        )
        return triggers.any { noAccent.contains(it) }
    }

    private fun isGreeting(noAccent: String): Boolean {
        val trimmed = noAccent.trim()
        val greetings = listOf("xin chao", "chao ban", "chao anh", "chao chi", "chao gia su", "hello", "hi", "alo")
        return greetings.any { trimmed == it || trimmed.startsWith("$it ") }
    }

    private fun isGratitude(noAccent: String): Boolean {
        val triggers = listOf("cam on", "thank", "cam on nhieu", "ok cam on", "tam biet", "bye")
        return triggers.any { noAccent.contains(it) }
    }

    private fun buildSimplerExplanation(
        solverEngine: OfflineSolverEngine,
        lastUserMsg: String,
        lastAiMsg: String,
        subject: String,
        currentQuery: String
    ): String {
        if (lastUserMsg.isBlank() && lastAiMsg.isBlank()) {
            return "Bạn hãy gửi cho mình câu hỏi hoặc bài tập bạn đang thấy khó hiểu nhé — mình sẽ tách từng bước nhỏ thật chậm và dễ hiểu cho bạn!"
        }

        val combinedNoAccent = solverEngine.stripVietnameseAccents(
            "$lastUserMsg $lastAiMsg $currentQuery".lowercase(Locale.ROOT)
        )

        return when {
            combinedNoAccent.contains("delta") || combinedNoAccent.contains("x^2") || combinedNoAccent.contains("x²") ->
                "Mình giải thích lại thật ngắn gọn theo kiểu dễ nhớ về bài trước (`$lastUserMsg`) nhé:\n\n" +
                    "1. **Xác định đúng 3 số a, b, c** theo mẫu `ax² + bx + c = 0` (nhớ lấy cả dấu `-` nếu có!).\n" +
                    "2. **Tính Δ = b² - 4ac** để xem phương trình có nghiệm không:\n" +
                    "   • Δ > 0 → 2 nghiệm phân biệt: `x₁ = (-b + √Δ) / (2a)` và `x₂ = (-b - √Δ) / (2a)`.\n" +
                    "   • Δ = 0 → 1 nghiệm kép: `x = -b / (2a)`.\n" +
                    "   • Δ < 0 → Vô nghiệm.\n" +
                    "3. **Mẹo kiểm tra lại**: Thay nghiệm vừa tìm vào đề bài xem vế trái có bằng 0 không!"

            combinedNoAccent.contains("x =") || combinedNoAccent.contains("tim x") || combinedNoAccent.contains("chuyen ve") ->
                "Mình tóm lại quy tắc giải bài trước (`$lastUserMsg`) bằng 3 ý siêu dễ nhớ:\n\n" +
                    "• **Bước 1 (Chuyển vế đổi dấu):** Giữ chữ `x` ở bên trái dấu `=`, chuyển hết số sang bên phải và nhớ **đổi dấu** (`+` thành `-`, `-` thành `+`).\n" +
                    "• **Bước 2 (Thu gọn):** Cộng/trừ các số bên phải lại thành một số gọn.\n" +
                    "• **Bước 3 (Chia hệ số):** Lấy số bên phải chia cho số đứng ngay trước `x` là ra đáp án.\n\n" +
                    "Bạn thấy bước nào còn vướng, nhắn rõ cho mình nhé!"

            combinedNoAccent.contains("u =") || combinedNoAccent.contains("ohm") || combinedNoAccent.contains("dong nang") ->
                "Ở bài Vật lí vừa rồi (`$lastUserMsg`), điều quan trọng nhất là **đổi về đơn vị chuẩn SI trước khi bấm máy**:\n\n" +
                    "• Khối lượng phải là **kg** (`1 kg = 1000 g`), vận tốc là **m/s**, hiệu điện thế là **V**, điện trở là **Ω**.\n" +
                    "• Sau khi đổi đơn vị xong, bạn chỉ cần viết đúng công thức gốc rồi thay số từng đại lượng là ra kết quả chuẩn xác."

            combinedNoAccent.contains("mol") ->
                "Ở bài Hóa vừa rồi (`$lastUserMsg`), bạn chỉ cần nhớ quy tắc **tam giác vàng số mol**:\n\n" +
                    "• Muốn tìm **số mol (`n`)** khi biết khối lượng (`m`): lấy **`n = m / M`** (với `M` là khối lượng mol nguyên tử/phân tử).\n" +
                    "• Ngược lại, muốn tìm **khối lượng (`m`)**: lấy **`m = n × M`**."

            else ->
                "Mình tóm tắt lại ý cốt lõi của nội dung vừa trao đổi ($subject) cho thật gọn nhé:\n\n" +
                    "• **Điểm mấu chốt:** Luôn đọc kỹ đề xem dữ kiện đã cho là gì và đề hỏi gì trước khi viết công thức.\n" +
                    "• **Cách làm từng bước:** Tách bài thành các ý nhỏ (1) Công thức/Quy tắc → (2) Thay số/Áp dụng → (3) Kiểm tra lại đơn vị và dấu.\n\n" +
                    "Bạn muốn mình giải thích kỹ hơn ở ý nào, cứ gõ rõ câu đó ra nha!"
        }
    }

    private fun buildSimilarExample(
        solverEngine: OfflineSolverEngine,
        lastUserMsg: String,
        lastAiMsg: String,
        subject: String
    ): String {
        val combinedNoAccent = solverEngine.stripVietnameseAccents(
            "$lastUserMsg $lastAiMsg".lowercase(Locale.ROOT)
        )

        return when {
            combinedNoAccent.contains("delta") || combinedNoAccent.contains("x^2") || combinedNoAccent.contains("x²") ->
                "Đây là **1 ví dụ tương tự** để bạn nắm chắc dạng phương trình bậc hai nhé:\n\n" +
                    "**Đề bài:** Giải phương trình `x² - 7x + 10 = 0`\n" +
                    "• Hệ số: `a = 1`, `b = -7`, `c = 10`\n" +
                    "• Tính biệt thức: `Δ = (-7)² - 4·1·10 = 49 - 40 = 9 > 0` (√Δ = 3)\n" +
                    "• Nghiệm 1: `x₁ = (7 + 3) / 2 = 5`\n" +
                    "• Nghiệm 2: `x₂ = (7 - 3) / 2 = 2`\n\n" +
                    "**Thử sức ngay:** Bạn thử nhập `x^2 - 6x + 8 = 0` để tự kiểm tra kết quả xem có ra `4` và `2` không nha!"

            combinedNoAccent.contains("x =") || combinedNoAccent.contains("tim x") || subject.startsWith("Toán") ->
                "Mình tặng bạn **1 ví dụ tương tự** cực dễ hiểu:\n\n" +
                    "**Đề bài:** Tìm `x` biết `3x - 4 = 11`\n" +
                    "• Bước 1 (Chuyển `-4` sang phải thành `+4`): `3x = 11 + 4 = 15`\n" +
                    "• Bước 2 (Chia cả hai vế cho `3`): `x = 15 / 3 = 5`\n\n" +
                    "**Thử sức:** Bạn thử giải câu `4x + 3 = 19` rồi nhắn vào đây để mình chấm cùng bạn nhé!"

            subject.startsWith("Vật lí") ->
                "Đây là **1 ví dụ Vật lí tương tự**:\n\n" +
                    "**Đề bài:** Một mạch điện có hiệu điện thế `U = 12 V`, điện trở `R = 4 Ω`. Tính cường độ dòng điện `I`.\n" +
                    "• Áp dụng định luật Ôm: `I = U / R = 12 / 4 = 3 A`.\n\n" +
                    "Bạn có thể nhắn một bài khác có số liệu mới (VD: `U = 24V, R = 6Ω`) để mình kiểm tra cùng bạn!"

            subject.startsWith("Hóa học") ->
                "Đây là **1 ví dụ Hóa học tương tự**:\n\n" +
                    "**Đề bài:** Tính số mol của `m = 11,2 g` sắt (`Fe`, `M = 56 g/mol`).\n" +
                    "• Áp dụng công thức: `n = m / M = 11,2 / 56 = 0,2 mol`.\n\n" +
                    "Bạn thử nhắn `Tính số mol của m = 8g CuO` hoặc bất kỳ chất nào khác để mình giải cùng bạn nhé!"

            subject.startsWith("Tiếng Anh") ->
                "Đây là **1 ví dụ Tiếng Anh tương tự** (Câu bị động - Passive Voice):\n\n" +
                    "• **Chủ động:** *Lan cleans the classroom every day.*\n" +
                    "• **Bị động:** *The classroom is cleaned by Lan every day.*\n" +
                    "→ Mẹo: Đưa tân ngữ (`the classroom`) lên đầu, chia `to be` theo thì (`is`), và đưa động từ chính về cột 3 (`cleaned`)."

            else ->
                "Đây là **1 ví dụ minh họa cách trình bày chuẩn**:\n\n" +
                    "• **Bước 1 – Tóm tắt ý chính:** Gạch chân 2–3 từ khóa quan trọng nhất trong đề.\n" +
                    "• **Bước 2 – Áp dụng kiến thức:** Nêu đúng định lý/công thức/luận điểm trọng tâm.\n" +
                    "• **Bước 3 – Kết luận rõ ràng:** Chốt lại đáp số hoặc thông điệp ngắn gọn.\n\n" +
                    "Bạn có thể gõ đề bài cụ thể (Toán, Lí, Hóa, Văn, Anh...) để mình làm mẫu sát đúng bài đó nhé!"
        }
    }

    private fun buildStudyPlan(subject: String): String {
        val focusNote = if (subject != "Kiến thức tổng hợp") {
            " (ưu tiên trọng tâm môn **$subject**)"
        } else {
            ""
        }
        return "Dưới đây là **Kế hoạch ôn thi khoa học trong 7 ngày**$focusNote mà không bị quá tải:\n\n" +
            "• **Ngày 1–2 (Củng cố nền tảng):** Hệ thống lại toàn bộ công thức, khái niệm cốt lõi và ghi ra 1 tờ giấy tóm tắt (mindmap).\n" +
            "• **Ngày 3–4 (Luyện theo chuyên đề):** Mỗi ngày chọn 2 chuyên đề còn yếu, làm 15–20 câu từ dễ đến vừa để chắc điểm nền.\n" +
            "• **Ngày 5 (Tổng ôn đề chuẩn):** Bấm giờ làm trọn vẹn 1 đề 30–40 câu ở tab **Luyện đề** giống thi thật.\n" +
            "• **Ngày 6 (Chữa lỗi sai):** Lập \"sổ tay lỗi sai\" — chỉ ôn lại những câu hay nhầm dấu, nhầm đơn vị hoặc đọc sót từ phủ định.\n" +
            "• **Ngày 7 (Thư giãn & Giữ phong độ):** Xem nhẹ lại sơ đồ tóm tắt, ngủ đủ giấc trước ngày thi.\n\n" +
            "**Khung giờ vàng mỗi tối:** Học 2 phiên Pomodoro (mỗi phiên 25 phút tập trung + 5 phút nghỉ), hiệu quả hơn ngồi lì 3 tiếng liên tục rất nhiều!"
    }

    private fun buildMemoryAndPomodoroTips(subject: String, noAccent: String): String {
        val extraTip = when {
            subject.startsWith("Tiếng Anh") ->
                "\n• **Mẹo riêng Tiếng Anh:** Học từ vựng theo cụm (collocation) và đặt 1 câu gắn với bản thân thay vì chép từ đơn lẻ."
            subject.startsWith("Toán") || subject.startsWith("Vật lí") || subject.startsWith("Hóa học") ->
                "\n• **Mẹo riêng Tự nhiên:** Thay vì đọc thuộc công thức bằng mắt, hãy che đáp án và tự viết lại công thức kèm ý nghĩa từng ký hiệu 3 lần."
            subject.startsWith("Ngữ văn") || subject.startsWith("Lịch sử") || subject.startsWith("Địa lí") ->
                "\n• **Mẹo riêng Xã hội:** Vẽ sơ đồ nhánh gồm **Từ khóa → Mốc/Sự kiện → Ý nghĩa**, rồi tự giảng lại thành tiếng trong 2 phút."
            else -> ""
        }

        val intro = if (noAccent.contains("pomodoro")) {
            "**Phương pháp Pomodoro** giúp bạn học tập trung gấp đôi mà không mệt:"
        } else {
            "**4 Mẹo ghi nhớ siêu tốc** dành cho học sinh 12 cấp:"
        }

        return "$intro\n\n" +
            "1. **Kỹ thuật Pomodoro (25/5):** Tập trung tuyệt đối 25 phút (tắt thông báo điện thoại) → nghỉ 5 phút đứng dậy uống nước/vươn vai. Sau 4 phiên thì nghỉ dài 20 phút.\n" +
            "2. **Active Recall (Chủ động gợi nhớ):** Đọc xong 1 mục, gấp sách lại và tự nhẩm xem mình nhớ được gì — việc \"ép não nhớ\" giúp lưu kiến thức lâu gấp 3 lần đọc lại thụ động.\n" +
            "3. **Kỹ thuật Feynman:** Thử giải thích kiến thức đó bằng ngôn ngữ đơn giản nhất như đang giảng cho em nhỏ nghe.\n" +
            "4. **Lặp lại ngắt quãng (1–3–7):** Ôn lại sau 1 ngày, 3 ngày và 7 ngày để đẩy kiến thức vào trí nhớ dài hạn.$extraTip"
    }

    private fun buildEmotionalSupport(noAccent: String): String {
        val closing = if (noAccent.contains("diem")) {
            "Một bài kiểm tra điểm chưa cao chỉ là tín hiệu chỉ ra phần kiến thức nào cần vá lại thôi, hoàn toàn không định nghĩa năng lực của bạn!"
        } else {
            "Bạn không hề đi một mình đâu — cứ đi từng bước nhỏ mỗi ngày, chắc chắn bạn sẽ tiến bộ."
        }
        return "Hít một hơi thật sâu nhé, anh/chị ở đây lắng nghe bạn nè!\n\n" +
            "Cảm giác mệt mỏi hay áp lực trước kỳ thi là điều học sinh nào cũng từng trải qua — điều đó chứng tỏ bạn rất quan tâm tới kết quả của mình. Ngay lúc này, bạn thử làm 3 việc nhỏ sau cùng mình:\n\n" +
            "1. **Tạm dừng 3 phút:** Đặt bút xuống, uống một ngụm nước mát và hít sâu 4 giây – giữ 4 giây – thở ra chậm 6 giây.\n" +
            "2. **Thu nhỏ mục tiêu:** Đừng nghĩ đến cả núi bài vở. Chỉ chọn **1 bài tập nhỏ nhất** hoặc **15 phút ôn 1 phần dễ nhất** để lấy lại cảm giác chiến thắng.\n" +
            "3. **Chia sẻ bài khó:** Có câu nào đang làm bạn bí, cứ gõ vào đây — mình cùng gỡ từng bước với bạn.\n\n" +
            closing
    }
}
