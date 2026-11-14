package com.vinhho.onthi10ai.data.solver

import com.vinhho.onthi10ai.data.solver.OfflineQuizBank.RawQuestion

internal object OfflineQuizBankMath {

    fun buildForGrade(grade: Int): List<RawQuestion> {
        val list = mutableListOf<RawQuestion>()
        when (grade) {
            1 -> {
                for (a in 2..6) {
                    for (b in 2..5) {
                        val sum = a + b
                        list.add(
                            RawQuestion(
                                question = "[Toán Lớp 1] Phép tính $a + $b có kết quả bằng bao nhiêu?",
                                options = distinctNumbers(sum, listOf(sum + 1, sum - 1, sum + 2)),
                                correctIndex = 0,
                                explanation = "$a + $b = $sum."
                            )
                        )
                    }
                }
                for (a in 11..19) {
                    for (b in 2..3) {
                        val diff = a - b
                        list.add(
                            RawQuestion(
                                question = "[Toán Lớp 1] Kết quả của phép trừ $a - $b là:",
                                options = distinctNumbers(diff, listOf(diff + 1, diff + 2, a + b)),
                                correctIndex = 0,
                                explanation = "$a trừ $b bằng $diff."
                            )
                        )
                    }
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 1] Số liền sau của số 19 là:", listOf("20", "18", "21", "10"), 0, "Số liền sau của 19 là 19 + 1 = 20."),
                        RawQuestion("[Toán Lớp 1] Số lớn nhất có một chữ số là:", listOf("9", "10", "8", "1"), 0, "Các số có một chữ số là 0..9, lớn nhất là 9."),
                        RawQuestion("[Toán Lớp 1] Số bé nhất có hai chữ số là:", listOf("10", "11", "0", "99"), 0, "Số bé nhất có hai chữ số là 10."),
                        RawQuestion("[Toán Lớp 1] Hình nào có 3 cạnh?", listOf("Hình tam giác", "Hình vuông", "Hình tròn", "Hình chữ nhật"), 0, "Hình tam giác có 3 cạnh và 3 đỉnh."),
                        RawQuestion("[Toán Lớp 1] Trong các số 25, 18, 42, 31 số lớn nhất là:", listOf("42", "31", "25", "18"), 0, "So sánh hàng chục: 4 > 3 > 2 > 1 nên 42 lớn nhất.")
                    )
                )
            }
            2 -> {
                for (i in 1..15) {
                    val a = 15 + i * 3
                    val b = 12 + i * 2
                    val sum = a + b
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 2] Tính: $a + $b = ?",
                            options = distinctNumbers(sum, listOf(sum + 10, sum - 1, sum + 2)),
                            correctIndex = 0,
                            explanation = "Đặt tính rồi tính: $a + $b = $sum."
                        )
                    )
                }
                for (mult in listOf(2, 5)) {
                    for (k in 2..9) {
                        val prod = mult * k
                        list.add(
                            RawQuestion(
                                question = "[Toán Lớp 2] Theo bảng nhân $mult, tích $mult × $k bằng:",
                                options = distinctNumbers(prod, listOf(prod + mult, prod - 1, mult + k)),
                                correctIndex = 0,
                                explanation = "$mult × $k = $prod."
                            )
                        )
                    }
                }
                for (i in 1..10) {
                    val a = 10 + i * 2
                    val b = 25 + i * 3
                    val x = b - a
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 2] Tìm x biết: x + $a = $b",
                            options = distinctNumbers(x, listOf(a + b, x + 2, x - 1)).map { "x = $it" },
                            correctIndex = 0,
                            explanation = "Muốn tìm số hạng chưa biết: x = $b - $a = $x."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 2] 1 dm bằng bao nhiêu cm?", listOf("10 cm", "100 cm", "1 cm", "20 cm"), 0, "1 dm = 10 cm."),
                        RawQuestion("[Toán Lớp 2] 1 ngày có bao nhiêu giờ?", listOf("24 giờ", "12 giờ", "60 giờ", "30 giờ"), 0, "Một ngày đêm có 24 giờ.")
                    )
                )
            }
            3 -> {
                for (a in 6..8) {
                    for (b in 4..8) {
                        val prod = a * b
                        list.add(
                            RawQuestion(
                                question = "[Toán Lớp 3] Tính nhẩm: $a × $b = ?",
                                options = distinctNumbers(prod, listOf(prod + a, prod - 2, a + b)),
                                correctIndex = 0,
                                explanation = "Theo bảng cửu chương: $a × $b = $prod."
                            )
                        )
                    }
                }
                for (i in 1..15) {
                    val a = 14 + i * 4
                    val b = 3 + (i % 6)
                    val c = 2 + (i % 4)
                    val valExpr = a + b * c
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 3] Giá trị của biểu thức $a + $b × $c là:",
                            options = distinctNumbers(valExpr, listOf((a + b) * c, valExpr + 5, valExpr - 3)),
                            correctIndex = 0,
                            explanation = "Nhân chia trước, cộng trừ sau: $b × $c = ${b * c}, rồi $a + ${b * c} = $valExpr."
                        )
                    )
                }
                for (i in 1..10) {
                    val l = 8 + i * 2
                    val w = 4 + i
                    val peri = (l + w) * 2
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 3] Chu vi hình chữ nhật có chiều dài ${l}cm và chiều rộng ${w}cm là:",
                            options = distinctNumbers(peri, listOf(l + w, l * w, peri + 4)).map { "$it cm" },
                            correctIndex = 0,
                            explanation = "Chu vi = (dài + rộng) × 2 = ($l + $w) × 2 = $peri cm."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 3] Phép chia 29 : 4 có số dư là:", listOf("1", "2", "3", "0"), 0, "29 : 4 = 7 dư 1 (vì 4 × 7 = 28, 29 - 28 = 1)."),
                        RawQuestion("[Toán Lớp 3] Hình vuông có cạnh 9 cm thì chu vi bằng:", listOf("36 cm", "81 cm", "18 cm", "27 cm"), 0, "Chu vi hình vuông = cạnh × 4 = 9 × 4 = 36 cm."),
                        RawQuestion("[Toán Lớp 3] 1 giờ 15 phút bằng bao nhiêu phút?", listOf("75 phút", "115 phút", "65 phút", "85 phút"), 0, "60 + 15 = 75 phút.")
                    )
                )
            }
            4 -> {
                for (i in 1..15) {
                    val x = 10 + i * 3
                    val avg = x + 4
                    val a = x
                    val b = x + 4
                    val c = x + 8
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 4] Trung bình cộng của ba số $a, $b và $c là:",
                            options = distinctNumbers(avg, listOf(avg + 2, avg - 2, a + b + c)),
                            correctIndex = 0,
                            explanation = "Trung bình cộng = ($a + $b + $c) : 3 = ${(a + b + c)} : 3 = $avg."
                        )
                    )
                }
                for (i in 1..15) {
                    val base = 6 + i * 2
                    val h = 4 + i
                    val area = base * h
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 4] Hình bình hành có độ dài đáy ${base}cm và chiều cao ${h}cm có diện tích là:",
                            options = distinctNumbers(area, listOf(area / 2, (base + h) * 2, area + base)).map { "$it cm²" },
                            correctIndex = 0,
                            explanation = "Diện tích hình bình hành S = đáy × chiều cao = $base × $h = $area cm²."
                        )
                    )
                }
                for (i in 1..10) {
                    val num1 = i
                    val num2 = i + 2
                    val den = 25
                    val sumNum = num1 + num2
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 4] Kết quả của phép cộng hai phân số $num1/$den + $num2/$den là:",
                            options = listOf("$sumNum/$den", "${num1 * num2}/$den", "$sumNum/${den * 2}", "${num2 - num1}/$den"),
                            correctIndex = 0,
                            explanation = "Cộng hai phân số cùng mẫu: giữ nguyên mẫu $den và cộng tử số $num1 + $num2 = $sumNum."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 4] Phân số 12/18 rút gọn thành phân số tối giản là:", listOf("2/3", "3/4", "4/6", "6/9"), 0, "Chia cả tử và mẫu cho 6: 12/18 = 2/3."),
                        RawQuestion("[Toán Lớp 4] Hình thoi có độ dài hai đường chéo là 8 cm và 6 cm có diện tích là:", listOf("24 cm²", "48 cm²", "14 cm²", "28 cm²"), 0, "S = (8 × 6) : 2 = 24 cm²."),
                        RawQuestion("[Toán Lớp 4] 2 tấn 50 kg bằng bao nhiêu kg?", listOf("2050 kg", "250 kg", "2005 kg", "2500 kg"), 0, "2 tấn = 2000 kg; 2000 + 50 = 2050 kg.")
                    )
                )
            }
            5 -> {
                for (i in 1..15) {
                    val v = 35 + i * 5
                    val t = 2 + (i % 3)
                    val s = v * t
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 5] Một ô tô đi với vận tốc $v km/giờ trong $t giờ. Quãng đường đi được là:",
                            options = distinctNumbers(s, listOf(s + v, v + t, s - 15)).map { "$it km" },
                            correctIndex = 0,
                            explanation = "Quãng đường s = v × t = $v × $t = $s km."
                        )
                    )
                }
                for (i in 1..15) {
                    val base = 6 + i * 2
                    val h = 4 + (i % 5) * 2
                    val area = (base * h) / 2
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 5] Diện tích hình tam giác có đáy ${base}cm và chiều cao ${h}cm là:",
                            options = distinctNumbers(area, listOf(base * h, area + 6, area - 2)).map { "$it cm²" },
                            correctIndex = 0,
                            explanation = "Diện tích tam giác S = (đáy × chiều cao) : 2 = ($base × $h) : 2 = $area cm²."
                        )
                    )
                }
                for (i in 1..10) {
                    val pct = 10 + i * 5
                    val total = 200
                    val part = total * pct / 100
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 5] Tìm $pct% của $total:",
                            options = distinctNumbers(part, listOf(part + 10, part - 5, pct * 2)),
                            correctIndex = 0,
                            explanation = "$pct% của $total là $total × $pct : 100 = $part."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 5] Thể tích hình lập phương có cạnh 4cm là:", listOf("64 cm³", "16 cm³", "96 cm³", "48 cm³"), 0, "V = a × a × a = 4 × 4 × 4 = 64 cm³."),
                        RawQuestion("[Toán Lớp 5] Diện tích hình tròn có bán kính r = 10 cm (π ≈ 3,14) là:", listOf("314 cm²", "62,8 cm²", "31,4 cm²", "628 cm²"), 0, "S = r × r × 3,14 = 10 × 10 × 3,14 = 314 cm²."),
                        RawQuestion("[Toán Lớp 5] Hỗn số 3 2/5 viết dưới dạng phân số là:", listOf("17/5", "13/5", "11/5", "15/5"), 0, "(3 × 5 + 2) / 5 = 17/5.")
                    )
                )
            }
            6 -> {
                val powerCases = listOf(
                    2 to 2, 2 to 3, 2 to 4, 2 to 5, 2 to 6,
                    3 to 2, 3 to 3, 3 to 4,
                    4 to 2, 4 to 3,
                    5 to 2, 5 to 3,
                    6 to 2, 7 to 2, 8 to 2
                )
                for ((a, b) in powerCases) {
                    var pow = 1
                    repeat(b) { pow *= a }
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 6] Giá trị của lũy thừa $a^$b bằng:",
                            options = distinctNumbers(pow, listOf(a * b, pow + a, pow - 1)),
                            correctIndex = 0,
                            explanation = "$a^$b là tích của $b thừa số $a: kết quả bằng $pow."
                        )
                    )
                }
                for (i in 1..15) {
                    val neg = -(5 + i * 2)
                    val pos = 8 + i * 3
                    val sum = neg + pos
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 6] Tổng hai số nguyên ($neg) + $pos bằng:",
                            options = distinctNumbers(sum, listOf(-sum, pos - neg, sum + 2)),
                            correctIndex = 0,
                            explanation = "Cộng hai số nguyên khác dấu: $pos - ${-neg} = $sum."
                        )
                    )
                }
                for (i in 1..10) {
                    val g = 2 + i
                    val x = g * 2
                    val y = g * 3
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 6] Ước chung lớn nhất ƯCLN($x, $y) bằng:",
                            options = distinctNumbers(g, listOf(g * 6, g * 2, 1)),
                            correctIndex = 0,
                            explanation = "$x = 2 × $g và $y = 3 × $g; 2 và 3 nguyên tố cùng nhau nên ƯCLN($x, $y) = $g."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 6] Số nào sau đây là số nguyên tố?", listOf("17", "15", "21", "9"), 0, "17 chỉ có đúng 2 ước tự nhiên là 1 và 17 nên là số nguyên tố."),
                        RawQuestion("[Toán Lớp 6] Góc bẹt có số đo bằng:", listOf("180°", "90°", "60°", "360°"), 0, "Góc bẹt có hai tia đối nhau và số đo bằng 180°."),
                        RawQuestion("[Toán Lớp 6] Bội chung nhỏ nhất BCNN(6, 8) là:", listOf("24", "48", "12", "2"), 0, "6 = 2·3, 8 = 2³ ⇒ BCNN = 2³·3 = 24.")
                    )
                )
            }
            7 -> {
                for (i in 1..15) {
                    val a = 40 + i * 2
                    val b = 50 + i
                    val c = 180 - a - b
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 7] Tam giác ABC có góc A = ${a}°, góc B = ${b}°. Số đo góc C bằng:",
                            options = distinctNumbers(c, listOf(a + b, 90 - a, c + 10)).map { "$it°" },
                            correctIndex = 0,
                            explanation = "Tổng ba góc trong tam giác bằng 180° ⇒ ∠C = 180° - ($a° + $b°) = $c°."
                        )
                    )
                }
                for (i in 1..15) {
                    val k = i + 1
                    val x = 2 * k
                    val y = 3 * k
                    val sum = x + y
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 7] Biết x/2 = y/3 và x + y = $sum. Giá trị của x là:",
                            options = distinctNumbers(x, listOf(y, k, x + 2)),
                            correctIndex = 0,
                            explanation = "Theo dãy tỉ số bằng nhau: x/2 = y/3 = (x+y)/(2+3) = $sum/5 = $k ⇒ x = 2·$k = $x."
                        )
                    )
                }
                for (i in 1..10) {
                    val xVal = i + 1
                    val polyVal = 2 * xVal * xVal - 3 * xVal + 1
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 7] Giá trị của đa thức P(x) = 2x² - 3x + 1 tại x = $xVal là:",
                            options = distinctNumbers(polyVal, listOf(polyVal + 3, polyVal - 2, 2 * xVal + 1)),
                            correctIndex = 0,
                            explanation = "P($xVal) = 2·($xVal)² - 3·$xVal + 1 = $polyVal."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 7] Hai đường thẳng song song bị một đường thẳng thứ ba cắt thì cặp góc so le trong:", listOf("Bằng nhau", "Bù nhau (tổng 180°)", "Phụ nhau (tổng 90°)", "Bằng 45°"), 0, "Nếu một đường thẳng cắt hai đường thẳng song song thì hai góc so le trong bằng nhau."),
                        RawQuestion("[Toán Lớp 7] Số thực nào sau đây là số vô tỉ?", listOf("√2", "3/4", "-5", "0,25"), 0, "√2 là số thập phân vô hạn không tuần hoàn."),
                        RawQuestion("[Toán Lớp 7] Trong một tam giác cân, góc ở đỉnh bằng 80° thì mỗi góc ở đáy bằng:", listOf("50°", "80°", "100°", "40°"), 0, "(180° - 80°) / 2 = 50°.")
                    )
                )
            }
            8 -> {
                for (i in 1..15) {
                    val a = i + 1
                    val expanded = "x² + ${2 * a}x + ${a * a}"
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 8] Khai triển hằng đẳng thức (x + $a)² ta được:",
                            options = listOf(expanded, "x² + ${a}x + ${a * a}", "x² + ${a * a}", "x² - ${2 * a}x + ${a * a}"),
                            correctIndex = 0,
                            explanation = "Áp dụng (A + B)² = A² + 2AB + B² với A = x, B = $a."
                        )
                    )
                }
                for (i in 1..15) {
                    val a = i + 1
                    val x = i + 2
                    val b = i * 3
                    val rhs = a * x + b
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 8] Nghiệm của phương trình bậc nhất ${a}x + $b = $rhs là:",
                            options = distinctNumbers(x, listOf(x + 1, x - 1, x + 2)).map { "x = $it" },
                            correctIndex = 0,
                            explanation = "${a}x = $rhs - $b = ${rhs - b} ⇒ x = $x."
                        )
                    )
                }
                for (i in 1..10) {
                    val x = i + 2
                    val y = 3 * x + 1
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 8] Với hàm số bậc nhất y = 3x + 1, giá trị f($x) bằng:",
                            options = distinctNumbers(y, listOf(y + 3, y - 1, 3 + x)),
                            correctIndex = 0,
                            explanation = "Thay x = $x vào y = 3x + 1 ta được 3·$x + 1 = $y."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 8] Hình chữ nhật có hai đường chéo vuông góc với nhau là hình gì?", listOf("Hình vuông", "Hình thang cân", "Hình bình hành", "Hình thang vuông"), 0, "Hình chữ nhật có hai đường chéo vuông góc là hình vuông."),
                        RawQuestion("[Toán Lớp 8] Theo định lí Py-ta-go, tam giác vuông có hai cạnh góc vuông 3cm và 4cm thì cạnh huyền bằng:", listOf("5 cm", "7 cm", "6 cm", "12 cm"), 0, "√(3² + 4²) = √25 = 5 cm."),
                        RawQuestion("[Toán Lớp 8] Phân tích đa thức x² - 9 thành nhân tử ta được:", listOf("(x - 3)(x + 3)", "(x - 9)(x + 9)", "(x - 3)²", "x(x - 9)"), 0, "Áp dụng hiệu hai bình phương A² - B² = (A - B)(A + B).")
                    )
                )
            }
            9 -> {
                val rootPairs = listOf(
                    1 to 2, 1 to 3, 2 to 3, 2 to 4, 3 to 4, 1 to 5, 2 to 5, 3 to 5,
                    4 to 5, 1 to 6, 2 to 6, 3 to 6, -1 to 3, -2 to 4, -2 to 5
                )
                for ((r1, r2) in rootPairs) {
                    val sum = r1 + r2
                    val prod = r1 * r2
                    val bPart = when {
                        sum == 0 -> ""
                        sum > 0 -> " - ${sum}x"
                        else -> " + ${-sum}x"
                    }
                    val cPart = when {
                        prod == 0 -> ""
                        prod > 0 -> " + $prod"
                        else -> " - ${-prod}"
                    }
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 9] Phương trình x²$bPart$cPart = 0 có tổng hai nghiệm x₁ + x₂ bằng:",
                            options = distinctNumbers(sum, listOf(-sum, prod, -prod)),
                            correctIndex = 0,
                            explanation = "Theo hệ thức Vi-ét: x₁ + x₂ = -b/a = $sum."
                        )
                    )
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 9] Phương trình x²$bPart$cPart = 0 có tích hai nghiệm x₁·x₂ bằng:",
                            options = distinctNumbers(prod, listOf(-prod, sum, sum + 1)),
                            correctIndex = 0,
                            explanation = "Theo hệ thức Vi-ét: x₁·x₂ = c/a = $prod."
                        )
                    )
                }
                for (k in 1..10) {
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 9] Điều kiện xác định của căn thức √(x - $k) là:",
                            options = listOf("x ≥ $k", "x > $k", "x ≤ $k", "x ≠ $k"),
                            correctIndex = 0,
                            explanation = "√A xác định khi A ≥ 0 ⇔ x - $k ≥ 0 ⇔ x ≥ $k."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 9] Trong một đường tròn, số đo góc nội tiếp bằng:", listOf("Nửa số đo cung bị chắn", "Số đo cung bị chắn", "Hai lần số đo cung bị chắn", "90°"), 0, "Số đo góc nội tiếp bằng nửa số đo cung bị chắn."),
                        RawQuestion("[Toán Lớp 9] Hệ phương trình { x + y = 7; x - y = 1 } có nghiệm (x; y) là:", listOf("(4; 3)", "(3; 4)", "(5; 2)", "(2; 5)"), 0, "Cộng hai phương trình: 2x = 8 ⇒ x = 4, suy ra y = 3."),
                        RawQuestion("[Toán Lớp 9] Trong tam giác vuông, sin của một góc nhọn bằng tỉ số nào?", listOf("Cạnh đối / Cạnh huyền", "Cạnh kề / Cạnh huyền", "Cạnh đối / Cạnh kề", "Cạnh kề / Cạnh đối"), 0, "Tìm sin lấy đối chia huyền; côsin lấy kề chia huyền.")
                    )
                )
            }
            10 -> {
                for (i in 1..15) {
                    val x = i + 1
                    val y = 2 * i + 1
                    val lenSq = x * x + y * y
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 10] Trong mặt phẳng Oxy, vectơ u = ($x; $y) có bình phương độ dài |u|² bằng:",
                            options = distinctNumbers(lenSq, listOf(x + y, x * y, lenSq + 2)),
                            correctIndex = 0,
                            explanation = "|u|² = $x² + $y² = $lenSq."
                        )
                    )
                }
                for (i in 1..15) {
                    val h = i + 1
                    val k = i * 2 - 3
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 10] Parabol y = x² - ${2 * h}x + ${h * h + k} có đỉnh I có hoành độ là:",
                            options = distinctNumbers(h, listOf(-h, 2 * h, k)),
                            correctIndex = 0,
                            explanation = "Hoành độ đỉnh parabol x_I = -b / (2a) = ${2 * h} / 2 = $h."
                        )
                    )
                }
                for (i in 1..10) {
                    val r = i + 1
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 10] Đường tròn (C): (x - 1)² + (y + 2)² = ${r * r} có bán kính R bằng:",
                            options = distinctNumbers(r, listOf(r * r, r + 1, 2 * r)),
                            correctIndex = 0,
                            explanation = "Phương trình (x - a)² + (y - b)² = R² ⇒ R = √(${r * r}) = $r."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 10] Cho tập A = {1, 2, 3, 4} và B = {3, 4, 5}. Tập hợp A ∩ B là:", listOf("{3, 4}", "{1, 2, 3, 4, 5}", "{1, 2}", "{5}"), 0, "Giao của A và B gồm các phần tử thuộc cả hai tập: {3, 4}."),
                        RawQuestion("[Toán Lớp 10] Đường thẳng Δ: 2x - 3y + 6 = 0 có một vectơ pháp tuyến là:", listOf("(2; -3)", "(3; 2)", "(2; 3)", "(-3; 6)"), 0, "Phương trình tổng quát ax + by + c = 0 có VTPT n = (a; b) = (2; -3)."),
                        RawQuestion("[Toán Lớp 10] Số các hoán vị của 4 phần tử phân biệt (P₄) bằng:", listOf("24", "16", "12", "4"), 0, "P₄ = 4! = 4 × 3 × 2 × 1 = 24.")
                    )
                )
            }
            11 -> {
                for (i in 1..15) {
                    val u1 = i + 1
                    val d = i + 2
                    val un = u1 + 3 * d
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 11] Cấp số cộng có u₁ = $u1 và công sai d = $d. Số hạng u₄ bằng:",
                            options = distinctNumbers(un, listOf(un + d, u1 + 4 * d, un - 1)),
                            correctIndex = 0,
                            explanation = "u₄ = u₁ + 3d = $u1 + 3·$d = $un."
                        )
                    )
                }
                for (i in 1..15) {
                    val a = i + 1
                    val n = (i % 4) + 2
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 11] Đạo hàm của hàm số y = ${a}x^${n + 1} là:",
                            options = listOf("${a * (n + 1)}x^$n", "${a * n}x^$n", "${a * (n + 1)}x^${n + 1}", "${a}x^$n"),
                            correctIndex = 0,
                            explanation = "Áp dụng (a·x^k)' = a·k·x^(k-1) ta được ${a * (n + 1)}x^$n."
                        )
                    )
                }
                for (i in 1..10) {
                    val a = i + 2
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 11] Giới hạn lim(n→+∞) (${a}n + 5) / (n + 1) bằng:",
                            options = distinctNumbers(a, listOf(5, 0, a + 5)),
                            correctIndex = 0,
                            explanation = "Chia tử và mẫu cho n được (${a} + 5/n)/(1 + 1/n) → $a."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 11] Tập giá trị của hàm số y = sin(x) là:", listOf("[-1; 1]", "ℝ", "[0; 1]", "(-1; 1)"), 0, "Với mọi x ∈ ℝ, -1 ≤ sin(x) ≤ 1."),
                        RawQuestion("[Toán Lớp 11] Công thức nghiệm của phương trình sin(x) = 0 là:", listOf("x = kπ (k ∈ ℤ)", "x = π/2 + kπ (k ∈ ℤ)", "x = k2π (k ∈ ℤ)", "x = π/4 + kπ (k ∈ ℤ)"), 0, "sin(x) = 0 ⇔ x = kπ."),
                        RawQuestion("[Toán Lớp 11] Đạo hàm của hàm số y = ln(x) (với x > 0) là:", listOf("1 / x", "e^x", "x", "-1 / x²"), 0, "(ln x)' = 1/x.")
                    )
                )
            }
            else -> {
                for (n in 2..15) {
                    val exp = n + 1
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 12] Đạo hàm của hàm số y = x^$exp - ${exp}x là:",
                            options = listOf("${exp}x^$n - $exp", "${n}x^$n - $exp", "${exp}x^${exp + 1}", "x^$n - 1"),
                            correctIndex = 0,
                            explanation = "y' = ${exp}x^$n - $exp."
                        )
                    )
                }
                for (a in 2..12) {
                    val logVal = a * a
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 12] Giá trị của log_$a($logVal) + 1 bằng:",
                            options = distinctNumbers(3, listOf(2, 4, a)),
                            correctIndex = 0,
                            explanation = "log_$a($a²) = 2 ⇒ 2 + 1 = 3."
                        )
                    )
                }
                for (k in 2..12) {
                    val integralVal = k * k
                    list.add(
                        RawQuestion(
                            question = "[Toán Lớp 12] Tích phân ∫(từ 0 đến $k) 2x dx bằng:",
                            options = distinctNumbers(integralVal, listOf(k * 2, integralVal + k, integralVal - 1)),
                            correctIndex = 0,
                            explanation = "Nguyên hàm của 2x là x². Thế cận từ 0 đến $k: $k² - 0 = $integralVal."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("[Toán Lớp 12] Tiệm cận đứng của đồ thị hàm số y = (2x + 1)/(x - 3) là đường thẳng:", listOf("x = 3", "y = 2", "x = -3", "y = 3"), 0, "Mẫu bằng 0 tại x = 3 (tử khác 0) nên tiệm cận đứng là x = 3."),
                        RawQuestion("[Toán Lớp 12] Tiệm cận ngang của đồ thị hàm số y = (4x - 1)/(2x + 5) là đường thẳng:", listOf("y = 2", "x = -5/2", "y = 4", "y = -1/5"), 0, "lim(x→±∞) y = 4/2 = 2 nên TCN là y = 2."),
                        RawQuestion("[Toán Lớp 12] Trong không gian Oxyz, mặt phẳng (P): 2x - y + 3z - 5 = 0 có một vectơ pháp tuyến là:", listOf("(2; -1; 3)", "(2; 1; 3)", "(-1; 3; -5)", "(2; -1; -5)"), 0, "Hệ số của x, y, z cho vectơ pháp tuyến n = (2; -1; 3)."),
                        RawQuestion("[Toán Lớp 12] Họ nguyên hàm của hàm số f(x) = e^(2x) là:", listOf("½·e^(2x) + C", "2·e^(2x) + C", "e^(2x) + C", "e^x + C"), 0, "∫ e^(ax) dx = (1/a)·e^(ax) + C."),
                        RawQuestion("[Toán Lớp 12] Thể tích khối cầu có bán kính R = 3 là:", listOf("36π", "12π", "27π", "108π"), 0, "V = (4/3)·π·R³ = (4/3)·π·27 = 36π."),
                        RawQuestion("[Toán Lớp 12] Mặt cầu (S): (x - 2)² + (y + 1)² + (z - 4)² = 25 có tâm I và bán kính R là:", listOf("I(2; -1; 4), R = 5", "I(-2; 1; -4), R = 5", "I(2; -1; 4), R = 25", "I(2; 1; 4), R = 5"), 0, "(x - a)² + (y - b)² + (z - c)² = R² ⇒ I(2; -1; 4), R = √25 = 5.")
                    )
                )
            }
        }
        return list
    }
}
