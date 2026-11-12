package ir.soren.launcher

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.Toast

class MainActivity : Activity(), LocationListener {

    companion object {
        /** Set to true if the animations stutter on the head unit. */
        const val LITE_MODE = false

        const val PREFS = "launcher"

        /** Packages tried (in order) for each button before falling back to a system intent. */
        val DEFAULT_PACKAGES: Map<String, List<String>> = mapOf(
            "map" to listOf(
                "ir.balad",
                "org.rajman.neshan.traffic.tehran",
                "com.google.android.apps.maps",
                "com.waze"
            ),
            "video" to listOf(
                "org.videolan.vlc",
                "com.mxtech.videoplayer.ad"
            )
        )
    }

    private lateinit var web: WebView
    private var locationStarted = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        web = WebView(this)
        web.setBackgroundColor(Color.parseColor("#020818"))
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.allowFileAccess = true
        web.isVerticalScrollBarEnabled = false
        web.isHorizontalScrollBarEnabled = false
        web.overScrollMode = View.OVER_SCROLL_NEVER
        web.addJavascriptInterface(Bridge(), "Android")
        setContentView(web)
        web.loadUrl("file:///android_asset/launcher.html" + if (LITE_MODE) "?lite=1" else "")

        ensureLocationPermission()
    }

    // ---- full screen ----

    @Suppress("DEPRECATION")
    private fun hideBars() {
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideBars()
    }

    // As the home screen, the back button should do nothing.
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {}

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        hideBars()
    }

    // ---- speed from GPS ----

    private fun ensureLocationPermission() {
        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocation() {
        if (locationStarted) return
        if (Build.VERSION.SDK_INT >= 23 &&
            checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) return
        val lm = getSystemService(LOCATION_SERVICE) as LocationManager
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this)
            locationStarted = true
        } catch (e: Exception) {
            // No GPS provider on this unit.
        }
    }

    private fun stopLocation() {
        if (!locationStarted) return
        (getSystemService(LOCATION_SERVICE) as LocationManager).removeUpdates(this)
        locationStarted = false
    }

    override fun onResume() {
        super.onResume()
        startLocation()
    }

    override fun onPause() {
        stopLocation()
        super.onPause()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        startLocation()
    }

    override fun onLocationChanged(location: Location) {
        val kmh = if (location.hasSpeed()) location.speed * 3.6f else 0f
        val shown = if (kmh < 3f) 0 else Math.round(kmh)
        web.evaluateJavascript("window.setSpeedNative && window.setSpeedNative($shown)", null)
    }

    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}

    // ---- buttons ----

    private fun launchKey(key: String) {
        if (key == "apps") {
            startActivity(Intent(this, AppsActivity::class.java))
            return
        }
        val saved = getSharedPreferences(PREFS, MODE_PRIVATE).getString("pkg_$key", null)
        val candidates = listOfNotNull(saved) + (DEFAULT_PACKAGES[key] ?: emptyList())
        for (pkg in candidates) {
            val intent = packageManager.getLaunchIntentForPackage(pkg)
            if (intent != null) {
                tryStart(intent)
                return
            }
        }
        when (key) {
            "map" -> tryStart(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=")))
            "bluetooth" -> tryStart(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
            "music" -> tryStart(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC))
            "video" -> tryStart(Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_GALLERY))
            "carsettings" -> tryStart(Intent(Settings.ACTION_SETTINGS))
            else -> {
                Toast.makeText(this, "برنامه این دکمه را از لیست برنامه‌ها با نگه‌داشتن انگشت انتخاب کن", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, AppsActivity::class.java))
            }
        }
    }

    private fun tryStart(intent: Intent) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "برنامه‌ای برای این دکمه پیدا نشد", Toast.LENGTH_LONG).show()
        }
    }

    private fun mediaKey(code: Int) {
        val am = getSystemService(AUDIO_SERVICE) as AudioManager
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
        am.dispatchMediaKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
    }

    inner class Bridge {
        @JavascriptInterface
        fun launch(key: String) {
            runOnUiThread { launchKey(key) }
        }

        @JavascriptInterface
        fun media(cmd: String) {
            runOnUiThread {
                when (cmd) {
                    "play_pause" -> mediaKey(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
                    "next" -> mediaKey(KeyEvent.KEYCODE_MEDIA_NEXT)
                    "prev" -> mediaKey(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                }
            }
        }
    }
}
