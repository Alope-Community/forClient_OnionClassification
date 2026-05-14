package com.afri.deteksibawang

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// =========================
// NAVY THEME COLOR
// =========================
private val Navy = Color(0xFF0D1B2A)
private val SoftBg = Color(0xFFF6F8FC)
private val AccentBlue = Color(0xFF1B4965)

data class AnalysisResult(
    val diseaseName: String,
    val accuracy: Float,
    val date: String,
    val symptoms: String,
    val cause: String,
    val treatment: String,
    val imageBitmap: Bitmap?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailAnalisisScreen(
    result: AnalysisResult,
    isHistory: Boolean,
    onBackClick: () -> Unit = {},
    onConfirm: () -> Unit = {},
) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Detail Analisis",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(SoftBg)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // =========================
            // IMAGE
            // =========================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {

                if (result.imageBitmap != null) {
                    Image(
                        bitmap = result.imageBitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================
            // RESULT CARD
            // =========================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {

                Column(modifier = Modifier.padding(20.dp)) {

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(modifier = Modifier.weight(1f)) {

                            Text(
                                "Hasil Deteksi",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )

                            Text(
                                result.diseaseName,
                                color = Navy,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        }

                        CircularProgressIndicator(
                            progress = { result.accuracy },
                            color = Navy,
                            trackColor = Color(0xFFE0E7EF),
                            strokeWidth = 6.dp,
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "${(result.accuracy * 100).toInt()}%",
                        color = AccentBlue,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    HorizontalDivider(color = Color(0xFFEAEFF5))

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Deteksi pada: ${result.date}",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // =========================
            // INFO SECTION
            // =========================
//            InfoSection("Gejala", result.symptoms, Icons.Default.Search, Navy)
//            InfoSection("Penyebab", result.cause, Icons.Default.BugReport, AccentBlue)
//            InfoSection("Penanganan", result.treatment, Icons.Default.MedicalServices, Color(0xFF2E7D32))

            Spacer(modifier = Modifier.height(24.dp))

            // =========================
            // BUTTON
            // =========================
//            Button(
//                onClick = onConfirm,
//                enabled = !isHistory,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(56.dp),
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = Navy
//                ),
//                shape = RoundedCornerShape(16.dp)
//            ) {
//                Text("Selesai", fontWeight = FontWeight.Bold)
//            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// =========================
// INFO SECTION
// =========================
@Composable
fun InfoSection(
    title: String,
    content: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {

        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Surface(
                    color = accentColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.padding(6.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    title,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                content,
                color = Color.DarkGray,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}