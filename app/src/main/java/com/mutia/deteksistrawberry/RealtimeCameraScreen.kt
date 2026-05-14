package com.mutia.deteksistrawberry

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun RealtimeCameraScreen(onBack: () -> Unit) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var result by remember { mutableStateOf<AnalysisResult?>(null) }

    val executor = remember { Executors.newSingleThreadExecutor() }
    val detector = remember { CNNDetector(context) }

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            cameraProvider?.unbindAll()
            executor.shutdownNow()
            detector.close()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // =========================
        // CAMERA PREVIEW
        // =========================
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->

                PreviewView(ctx).apply {

                    val cameraProviderFuture =
                        ProcessCameraProvider.getInstance(ctx)

                    cameraProviderFuture.addListener({

                        val provider = cameraProviderFuture.get()
                        cameraProvider = provider

                        val preview = Preview.Builder().build()
                        preview.setSurfaceProvider(this.surfaceProvider)

                        val imageAnalysis = ImageAnalysis.Builder()
                            .setBackpressureStrategy(
                                ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                            )
                            .build()

                        imageAnalysis.setAnalyzer(executor) { imageProxy ->

                            try {

                                val bitmap = imageProxy.toBitmap()
                                val rotated = rotateBitmap(
                                    bitmap,
                                    imageProxy.imageInfo.rotationDegrees.toFloat()
                                )

                                // =========================
                                // CLASSIFICATION ONLY
                                // =========================
                                val prediction = detector.detect(rotated)

                                if (prediction != null) {

                                    val info = DiseaseData.getInfo(
                                        prediction.label.replace("_", " ")
                                    )

                                    result = AnalysisResult(
                                        diseaseName = prediction.label,
                                        accuracy = prediction.score,
                                        date = "",
                                        symptoms = info.first,
                                        cause = info.second,
                                        treatment = info.third,
                                        imageBitmap = rotated
                                    )

                                } else {
                                    result = null
                                }

                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                imageProxy.close()
                            }
                        }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            provider.unbindAll()
                            provider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }, ContextCompat.getMainExecutor(ctx))
                }
            }
        )

        // =========================
        // RESULT UI
        // =========================
        result?.let {

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 80.dp)
                    .background(
                        Color.Black.copy(alpha = 0.6f),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    text = it.diseaseName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Text(
                    text = "Akurasi: ${(it.accuracy * 100).toInt()}%",
                    color = Color.Green,
                    fontSize = 16.sp
                )
            }
        }

        // =========================
        // BACK BUTTON
        // =========================
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
    }
}

// =========================
// ROTATE BITMAP
// =========================
fun rotateBitmap(bitmap: Bitmap, degrees: Float): Bitmap {
    if (degrees == 0f) return bitmap
    val matrix = Matrix()
    matrix.postRotate(degrees)
    return Bitmap.createBitmap(
        bitmap,
        0,
        0,
        bitmap.width,
        bitmap.height,
        matrix,
        true
    )
}