package com.example.game_vol1

import android.graphics.BitmapFactory
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.example.game_vol1.models.HeritagePlace
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors

object PlaceImageLoader {
    private const val TAG = "PlaceImageLoader"
    private val executor = Executors.newFixedThreadPool(3)

    fun loadInto(imageView: ImageView, place: HeritagePlace) {
        val requestKey = place.id
        imageView.tag = requestKey
        imageView.visibility = View.VISIBLE
        imageView.setImageDrawable(null)
        imageView.setBackgroundColor(0x2238BDF8)
        imageView.contentDescription = "${place.title} photo"

        executor.execute {
            val bitmap = runCatching {
                val imageUrl = place.imageUrl.ifBlank { wikipediaThumbnail(place.wikipediaTitle) }
                if (imageUrl.isBlank()) return@runCatching null
                downloadBitmap(imageUrl)
            }.onFailure {
                Log.w(TAG, "Could not load image for ${place.title}.", it)
            }.getOrNull()

            imageView.post {
                if (imageView.tag != requestKey) return@post
                if (bitmap != null) {
                    imageView.setBackgroundColor(0x00000000)
                    imageView.setImageBitmap(bitmap)
                }
            }
        }
    }

    private fun wikipediaThumbnail(title: String): String {
        if (title.isBlank()) return ""
        val encoded = URLEncoder.encode(title, "UTF-8").replace("+", "%20")
        val connection = URL("https://en.wikipedia.org/api/rest_v1/page/summary/$encoded")
            .openConnection() as HttpURLConnection
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.requestMethod = "GET"
        connection.setRequestProperty("User-Agent", "HeritageHuntAndroid/1.0")
        return connection.inputStream.bufferedReader().use { reader ->
            val json = JSONObject(reader.readText())
            json.optJSONObject("thumbnail")
                ?.optString("source")
                ?.replace("http://", "https://")
                .orEmpty()
        }
    }

    private fun downloadBitmap(imageUrl: String) =
        (URL(imageUrl).openConnection() as HttpURLConnection).run {
            connectTimeout = 5000
            readTimeout = 5000
            requestMethod = "GET"
            inputStream.use(BitmapFactory::decodeStream)
        }
}
