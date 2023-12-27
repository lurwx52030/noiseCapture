package com.haiyaa.noiseCapture.lib.mqtt

interface IMQTTAutoPublisher {
    // 開始自動發送
    fun start()

    // 停止自動發送
    fun end()
}