package com.example.appuse

import com.example.appuse.Model.NewsResponseApi
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


const val Base_url = "https://newsapi.org/v2/"
const val Api_key = "d0e1f5d0443344508549ba856d46d66c"


interface NewsInterface {
    @GET("top-headlines?apiKey=$Api_key")
    fun getHeadLines(
        @Query("country") country: String,
        @Query("category") category: String,
        @Query("page") page: Int
    ): Call<NewsResponseApi>

}


object NewsService {
    val newsInstance: NewsInterface

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        newsInstance = retrofit.create(NewsInterface::class.java)
    }

}