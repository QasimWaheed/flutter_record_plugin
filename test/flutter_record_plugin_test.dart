import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_record_plugin/flutter_record_plugin.dart';

void main() {
  const MethodChannel channel = MethodChannel('flutter_record_plugin');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    //expect(await FlutterRecordPlugin.platformVersion, '42');
  });
}
