package com.vinhho.onthi10ai.data.solver

import com.vinhho.onthi10ai.data.solver.OfflineQuizBank.RawQuestion

internal object OfflineQuizBankEnglish {

    fun buildForGrade(grade: Int): List<RawQuestion> = when (grade) {
        in 1..5 -> primaryQuestions(grade)
        in 6..9 -> secondaryQuestions(grade)
        else -> highSchoolQuestions(grade)
    }

    private fun primaryQuestions(g: Int): List<RawQuestion> {
        val tag = "[Tiếng Anh Lớp $g]"
        return listOf(
            RawQuestion("$tag What _____ your name?", listOf("is", "are", "am", "do"), 0, "'Your name' là danh từ số ít nên dùng 'is'."),
            RawQuestion("$tag I go to school _____ Monday to Friday.", listOf("from", "at", "in", "on"), 0, "Cấu trúc 'from ... to ...' nghĩa là từ ... đến ..."),
            RawQuestion("$tag Look! The children _____ football in the yard.", listOf("are playing", "play", "played", "plays"), 0, "Có 'Look!' diễn tả hành động đang xảy ra → thì hiện tại tiếp diễn 'are playing'."),
            RawQuestion("$tag What is the opposite of 'big'?", listOf("small", "tall", "long", "heavy"), 0, "'Small' (nhỏ) trái nghĩa với 'big' (lớn)."),
            RawQuestion("$tag Lan likes _____ books in her free time.", listOf("reading", "read", "reads", "to reading"), 0, "Sau 'like' dùng động từ thêm '-ing': 'reading'."),
            RawQuestion("$tag How many _____ are there in your classroom?", listOf("desks", "desk", "a desk", "the desk"), 0, "Sau 'How many' dùng danh từ đếm được số nhiều 'desks'."),
            RawQuestion("$tag My mother is a doctor. She works in a _____.", listOf("hospital", "school", "factory", "bakery"), 0, "Bác sĩ (doctor) làm việc tại bệnh viện (hospital)."),
            RawQuestion("$tag There _____ a cat under the table.", listOf("is", "are", "am", "be"), 0, "'A cat' là danh từ số ít nên dùng 'There is'."),
            RawQuestion("$tag What time _____ you get up every morning?", listOf("do", "does", "are", "is"), 0, "Câu hỏi thì hiện tại đơn với chủ ngữ 'you' dùng trợ động từ 'do'."),
            RawQuestion("$tag Today is Monday. Tomorrow is _____.", listOf("Tuesday", "Sunday", "Wednesday", "Friday"), 0, "Hôm nay là thứ Hai (Monday), ngày mai là thứ Ba (Tuesday)."),
            RawQuestion("$tag Can a bird fly? — Yes, _____.", listOf("it can", "it does", "it is", "I can"), 0, "Câu hỏi với 'Can a bird...' trả lời 'Yes, it can.'"),
            RawQuestion("$tag My sister _____ ten years old.", listOf("is", "are", "am", "have"), 0, "'My sister' là ngôi thứ 3 số ít nên dùng 'is'."),
            RawQuestion("$tag Which word is a fruit?", listOf("banana", "carrot", "bread", "milk"), 0, "'Banana' (chuối) là trái cây; carrot là rau củ."),
            RawQuestion("$tag Where is Nam? — He is _____ the library.", listOf("in", "on", "under", "between"), 0, "Ở bên trong thư viện dùng giới từ 'in'."),
            RawQuestion("$tag We have English _____ Wednesdays and Fridays.", listOf("on", "in", "at", "to"), 0, "Trước các ngày trong tuần dùng giới từ 'on'."),
            RawQuestion("$tag _____ is your favorite color? — It's blue.", listOf("What", "Who", "Where", "When"), 0, "Hỏi về đồ vật/màu sắc dùng từ để hỏi 'What'."),
            RawQuestion("$tag My father goes to work _____ motorbike.", listOf("by", "on", "in", "with"), 0, "Đi bằng phương tiện gì dùng 'by + phương tiện'."),
            RawQuestion("$tag She has long black _____.", listOf("hair", "hairs", "eye", "nose"), 0, "'Hair' (tóc) là danh từ không đếm được."),
            RawQuestion("$tag It is very hot in the _____.", listOf("summer", "winter", "autumn", "spring"), 0, "Mùa hè (summer) thời tiết rất nóng."),
            RawQuestion("$tag How _____ is this pencil? — It is 5,000 dong.", listOf("much", "many", "old", "tall"), 0, "Hỏi giá tiền dùng 'How much'."),
            RawQuestion("$tag Close your book and listen _____ me, please.", listOf("to", "at", "for", "with"), 0, "Cụm động từ 'listen to' (lắng nghe ai đó)."),
            RawQuestion("$tag My brother _____ his teeth twice a day.", listOf("brushes", "brush", "brushing", "brushed"), 0, "Chủ ngữ số ít 'My brother' ở hiện tại đơn → động từ kết thúc bằng 'sh' thêm 'es'."),
            RawQuestion("$tag Would you like _____ apple juice?", listOf("some", "any", "a", "an"), 0, "Trong lời mời lịch sự 'Would you like...' dùng 'some'."),
            RawQuestion("$tag Which animal has a very long neck?", listOf("giraffe", "elephant", "tiger", "monkey"), 0, "'Giraffe' là hươu cao cổ."),
            RawQuestion("$tag I was at home _____ evening.", listOf("yesterday", "tomorrow", "next", "now"), 0, "'Was' ở quá khứ đơn → đi với 'yesterday evening'."),
            RawQuestion("$tag My birthday is _____ May.", listOf("in", "on", "at", "from"), 0, "Trước tháng không có ngày cụ thể dùng 'in'."),
            RawQuestion("$tag The sun rises in the _____.", listOf("east", "west", "north", "south"), 0, "Mặt trời mọc ở hướng đông (east)."),
            RawQuestion("$tag Are there _____ oranges in the fridge?", listOf("any", "some", "a", "an"), 0, "Trong câu hỏi thông thường dùng 'any' với danh từ số nhiều."),
            RawQuestion("$tag He is good _____ drawing pictures.", listOf("at", "in", "on", "for"), 0, "Cấu trúc 'be good at + V-ing' (giỏi về việc gì)."),
            RawQuestion("$tag Let's _____ a snowman in the winter.", listOf("make", "making", "makes", "made"), 0, "Sau 'Let's' dùng động từ nguyên thể không 'to': 'make'."),
            RawQuestion("$tag Which month comes after April?", listOf("May", "March", "June", "February"), 0, "Sau tháng Tư (April) là tháng Năm (May)."),
            RawQuestion("$tag She _____ to Hanoi last summer.", listOf("went", "goes", "go", "going"), 0, "'Last summer' là quá khứ đơn → dùng 'went'."),
            RawQuestion("$tag You shouldn't _____ too much candy at night.", listOf("eat", "eating", "to eat", "eats"), 0, "Sau động từ khuyết thiếu 'shouldn't' dùng V nguyên thể."),
            RawQuestion("$tag Whose bag is this? — It's _____.", listOf("mine", "my", "me", "I"), 0, "Đứng một mình thay cho 'my bag' dùng đại từ sở hữu 'mine'."),
            RawQuestion("$tag What's the _____ like today? — It's sunny and warm.", listOf("weather", "season", "climate", "sky"), 0, "Hỏi thời tiết hôm nay: 'What's the weather like today?'"),
            RawQuestion("$tag An elephant is _____ than a mouse.", listOf("bigger", "big", "biggest", "more big"), 0, "So sánh hơn với tính từ ngắn 'big' → 'bigger'."),
            RawQuestion("$tag My hobby is _____ stamps.", listOf("collecting", "collect", "collects", "collected"), 0, "Sau 'is' chỉ sở thích dùng danh động từ 'collecting'."),
            RawQuestion("$tag Please turn _____ the lights before leaving the room.", listOf("off", "on", "up", "at"), 0, "'Turn off' nghĩa là tắt đèn trước khi ra khỏi phòng."),
            RawQuestion("$tag How do you _____ your name? — L-A-N.", listOf("spell", "speak", "talk", "tell"), 0, "Đánh vần từng chữ cái dùng động từ 'spell'."),
            RawQuestion("$tag December is the _____ month of the year.", listOf("twelfth", "twelve", "twenty", "second"), 0, "Tháng 12 là tháng thứ mười hai (twelfth)."),
            RawQuestion("$tag My aunt lives _____ the third floor of that building.", listOf("on", "in", "at", "under"), 0, "Nói về tầng lầu dùng giới từ 'on' (on the third floor)."),
            RawQuestion("$tag Yesterday, Mai _____ a letter to her pen friend.", listOf("wrote", "writes", "write", "is writing"), 0, "Có 'Yesterday' dùng quá khứ đơn của 'write' là 'wrote'.")
        )
    }

