package com.dreamsong.app

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import org.json.JSONObject
import java.net.URL
import java.net.URLEncoder
import java.util.concurrent.Executors
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class MainActivity : Activity() {

    private val executor = Executors.newSingleThreadExecutor()
    private val handler = Handler(Looper.getMainLooper())
    private var searchTask = 0
    private lateinit var resultsContainer: LinearLayout
    private lateinit var player: ExoPlayer
    private lateinit var miniPlayer: LinearLayout
    private lateinit var nowPlayingText: TextView
    private lateinit var playPauseButton: TextView
    private data class Song(
    val title: String,
    val artist: String,
    val previewUrl: String
)
    private val bg = Color.rgb(13, 15, 22)
    private val panel = Color.rgb(25, 29, 40)
    private val blue = Color.rgb(65, 135, 255)
    private val white = Color.WHITE
    private val gray = Color.rgb(155, 163, 180)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        player = ExoPlayer.Builder(this).build()

        window.statusBarColor = bg
        window.navigationBarColor = bg

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(24))
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val brand = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        brand.addView(label("DREAMSONG", 23, white, true))
        brand.addView(label("YOUR MUSIC, YOUR WORLD", 10, gray))

        header.addView(brand, LinearLayout.LayoutParams(0, -2, 1f))

        val avatar = label("♪", 25, white, true).apply {
            gravity = Gravity.CENTER
            background = rounded(blue, 50)
        }
        header.addView(avatar, LinearLayout.LayoutParams(dp(46), dp(46)))

        content.addView(header)
        content.addView(space(28))

        content.addView(label("ค้นหาเพลงที่ใช่", 25, white, true))
        content.addView(label("ค้นพบเสียงเพลงในแบบของคุณ", 14, gray))
        content.addView(space(16))

        val search = EditText(this).apply {
            hint = "ค้นหาชื่อเพลง หรือศิลปิน..."
            setHintTextColor(gray)
            setTextColor(white)
            textSize = 14f
            setSingleLine(true)
            setPadding(dp(16), 0, dp(16), 0)
            background = rounded(panel, 16)
        }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(52)))
search.addTextChangedListener(object : TextWatcher {
    override fun beforeTextChanged(
        s: CharSequence?, start: Int, count: Int, after: Int
    ) {}

    override fun onTextChanged(
        s: CharSequence?, start: Int, before: Int, count: Int
    ) {}


  override fun afterTextChanged(s: Editable?) {
        val query = s?.toString()?.trim().orEmpty()
        if (query.length >= 2) {
            searchSongs(query)
        }
    }
})
resultsContainer = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
}
content.addView(resultsContainer)

        content.addView(space(28))
        content.addView(label("แนะนำสำหรับคุณ", 20, white, true))
        content.addView(space(14))

        val feature = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.rgb(28, 79, 160),
                    Color.rgb(24, 34, 65),
                    Color.rgb(25, 29, 40)
                )
            ).apply { cornerRadius = dp(20).toFloat() }
        }

        feature.addView(label("DREAMSONG PLAYLIST", 11, Color.LTGRAY, true))
        feature.addView(space(12))
        feature.addView(label("เสียงเพลงที่เป็นคุณ", 23, white, true))
        feature.addView(label("เลือกเพลงโปรด แล้วสร้างโลกของคุณ", 13, Color.LTGRAY))
        feature.addView(space(18))

        val explore = label("ค้นพบเพลง  →", 14, white, true).apply {
            gravity = Gravity.CENTER
            background = rounded(blue, 12)
        }
        feature.addView(explore, LinearLayout.LayoutParams(dp(145), dp(42)))

        content.addView(feature)
        content.addView(space(28))

        content.addView(label("หมวดหมู่", 20, white, true))
        content.addView(space(12))

        val categories = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        listOf("ทั้งหมด", "Pop", "Rock", "Chill", "Electronic").forEachIndexed { index, name ->
            val chip = label(name, 13, if (index == 0) white else gray, index == 0).apply {
                gravity = Gravity.CENTER
                setPadding(dp(17), dp(10), dp(17), dp(10))
                background = rounded(if (index == 0) blue else panel, 30)
            }
            val lp = LinearLayout.LayoutParams(-2, dp(40))
            lp.marginEnd = dp(9)
            categories.addView(chip, lp)
        }

        val categoryScroll = HorizontalScrollView(this).apply {
            isHorizontalScrollBarEnabled = false
            addView(categories)
        }
        content.addView(categoryScroll)
        content.addView(space(28))

        content.addView(label("เพลงยอดนิยม", 20, white, true))
        content.addView(space(14))

        val songs = listOf(
            Triple("Midnight Dreams", "Dream Artist", "03:42"),
            Triple("Blue Horizon", "Sky Music", "04:15"),
            Triple("Lost in the Night", "Moonlight", "03:28"),
            Triple("Ocean Memories", "Wave Studio", "04:02")
        )

        songs.forEachIndexed { index, song ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(8), 0, dp(8))
            }

            val cover = label("♫", 23, white, true).apply {
                gravity = Gravity.CENTER
                background = rounded(
                    if (index % 2 == 0) Color.rgb(38, 65, 110)
                    else Color.rgb(65, 48, 105), 13
                )
            }
            row.addView(cover, LinearLayout.LayoutParams(dp(54), dp(54)))

            val info = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(13), 0, 0, 0)
            }
            info.addView(label(song.first, 15, white, true))
            info.addView(space(4))
            info.addView(label(song.second, 12, gray))

            row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))

            content.addView(row)
        }

        content.addView(space(20))
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val bottom = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.rgb(19, 22, 31))
        }

        listOf("⌂\nหน้าหลัก", "⌕\nค้นหา", "♫\nคลังเพลง", "☻\nโปรไฟล์").forEachIndexed { index, name ->
            val item = label(name, 12, if (index == 0) blue else gray, index == 0).apply {
                gravity = Gravity.CENTER
            }
            bottom.addView(item, LinearLayout.LayoutParams(0, dp(55), 1f))
        }

