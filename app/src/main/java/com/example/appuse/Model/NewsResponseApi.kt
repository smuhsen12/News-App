package com.example.appuse.Model

//NewsResponse Object for Api

data class NewsResponseApi (
val articles: MutableList<Article>,
val status: String,
val totalResults: Int
)