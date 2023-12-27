package com.haiyaa.noiseCapture.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.haiyaa.noiseCapture.lib.recorder.NoiseDBRecorder
import com.haiyaa.noiseCapture.ui.theme.NoiseCaptureTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun NoiseDBCard(db: MutableState<Double>?=null, snackbarHostState: SnackbarHostState? =null) {
    val context = LocalContext.current

    val recordAudioPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO
    )

    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val permissionsGranted = permissions.values.reduce { acc, isPermissionGranted ->
                acc && isPermissionGranted
            }

            if (!permissionsGranted) {
                return@rememberLauncherForActivityResult
            }
        }
    )


    val recorder by lazy {
        NoiseDBRecorder(context, db!!)
    }

    val isRecording = remember {
        mutableStateOf(false)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp,0.dp,0.dp,10.dp)
            .height(170.dp)
    ) {
        Text(
            text = "噪音偵測",
            modifier = Modifier.padding(15.dp),
            textAlign = TextAlign.Center,
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${"%.2f".format(db!!.value)} db",
                fontSize = 50.sp,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
                    .padding(15.dp, 0.dp, 0.dp, 0.dp)
            )

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Button(onClick = {
                    isRecording.value = true

                    when (PackageManager.PERMISSION_GRANTED) {
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) -> {
                            recorder.start()
                            CoroutineScope(Dispatchers.IO).launch {
                                snackbarHostState!!.showSnackbar(message = "偵測中...")
                            }
                        }

                        else -> {
                            recordAudioPermissionLauncher.launch(recordAudioPermissions)
                        }
                    }
                }, enabled = !isRecording.value) {
                    Text(text = "開始偵測")
                }
                Button(
                    onClick = {
                        isRecording.value = false

                        recorder.stop()
                        CoroutineScope(Dispatchers.IO).launch {
                            snackbarHostState!!.showSnackbar(message = "偵測結束...")
                        }
                    },
                    enabled = isRecording.value
                ) {
                    Text(text = "結束偵測")
                }
            }
        }
    }
}

fun startMeasure() {
    val mediaRecorder = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        setOutputFile("/dev/null")
        setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    }

    try {
        mediaRecorder.prepare()
        mediaRecorder.start()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Preview(showBackground = true)
@Composable
fun NoiseDBCardPreview() {
    NoiseCaptureTheme {
        NoiseDBCard()
    }
}