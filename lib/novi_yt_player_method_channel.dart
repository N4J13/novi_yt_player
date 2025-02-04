import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'novi_yt_player_platform_interface.dart';

/// An implementation of [NoviYtPlayerPlatform] that uses method channels.
class MethodChannelNoviYtPlayer extends NoviYtPlayerPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('novi_yt_player');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
