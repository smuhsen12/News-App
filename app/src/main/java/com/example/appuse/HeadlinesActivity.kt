package com.example.appuse

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appuse.Model.Article
import org.jetbrains.anko.doAsync


class HeadlinesActivity : AppCompatActivity() {
    // declare vars
    lateinit var prevButton: Button
    lateinit var nextButton: Button
    lateinit var textView: TextView
    lateinit var pageIndicatorText: TextView
    private lateinit var recyclerView: RecyclerView

    // data vars
    private lateinit var category: String
    private lateinit var newsManager: NewsManager
    private lateinit var newsArticles: MutableList<Article>

    // dropdown
    lateinit var dropDown: Spinner

    // to keep track of pages
    var currentPage: Int = 1
    var totalNewsItems: Int = 0
    var numberOfPages: Int = 0

    // for data persistence
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_headlines)
        // init shared prefs
        sharedPreference = getSharedPreferences("PREFS", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()
        // set initial page indicator to 1
        currentPage = 1
        newsManager = NewsManager()
        // init views
        dropDown = findViewById(R.id.dropdownCategory)
        recyclerView = findViewById(R.id.recyclerViewTopNews)
        prevButton = findViewById(R.id.prevButton)
        nextButton = findViewById(R.id.nextButton)
        pageIndicatorText = findViewById(R.id.pageNumber)

        // set click listener for previous page button
        prevButton.setOnClickListener {
            if (currentPage > 1) {
                // update index and show on screen
                currentPage--
                pageIndicatorText.setText(currentPage.toString() + "/" + numberOfPages.toString())
            }
            nextButton.isEnabled = true // enable next button in case not already
            if (currentPage == 1) {
                // if current page is the first page, previous button should be disabled
                prevButton.isEnabled = false
            }
            // load news at the current page index, and choosen category
            loadNews(currentPage.toString(), category)
        }
        // set click listener for next page button
        nextButton.setOnClickListener(View.OnClickListener {
            if (currentPage < numberOfPages) {
                // update index and show on screen
                currentPage++
                pageIndicatorText.setText(currentPage.toString() + "/" + numberOfPages.toString())
            }
            // enable previous button in case not already
            prevButton.isEnabled = true
            if (currentPage == numberOfPages) {
                // if current page is the last page, next button should be disabled
                nextButton.isEnabled = false
            }
            // load news at the current page index, and choosen category
            loadNews(currentPage.toString(), category)
        })

        // disable the previous button when the page index is 1 initially
        prevButton.isEnabled = false

        // init dropdown items
        val categories = resources.getStringArray(R.array.Categories)
        // assign dropdown previously selected item, for data persistence
        category = sharedPreference.getString("category", categories[0]).toString()
        // show prompt for selected category
        Toast.makeText(
            this@HeadlinesActivity,
            getString(R.string.selected_item) + " " +
                    "" + category, Toast.LENGTH_SHORT
        ).show()
        // attach adapter and make selection based on previously selected item
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item, categories
        )
        dropDown.adapter = adapter
        dropDown.setSelection(categories.indexOf(category))

        // dropdown item selected listener
        dropDown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                // update category, and page index reset
                category = categories[position]
                currentPage = 1
                // reset prev, next button
                prevButton.isEnabled = false
                nextButton.isEnabled = true
                // load news for selected category
                loadNews(currentPage.toString(), categories[position])
                // show selected category prompt
                Toast.makeText(
                    this@HeadlinesActivity,
                    getString(R.string.selected_item) + " " +
                            "" + categories[position], Toast.LENGTH_SHORT
                ).show()
                // update the shared prefs for selected category
                editor.putString("category", dropDown.selectedItem.toString())
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
        // load news initially
        loadNews(currentPage.toString(), category)
    }

    private fun loadNews(currentPg: String, category: String) {
        // Networking needs to be done on a background thread
        doAsync {
            // Use our TwitterManager to get Tweets from the Twitter API. If there is network
            // connection issues, the catch-block will fire and we'll show the user an error message.
            // load news articles from API, by category
            newsArticles = try {
                newsManager.retrieveTopHeadlinesByCategory(currentPg, category)
            } catch (exception: Exception) {
                Log.e("HeadlinesActivity", "Retrieving Headlines failed", exception)
                mutableListOf()
            }
            // load total number of news items to be able to know the total pages that can be loaded
            totalNewsItems = try {
                newsManager.retrieveTotalNumberOfTopHeadlinesByCategory(category)
            } catch (exception: Exception) {
                Log.e("HeadlinesActivity", "Retrieving Headlines failed", exception)
                0
            }

            runOnUiThread {
                // update recyclerview and show prompt
                if (newsArticles.isNotEmpty()) {
                    val adapter = HeadlineNewsAdapter(this@HeadlinesActivity, newsArticles)
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(this@HeadlinesActivity)
                    // keep track of total available number of pages that can be navigated
                    numberOfPages = (totalNewsItems / 20) + 1
                    // next button disable when only one page is available from API fetch
                    nextButton.isEnabled = currentPage != numberOfPages
                    pageIndicatorText.setText(currentPage.toString() + "/" + numberOfPages.toString())
                } else {
                    // failure message
                    Toast.makeText(
                        this@HeadlinesActivity,
                        "Failed to retrieve Headlines!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}