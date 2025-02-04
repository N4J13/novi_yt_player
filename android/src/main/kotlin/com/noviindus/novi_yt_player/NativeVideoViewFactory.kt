package com.noviindus.novi_yt_player

import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class NativeVideoViewFactory(private val messenger: BinaryMessenger) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    override fun create(context: Context, id: Int, creationParams: Any?): PlatformView {
        return NativeVideoView(
            context,
            messenger,
            id,
            creationParams as Map<*, *>?
        )
    }
}