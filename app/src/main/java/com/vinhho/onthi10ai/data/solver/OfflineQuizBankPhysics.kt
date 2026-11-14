package com.vinhho.onthi10ai.data.solver

import com.vinhho.onthi10ai.data.solver.OfflineQuizBank.RawQuestion
import kotlin.math.abs

internal object OfflineQuizBankPhysics {

    fun buildForGrade(grade: Int): List<RawQuestion> {
        val list = mutableListOf<RawQuestion>()
        when (grade) {
            in 1..5 -> list.addAll(primarySciencePhysics(grade))
            6 -> {
                val tag = "[KHTN / Vật lí Lớp 6]"
                // 25 unique mass -> weight questions (distinct masses 2..50)
                for (i in 1..25) {
                    val m = i * 2
                    val p = m * 10
                    list.add(
                        RawQuestion(
                            question = "$tag Một vật có khối lượng $m kg có trọng lượng xấp xỉ bao nhiêu Niutơn (lấy g ≈ 10 N/kg)?",
                            options = distinctNumbers(p, listOf(m, p + 10, p / 2)).map { "$it N" },
                            correctIndex = 0,
                            explanation = "Áp dụng công thức P = 10 · m = 10 × $m = $p N."
                        )
                    )
                }
                // 10 unique unit conversions kg -> g
                for (k in 1..10) {
                    val kg = k * 3 + 1
                    val g = kg * 1000
                    list.add(
                        RawQuestion(
                            question = "$tag Đổi đơn vị khối lượng: $kg kg bằng bao nhiêu gam?",
                            options = distinctNumbers(g, listOf(kg * 100, kg * 10, g + 500)).map { "$it g" },
                            correctIndex = 0,
                            explanation = "1 kg = 1.000 g ⇒ $kg kg = $g g."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Đơn vị đo chiều dài hợp pháp trong hệ đo lường quốc tế SI là:", listOf("Mét (m)", "Kilôgam (kg)", "Giây (s)", "Niutơn (N)"), 0, "Đơn vị chuẩn đo độ dài trong hệ SI là mét (m)."),
                        RawQuestion("$tag Dụng cụ dùng để đo khối lượng của một vật là:", listOf("Cân", "Thước kẻ", "Lực kế", "Nhiệt kế"), 0, "Đo khối lượng dùng cân; đo độ lớn lực dùng lực kế."),
                        RawQuestion("$tag Nhiệt kế y tế dùng để đo:", listOf("Nhiệt độ cơ thể người", "Khối lượng cơ thể", "Áp suất khí quyển", "Cường độ dòng điện"), 0, "Nhiệt kế y tế đo nhiệt độ cơ thể trong khoảng 35°C – 42°C."),
                        RawQuestion("$tag Dụng cụ dùng để đo độ lớn của lực là:", listOf("Lực kế", "Nhiệt kế", "Đồng hồ bấm giây", "Ống đong"), 0, "Lực kế lò xo hoạt động dựa vào sự biến dạng đàn hồi của lò xo."),
                        RawQuestion("$tag Lực hút của Trái Đất tác dụng lên mọi vật gần mặt đất gọi là:", listOf("Trọng lực", "Lực ma sát", "Lực đàn hồi", "Lực đẩy Ác-si-mét"), 0, "Trọng lực có phương thẳng đứng và chiều hướng về phía Trái Đất."),
                        RawQuestion("$tag Nước tinh khiết đóng băng ở áp suất tiêu chuẩn tại nhiệt độ nào?", listOf("0°C", "100°C", "37°C", "-10°C"), 0, "Ở 1 atm, nước đá tan / đóng băng ở 0°C và sôi ở 100°C."),
                        RawQuestion("$tag Dạng năng lượng mà một vật có được do chuyển động gọi là:", listOf("Động năng", "Hóa năng", "Quang năng", "Thế năng đàn hồi"), 0, "Vật chuyển động càng nhanh thì động năng càng lớn."),
                        RawQuestion("$tag Lực nào xuất hiện khi bánh xe trượt trên mặt đường giúp xe phanh dừng lại?", listOf("Lực ma sát trượt", "Trọng lực", "Lực đẩy của gió", "Lực từ"), 0, "Lực ma sát cản trở chuyển động trượt giữa hai bề mặt tiếp xúc.")
                    )
                )
            }
            7 -> {
                val tag = "[KHTN / Vật lí Lớp 7]"
                val speeds = listOf(12, 15, 18, 20, 24, 25, 30, 32, 36, 40, 42, 45, 48, 50, 54, 55, 60, 65, 70, 72, 75, 80, 84, 90, 96)
                speeds.forEachIndexed { idx, v ->
                    val t = (idx % 4) + 2
                    val s = v * t
                    list.add(
                        RawQuestion(
                            question = "$tag Một ô tô đi đều hết quãng đường $s km trong thời gian $t giờ. Tốc độ của xe là:",
                            options = distinctNumbers(v, listOf(v + 5, s + t, v * 2)).map { "$it km/h" },
                            correctIndex = 0,
                            explanation = "Tốc độ v = s / t = $s / $t = $v km/h."
                        )
                    )
                }
                for (i in 1..10) {
                    val inc = 15 + i * 5
                    list.add(
                        RawQuestion(
                            question = "$tag Chiếu tia sáng tới gương phẳng với góc tới bằng ${inc}°. Góc phản xạ bằng bao nhiêu?",
                            options = distinctNumbers(inc, listOf(90 - inc, inc * 2, 180 - inc)).map { "$it°" },
                            correctIndex = 0,
                            explanation = "Theo định luật phản xạ ánh sáng, góc phản xạ bằng góc tới: i' = i = $inc°."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Sóng âm KHÔNG truyền được trong môi trường nào?", listOf("Chân không", "Chất rắn", "Chất lỏng", "Chất khí"), 0, "Âm thanh truyền qua dao động hạt vật chất nên không truyền được trong chân không."),
                        RawQuestion("$tag Độ cao (bổng hay trầm) của âm phụ thuộc vào đại lượng nào?", listOf("Tần số dao động (Hz)", "Biên độ dao động", "Màu sắc nguồn âm", "Nhiệt độ phòng"), 0, "Tần số càng lớn âm phát ra càng cao (bổng); biên độ càng lớn âm càng to."),
                        RawQuestion("$tag Đơn vị đo tốc độ hợp pháp trong hệ SI là:", listOf("m/s", "km/h", "km", "m/s²"), 0, "Đơn vị chuẩn SI là mét trên giây (m/s); ngoài ra thường dùng km/h."),
                        RawQuestion("$tag Ảnh của một vật tạo bởi gương phẳng có tính chất nào?", listOf("Ảnh ảo, cùng chiều và có kích thước bằng vật", "Ảnh thật, ngược chiều với vật", "Ảnh ảo, lớn hơn vật", "Ảnh thật, nhỏ hơn vật"), 0, "Khoảng cách từ ảnh tới gương phẳng bằng khoảng cách từ vật tới gương."),
                        RawQuestion("$tag Vật liệu nào sau đây hấp thụ âm tốt và thường dùng làm tường cách âm?", listOf("Xốp mềm, rèm nhung, thảm nỉ", "Tấm thép phẳng nhẵn", "Mặt đá hoa cương", "Tấm kính dày"), 0, "Vật liệu mềm, bề mặt gồ ghề, xốp phản xạ âm kém và hấp thụ âm tốt."),
                        RawQuestion("$tag Đầu kim la bàn chỉ hướng Bắc - Nam địa lí do chịu tác dụng của yếu tố nào?", listOf("Từ trường Trái Đất", "Trọng lực của Mặt Trăng", "Gió mùa Đông Bắc", "Lực đẩy Ác-si-mét"), 0, "Trái Đất đóng vai trò như một thanh nam châm khổng lồ."),
                        RawQuestion("$tag Để giảm tiếng ồn gây ô nhiễm gần trường học và bệnh viện, ta KHÔNG nên làm gì?", listOf("Bấm còi hơi liên tục khi đi qua", "Trồng nhiều cây xanh quanh tường rào", "Treo biển cấm bóp còi", "Lắp cửa kính cách âm"), 0, "Tiếng còi lớn gây ô nhiễm tiếng ồn ảnh hưởng thính giác và sự tập trung."),
                        RawQuestion("$tag Tốc độ truyền âm trong các môi trường thường xếp theo thứ tự giảm dần là:", listOf("Chất rắn > Chất lỏng > Chất khí", "Chất khí > Chất lỏng > Chất rắn", "Chất lỏng > Chất rắn > Chất khí", "Chân không > Chất rắn > Chất khí"), 0, "Các phân tử chất rắn xếp khít nhau nên truyền dao động âm nhanh nhất.")
                    )
                )
            }
            8 -> {
                val tag = "[KHTN / Vật lí Lớp 8]"
                for (i in 1..22) {
                    val s = (i % 5) + 2
                    val p = 120 * i
                    val f = p * s
                    list.add(
                        RawQuestion(
                            question = "$tag Tác dụng áp lực F = ${f} N vuông góc lên diện tích bị ép S = ${s} m². Áp suất gây ra bằng:",
                            options = distinctNumbers(p, listOf(p + 60, f + s, p * 2)).map { "$it Pa" },
                            correctIndex = 0,
                            explanation = "Áp suất p = F / S = $f / $s = $p Pa."
                        )
                    )
                }
                for (i in 1..12) {
                    val d = 800 + i * 50
                    val v = (i % 4) + 2
                    val m = d * v
                    list.add(
                        RawQuestion(
                            question = "$tag Một khối vật liệu có thể tích V = $v m³ và khối lượng riêng D = $d kg/m³ có khối lượng bằng:",
                            options = distinctNumbers(m, listOf(m + d, m / 2, d + v)).map { "$it kg" },
                            correctIndex = 0,
                            explanation = "Khối lượng m = D · V = $d × $v = $m kg."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Công thức tính lực đẩy Ác-si-mét tác dụng lên vật nhúng trong chất lỏng là:", listOf("F_A = d · V", "F_A = m / V", "F_A = F / S", "F_A = P · t"), 0, "Trong đó d là trọng lượng riêng chất lỏng, V là thể tích phần chất lỏng bị vật chiếm chỗ."),
                        RawQuestion("$tag Đơn vị của khối lượng riêng trong hệ SI là:", listOf("kg/m³", "N/m²", "kg·m/s", "J/kg"), 0, "D = m / V có đơn vị chuẩn kg/m³."),
                        RawQuestion("$tag Một vật đặc nhúng chìm hoàn toàn vào chất lỏng sẽ nổi lên mặt thoáng khi:", listOf("Trọng lượng riêng của vật nhỏ hơn trọng lượng riêng chất lỏng (d_vật < d_lỏng)", "d_vật > d_lỏng", "Thể tích vật bằng 0", "Chất lỏng không có trọng lượng"), 0, "Khi lực đẩy Ác-si-mét lớn hơn trọng lượng vật, vật nổi lên."),
                        RawQuestion("$tag Dòng điện trong kim loại là dòng dịch chuyển có hướng của hạt nào?", listOf("Các electron tự do", "Các hạt proton", "Các hạt neutron", "Các ion dương cố định"), 0, "Electron tự do dịch chuyển từ cực âm sang cực dương của nguồn điện."),
                        RawQuestion("$tag Dụng cụ mắc nối tiếp trong mạch để đo cường độ dòng điện là:", listOf("Ampe kế", "Vôn kế", "Nhiệt kế", "Lực kế"), 0, "Ampe kế mắc nối tiếp; Vôn kế mắc song song để đo hiệu điện thế."),
                        RawQuestion("$tag Cánh tay đòn càng dài thì lực cần thiết để làm quay vật quanh trục cố định (mômen lực không đổi) sẽ:", listOf("Càng nhỏ", "Càng lớn", "Không đổi", "Bằng 0"), 0, "Mômen lực M = F · d; d càng lớn thì lực F cần tác dụng càng nhỏ."),
                        RawQuestion("$tag Quá trình truyền nhiệt chủ yếu diễn ra trong chất rắn (như thìa kim loại nhúng vào nước nóng) là:", listOf("Dẫn nhiệt", "Đối lưu", "Bức xạ nhiệt", "Bay hơi"), 0, "Chất rắn truyền nhiệt chủ yếu bằng hình thức dẫn nhiệt."),
                        RawQuestion("$tag Chất nào dẫn điện tốt nhất trong điều kiện bình thường ở nhóm sau?", listOf("Bạc (Ag) và Đồng (Cu)", "Nhựa PVC", "Thủy tinh khô", "Cao su lưu hóa"), 0, "Kim loại chứa mật độ electron tự do lớn nên dẫn điện tốt.")
                    )
                )
            }
            9 -> {
                val tag = "[KHTN / Vật lí Lớp 9]"
                for (i in 1..15) {
                    val r = 5 * i
                    val iAmp = (i % 5) + 1
                    val u = r * iAmp
                    list.add(
                        RawQuestion(
                            question = "$tag Đặt hiệu điện thế U = ${u} V vào hai đầu điện trở R = ${r} Ω. Cường độ dòng điện qua điện trở là:",
                            options = distinctNumbers(iAmp, listOf(iAmp + 1, iAmp + 2, u + r)).map { "$it A" },
                            correctIndex = 0,
                            explanation = "Định luật Ôm: I = U / R = $u / $r = $iAmp A."
                        )
                    )
                }
                for (i in 1..15) {
                    val r1 = 4 + i * 2
                    val r2 = 6 + i * 3
                    val rSum = r1 + r2
                    list.add(
                        RawQuestion(
                            question = "$tag Hai điện trở R₁ = ${r1} Ω và R₂ = ${r2} Ω mắc nối tiếp. Điện trở tương đương của đoạn mạch là:",
                            options = distinctNumbers(rSum, listOf(abs(r2 - r1) + 1, r1 * r2, rSum + 5)).map { "$it Ω" },
                            correctIndex = 0,
                            explanation = "Mạch nối tiếp: R_tđ = R₁ + R₂ = $r1 + $r2 = $rSum Ω."
                        )
                    )
                }
                for (i in 1..8) {
                    val u = 10 + i * 10
                    val amp = i + 1
                    val pWatt = u * amp
                    list.add(
                        RawQuestion(
                            question = "$tag Một bóng đèn hoạt động bình thường ở hiệu điện thế U = $u V với cường độ dòng điện I = $amp A. Công suất tiêu thụ của đèn là:",
                            options = distinctNumbers(pWatt, listOf(pWatt + 20, u + amp, pWatt * 2)).map { "$it W" },
                            correctIndex = 0,
                            explanation = "Công suất điện P = U · I = $u × $amp = $pWatt W."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Công thức định luật Jun - Len-xơ tính nhiệt lượng tỏa ra trên dây dẫn là:", listOf("Q = I² · R · t", "Q = I · R · t", "Q = U · I / t", "Q = R · t / I²"), 0, "Định luật Jun - Len-xơ: Q = I² · R · t (tính bằng Jun)."),
                        RawQuestion("$tag Thấu kính hội tụ cho ảnh thật ngược chiều, lớn hơn vật khi vật đặt ở vị trí:", listOf("f < d < 2f", "d < f", "d = f", "d > 2f"), 0, "Khi d > 2f ảnh thật nhỏ hơn vật; khi d < f cho ảnh ảo cùng chiều lớn hơn vật."),
                        RawQuestion("$tag Hiện tượng khúc xạ ánh sáng xảy ra khi tia sáng truyền xiên góc qua:", listOf("Mặt phân cách giữa hai môi trường trong suốt khác nhau", "Mặt gương phẳng mạ bạc", "Tấm gỗ đặc", "Chân không đồng nhất"), 0, "Tia sáng bị gãy khúc tại mặt phân cách giữa hai môi trường trong suốt."),
                        RawQuestion("$tag Thế năng trọng trường của một vật phụ thuộc vào đại lượng nào?", listOf("Khối lượng của vật và độ cao so với mốc thế năng", "Vận tốc và màu sắc", "Hình dạng và mùi vị", "Cường độ dòng điện"), 0, "Công thức W_t = P · h = 10 · m · h (hay m · g · h)."),
                        RawQuestion("$tag Khi không có lực cản ma sát, tổng động năng và thế năng của vật rơi tự do thay đổi như thế nào?", listOf("Được bảo toàn (không đổi)", "Tăng lên vô hạn", "Giảm dần về 0", "Chuyển hết thành từ năng"), 0, "Định luật bảo toàn cơ năng: W = W_đ + W_t = hằng số.")
                    )
                )
            }
            10 -> {
                val tag = "[Vật lí Lớp 10]"
                for (i in 1..18) {
                    val m = 2 + i
                    val a = 2 + (i % 5)
                    val f = m * a
                    list.add(
                        RawQuestion(
                            question = "$tag Một vật khối lượng $m kg thu gia tốc $a m/s². Hợp lực tác dụng lên vật có độ lớn:",
                            options = distinctNumbers(f, listOf(f + m, m + a, f * 2)).map { "$it N" },
                            correctIndex = 0,
                            explanation = "Theo định luật II Newton: F = m · a = $m × $a = $f N."
                        )
                    )
                }
                for (i in 1..18) {
                    val m = 2 * i
                    val v = 3 + (i % 5)
                    val ke = (m * v * v) / 2
                    list.add(
                        RawQuestion(
                            question = "$tag Vật khối lượng $m kg chuyển động với tốc độ $v m/s có động năng bằng:",
                            options = distinctNumbers(ke, listOf(m * v, ke * 2, ke + 4)).map { "$it J" },
                            correctIndex = 0,
                            explanation = "Động năng W_đ = ½ · m · v² = 0,5 × $m × $v² = $ke J."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Công thức tính quãng đường rơi tự do (v₀ = 0) trong thời gian t là:", listOf("s = ½ · g · t²", "s = g · t", "s = 2 · g · t²", "s = g · t²"), 0, "Rơi tự do là chuyển động thẳng nhanh dần đều với gia tốc g."),
                        RawQuestion("$tag Đơn vị của công cơ học và năng lượng trong hệ SI là:", listOf("Jun (J)", "Oát (W)", "Niutơn (N)", "Mét trên giây (m/s)"), 0, "1 J = 1 N·m."),
                        RawQuestion("$tag Trong chuyển động tròn đều, vectơ gia tốc hướng tâm luôn có hướng nào?", listOf("Hướng vào tâm quỹ đạo tròn", "Tiếp tuyến cùng chiều chuyển động", "Hướng ra xa tâm", "Thẳng đứng hướng lên"), 0, "Gia tốc hướng tâm a_ht = v² / r = ω² · r."),
                        RawQuestion("$tag Động lượng của một vật khối lượng m chuyển động với vận tốc v được xác định bởi:", listOf("p = m · v", "p = ½ · m · v²", "p = m · g · h", "p = F · s"), 0, "Đơn vị động lượng là kg·m/s (hoặc N·s)."),
                        RawQuestion("$tag Định luật III Newton phát biểu rằng lực và phản lực là hai lực trực đối:", listOf("Cùng phương, ngược chiều, cùng độ lớn nhưng đặt vào hai vật khác nhau", "Đặt vào cùng một vật nên tự triệt tiêu", "Vuông góc với nhau", "Có độ lớn khác nhau"), 0, "F_AB = -F_BA."),
                        RawQuestion("$tag Lực đàn hồi của lò xo xuất hiện khi lò xo bị biến dạng có độ lớn tỉ lệ thuận với:", listOf("Độ biến dạng |Δl| trong giới hạn đàn hồi (Định luật Hooke)", "Khối lượng khí quyển", "Nhiệt độ phòng", "Bình phương thời gian"), 0, "F_đh = k · |Δl|.")
                    )
                )
            }
            11 -> {
                val tag = "[Vật lí Lớp 11]"
                for (i in 1..18) {
                    val a = 2 + i
                    val vmax = a * 10
                    list.add(
                        RawQuestion(
                            question = "$tag Vật dao động điều hòa với biên độ A = $a cm và tần số góc ω = 10 rad/s. Tốc độ cực đại là:",
                            options = distinctNumbers(vmax, listOf(vmax * 10, a * 20, vmax / 2)).map { "$it cm/s" },
                            correctIndex = 0,
                            explanation = "v_max = ω · A = 10 × $a = $vmax cm/s."
                        )
                    )
                }
                for (f in 1..18) {
                    val freq = f * 5
                    val v = 100 + f * 25
                    val lambda = v / freq
                    list.add(
                        RawQuestion(
                            question = "$tag Sóng cơ tần số f = $freq Hz truyền với tốc độ v = $v m/s có bước sóng λ bằng:",
                            options = distinctNumbers(lambda, listOf(lambda + 2, lambda * 2, v * freq)).map { "$it m" },
                            correctIndex = 0,
                            explanation = "Bước sóng λ = v / f = $v / $freq = $lambda m."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Chu kì dao động nhỏ của con lắc lò xo (độ cứng k, khối lượng m) là:", listOf("T = 2π · √(m / k)", "T = 2π · √(k / m)", "T = (1 / 2π) · √(m / k)", "T = 2π · m / k"), 0, "Tần số góc ω = √(k/m) ⇒ T = 2π/ω = 2π·√(m/k)."),
                        RawQuestion("$tag Điều kiện để hai nguồn sóng kết hợp giao thoa với nhau tạo cực đại tại điểm M là hiệu đường đi d₂ - d₁ bằng:", listOf("Một số nguyên lần bước sóng (k · λ)", "Một số bán nguyên lần bước sóng", "Bằng 0 duy nhất", "λ / 4"), 0, "Cực đại giao thoa: d₂ - d₁ = k·λ; cực tiểu: (k + 0,5)·λ."),
                        RawQuestion("$tag Định luật Cu-lông cho biết lực tương tác tĩnh điện giữa hai điện tích điểm tỉ lệ nghịch với:", listOf("Bình phương khoảng cách giữa hai điện tích (r²)", "Khoảng cách r", "Lập phương khoảng cách r³", "Khối lượng điện tích"), 0, "F = k · |q₁q₂| / (ε · r²)."),
                        RawQuestion("$tag Cường độ điện trường tại một điểm đặc trưng cho phương diện nào của điện trường?", listOf("Tác dụng lực điện lên điện tích thử đặt tại điểm đó", "Khối lượng không khí", "Nhiệt độ môi trường", "Từ thông xuyên qua dây"), 0, "E = F / |q|, đơn vị là V/m."),
                        RawQuestion("$tag Tụ điện là hệ gồm hai vật dẫn đặt gần nhau và ngăn cách nhau bởi:", listOf("Lớp điện môi (chất cách điện)", "Dây đồng nối tắt", "Thanh sắt non", "Dung dịch axit mạnh"), 0, "Đại lượng đặc trưng cho khả năng tích điện là điện dung C (Fara)."),
                        RawQuestion("$tag Hiện tượng cộng hưởng cơ xảy ra khi tần số của ngoại lực cưỡng bức:", listOf("Bằng tần số dao động riêng của hệ", "Bằng một nửa tần số riêng", "Bằng 0 Hz", "Lớn gấp 10 lần tần số riêng"), 0, "Khi cộng hưởng, biên độ dao động cưỡng bức đạt giá trị cực đại.")
                    )
                )
            }
            else -> {
                val tag = "[Vật lí Lớp 12]"
                for (i in 1..18) {
                    val m = i
                    val dt = 10 + i * 2
                    val c = 4200
                    val qKJ = (m * c * dt) / 1000
                    list.add(
                        RawQuestion(
                            question = "$tag Nhiệt lượng cần thiết để đun $m kg nước (nhiệt dung riêng c = 4200 J/kg.K) tăng thêm $dt°C là:",
                            options = distinctNumbers(qKJ, listOf(qKJ * 2, qKJ + 42, qKJ / 2)).map { "$it kJ" },
                            correctIndex = 0,
                            explanation = "Q = m · c · Δt = $m × 4200 × $dt = ${qKJ * 1000} J = $qKJ kJ."
                        )
                    )
                }
                val decayCases = listOf(
                    Triple(400, 1, 200), Triple(800, 2, 200), Triple(1600, 3, 200),
                    Triple(600, 1, 300), Triple(1200, 2, 300), Triple(2400, 3, 300),
                    Triple(1000, 1, 500), Triple(2000, 2, 500), Triple(3200, 4, 200),
                    Triple(6400, 5, 200), Triple(4800, 4, 300), Triple(9600, 5, 300),
                    Triple(1280, 3, 160), Triple(2560, 4, 160), Triple(5120, 5, 160),
                    Triple(3600, 2, 900)
                )
                for ((n0, halfLives, remain) in decayCases) {
                    list.add(
                        RawQuestion(
                            question = "$tag Một mẫu phóng xạ ban đầu có N₀ = $n0 hạt nhân. Sau $halfLives chu kì bán rã, số hạt nhân còn lại chưa phân rã là:",
                            options = distinctNumbers(remain, listOf(n0 - remain, remain * 2, remain / 2)).map { "$it hạt" },
                            correctIndex = 0,
                            explanation = "N = N₀ · 2^(-t/T) = $n0 / 2^$halfLives = $remain hạt."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Phương trình trạng thái của một lượng khí lí tưởng xác định là:", listOf("p · V / T = hằng số", "p · T / V = hằng số", "V · T / p = hằng số", "p + V = T"), 0, "Phương trình Clapeyron–Mendeleev: pV = nRT."),
                        RawQuestion("$tag Đơn vị đo từ thông trong hệ SI là:", listOf("Vêbe (Wb)", "Tesla (T)", "Henri (H)", "Vôn (V)"), 0, "Từ thông Φ = B·S·cosα có đơn vị là Vêbe (Wb)."),
                        RawQuestion("$tag Theo định luật I Nhiệt động lực học (ΔU = A + Q), hệ nhận nhiệt lượng Q > 0 và nhận công A > 0 thì nội năng của hệ sẽ:", listOf("Tăng lên (ΔU > 0)", "Giảm xuống", "Không đổi", "Bằng 0"), 0, "Quy ước: nhận vào dương (+), truyền ra âm (-)."),
                        RawQuestion("$tag Trong quá trình đẳng nhiệt của một lượng khí lí tưởng nhất định, áp suất p và thể tích V liên hệ với nhau theo định luật:", listOf("Boyle (p tỉ lệ nghịch với V: p₁V₁ = p₂V₂)", "Charles (V tỉ lệ thuận T)", "Ohm", "Faraday"), 0, "Khi nhiệt độ T không đổi, nén khí giảm thể tích thì áp suất tăng."),
                        RawQuestion("$tag Độ lớn suất điện động cảm ứng xuất hiện trong mạch kín tỉ lệ thuận với:", listOf("Tốc độ biến thiên từ thông qua mạch (|ΔΦ / Δt|)", "Điện trở dây dẫn", "Chiều dài dây nối đất", "Nhiệt độ tuyệt đối"), 0, "Định luật Faraday: |e_c| = |ΔΦ / Δt|."),
                        RawQuestion("$tag Tia phóng xạ nào có bản chất là dòng hạt nhân nguyên tử Heli (⁴₂He) mang điện tích +2e?", listOf("Tia alpha (α)", "Tia beta trừ (β⁻)", "Tia gamma (γ)", "Tia X"), 0, "Tia α bị lệch về bản âm trong điện trường và có khả năng ion hóa mạnh."),
                        RawQuestion("$tag Sóng điện từ truyền trong chân không với tốc độ bằng bao nhiêu?", listOf("c ≈ 3 × 10⁸ m/s (tốc độ ánh sáng)", "340 m/s", "3 × 10⁵ m/s", "0 m/s"), 0, "Sóng điện từ là sóng ngang truyền được cả trong chân không."),
                        RawQuestion("$tag Hạt nhân bền vững nhất là những hạt nhân có đại lượng nào lớn nhất?", listOf("Năng lượng liên kết riêng (tính trên mỗi nuclôn)", "Số khối A lớn nhất", "Số nơtron bằng 0", "Chu kì bán rã ngắn nhất"), 0, "Các hạt nhân trung bình (50 < A < 80, như Sắt) có năng lượng liên kết riêng lớn nhất.")
                    )
                )
            }
        }
        return list
    }

    private fun primarySciencePhysics(g: Int): List<RawQuestion> {
        val tag = "[Khoa học / Vật lí Lớp $g]"
        val list = mutableListOf<RawQuestion>()
        for (h in 1..20) {
            val mins = h * 60
            list.add(
                RawQuestion(
                    question = "$tag Đổi đơn vị thời gian: $h giờ bằng bao nhiêu phút?",
                    options = distinctNumbers(mins, listOf(h * 100, mins + 30, mins - 15)).map { "$it phút" },
                    correctIndex = 0,
                    explanation = "1 giờ = 60 phút ⇒ $h giờ = $h × 60 = $mins phút."
                )
            )
        }
        for (m in 1..15) {
            val cm = m * 100
            list.add(
                RawQuestion(
                    question = "$tag Đổi đơn vị đo chiều dài: $m mét (m) bằng bao nhiêu xentimét (cm)?",
                    options = distinctNumbers(cm, listOf(m * 10, m * 1000, cm + 50)).map { "$it cm" },
                    correctIndex = 0,
                    explanation = "1 m = 100 cm ⇒ $m m = $cm cm."
                )
            )
        }
        list.addAll(
            listOf(
                RawQuestion("$tag Vật liệu nào sau đây dẫn điện tốt?", listOf("Đồng", "Nhựa", "Gỗ khô", "Thủy tinh"), 0, "Kim loại đồng dẫn điện tốt; nhựa, gỗ khô và thủy tinh cách điện."),
                RawQuestion("$tag Nước tồn tại ở những thể nào trong tự nhiên?", listOf("Rắn, lỏng, khí", "Chỉ thể lỏng", "Chỉ thể rắn và lỏng", "Chỉ thể khí"), 0, "Nước có 3 thể: rắn (băng/đá), lỏng (nước), khí (hơi nước)."),
                RawQuestion("$tag Để tiết kiệm điện và bảo vệ môi trường, chúng ta nên:", listOf("Tắt các thiết bị điện khi không sử dụng", "Bật đèn suốt cả ngày lẫn đêm", "Mở cửa tủ lạnh liên tục", "Cắm sạc điện thoại bỏ quên cả tuần"), 0, "Tắt thiết bị điện khi ra khỏi phòng giúp tiết kiệm năng lượng và an toàn."),
                RawQuestion("$tag Nguồn sáng tự nhiên lớn nhất chiếu sáng và sưởi ấm Trái Đất là:", listOf("Mặt Trời", "Mặt Trăng", "Đèn pin", "Ngọn nến"), 0, "Mặt Trăng không tự phát sáng mà chỉ phản chiếu ánh sáng Mặt Trời."),
                RawQuestion("$tag Khi đặt gần hai cực cùng tên (N-N hoặc S-S) của hai thanh nam châm, chúng sẽ:", listOf("Đẩy nhau", "Hút nhau mạnh", "Không có tác dụng gì", "Tan chảy"), 0, "Hai cực cùng tên đẩy nhau, hai cực khác tên hút nhau."),
                RawQuestion("$tag Bóng tối hình thành phía sau một vật cản sáng khi có yếu tố nào?", listOf("Ánh sáng truyền thẳng gặp vật không cho ánh sáng truyền qua", "Nhiệt độ xuống 0°C", "Có gió thổi mạnh", "Vật làm bằng thủy tinh trong suốt"), 0, "Ánh sáng truyền theo đường thẳng trong không khí đồng chất."),
                RawQuestion("$tag Năng lượng nào đang được dùng để chạy quạt điện và máy giặt trong gia đình?", listOf("Điện năng", "Năng lượng hạt nhân trực tiếp", "Năng lượng thủy triều", "Năng lượng địa nhiệt"), 0, "Các thiết bị điện gia dụng biến đổi điện năng thành cơ năng, nhiệt năng...")
            )
        )
        return list
    }
}
