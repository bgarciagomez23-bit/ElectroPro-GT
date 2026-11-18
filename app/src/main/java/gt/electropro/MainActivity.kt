package gt.electropro

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 14, 20)
        window.navigationBarColor = Color.rgb(8, 14, 20)
        setContent {
            ElectroProWebApp()
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun ElectroProWebApp() {
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(Color.rgb(8, 14, 20))
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.javaScriptCanOpenWindowsAutomatically = false
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl("file:///android_asset/index.html")
                webView = this
            }
        },
        update = { view ->
            if (webView !== view) webView = view
        }
    )
}