    private fun secondaryQuestions(g: Int): List<RawQuestion> {
        val tag = "[Tiếng Anh Lớp $g]"
        return listOf(
            RawQuestion("$tag If Lan _____ harder, she will get high marks.", listOf("studies", "studied", "will study", "studying"), 0, "Câu điều kiện loại 1: If + S + V(s/es), S + will + V."),
            RawQuestion("$tag This school _____ in 1995.", listOf("was built", "built", "is built", "has built"), 0, "Bị động thì quá khứ đơn với 'in 1995': 'was built'."),
            RawQuestion("$tag The boy _____ is standing near the gate is my classmate.", listOf("who", "which", "whom", "whose"), 0, "Đại từ quan hệ thay cho danh từ chỉ người làm chủ ngữ là 'who'."),
            RawQuestion("$tag They have lived in Hanoi _____ 2018.", listOf("since", "for", "in", "at"), 0, "Trước mốc thời gian cụ thể trong hiện tại hoàn thành dùng 'since'."),
            RawQuestion("$tag Nam said that he _____ his homework then.", listOf("was doing", "is doing", "does", "will do"), 0, "Câu tường thuật lùi thì hiện tại tiếp diễn thành quá khứ tiếp diễn 'was doing'."),
            RawQuestion("$tag I wish I _____ speak English fluently.", listOf("could", "can", "will", "may"), 0, "Câu ước ở hiện tại: S + wish + S + V2/ed / could + V."),
            RawQuestion("$tag Let's go to the library, _____?", listOf("shall we", "will we", "do we", "don't we"), 0, "Câu hỏi đuôi của câu rủ 'Let's...' luôn là 'shall we'."),
            RawQuestion("$tag We stayed at home _____ it rained heavily.", listOf("because", "although", "despite", "so"), 0, "Chỉ nguyên nhân trước một mệnh đề dùng 'because'."),
            RawQuestion("$tag Although she was tired, she _____ her project on time.", listOf("finished", "finishes", "has finish", "finishing"), 0, "Vế trước quá khứ 'was tired' → vế sau chia quá khứ đơn 'finished'."),
            RawQuestion("$tag My father used to _____ in the countryside when he was young.", listOf("live", "living", "lived", "lives"), 0, "Cấu trúc 'used to + V nguyên thể' chỉ thói quen trong quá khứ."),
            RawQuestion("$tag She is looking forward to _____ from her cousin soon.", listOf("hearing", "hear", "heard", "be heard"), 0, "Cụm 'look forward to + V-ing' (mong chờ điều gì)."),
            RawQuestion("$tag English _____ all over the world today.", listOf("is spoken", "speaks", "spoke", "was speaking"), 0, "Câu bị động thì hiện tại đơn: 'is spoken'."),
            RawQuestion("$tag You have never been to Da Nang, _____?", listOf("have you", "haven't you", "did you", "are you"), 0, "Mệnh đề chứa 'never' mang nghĩa phủ định → câu hỏi đuôi khẳng định 'have you'."),
            RawQuestion("$tag The book _____ I bought yesterday is very informative.", listOf("which", "who", "whose", "where"), 0, "Thay cho vật 'the book' làm tân ngữ dùng 'which'."),
            RawQuestion("$tag He suggested _____ solar energy to save electricity.", listOf("using", "to use", "use", "used"), 0, "Cấu trúc 'suggest + V-ing' (gợi ý làm gì)."),
            RawQuestion("$tag If I _____ you, I would apologize to her immediately.", listOf("were", "am", "was", "will be"), 0, "Điều kiện loại 2 trái thực tế hiện tại: dùng 'were' cho mọi ngôi."),
            RawQuestion("$tag Hoa has worked as a volunteer _____ five years.", listOf("for", "since", "during", "ago"), 0, "Trước khoảng thời gian 'five years' dùng 'for'."),
            RawQuestion("$tag Would you mind _____ the window? It's a bit stuffy here.", listOf("opening", "open", "to open", "opened"), 0, "Cấu trúc lịch sự: 'Would you mind + V-ing?'"),
            RawQuestion("$tag That's the village _____ my grandparents were born.", listOf("where", "which", "who", "when"), 0, "Trạng từ quan hệ chỉ nơi chốn (tương đương 'in which') là 'where'."),
            RawQuestion("$tag By the time he arrived at the station, the bus _____.", listOf("had left", "left", "has left", "leaves"), 0, "Hành động xảy ra trước một thời điểm quá khứ dùng quá khứ hoàn thành 'had left'."),
            RawQuestion("$tag She asked me if I _____ play the guitar.", listOf("could", "can", "will", "am able"), 0, "Câu hỏi Yes/No tường thuật phải lùi 'can' thành 'could'."),
            RawQuestion("$tag Remember _____ off the gas stove before going out.", listOf("to turn", "turning", "turn", "turned"), 0, "'Remember + to V': nhớ phải làm việc gì trong tương lai."),
            RawQuestion("$tag Despite _____ bad weather, they went camping in the forest.", listOf("the", "of", "although", "because"), 0, "'Despite' đi trực tiếp với cụm danh từ (không có 'of')."),
            RawQuestion("$tag Mount Fansipan is the _____ mountain in Vietnam.", listOf("highest", "higher", "high", "most high"), 0, "So sánh nhất với tính từ ngắn 'high' → 'the highest'."),
            RawQuestion("$tag Avoid _____ fast food too often if you want to stay healthy.", listOf("eating", "eat", "to eat", "ate"), 0, "Sau động từ 'avoid' dùng danh động từ 'V-ing'."),
            RawQuestion("$tag A new bridge _____ in our town next year.", listOf("will be built", "will build", "is built", "was built"), 0, "Bị động thì tương lai đơn: 'will be + V3/ed'."),
            RawQuestion("$tag Neither Minh nor his friends _____ absent from class today.", listOf("are", "is", "was", "has been"), 0, "Với 'Neither A nor B', động từ chia theo chủ ngữ gần nhất B ('his friends' → số nhiều 'are')."),
            RawQuestion("$tag She speaks English much _____ than she did last year.", listOf("more fluently", "fluently", "most fluently", "fluenter"), 0, "So sánh hơn với trạng từ dài 'fluently' → 'more fluently'."),
            RawQuestion("$tag The man _____ car was stolen reported it to the police.", listOf("whose", "who", "whom", "which"), 0, "Đại từ quan hệ chỉ sở hữu đứng trước danh từ 'car' dùng 'whose'."),
            RawQuestion("$tag It took us two hours _____ to the museum.", listOf("to travel", "travel", "traveling", "traveled"), 0, "Cấu trúc 'It takes/took + O + time + to V'."),
            RawQuestion("$tag Unless you _____ harder, you won't pass the entrance exam.", listOf("work", "don't work", "worked", "will work"), 0, "'Unless' = 'If ... not', mệnh đề sau 'Unless' chia khẳng định thì hiện tại đơn."),
            RawQuestion("$tag While I _____ dinner, my friend called me.", listOf("was having", "had", "have", "am having"), 0, "Hành động đang diễn ra trong quá khứ thì có hành động khác xen vào → dùng 'was having'."),
            RawQuestion("$tag Plastic bags take hundreds of years to _____ in the environment.", listOf("decompose", "recycle", "produce", "protect"), 0, "'Decompose' nghĩa là phân hủy sinh học."),
            RawQuestion("$tag Choose the word whose underlined part is pronounced differently: 'wanted', 'needed', 'visited', 'stopped'.", listOf("stopped", "wanted", "needed", "visited"), 0, "'Stopped' phát âm đuôi -ed là /t/; các từ còn lại là /ɪd/."),
            RawQuestion("$tag She is _____ an intelligent student that every teacher praises her.", listOf("such", "so", "too", "very"), 0, "Cấu trúc 'such + (a/an) + adj + N + that...'."),
            RawQuestion("$tag The coffee was _____ hot for me to drink.", listOf("too", "so", "such", "enough"), 0, "Cấu trúc 'too + adj + (for sb) + to V' (quá ... để làm gì)."),
            RawQuestion("$tag This is the first time I _____ Ha Long Bay.", listOf("have visited", "visited", "visit", "am visiting"), 0, "Sau 'This is the first time' dùng thì hiện tại hoàn thành."),
            RawQuestion("$tag People should use public transport to _____ air pollution.", listOf("reduce", "increase", "pollute", "waste"), 0, "'Reduce air pollution' nghĩa là giảm ô nhiễm không khí."),
            RawQuestion("$tag Not only Hoa _____ also her sister is fond of traditional music.", listOf("but", "and", "or", "yet"), 0, "Cặp liên từ tương quan 'Not only ... but also ...'."),
            RawQuestion("$tag It is important for students _____ soft skills early.", listOf("to develop", "developing", "develop", "developed"), 0, "Cấu trúc 'It is + adj + for sb + to V'."),
            RawQuestion("$tag The test was easier than I had expected, _____?", listOf("wasn't it", "was it", "hadn't it", "didn't it"), 0, "Câu hỏi đuôi dựa vào động từ chính 'was' → 'wasn't it'."),
            RawQuestion("$tag We congratulated Mai _____ winning the first prize in the English contest.", listOf("on", "for", "at", "about"), 0, "Cấu trúc 'congratulate sb on V-ing/N' (chúc mừng ai về việc gì)."),
            RawQuestion("$tag Choose the synonym of 'enormous':", listOf("huge", "tiny", "narrow", "weak"), 0, "'Enormous' (khổng lồ) đồng nghĩa với 'huge'.")
        )
    }

