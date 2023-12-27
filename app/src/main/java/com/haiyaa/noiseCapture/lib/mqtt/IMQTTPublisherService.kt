package com.haiyaa.noiseCapture.lib.mqtt

import android.content.Context

interface IMQTTPublisherService {
    // 連接
    fun connect(serverURI: String)

    // 訂閱
    fun subscribe(topic: String, qos: Int = 1)

    // 取消訂閱
    fun unsubscribe(topic: String)

    // 發布
    fun publish(topic: String, msg: String, qos: Int = 1, retained: Boolean = false)

    // 斷連
    fun disconnect()
}