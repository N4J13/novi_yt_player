import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'novi_yt_player_method_channel.dart';

abstract class NoviYtPlayerPlatform extends PlatformInterface {
  /// Constructs a NoviYtPlayerPlatform.
  NoviYtPlayerPlatform() : super(token: _token);

  static final Object _token = Object();

  static NoviYtPlayerPlatform _instance = MethodChannelNoviYtPlayer();

  /// The default instance of [NoviYtPlayerPlatform] to use.
  ///
  /// Defaults to [MethodChannelNoviYtPlayer].
  static NoviYtPlayerPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [NoviYtPlayerPlatform] when
  /// they register themselves.
  static set instance(NoviYtPlayerPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