    private fun highSchoolQuestions(g: Int): List<RawQuestion> {
        val tag = "[Tiếng Anh Lớp $g]"
        return listOf(
            RawQuestion("$tag Hardly _____ the room when the phone rang.", listOf("had I entered", "I had entered", "did I entered", "have I entered"), 0, "Đảo ngữ 'Hardly + had + S + V3/ed + when + S + V2/ed'."),
            RawQuestion("$tag The more you practice, _____ you will become.", listOf("the more confident", "more confident", "the most confident", "confident"), 0, "So sánh kép: 'The + comparative..., the + comparative...'."),
            RawQuestion("$tag Had he studied harder, he _____ the exam.", listOf("would have passed", "would pass", "will pass", "passed"), 0, "Đảo ngữ điều kiện loại 3: 'Had + S + V3/ed, S + would have + V3/ed'."),
            RawQuestion("$tag Not only _____ well, but she also plays the piano.", listOf("does she sing", "she sings", "sings she", "is she sing"), 0, "Đảo ngữ với 'Not only' đứng đầu câu: 'Not only + trợ động từ + S + V'."),
            RawQuestion("$tag _____ all the warnings, the climbers decided to continue their journey.", listOf("Notwithstanding", "Although", "Even though", "Because of"), 0, "'Notwithstanding' (= Despite) đi với cụm danh từ để chỉ sự nhượng bộ."),
            RawQuestion("$tag _____ the heavy rain, the outdoor concert was postponed.", listOf("Due to", "Because", "Although", "In spite of"), 0, "'Due to + cụm danh từ' chỉ nguyên nhân."),
            RawQuestion("$tag She had her car _____ at the local garage yesterday.", listOf("repaired", "repair", "repairing", "to repair"), 0, "Thể nhờ bảo bị động: 'have + something + V3/ed'."),
            RawQuestion("$tag Scarcely had the concert begun _____ the power went out.", listOf("when", "than", "after", "while"), 0, "Cấu trúc đảo ngữ 'Scarcely/Hardly ... when ...'; còn 'No sooner ... than ...'."),
            RawQuestion("$tag No sooner _____ home than it started to pour.", listOf("had she arrived", "she had arrived", "did she arrived", "was she arrive"), 0, "Đảo ngữ 'No sooner + had + S + V3/ed + than...'."),
            RawQuestion("$tag Were I to win the scholarship, I _____ study abroad.", listOf("would", "will", "had", "am"), 0, "Đảo ngữ điều kiện loại 2: 'Were + S + to V, S + would + V'."),
            RawQuestion("$tag _____ from the top of the hill, the city looks breathtaking at night.", listOf("Seen", "Seeing", "Having seen", "To see"), 0, "Rút gọn mệnh đề trạng ngữ dạng bị động dùng quá khứ phân từ 'Seen'."),
            RawQuestion("$tag _____ his homework, Nam went out to play basketball with his friends.", listOf("Having finished", "Finished", "To finish", "Being finish"), 0, "Nhấn mạnh hành động xảy ra trước dùng phân từ hoàn thành 'Having + V3/ed'."),
            RawQuestion("$tag The manager insisted that the report _____ submitted before Friday.", listOf("be", "is", "was", "will be"), 0, "Thức giả định sau 'insist that': S + (should) + V nguyên thể ('be')."),
            RawQuestion("$tag It is imperative that every citizen _____ traffic regulations.", listOf("obey", "obeys", "obeyed", "obeying"), 0, "Sau 'It is imperative that' động từ luôn ở dạng nguyên thể không 'to'."),
            RawQuestion("$tag Never before _____ such a magnificent performance.", listOf("have we witnessed", "we have witnessed", "did we witnessed", "we witnessed"), 0, "Trạng từ phủ định 'Never before' đứng đầu câu gây đảo ngữ."),
            RawQuestion("$tag Only after checking the data twice _____ the mistake.", listOf("did he realize", "he realized", "realized he", "had he realize"), 0, "Đảo ngữ với 'Only after + V-ing/mệnh đề': đảo ngữ ở mệnh đề chính."),
            RawQuestion("$tag The company is trying to _____ down on unnecessary expenses.", listOf("cut", "turn", "put", "take"), 0, "Cụm động từ 'cut down on' nghĩa là cắt giảm."),
            RawQuestion("$tag She takes _____ her mother in both appearance and personality.", listOf("after", "up", "over", "off"), 0, "'Take after' nghĩa là giống ai đó về ngoại hình hoặc tính cách."),
            RawQuestion("$tag Don't put _____ until tomorrow what you can do today.", listOf("off", "out", "away", "on"), 0, "'Put off' nghĩa là trì hoãn."),
            RawQuestion("$tag The scientist came _____ a brilliant solution during the experiment.", listOf("up with", "across with", "down with", "in for"), 0, "'Come up with' nghĩa là nảy ra ý tưởng/giải pháp."),
            RawQuestion("$tag There is a growing _____ among young people about mental health.", listOf("awareness", "aware", "awaken", "awfully"), 0, "Sau tính từ 'growing' cần danh từ 'awareness' (nhận thức)."),
            RawQuestion("$tag Renewable energy sources are considered _____ friendly.", listOf("environmentally", "environment", "environmental", "environmentalist"), 0, "Trước tính từ 'friendly' cần trạng từ 'environmentally'."),
            RawQuestion("$tag By next June, she _____ at this university for four years.", listOf("will have been studying", "will study", "studied", "has studied"), 0, "'By + mốc thời gian tương lai' đi với thì tương lai hoàn thành (tiếp diễn)."),
            RawQuestion("$tag The novel _____ into over thirty languages since its publication.", listOf("has been translated", "translated", "was translating", "translates"), 0, "Có 'since' và mang nghĩa bị động → hiện tại hoàn thành bị động."),
            RawQuestion("$tag Little _____ that the surprise party had already been arranged.", listOf("did she know", "she knew", "knew she", "does she knew"), 0, "Từ phủ định 'Little' đứng đầu câu gây đảo ngữ."),
            RawQuestion("$tag But for your timely assistance, we _____ in serious trouble now.", listOf("would be", "would have been", "will be", "are"), 0, "Điều kiện hỗn hợp (3-2) có 'now' ở mệnh đề chính → 'would be'."),
            RawQuestion("$tag Many endangered species are on the _____ of extinction.", listOf("verge", "edge", "limit", "corner"), 0, "Thành ngữ 'on the verge of extinction' (trên bờ vực tuyệt chủng)."),
            RawQuestion("$tag She has a good _____ of humor, which makes everyone feel relaxed.", listOf("sense", "feeling", "touch", "mind"), 0, "Cụm cố định 'a sense of humor' (khiếu hài hước)."),
            RawQuestion("$tag The candidate was rejected because he failed to _____ the requirements.", listOf("meet", "catch", "reach", "make"), 0, "Collocation 'meet the requirements' (đáp ứng yêu cầu)."),
            RawQuestion("$tag Choose the word whose main stress is placed differently: 'photograph', 'politics', 'strategy', 'mechanic'.", listOf("mechanic", "photograph", "politics", "strategy"), 0, "'Mechanic' nhấn âm tiết thứ 2; ba từ còn lại nhấn âm 1."),
            RawQuestion("$tag Choose the word closest in meaning to 'ubiquitous':", listOf("omnipresent", "scarce", "temporary", "fragile"), 0, "'Ubiquitous' (có mặt khắp nơi) đồng nghĩa với 'omnipresent'."),
            RawQuestion("$tag Choose the word opposite in meaning to 'mitigate':", listOf("aggravate", "alleviate", "lessen", "soften"), 0, "'Mitigate' (làm dịu bớt) trái nghĩa với 'aggravate' (làm trầm trọng thêm)."),
            RawQuestion("$tag The committee consists _____ ten experienced professors.", listOf("of", "in", "with", "about"), 0, "Động từ chủ động 'consist of' = 'be made up of' (bao gồm)."),
            RawQuestion("$tag He denied _____ the confidential documents to competitors.", listOf("leaking", "to leak", "leak", "leaked"), 0, "Sau 'deny' dùng danh động từ 'V-ing'."),
            RawQuestion("$tag It was not until midnight _____ the rescuers reached the cabin.", listOf("that", "when", "which", "where"), 0, "Cấu trúc nhấn mạnh 'It was not until + time + that + S + V'."),
            RawQuestion("$tag She made a great _____ to bridge the generation gap in her family.", listOf("effort", "result", "cause", "effect"), 0, "Collocation 'make an effort to V' (nỗ lực làm gì)."),
            RawQuestion("$tag Under no circumstances _____ leave the laboratory unattended.", listOf("must you", "you must", "you should", "can you to"), 0, "'Under no circumstances' đứng đầu câu bắt buộc đảo ngữ."),
            RawQuestion("$tag The article provides a comprehensive _____ of climate change impacts.", listOf("analysis", "analyze", "analytical", "analyst"), 0, "Sau tính từ 'comprehensive' cần danh từ 'analysis' (sự phân tích)."),
            RawQuestion("$tag Having been _____ of the danger, the residents evacuated immediately.", listOf("warned", "warning", "warn", "to warn"), 0, "Phân từ hoàn thành bị động: 'Having been + V3/ed'."),
            RawQuestion("$tag Most students prefer interactive lessons _____ traditional lectures.", listOf("to", "than", "over", "from"), 0, "Cấu trúc 'prefer A to B' (thích A hơn B)."),
            RawQuestion("$tag Artificial intelligence is transforming the job market at an _____ pace.", listOf("unprecedented", "unprepared", "unpleasant", "unwilling"), 0, "'At an unprecedented pace' nghĩa là với tốc độ chưa từng có."),
            RawQuestion("$tag So complicated _____ that nobody could solve it within ten minutes.", listOf("was the puzzle", "the puzzle was", "did the puzzle", "the puzzle did"), 0, "Đảo ngữ với 'So + adj + be + S + that...'")
        )
    }
}
