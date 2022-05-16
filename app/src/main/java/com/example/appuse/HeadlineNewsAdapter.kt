package com.example.appuse

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.appuse.Model.Article
import com.squareup.picasso.Picasso


class HeadlineNewsAdapter(private val mContext: Context, val newsArticles: List<Article>) :
    RecyclerView.Adapter<HeadlineNewsAdapter.ViewHolder>() {
    // A ViewHolder represents the Views that comprise a single row in our list (e.g.
    // our row to display a Tweet contains three TextViews and one ImageView).
    // The "rootLayout" passed into the constructor comes from onCreateViewHolder. From the root layout, we can
    // call findViewById to search through the hierarchy to find the Views we care about in our new row.
    class ViewHolder(rootLayout: View) : RecyclerView.ViewHolder(rootLayout) {
        val headlineText: TextView = rootLayout.findViewById(R.id.headline)
        val contentText: TextView = rootLayout.findViewById(R.id.news_content_top)
        val image: ImageView = rootLayout.findViewById(R.id.imageHeadline)
        //val icon: ImageView = rootLayout.findViewById(R.id.icon)
    }

    // The RecyclerView needs a "fresh" / new row, so we need to:
    // 1. Read in the XML file for the row type
    // 2. Use the new row to build a ViewHolder to return
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // A LayoutInflater is an object that knows how to read & parse an XML file
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)

        // Read & parse the XML file to create a new row at runtime
        // The 'inflate' function returns a reference to the root layout (the "top" view in the hierarchy) in our newly created row
        val rootLayout: View = layoutInflater.inflate(R.layout.top_news_source_item, parent, false)

        // We can now create a ViewHolder from the root view
        val viewHolder = ViewHolder(rootLayout)
        return viewHolder
    }


    // The RecyclerView is ready to display a new (or recycled) row on the screen, represented a our ViewHolder.
    // We're given the row position / index that needs to be rendered.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentNews = newsArticles[position]
        // update text views
        holder.headlineText.setText(currentNews.source?.name ?: " ")
        holder.contentText.setText(currentNews.content)
        // load image into the image view
        if (currentNews.urlToImage?.isNotEmpty() == true) {
            Picasso
                .get()
                .load(currentNews.urlToImage)
                .into(holder.image)
        }
        // set on item click listener to open browser when tapped
        holder.itemView.setOnClickListener {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(currentNews.url)
            mContext.startActivity(i)
        }
    }


    // How many rows (total) do you want the adapter to render?
    override fun getItemCount(): Int {
        return newsArticles.size
    }
}