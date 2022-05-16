package  com.example.appuse

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class Search : AppCompatActivity() {

    // Search Bar
    private lateinit var searchEditText: EditText

    // Search
    private lateinit var searchButton: Button

    // View Map
    private lateinit var mapButton: Button

    // Top Headlines
    private lateinit var headlinesButton: Button

    // shared prefs
    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        // init shared prefs
        sharedPreference = getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)
        editor = sharedPreference.edit()

        // get previously searched term from shared prefs
        val previousSearch = sharedPreference.getString("searchtext", "")

        // Locates matching elements
        searchEditText = findViewById(R.id.search)
        // set text to previously searched term
        searchEditText.setText(previousSearch.toString())
        searchEditText.addTextChangedListener(textWatcher)
        searchButton = findViewById(R.id.searchButton)
        mapButton = findViewById(R.id.mapButton)
        headlinesButton = findViewById(R.id.headlinesButton)

        searchButton.isEnabled = false

        //Button to view source page
        searchButton.setOnClickListener { view: View ->
            saveSharedPrefs()
            val intent = Intent(this, SourcePageActivity::class.java)
            val searchVal = searchEditText.text
            intent.putExtra("search", searchVal.toString())
            intent.putExtra("category", "everything")
            startActivity(intent)
        }

        // View Map
        mapButton.setOnClickListener { view: View ->
            // An Intent is used to start a new Activity.
            // 1st param == a "Context" which is a reference point into the Android system. All Activities are Contexts by inheritance.
            // 2nd param == the Class-type of the Activity you want to navigate to.
            val intent = Intent(this, MapsActivity::class.java)
            // An Intent can also be used like a Map (key-value pairs) to pass data between Activities.
            // intent.putExtra("LOCATION", "Washington D.C.")
            // "Executes" our Intent to start a new Activity
            startActivity(intent)
        }

        headlinesButton.setOnClickListener { view: View ->
            // launch headlines activity
            val intent = Intent(this, HeadlinesActivity::class.java)
            startActivity(intent)
        }
    }

    private val textWatcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        // We can use any of the three functions -- here, we just use onTextChanged -- the goal
        // is the enable the login button only if there is text in both the username & password fields.
        override fun onTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            // Kotlin shorthand for username.getText().toString()
            // .toString() is needed because getText() returns an Editable (basically a char array).
            val inputtedSearch: String = searchEditText.text.toString()
            val enableButton: Boolean =
                inputtedSearch.isNotBlank()

            // Kotlin shorthand for login.setEnabled(enableButton)
            searchButton.isEnabled = enableButton
        }

        override fun afterTextChanged(p0: Editable?) {}

    }

    override fun onBackPressed() {
        super.onBackPressed()
        // save updated text on back pressed
        saveSharedPrefs()
    }

    private fun saveSharedPrefs() {
        editor.putString("searchtext", searchEditText.text.toString())
        editor.apply()
    }
}