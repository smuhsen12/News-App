package com.example.appuse

import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appuse.Model.Article
import com.example.appuse.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import org.jetbrains.anko.doAsync

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private var currentAddress: Address? = null

    private lateinit var currentLocation: ImageButton
    private lateinit var confirm: MaterialButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var locText: TextView
    private lateinit var newsManager: NewsManager
    private lateinit var newsArticles: MutableList<Article>

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentLocation = findViewById(R.id.current_location)
        confirm = findViewById(R.id.confirm)
        recyclerView = findViewById(R.id.mapsRecyclerView)
        newsManager = NewsManager()
        locText = findViewById(R.id.newsAt)

        confirm.setOnClickListener {
            if (currentAddress != null) {

            }
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.setOnMapLongClickListener { coords: LatLng ->
            mMap.clear()

            doAsync {
                val geocoder: Geocoder = Geocoder(this@MapsActivity)
                val results: List<Address> = try {
                    geocoder.getFromLocation(
                        coords.latitude,
                        coords.longitude,
                        10
                    )
                } catch (exception: Exception) {
                    Log.e("MapsActivity", "Geocoding failed!", exception)
                    listOf()
                }

                runOnUiThread {
                    if (results.isNotEmpty()) {
                        val firstResult = results[0]
                        val addressLine = firstResult.getAddressLine(0)

                        val marker = MarkerOptions()
                            .position(coords)
                            .title(addressLine)

                        mMap.addMarker(marker)
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, 10.0f))

                        updateConfirmButton(firstResult)
                    } else {
                        Toast.makeText(this@MapsActivity, "No results found!", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    private fun loadNews(location: String) {
        // Networking needs to be done on a background thread
        doAsync {
            // Use our TwitterManager to get Tweets from the Twitter API. If there is network
            // connection issues, the catch-block will fire and we'll show the user an error message.
            // load news articles from API, by category
            newsArticles = try {
                newsManager.retrieveNewsByLocation(location)
            } catch (exception: Exception) {
                Log.e("HeadlinesActivity", "Retrieving Headlines failed", exception)
                mutableListOf()
            }

            runOnUiThread {
                // update recyclerview and show prompt
                if (newsArticles.isNotEmpty()) {
                    val adapter = HeadlineNewsAdapter(this@MapsActivity, newsArticles)
                    recyclerView.adapter = adapter
                    val linearLayoutManager = LinearLayoutManager(
                        this@MapsActivity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    recyclerView.layoutManager = linearLayoutManager
                    // keep track of total available number of pages that can be navigated
                } else {
                    // failure message
                    Toast.makeText(
                        this@MapsActivity,
                        "Failed to retrieve Headlines!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun updateConfirmButton(address: Address) {
        // Flip button to green
        // Change icon to check
        currentAddress = address
        confirm.icon = AppCompatResources.getDrawable(this, R.drawable.ic_check)
        confirm.text = address.getAddressLine(0)
        confirm.setBackgroundColor(getColor(R.color.buttonGreen))
        loadNews(address.adminArea)
        locText.setText("News At: " + address.adminArea)
        Toast.makeText(
            this@MapsActivity,
            "News at: " + address.adminArea,
            Toast.LENGTH_SHORT
        ).show()
    }
}