package com.example.appuse

import android.util.Base64
import android.util.Log
import com.example.appuse.Model.Article
import com.example.appuse.Model.Constants
import com.example.appuse.Model.Source
import com.example.appuse.Model.Sources
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject

class NewsManager {


    private val okHttpClient: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()

        // This will cause all network traffic to be logged to the console for easy debugging
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(loggingInterceptor)

        okHttpClient = builder.build()
    }

    fun retrieveOAuthToken(apiKey: String, apiSecret: String): String {
        val concatenatedSecrets = "$apiKey:$apiSecret"
        val base64Encoded = Base64.encodeToString(concatenatedSecrets.toByteArray(), Base64.NO_WRAP)

        val requestBody = "grant_type=client_credentials".toRequestBody(
            contentType = "application/x-www-form-urlencoded".toMediaType()
        )

        val request = Request.Builder()
            .url("https://api.twitter.com/oauth2/token")
            .header("Authorization", "Basic $base64Encoded")
            .post(requestBody)
            .build()

        val response = okHttpClient.newCall(request).execute()
        val responseBody = response.body?.string()

        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            val json = JSONObject(responseBody)
            return json.getString("access_token")
        }

        return ""
    }

    fun retrieveNewsByLocation(location: String): MutableList<Article> {
        // Form the Search Tweets request per the docs at: https://developer.twitter.com/en/docs/tweets/search/api-reference/get-search-tweets.html
        // The "Authorization" header here is similar to an API Key... we'll see with Lecture 7.
        val request: Request = Request.Builder()
            .url("https://newsapi.org/v2/top-headlines?q=$location&apiKey=${Constants.NEWS_API_KEY}")
            .get()
            .build()
        // This executes the request and waits for a response from the server
        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        // The .isSuccessful checks to see if the status code is 200-299
        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            val newsArticle = mutableListOf<Article>()
            // Parse our way through the JSON hierarchy, picking out what we need from each Tweet
            val json: JSONObject = JSONObject(responseBody)
            val statuses: JSONArray = json.getJSONArray("articles")

            for (i in 0 until statuses.length()) {
                val curr: JSONObject = statuses.getJSONObject(i)
                val author: String = curr.getString("author")
                val title: String = curr.getString("title")
                val description = curr.getString("description")
                val url = curr.getString("url")
                val urlToImage = curr.getString("urlToImage")
                val publishedAt = curr.getString("publishedAt")
                val content = curr.getString("content")
                val source = curr.getJSONObject("source")
                val id = source.getString("id")
                val name = source.getString("name")

                /**
                 * author: "/auteur/369778-thibaut-keutchayan.html",
                title: "XPeng P5 : la très prometteuse voiture électrique arrive enfin en Europe",
                description: "C'est officiel, le modèle P5 de XPeng, une berline 100 % électrique
                , s'ouvre à la réservation dans quatre pays européens.",
                url: "https://www.clubic.com/mag/transports/actualite-413229-xpeng-p5-la-tres-prometteuse-voiture-electrique-arrive-enfin-en-europe.html",
                urlToImage: "https://pic.clubic.com/v1/images/1983456/raw",
                publishedAt: "2022-03-13T16:15:00Z",
                content: "C'est officiel, le modèle P5 de XPeng, une berline 100 % électrique, s'ouvre à la réservation dans quatre pays européens.
                Après son SUV G9, XPeng compte bien poursuivre son implantation sur le Vieu… [+2436 chars]"
                 */

                val sourceObj = Source(
                    id = id,
                    name = name
                )
                val newsArticleObj = Article(
                    author = author,
                    content = content,
                    description = description,
                    publishedAt = publishedAt,
                    source = sourceObj,
                    title = title,
                    url = url,
                    urlToImage = urlToImage
                )
                newsArticle.add(newsArticleObj)
            }

            return newsArticle
        }

        return mutableListOf()
    }

    fun retrieveNewsSourcesByCategory(type: String): MutableList<Sources> {
        // Form the Search Tweets request per the docs at: https://developer.twitter.com/en/docs/tweets/search/api-reference/get-search-tweets.html
        // The "Authorization" header here is similar to an API Key... we'll see with Lecture 7.
        val request: Request = Request.Builder()
            .url("https://newsapi.org/v2/top-headlines/sources?country=us&category=$type&apiKey=${Constants.NEWS_API_KEY}")
            .get()
            .build()

        // This executes the request and waits for a response from the server
        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        // The .isSuccessful checks to see if the status code is 200-299
        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            val newsSources = mutableListOf<Sources>()
            // Parse our way through the JSON hierarchy, picking out what we need from each Tweet
            val json: JSONObject = JSONObject(responseBody)
            val statuses: JSONArray = json.getJSONArray("sources")

            for (i in 0 until statuses.length()) {
                val curr: JSONObject = statuses.getJSONObject(i)
                val id: String = curr.getString("id")
                val name: String = curr.getString("name")
                val description = curr.getString("description")
                val url = curr.getString("url")
                val category = curr.getString("category")
                val language = curr.getString("language")
                val country = curr.getString("country")

                val sourcesObj = Sources(
                    id = id,
                    name = name,
                    description = description,
                    url = url,
                    language = language,
                    category = category,
                    country = country
                )
                newsSources.add(sourcesObj)
            }

            return newsSources
        }

        return mutableListOf()
    }

    fun retrieveTopHeadlinesByCategory(page: String, type: String): MutableList<Article> {
        // Form the Search Tweets request per the docs at: https://developer.twitter.com/en/docs/tweets/search/api-reference/get-search-tweets.html
        // The "Authorization" header here is similar to an API Key... we'll see with Lecture 7.
        val request: Request = Request.Builder()
            .url("https://newsapi.org/v2/top-headlines?country=us&category=${type}&page=${page}&apiKey=${Constants.NEWS_API_KEY}")
            //.header("Authorization", "Bearer $oAuthToken")
            .get()
            .build()

        // This executes the request and waits for a response from the server
        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        // The .isSuccessful checks to see if the status code is 200-299
        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            val newsArticle = mutableListOf<Article>()
            // Parse our way through the JSON hierarchy, picking out what we need from each Tweet
            val json: JSONObject = JSONObject(responseBody)
            val statuses: JSONArray = json.getJSONArray("articles")
            val noOfarticles: String = json.getString("totalResults")

            Log.d("TOTAL ", noOfarticles + " ARTICLES")

            for (i in 0 until statuses.length()) {
                val curr: JSONObject = statuses.getJSONObject(i)
                val author: String = curr.getString("author")
                val title: String = curr.getString("title")
                val description = curr.getString("description")
                val url = curr.getString("url")
                val urlToImage = curr.getString("urlToImage")
                val publishedAt = curr.getString("publishedAt")
                val content = curr.getString("content")
                val source = curr.getJSONObject("source")
                val id = source.getString("id")
                val name = source.getString("name")

                val sourceObj = Source(
                    id = id,
                    name = name
                )
                val newsArticleObj = Article(
                    author = author,
                    content = content,
                    description = description,
                    publishedAt = publishedAt,
                    source = sourceObj,
                    title = title,
                    url = url,
                    urlToImage = urlToImage
                )
                newsArticle.add(newsArticleObj)
            }

            return newsArticle
        }

        return mutableListOf()
    }

    fun retrieveNewsBySource(search: String, source: String): MutableList<Article> {
        // Form the Search Tweets request per the docs at: https://developer.twitter.com/en/docs/tweets/search/api-reference/get-search-tweets.html
        // The "Authorization" header here is similar to an API Key... we'll see with Lecture 7.
        val request: Request = Request.Builder()
            .url("https://newsapi.org/v2/everything?q=${search}&sources=${source}&apiKey=${Constants.NEWS_API_KEY}")
            .get()
            .build()

        // This executes the request and waits for a response from the server
        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        // The .isSuccessful checks to see if the status code is 200-299
        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            val newsArticle = mutableListOf<Article>()
            // Parse our way through the JSON hierarchy, picking out what we need from each Tweet
            val json: JSONObject = JSONObject(responseBody)
            val statuses: JSONArray = json.getJSONArray("articles")
            val noOfarticles: String = json.getString("totalResults")

            Log.d("TOTAL ", noOfarticles + " ARTICLES")

            for (i in 0 until statuses.length()) {
                val curr: JSONObject = statuses.getJSONObject(i)
                val author: String = curr.getString("author")
                val title: String = curr.getString("title")
                val description = curr.getString("description")
                val url = curr.getString("url")
                val urlToImage = curr.getString("urlToImage")
                val publishedAt = curr.getString("publishedAt")
                val content = curr.getString("content")
                val source1 = curr.getJSONObject("source")
                val id = source1.getString("id")
                val name = source1.getString("name")

                val sourceObj = Source(
                    id = id,
                    name = name
                )
                val newsArticleObj = Article(
                    author = author,
                    content = content,
                    description = description,
                    publishedAt = publishedAt,
                    source = sourceObj,
                    title = title,
                    url = url,
                    urlToImage = urlToImage
                )
                newsArticle.add(newsArticleObj)
            }
            return newsArticle
        }
        return mutableListOf()
    }

    fun retrieveTotalNumberOfTopHeadlinesByCategory(type: String): Int {
        // Form the Search Tweets request per the docs at: https://developer.twitter.com/en/docs/tweets/search/api-reference/get-search-tweets.html
        // The "Authorization" header here is similar to an API Key... we'll see with Lecture 7.
        val request: Request = Request.Builder()
            .url("https://newsapi.org/v2/top-headlines?country=us&category=${type}&apiKey=${Constants.NEWS_API_KEY}")
            //.header("Authorization", "Bearer $oAuthToken")
            .get()
            .build()

        // This executes the request and waits for a response from the server
        val response: Response = okHttpClient.newCall(request).execute()
        val responseBody: String? = response.body?.string()

        // The .isSuccessful checks to see if the status code is 200-299
        if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
            // Parse our way through the JSON hierarchy, picking out what we need from each Tweet
            val json: JSONObject = JSONObject(responseBody)
            val statuses: JSONArray = json.getJSONArray("articles")
            val numberOfArticles: String = json.getString("totalResults")

            Log.d("TOTAL ", numberOfArticles + " ARTICLES")

            return numberOfArticles.toInt()
        }
        return 0
    }

}