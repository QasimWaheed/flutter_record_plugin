package com.hrl.flutter_record_plugin

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.io.IOException
import java.util.*

class FlutterRecordPlugin : FlutterPlugin, MethodChannel.MethodCallHandler {

    private var isRecording = false
    private val LOG_TAG = "AudioRecorder"
    private var mRecorder: MediaRecorder? = null
    private var mFilePath: String? = null
    private var startTime: Date? = null
    private var mExtension = ""
    private lateinit var channel: MethodChannel
    private lateinit var context: Context

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "flutter_record_plugin")
        channel.setMethodCallHandler(this)
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(this)
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: MethodChannel.Result) {
        when (call.method) {
            "start" -> {
                Log.d(LOG_TAG, "Start")
                val path = call.argument<String>("path")
                mExtension = call.argument<String>("extension") ?: ""
                startTime = Calendar.getInstance().time
                mFilePath = path ?: "${Environment.getExternalStorageDirectory().absolutePath}/${startTime!!.time}$mExtension"
                Log.d(LOG_TAG, mFilePath!!)
                startRecording()
                isRecording = true
                result.success(null)
            }
            "stop" -> {
                Log.d(LOG_TAG, "Stop")
                stopRecording()
                val duration = Calendar.getInstance().time.time - startTime!!.time
                Log.d(LOG_TAG, "Duration : $duration")
                isRecording = false
                val recordingResult = mapOf(
                    "duration" to duration,
                    "path" to mFilePath,
                    "audioOutputFormat" to mExtension
                )
                result.success(recordingResult)
            }
            "isRecording" -> {
                Log.d(LOG_TAG, "Get isRecording")
                result.success(isRecording)
            }
            "hasPermissions" -> {
                Log.d(LOG_TAG, "Get hasPermissions")
                val pm = context.packageManager
                val hasStoragePerm = pm.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, context.packageName)
                val hasRecordPerm = pm.checkPermission(Manifest.permission.RECORD_AUDIO, context.packageName)
                val hasPermissions = hasStoragePerm == PackageManager.PERMISSION_GRANTED && hasRecordPerm == PackageManager.PERMISSION_GRANTED
                result.success(hasPermissions)
            }
            else -> result.notImplemented()
        }
    }

    private fun startRecording() {
        if (isOutputFormatWav()) {
            startWavRecording()
        } else {
            startNormalRecording()
        }
    }

    private fun startNormalRecording() {
        mRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(getOutputFormatFromString(mExtension))
            setOutputFile(mFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                prepare()
                start()
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        }
    }

    private fun startWavRecording() {
        WavRecorder(context, mFilePath!!).startRecording()
    }

    private fun stopRecording() {
        if (isOutputFormatWav()) {
            stopWavRecording()
        } else {
            stopNormalRecording()
        }
    }

    private fun stopNormalRecording() {
        mRecorder?.apply {
            try {
                stop()
                reset()
                release()
            } catch (e: RuntimeException) {
                // Handle exception
            }
        }
        mRecorder = null
    }

    private fun stopWavRecording() {
        WavRecorder(context, mFilePath!!).stopRecording()
    }

    private fun getOutputFormatFromString(outputFormat: String): Int {
        return when (outputFormat) {
            ".mp4", ".aac", ".m4a" -> MediaRecorder.OutputFormat.MPEG_4
            else -> MediaRecorder.OutputFormat.MPEG_4
        }
    }

    private fun isOutputFormatWav(): Boolean {
        return mExtension == ".wav"
    }
}