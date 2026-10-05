package com.pathpedi.app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        webView.addJavascriptInterface(PrintBridge(this), "AndroidPrint")

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

                // Existing window.print() calls in the HTML will use
                // Android's native Print/PDF system.
                view?.evaluateJavascript(
                    """
                    (function() {
                        window.print = function() {
                            if (window.AndroidPrint && AndroidPrint.print) {
                                AndroidPrint.print();
                            }
                        };
                    })();
                    """.trimIndent(),
                    null
                )
            }
        }

        webView.webChromeClient = WebChromeClient()
        webView.loadUrl("file:///android_asset/Pathpedi.html")

        setContentView(webView)
    }

    private class PrintBridge(private val context: Context) {

        @JavascriptInterface
        fun print() {
            val activity = context as? AppCompatActivity ?: return

            activity.runOnUiThread {
                val printManager =
                    activity.getSystemService(Context.PRINT_SERVICE) as PrintManager

                val printAdapter =
                    activity.webView.createPrintDocumentAdapter("Pathpedi Statement")

                val attributes = PrintAttributes.Builder()
                    .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                    .setResolution(
                        PrintAttributes.Resolution(
                            "pathpedi_print",
                            "Pathpedi Print",
                            300,
                            300
                        )
                    )
                    .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                    .build()

                printManager.print(
                    "Pathpedi Statement",
                    printAdapter,
                    attributes
                )
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
