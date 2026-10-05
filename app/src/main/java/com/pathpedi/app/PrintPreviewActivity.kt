package com.pathpedi.app

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.view.Gravity
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class PrintPreviewActivity : AppCompatActivity() {

    private lateinit var previewWebView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createScreen()

        val htmlFilePath =
            intent.getStringExtra("HTML_FILE")

        if (!htmlFilePath.isNullOrEmpty()) {

            val file = File(htmlFilePath)

            if (file.exists()) {

                previewWebView.loadUrl(
                    "file://${file.absolutePath}"
                )
            }
        }
    }

    private fun createScreen() {

        // Main vertical layout
        val root = LinearLayout(this)

        root.orientation =
            LinearLayout.VERTICAL

        root.layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )

        // -----------------------------------------
        // TOP BAR
        // -----------------------------------------

        val toolbar = LinearLayout(this)

        toolbar.orientation =
            LinearLayout.HORIZONTAL

        toolbar.gravity =
            Gravity.CENTER_VERTICAL

        toolbar.setPadding(
            12,
            8,
            12,
            8
        )

        // Back button
        val backButton = Button(this)

        backButton.text = "← Back"

        backButton.setOnClickListener {
            finish()
        }

        toolbar.addView(
            backButton,
            LinearLayout.LayoutParams(
                100,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Title
        val title = TextView(this)

        title.text =
            "  Pathpedi Print Preview"

        title.textSize = 18f

        title.setPadding(
            8,
            0,
            8,
            0
        )

        title.layoutParams =
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )

        toolbar.addView(title)

        // Print button
        val printButton = Button(this)

        printButton.text = "Print"

        printButton.setOnClickListener {
            printDocument()
        }

        toolbar.addView(
            printButton,
            LinearLayout.LayoutParams(
                100,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        root.addView(toolbar)

        // -----------------------------------------
        // PREVIEW WEBVIEW
        // -----------------------------------------

        previewWebView = WebView(this)

        previewWebView.settings.apply {

            javaScriptEnabled = true

            domStorageEnabled = true

            databaseEnabled = true

            allowFileAccess = true

            allowContentAccess = true

            builtInZoomControls = true

            displayZoomControls = false

            loadWithOverviewMode = true

            useWideViewPort = true

            cacheMode =
                WebSettings.LOAD_NO_CACHE
        }

        previewWebView.layoutParams =
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )

        root.addView(
            previewWebView
        )

        setContentView(root)
    }

    /**
     * Android System Print
     *
     * Important:
     * System print preview ab MainActivity ke
     * upar directly nahi khulega.
     *
     * Ye custom PrintPreviewActivity ke upar khulega.
     *
     * System preview se Back karne par
     * PrintPreviewActivity return hogi.
     */
    private fun printDocument() {

    val printManager =
        getSystemService(Context.PRINT_SERVICE) as PrintManager

    val printAdapter =
        previewWebView.createPrintDocumentAdapter(
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

    // Remove the custom Print Preview Activity
    // from the Android back stack.
    // After pressing Back in Android Print Preview,
    // the user will return directly to Pathpedi.
    window.decorView.postDelayed({

        if (!isFinishing && !isDestroyed) {
            finish()
        }

    }, 1000)
}

    /**
     * Android Back
     *
     * PrintPreviewActivity se Back
     * directly MainActivity par jayega.
     */
    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {

        if (::previewWebView.isInitialized) {

            previewWebView.stopLoading()

            previewWebView.loadUrl(
                "about:blank"
            )

            previewWebView.clearHistory()

            previewWebView.removeAllViews()

            previewWebView.destroy()
        }

        super.onDestroy()
    }
}
