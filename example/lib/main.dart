import 'dart:io' as io;
import 'dart:math';

import 'package:flutter_record_plugin/flutter_record_plugin.dart';
import 'package:file/file.dart';
import 'package:file/local.dart';
import 'package:flutter/material.dart';
import 'package:path_provider/path_provider.dart';

import 'package:permission_handler/permission_handler.dart';



void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
        appBar: new AppBar(
          title: new Text('Plugin audio recorder'),
        ),
        body: new AppBody(),
      ),
    );
  }
}

class AppBody extends StatefulWidget {
  final LocalFileSystem localFileSystem;

  AppBody({localFileSystem})
      : this.localFileSystem = localFileSystem ?? LocalFileSystem();

  @override
  State<StatefulWidget> createState() => new AppBodyState();
}

class AppBodyState extends State<AppBody> {
  Recording _recording = Recording();
  bool _isRecording = false;
  Random random = Random();
  TextEditingController _controller = TextEditingController();

  @override
  Widget build(BuildContext context) {
    return Center(
      child: Padding(
        padding: EdgeInsets.all(8.0),
        child: Column(
            mainAxisAlignment: MainAxisAlignment.spaceAround,
            children: <Widget>[
              TextButton(
                onPressed: _isRecording ? _start : _start,
                child: Text("Start"),
                style: TextButton.styleFrom(
                    backgroundColor: Colors.green,
                ),
              ),
              TextButton(
                onPressed: _isRecording ? _stop : _stop,
                child: Text("Stop"),
                style: TextButton.styleFrom(
                  backgroundColor: Colors.red,
                ),
              ),
              TextField(
                controller: _controller,
                decoration: InputDecoration(
                  hintText: 'Enter a custom path',
                ),
              ),
              Text("File path of the record: ${_recording.path}"),
              Text("Format: ${_recording.audioOutputFormat}"),
              Text("Extension : ${_recording.extension}"),
              Text(
                  "Audio recording duration : ${_recording.duration.toString()}")
            ]),
      ),
    );
  }

  _start() async {
    try {
      if (await FlutterRecordPlugin.hasPermissions) {
        if (_controller.text.trim().isNotEmpty) {
          String path = _controller.text;
          if (!_controller.text.contains('/')) {
            io.Directory appDocDirectory =
            await getApplicationDocumentsDirectory();
            path = appDocDirectory.path + '/' +  DateTime.now().millisecondsSinceEpoch.toString();
          }
          print("Start recording: $path");
          await FlutterRecordPlugin.start(
              path: path, audioOutputFormat: AudioOutputFormat.AAC);
        } else {
          await FlutterRecordPlugin.start();
        }
        bool isRecording = await FlutterRecordPlugin.isRecording;
        setState(() {
          _recording = Recording(duration: Duration(), path: "");
          _isRecording = isRecording;
        });

        _stop();

      } else {
        ScaffoldMessenger.of(context).showSnackBar(
            SnackBar(content: Text("You must accept permissions")));

        requestPermission();
      }
    } catch (e) {
      print(e);
    }
  }

  Future requestPermission() async {

    // 申请权限

    Map<Permission, PermissionStatus> statuses = await [
      Permission.storage,
      Permission.microphone,
    ].request();

    // 申请结果



    if (statuses[Permission.storage] == PermissionStatus.granted) {

      //  Fluttertoast.showToast(msg: "权限申请通过");

    } else {

      //Fluttertoast.showToast(msg: "权限申请被拒绝");

    }

  }


  _stop() async {
    var recording = await FlutterRecordPlugin.stop();
    print("Stop recording: ${recording.path}");
    bool isRecording = await FlutterRecordPlugin.isRecording;
    File file = widget.localFileSystem.file(recording.path);
    print("  File length: ${await file.length()}");
    setState(() {
      _recording = recording;
      _isRecording = isRecording;
    });
   // _controller.text = recording.path;
  }
}