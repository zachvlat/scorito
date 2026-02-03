package com.zachvlat.footballscores.data.repository

import android.util.Log
import com.zachvlat.footballscores.data.api.LiveScoresApi
import com.zachvlat.footballscores.data.api.MatchDetailApi
import com.zachvlat.footballscores.data.api.BasketballApi
import com.zachvlat.footballscores.data.model.LiveScoresResponse
import com.zachvlat.footballscores.data.model.MatchDetailResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class LiveScoresRepository {
    
    private val api: LiveScoresApi
    private val basketballApi: BasketballApi
    private val matchDetailApi: MatchDetailApi
    
    init {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        
        val retrofit = Retrofit.Builder()
            .baseUrl("https://prod-cdn-mev-api.livescore.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val matchDetailRetrofit = Retrofit.Builder()
            .baseUrl("https://prod-cdn-public-api.livescore.com/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        api = retrofit.create(LiveScoresApi::class.java)
        basketballApi = retrofit.create(BasketballApi::class.java)
        matchDetailApi = matchDetailRetrofit.create(MatchDetailApi::class.java)
    }
    
    suspend fun getTodayLiveScores(): Result<LiveScoresResponse> {
        return try {
            val url = LiveScoresApi.getTodayUrl()
            Log.d("LiveScoresRepository", "Fetching URL: $url")
            val response = api.getLiveScores(url)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LiveScoresRepository", "Error fetching live scores", e)
            Result.failure(e)
        }
    }
    
    suspend fun getLiveScoresForDate(dateString: String): Result<LiveScoresResponse> {
        return try {
            val url = LiveScoresApi.getUrlForDateString(dateString)
            Log.d("LiveScoresRepository", "Fetching URL: $url")
            val response = api.getLiveScores(url)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LiveScoresRepository", "Error fetching live scores for date: $dateString", e)
            Result.failure(e)
        }
    }
    
    suspend fun getTodayBasketballScores(): Result<LiveScoresResponse> {
        return try {
            val url = BasketballApi.getTodayUrl()
            Log.d("LiveScoresRepository", "Fetching basketball URL: $url")
            val response = basketballApi.getBasketballScores(url)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LiveScoresRepository", "Error fetching basketball scores", e)
            Result.failure(e)
        }
    }
    
    suspend fun getBasketballScoresForDate(dateString: String): Result<LiveScoresResponse> {
        return try {
            val url = BasketballApi.getUrlForDateString(dateString)
            Log.d("LiveScoresRepository", "Fetching basketball URL: $url")
            val response = basketballApi.getBasketballScores(url)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LiveScoresRepository", "Error fetching basketball scores for date: $dateString", e)
            Result.failure(e)
        }
    }
    
    suspend fun getMatchDetails(matchId: String): Result<MatchDetailResponse> {
        return try {
            Log.d("LiveScoresRepository", "Fetching match details for ID: $matchId")
            val response = matchDetailApi.getMatchDetails(matchId)
            Result.success(response)
        } catch (e: Exception) {
            Log.e("LiveScoresRepository", "Error fetching match details for ID: $matchId", e)
            Result.failure(e)
        }
    }
}