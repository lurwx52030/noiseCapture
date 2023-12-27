package com.haiyaa.noiseCapture.lib.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.compose.runtime.MutableState
import java.io.File
import kotlin.math.roundToInt

class NoiseDBRecorder(private val context: Context, val DB: MutableState<Double>) : IAudioRecorder {
    private var recorder: MediaRecorder? = null

    var db: Double = 0.0

    val handlerMeasure = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    val amp = recorder?.maxAmplitude
                    // 公式：Gdb = 20log10(V1/V0)
                    // Google已提供方法幫你取得麥克風的檢測電壓(V1)以及參考電壓(V0)
                    db = 20 * (amp!!.let { Math.abs(it.toDouble()) }.let { Math.log10(it) })
                    Log.d("6", db.toString())

                    // if -Infinity
                    if (db == Double.NEGATIVE_INFINITY) {
                        db = 0.0
                    }

                    DB.value=db
                }
            }
            super.handleMessage(msg)
        }
    }

    val taskMeasure = object : Runnable {
        override fun run() {
            handlerMeasure.sendEmptyMessage(1)

            //每1秒抓取一次檢測結果
            handlerMeasure.postDelayed(this, 1000)
        }
    }

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override fun start() {
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            val tmpRecordingFolder = File(context.filesDir, "tmp_media")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                setOutputFile(tmpRecordingFolder)
            } else {
                setOutputFile("/dev/null")
            }

            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
                start()
                handlerMeasure.post(taskMeasure)

                recorder = this
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun stop() {
        handlerMeasure.removeCallbacks(taskMeasure)
        try {
            recorder?.release()
            recorder?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}