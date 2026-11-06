package com.darknes.os

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private val gold = Color.rgb(224, 183, 91)
    private val apps = mutableListOf<android.content.pm.ResolveInfo>()
    private lateinit var appGrid: GridLayout
    private lateinit var search: EditText
    private lateinit var clock: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(9, 12, 19)
        window.navigationBarColor = Color.rgb(9, 12, 19)
        window.decorView.systemUiVisibility = 0
        buildUi()
        loadApps()
    }

    private fun panel(color: Int = Color.argb(150, 24, 31, 44), radius: Float = 24f): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = radius; setStroke(1, Color.argb(55, 235, 202, 135)) }

    private fun text(value: String, size: Float, color: Int = Color.WHITE, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = value; textSize = size; setTextColor(color)
            if (bold) typeface = Typeface.create("sans-serif", Typeface.BOLD)
        }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(22), dp(22), dp(18))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(8, 13, 23), Color.rgb(20, 31, 47), Color.rgb(8, 12, 20)))
        }
        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val brand = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; layoutParams = LinearLayout.LayoutParams(0, -2, 1f) }
        brand.addView(text("DARKNES", 13f, gold, true))
        brand.addView(text("VISION  •  HOME", 10f, Color.LTGRAY))
        clock = text("--:--", 34f, Color.WHITE, true)
        header.addView(brand); header.addView(clock)
        root.addView(header)
        root.addView(text(SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date()).uppercase(), 11f, Color.LTGRAY).apply {
            setPadding(0, dp(5), 0, dp(18))
        })

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(17), dp(18), dp(17))
            background = panel(Color.argb(125, 41, 51, 65), 26f)
        }
        hero.addView(text("YOUR SPACE. YOUR RULES.", 11f, gold, true))
        hero.addView(text("Добро пожаловать", 23f, Color.WHITE, true).apply { setPadding(0, dp(6), 0, dp(3)) })
        hero.addView(text("Darknes OS — твой центр приложений", 12f, Color.LTGRAY))
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(16) })

        search = EditText(this).apply {
            hint = "Поиск приложений"; setHintTextColor(Color.GRAY); setTextColor(Color.WHITE)
            textSize = 14f; setSingleLine(true); setPadding(dp(16), 0, dp(16), 0)
            background = panel(Color.argb(110, 32, 40, 53), 18f)
        }
        root.addView(search, LinearLayout.LayoutParams(-1, dp(48)).apply { bottomMargin = dp(16) })

        val section = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        section.addView(text("ПРИЛОЖЕНИЯ", 12f, gold, true), LinearLayout.LayoutParams(0, -2, 1f))
        section.addView(text("Все приложения  ↗", 11f, Color.LTGRAY))
        root.addView(section, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = dp(10) })

        val scroll = ScrollView(this).apply { clipToPadding = false }
        appGrid = GridLayout(this).apply { columnCount = 4; useDefaultMargins = false }
        scroll.addView(appGrid)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER
            background = panel(Color.argb(170, 25, 33, 47), 22f); setPadding(dp(10), dp(12), dp(10), dp(12))
        }
        listOf("⌂\nГлавная", "◈\nDarknes ID", "⚙\nНастройки").forEachIndexed { index, label ->
            val item = text(label, 11f, if (index == 0) gold else Color.LTGRAY, index == 0).apply {
                gravity = Gravity.CENTER; setOnClickListener {
                    if (index == 1) Toast.makeText(this@MainActivity, "Darknes ID скоро появится", Toast.LENGTH_SHORT).show()
                    if (index == 2) startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))
                }
            }
            footer.addView(item, LinearLayout.LayoutParams(0, -2, 1f))
        }
        root.addView(footer, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        setContentView(root)
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderApps(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })
        updateClock()
        clock.post(object : Runnable {
            override fun run() { updateClock(); clock.postDelayed(this, 30_000) }
        })
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER)
        apps.clear()
        apps.addAll(packageManager.queryIntentActivities(intent, 0).sortedBy {
            it.loadLabel(packageManager).toString().lowercase(Locale.getDefault())
        })
        renderApps("")
    }

    private fun renderApps(filter: String) {
        appGrid.removeAllViews()
        val shown = apps.filter { it.loadLabel(packageManager).toString().contains(filter, true) }
        shown.forEach { info ->
            val tile = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
                setPadding(dp(5), dp(12), dp(5), dp(12)); background = panel(Color.argb(75, 46, 57, 73), 18f)
                isClickable = true; isFocusable = true
                setOnClickListener {
                    val launch = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
                    if (launch != null) startActivity(launch)
                }
            }
            val icon = ImageView(this).apply { setImageDrawable(info.loadIcon(packageManager)) }
            tile.addView(icon, LinearLayout.LayoutParams(dp(42), dp(42)))
            tile.addView(text(info.loadLabel(packageManager).toString(), 10f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER; maxLines = 2; ellipsize = android.text.TextUtils.TruncateAt.END
                setPadding(0, dp(7), 0, 0)
            }, LinearLayout.LayoutParams(-1, dp(32)))
            val lp = GridLayout.LayoutParams().apply {
                width = 0; height = dp(102); columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(4), dp(4), dp(4), dp(4))
            }
            appGrid.addView(tile, lp)
        }
        if (shown.isEmpty()) {
            appGrid.addView(text("Ничего не найдено", 13f, Color.LTGRAY).apply { setPadding(dp(12), dp(20), 0, 0) })
        }
    }

    private fun updateClock() {
        clock.text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
