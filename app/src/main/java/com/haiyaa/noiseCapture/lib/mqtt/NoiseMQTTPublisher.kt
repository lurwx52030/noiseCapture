package com.haiyaa.noiseCapture.lib.mqtt

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.compose.runtime.MutableState
import org.eclipse.paho.mqttv5.client.IMqttToken
import org.eclipse.paho.mqttv5.client.MqttCallback
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttMessage
import org.eclipse.paho.mqttv5.common.packet.MqttProperties


class NoiseMQTTPublisher(
    val db: MutableState<Double>
) : IMQTTPublisherService, IMQTTAutoPublisher {

    companion object {
        val TAG = "6"
    }

    private val handlerPublish = object : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                1 -> {
                    publish("noise", db.value.toString())
                }
            }
        }
    }

    val taskPublish = object : Runnable {
        override fun run() {
            handlerPublish.sendEmptyMessage(1)

            //每1.5秒向MQTT BROKER Publish一次
            handlerPublish.postDelayed(this, 1500)
        }

    }

    private var client: MqttClient? = null

    val isConnected: Boolean
        get() = client != null

    private val callback: MqttCallback = object : MqttCallback {
        override fun disconnected(disconnectResponse: MqttDisconnectResponse) {
            Log.e(TAG, "disconnected: " + disconnectResponse.exception)
        }

        override fun mqttErrorOccurred(exception: MqttException) {
            Log.e(TAG, "mqttErrorOccurred: " + exception.localizedMessage)
        }

        @Throws(java.lang.Exception::class)
        override fun messageArrived(topic: String, message: MqttMessage) {
            Log.i(TAG, "messageArrived:" + String(message.payload))
            try {
                // todo messageArrived
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
                throw java.lang.Exception()
            }
        }

        override fun deliveryComplete(token: IMqttToken) {
            Log.i(TAG, "deliveryComplete")
        }

        override fun connectComplete(reconnect: Boolean, serverURI: String) {
            Log.i(
                TAG,
                "connectComplete: " + (if (reconnect) "reconnect to " else "connect to ") + serverURI
            )
        }

        override fun authPacketArrived(reasonCode: Int, properties: MqttProperties) {
            Log.i(TAG, "authPacketArrived ")
        }
    }

    override fun connect(broker: String) {
        val persistence = MemoryPersistence()

        try {
            val client_id = "Android Noise" + System.currentTimeMillis()
            client = MqttClient(broker, client_id, persistence)
            client!!.setCallback(callback)
            val connOpts = MqttConnectionOptions()
            connOpts.isCleanStart = true
            connOpts.isAutomaticReconnect = true
            connOpts.connectionTimeout = 30
            connOpts.keepAliveInterval = 20
            Log.i(TAG, "Connecting to broker: $broker")
            client!!.connect(connOpts)
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            throw e
        }
    }

    override fun subscribe(topic: String, qos: Int) {
        try {
            Log.d(TAG, "topic=${topic}, qos=${qos}")
            client?.subscribe(topic, qos)
        } catch (e: MqttException) {
            Log.e(TAG, topic + "subscribeFailed！" + e.message)
            throw e
        }
    }

    override fun unsubscribe(topic: String) {
        try {
            Log.d(TAG, "topic=${topic}")
            client?.unsubscribe(topic)
        } catch (e: MqttException) {
            Log.e(TAG, topic + "unSubscribeFailed！" + e.message)
            throw e
        }
    }

    override fun publish(topic: String, msg: String, qos: Int, retained: Boolean) {
        try {
            client?.publish(topic, msg.toByteArray(), 2, false)
            Log.i(TAG, "$topic:$msg")
        } catch (e: MqttException) {
            Log.e(TAG, topic + "publishFailed" + e.message)
            throw e
        }
    }

    override fun disconnect() {
        try {
            client?.disconnect()
            client = null
        } catch (e: MqttException) {
            Log.e(TAG, "disconnect failed:" + e.message)
            throw e
        }
    }

    override fun start() {
        try {
            handlerPublish.post(taskPublish)
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            throw e
        }

    }

    override fun end() {
        try {
            handlerPublish.removeCallbacks(taskPublish)
        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
            throw e
        }
    }

}