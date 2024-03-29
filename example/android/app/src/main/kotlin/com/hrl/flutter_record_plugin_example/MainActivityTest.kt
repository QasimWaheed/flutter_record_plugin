package com.hrl.flutter_record_plugin_example

import androidx.test.rule.ActivityTestRule
import io.flutter.plugins.firebasecoreexample.MainActivity
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(FlutterRunner::class)
class MainActivityTest {
    // Replace `MainActivity` with `io.flutter.embedding.android.FlutterActivity` if you removed `MainActivity`.
    @Rule
    var rule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)
}