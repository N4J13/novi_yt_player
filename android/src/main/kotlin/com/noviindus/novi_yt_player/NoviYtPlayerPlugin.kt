package com.noviindus.novi_yt_player

import android.content.Context
import androidx.annotation.NonNull

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.platform.PlatformViewRegistry

/** NoviYtPlayerPlugin */
class NoviYtPlayerPlugin: FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private  lateinit var context : Context
  // This is to register PlatformViewRegistry that helps to show native views on flutter side
  private  lateinit var registry: PlatformViewRegistry


  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {

    // Assign Context to flutter plugin context
    context =  flutterPluginBinding.applicationContext
    // Assign platform view registry from flutter
    registry = flutterPluginBinding.platformViewRegistry
    
    // Creating and assigning method channel
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "novi_yt_player")
    channel.setMethodCallHandler(this)

    // Register the native view factory
    registry.registerViewFactory(
      "native_video_view",
      NativeVideoViewFactory(flutterPluginBinding.binaryMessenger)
    )
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }
}
