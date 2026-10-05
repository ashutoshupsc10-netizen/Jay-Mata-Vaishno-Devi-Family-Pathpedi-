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
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // =========================
        // WEBVIEW
        // =========================
        webView = WebView(this)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // =========================
        // ANDROID BRIDGE
        // =========================
        webView.addJavascriptInterface(
            PrintBridge(this),
            "AndroidPrint"
        )

        // =========================
        // WEBVIEW CLIENT
        // =========================
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                super.onPageFinished(view, url)

                // HTML window.print() -> Android native print
                view?.evaluateJavascript(
                    """
                    (function() {
                        window.print = function() {
                            if (
                                window.AndroidPrint &&
                                AndroidPrint.print
                            ) {
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

        // =========================
        // LOAD PATHPEDI HTML
        // =========================
        webView.loadUrl(
            "file:///android_asset/Pathpedi.html"
        )

        setContentView(webView)

        // =========================
        // MODERN BACK HANDLING
        // =========================
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (webView.canGoBack()) {

                        webView.goBack()

                    } else {

                        // Do NOT close / background the app.
                        // Stay on the Pathpedi main screen.
                    }
                }
            }
        )
    }

    // =========================
    // RESTORE WEBVIEW AFTER
    // PRINT PREVIEW
    // =========================
    override fun onResume() {
        super.onResume()

        if (::webView.isInitialized) {

            webView.visibility = WebView.VISIBLE

            webView.postDelayed({

                if (!isFinishing && !isDestroyed) {
                    webView.requestFocus()
                }

            }, 300)
        }
    }

    // =========================
    // PRINT BRIDGE
    // =========================
    private class PrintBridge(
        private val context: Context
    ) {

        @JavascriptInterface
        fun print() {

            val activity =
                context as? MainActivity ?: return

            activity.runOnUiThread {

                val printManager =
                    activity.getSystemService(
                        Context.PRINT_SERVICE
                    ) as PrintManager

                val printAdapter =
                    activity.webView
                        .createPrintDocumentAdapter(
                            "Pathpedi Statement"
                        )

                val attributes =
                    PrintAttributes.Builder()
                        .setMediaSize(
                            PrintAttributes.MediaSize.ISO_A4
                        )
                        .setResolution(
                            PrintAttributes.Resolution(
                                "pathpedi_print",
                                "Pathpedi Print",
                                300,
                                300
                            )
                        )
                        .setMinMargins(
                            PrintAttributes.Margins.NO_MARGINS
                        )
                        .build()

                printManager.print(
                    "Pathpedi Statement",
                    printAdapter,
                    attributes
                )
            }
        }
    }
}
