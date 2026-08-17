package br.com.jmeletrica.orcamentos

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.allowFileAccess = false
        webView.settings.allowContentAccess = false
        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.setBackgroundColor(0xFFF5F7FA.toInt())
        setContentView(webView)

        if (savedInstanceState == null) {
            loadAppPage()
        } else {
            webView.restoreState(savedInstanceState)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack() else finish()
            }
        })
    }

    private fun loadAppPage() {
        val pageUrl = "https://raw.githubusercontent.com/johnycristiansan-oss/jm-eletrica-e-contrucoes/main/orcamento/index.html"
        val baseUrl = "https://raw.githubusercontent.com/johnycristiansan-oss/jm-eletrica-e-contrucoes/main/orcamento/"

        Thread {
            try {
                val connection = URL(pageUrl).openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 15000
                connection.readTimeout = 15000
                connection.setRequestProperty("User-Agent", "JM-Orcamentos-Android")
                val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                connection.disconnect()

                runOnUiThread {
                    webView.loadDataWithBaseURL(
                        baseUrl,
                        html,
                        "text/html",
                        "UTF-8",
                        pageUrl
                    )
                }
            } catch (e: Exception) {
                runOnUiThread {
                    val message = e.message ?: "Não foi possível carregar o aplicativo."
                    webView.loadDataWithBaseURL(
                        baseUrl,
                        "<html><body style='font-family:Arial;padding:24px'><h2>JM Orçamentos</h2><p>Não foi possível carregar a tela agora.</p><p>$message</p></body></html>",
                        "text/html",
                        "UTF-8",
                        pageUrl
                    )
                }
            }
        }.start()
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
