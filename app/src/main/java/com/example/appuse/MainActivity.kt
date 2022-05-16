package com.example.appuse

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {
    //TODO this activity is redundant, should be removed
    //declare vars
    lateinit var textView: TextView

    // dropdown
    lateinit var dropDown: Spinner
    lateinit var sourcesRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        textView = findViewById(R.id.searchedTerm)
        dropDown = findViewById(R.id.dropdownCategory)
        sourcesRecyclerView = findViewById(R.id.recyclerViewNewsList)

        val text = intent.getStringExtra("data")
        Toast.makeText(this, text.toString(), Toast.LENGTH_LONG).show()
        textView.text = text
        // access the items of the list
        val languages = resources.getStringArray(R.array.Categories)

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, languages
        )
        dropDown.adapter = adapter

        dropDown.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View, position: Int, id: Long
            ) {
                Toast.makeText(
                    this@MainActivity,
                    getString(R.string.selected_item) + " " +
                            "" + languages[position], Toast.LENGTH_SHORT
                ).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // write code to perform some action
            }
        }
    }

}