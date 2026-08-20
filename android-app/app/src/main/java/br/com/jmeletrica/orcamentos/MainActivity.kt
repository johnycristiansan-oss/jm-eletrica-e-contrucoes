package br.com.jmeletrica.orcamentos

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONObject

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
            runOnUiThread {
                val fine = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                val coarse = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                if (!fine && !coarse) {
                    ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), locationRequestCode)
                } else {
                    webView.evaluateJavascript("window.onLocationPermissionResult && window.onLocationPermissionResult(true)", null)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationRequestCode) {
            val granted = grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            webView.evaluateJavascript("window.onLocationPermissionResult && window.onLocationPermissionResult($granted)", null)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
