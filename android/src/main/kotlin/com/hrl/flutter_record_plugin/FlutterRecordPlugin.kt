package com.hrl.flutter_record_plugin

import androidx.annotation.NonNull

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Environment
import android.util.Log
import java.io.IOException
import java.util.Calendar
import java.util.Date
import java.util.HashMap
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

/** FlutterRecordPlugin  */
class FlutterRecordPlugin private constructor(registrar: Registrar) : FlutterPlugin, MethodCallHandler {
    private val registrar: Registrar
    private var isRecording = false
    private var mRecorder: MediaRecorder? = null
    private var startTime: Date? = null
    private var mExtension = ""
    private var wavRecorder: WavRecorder? = null

    init {
        this.registrar = registrar
    }

    @Override
    fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "start" -> {
                Log.d(LOG_TAG, "Start")
                val path: String = call.argument("path")
                mExtension = call.argument("extension")
                startTime = Calendar.getInstance().getTime()
                if (path != null) {
                    mFilePath = path
                } else {
                    val fileName: String = String.valueOf(startTime.getTime())
                    mFilePath = Environment.getExternalStorageDirectory()
                        .getAbsolutePath() + "/" + fileName + mExtension
                }
                Log.d(LOG_TAG, mFilePath)
                startRecording()
                isRecording = true
                result.success(null)
            }

            "stop" -> {
                Log.d(LOG_TAG, "Stop")
                stopRecording()
                val duration: Long =
                    Calendar.getInstance().getTime().getTime() - startTime.getTime()
                Log.d(LOG_TAG, "Duration : " + String.valueOf(duration))
                isRecording = false
                val recordingResult: HashMap<String, Object> = HashMap()
                recordingResult.put("duration", duration)
                recordingResult.put("path", mFilePath)
                recordingResult.put("audioOutputFormat", mExtension)
                result.success(recordingResult)
            }

            "isRecording" -> {
                Log.d(LOG_TAG, "Get isRecording")
                result.success(isRecording)
            }

            "hasPermissions" -> {
                Log.d(LOG_TAG, "Get hasPermissions")
                val context: Context = registrar.context()
                val pm: PackageManager = context.getPackageManager()
                val hasStoragePerm: Int = pm.checkPermission(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    context.getPackageName()
                )
                val hasRecordPerm: Int =
                    pm.checkPermission(Manifest.permission.RECORD_AUDIO, context.getPackageName())
                val hasPermissions =
                    hasStoragePerm == PackageManager.PERMISSION_GRANTED && hasRecordPerm == PackageManager.PERMISSION_GRANTED
                result.success(hasPermissions)
            }

            else -> result.notImplemented()
        }
    }

    private fun startRecording() {
        if (isOutputFormatWav) {
            startWavRecording()
        } else {
            startNormalRecording()
        }
    }

    private fun startNormalRecording() {
        mRecorder = MediaRecorder()
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mRecorder.setOutputFormat(getOutputFormatFromString(mExtension))
        mRecorder.setOutputFile(mFilePath)
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        try {
            mRecorder.prepare()
        } catch (e: IOException) {
            Log.e(LOG_TAG, "prepare() failed")
        }
        mRecorder.start()
    }

    private fun startWavRecording() {
        wavRecorder = WavRecorder(registrar.context(), mFilePath)
        wavRecorder.startRecording()
    }

    private fun stopRecording() {
        if (isOutputFormatWav) {
            stopWavRecording()
        } else {
            stopNormalRecording()
        }
    }

    private fun stopNormalRecording() {
        if (mRecorder != null) {
            try {
                mRecorder.stop()
                mRecorder.reset()
                mRecorder.release()
                mRecorder = null
            } catch (e: RuntimeException) {
            }
        }
    }

    private fun stopWavRecording() {
        wavRecorder.stopRecording()
    }

    private fun getOutputFormatFromString(outputFormat: String): Int {
        return when (outputFormat) {
            ".mp4", ".aac", ".m4a" -> MediaRecorder.OutputFormat.MPEG_4
            else -> MediaRecorder.OutputFormat.MPEG_4
        }
    }

    private val isOutputFormatWav: Boolean
        private get() = mExtension.equals(".wav")

    companion object {
        private const val LOG_TAG = "AudioRecorder"
        private var mFilePath: String? = null

        /** Plugin registration.  */
        fun registerWith(registrar: Registrar) {
            val channel = MethodChannel(registrar.messenger(), "flutter_record_plugin")
            channel.setMethodCallHandler(FlutterRecordPlugin(registrar))
        }
    }
}
