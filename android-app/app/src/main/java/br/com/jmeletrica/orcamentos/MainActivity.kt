package br.com.jmeletrica.orcamentos

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView

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
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                val script = """
                    (function() {
                        const logoUrl = 'https://raw.githubusercontent.com/johnycristiansan-oss/jm-eletrica-e-contrucoes/main/jm.png';
                        document.querySelectorAll('.logo').forEach(function(el) {
                            el.innerHTML = '';
                            el.style.backgroundImage = 'url(' + logoUrl + ')';
                            el.style.backgroundSize = 'contain';
                            el.style.backgroundPosition = 'center';
                            el.style.backgroundRepeat = 'no-repeat';
                            el.style.backgroundColor = '#fff';
                            el.style.border = '0';
                            el.style.boxShadow = 'none';
                        });
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

    override fun onSaveInstanceState(outState: Bundle) {
        webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
