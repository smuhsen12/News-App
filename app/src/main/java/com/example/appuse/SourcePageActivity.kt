package com.example.appuse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appuse.Model.Sources
import org.jetbrains.anko.doAsync


class SourcePageActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    // declare vars
    private lateinit var recyclerView: RecyclerView

    lateinit var spinner: Spinner
    private lateinit var searchItem: String
    private lateinit var category: String
    private lateinit var newsManager: NewsManager
    private lateinit var newsSources: MutableList<Sources>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_source)

        // init news manager
        newsManager = NewsManager()

        // Code below adapted for spinner functionality:
        // https://www.tutorialspoint.com/how-to-get-spinner-value-in-kotlin
        spinner = findViewById(R.id.spinner)
        val adapter = ArrayAdapter.createFromResource(
            this, R.array.Categories, android.R.layout.simple_spinner_item
        )

        // set spinner adapter
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        // get strings from previous activity
        searchItem = intent.getStringExtra("search").toString()
        category = intent.getStringExtra("category").toString()
        recyclerView = findViewById(R.id.recyclerView)

        // load news initially
        loadNews(searchItem, category)

    }

    private fun loadNews(searchItem: String, category: String) {
        // Networking needs to be done on a background thread
        doAsync {
            // Use our TwitterManager to get Tweets from the Twitter API. If there is network
            // connection issues, the catch-block will fire and we'll show the user an error message.
            newsSources = try {
                // retrieve the news sources by category
                newsManager.retrieveNewsSourcesByCategory(category)
            } catch (exception: Exception) {
                // error handling
                Log.e("TweetsActivity", "Retrieving Tweets failed", exception)
                mutableListOf()
            }

            runOnUiThread {
                // set the adapter and recycler view
                if (newsSources.isNotEmpty()) {
                    val adapter = NewsAdapter(this@SourcePageActivity, newsSources, searchItem)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@SourcePageActivity)
                } else {
                    // show failure message
                    Toast.makeText(
                        this@SourcePageActivity,
                        "Failed to retrieve News!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    override fun onNothingSelected(parent: AdapterView<*>?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val text: String = parent?.getItemAtPosition(position).toString()
        // load news when selected item is changed.
        loadNews(searchItem, text)
        // show user selected item prompt, remove if not required
        Toast.makeText(
            this,
            getString(R.string.selected_item) + " " + text, Toast.LENGTH_SHORT
        ).show()
    }


}
