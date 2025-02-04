import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class NoviYtPlayer extends StatefulWidget {
  final String url;
  final String title;

  const NoviYtPlayer({
    super.key,
    required this.url,
    required this.title,
  });

  @override
  State<NoviYtPlayer> createState() => _NoviYtPlayerState();
}

class _NoviYtPlayerState extends State<NoviYtPlayer> {
  final MethodChannel _channel = const MethodChannel('video_player');
  double _currentPosition = 0;
  Orientation _orientation = Orientation.portrait;

  @override
  void initState() {
    super.initState();
    _setupMethodCallHandler();
    _setInitialOrientation();
  }

  void _setInitialOrientation() {
    SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
    SystemChrome.setEnabledSystemUIMode(
      SystemUiMode.manual,
      overlays: SystemUiOverlay.values,
    );
  }

  void _setupMethodCallHandler() {
    _channel.setMethodCallHandler((call) async {
      if (!mounted) return;
      switch (call.method) {
        case 'updatePosition':
        case 'onPositionChanged':
          setState(() {
            _currentPosition = (call.arguments as double?) ?? 0;
          });
          break;
        case 'onStateChanged':
          setState(() {
            // Handle state changes if needed
          });
          break;
      }
    });
  }

  String? _extractYouTubeId(String url) {
    final RegExp regExp = RegExp(
      r'(?:https?:\/\/)?(?:www\.)?(?:youtube\.com\/(?:[^\/\n\s]+\/\S+\/|(?:v|e(?:mbed|live)?)\/|\S*?[?&]v=)|youtu\.be\/|youtube\.com\/live\/)([a-zA-Z0-9_-]{11})',
      caseSensitive: false,
      multiLine: false,
    );
    final Match? match = regExp.firstMatch(url);
    return match?.group(1);
  }

  void _toggleFullScreen() {
    if (_orientation == Orientation.portrait) {
      // Enter fullscreen
      SystemChrome.setEnabledSystemUIMode(SystemUiMode.immersiveSticky);
      SystemChrome.setPreferredOrientations([
        DeviceOrientation.landscapeRight,
        DeviceOrientation.landscapeLeft,
      ]);
      setState(() => _orientation = Orientation.landscape);
    } else {
      // Exit fullscreen
      SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
      SystemChrome.setEnabledSystemUIMode(
        SystemUiMode.manual,
        overlays: SystemUiOverlay.values,
      );
      setState(() => _orientation = Orientation.portrait);
    }
  }

  @override
  Widget build(BuildContext context) {
    final String? videoId = _extractYouTubeId(widget.url);
    final String embedUrl = videoId != null
        ? 'https://www.youtube.com/embed/$videoId?rel=0&autoplay=1'
        : widget.url;

    return PopScope(
      canPop: _orientation == Orientation.portrait,
      onPopInvoked: (didPop) {
        if (_orientation == Orientation.landscape) {
          _toggleFullScreen();
        }
      },
      child: Container(
        color: Colors.black,
        width: _orientation == Orientation.landscape
            ? MediaQuery.of(context).size.width
            : null,
        height: _orientation == Orientation.landscape
            ? MediaQuery.of(context).size.height
            : null,
        child: Stack(
          children: [
            Center(
              child: AspectRatio(
                aspectRatio: 16 / 9,
                child: AndroidView(
                  viewType: 'native_video_view',
                  creationParams: {
                    'url': embedUrl,
                    'title': widget.title,
                  },
                  creationParamsCodec: const StandardMessageCodec(),
                ),
              ),
            ),
            Positioned(
              bottom: 0,
              right: 7,
              child: IconButton(
                icon: Opacity(
                  opacity: 0,
                  child: Icon(
                    _orientation == Orientation.portrait
                        ? Icons.fullscreen
                        : Icons.fullscreen_exit,
                    color: Colors.white,
                    size: 30,
                  ),
                ),
                onPressed: _toggleFullScreen,
              ),
            ),
          ],
        ),
      ),
    );
  }

  @override
  void dispose() {
    _channel.setMethodCallHandler(null);
    SystemChrome.setPreferredOrientations([DeviceOrientation.portraitUp]);
    SystemChrome.setEnabledSystemUIMode(
      SystemUiMode.manual,
      overlays: SystemUiOverlay.values,
    );
    super.dispose();
  }
}
