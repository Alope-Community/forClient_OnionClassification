package com.afri.deteksibawang

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import java.text.SimpleDateFormat
import java.util.*

class home : Fragment() {

    private lateinit var imgPreview: ImageView
    private lateinit var CNNDetector: CNNDetector

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_home, container, false)

        imgPreview = view.findViewById(R.id.imgPreview)
        CNNDetector = CNNDetector(requireContext())

        val btnCamera = view.findViewById<MaterialButton>(R.id.btnCamera)
        val btnGallery = view.findViewById<MaterialButton>(R.id.btnGallery)
        val btnAnalisis = view.findViewById<MaterialButton>(R.id.btnAnalisis)
        val cardPreview = view.findViewById<View>(R.id.cardPreview)

        // =========================
        // CAMERA RESULT
        // =========================
        val cameraLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val bitmap = result.data?.extras?.get("data") as? Bitmap
                    if (bitmap != null) {
                        showImage(cardPreview)
                        imgPreview.setImageBitmap(bitmap)
                    }
                }
            }

        // =========================
        // GALLERY RESULT
        // =========================
        val galleryLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                if (uri != null) {
                    showImage(cardPreview)
                    imgPreview.setImageURI(uri)
                }
            }

        btnCamera.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraLauncher.launch(intent)
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        // =========================
        // ANALISIS BUTTON
        // =========================
        btnAnalisis.setOnClickListener {

            val drawable = imgPreview.drawable

            if (drawable == null) {
                Toast.makeText(requireContext(), "Ambil gambar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val bitmap = (drawable as BitmapDrawable).bitmap

            // 🔥 FIX: detect itu SINGLE result, bukan list
            val result = CNNDetector.detect(bitmap)

            if (result == null) {
                Toast.makeText(
                    requireContext(),
                    "Tidak ada penyakit terdeteksi",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val info = DiseaseData.getInfo(
                result.label.replace("_", " ")
            )

            val analysisResult = AnalysisResult(
                diseaseName = result.label,
                accuracy = result.score,
                date = SimpleDateFormat(
                    "dd MMMM yyyy",
                    Locale.getDefault()
                ).format(Date()),
                symptoms = info.first,
                cause = info.second,
                treatment = info.third,
                imageBitmap = bitmap
            )

            // simpan ke holder
            AnalysisDataHolder.analysisResult = analysisResult
            AnalysisDataHolder.isHistory = false
            AnalysisDataHolder.imageBitmap = bitmap

            startActivity(
                Intent(requireContext(), DetailAnalisisActivity::class.java)
            )
        }

        return view
    }

    // =========================
    // SHOW IMAGE CARD
    // =========================
    private fun showImage(cardPreview: View) {
        imgPreview.visibility = View.VISIBLE
        cardPreview.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        super.onDestroy()
        CNNDetector.close()
    }
}