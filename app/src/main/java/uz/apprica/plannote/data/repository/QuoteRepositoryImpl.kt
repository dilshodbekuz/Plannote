package uz.apprica.plannote.data.repository

import uz.apprica.plannote.domain.repository.QuoteRepository
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteRepositoryImpl @Inject constructor() : QuoteRepository {

    override fun getTodayQuote(): String {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return QUOTES[dayOfYear % QUOTES.size]
    }

    override fun getRandomQuote(): String = QUOTES.random()

    override fun getAllQuotes(): List<String> = QUOTES

    companion object {
        val QUOTES = listOf(
            "Har bir katta muvaffaqiyat kichik harakatlar yig'indisidir. — Aristotel",
            "Bugun qilgan harakatingiz ertangi o'zingizni belgilaydi.",
            "Muvaffaqiyat — bu har kuni biroz yaxshilanish. — James Clear",
            "Vaqt – eng qimmatli resurs, uni oqilona sarfla.",
            "Rejali hayot – maqsadli hayot.",
            "Intizom — erkinlikning asosi. — Jocko Willink",
            "Bitta qaror hayotingizni o'zgartira oladi.",
            "Har kun yangi imkoniyat, undan foydalaning!",
            "Qiyin yo'l ko'pincha to'g'ri yo'ldir.",
            "Hech narsa bir kunda o'zgarmaydi, lekin har kuni nimadir o'zgaradi.",
            "Kichik g'alaba — katta muvaffaqiyatning boshlanishi.",
            "O'z kuchingga ishon, boshqalar fikridan ko'ra.",
            "Maqsad — yo'lning boshlanishi, iroda — uning davomi.",
            "Har bir expert bir vaqtlar yangi boshlagan. — Helen Hayes",
            "Harakatlar so'zlardan kuchliroq.",
            "Eng yaxshi vaqt bu — endi! — Konfutsiy",
            "Qiyinchiliklardan qo'rqma, ulardan o'rgan.",
            "Muvaffaqiyat taqdir emas — bu tanlov.",
            "O'rganish hech qachon tugamaydi.",
            "Kechagi siz bilan bugungi sizni solishtir, boshqa bilan emas.",
            "Sabr — barcha dardlarning darmoni. — Darvishlar hikmati",
            "Bilim — eng kuchli qurol. — Nelson Mandela",
            "Har bir kunda yaxshilik qil, bu yetarli.",
            "Maqsadingga erishish uchun avval uni yoz.",
            "Ishni bugun boshla, ertaga emas.",
            "O'z hayotingning muallifi bo'l.",
            "Qo'rquv — o'sishning boshlanishi.",
            "Baxtli bo'lish — bu tanlov.",
            "Har bir inson o'z taqdirini o'zi yaratadi.",
            "Ilhomlanish kutma — harakat qil, ilhom o'zi keladi."
        )
    }
}
