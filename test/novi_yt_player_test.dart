import 'package:flutter_test/flutter_test.dart';
import 'package:novi_yt_player/novi_yt_player.dart';
import 'package:novi_yt_player/novi_yt_player_platform_interface.dart';
import 'package:novi_yt_player/novi_yt_player_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockNoviYtPlayerPlatform
    with MockPlatformInterfaceMixin
    implements NoviYtPlayerPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final NoviYtPlayerPlatform initialPlatform = NoviYtPlayerPlatform.instance;

  test('$MethodChannelNoviYtPlayer is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelNoviYtPlayer>());
  });

  test('getPlatformVersion', () async {
    NoviYtPlayer noviYtPlayerPlugin = NoviYtPlayer();
    MockNoviYtPlayerPlatform fakePlatform = MockNoviYtPlayerPlatform();
    NoviYtPlayerPlatform.instance = fakePlatform;

    expect(await noviYtPlayerPlugin.getPlatformVersion(), '42');
  });
}
