package com.noviindus.novi_yt_player

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.ProgressBar
import io.flutter.plugin.platform.PlatformView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import android.os.Bundle
import android.util.TypedValue
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.delay
import io.flutter.plugin.common.BinaryMessenger
import android.webkit.JavascriptInterface
import androidx.appcompat.app.AppCompatActivity

@SuppressLint("SetJavaScriptEnabled")
class NativeVideoView(
    context: Context,
    messenger: BinaryMessenger,
    id: Int,
    creationParams: Map<*, *>?
) : PlatformView {
    private val view: View = LayoutInflater.from(context).inflate(R.layout.native_video_view, null, false)
    private val webView: WebView = view.findViewById(R.id.webView)
    private val progressBar: ProgressBar = view.findViewById(R.id.loadingIndicator)
    private var customView: View? = null
    private val fullscreenContainer: FrameLayout = FrameLayout(context)
    private val mainLayout: FrameLayout = FrameLayout(context)
    private var playingPositionJob: Job? = null
    private var webViewBundle: Bundle? = null
    private val methodChannel = MethodChannel(messenger, "video_player")

    init {
        // WebView setup
        webView.settings.javaScriptEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        webView.addJavascriptInterface(object {
            @JavascriptInterface
            fun onPositionChanged(position: Double) {
                CoroutineScope(Dispatchers.Main).launch {
                    methodChannel.invokeMethod("onPositionChanged", position)
                }
            }
        }, "Android")

        setupWebViewClient()
        setupWebChromeClient(context)

        webViewBundle?.let { webView.restoreState(it) }
            ?: webView.loadUrl(creationParams?.get("url") as? String ?: "")

        setupPositionTracking()
    }

    private fun setupWebViewClient() {
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // Your existing JavaScript injection code
                val jsCode = """
        // Helper function to hide elements by class name
        function hideElementsByClassName(className) {
            const elements = document.getElementsByClassName(className);
            for (let i = 0; i < elements.length; i++) {
                elements[i].style.display = 'none';
            }
        }

        // Function to hide YouTube menu items
        function hideElements() {
            const menuItems = document.getElementsByClassName("ytp-menuitem");
            if (menuItems.length > 3) {
                menuItems[4].style.display = 'none';
            }
        }

        // Initial hide and set interval
        hideElements();
        setInterval(hideElements, 500);

        // Hide various YouTube interface elements
        const elementsToHide = [
            "ytp-fullscreen-button ytp-button",
            "ytp-large-play-button ytp-button ytp-large-play-button-red-bg",
            "ytp-youtube-button ytp-button yt-uix-sessionlink",
            "ytp-chrome-top ytp-show-cards-title",
            "html5-endscreen ytp-player-content videowall-endscreen ytp-endscreen-paginate ytp-show-tiles",
            "annotation annotation-type-custom iv-branding",
            "ytp-error-content-wrap-subreason",
            "ytp-small-redirect"
        ];

        elementsToHide.forEach(hideElementsByClassName);

        // Function to hide overlay elements
        function hideOverlayElements() {
            hideElementsByClassName("ytp-pause-overlay-container");
        }

        // Get video element
        const video = document.querySelector('video');

        // Video event listeners
        if (video) {
            // Time update listener
            video.addEventListener('timeupdate', function() {
                const position = video.currentTime;
                Android.onPositionChanged(position);
            });

            // Pause listener
            video.addEventListener('pause', hideOverlayElements);

            // Initial hide for paused state
            hideOverlayElements();

            // Play listener
            video.addEventListener('play', function() {
                setTimeout(hideOverlayElements, 500);

                // Set up periodic checks
                const checkElements = setInterval(function() {
                    const menuItems = document.getElementsByClassName("ytp-menuitem");
                    if (menuItems.length > 3) {
                        menuItems[4].style.display = 'none';
                        menuItems[5].style.display = 'none';
                        clearInterval(checkElements);
                    }

                    hideElementsByClassName("branding-img-container ytp-button");
                }, 500);

                // Check and hide related videos
                const checkRelatedVideos = setInterval(function() {
                    const relatedVideos = document.getElementsByClassName("ytp-endscreen-content");
                    if (relatedVideos.length > 0) {
                        hideElementsByClassName("ytp-endscreen-content");
                        clearInterval(checkRelatedVideos);
                    }
                }, 500);

                // Check and hide navigation buttons
                const checkButtons = setInterval(function() {
                    const buttonsToHide = [
                        "ytp-button ytp-endscreen-previous",
                        "ytp-button ytp-endscreen-next"
                    ];
                    
                    let found = false;
                    buttonsToHide.forEach(className => {
                        const elements = document.getElementsByClassName(className);
                        if (elements.length > 0) {
                            hideElementsByClassName(className);
                            found = true;
                        }
                    });

                    if (found) {
                        clearInterval(checkButtons);
                    }
                }, 500);
            });
        }
    """
                view?.evaluateJavascript(jsCode) { result ->
                    println("JavaScript Injection Result: $result")
                }

                progressBar.visibility = View.GONE
                webView.visibility = View.VISIBLE
            }
        }
    }

    private fun setupWebChromeClient(context: Context) {
        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
                if (customView != null) {
                    callback?.onCustomViewHidden()
                    return
                }

                // Fullscreen logic
                customView = view
                fullscreenContainer.addView(view)
                fullscreenContainer.visibility = View.VISIBLE
                webView.visibility = View.GONE

                // Assuming this is an Activity
                val activity = context as? AppCompatActivity
                activity?.supportActionBar?.hide()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ (API 30+)
                    activity?.window?.insetsController?.let { controller ->
                        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                } else {
                    // Android 10 and below
                    @Suppress("DEPRECATION")
                    activity?.window?.decorView?.systemUiVisibility = (
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                            )
                }

            }


            override fun onHideCustomView() {
                // Exit fullscreen
                if (customView == null) return

                fullscreenContainer.removeView(customView)
                customView = null
                fullscreenContainer.visibility = View.GONE

                val heightInDp = 250
                val heightInPixels = TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    heightInDp.toFloat(),
                    view.resources.displayMetrics
                ).toInt()
                mainLayout.layoutParams.height = heightInPixels
                webView.visibility = View.VISIBLE

                val activity = context as? AppCompatActivity
                activity?.supportActionBar?.show()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+ (API 30+)
                    activity?.window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                } else {
                    // Android 10 and below
                    @Suppress("DEPRECATION")
                    activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
                }
            }


        }
    }

    private fun setupPositionTracking() {
        playingPositionJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                webView.evaluateJavascript(
                    "document.querySelector('video').currentTime.toString();"
                ) { result ->
                    result?.toDoubleOrNull()?.let { position ->
                        methodChannel.invokeMethod("updatePosition", position)
                    }
                }
                delay(500)
            }
        }
    }

    fun saveState() {
        webViewBundle = Bundle().also { webView.saveState(it) }
    }

    fun restoreState() {
        webViewBundle?.let { webView.restoreState(it) }
    }

    override fun getView(): View = view

    override fun dispose() {
        playingPositionJob?.cancel()
    }
}

