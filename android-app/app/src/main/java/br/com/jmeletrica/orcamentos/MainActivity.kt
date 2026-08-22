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
                    (function(){
                      window.JMStorage = window.JMStorage || {};
                      JMStorage.getState=function(){try{return JSON.parse(JMNative.loadState()||'{}')}catch(e){return {}}};
                      JMStorage.saveState=function(s){try{JMNative.saveState(JSON.stringify(s||{}));return s}catch(e){return s}};
                      JMStorage.getProfile=function(){return this.getState().profile||{}};
                      JMStorage.saveProfile=function(p){var s=this.getState();s.profile=p||{};return this.saveState(s)};
                      JMStorage.getLocation=function(){return this.getState().location||{}};
                      JMStorage.saveLocation=function(l){var s=this.getState();s.location=l||{};return this.saveState(s)};
                      JMStorage.getDraft=function(){return this.getState().draft||null};
                      JMStorage.saveDraft=function(d){var s=this.getState();s.draft=d;return this.saveState(s)};
                      JMStorage.clearDraft=function(){var s=this.getState();delete s.draft;return this.saveState(s)};
                      JMStorage.getRequests=function(){return this.getState().requests||[]};
                      JMStorage.addRequest=function(r){var s=this.getState();s.requests=s.requests||[];s.requests.push(r);return this.saveState(s)};
                      JMStorage.saveRequests=function(r){var s=this.getState();s.requests=Array.isArray(r)?r:[];return this.saveState(s)};
                      if(window.onNativeReady)window.onNativeReady(JMNative.loadState());
                    })();
                """.trimIndent()
                view.evaluateJavascript(script,null)
            }
        }
        webView.webChromeClient = WebChromeClient()
        webView.setBackgroundColor(0xFFF5F7FA.toInt())
        setContentView(webView)
        webView.loadUrl("file:///android_asset/index.html")
        onBackPressedDispatcher.addCallback(this,object:OnBackPressedCallback(true){override fun handleOnBackPressed(){if(webView.canGoBack())webView.goBack()else finish()}})
    }

    inner class AppBridge {
        @JavascriptInterface fun saveState(state:String){prefs.edit().putString("state",state).apply()}
        @JavascriptInterface fun loadState():String=prefs.getString("state","{}")?:"{}"
        @JavascriptInterface fun requestLocation(){
            val fine=ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED
            val coarse=ContextCompat.checkSelfPermission(this@MainActivity,Manifest.permission.ACCESS_COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED
            if(!fine&&!coarse){ActivityCompat.requestPermissions(this@MainActivity,arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION),locationRequestCode);return}
            sendLastKnownLocation()
        }
        @SuppressLint("MissingPermission") private fun sendLastKnownLocation(){
            val manager=getSystemService(LOCATION_SERVICE) as LocationManager
            val provider=when{manager.isProviderEnabled(LocationManager.GPS_PROVIDER)->LocationManager.GPS_PROVIDER;manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)->LocationManager.NETWORK_PROVIDER;else->null}
            val location=provider?.let{manager.getLastKnownLocation(it)}
            val js=if(location!=null)"window.onNativeLocation&&window.onNativeLocation(${location.latitude},${location.longitude});"else"window.onNativeLocationUnavailable&&window.onNativeLocationUnavailable();"
            runOnUiThread{webView.evaluateJavascript(js,null)}
        }
    }
    override fun onRequestPermissionsResult(requestCode:Int,permissions:Array<out String>,grantResults:IntArray){super.onRequestPermissionsResult(requestCode,permissions,grantResults);if(requestCode==locationRequestCode){if(grantResults.any{it==PackageManager.PERMISSION_GRANTED})sendLocationToWeb()else webView.evaluateJavascript("window.onLocationPermissionResult&&window.onLocationPermissionResult(false)",null)}}
    @SuppressLint("MissingPermission") private fun sendLocationToWeb(){
        val manager=getSystemService(LOCATION_SERVICE) as LocationManager
        val provider=when{manager.isProviderEnabled(LocationManager.GPS_PROVIDER)->LocationManager.GPS_PROVIDER;manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)->LocationManager.NETWORK_PROVIDER;else->null}
        val location=provider?.let{manager.getLastKnownLocation(it)}
        if(location!=null)webView.evaluateJavascript("window.onNativeLocation&&window.onNativeLocation(${location.latitude},${location.longitude})",null)else webView.evaluateJavascript("window.onNativeLocationUnavailable&&window.onNativeLocationUnavailable()",null)
    }
    override fun onSaveInstanceState(outState:Bundle){webView.saveState(outState);super.onSaveInstanceState(outState)}
    override fun onDestroy(){webView.removeJavascriptInterface("JMNative");webView.destroy();super.onDestroy()}
}
