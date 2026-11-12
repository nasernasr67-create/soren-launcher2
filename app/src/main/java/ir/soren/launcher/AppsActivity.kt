package ir.soren.launcher

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/** Simple app drawer. Tap to open an app, long-press to assign it to a dock button. */
class AppsActivity : Activity() {

    private class Entry(val label: String, val pkg: String, val icon: Drawable)

    private val keys = arrayOf("map", "bluetooth", "radio", "music", "video", "carsettings")
    private val names = arrayOf("نقشه", "بلوتوث", "رادیو", "موزیک", "ویدیو", "تنظیمات ماشین")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pm = packageManager
        val query = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val list = pm.queryIntentActivities(query, 0)
            .map { Entry(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
            .filter { it.pkg != packageName }
            .sortedBy { it.label.lowercase() }

        val d = resources.displayMetrics.density
        val grid = GridView(this)
        grid.setBackgroundColor(Color.parseColor("#020818"))
        grid.numColumns = 6
        grid.stretchMode = GridView.STRETCH_COLUMN_WIDTH
        grid.verticalSpacing = (16 * d).toInt()
        grid.setPadding((24 * d).toInt(), (24 * d).toInt(), (24 * d).toInt(), (24 * d).toInt())

        grid.adapter = object : BaseAdapter() {
            override fun getCount() = list.size
            override fun getItem(position: Int) = list[position]
            override fun getItemId(position: Int) = position.toLong()
            override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val e = list[position]
                val box = LinearLayout(this@AppsActivity)
                box.orientation = LinearLayout.VERTICAL
                box.gravity = Gravity.CENTER_HORIZONTAL
                box.layoutParams = AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (104 * d).toInt())
                val icon = ImageView(this@AppsActivity)
                icon.setImageDrawable(e.icon)
                icon.layoutParams = LinearLayout.LayoutParams((56 * d).toInt(), (56 * d).toInt())
                val label = TextView(this@AppsActivity)
                label.text = e.label
                label.setTextColor(Color.WHITE)
                label.textSize = 12f
                label.gravity = Gravity.CENTER
                label.maxLines = 2
                box.addView(icon)
                box.addView(label)
                return box
            }
        }

        grid.setOnItemClickListener { _, _, position, _ ->
            pm.getLaunchIntentForPackage(list[position].pkg)?.let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(it)
            }
        }
        grid.setOnItemLongClickListener { _, _, position, _ ->
            val e = list[position]
            AlertDialog.Builder(this)
                .setTitle("«${e.label}» برای کدام دکمه باشد؟")
                .setItems(names) { _, which ->
                    getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE)
                        .edit().putString("pkg_" + keys[which], e.pkg).apply()
                    Toast.makeText(this, "ذخیره شد: ${names[which]} ← ${e.label}", Toast.LENGTH_SHORT).show()
                }
                .show()
            true
        }
        setContentView(grid)
    }
}
