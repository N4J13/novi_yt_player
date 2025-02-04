import 'package:flutter/material.dart';
import 'package:novi_yt_player/novi_yt_player.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
        ),
        body: const AspectRatio(
          aspectRatio: 16 / 9,
          child: NoviYtPlayer(
            url: 'https://youtu.be/tjnXUQvLzD8?si=S8QTTZJqJhpPgQtm',
            title: 'Flutter YouTube Player',
          ),
        ),
      ),
    );
  }
}
