package com.haiyaa.noiseCapture.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.haiyaa.noiseCapture.lib.mqtt.NoiseMQTTPublisher
import com.haiyaa.noiseCapture.ui.theme.NoiseCaptureTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.Inet4Address

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MqttServiceCard(
    db: MutableState<Double>? = null,
    snackbarHostState: SnackbarHostState? = null
) {
    val context = LocalContext.current

    val mqttHost = remember {
        mutableStateOf("")
    }

    val mqttPort = remember {
        mutableStateOf("")
    }

    val NoiseMQTTPublisher by lazy {
        NoiseMQTTPublisher(db!!)
    }

    val isConnected = remember {
        mutableStateOf(false)
    }

    val isAutoPublishing = remember {
        mutableStateOf(false)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
    ) {
        Text(
            text = "MQTT - 分貝資料之Publish",
            modifier = Modifier.padding(15.dp),
            textAlign = TextAlign.Center,
        )

        TextField(
            label = { Text(text = "MQTT BROKER HOST") },
            value = mqttHost.value,
            singleLine = true,
            onValueChange = { a ->
                mqttHost.value = a
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp, 5.dp),
        )

        TextField(
            label = { Text(text = "MQTT BROKER PORT") },
            value = mqttPort.value,
            singleLine = true,
            onValueChange = { a -> mqttPort.value = a },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp, 5.dp),
        )

        Column(modifier = Modifier.padding(5.dp)) {
            Row(
                modifier = Modifier
                    .padding(0.dp, 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.size(160.dp, 45.dp),
                    onClick = {
                        try {
                            val uri = "tcp://${mqttHost.value}:${mqttPort.value}"
                            NoiseMQTTPublisher.connect(uri)

                            isConnected.value = true
                        } catch (e: Exception) {
                            CoroutineScope(Dispatchers.IO).launch {
                                snackbarHostState!!.showSnackbar(message = "連接失敗，請檢查broker的ip address與port...")
                            }
                        }
                    },
                    enabled = !isConnected.value
                ) {
                    Text(text = "連接BROKER")
                }
                Button(
                    modifier = Modifier.size(160.dp, 45.dp),
                    onClick = {
                        NoiseMQTTPublisher.disconnect()
                        isConnected.value = false
                    },
                    enabled = isConnected.value
                ) {
                    Text(text = "斷開連接BROKER")
                }
            }

            Row(
                modifier = Modifier
                    .padding(0.dp, 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    modifier = Modifier.size(160.dp, 45.dp),
                    onClick = {
                        try {
                            if (NoiseMQTTPublisher.isConnected) {
                                NoiseMQTTPublisher.start()
                                isAutoPublishing.value = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    snackbarHostState!!.showSnackbar(message = "開始自動把分貝傳回broker...")
                                }
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    snackbarHostState!!.showSnackbar(message = "尚未連線...")
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("6", e.toString())
                            CoroutineScope(Dispatchers.IO).launch {
                                snackbarHostState!!.showSnackbar(message = "發生錯誤，以致不能自動把分貝傳回broker...")
                            }
                        }
                    },
                    enabled = !isAutoPublishing.value
                ) {
                    Text(text = "開始自動Publish")
                }
                Button(
                    modifier = Modifier.size(160.dp, 45.dp),
                    onClick = {
                        try {
                            if (NoiseMQTTPublisher.isConnected) {
                                NoiseMQTTPublisher.end()
                                isAutoPublishing.value = false
                                CoroutineScope(Dispatchers.IO).launch {
                                    snackbarHostState!!.showSnackbar(message = "停止自動把分貝傳回broker...")
                                }
                            } else {
                                CoroutineScope(Dispatchers.IO).launch {
                                    snackbarHostState!!.showSnackbar(message = "尚未連線...")
                                }
                            }
                        } catch (e: Exception) {
                            Log.d("6", e.toString())
                            CoroutineScope(Dispatchers.IO).launch {
                                snackbarHostState!!.showSnackbar(message = "發生錯誤，以致停止自動把分貝傳回broker...")
                            }
                        }
                    },
                    enabled = isAutoPublishing.value
                ) {
                    Text(text = "停止自動Publish")
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun MqttServiceCardPreview() {
    NoiseCaptureTheme {
        MqttServiceCard()
    }
}