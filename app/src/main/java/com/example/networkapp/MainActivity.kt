package com.example.networkapp

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONObject
import java.io.File

// TODO (1: Fix any bugs)
// TODO (2: Add function saveComic(...) to save comic info when downloaded
// TODO (3: Automatically load previously saved comic when app starts)

lateinit var file : File
class MainActivity : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    lateinit var titleTextView: TextView
    lateinit var descriptionTextView: TextView
    lateinit var numberEditText: EditText
    lateinit var showButton: Button
    lateinit var comicImageView: ImageView

    companion object {
        private const val PREFS_NAME = "ComicsPrefs"
        private const val KEY_TITLE = "title"
        private const val KEY_ALT = "alt"
        private const val KEY_IMG = "img"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestQueue = Volley.newRequestQueue(this)

        titleTextView = findViewById<TextView>(R.id.comicTitleTextView)
        descriptionTextView = findViewById<TextView>(R.id.comicDescriptionTextView)
        numberEditText = findViewById<EditText>(R.id.comicNumberEditText)
        showButton = findViewById<Button>(R.id.showComicButton)
        comicImageView = findViewById<ImageView>(R.id.comicImageView)

        loadSavedComic()

        showButton.setOnClickListener {
            downloadComic(numberEditText.text.toString())
        }

    }

    // Fetches comic from web as JSONObject
    private fun downloadComic (comicId: String) {
        val id = comicId.trim()

        if (id.isEmpty()) {
            Toast.makeText(this, "Enter comic Number", Toast.LENGTH_SHORT).show()
            return
        }
        val url = "https://xkcd.com/$comicId/info.0.json"
        requestQueue.add (
            JsonObjectRequest(
                Request.Method.GET,
                url,
                null
                , { response ->
                    showComic(response)
                  saveComic(response)
                  }, { error ->
                      Toast.makeText(this, "Comic not found: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        )
    }

    // Display a comic for a given comic JSON object
    private fun showComic (comicObject: JSONObject) {
        titleTextView.text = comicObject.optString("title")
        descriptionTextView.text = comicObject.optString("alt")
        Picasso.get().load(comicObject.optString("img")).into(comicImageView)

        val imageURL = comicObject.optString("img", "")
        if (imageURL.isNotEmpty()) {
            Picasso.get().load(imageURL).into(comicImageView)
        }
    }

    // Implement this function
    private fun saveComic(comicObject: JSONObject) {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        prefs.edit()
            .putString(KEY_TITLE, comicObject.optString("title"))
            .putString(KEY_ALT, comicObject.optString("alt"))
            .putString(KEY_IMG, comicObject.optString("img"))
            .apply()
    }
    private fun loadSavedComic() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedTitle = prefs.getString(KEY_TITLE, null)
        val savedAlt = prefs.getString(KEY_ALT, null)
        val savedImg = prefs.getString(KEY_IMG, null)

        if (savedTitle == null && savedAlt == null && savedImg == null) {
            return
        }

        val savedComic = JSONObject()
        savedComic.put("title", savedTitle ?: "")
        savedComic.put("alt", savedAlt ?: "")
        savedComic.put("img", savedImg ?: "")

        showComic(savedComic)
    }
}