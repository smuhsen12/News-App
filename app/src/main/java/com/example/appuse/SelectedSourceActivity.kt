package com.example.appuse

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appuse.Model.Article
import org.jetbrains.anko.doAsync

class SelectedSourceActivity : AppCompatActivity() {
    // declare vars
    lateinit var textView: TextView
    private lateinit var recyclerView: RecyclerView

    private lateinit var source: String
    private lateinit var search: String
    private lateinit var newsManager: NewsManager
    private lateinit var newsArticles: MutableList<Article>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.selected_source_news)
        // init vars
        newsManager = NewsManager()
        recyclerView = findViewById(R.id.recyclerViewTopNews)
        textView = findViewById(R.id.newsBySource)
        // get the strings from previous activity
        source = intent.getStringExtra("source").toString()
        search = intent.getStringExtra("search").toString()
        // show the user source and search term
        textView.setText("News By: "+ source + " for " + search)
        // load the news
        loadNews(search, source)
    }

    private fun loadNews(search: String, source: String) {
        // Networking needs to be done on a background thread
        doAsync {
            // Use our TwitterManager to get Tweets from the Twitter API. If there is network
            // connection issues, the catch-block will fire and we'll show the user an error message.
            newsArticles = try {
                newsManager.retrieveNewsBySource(search, source)
            } catch (exception: Exception) {
                Log.e("HeadlinesActivity", "Retrieving Headlines failed", exception)
                mutableListOf()
            }

            runOnUiThread {
                if (newsArticles.isNotEmpty()) {
                    // using the same news adapter to list the news articles
                    val adapter = HeadlineNewsAdapter(this@SelectedSourceActivity, newsArticles)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@SelectedSourceActivity)
                } else {
                    // show failure message
                    Toast.makeText(
                        this@SelectedSourceActivity,
                        "Failed to retrieve News!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}