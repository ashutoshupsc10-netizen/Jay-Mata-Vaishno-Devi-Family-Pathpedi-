package com.pathpedi.app

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

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

        webView.addJavascriptInterface(
            PrintBridge(this),
            "AndroidPrint"
        )

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)

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
        setContentView(webView)

        webView.loadUrl("file:///android_asset/Pathpedi.html")

        // Stylish Pathpedi exit confirmation
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (webView.canGoBack()) {
                        webView.goBack()
                        return
                    }

                    showPathpediExitDialog()
                }
            }
        )
    }

    override fun onResume() {
        super.onResume()
        if (::webView.isInitialized) {
            webView.visibility = View.VISIBLE
            webView.postDelayed({
                if (!isFinishing && !isDestroyed) {
                    webView.requestFocus()
                }
            }, 200)
        }
    }

    /**
     * Pathpedi-style native exit dialog.
     * Matches the HTML design: teal primary color, white card,
     * rounded corners and clean Cancel / Exit actions.
     */
    private fun showPathpediExitDialog() {

        val dialog = Dialog(this)

        val density = resources.displayMetrics.density
        fun dp(value: Int): Int = (value * density).toInt()

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(18), dp(20), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(20).toFloat()
                setStroke(dp(1), Color.rgb(226, 232, 240))
            }
        }

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val icon = TextView(this).apply {
            text = "🚪"
            textSize = 24f
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                setColor(Color.rgb(204, 251, 241))
                cornerRadius = dp(13).toFloat()
            }
        }

        header.addView(
            icon,
            LinearLayout.LayoutParams(dp(48), dp(48))
        )

        val titleBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
        }

        val title = TextView(this).apply {
            text = "Exit Pathpedi?"
            textSize = 18f
            setTextColor(Color.rgb(15, 23, 42))
            setTypeface(typeface, Typeface.BOLD)
        }

        val subtitle = TextView(this).apply {
            text = "Jay Mata Vaishno Devi Family Pathpedi"
            textSize = 12f
            setTextColor(Color.rgb(100, 116, 139))
            setPadding(0, dp(3), 0, 0)
        }

        titleBox.addView(title)
        titleBox.addView(subtitle)

        header.addView(
            titleBox,
            LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
        )

        card.addView(header)

        val message = TextView(this).apply {
            text = "Kya aap Pathpedi app band karna chahte hain?"
            textSize = 15f
            setTextColor(Color.rgb(71, 85, 105))
            setPadding(0, dp(18), 0, dp(16))
        }

        card.addView(message)

        val divider = View(this).apply {
            setBackgroundColor(Color.rgb(241, 245, 249))
        }

        card.addView(
            divider,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            )
        )

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END or Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(2))
        }

        val cancel = TextView(this).apply {
            text = "Cancel"
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.rgb(51, 65, 85))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(9).toFloat()
                setStroke(dp(1), Color.rgb(203, 213, 225))
            }
            isClickable = true
            isFocusable = true
            setPadding(dp(18), 0, dp(18), 0)
        }

        val exit = TextView(this).apply {
            text = "Exit"
            textSize = 14f
            gravity = Gravity.CENTER
            setTypeface(typeface, Typeface.BOLD)
            setTextColor(Color.WHITE)
            background = GradientDrawable().apply {
                setColor(Color.rgb(15, 118, 110))
                cornerRadius = dp(9).toFloat()
            }
            isClickable = true
            isFocusable = true
            setPadding(dp(20), 0, dp(20), 0)
        }

        actions.addView(
            cancel,
            LinearLayout.LayoutParams(dp(110), dp(46)).apply {
                rightMargin = dp(8)
            }
        )

        actions.addView(
            exit,
            LinearLayout.LayoutParams(dp(100), dp(46))
        )

        card.addView(actions)

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        exit.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.setContentView(card)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        dialog.window?.let { window ->
            window.setBackgroundDrawableResource(android.R.color.transparent)
            window.addFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND)

            val params = window.attributes
            params.dimAmount = 0.48f
            window.attributes = params

            window.setLayout(
                (resources.displayMetrics.widthPixels * 0.90).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        dialog.show()

        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.90).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private class PrintBridge(
        private val context: Context
    ) {

        @JavascriptInterface
        fun print() {
            val activity = context as? MainActivity ?: return
            activity.runOnUiThread {
                activity.webView.evaluateJavascript(
                    "(function(){return document.documentElement.outerHTML;})()"
                ) { htmlResult ->
                    try {
                        val html = decodeJsString(htmlResult)
                        val file = File(
                            activity.cacheDir,
                            "pathpedi_print_preview.html"
                        )
                        file.writeText(html, Charsets.UTF_8)

                        val intent = Intent(
                            activity,
                            PrintPreviewActivity::class.java
                        )
                        intent.putExtra("HTML_FILE", file.absolutePath)
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
                    val file = File(
                        activity.cacheDir,
                        "pathpedi_daybook_print.html"
                    )
                    file.writeText(html, Charsets.UTF_8)

                    val intent = Intent(
                        activity,
                        PrintPreviewActivity::class.java
                    )
                    intent.putExtra("HTML_FILE", file.absolutePath)
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        @JavascriptInterface
        fun saveCSV(filename: String, base64Data: String): Boolean {

            val activity = context as? MainActivity ?: return false

            return try {

                val safeName = filename
                    .replace("/", "_")
                    .replace("\\", "_")
                    .ifBlank { "pathpedi_export.csv" }

                val bytes = Base64.decode(
                    base64Data,
                    Base64.DEFAULT
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                        put(MediaStore.Downloads.MIME_TYPE, "text/csv")
                        put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + "/Pathpedi/"
                        )
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val resolver = activity.contentResolver

                    val uri = resolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                    ) ?: throw Exception("MediaStore insert failed")

                    try {
                        resolver.openOutputStream(uri)?.use { output ->
                            output.write(bytes)
                            output.flush()
                        } ?: throw Exception("Could not open CSV output stream")

                        val done = ContentValues().apply {
                            put(MediaStore.Downloads.IS_PENDING, 0)
                        }

                        resolver.update(uri, done, null, null)

                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                "CSV saved: Downloads/Pathpedi/$safeName",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        true

                    } catch (e: Exception) {
                        resolver.delete(uri, null, null)
                        throw e
                    }

                } else {

                    val downloads = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )

                    val folder = File(downloads, "Pathpedi")

                    if (!folder.exists() && !folder.mkdirs()) {
                        throw Exception("Could not create Downloads/Pathpedi")
                    }

                    val file = File(folder, safeName)

                    FileOutputStream(file).use {
                        it.write(bytes)
                    }

                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "CSV saved: Downloads/Pathpedi/$safeName",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    true
                }

            } catch (e: Exception) {

                e.printStackTrace()

                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "CSV save failed: " + (e.message ?: "Unknown error"),
                        Toast.LENGTH_LONG
                    ).show()
                }

                false
            }
        }

        @JavascriptInterface
        fun saveBackup(filename: String, base64Data: String): Boolean {

            val activity = context as? MainActivity ?: return false

            return try {

                val safeName = filename
                    .replace("/", "_")
                    .replace("\\", "_")
                    .ifBlank { "pathpedi_backup.json" }

                val bytes = Base64.decode(
                    base64Data,
                    Base64.DEFAULT
                )

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                    val values = ContentValues().apply {
                        put(MediaStore.Downloads.DISPLAY_NAME, safeName)
                        put(MediaStore.Downloads.MIME_TYPE, "application/json")
                        put(
                            MediaStore.Downloads.RELATIVE_PATH,
                            Environment.DIRECTORY_DOWNLOADS + "/Pathpedi/"
                        )
                        put(MediaStore.Downloads.IS_PENDING, 1)
                    }

                    val resolver = activity.contentResolver

                    val uri = resolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        values
                    ) ?: throw Exception("Backup MediaStore insert failed")

                    try {
                        resolver.openOutputStream(uri)?.use { output ->
                            output.write(bytes)
                            output.flush()
                        } ?: throw Exception("Could not open backup output stream")

                        val done = ContentValues().apply {
                            put(MediaStore.Downloads.IS_PENDING, 0)
                        }

                        resolver.update(uri, done, null, null)

                        activity.runOnUiThread {
                            Toast.makeText(
                                activity,
                                "Backup saved: Downloads/Pathpedi/$safeName",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        true

                    } catch (e: Exception) {
                        resolver.delete(uri, null, null)
                        throw e
                    }

                } else {

                    val downloads = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                    )

                    val folder = File(downloads, "Pathpedi")

                    if (!folder.exists() && !folder.mkdirs()) {
                        throw Exception("Could not create Downloads/Pathpedi")
                    }

                    val file = File(folder, safeName)

                    FileOutputStream(file).use {
                        it.write(bytes)
                    }

                    activity.runOnUiThread {
                        Toast.makeText(
                            activity,
                            "Backup saved: Downloads/Pathpedi/$safeName",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    true
                }

            } catch (e: Exception) {

                e.printStackTrace()

                activity.runOnUiThread {
                    Toast.makeText(
                        activity,
                        "Backup save failed: " + (e.message ?: "Unknown error"),
                        Toast.LENGTH_LONG
                    ).show()
                }

                false
            }
        }

        private fun decodeJsString(value: String): String {
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
                    value.substring(1, value.length - 1)
                }
            }
            return value
        }
    }
}
