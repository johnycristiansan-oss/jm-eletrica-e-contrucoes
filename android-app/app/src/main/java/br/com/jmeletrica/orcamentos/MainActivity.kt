package br.com.jmeletrica.orcamentos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private val prefs by lazy { getSharedPreferences("jm_app", MODE_PRIVATE) }
    private val locationRequestCode = 1001

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = true
        webView.settings.allowContentAccess = true
        webView.settings.loadsImagesAutomatically = true
        webView.settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_NEVER_ALLOW
        webView.addJavascriptInterface(AppBridge(), "JMNative")
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                val script = """
                    (function() {
                        const logoUrl = 'https://raw.githubusercontent.com/johnycristiansan-oss/jm-eletrica-e-contrucoes/main/jm.png';
                        document.querySelectorAll('.logo').forEach(function(el) {
                            el.style.backgroundImage = 'url(' + logoUrl + ')';
                            el.style.backgroundSize = 'contain';
                            el.style.backgroundPosition = 'center';
                            el.style.backgroundRepeat = 'no-repeat';
                            el.style.backgroundColor = '#fff';
                            el.style.border = '0';
                            el.style.boxShadow = 'none';
                        });
                        if (window.onNativeReady) window.onNativeReady(JMNative.loadState());
                    })();
                """.trimIndent()
                view.evaluateJavascript(script, null)
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.setBackgroundColor(0xFFF5F7FA.toInt())
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    inner class AppBridge {
        @JavascriptInterface
        fun saveState(state: String) {
            prefs.edit().putString("state", state).apply()
        }

        @JavascriptInterface
        fun loadState(): String = prefs.getString("state", "{}") ?: "{}"

        @JavascriptInterface
        fun requestLocation() {
            val fine = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (!fine && !coarse) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), locationRequestCode)
                return
            }
            sendLastKnownLocation()
        }

        @SuppressLint("MissingPermission")
        private fun sendLastKnownLocation() {
            val manager = getSystemService(LOCATION_SERVICE) as LocationManager
            val provider = when {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }
            val location = provider?.let { manager.getLastKnownLocation(it) }
            val js = if (location != null) {
                "window.onNativeLocation && window.onNativeLocation(${location.latitude},${location.longitude});"
            } else {
                "window.onNativeLocationUnavailable && window.onNativeLocationUnavailable();"
            }
            runOnUiThread { webView.evaluateJavascript(js, null) }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            if (granted) {
                sendLocationToWeb()
            } else {
                webView.evaluateJavascript("window.onLocationPermissionResult && window.onLocationPermissionResult(false)", null)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun sendLocationToWeb() {
        val manager = getSystemService(LOCATION_SERVICE) as LocationManager
        val provider = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        val location = provider?.let { manager.getLastKnownLocation(it) }
        if (location != null) {
            webView.evaluateJavascript("window.onNativeLocation && window.onNativeLocation(${location.latitude},${location.longitude})", null)
        } else {
            webView.evaluateJavascript("window.onNativeLocationUnavailable && window.onNativeLocationUnavailable()", null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView.removeJavascriptInterface("JMNative")
        webView.destroy()
        super.onDestroy()
    }
}
