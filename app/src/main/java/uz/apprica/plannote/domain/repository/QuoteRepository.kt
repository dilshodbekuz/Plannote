package uz.apprica.plannote.domain.repository

/**
 * Kunlik motivatsion iqtiboslar repository-si.
 * Room ishlatish shart emas — barcha iqtiboslar hardcode (offline-first).
 */
interface QuoteRepository {
    /** Bugungi iqtibos: dayOfYear % quotes.size (har kuni o'zgaradi) */
    fun getTodayQuote(): String
    /** Tasodifiy iqtibos (refresh tugmasi uchun) */
    fun getRandomQuote(): String
    /** Barcha iqtiboslar ro'yxati */
    fun getAllQuotes(): List<String>
}
