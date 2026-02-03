package com.zachvlat.footballscores.data.api

import com.zachvlat.footballscores.data.model.LiveScoresResponse
import retrofit2.http.GET
import java.text.SimpleDateFormat
import java.util.*

interface BasketballApi {
    
    @GET
    suspend fun getBasketballScores(@retrofit2.http.Url url: String): LiveScoresResponse
    
    companion object {
        private const val BASE_URL = "https://prod-cdn-mev-api.livescore.com/v1/api/app/date/basketball/"
        private const val LOCALE_PARAM = "?locale=en"
        
        fun getTodayUrl(): String {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            val today = Date()
            return "$BASE_URL${dateFormat.format(today)}/2$LOCALE_PARAM"
        }
        
        fun getUrlForDate(date: Date): String {
            val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
            return "$BASE_URL${dateFormat.format(date)}/2$LOCALE_PARAM"
        }
        
        fun getUrlForDateString(dateString: String): String {
            return "$BASE_URL$dateString/2$LOCALE_PARAM"
        }
    }
}