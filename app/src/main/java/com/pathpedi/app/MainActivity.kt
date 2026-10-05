package com.pathpedi.app

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.io.File

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
            javaScriptCanOpenWindowsAutomatically = true
            cacheMode = WebSettings.LOAD_DEFAULT
        }

        // Android bridge
        webView.addJavascriptInterface(
            PrintBridge(this),
            "AndroidPrint"
        )

        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(
                view: WebView?,
                url: String?
            ) {
                super.onPageFinished(view, url)

                // HTML ka window.print() Android Print Preview ko
                // hamare custom preview screen par bhejega.
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

        setContentView(webView)

        // Pathpedi HTML
        webView.loadUrl(
            "file:///android_asset/Pathpedi.html"
        )

        // Android Back handling
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    if (webView.canGoBack()) {
                        webView.goBack()
                    } else {
                        // Main screen par Back se app close/background
                        // nahi karenge.
                    }
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()

        if (::webView.isInitialized) {
            webView.visibility = WebView.VISIBLE

            webView.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    webView.requestFocus()
                }
            }, 200)
        }
    }

    /**
     * JavaScript → Android bridge
     */
    private class PrintBridge(
        private val context: Context
    ) {

        @JavascriptInterface
        fun print() {

            val activity =
                context as? MainActivity ?: return

            activity.runOnUiThread {

                // Current Pathpedi page ka complete HTML
                activity.webView.evaluateJavascript(
                    "(function(){return document.documentElement.outerHTML;})()"
                ) { htmlResult ->

                    try {

                        // evaluateJavascript JSON string return karta hai,
                        // isliye quotes/unicode decode kar rahe hain.
                        val html = decodeJsString(htmlResult)

                        // Temporary HTML file
                        val file = File(
                            activity.cacheDir,
                            "pathpedi_print_preview.html"
                        )

                        file.writeText(
                            html,
                            Charsets.UTF_8
                        )

                        // Custom in-app Print Preview
                        val intent = Intent(
                            activity,
                            PrintPreviewActivity::class.java
                        )

                        intent.putExtra(
                            "HTML_FILE",
                            file.absolutePath
                        )

                        activity.startActivity(intent)

                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
@JavascriptInterface
fun printHtml(html: String) {

    val activity = context as? MainActivity ?: return

    activity.runOnUiThread {

        try {

            // Temporary HTML file
            val file = File(
                activity.cacheDir,
                "pathpedi_daybook_print.html"
            )

            file.writeText(
                html,
                Charsets.UTF_8
            )

            // Custom in-app Print Preview
            val intent = Intent(
                activity,
                PrintPreviewActivity::class.java
            )

            intent.putExtra(
                "HTML_FILE",
                file.absolutePath
            )

            activity.startActivity(intent)

        } catch (e: Exception) {

            e.printStackTrace()

        }
    }
}
        private fun decodeJsString(
            value: String
        ): String {

            if (
                value.length >= 2 &&
                value.first() == '"' &&
                value.last() == '"'
            ) {
                return try {

                    org.json.JSONTokener(value)
                        .nextValue()
                        .toString()

                } catch (e: Exception) {

                    value.substring(
                        1,
                        value.length - 1
                    )
                }
            }

            return value
        }
    }
}
