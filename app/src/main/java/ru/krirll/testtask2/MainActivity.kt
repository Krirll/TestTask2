package ru.krirll.testtask2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("SetJavaScriptEnabled")
class MainActivity : AppCompatActivity() {

    private var alertDialog: AlertDialog? = null
    private var dialogState: Boolean = false
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initWebView()
    }

    private fun initWebView() {
        webView = findViewById(R.id.webView)
        webView?.settings?.javaScriptEnabled = true
        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }
        webView?.settings?.cacheMode = WebSettings.LOAD_DEFAULT
        webView?.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request?.url.toString() == "https://yandex.ru/maps/") {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(request?.url.toString())))
                    return true
                }
                if (request?.url.toString().contains("https://yandex.ru/pogoda/")) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://yandex.ru/pogoda/")
                        )
                    )
                    return true
                }
                return false
            }
        }
        val s = checkLastSite()
        webView?.loadUrl(s)

    }

    private fun checkLastSite(): String {
        val saved =
            getSharedPreferences(
                getString(R.string.app_name),
                Context.MODE_PRIVATE
            ).getString(
                LAST_SITE, ""
            )
        return if (saved == null || saved == "") URL else saved
    }

    override fun onBackPressed() {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            if (webView?.url != URL) {
                webView?.clearHistory()
                webView?.loadUrl(URL)
            } else
                createDialog()
        }
    }

    private fun createDialog() {
        alertDialog = AlertDialog.Builder(this).apply {
            title = getString(R.string.confirmation)
            setMessage(getString(R.string.are_you_sure))
            setCancelable(false)
            setPositiveButton(getString(R.string.ok)) { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }
        }.create()
        alertDialog?.show()
    }

    override fun onPause() {
        super.onPause()
        dialogState = alertDialog?.isShowing ?: false
        saveSite()
    }

    private fun saveSite() {
        getSharedPreferences(
            getString(R.string.app_name),
            Context.MODE_PRIVATE
        ).edit()
            .putString(LAST_SITE, webView?.url)
            .apply()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(ALERT_STATE, dialogState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        dialogState = savedInstanceState.getBoolean(ALERT_STATE, false)
        if (dialogState)
            createDialog()
    }

    companion object {
        private const val ALERT_STATE = "ALERT_STATE"
        private const val LAST_SITE = "LAST_SITE"
        private const val URL = "https://yandex.ru/"
    }
}