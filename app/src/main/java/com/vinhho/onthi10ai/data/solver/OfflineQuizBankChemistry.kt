package com.vinhho.onthi10ai.data.solver

import com.vinhho.onthi10ai.data.solver.OfflineQuizBank.RawQuestion
import java.util.Locale

internal object OfflineQuizBankChemistry {

    fun buildForGrade(grade: Int): List<RawQuestion> {
        val list = mutableListOf<RawQuestion>()
        when (grade) {
            in 1..5 -> list.addAll(primaryScienceChemistry(grade))
            6 -> {
                val tag = "[KHTN / Hóa học Lớp 6]"
                list.addAll(
                    listOf(
                        RawQuestion("$tag Trong không khí sạch, khí oxygen (O₂) chiếm khoảng bao nhiêu phần trăm thể tích?", listOf("21%", "78%", "1%", "50%"), 0, "Không khí sạch gồm ~78% nitrogen, ~21% oxygen và ~1% khí khác."),
                        RawQuestion("$tag Khí nào chiếm tỉ lệ thể tích lớn nhất (~78%) trong không khí?", listOf("Nitrogen (N₂)", "Oxygen (O₂)", "Carbon dioxide (CO₂)", "Hơi nước"), 0, "Khí nitrogen trơ về mặt hóa học ở điều kiện thường giúp điều hòa sự cháy."),
                        RawQuestion("$tag Quá trình chất chuyển từ thể lỏng sang thể hơi gọi là:", listOf("Sự bay hơi", "Sự nóng chảy", "Sự đông đặc", "Sự ngưng tụ"), 0, "Lỏng → hơi là sự bay hơi; hơi → lỏng là sự ngưng tụ."),
                        RawQuestion("$tag Quá trình chất chuyển từ thể rắn sang thể lỏng khi đun nóng gọi là:", listOf("Sự nóng chảy", "Sự đông đặc", "Sự bay hơi", "Sự thăng hoa"), 0, "Ví dụ nước đá tan thành nước lỏng là sự nóng chảy."),
                        RawQuestion("$tag Hỗn hợp đồng nhất của chất tan và dung môi được gọi là:", listOf("Dung dịch", "Huyền phù", "Nhũ tương", "Chất tinh khiết"), 0, "Dung dịch đồng nhất không tách lớp như nước muối loãng, nước đường."),
                        RawQuestion("$tag Hỗn hợp gồm các hạt chất rắn lơ lửng trong chất lỏng (như nước phù sa đục) gọi là:", listOf("Huyền phù", "Dung dịch", "Chất tinh khiết", "Hợp kim"), 0, "Để yên lâu, các hạt rắn trong huyền phù sẽ lắng xuống đáy."),
                        RawQuestion("$tag Hỗn hợp gồm hai chất lỏng không tan vào nhau phân tán thành giọt nhỏ (như dầu ăn lắc với nước) gọi là:", listOf("Nhũ tương", "Dung dịch thật", "Đơn chất", "Khoáng vật"), 0, "Sữa tươi và sốt mayonnaise là các nhũ tương quen thuộc."),
                        RawQuestion("$tag Phương pháp nào phù hợp nhất để tách cát lẫn trong nước?", listOf("Lọc (qua giấy lọc hoặc phễu lọc)", "Chưng cất phân đoạn", "Dùng nam châm", "Chiết bằng phễu chiết"), 0, "Cát không tan bị giữ lại trên giấy lọc, nước đi qua."),
                        RawQuestion("$tag Để tách muối ăn hòa tan ra khỏi nước biển, người dân thường dùng phương pháp nào?", listOf("Cô cạn / làm bay hơi nước", "Dùng phễu lọc cát", "Dùng nam châm hút muối", "Để lắng tự nhiên"), 0, "Nước bay hơi hết để lại tinh thể muối rắn."),
                        RawQuestion("$tag Để tách dầu ăn nổi trên mặt nước mà không cần đun nóng, ta dùng phương pháp nào?", listOf("Chiết bằng phễu chiết", "Lọc bằng giấy lọc", "Cô cạn", "Sàng bằng rây"), 0, "Dầu ăn nhẹ hơn và không tan trong nước nên tách thành lớp trên."),
                        RawQuestion("$tag Để tách bột sắt lẫn trong bột lưu huỳnh nhanh nhất, ta dùng dụng cụ nào?", listOf("Nam châm", "Giấy lọc", "Bình cầu chưng cất", "Nhiệt kế"), 0, "Sắt có tính nhiễm từ bị nam châm hút ra khỏi lưu huỳnh."),
                        RawQuestion("$tag Chất nào sau đây là chất tinh khiết?", listOf("Nước cất", "Nước biển", "Nước cam ép", "Không khí"), 0, "Nước cất chỉ chứa duy nhất một chất là H₂O."),
                        RawQuestion("$tag Nguyên liệu đá vôi (thành phần chính CaCO₃) thường dùng để sản xuất vật liệu nào?", listOf("Xi măng và vôi sống", "Xăng dầu", "Thủy tinh hữu cơ", "Cao su tổng hợp"), 0, "Nung đá vôi thu được vôi sống (CaO) và khí CO₂."),
                        RawQuestion("$tag Dấu hiệu nào chứng tỏ xăng dầu là nhiên liệu dễ gây hỏa hoạn?", listOf("Dễ bay hơi và bắt cháy nhanh khi gặp tia lửa", "Không bao giờ cháy", "Chìm hoàn toàn dưới nước", "Cứng như sắt"), 0, "Cần bảo quản xăng dầu xa nguồn nhiệt và lửa."),
                        RawQuestion("$tag Lương thực nào sau đây chứa hàm lượng tinh bột cao nhất?", listOf("Gạo, ngô, khoai, sắn", "Muối ăn", "Nước khoáng", "Dầu thực vật"), 0, "Tinh bột cung cấp năng lượng chủ yếu cho hoạt động của con người."),
                        RawQuestion("$tag Khí nào là nguyên nhân chính gây hiệu ứng nhà kính làm Trái Đất nóng lên?", listOf("Carbon dioxide (CO₂)", "Oxygen (O₂)", "Nitrogen (N₂)", "Heli (He)"), 0, "Đốt nhiều nhiên liệu hóa thạch làm tăng nồng độ CO₂ khí quyển."),
                        RawQuestion("$tag Loại vật liệu nào bền nhẹ, dễ tạo hình nhưng rất khó phân hủy gây ô nhiễm trắng?", listOf("Nhựa (chất dẻo tổng hợp)", "Gỗ tự nhiên", "Giấy báo cũ", "Lá chuối tươi"), 0, "Cần hạn chế túi ni lông dùng một lần và tăng cường tái chế nhựa."),
                        RawQuestion("$tag Hiện tượng nào sau đây là biến đổi vật lí (không tạo ra chất mới)?", listOf("Thanh sắt được dát mỏng thành lá sắt", "Thanh sắt để lâu ngoài mưa bị gỉ nâu", "Đốt cháy tờ giấy thành tro đen", "Thức ăn bị ôi thiu"), 0, "Dát mỏng chỉ thay đổi hình dạng, bản chất vẫn là sắt."),
                        RawQuestion("$tag Hiện tượng nào sau đây là biến đổi hóa học (có tạo ra chất mới)?", listOf("Đốt cháy nến tạo ra khí CO₂ và hơi nước", "Nước đá tan thành nước lỏng", "Hòa tan đường vào cốc nước ấm", "Xé nhỏ tờ giấy"), 0, "Quá trình cháy biến đổi sáp nến thành các chất mới."),
                        RawQuestion("$tag Bình chữa cháy bằng khí CO₂ dập tắt đám cháy bằng cơ chế nào?", listOf("Ngăn cách chất cháy với khí oxygen và làm lạnh vùng cháy", "Cung cấp thêm oxygen cho lửa", "Làm nóng đám cháy", "Thổi bay chất lỏng ra xa"), 0, "CO₂ nặng hơn không khí và không duy trì sự cháy."),
                        RawQuestion("$tag Để hòa tan đường vào nước nhanh hơn, ta KHÔNG nên làm cách nào?", listOf("Cho thêm đá lạnh vào cốc nước", "Khuấy đều bằng thìa", "Dùng nước ấm", "Nghiền nhỏ đường phèn trước khi pha"), 0, "Nhiệt độ cao và khuấy trộn giúp chất rắn tan nhanh hơn.")
                    )
                )
                // Add 21 unique percentage / density / concentration grade 6 questions
                for (i in 1..21) {
                    val salt = i * 5
                    val water = 100 - salt
                    list.add(
                        RawQuestion(
                            question = "$tag Hòa tan hoàn toàn $salt g muối ăn vào $water g nước cất thu được dung dịch nước muối có tổng khối lượng là:",
                            options = distinctNumbers(100, listOf(water, salt, 100 + salt)).map { "$it g" },
                            correctIndex = 0,
                            explanation = "Khối lượng dung dịch = m_chất tan + m_dung môi = $salt + $water = 100 g."
                        )
                    )
                }
            }
            7 -> {
                val tag = "[KHTN / Hóa học Lớp 7]"
                val massNumbers = listOf(
                    "Hydrogen (H)" to 1, "Helium (He)" to 4, "Lithium (Li)" to 7, "Carbon (C)" to 12,
                    "Nitrogen (N)" to 14, "Oxygen (O)" to 16, "Fluorine (F)" to 19, "Neon (Ne)" to 20,
                    "Sodium (Na)" to 23, "Magnesium (Mg)" to 24, "Aluminium (Al)" to 27, "Silicon (Si)" to 28,
                    "Phosphorus (P)" to 31, "Sulfur (S)" to 32, "Chlorine (Cl)" to 35, "Potassium (K)" to 39,
                    "Calcium (Ca)" to 40, "Iron (Fe)" to 56, "Copper (Cu)" to 64, "Zinc (Zn)" to 65,
                    "Bromine (Br)" to 80, "Silver (Ag)" to 108, "Barium (Ba)" to 137
                )
                massNumbers.forEach { (elem, mass) ->
                    list.add(
                        RawQuestion(
                            question = "$tag Khối lượng nguyên tử xấp xỉ của nguyên tố $elem tính theo amu là:",
                            options = distinctNumbers(mass, listOf(mass + 2, (mass - 2).coerceAtLeast(2), mass + 8)).map { "$it amu" },
                            correctIndex = 0,
                            explanation = "Nguyên tử khối của $elem là $mass amu."
                        )
                    )
                }
                for (z in 3..14) {
                    list.add(
                        RawQuestion(
                            question = "$tag Một nguyên tử trung hòa về điện có hạt nhân chứa $z proton. Số electron ở lớp vỏ của nguyên tử này là:",
                            options = distinctNumbers(z, listOf(z + 1, z * 2, z + 2)).map { "$it electron" },
                            correctIndex = 0,
                            explanation = "Vì nguyên tử trung hòa điện nên Số p = Số e = $z."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Trong hạt nhân nguyên tử, hạt mang điện tích dương là:", listOf("Proton", "Electron", "Neutron", "Phân tử"), 0, "Hạt nhân gồm proton (+) và neutron (không mang điện); vỏ gồm electron (-)."),
                        RawQuestion("$tag Nguyên tố Fe (hóa trị III) kết hợp với O (hóa trị II) tạo hợp chất oxit có công thức hóa học là:", listOf("Fe₂O₃", "FeO", "Fe₃O₂", "Fe₂O"), 0, "Quy tắc hóa trị: 2 × III = 3 × II → Fe₂O₃."),
                        RawQuestion("$tag Liên kết hóa học trong phân tử nước (H₂O) được hình thành bằng cách dùng chung cặp electron là loại liên kết:", listOf("Liên kết cộng hóa trị", "Liên kết ion", "Liên kết kim loại", "Liên kết van der Waals"), 0, "Hai phi kim H và O góp chung electron tạo liên kết cộng hóa trị."),
                        RawQuestion("$tag Bảng tuần hoàn các nguyên tố hóa học sắp xếp các nguyên tố theo chiều tăng dần của:", listOf("Điện tích hạt nhân (số proton Z)", "Độ cứng", "Màu sắc", "Thứ tự bảng chữ cái"), 0, "Số thứ tự ô nguyên tố bằng số đơn vị điện tích hạt nhân Z."),
                        RawQuestion("$tag Chu kì trong bảng tuần hoàn là dãy các nguyên tố có cùng yếu tố nào trong cấu tạo vỏ nguyên tử?", listOf("Cùng số lớp electron", "Cùng số neutron", "Cùng hóa trị cao nhất", "Cùng khối lượng riêng"), 0, "Số thứ tự chu kì bằng số lớp electron của nguyên tử."),
                        RawQuestion("$tag Phân tử khối của hợp chất CaCO₃ (Ca=40, C=12, O=16) là:", listOf("100 amu", "68 amu", "84 amu", "116 amu"), 0, "40 + 12 + 3×16 = 100 amu."),
                        RawQuestion("$tag Phân tử khối của axit sunfuric H₂SO₄ (H=1, S=32, O=16) là:", listOf("98 amu", "82 amu", "64 amu", "100 amu"), 0, "2×1 + 32 + 4×16 = 98 amu.")
                    )
                )
            }
            8, 9 -> {
                val tag = "[KHTN / Hóa học Lớp $grade]"
                val elements = listOf(
                    Triple("Sắt (Fe)", 56, listOf(5.6, 11.2, 16.8, 22.4, 28.0)),
                    Triple("Đồng (Cu)", 64, listOf(6.4, 12.8, 19.2, 25.6, 32.0)),
                    Triple("Kẽm (Zn)", 65, listOf(6.5, 13.0, 19.5, 26.0, 32.5)),
                    Triple("Nhôm (Al)", 27, listOf(2.7, 5.4, 8.1, 10.8, 13.5)),
                    Triple("Magie (Mg)", 24, listOf(2.4, 4.8, 7.2, 9.6, 12.0)),
                    Triple("Canxi (Ca)", 40, listOf(4.0, 8.0, 12.0, 16.0, 20.0))
                )
                for ((name, molarMass, masses) in elements) {
                    for (m in masses) {
                        val n = m / molarMass
                        val nStr = String.format(Locale.US, "%.1f", n)
                        list.add(
                            RawQuestion(
                                question = "$tag Số mol của $m gam $name (M = $molarMass g/mol) là:",
                                options = listOf(
                                    "$nStr mol",
                                    String.format(Locale.US, "%.1f mol", n + 0.2),
                                    String.format(Locale.US, "%.1f mol", n + 0.4),
                                    String.format(Locale.US, "%.1f mol", n + 0.7)
                                ),
                                correctIndex = 0,
                                explanation = "Áp dụng n = m / M = $m / $molarMass = $nStr mol."
                            )
                        )
                    }
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Dung dịch làm quỳ tím chuyển sang màu xanh là:", listOf("NaOH", "HCl", "H₂SO₄", "NaCl"), 0, "Dung dịch kiềm (bazơ tan) có pH > 7 làm quỳ tím hóa xanh."),
                        RawQuestion("$tag Dung dịch làm quỳ tím chuyển sang màu đỏ là:", listOf("HCl", "KOH", "Ca(OH)₂", "Na₂SO₄"), 0, "Dung dịch axit có pH < 7 làm quỳ tím hóa đỏ."),
                        RawQuestion("$tag Môi trường trung tính (như nước tinh khiết ở 25°C) có giá trị pH bằng:", listOf("7", "1", "14", "0"), 0, "pH = 7 trung tính; pH < 7 axit; pH > 7 bazơ."),
                        RawQuestion("$tag Kim loại nào sau đây tác dụng được với dung dịch HCl loãng giải phóng khí H₂?", listOf("Fe", "Cu", "Ag", "Au"), 0, "Fe đứng trước H trong dãy hoạt động hóa học kim loại."),
                        RawQuestion("$tag Khí nào làm mất màu dung dịch nước brom ở điều kiện thường?", listOf("Etilen (C₂H₄)", "Metan (CH₄)", "Nitơ (N₂)", "Hiđro (H₂)"), 0, "Etilen có liên kết đôi C=C nên tham gia phản ứng cộng với Br₂."),
                        RawQuestion("$tag Phản ứng giữa axit axetic (CH₃COOH) và ancol etylic (C₂H₅OH) khi có H₂SO₄ đặc xúc tác đun nóng tạo thành:", listOf("Etyl axetat (CH₃COOC₂H₅)", "Natri axetat", "Metan", "Glucôzơ"), 0, "Đây là phản ứng este hóa thuận nghịch tạo mùi thơm hoa quả."),
                        RawQuestion("$tag Theo định luật bảo toàn khối lượng trong phản ứng hóa học A + B → C + D:", listOf("m_A + m_B = m_C + m_D", "m_A + m_B > m_C + m_D", "m_A = m_C", "Tổng khối lượng giảm đi một nửa"), 0, "Tổng khối lượng các chất sản phẩm luôn bằng tổng khối lượng các chất tham gia."),
                        RawQuestion("$tag Thể tích của 0,5 mol khí bất kỳ ở điều kiện chuẩn (25°C, 1 bar; V_m = 24,79 L/mol) là:", listOf("12,395 lít", "24,79 lít", "11,2 lít", "22,4 lít"), 0, "V = n × 24,79 = 0,5 × 24,79 = 12,395 lít."),
                        RawQuestion("$tag Hiện tượng xảy ra khi nhỏ vài giọt dung dịch phenolphtalein không màu vào dung dịch NaOH là:", listOf("Dung dịch chuyển sang màu hồng", "Dung dịch hóa xanh lam", "Xuất hiện kết tủa vàng", "Không đổi màu"), 0, "Phenolphtalein hóa hồng trong môi trường kiềm."),
                        RawQuestion("$tag Để nhận biết khí CO₂, người ta thường dẫn khí qua dung dịch nào để thấy kết tủa trắng?", listOf("Nước vôi trong Ca(OH)₂ dư", "Nước cất", "Dung dịch HCl", "Dung dịch NaCl"), 0, "CO₂ + Ca(OH)₂ → CaCO₃↓ (trắng) + H₂O."),
                        RawQuestion("$tag Oxit nào thuộc loại oxit bazơ tác dụng với nước tạo dung dịch kiềm?", listOf("CaO (vôi sống)", "CO₂", "SO₂", "P₂O₅"), 0, "CaO + H₂O → Ca(OH)₂."),
                        RawQuestion("$tag Kim loại nhẹ, màu trắng bạc, dẫn điện tốt thường dùng làm vỏ máy bay và dây cáp điện là:", listOf("Nhôm (Al)", "Chì (Pb)", "Vàng (Au)", "Thủy ngân (Hg)"), 0, "Nhôm có khối lượng riêng nhỏ (2,7 g/cm³) và bền trong không khí nhờ màng oxit bảo vệ.")
                    )
                )
            }
            10 -> {
                val tag = "[Hóa học Lớp 10]"
                for (z in 6..25) {
                    val p = z
                    val n = z + (if (z % 2 == 0) 0 else 1)
                    val a = p + n
                    list.add(
                        RawQuestion(
                            question = "$tag Nguyên tử của nguyên tố có số hiệu nguyên tử Z = $z và số khối A = $a. Số hạt neutron trong hạt nhân là:",
                            options = distinctNumbers(n, listOf(p, a, a + p)),
                            correctIndex = 0,
                            explanation = "Số khối A = Z + N ⇒ Số neutron N = A - Z = $a - $z = $n."
                        )
                    )
                }
                for (z in 11..22) {
                    val a = z * 2 + (if (z > 16) 2 else 0)
                    list.add(
                        RawQuestion(
                            question = "$tag Tổng số hạt proton và electron trong một nguyên tử trung hòa có số hiệu Z = $z là:",
                            options = distinctNumbers(2 * z, listOf(z, 3 * z, a)),
                            correctIndex = 0,
                            explanation = "Trong nguyên tử trung hòa p = e = $z ⇒ tổng (p + e) = 2·$z = ${2 * z}."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Liên kết hóa học trong phân tử NaCl là loại liên kết nào?", listOf("Liên kết ion", "Liên kết cộng hóa trị không cực", "Liên kết hiđrô", "Liên kết kim loại"), 0, "Na⁺ và Cl⁻ hút nhau bằng lực hút tĩnh điện tạo liên kết ion."),
                        RawQuestion("$tag Halogen nào có tính oxi hóa mạnh nhất?", listOf("Flo (F₂)", "Clo (Cl₂)", "Brom (Br₂)", "Iot (I₂)"), 0, "Tính oxi hóa của các đơn chất halogen giảm dần từ F₂ → Cl₂ → Br₂ → I₂."),
                        RawQuestion("$tag Phản ứng tỏa nhiệt có giá trị biến thiên entanpi chuẩn Δ_r H°₂₉₈ như thế nào?", listOf("Δ_r H°₂₉₈ < 0", "Δ_r H°₂₉₈ > 0", "Δ_r H°₂₉₈ = 0", "Δ_r H°₂₉₈ = +100 kJ"), 0, "Phản ứng tỏa nhiệt giải phóng nhiệt lượng ra môi trường nên ΔH < 0."),
                        RawQuestion("$tag Trong phản ứng oxi hóa - khử, chất khử là chất có đặc điểm gì?", listOf("Nhường electron (số oxi hóa tăng sau phản ứng)", "Nhận electron (số oxi hóa giảm)", "Không đổi số oxi hóa", "Luôn là nước"), 0, "Khử cho - O nhận."),
                        RawQuestion("$tag Số oxi hóa của nguyên tố S trong phân tử H₂SO₄ là:", listOf("+6", "+4", "-2", "0"), 0, "2(+1) + x + 4(-2) = 0 ⇒ x = +6."),
                        RawQuestion("$tag Yếu tố nào KHÔNG làm tăng tốc độ của phản ứng hóa học thông thường?", listOf("Giảm nhiệt độ phản ứng xuống thấp", "Tăng nồng độ chất tham gia", "Nghiền nhỏ chất rắn (tăng diện tích tiếp xúc)", "Thêm chất xúc tác thích hợp"), 0, "Hạ nhiệt độ làm giảm động năng phân tử nên tốc độ phản ứng chậm lại."),
                        RawQuestion("$tag Cấu hình electron lớp ngoài cùng của các nguyên tố khí hiếm (trừ Heli) là:", listOf("ns²np⁶ (8 electron lớp ngoài cùng)", "ns²np⁵", "ns¹", "ns²np⁴"), 0, "Lớp vỏ bão hòa 8 electron bền vững giúp khí hiếm rất trơ."),
                        RawQuestion("$tag Liên kết hiđrô giữa các phân tử nước giải thích cho hiện tượng nào sau đây?", listOf("Nước có nhiệt độ sôi cao bất thường (100°C) so với H₂S", "Nước không hòa tan được muối", "Nước dẫn điện rất mạnh", "Nước nặng hơn thủy ngân"), 0, "Liên kết hiđrô liên phân tử cần thêm nhiệt lượng để phá vỡ khi sôi.")
                    )
                )
            }
            11 -> {
                val tag = "[Hóa học Lớp 11]"
                for (exp in 1..6) {
                    list.add(
                        RawQuestion(
                            question = "$tag Dung dịch axit mạnh có nồng độ [H⁺] = 10^(-$exp) M. Giá trị pH của dung dịch là:",
                            options = distinctNumbers(exp, listOf(14 - exp, exp + 2, exp + 1)),
                            correctIndex = 0,
                            explanation = "pH = -lg[H⁺] = -lg(10^(-$exp)) = $exp."
                        )
                    )
                }
                for (exp in 1..6) {
                    val ph = 14 - exp
                    list.add(
                        RawQuestion(
                            question = "$tag Dung dịch bazơ mạnh NaOH có nồng độ [OH⁻] = 10^(-$exp) M ở 25°C. Giá trị pH của dung dịch là:",
                            options = distinctNumbers(ph, listOf(exp, ph - 2, ph + 1)),
                            correctIndex = 0,
                            explanation = "pOH = $exp ⇒ pH = 14 - pOH = 14 - $exp = $ph."
                        )
                    )
                }
                val alkanes = listOf(
                    "Metan (CH₄)" to 16, "Etan (C₂H₆)" to 30, "Propan (C₃H₈)" to 44,
                    "Butan (C₄H₁₀)" to 58, "Pentan (C₅H₁₂)" to 72, "Hexan (C₆H₁₄)" to 86,
                    "Etilen (C₂H₄)" to 28, "Propen (C₃H₆)" to 42, "But-1-en (C₄H₈)" to 56,
                    "Axetilen (C₂H₂)" to 26, "Propin (C₃H₄)" to 40, "Benzen (C₆H₆)" to 78,
                    "Toluen (C₇H₈)" to 92, "Stiren (C₈H₈)" to 104, "Metanol (CH₃OH)" to 32,
                    "Etanol (C₂H₅OH)" to 46, "Axit fomic (HCOOH)" to 46, "Axit axetic (CH₃COOH)" to 60,
                    "Fomanđehit (HCHO)" to 30, "Axetanđehit (CH₃CHO)" to 44
                )
                for ((compound, mVal) in alkanes) {
                    list.add(
                        RawQuestion(
                            question = "$tag Phân tử khối của hợp chất hữu cơ $compound là:",
                            options = distinctNumbers(mVal, listOf(mVal + 14, (mVal - 14).coerceAtLeast(12), mVal + 2)),
                            correctIndex = 0,
                            explanation = "Cộng nguyên tử khối C=12, H=1, O=16 của $compound cho kết quả $mVal."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Chất nào sau đây thuộc loại hiđrocacbon no (ankan)?", listOf("Propan (C₃H₈)", "Etilen (C₂H₄)", "Axetilen (C₂H₂)", "Benzen (C₆H₆)"), 0, "Ankan mạch hở có công thức chung CₙH₂ₙ₊₂ (n ≥ 1)."),
                        RawQuestion("$tag Phản ứng đặc trưng của ankan là:", listOf("Phản ứng thế (với halogen khi chiếu sáng)", "Phản ứng cộng H₂", "Phản ứng trùng hợp", "Phản ứng tráng bạc"), 0, "Ankan chỉ có liên kết đơn σ bền vững nên dễ tham gia phản ứng thế."),
                        RawQuestion("$tag Trong hệ cân bằng hóa học thuận nghịch, khi tăng áp suất thì cân bằng chuyển dịch theo chiều nào?", listOf("Chiều làm giảm số mol phân tử khí", "Chiều làm tăng số mol khí", "Không chuyển dịch trong mọi trường hợp", "Chiều tỏa nhiệt luôn thắng thế"), 0, "Theo nguyên lí chuyển dịch cân bằng Le Chatelier."),
                        RawQuestion("$tag Hiện tượng phú dưỡng ở ao hồ thường do dư thừa quá mức các chất dinh dưỡng nào?", listOf("Hợp chất của Nitơ (N) và Photpho (P)", "Bụi cát mịn", "Oxy hòa tan", "Khí trơ Argon"), 0, "Từ phân bón dư thừa và nước thải sinh hoạt làm tảo nở hoa gây thiếu oxy trong nước."),
                        RawQuestion("$tag Ankin nào đầu dãy có liên kết ba đầu mạch tác dụng với dung dịch AgNO₃/NH₃ tạo kết tủa vàng nhạt?", listOf("Axetilen (C₂H₂)", "Etan (C₂H₆)", "Etilen (C₂H₄)", "But-2-in"), 0, "Hiđro gắn ở cacbon mang nối ba đầu mạch (≡C–H) có tính linh động thế được Ag⁺."),
                        RawQuestion("$tag Phản ứng tráng bạc (tạo lớp Ag sáng bóng với thuốc thử Tollens) là phản ứng đặc trưng nhận biết nhóm chức nào?", listOf("Nhóm anđehit (–CHO)", "Nhóm ancol (–OH)", "Nhóm ete (–O–)", "Vòng benzen"), 0, "R–CHO bị oxi hóa tạo muối amoni và kết tủa Ag kim loại."),
                        RawQuestion("$tag Phenol (C₆H₅OH) tác dụng được với dung dịch nào sau đây mà ancol etylic (C₂H₅OH) không phản ứng?", listOf("Dung dịch NaOH và nước brom", "Kim loại Na", "Axit HBr đặc", "Khí O₂ cháy"), 0, "Do ảnh hưởng của vòng benzen, phenol có tính axit yếu tan được trong dung dịch kiềm."),
                        RawQuestion("$tag Phân bón hóa học nào cung cấp nguyên tố dinh dưỡng giúp cây phát triển mạnh cành lá?", listOf("Phân đạm (chứa N)", "Phân lân (chứa P)", "Phân kali (chứa K)", "Vôi bột (CaO)"), 0, "Đạm kích thích sinh trưởng thân lá; lân phát triển rễ ra hoa; kali tăng chống chịu."),
                        RawQuestion("$tag Axit sunfuric đặc (H₂SO₄ đặc) có tính chất đặc biệt nguy hiểm nào khi tiếp xúc với đường hoặc vải sợi?", listOf("Tính háo nước mạnh (than hóa chất hữu cơ)", "Tính bay hơi lạnh", "Làm đóng băng nước", "Không gây bỏng da"), 0, "Khi pha loãng H₂SO₄ đặc phải rót từ từ axit vào nước và khuấy đều."),
                        RawQuestion("$tag Sắp xếp độ mạnh axit tăng dần của các hợp chất hữu cơ sau: C₂H₅OH, C₆H₅OH, H₂CO₃, CH₃COOH.", listOf("C₂H₅OH < C₆H₅OH < H₂CO₃ < CH₃COOH", "CH₃COOH < H₂CO₃ < C₆H₅OH < C₂H₅OH", "C₆H₅OH < C₂H₅OH < CH₃COOH", "H₂CO₃ < C₂H₅OH < CH₃COOH"), 0, "Phenol yếu hơn axit cacbonic, còn axit axetic mạnh hơn axit cacbonic (đẩy được CO₂ khỏi muối cacbonat).")
                    )
                )
            }
            else -> {
                val tag = "[Hóa học Lớp 12]"
                val esters = listOf(
                    "Metyl fomat (M = 60)" to 60, "Etyl fomat (M = 74)" to 74,
                    "Metyl axetat (M = 74)" to 74, "Etyl axetat (M = 88)" to 88,
                    "Metyl propionat (M = 88)" to 88, "Metyl acrylat (M = 86)" to 86,
                    "Vinyl axetat (M = 86)" to 86, "Glucôzơ (M = 180)" to 180,
                    "Saccarozơ (M = 342)" to 342, "Glyxin (M = 75)" to 75,
                    "Alanin (M = 89)" to 89, "Valin (M = 117)" to 117,
                    "Anilin (M = 93)" to 93, "Metylamin (M = 31)" to 31,
                    "Đimetylamin (M = 45)" to 45, "Etylamin (M = 45)" to 45
                )
                esters.forEachIndexed { idx, (name, mMass) ->
                    val mol = (idx % 4) + 1
                    val mass = mol * mMass
                    list.add(
                        RawQuestion(
                            question = "$tag Số mol của $mass gam $name là:",
                            options = distinctNumbers(mol, listOf(mol + 1, mol + 2, mol + 3)).map { "$it mol" },
                            correctIndex = 0,
                            explanation = "Áp dụng n = m / M = $mass / $mMass = $mol mol."
                        )
                    )
                }
                for (i in 1..10) {
                    val mFe = i * 56
                    val nFe = i
                    val mCu = nFe * 64
                    list.add(
                        RawQuestion(
                            question = "$tag Cho $nFe mol kim loại Fe tác dụng hoàn toàn với dung dịch CuSO₄ dư. Khối lượng Cu kim loại thu được là (Cu = 64):",
                            options = distinctNumbers(mCu, listOf(mFe, mCu + 32, mCu / 2)).map { "$it g" },
                            correctIndex = 0,
                            explanation = "Fe + CuSO₄ → FeSO₄ + Cu ⇒ n_Cu = n_Fe = $nFe mol ⇒ m_Cu = $nFe × 64 = $mCu g."
                        )
                    )
                }
                list.addAll(
                    listOf(
                        RawQuestion("$tag Công thức tổng quát của este no, đơn chức, mạch hở là:", listOf("CₙH₂ₙO₂ (n ≥ 2)", "CₙH₂ₙ₊₂O₂ (n ≥ 2)", "CₙH₂ₙ₋₂O₂ (n ≥ 2)", "CₙH₂ₙO (n ≥ 2)"), 0, "Có 1 liên kết đôi C=O ở nhóm chức –COO– nên độ bất bão hòa k = 1."),
                        RawQuestion("$tag Thủy phân este trong dung dịch kiềm (NaOH, KOH) đun nóng gọi là phản ứng:", listOf("Xà phòng hóa", "Este hóa", "Trùng hợp", "Đề hiđrat hóa"), 0, "Phản ứng một chiều tạo muối của axit cacboxylic và ancol."),
                        RawQuestion("$tag Cacbohiđrat nào sau đây thuộc loại đisaccarit?", listOf("Saccarozơ", "Glucôzơ", "Fructôzơ", "Tinh bột"), 0, "Saccarozơ (C₁₂H₂₂O₁₁) thủy phân tạo 1 glucôzơ + 1 fructôzơ."),
                        RawQuestion("$tag Dung dịch chất nào sau đây làm quỳ tím chuyển sang màu xanh?", listOf("Metylamin (CH₃NH₂)", "Anilin (C₆H₅NH₂)", "Glyxin", "Glucôzơ"), 0, "Amin béo bậc I có tính bazơ mạnh hơn NH₃."),
                        RawQuestion("$tag Tơ nilon-6,6 được điều chế bằng phản ứng nào?", listOf("Trùng ngưng", "Trùng hợp", "Thủy phân", "Este hóa"), 0, "Trùng ngưng hexametylenđiamin với axit ađipic tách ra phân tử H₂O."),
                        RawQuestion("$tag Kim loại nào có độ dẫn điện tốt nhất ở nhiệt độ thường?", listOf("Bạc (Ag)", "Đồng (Cu)", "Vàng (Au)", "Nhôm (Al)"), 0, "Thứ tự dẫn điện giảm dần: Ag > Cu > Au > Al > Fe."),
                        RawQuestion("$tag Chất béo (triglyceride) là trieste của axit béo với ancol đa chức nào?", listOf("Glixerol (C₃H₅(OH)₃)", "Etilen glicol", "Ancol etylic", "Metanol"), 0, "Khi xà phòng hóa chất béo thu được xà phòng (muối Na/K axit béo) và glixerol."),
                        RawQuestion("$tag Trong pin điện hóa Zn–Cu chuẩn, quá trình xảy ra ở cực âm (anot) là:", listOf("Sự oxi hóa kim loại kẽm: Zn → Zn²⁺ + 2e", "Sự khử ion Cu²⁺", "Sự khử ion H⁺", "Không có phản ứng"), 0, "Cực âm (anot) luôn xảy ra quá trình oxi hóa; cực dương (catot) xảy ra quá trình khử."),
                        RawQuestion("$tag Phương pháp chung điều chế các kim loại hoạt động hóa học mạnh như Na, Mg, Al trong công nghiệp là:", listOf("Điện phân nóng chảy muối halogenua hoặc oxit", "Nhiệt luyện bằng CO", "Thủy luyện dùng Fe đẩy ra khỏi dung dịch", "Đun nóng trong không khí"), 0, "Al được sản xuất bằng điện phân nóng chảy Al₂O₃ có mặt criolit."),
                        RawQuestion("$tag Polime nào sau đây được điều chế bằng phản ứng trùng hợp?", listOf("Polietilen (PE) và PVC", "Nilon-6,6", "Tơ lapsan", "Tinh bột"), 0, "Từ các monome có liên kết đôi C=C như CH₂=CH₂, CH₂=CHCl."),
                        RawQuestion("$tag Chất nào tác dụng với dung dịch HNO₃ đặc tạo kết tủa vàng (phản ứng màu biure/xantoprotein)?", listOf("Lòng trắng trứng (albumin/protein)", "Glucôzơ", "Saccarozơ", "Dầu ăn"), 0, "Nhờ vòng thơm trong một số gốc axit amin của phân tử protein."),
                        RawQuestion("$tag Phản ứng nào chứng minh trong phân tử glucôzơ có nhiều nhóm –OH kề nhau?", listOf("Hòa tan Cu(OH)₂ ở nhiệt độ thường tạo dung dịch xanh lam", "Phản ứng tráng bạc", "Phản ứng lên men rượu", "Phản ứng cộng H₂"), 0, "Tạo phức đồng-glucozơ màu xanh lam đặc trưng."),
                        RawQuestion("$tag Amino axit (như glyxin H₂N–CH₂–COOH) thể hiện tính chất lưỡng tính vì:", listOf("Tác dụng được với cả axit mạnh (HCl) và bazơ mạnh (NaOH)", "Chỉ tan trong xăng", "Không tan trong nước", "Làm mất màu nước brom"), 0, "Chứa đồng thời nhóm bazơ –NH₂ và nhóm axit –COOH."),
                        RawQuestion("$tag Kim loại kiềm nào nhẹ nhất và nổi trên mặt nước khi tác dụng mạnh giải phóng khí H₂?", listOf("Liti (Li) / Natri (Na) / Kali (K)", "Sắt (Fe)", "Đồng (Cu)", "Chì (Pb)"), 0, "Các kim loại kiềm nhóm IA có khối lượng riêng nhỏ và tính khử rất mạnh."),
                        RawQuestion("$tag Hiện tượng ăn mòn điện hóa xảy ra nhanh nhất khi gắn hai kim loại khác nhau tiếp xúc nhau trong môi trường nào?", listOf("Dung dịch chất điện li (như nước biển, không khí ẩm có CO₂)", "Chân không tuyệt đối", "Dầu hỏa khan", "Khí nitơ khô"), 0, "Ví dụ vỏ tàu biển bằng thép gắn lá kẽm để bảo vệ vỏ thép theo phương pháp điện hóa."),
                        RawQuestion("$tag Tinh bột bị thủy phân hoàn toàn nhờ axit loãng đun nóng hoặc enzim thu được sản phẩm cuối cùng là:", listOf("Glucôzơ (C₆H₁₂O₆)", "Fructôzơ", "Saccarozơ", "Axit axetic"), 0, "(C₆H₁₀O₅)ₙ + nH₂O → nC₆H₁₂O₆.")
                    )
                )
            }
        }
        return list
    }

    private fun primaryScienceChemistry(g: Int): List<RawQuestion> {
        val tag = "[Khoa học / Hóa học Lớp $g]"
        val list = mutableListOf<RawQuestion>()
        for (i in 1..20) {
            val salt = i * 2
            val water = 50 + i * 3
            val total = salt + water
            list.add(
                RawQuestion(
                    question = "$tag Trộn đều $salt g đường với $water g nước ấm thu được một cốc nước đường có tổng khối lượng là:",
                    options = distinctNumbers(total, listOf(water - salt, total + 10, salt * 2)).map { "$it g" },
                    correctIndex = 0,
                    explanation = "Tổng khối lượng hỗn hợp = $salt + $water = $total g."
                )
            )
        }
        for (i in 1..15) {
            val air = 100 * i
            val oxygen = 21 * i
            list.add(
                RawQuestion(
                    question = "$tag Biết khí oxygen chiếm khoảng 21% thể tích không khí. Trong $air lít không khí sạch có khoảng bao nhiêu lít khí oxygen?",
                    options = distinctNumbers(oxygen, listOf(78 * i, oxygen + 10, air / 2)).map { "$it lít" },
                    correctIndex = 0,
                    explanation = "21% của $air lít = $air × 21 / 100 = $oxygen lít."
                )
            )
        }
        list.addAll(
            listOf(
                RawQuestion("$tag Khí nào trong không khí duy trì sự cháy và sự hô hấp của người, động vật?", listOf("Khí oxygen (ô-xi)", "Khí nitơ", "Khí các-bô-níc", "Khí hê-li"), 0, "Thiếu khí ô-xi, con người không thể thở và ngọn lửa sẽ tắt."),
                RawQuestion("$tag Chất nào sau đây hòa tan được trong nước sạch?", listOf("Muối ăn và đường kính", "Cát và sỏi", "Dầu ăn", "Bột sắt"), 0, "Dầu ăn nhẹ nổi trên mặt nước, cát và sắt không tan."),
                RawQuestion("$tag Đồ vật bằng sắt để lâu ngoài trời mưa ẩm dễ xảy ra hiện tượng gì?", listOf("Bị gỉ sét (tạo lớp gỉ màu nâu đỏ)", "Biến thành vàng ròng", "Tan hết thành hơi nước", "Phát sáng trong đêm"), 0, "Sắt tác dụng với oxygen và hơi nước trong không khí tạo gỉ sắt."),
                RawQuestion("$tag Đá vôi (canxi cacbonat) có tính chất đặc trưng nào khi nhỏ giấm ăn (axit) vào?", listOf("Sủi bọt khí (khí CO₂)", "Hóa thành băng đá", "Đổi sang màu tím thẫm", "Không có hiện tượng gì"), 0, "Axit trong giấm hoặc chanh tác dụng với đá vôi giải phóng bọt khí."),
                RawQuestion("$tag Để làm sạch nước giếng bị lẫn cát bùn trước khi đun sôi, người ta thường làm gì?", listOf("Để lắng và lọc qua cát, sỏi, than hoạt tính", "Cho thêm dầu hỏa", "Ném thêm đất sét", "Phơi ngoài đường bụi"), 0, "Lọc giúp giữ lại tạp chất không tan; đun sôi giúp diệt vi khuẩn."),
                RawQuestion("$tag Vật liệu nào nhẹ, bền, không bị gỉ sét dùng làm khung cửa kính hiện đại?", listOf("Nhôm (Al)", "Giấy bìa", "Đất nung", "Sáp ong"), 0, "Hợp kim nhôm vừa nhẹ vừa có độ bền cao."),
                RawQuestion("$tag Khi ngửi thấy mùi khí ga rò rỉ trong bếp, việc làm an toàn đầu tiên là:", listOf("Khóa van bình ga, mở toang cửa sổ và không bật công tắc điện hay bật lửa", "Bật đèn sáng để kiểm tra", "Quẹt diêm tìm chỗ rò", "Đóng kín mọi cửa"), 0, "Tia lửa điện từ công tắc có thể kích nổ hỗn hợp khí ga và không khí.")
            )
        )
        return list
    }
}