miniPlayer = LinearLayout(this).apply {
    orientation = LinearLayout.HORIZONTAL
    gravity = Gravity.CENTER_VERTICAL
    setPadding(dp(16), dp(10), dp(16), dp(10))
    background = rounded(panel, 16)
}

nowPlayingText = label("ยังไม่ได้เลือกเพลง", 13, white, true)

miniPlayer.addView(
    nowPlayingText,
    LinearLayout.LayoutParams(0, dp(44), 1f)
)

playPauseButton = label("▶", 22, blue, true).apply {
    gravity = Gravity.CENTER
    setOnClickListener {
        if (player.isPlaying) {
            player.pause()
            text = "▶"
        } else {
            player.play()
            text = "Ⅱ"
        }
    }
}

miniPlayer.addView(
    playPauseButton,
    LinearLayout.LayoutParams(dp(48), dp(44))
)

root.addView(miniPlayer)
root.addView(bottom)
        setContentView(root)
    }

private fun searchSongs(query: String) {
    val task = ++searchTask

    if (query.isBlank()) {
        resultsContainer.removeAllViews()
        return
    }

    executor.execute {
        try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = URL("https://itunes.apple.com/search?term=$encoded&entity=song&limit=20")

            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val json = JSONObject(
                connection.getInputStream().bufferedReader().use { it.readText() }
            )

            val results = json.getJSONArray("results")
            val songList = mutableListOf<Song>()

            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)
                songList.add(
    Song(
        item.optString("trackName"),
        item.optString("artistName"),
        item.optString("previewUrl")
    )
)
}
            handler.post {
                if (task == searchTask) {
                    resultsContainer.removeAllViews()

                    if (songList.isEmpty()) {
                        resultsContainer.addView(label("ไม่พบเพลงที่ค้นหา", 14, gray))
                    }

                    songList.forEachIndexed { index, song ->
                        val row = LinearLayout(this).apply {
                            orientation = LinearLayout.HORIZONTAL
                            gravity = Gravity.CENTER_VERTICAL
                            setPadding(dp(12), dp(12), dp(12), dp(12))
                            background = rounded(panel, 12)
                        }

                        row.addView(
                            label("${index + 1}", 16, blue, true),
                            LinearLayout.LayoutParams(dp(30), -2)
                        )

                        val info = LinearLayout(this).apply {
                            orientation = LinearLayout.VERTICAL
                        }

                        info.addView(label(song.title, 14, white, true))
                        info.addView(label(song.artist, 12, gray))

                        row.addView(info, LinearLayout.LayoutParams(0, -2, 1f))

                        row.setOnClickListener {
                            if (song.previewUrl.isBlank()) {
                                Toast.makeText(this, "เพลงนี้ไม่มีเสียง Preview", Toast.LENGTH_SHORT).show()
                            } else {
                                player.setMediaItem(MediaItem.fromUri(song.previewUrl))
                                player.prepare()
                                player.play()
                                nowPlayingText.text = "${song.title} — ${song.artist}"
                                playPauseButton.text = "Ⅱ"
                            }
                        }

                        val params = LinearLayout.LayoutParams(-1, -2)
                        params.bottomMargin = dp(8)
                        resultsContainer.addView(row, params)
                    }
                }
            }


        } catch (e: Exception) {
            handler.post {
                if (task == searchTask) {
                    Toast.makeText(
                        this,
                        "ค้นหาไม่สำเร็จ: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}

    private fun label(
        text: String,
        size: Int,
        color: Int,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size.toFloat()
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun space(height: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(height))
        }
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }
}
