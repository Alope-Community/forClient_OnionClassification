package com.mutia.deteksistrawberry

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeOp.ResizeMethod

class CNNDetector(private val context: Context) {

    private var interpreter: Interpreter? = null
    private var labels = listOf<String>()

    private var inputWidth = 224
    private var inputHeight = 224
    private var numClasses = 0

    init {

        val model = FileUtil.loadMappedFile(
            context,
            "model_penyakit_bawang.tflite"
        )

        interpreter = Interpreter(model)

        val inputShape = interpreter!!.getInputTensor(0).shape()

        inputHeight = inputShape[1]
        inputWidth = inputShape[2]

        val outputShape = interpreter!!.getOutputTensor(0).shape()

        // contoh output: [1, 2]
        numClasses = outputShape[1]

        labels = FileUtil.loadLabels(
            context,
            "labels.txt"
        )

        Log.d(
            "CNNDetector",
            "Input Shape: ${inputShape.contentToString()}"
        )

        Log.d(
            "CNNDetector",
            "Output Shape: ${outputShape.contentToString()}"
        )

        Log.d(
            "CNNDetector",
            "Labels: $labels"
        )
    }

    @Synchronized
    fun detect(bitmap: Bitmap): Detection? {

        val currentInterpreter = interpreter ?: return null

        val imageProcessor = ImageProcessor.Builder()
            .add(
                ResizeOp(
                    inputHeight,
                    inputWidth,
                    ResizeMethod.BILINEAR
                )
            )
            .add(NormalizeOp(0f, 255f))
            .build()

        var tensorImage = TensorImage(DataType.FLOAT32)

        tensorImage.load(bitmap)

        tensorImage = imageProcessor.process(tensorImage)

        // output [1][numClasses]
        val output = Array(1) {
            FloatArray(numClasses)
        }

        currentInterpreter.run(
            tensorImage.buffer,
            output
        )

        val scores = output[0]

        Log.d(
            "CNNDetector",
            "Scores: ${scores.contentToString()}"
        )

        var maxScore = -1f
        var classId = -1

        for (i in scores.indices) {

            if (scores[i] > maxScore) {
                maxScore = scores[i]
                classId = i
            }
        }

        if (classId == -1) return null

        val label = if (classId < labels.size) {
            labels[classId]
        } else {
            "Unknown"
        }

        return Detection(
            label = label,
            score = maxScore
        )
    }

    @Synchronized
    fun close() {
        interpreter?.close()
        interpreter = null
    }

    data class Detection(
        val label: String,
        val score: Float
    )
}